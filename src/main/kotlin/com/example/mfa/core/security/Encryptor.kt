package com.example.mfa.core.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Component
class Encryptor (
    @param:Value("\${encryption.secret.key}") private val secretKeyString: String
) {
    private val algorithm = "AES/GCM/NoPadding"
    private val aes = "AES"
    private val gcmIVLength = 12
    private val gcmTagLength = 128

    private val secretKey: SecretKeySpec

    init {
        if (secretKeyString.length != 32) {
            throw IllegalArgumentException("Encryption secret key must be 32 characters long for AES-256.")
        }
        secretKey = SecretKeySpec(secretKeyString.toByteArray(), aes)
    }

    fun encrypt(plaintext: String): String {
        // 1. Generate a random Initialization Vector (IV)
        val iv = ByteArray(gcmIVLength)
        java.security.SecureRandom().nextBytes(iv)

        // 2. Initialize the cipher for encryption
        val cipher = Cipher.getInstance(algorithm)
        val gcmParameterSpec = GCMParameterSpec(gcmTagLength, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)

        // 3. Encrypt the data
        val ciphertext = cipher.doFinal(plaintext.toByteArray())

        // 4. Combine IV and ciphertext and Base64 encode for safe storage/transmission
        val ivBase64 = Base64.getEncoder().encodeToString(iv)
        val ciphertextBase64 = Base64.getEncoder().encodeToString(ciphertext)

        return "$ivBase64:$ciphertextBase64"
    }

    fun decrypt(encryptedString: String): String {
        // 1. Split the string into the IV and the ciphertext
        val parts = encryptedString.split(":")
        if (parts.size != 2) {
            throw IllegalArgumentException("Invalid encrypted string format.")
        }

        // 2. Base64 decode the IV and ciphertext
        val iv = Base64.getDecoder().decode(parts[0])
        val ciphertext = Base64.getDecoder().decode(parts[1])

        // 3. Initialize the cipher for decryption
        val cipher = Cipher.getInstance(algorithm)
        val gcmParameterSpec = GCMParameterSpec(gcmTagLength, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)

        // 4. Decrypt the data
        val decryptedBytes = cipher.doFinal(ciphertext)

        return String(decryptedBytes)
    }
}