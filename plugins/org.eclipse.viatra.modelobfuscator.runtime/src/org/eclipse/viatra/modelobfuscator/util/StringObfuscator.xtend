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
package org.eclipse.viatra.modelobfuscator.util

import com.google.common.base.Preconditions
import com.google.common.hash.Hashing
import com.google.common.io.BaseEncoding
import java.math.BigInteger
import org.eclipse.viatra.modelobfuscator.api.DataTypeObfuscator

/**
 * @author Abel Hegedus
 * 
 */
class StringObfuscator implements DataTypeObfuscator<String> {

    private String seed
    private String salt
    private String prefix
    private byte[] seedNumber
    private val coder = BaseEncoding.base32.omitPadding.lowerCase
    private int baseOffset

    @Deprecated
    new(String seed, String salt) {
        this(seed, salt, "")
    }

    @Deprecated
    new(String seed, String salt, String prefix) {
        Preconditions.checkArgument(seed != null, "Seed cannot be null")
        Preconditions.checkArgument(salt != null, "Salt cannot be null")
        Preconditions.checkArgument(prefix != null, "Prefix cannot be null")
        this.seed = seed
        this.seedNumber = seed.bytes
        this.salt = salt
        this.prefix = prefix
        val offsetHash = Hashing.md5.newHasher.putUnencodedChars(seed).putUnencodedChars(salt).putUnencodedChars(prefix).hash
        this.baseOffset = offsetHash.asInt%seedNumber.length 
    }

    new(BigInteger seed, String salt, String prefix) {
        Preconditions.checkArgument(seed != null, "Seed cannot be null")
        Preconditions.checkArgument(salt != null, "Salt cannot be null")
        Preconditions.checkArgument(prefix != null, "Prefix cannot be null")
        this.seed = seed.toString(36)
        this.seedNumber = seed.toByteArray.tail
        this.salt = salt
        this.prefix = prefix
        val offsetHash = Hashing.md5.newHasher.putBytes(seedNumber).putUnencodedChars(salt).putUnencodedChars(prefix).hash
        this.baseOffset = Math.abs(offsetHash.asInt % (seedNumber.length))
    }

    override obfuscateData(String original) {
        if (original != null) {
            val salted = salt + original
            val offset = salted.bytes.length + baseOffset
            val obfuscatedBytes = ObfuscatorUtil.xorWithSeed(salted.bytes, seedNumber, offset)
            return addPrefix(coder.encode(obfuscatedBytes))
        }
    }

    override restoreData(String obfuscated) {
        if (obfuscated != null) {
            val obfuscatedBytes = coder.decode(removePrefix(obfuscated))
            val offset = obfuscatedBytes.length + baseOffset
            val salted = new String(ObfuscatorUtil.xorWithSeed(obfuscatedBytes, seedNumber, offset))
            Preconditions.checkState(salted.startsWith(salt),"Salt %s does not match in %s",salt,obfuscated)
            return salted.substring(salt.length)
        }
    }

    private def String addPrefix(String data) {
        return prefix + data
    }

    private def String removePrefix(String data) {
        Preconditions.checkArgument(data.startsWith(prefix),"Prefix %s does not match in %s",prefix,data)
        return data.substring(prefix.length)
    }

    def getSeed() {
        seed
    }

    def getSalt() {
        salt
    }

    def getPrefix() {
        prefix
    }

}
