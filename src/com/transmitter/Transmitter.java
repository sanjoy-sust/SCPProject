package com.transmitter;

import com.sun.javacard.apduio.Apdu;

public abstract class Transmitter {

	public Boolean openChannel() {
		return true;
	}

	public Boolean closeChannel() {
		return true;
	}

	public abstract Apdu sendApdu(Apdu apdu);

}
