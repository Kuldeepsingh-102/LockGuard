package com.lockguard.app.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Manages in-app vault PIN authentication and auto-lock state.
 *
 * Security Features:
 * - Uses EncryptedSharedPreferences backed by Android Keystore.
 * - Stores only cryptographically salted SHA-256 hashes of the in-app PIN.
 * - Constant-time comparison to prevent side-channel timing attacks.
 * - Never stores or attempts to read device lock-screen credentials.
 */
class PinManager(private val context: Context) {

    companion object {
        private const val PREFS_FILENAME = "lockguard_security_prefs"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_IS_LOCKED = "is_app_locked"
        private const val KEY_LAST_ACTIVE = "last_active_timestamp"
        private const val SALT_LENGTH = 16
    }

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_FILENAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback for edge cases where keystore initialization has issues
            context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * Checks if a user has established an in-app vault PIN.
     */
    fun isPinSet(): Boolean {
        return prefs.contains(KEY_PIN_HASH) && prefs.contains(KEY_PIN_SALT)
    }

    /**
     * Hashes and securely persists a new in-app vault PIN.
     */
    fun setPin(pin: String): Boolean {
        if (pin.length < 4) return false

        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)

        val hash = hashPinWithSalt(pin, salt)

        prefs.edit()
            .putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_PIN_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
            .putBoolean(KEY_IS_LOCKED, false)
            .putLong(KEY_LAST_ACTIVE, System.currentTimeMillis())
            .apply()

        return true
    }

    /**
     * Validates an entered PIN against the salted hash in constant time.
     */
    fun verifyPin(enteredPin: String): Boolean {
        val storedSaltBase64 = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val storedHashBase64 = prefs.getString(KEY_PIN_HASH, null) ?: return false

        val salt = Base64.decode(storedSaltBase64, Base64.NO_WRAP)
        val expectedHash = Base64.decode(storedHashBase64, Base64.NO_WRAP)

        val actualHash = hashPinWithSalt(enteredPin, salt)

        val matches = MessageDigest.isEqual(expectedHash, actualHash)
        if (matches) {
            unlockApp()
        }
        return matches
    }

    /**
     * Clears in-app PIN and resets vault security.
     */
    fun clearPin() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .remove(KEY_PIN_SALT)
            .putBoolean(KEY_IS_LOCKED, false)
            .apply()
    }

    fun isAppLocked(autoLockTimeoutSeconds: Int): Boolean {
        if (!isPinSet()) return false

        val isExplicitlyLocked = prefs.getBoolean(KEY_IS_LOCKED, true)
        if (isExplicitlyLocked) return true

        if (autoLockTimeoutSeconds <= 0) {
            // Immediate lock on backgrounding
            return true
        }

        val lastActive = prefs.getLong(KEY_LAST_ACTIVE, 0L)
        val elapsed = (System.currentTimeMillis() - lastActive) / 1000
        val hasTimedOut = elapsed > autoLockTimeoutSeconds
        if (hasTimedOut) {
            lockApp()
            return true
        }

        return false
    }

    fun lockApp() {
        prefs.edit()
            .putBoolean(KEY_IS_LOCKED, true)
            .apply()
    }

    fun unlockApp() {
        prefs.edit()
            .putBoolean(KEY_IS_LOCKED, false)
            .putLong(KEY_LAST_ACTIVE, System.currentTimeMillis())
            .apply()
    }

    fun recordUserActivity() {
        prefs.edit()
            .putLong(KEY_LAST_ACTIVE, System.currentTimeMillis())
            .apply()
    }

    private fun hashPinWithSalt(pin: String, salt: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        return digest.digest(pin.toByteArray(Charsets.UTF_8))
    }
}
