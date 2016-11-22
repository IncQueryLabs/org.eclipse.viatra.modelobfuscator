/*******************************************************************************
 * Copyright (c) 2010-2015, Tamas Borbas, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Tamas Borbas - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra.modelobfuscator.emf.uml

import com.google.common.base.Preconditions
import java.math.BigInteger
import org.eclipse.uml2.uml.Model
import org.eclipse.uml2.uml.Profile
import org.eclipse.viatra.modelobfuscator.util.StringObfuscator

class UMLProfileReplacerBuilder {
	private Model inputModel
	private Profile newProfile
	private BigInteger seedNumber
    private String prefix = ""
	private String saltString = ""
    
	protected new() {}
	
	def static create() {
		new UMLProfileReplacerBuilder
	}
	
	def setInput(Model inputModel) {
		this.inputModel = inputModel
		return this
	}
	
	def setNewProfile(Profile newProfile) {
		this.newProfile = newProfile
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
    
    /**
     * Sets the salt used for obfuscating String values.
     * See {@link StringObfuscator} for details.
     * 
     */
    def setSalt(String salt) {
        saltString = salt
        return this
    }
    
    /**
     * Sets the prefix used for obfuscating String values.
     * See {@link StringObfuscator} for details.
     */
    def setPrefix(String prefix) {
        this.prefix = prefix
        return this
    }
	
	def build() {
    	Preconditions.checkState(inputModel != null, "Input model cannot be null")
    	Preconditions.checkState(newProfile != null, "New profile cannot be null")
    	Preconditions.checkState(seedNumber != null, "Salt string cannot be null")
    	Preconditions.checkState(prefix != null, "Prefix cannot be null (empty string allowed)")
        Preconditions.checkState(saltString != null, "Salt cannot be null (empty string allowed)")
        val SimpleUMLProfileReplacer replacer = new SimpleUMLProfileReplacer()
    	replacer.inputModel = inputModel
    	replacer.newProfile = newProfile
    	replacer.stringObfuscator = new StringObfuscator(seedNumber, saltString, prefix)
    	return replacer
	}
}