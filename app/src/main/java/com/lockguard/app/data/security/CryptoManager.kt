package com.lockguard.app.data.security

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Handles AES-256-GCM hardware-backed encryption using the Android Keystore system.
 *
 * Privacy Guarantees:
 * - Master encryption key is stored securely in the hardware-backed Android KeyStore.
 * - Photos are encrypted with AES/GCM/NoPadding before being written to disk.
 * - Encrypted files are stored in internal, app-private storage (context.filesDir).
 * - No plain-text photos are ever written to unencrypted storage or external directories.
 */
class CryptoManager(private val context: Context) {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "LockGuardPhotoKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH_BYTES = 12
        private const val GCM_TAG_LENGTH_BITS = 128
        private const val PHOTOS_DIR = "intruder_photos"
        private const val SHARED_CACHE_DIR = "shared_photos"
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    init {
        ensureKeyExists()
        // Ensure private directories exist
        getPhotosDirectory()
        getSharedCacheDirectory()
    }

    /**
     * Retrieves or generates an AES-256 key inside the Android Keystore.
     */
    private fun ensureKeyExists() {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val keyGenSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build()

            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    private fun getPhotosDirectory(): File {
        val dir = File(context.filesDir, PHOTOS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun getSharedCacheDirectory(): File {
        val dir = File(context.cacheDir, SHARED_CACHE_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Encrypts raw image bytes and saves them to a private app file.
     * File structure: [12 bytes IV][Ciphertext + 16 bytes GCM Auth Tag]
     *
     * @param rawBytes Captured JPEG bytes from CameraX
     * @return Absolute path to the encrypted file
     */
    suspend fun encryptAndSavePhoto(rawBytes: ByteArray): String = withContext(Dispatchers.IO) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv

        val encryptedBytes = cipher.doFinal(rawBytes)

        val fileName = "intruder_${System.currentTimeMillis()}_${(1000..9999).random()}.enc"
        val destinationFile = File(getPhotosDirectory(), fileName)

        FileOutputStream(destinationFile).use { fos ->
            fos.write(iv)
            fos.write(encryptedBytes)
            fos.flush()
        }

        destinationFile.absolutePath
    }

    /**
     * Decrypts an encrypted photo file directly into an in-memory Bitmap.
     * The plain-text image is never written to disk during this operation.
     */
    suspend fun decryptPhotoToBitmap(filePath: String): Bitmap? = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists() || file.length() <= GCM_IV_LENGTH_BYTES) {
            return@withContext null
        }

        try {
            val decryptedBytes = decryptPhotoBytes(file)
            BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decrypts an encrypted file to raw byte array in memory.
     */
    suspend fun decryptPhotoBytes(file: File): ByteArray = withContext(Dispatchers.IO) {
        FileInputStream(file).use { fis ->
            val iv = ByteArray(GCM_IV_LENGTH_BYTES)
            val bytesRead = fis.read(iv)
            if (bytesRead != GCM_IV_LENGTH_BYTES) {
                throw IllegalStateException("Corrupt encrypted file: IV header is invalid")
            }

            val encryptedPayload = fis.readBytes()

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

            cipher.doFinal(encryptedPayload)
        }
    }

    /**
     * Creates a temporary decrypted JPEG file in app cache specifically for explicit user sharing.
     * This temporary file is isolated to the cache directory and exposed via FileProvider.
     */
    suspend fun createTemporaryShareFile(encryptedFilePath: String): File? = withContext(Dispatchers.IO) {
        val sourceFile = File(encryptedFilePath)
        if (!sourceFile.exists()) return@withContext null

        try {
            val decryptedBytes = decryptPhotoBytes(sourceFile)
            val tempFile = File(getSharedCacheDirectory(), "evidence_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { fos ->
                fos.write(decryptedBytes)
                fos.flush()
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Safely deletes an encrypted photo file from internal storage.
     */
    suspend fun deletePhotoFile(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Wipes all stored encrypted intruder photos and clears temporary share cache.
     */
    suspend fun clearAllPhotos(): Unit = withContext(Dispatchers.IO) {
        try {
            getPhotosDirectory().listFiles()?.forEach { it.delete() }
            getSharedCacheDirectory().listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
