package com.lockguard.app.data.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.lockguard.app.data.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.coroutines.resume

/**
 * Manages CameraX front-facing capture for unauthorized authentication events.
 *
 * Security & Architectural Compliance:
 * - Operates only when explicit CAMERA permission has been granted.
 * - Binds solely the ImageCapture use case to lifecycle (no preview surface required).
 * - Rotates image according to device orientation and sensor metadata.
 * - Encrypts raw photo bytes immediately using AES-256-GCM via [CryptoManager].
 * - Plaintext photo bytes never touch persistent disk storage.
 * - Follows Android background camera restrictions: captures when app is in foreground
 *   or active authentication context.
 */
class CameraCaptureManager(
    private val context: Context,
    private val cryptoManager: CryptoManager
) {

    /**
     * Checks if the device has granted runtime CAMERA permission.
     */
    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if device hardware possesses a front-facing camera.
     */
    suspend fun hasFrontCamera(): Boolean = withContext(Dispatchers.Main) {
        val cameraProvider = getCameraProvider() ?: return@withContext false
        cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
    }

    /**
     * Captures an intruder photo using the front camera and encrypts it immediately.
     *
     * @param lifecycleOwner The active UI lifecycle owner (e.g., MainActivity or AppLockActivity)
     * @return Result containing the encrypted file path or failure exception
     */
    suspend fun captureIntruderPhoto(lifecycleOwner: LifecycleOwner): Result<String> {
        if (!hasCameraPermission()) {
            return Result.failure(SecurityException("Camera permission has not been granted by user."))
        }

        return withContext(Dispatchers.Main) {
            val cameraProvider = getCameraProvider()
                ?: return@withContext Result.failure(IllegalStateException("ProcessCameraProvider unavailable."))

            val cameraSelector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                return@withContext Result.failure(IllegalStateException("No camera sensor detected on this device."))
            }

            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(android.view.Surface.ROTATION_0)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture)

                // Capture image in memory
                val rawBytes = takePictureBytes(imageCapture)

                // Unbind camera immediately after capture to release hardware resources
                cameraProvider.unbindAll()

                // Encrypt bytes and write to app private storage
                val encryptedFilePath = cryptoManager.encryptAndSavePhoto(rawBytes)
                Result.success(encryptedFilePath)
            } catch (e: Exception) {
                cameraProvider.unbindAll()
                Result.failure(e)
            }
        }
    }

    private suspend fun takePictureBytes(imageCapture: ImageCapture): ByteArray =
        suspendCancellableCoroutine { continuation ->
            val executor = ContextCompat.getMainExecutor(context)

            imageCapture.takePicture(
                executor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        try {
                            val rotatedBytes = processAndRotateImage(image)
                            image.close()
                            if (continuation.isActive) {
                                continuation.resume(rotatedBytes)
                            }
                        } catch (e: Exception) {
                            image.close()
                            if (continuation.isActive) {
                                continuation.resumeWith(Result.failure(e))
                            }
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        if (continuation.isActive) {
                            continuation.resumeWith(Result.failure(exception))
                        }
                    }
                }
            )
        }

    /**
     * Converts ImageProxy into compressed JPEG bytes, rotating if necessary.
     */
    private fun processAndRotateImage(image: ImageProxy): ByteArray {
        val plane = image.planes[0]
        val buffer: ByteBuffer = plane.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val rotationDegrees = image.imageInfo.rotationDegrees
        if (rotationDegrees == 0) {
            return bytes
        }

        val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
            // Front camera mirror correction if needed
            postScale(-1f, 1f, originalBitmap.width / 2f, originalBitmap.height / 2f)
        }

        val rotatedBitmap = Bitmap.createBitmap(
            originalBitmap,
            0,
            0,
            originalBitmap.width,
            originalBitmap.height,
            matrix,
            true
        )

        val outputStream = ByteArrayOutputStream()
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        return outputStream.toByteArray()
    }

    private suspend fun getCameraProvider(): ProcessCameraProvider? =
        suspendCancellableCoroutine { continuation ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val provider = cameraProviderFuture.get()
                    continuation.resume(provider)
                } catch (e: Exception) {
                    continuation.resume(null)
                }
            }, ContextCompat.getMainExecutor(context))
        }
}
