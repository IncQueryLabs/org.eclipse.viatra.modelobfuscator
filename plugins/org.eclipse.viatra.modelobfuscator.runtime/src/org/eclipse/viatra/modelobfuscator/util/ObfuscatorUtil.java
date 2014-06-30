/*******************************************************************************
 * Copyright (c) 2010-2014, Abel Hegedus, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abel Hegedus - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.modelobfuscator.util;

import com.google.common.base.Preconditions;

/**
 * @author Abel Hegedus
 *
 */
public class ObfuscatorUtil {

    public static byte[] xorWithSeed(byte[] input, byte[] key) {
        byte[] out = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            out[i] = (byte) (input[i] ^ key[i%key.length]);
        }
        return out;
    }
    
    public static String generateHexSeed(int length) {
        Preconditions.checkArgument(length > 0,"Length must be positive");
        // each fragment is 3 characters and we need an additional one
        int noOfFragments = (length / 3) + 1;
        StringBuilder sb = new StringBuilder(noOfFragments * 3);
        for(int i = 0; i < noOfFragments; i++) {
            // randomized string but first 3 chars not too random (typically "3fd")
            String fragment = Long.toHexString(Double.doubleToLongBits(Math.random()));
            // take only last 3 characters
            sb.append(fragment.substring(3, 6));
        }
        return sb.substring(0, length);
    }
    
}
