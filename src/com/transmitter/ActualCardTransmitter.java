package com.transmitter;

import java.util.List;

import com.sun.javacard.apduio.Apdu;
import com.utils.ByteArrayUtils;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

public class ActualCardTransmitter extends Transmitter {
	public static CardChannel channel = null;
	public static Card card = null;

	@Override
	public Boolean openChannel() {
		try {
			// show the list of available terminals
			TerminalFactory factory = TerminalFactory.getDefault();
			List<CardTerminal> terminals = factory.terminals().list();
			System.out.println("Terminals: " + terminals);
			// get the first terminal
			CardTerminal terminal = terminals.get(0);
			// establish a connection with the card
			card = terminal.connect("*");
			System.out.println("card: " + card);
			channel = card.getBasicChannel();
			return true;
		} catch (Exception ert) {
			System.out.println("Exception Occures ->" + ert);
		}
		return false;
	}

	@Override
	public Boolean closeChannel() {
		try {
			card.disconnect(false);
			return true;
		} catch (CardException e) {
			System.out.println("Exception Occures ->" + e);
		}
		return false;
	}

	@Override
	public Apdu sendApdu(Apdu apdu) {
		Apdu responseApdu = new Apdu();
		try {
            System.out.println(ByteArrayUtils.getHex(apdu.getCommandApduBytes()));
			ResponseAPDU r = channel.transmit(new CommandAPDU(apdu.getCommandApduBytes()));
			System.out.println("response: " + ByteArrayUtils.getHex(r.getBytes()));

			responseApdu.dataOut = r.getData();
			responseApdu.sw1sw2 = new byte[] { (byte) r.getSW1(), (byte) r.getSW2() };
			return responseApdu;

		} catch (Exception ert) {
			System.out.println("Exception Occures ->" + ert);
		}
		return null;
	}

}
