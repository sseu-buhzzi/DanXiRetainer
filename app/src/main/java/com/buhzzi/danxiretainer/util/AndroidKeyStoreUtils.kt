package com.buhzzi.danxiretainer.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

private const val ANDROID_KEYSTORE = "AndroidKeyStore"

private const val KEY_ALIAS = "DanXiRetainerAESKey"

private const val TRANSFORMATION = "AES/GCM/NoPadding"

private const val IV_SIZE = 12

private const val TAG_SIZE = 128

private fun getOrCreateKey() = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
	.getEntry(KEY_ALIAS, null)
	?.let { (it as KeyStore.SecretKeyEntry).secretKey }
	?: run {
		val keyGenerator = KeyGenerator.getInstance(
			KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE
		)
		val keyGenParameterSpec = KeyGenParameterSpec.Builder(
			KEY_ALIAS,
			KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
		)
			.setBlockModes(KeyProperties.BLOCK_MODE_GCM)
			.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
			.setKeySize(256)
			.build()
		keyGenerator.init(keyGenParameterSpec)
		keyGenerator.generateKey()
	}

fun androidKeyStoreEncrypt(data: ByteArray): ByteArray {
	val cipher = Cipher.getInstance(TRANSFORMATION)

	cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
	val cipherText = cipher.doFinal(data)
	return cipher.iv + cipherText
}

fun androidKeyStoreDecrypt(data: ByteArray): ByteArray {
	val iv = data.copyOfRange(0, IV_SIZE)
	val cipherText = data.copyOfRange(IV_SIZE, data.size)
	val cipher = Cipher.getInstance(TRANSFORMATION)
	val spec = GCMParameterSpec(TAG_SIZE, iv)

	cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)
	return cipher.doFinal(cipherText)
}
