package com.utils;

import java.security.SecureRandom;

public class RandomNumberGenerator {

	public static byte[] getRandomNumber(int size) {
		SecureRandom sr = new SecureRandom();
		byte[] rndBytes = new byte[size];
		sr.nextBytes(rndBytes);
		return rndBytes;
	}

	public static void main(String[] args) {
		System.out.println(new String(getRandomNumber(8)).length());
	}

}
