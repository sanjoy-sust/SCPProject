package com.transmitter;

import com.card.OnCardOperation;
import com.sun.javacard.apduio.Apdu;
import com.utils.ByteArrayUtils;

public class VirtualCardTransmitter extends Transmitter {

	@Override
	public Apdu sendApdu(Apdu apdu) {
		OnCardOperation onCardOp = new OnCardOperation();

		byte[] ins = ByteArrayUtils.getMessage(apdu.getCommandApduBytes(), 1, 2);
		System.out.println("ins IS " + ByteArrayUtils.getHex(ins));
		if (ByteArrayUtils.getHex(ins).toString().equals("50")) {
			System.out.println("\nInitialize Update Command Sending...");
			System.out.println("apduCommand ----->>>> " + ByteArrayUtils.getHex(apdu.getCommandApduBytes()));
			return onCardOp.getInitialUpdateResponse(apdu);
		} else if (ByteArrayUtils.getHex(ins).toString().equals("82")) {
			System.out.println("\nExternal Authentication Command Sending...");
			return onCardOp.getExternalAuthenticateResponse(apdu);
		} else
		{
		    System.out.println("False Return");
			return null;
		}
	}
}
