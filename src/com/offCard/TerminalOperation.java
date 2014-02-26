package com.offCard;

import java.util.Arrays;

import com.sun.javacard.apduio.Apdu;
import com.transmitter.Transmitter;
import com.transmitter.VirtualCardTransmitter;
import com.utils.ByteArrayUtils;
import com.utils.ConstantsUtils;
import com.utils.SCPUtils;

public class TerminalOperation {

	public static byte[] generateHostChallenge() {
		// return RandomNumberGenerator.getRandomNumber(8);
		return ConstantsUtils.hostChallenge;
	}

	public static Apdu getInitialUpdateCommand(byte[] hostChallenge) {
		Apdu apdu = new Apdu();
		apdu.command[Apdu.CLA] = (byte) 0x80;
		apdu.command[Apdu.INS] = (byte) 0x50;
		apdu.Lc = 8;
		apdu.command[Apdu.P1] = 0;
		apdu.command[Apdu.P2] = 0;
		apdu.Le = 0;
		apdu.setDataIn(hostChallenge);
		return apdu;
	}

	public static Apdu getExternalAuthenticateCommand(byte[] hostCryptogram, byte[] cmac) {
		Apdu apdu = new Apdu();
		apdu.command[Apdu.CLA] = (byte) 0x84;
		apdu.command[Apdu.INS] = (byte) 0x82;
		apdu.Lc = 16;
		apdu.command[Apdu.P1] = (byte) 0x00;
		apdu.command[Apdu.P2] = 0;
		apdu.Le = 0;
		byte[][] toConcateData = new byte[2][];
		toConcateData[0] = hostCryptogram;
		toConcateData[1] = cmac;
		apdu.setDataIn(ByteArrayUtils.combine(toConcateData));

		System.out.println("External command ::" + apdu);
		return apdu;
	}

	public static boolean verifyCardCryptogram(byte[] hostChallenge, Apdu initializeResponseData) {

		byte[] cardCryptogramFromCard = ByteArrayUtils.getMessage(initializeResponseData.getDataOut(), 20, 28);
		byte[] cardChallenge = ByteArrayUtils.getMessage(initializeResponseData.getDataOut(), 14, 20);

		byte[][] concatedDataForSessionKey = new byte[3][];
		concatedDataForSessionKey[0] = new byte[] { 0x01, (byte) 0x82 };
		concatedDataForSessionKey[1] = ByteArrayUtils.getMessage(initializeResponseData.getDataOut(), 12, 14);
		concatedDataForSessionKey[2] = ByteArrayUtils.getMessage(ConstantsUtils.derivationDataEncSessionKey, 4, 16);
		byte[] derivationData = ByteArrayUtils.combine(concatedDataForSessionKey);
		byte[] sequenceCounter = concatedDataForSessionKey[1];
		byte[] SENC = SCPUtils.getSessionKey(derivationData);
		System.out.println("Encryption Session Key ::"+ ByteArrayUtils.getHex(SENC));
		try {
			byte[] cardCryptogram = SCPUtils.getCardCryptogram(hostChallenge, cardChallenge, SENC,sequenceCounter);
			System.out.println(" CardCryptogram from Response: " + ByteArrayUtils.getHex(cardCryptogramFromCard));
			System.out.println(" CardCryptogram from Terminal: " + ByteArrayUtils.getHex(cardCryptogram));
			if (Arrays.equals(cardCryptogramFromCard, cardCryptogram)) {
				return true;
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return false;
	}

	private static void printData(Apdu initialCommand, Apdu initializeResponseData) {

		System.out.println("Initial Command       -> " + initialCommand);
		System.out.println("Response Data         -> " + initializeResponseData);
		System.out.println("Initial Response Data -> " + ByteArrayUtils.getHex(initializeResponseData.getDataOut()) + " And length = "
				+ ByteArrayUtils.getHex(initializeResponseData.getDataOut()).length());

		System.out.println("Key diversification(10) + Key Info(2) + Sequence Counter (2) + Card Challenge (6) + Card Cryptogram(8) = Initialize Response(28)");
		System.out.println("Initial Update Command Successfully received by card and send response successfully.Status is  "
				+ ByteArrayUtils.getHex(initializeResponseData.getSw1Sw2()));

	}

	public static void main(String[] args) {

		// For INITIALIZE Update Response
		byte[] hostChallenge = generateHostChallenge();
		Apdu initialCommand = getInitialUpdateCommand(hostChallenge);
		Transmitter vCard = new VirtualCardTransmitter();

		if (vCard.openChannel()) {
			// send APDU and Get Initialize Response Data
			Apdu initializeResponseData = vCard.sendApdu(initialCommand);
			printData(initialCommand, initializeResponseData);

			// verify Cryptogram and send external authenticate apdu
			if (ByteArrayUtils.getHex(initializeResponseData.getSw1Sw2()).equals("9000")) {
				boolean isVerifysuccess = verifyCardCryptogram(hostChallenge, initializeResponseData);
				if (isVerifysuccess) {

					System.out.println(" Cryptogram verifed Successfully.\n");

					// generate Host Cryptogram
					byte[] sequenceCounter = ByteArrayUtils.getMessage(initializeResponseData.getDataOut(), 12,14);
					byte[] cardChallenge = ByteArrayUtils.getMessage(initializeResponseData.getDataOut(), 14, 20);
					byte[] hostCryptogram = SCPUtils.generateHostCryptogram(hostChallenge, cardChallenge,sequenceCounter);
					System.out.println(" HostCryptogram Generated from terminal.  " + ByteArrayUtils.getHex(hostCryptogram));

					// CMAC Generation
					byte[] cmac = SCPUtils.generateCMAC(hostCryptogram,sequenceCounter);

					// generate External Authecate APDU
					Apdu xternalCommand = getExternalAuthenticateCommand(hostCryptogram, cmac);
					Apdu authenticateResponse = vCard.sendApdu(xternalCommand);
					if (ByteArrayUtils.getHex(authenticateResponse.getSw1Sw2()).equals("9000")) {
						System.out.println(" SCP communication is Successful. ");
						if (vCard.closeChannel()) {
							System.out.println("Terminated SuccessFully");
						}
					}
				}
			}
		} else {
			System.out.println("Can not Open Any Terminal");
		}
	}
}
