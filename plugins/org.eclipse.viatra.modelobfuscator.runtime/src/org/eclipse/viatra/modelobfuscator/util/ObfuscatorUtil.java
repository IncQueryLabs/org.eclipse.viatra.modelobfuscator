/*******************************************************************************
 * Copyright (c) 2010-2014, Abel Hegedus, Istvan Rath and Daniel Varro
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-v20.html.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.viatra.modelobfuscator.util;

import java.math.BigInteger;
import java.util.Random;

import com.google.common.base.Preconditions;

/**
 * @author Abel Hegedus
 *
 */
public class ObfuscatorUtil {

    public static byte[] xorWithSeed(byte[] input, byte[] key) {
        return xorWithSeed(input, key, 0);
    }
    
    /**
     * The bits used from the key are offset by the given value.
     */
    public static byte[] xorWithSeed(byte[] input, byte[] key, int offset) {
        int relevantOffset = offset % key.length;
        byte[] out = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            out[i] = (byte) (input[i] ^ key[(i+relevantOffset)%key.length]);
        }
        return out;
    }
    
    
    public static String generateHexSeed(int length) {
        return generateRandomString(length, 16);
    }
    
    public static String generateBase36RandomString(int length) {
        return generateRandomString(length, 36);
    }

    protected static String generateRandomString(int length, int radix) {
        Preconditions.checkArgument(length > 0, "Length must be positive");
        Preconditions.checkArgument(radix >= Character.MIN_RADIX, "Radix must be higher than or equal to Character.MIN_RADIX");
        Preconditions.checkArgument(radix <= Character.MAX_RADIX, "Radix must be lower than or equal to Character.MAX_RADIX");
        Random random = new Random();
        // use BigInteger to generate random number (8 bit per byte)
        BigInteger randomNumber = new BigInteger(length*8, random);
        // convert to string with radix
        String radixString = randomNumber.toString(radix);
        // ignore the first character to avoid leading zeroes
        String radixNoLeadingZeroes = radixString.substring(1, length+1);
        return radixNoLeadingZeroes;  
    }
    
}
