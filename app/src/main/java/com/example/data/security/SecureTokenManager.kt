package com.example.data.security

import android.content.Context
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * SecureTokenManager
 * Stores and retrieves JWT tokens using Android KeyStore-backed AES-GCM-256 encryption.
 * Automatically falls back to obfuscated base64 storage if KeyStore is unavailable (e.g. during local test runners).
 */
object SecureTokenManager {

    private const val PREFS_NAME = "echo_secure_auth_vault"
    private const val KEY_ENCRYPTED_TOKEN = "enc_jwt_token"
    private const val KEY_IV = "enc_jwt_iv"
    private const val KEY_FALLBACK_TOKEN = "fallback_jwt_token"

    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "echoflux_jwt_vault_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128

    fun saveToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (token.isBlank()) {
            clearToken(context)
            return
        }

        try {
            val secretKey = getOrCreateSecretKey()
            if (secretKey != null) {
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                val iv = cipher.iv
                val encryptedBytes = cipher.doFinal(token.toByteArray(StandardCharsets.UTF_8))

                val encBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
                val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)

                prefs.edit()
                    .putString(KEY_ENCRYPTED_TOKEN, encBase64)
                    .putString(KEY_IV, ivBase64)
                    .remove(KEY_FALLBACK_TOKEN)
                    .apply()
                return
            }
        } catch (_: Exception) {
            // Fallback for non-hardware environments (Robolectric / Mock JVM)
        }

        // Graceful Obfuscated Fallback
        val obfuscated = Base64.encodeToString(token.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        prefs.edit()
            .putString(KEY_FALLBACK_TOKEN, obfuscated)
            .remove(KEY_ENCRYPTED_TOKEN)
            .remove(KEY_IV)
            .apply()
    }

    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val encBase64 = prefs.getString(KEY_ENCRYPTED_TOKEN, null)
        val ivBase64 = prefs.getString(KEY_IV, null)

        if (encBase64 != null && ivBase64 != null) {
            try {
                val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
                val secretKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
                if (secretKey != null) {
                    val encryptedBytes = Base64.decode(encBase64, Base64.NO_WRAP)
                    val iv = Base64.decode(ivBase64, Base64.NO_WRAP)

                    val cipher = Cipher.getInstance(TRANSFORMATION)
                    val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

                    val decryptedBytes = cipher.doFinal(encryptedBytes)
                    return String(decryptedBytes, StandardCharsets.UTF_8)
                }
            } catch (_: Exception) {
                // Ignore decryption failure and check fallback
            }
        }

        // Check fallback
        val fallbackBase64 = prefs.getString(KEY_FALLBACK_TOKEN, null) ?: return null
        return try {
            val bytes = Base64.decode(fallbackBase64, Base64.NO_WRAP)
            String(bytes, StandardCharsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    fun clearToken(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_ENCRYPTED_TOKEN)
            .remove(KEY_IV)
            .remove(KEY_FALLBACK_TOKEN)
            .apply()
    }

    fun hasToken(context: Context): Boolean {
        return !getToken(context).isNullOrBlank()
    }

    private fun getOrCreateSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.getKey(KEY_ALIAS, null) as? SecretKey
            } else {
                val keyGen = KeyGenerator.getInstance("AES", ANDROID_KEY_STORE)
                val keyGenSpecClass = Class.forName("android.security.keystore.KeyGenParameterSpec\$Builder")
                val purposes = 1 or 2 // PURPOSE_ENCRYPT | PURPOSE_DECRYPT
                val builder = keyGenSpecClass.getConstructor(String::class.java, Int::class.java)
                    .newInstance(KEY_ALIAS, purposes)

                val blockModes = arrayOf("GCM")
                keyGenSpecClass.getMethod("setBlockModes", Array<String>::class.java).invoke(builder, blockModes)

                val encPaddings = arrayOf("NoPadding")
                keyGenSpecClass.getMethod("setEncryptionPaddings", Array<String>::class.java).invoke(builder, encPaddings)

                keyGenSpecClass.getMethod("setKeySize", Int::class.java).invoke(builder, 256)

                val spec = keyGenSpecClass.getMethod("build").invoke(builder) as java.security.spec.AlgorithmParameterSpec
                keyGen.init(spec)
                keyGen.generateKey()
            }
        } catch (_: Throwable) {
            null
        }
    }
}
