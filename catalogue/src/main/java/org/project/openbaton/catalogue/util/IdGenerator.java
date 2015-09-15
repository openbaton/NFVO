/*******************************************************************************
 * Copyright (C) 2014 FhG Fokus
 *
 * This file is part of the OpenSDNCore project.
 ******************************************************************************/
package org.project.openbaton.catalogue.util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.UUID;

public class IdGenerator {

	 private static SecureRandom random = new SecureRandom();

	  public static String createId() {
	    return new BigInteger(32, random).toString();
	  }

    public static String createUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}