/*******************************************************************************
 * Copyright (c) 2010-2014, Abel Hegedus, Istvan Rath and Daniel Varro
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-v20.html.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.viatra.modelobfuscator.xml

import com.google.common.base.Preconditions
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import org.eclipse.viatra.modelobfuscator.util.ObfuscatorUtil
import org.eclipse.viatra.modelobfuscator.util.StringObfuscator

/**
 * Builder API for setting up an XML obfuscator. Currently the only implementation that can
 * be built is {@link XMLModelObfuscator}. the input for the obfuscator is an InputStream, while the output is 
 * written into an OutputStream. The obfuscated attributes and content is configured by a {@link XMLSchemaConfiguration}. 
 *  
 * <p/>The builder allows setting a seed and a salt for the obfuscator, which is used when initializing
 * the {@link StringObfuscator}, thus supporting reproducibility and restoration.
 * 
 * <p/>The default value of the seed is a random, 32 character long, hexadecimal string created by
 * {@link ObfuscatorUtil#generateHexSeed}. The default value of the salt is empty.
 * 
 * @author Abel Hegedus
 * 
 */
class XMLModelObfuscatorBuilder {

    private InputStream inputStream
    private OutputStream outputStream
    private extension XMLSchemaConfiguration config
    protected extension StringObfuscator stringObfuscator
    private String saltString = ""
    private String prefix = ""
    private BigInteger seedNumber = new BigInteger(ObfuscatorUtil.generateBase36RandomString(32), 36)

    /**
     * Hiding constructor of builder
     */
    protected new() {
    }

    /**
     * Creates a new, unconfigured builder for XML model obfuscators.
     */
    def static create() {
        new XMLModelObfuscatorBuilder
    }

    /**
     * Sets the input of the built obfuscator
     */
    def setInput(InputStream input) {
        inputStream = input
        return this
    }

    /**
     * Sets the output of the built obfuscator
     */
    def setOutput(OutputStream output) {
        outputStream = output
        return this
    }

    /**
     * Sets the configuration used for selecting what to obfuscate.
     * See {@link XMLSchemaConfiguration} for details.
     */
    def setSchemaConfiguration(XMLSchemaConfiguration configuration) {
        this.config = configuration
        return this
    }

    /**
     * Sets the salt used for obfuscating String values.
     * See {@link StringObfuscator} for details.
     */
    def setSalt(String salt) {
        saltString = salt
        return this
    }

    /**
     * Sets the seed used for obfuscating String values.
     * See {@link StringObfuscator} for details.
     * <p>
     * Must be a base 36 number!
     * 
     * @throw NumberFormatException if seed is not base 36
     */
    @Deprecated
    def setSeed(String seed) {
        seedNumber = new BigInteger(seed, 36)
        return this
    }

    /**
     * Sets the seed used for obfuscating String values.
     * See {@link StringObfuscator} for details.
     */
    def setSeed(BigInteger seed) {
        seedNumber = seed
        return this
    }

    def getSeed() {
        return seedNumber.toString(36)
    }

    def getSalt() {
        return saltString
    }

    /**
     * Returns the {@link XMLModelObfuscator} instance built using the current configuration of the builder. 
     */
    def build() {
        Preconditions.checkState(inputStream != null, "Input resource set cannot be null")
        Preconditions.checkState(outputStream != null, "Output resource set cannot be null")
        Preconditions.checkState(seedNumber != null, "Seed cannot be null")
        Preconditions.checkState(prefix != null, "Prefix cannot be null (empty string allowed)")
        Preconditions.checkState(saltString != null, "Salt cannot be null (empty string allowed)")
        val obfuscator = new XMLModelObfuscator()
        obfuscator.inputStream = inputStream
        obfuscator.outputStream = outputStream
        obfuscator.config = config
        obfuscator.stringObfuscator = new StringObfuscator(seedNumber, saltString, prefix)
        return obfuscator
    }

}
