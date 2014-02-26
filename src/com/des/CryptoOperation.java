package com.des;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.utils.ByteArrayUtils;
import com.utils.ConstantsUtils;

public class CryptoOperation {

	static byte[] IvParam = ConstantsUtils.icv;//new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

	public static void main(String[] args) throws Exception {

		String str = "qqqqqqqq";
		System.out.println(str.getBytes().length);
		byte[] encryptedString = encrypt(str.getBytes(), ConstantsUtils.baseKey);
		System.out.println(ByteArrayUtils.getHex(encryptedString));
		String org = decrypt(encryptedString, ConstantsUtils.baseKey);
		System.out.println(org);

	}

	public static byte[] encrypt(byte[] plainTextBytes, byte[] sKey) throws Exception {

		SecretKey key = new SecretKeySpec(sKey, "DESede");
		IvParameterSpec iv = new IvParameterSpec(IvParam);
		Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);

		byte[] cipherText = cipher.doFinal(plainTextBytes);
		return cipherText;
	}

	public static String decrypt(byte[] message, byte[] sKey) throws Exception {

		final SecretKey key = new SecretKeySpec(sKey, "DESede");
		final IvParameterSpec iv = new IvParameterSpec(IvParam);
		final Cipher decipher = Cipher.getInstance("DESede/CBC/NoPadding");
		decipher.init(Cipher.DECRYPT_MODE, key, iv);

		final byte[] plainText = decipher.doFinal(message);
		return new String(plainText);
	}

}
