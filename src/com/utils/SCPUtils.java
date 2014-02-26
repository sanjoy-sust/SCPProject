package com.utils;

import com.des.CryptoOperation;
import com.des.RetailMACGenerator;

public class SCPUtils {

	public static byte[] getSessionKey(byte[] derivationalData) {
		try {
			return CryptoOperation.encrypt(derivationalData, ConstantsUtils.baseKey);
		} catch (Exception e) {
			System.out.println("Exception "+e);
		}
		return null;
	}

	public static byte[] generateHostCryptogram(byte[] hostChallenge, byte[] cardChallenge,byte[] sequenceCounter) {
		//byte[] sequenceCounter = new byte[] { (byte) 0x00, (byte) 0x00 };

		byte[][] dataToConcate = new byte[4][];
		dataToConcate[0] = sequenceCounter;
		dataToConcate[1] = cardChallenge;
		dataToConcate[2] = hostChallenge;
		dataToConcate[3] = ConstantsUtils.EightpaddingBytes;

		byte[] concatedData = ByteArrayUtils.combine(dataToConcate);
		
		byte[][] derivationalDataToConcate = new byte[3][];
		derivationalDataToConcate[0] = new byte[]{0x01,(byte)0x82};
		derivationalDataToConcate[1] = sequenceCounter;
		derivationalDataToConcate[2] = ByteArrayUtils.getMessage(ConstantsUtils.derivationDataEncSessionKey, 4,16);
		byte[] derivationalData = ByteArrayUtils.combine(derivationalDataToConcate);
		byte[] S_ENC = getSessionKey(derivationalData);

		byte[][] toConcateKey = new byte[2][];
		toConcateKey[0] = S_ENC;
		toConcateKey[1] = ByteArrayUtils.getMessage(S_ENC, 0, 8);
		byte[] concatedKey = ByteArrayUtils.combine(toConcateKey);

		System.out.println("Card Challenge  --> " + ByteArrayUtils.getHex(cardChallenge));
		try {
			byte[] hostCryptogram = CryptoOperation.encrypt(concatedData, concatedKey);

			byte[] cryptogram = ByteArrayUtils.getMessage(hostCryptogram, 16, 24);
			System.out.println("Host Cryptogram    --> " + ByteArrayUtils.getHex(cryptogram));
			return cryptogram;
		} catch (Exception e) {
			System.out.println("Exception --> "+e);
		}
		return null;
	}

	public static byte[] generateCMAC(byte[] hostCryptogram,byte[] sequenceCounter) {

		byte[] data = prepareCMACCommand(hostCryptogram);
		
		byte[][] derivationalDataToConcate = new byte[3][]; 
		
		derivationalDataToConcate[0] = new byte[]{0x01,0x01};
		derivationalDataToConcate[1] = sequenceCounter;
		derivationalDataToConcate[2] =ByteArrayUtils.getMessage(ConstantsUtils.derivationDataCMAC,4,16);
		byte[] derivationalDataCMAC = ByteArrayUtils.combine(derivationalDataToConcate);
		
		System.out.println("Sequence CounterCMAC "+ByteArrayUtils.getHex(sequenceCounter));
		System.out.println("DerivationalDataCMAC = "+ByteArrayUtils.getHex(derivationalDataCMAC));
		
		byte[] cmacSessionKey = getSessionKey(derivationalDataCMAC);

		System.out.println("CMAC Session Key --> " + ByteArrayUtils.getHex(cmacSessionKey));
		System.out.println("Data for Key     --> " + ByteArrayUtils.getHex(data));

		RetailMACGenerator macGen = new RetailMACGenerator();
		return macGen.retailMac(cmacSessionKey, data);
	}

	public static byte[] prepareCMACCommand(byte[] hostCryptogram) {

		byte[][] dataForCMACGeneration = new byte[4][];
		byte[] apduDataHeader = new byte[] { (byte) 0x84, (byte) 0x82, 0x00, 0x00, 0x10 };
		dataForCMACGeneration[0] = apduDataHeader;
		dataForCMACGeneration[1] = hostCryptogram;
		//dataForCMACGeneration[2] = ConstantsUtils.EightpaddingBytes;
		dataForCMACGeneration[2] = new byte[] { (byte)0x80, 0x00, 0x00 };
		byte[] combinedData = ByteArrayUtils.combine(dataForCMACGeneration);
		return combinedData;
	}

	public static byte[] getCardCryptogram(byte[] hostchallenge, byte[] cardChallenge, byte[] SENC,byte[] sequenceCounter) throws Exception {

		System.out.println("\nCard Cryptogram Generating...");
		byte[][] toConcate = new byte[4][];
		toConcate[0] = hostchallenge;
		toConcate[1] = sequenceCounter;
		toConcate[2] = cardChallenge;
		toConcate[3] = ConstantsUtils.EightpaddingBytes;
		byte[] concatedString = ByteArrayUtils.combine(toConcate);

		byte[][] toConcateKey = new byte[2][];
		toConcateKey[0] = SENC;
		toConcateKey[1] = ByteArrayUtils.getMessage(SENC, 0, 8);

		byte[] concatedKey = ByteArrayUtils.combine(toConcateKey);

		byte[] cardCryptogram = CryptoOperation.encrypt(concatedString, concatedKey);
		byte[] cryptogram = ByteArrayUtils.getMessage(cardCryptogram, 16, 24);
		return cryptogram;
	}

	public static void main(String[] args) {

	}

}
