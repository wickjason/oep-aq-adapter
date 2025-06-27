package com.oracle.aq.engine.services;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.origin.SystemEnvironmentOrigin;

import com.oracle.aq.engine.models.PinEventTy;

/** This is a utility class containing common methods for the project */
public final class AQAdapterUtils {

	private AQAdapterUtils() {
	}

	public static String getPayload(final PinEventTy message) throws SQLException, IOException {
		// return message.getMessage();
		System.out.println("AQAdapterUtils.getPayload()");
		if (message == null) {
			return null;
		}
		final String flistBuff = message.getFlistBuf();
		if (flistBuff == null || flistBuff.isEmpty()) {
			System.out.println("AQAdapterUtils.getPayload() flistBuff is null");
			final Reader reader = message.getLargeFlistBuf().getCharacterStream();
			if (reader == null) {
				return null;
			}
			final char[] buffer = new char[(int) message.getLargeFlistBuf().length()];
			if (buffer.length == 0) {
				return null;
			}
			reader.read(buffer);
			return String.valueOf(buffer);
		} else {
			System.out.println("AQAdapterUtils.getPayload() flistBuff is not null");
			return flistBuff;
		}

	}
}