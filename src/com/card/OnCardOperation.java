package com.card;

import com.sun.javacard.apduio.Apdu;
import com.utils.ByteArrayUtils;
import com.utils.ConstantsUtils;
import com.utils.RandomNumberGenerator;
import com.utils.SCPUtils;

public class OnCardOperation {
	public static byte[] generateCardChallenge() {
		// return RandomNumberGenerator.getRandomNumber(6);
		return ConstantsUtils.cardChallenge;
	}

	public Apdu getInitialUpdateResponse(Apdu apdu) {
		Apdu response = new Apdu();
		byte[] cardChallenge = generateCardChallenge();
		System.out.println("Card Challenge " + ByteArrayUtils.getHex(cardChallenge));
		byte[] hostchallenge = apdu.getDataIn();
		System.out.println("Host Challenge " + ByteArrayUtils.getHex(hostchallenge));
		byte[] SENC = SCPUtils.getSessionKey(ConstantsUtils.derivationDataEncSessionKey);
		System.out.println("Secure channel Encryption Key " + ByteArrayUtils.getHex(SENC));
		try {
			byte[] sequenceCounter = new byte[]{0x00,0x00};
			byte[] cardCryptogram = SCPUtils.getCardCryptogram(hostchallenge, cardChallenge, SENC,sequenceCounter);
			System.out.println(" CardCryptogram Generated --> " + ByteArrayUtils.getHex(cardCryptogram));
			byte[][] responseData = new byte[5][];

			responseData[0] = RandomNumberGenerator.getRandomNumber(10);
			responseData[1] = new byte[] { (byte) 0x00, (byte) 0x02 };
			responseData[2] = new byte[] { (byte) 0x00, (byte) 0x00 };
			responseData[3] = cardChallenge;
			responseData[4] = cardCryptogram;
			byte[] dataField = ByteArrayUtils.combine(responseData);
			System.out.println("Data At Card for INITIAL RESPONSE :: " + ByteArrayUtils.getHex(dataField) + " And Length Of Data " + ByteArrayUtils.getHex(dataField).length());
			response.dataOut = dataField;
			byte[] status = new byte[] { (byte) 0x90, (byte) 0x00 };
			response.sw1sw2 = status;
		} catch (Exception e) {
			// e.printStackTrace();
		}

		return response;
	}

	public boolean verifyCMAC(byte[] dataField) {

		byte[] hostCryptogramFromTerminal = ByteArrayUtils.getMessage(dataField, 0, 8);
		byte[] cmacFromTerminal = ByteArrayUtils.getMessage(dataField, 8, 16);

		System.out.println(" At Verify CMAC \n HostCryptogram :--> " + ByteArrayUtils.getHex(hostCryptogramFromTerminal) + "\n CMAC   -->"
				+ ByteArrayUtils.getHex(cmacFromTerminal));

		// CMAC Generation

		byte[] sequenceCounter = new byte[]{0x00,0x00};
		byte[] cmac = SCPUtils.generateCMAC(hostCryptogramFromTerminal,sequenceCounter);
		if (ByteArrayUtils.byteToHex(cmacFromTerminal).equals(ByteArrayUtils.byteToHex(cmac))) {
			System.out.println("CMAC Verified Successfully\n");
			return true;
		} else {
			System.out.println("CMAC is not Verified Successfully\n");
		}
		return false;
	}

	public boolean verifyHostCryptogram(byte[] hostCryptogramFromTerminal) {

		byte[] sequenceCounter = new byte[]{0x00,0x00};
		byte[] hostcryprtogram = SCPUtils.generateHostCryptogram(ConstantsUtils.hostChallenge, ConstantsUtils.cardChallenge,sequenceCounter);
		System.out.println("\nAt verifyHostCryptogram -------------------");
		System.out.println(" HostCryptogram generated for verify :: " + ByteArrayUtils.getHex(hostcryprtogram));
		System.out.println(" HostCryptogram fromTerminal for verify :: " + ByteArrayUtils.getHex(hostCryptogramFromTerminal));

		if (ByteArrayUtils.getHex(hostcryprtogram).equals(ByteArrayUtils.getHex(hostCryptogramFromTerminal))) {
			System.out.println("HostCryptogram Verified Successfully ----------------\n");
			return true;
		} else {
			System.out.println("HostCryptogram is not Verified Successfully ----------------\n");
		}
		return false;
	}

	public Apdu getExternalAuthenticateResponse(Apdu apdu) {
		byte[] dataField = apdu.getDataIn();
		byte[] hostCryptogramFromTerminal = ByteArrayUtils.getMessage(dataField, 0, 8);
		byte[] cmacFromTerminal = ByteArrayUtils.getMessage(dataField, 8, 16);
		System.out.println("HostCryptogram From terminal = " + ByteArrayUtils.getHex(hostCryptogramFromTerminal));
		System.out.println("CMAC From terminal = " + ByteArrayUtils.getHex(cmacFromTerminal));

		// verify hostcryptogram
		boolean isVerifyHostCryptogram = verifyHostCryptogram(hostCryptogramFromTerminal);

		// verify C-MAC
		boolean isVerifyCMAC = verifyCMAC(dataField);
		// initialize icv
		Apdu responseApdu = new Apdu();
		if (isVerifyHostCryptogram && isVerifyCMAC)
			responseApdu.sw1sw2 = new byte[] { (byte) 0x90, 0x00 };
		else {
			responseApdu.sw1sw2 = new byte[] { (byte) 0x6A, (byte) 0x88 };
		}
		System.out.println("External Authentication Response Send To Terminal...");
		return responseApdu;
	}

	public static void main(String[] args) {

		System.out.println("Base Key " + new String(ByteArrayUtils.byteToHex(ConstantsUtils.baseKey)));
	}

}
