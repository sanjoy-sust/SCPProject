package com.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ByteArrayUtils {
	static String HEXES = "0123456789ABCDEF";

	public static String byteToHex(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();
		for (byte aByte : bytes) {
			String hex = Integer.toHexString(0xff & aByte);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}
	public static byte[] combine(byte[][] messages) {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			for (byte[] bs : messages) {
				outputStream.write(bs);
			}
		} catch (Exception ert) {

		}
		byte[] concatenatedArray = outputStream.toByteArray();
		try {
			outputStream.close();
		} catch (IOException e) {
			System.out.println(e);
		}
		return concatenatedArray;
	}

	public static byte[] getMessage(byte[] input, int start, int endpoint) {
		return Arrays.copyOfRange(input, start, endpoint);
	}

	public static String getHex(byte[] raw) {
		if (raw == null) {
			return null;
		}
		StringBuilder hex = new StringBuilder(2 * raw.length);
		for ( byte b : raw) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(
					HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}

}
