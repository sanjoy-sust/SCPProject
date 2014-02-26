package com.transmitter;

public class TransmitterFactory {

	public static Transmitter getTransmitter(String card) {
		if (card.equals("AT"))
			return new ActualCardTransmitter();
		else if (card.equals("VT"))
			return new VirtualCardTransmitter();
		return null;
	}

}
