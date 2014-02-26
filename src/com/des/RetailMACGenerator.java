package com.des;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.utils.ByteArrayUtils;
import com.utils.ConstantsUtils;

public class RetailMACGenerator {

	public byte[] retailMac(byte[] key, byte[] data) {
		byte[] mac;
		byte[] icv = ConstantsUtils.icv;
		
		
		if (data.length % 8 != 0) {
			System.out.println("data block size must be multiple of 8.But Size is = " + data.length);
			return null;
		}
		// DESedeKey
		byte[][] keysToCombine = new byte[2][];
		keysToCombine[0] = key;
		keysToCombine[1] = ByteArrayUtils.getMessage(key, 0, 8);
		byte[] desKey = ByteArrayUtils.combine(keysToCombine);
		SecretKey desedeCMAC = new SecretKeySpec(desKey, "DESede");

		// desCMAC Key
		byte[] desCMAC = keysToCombine[1];
		SecretKey singledesCMAC = new SecretKeySpec(desCMAC, "DES");

		try {
			Cipher desedeCBCCipher = Cipher.getInstance("DESede/CBC/NoPadding");
			Cipher desCipher = Cipher.getInstance("DES/CBC/NoPadding");
			IvParameterSpec ivSpec = new IvParameterSpec(icv);
			
			int blocks = data.length / 8;
			for (int i = 0; i < blocks - 1; i++) {
				desCipher.init(Cipher.ENCRYPT_MODE, singledesCMAC, ivSpec);
				byte[] dataBlock = ByteArrayUtils.getMessage(data, i * 8, i * 8 + 8);
				byte[] block = desCipher.doFinal(dataBlock);
				ivSpec = new IvParameterSpec(block);
			}

			desedeCBCCipher.init(Cipher.ENCRYPT_MODE, desedeCMAC, ivSpec);
			byte[] dataBlock = ByteArrayUtils.getMessage(data, (blocks-1)*8, blocks*8);
			mac = desedeCBCCipher.doFinal(dataBlock);
			System.out.println("Generated RetailMac Is ---> " + ByteArrayUtils.getHex(mac));
			return mac;
		} catch (Exception ert) {
			System.out.println(ert);
		}
		return null;
	}

	public static void main(String[] args) {

	}

}
