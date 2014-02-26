package com.offCard;

import java.util.Scanner;

import com.sun.javacard.apduio.Apdu;
import com.transmitter.Transmitter;
import com.transmitter.TransmitterFactory;
import com.utils.ByteArrayUtils;
import com.utils.SCPUtils;

public class Terminal {

	private static void printData(Apdu initialCommand, Apdu initializeResponseData) {

		System.out.println("Initial Command       -> " + initialCommand);
		System.out.println("Response Data         -> " + initializeResponseData);
		System.out.println("Initial Response Data -> " + ByteArrayUtils.getHex(initializeResponseData.getDataOut()) + " And length = "
				+ ByteArrayUtils.getHex(initializeResponseData.getDataOut()).length());
		System.out.println("Key diversification(10) + Key Info(2) + Sequence Counter (2) + Card Challenge (6) + Card Cryptogram(8) = Initialize Response(28)");
		System.out.println("Initial Update Command Successfully received by card and send response successfully.Status is  "
				+ ByteArrayUtils.getHex(initializeResponseData.getSw1Sw2()));

	}

	public static void rootTask() {
		
		System.out.println("Please Enter Your Choice\n a)AT for Actual Card\n b)VT for Virtual Card\n");
		
		Scanner input  = new Scanner(System.in);
		String cardChoice = input.nextLine();
		if(!cardChoice.equals("AT") && !cardChoice.equals("VT"))
		{
			System.out.println("Sorry!!! Please Enter Valid Data");
			return;
		}
		
		// For INITIALIZE Update Response
		Transmitter vCard = TransmitterFactory.getTransmitter(cardChoice);
		byte[] hostChallenge = TerminalOperation.generateHostChallenge();
		Apdu initialCommand = TerminalOperation.getInitialUpdateCommand(hostChallenge);

		if (vCard.openChannel()) {

			// send APDU and Get Initialize Response Data
			Apdu initializeResponseData = vCard.sendApdu(initialCommand);
			printData(initialCommand, initializeResponseData);

			// verify Cryptogram and send external authenticate apdu
			if (ByteArrayUtils.getHex(initializeResponseData.getSw1Sw2()).equals("9000")) {
				boolean isVerifysuccess = TerminalOperation.verifyCardCryptogram(hostChallenge, initializeResponseData);
				if (isVerifysuccess) {

					System.out.println(" Cryptogram verifed Successfully.\n");

					// generate Host Cryptogram
					byte[] sequenceCounter = ByteArrayUtils.getMessage(initializeResponseData.getDataOut(), 12, 14);
					byte[] cardChallenge = ByteArrayUtils.getMessage(initializeResponseData.getDataOut(), 14, 20);
					byte[] hostCryptogram = SCPUtils.generateHostCryptogram(hostChallenge, cardChallenge,sequenceCounter);
					System.out.println(" HostCryptogram Generated from terminal.  " + ByteArrayUtils.getHex(hostCryptogram));

					// CMAC Generation
					byte[] cmac = SCPUtils.generateCMAC(hostCryptogram,sequenceCounter);

					// generate External Authenticate APDU
					Apdu xternalCommand = TerminalOperation.getExternalAuthenticateCommand(hostCryptogram, cmac);
					Apdu authenticateResponse = vCard.sendApdu(xternalCommand);
					if (ByteArrayUtils.getHex(authenticateResponse.getSw1Sw2()).equals("9000")) {
						System.out.println("Success Status -> " + ByteArrayUtils.getHex(authenticateResponse.getSw1Sw2()));
						System.out.println("SCP communication is Successful. ");

						if (vCard.closeChannel()) {
							System.out.println("Terminated SuccessFully");
						} else {
							System.out.println("Termination Process UnSuccessful");
						}
					} else {
						System.out.println("Erorr Status Return -> " + ByteArrayUtils.getHex(authenticateResponse.getSw1Sw2()));
					}
				}
			}
		} else {
			System.out.println("Can not Open Any On-Card");
		}
	}
	
	public static void main(String[] args) {
		rootTask();
	}
}
