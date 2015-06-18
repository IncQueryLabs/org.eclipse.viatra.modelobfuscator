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

import org.eclipse.uml2.uml.Model
import org.eclipse.uml2.uml.Profile
import com.google.common.base.Preconditions
import org.eclipse.viatra.modelobfuscator.util.StringObfuscator

class UMLProfileReplacerBuilder {
	private Model inputModel
	private Profile newProfile
	private String seedString
	
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
	
	def setSeed(String seedString) {
		this.seedString = seedString
		return this
	}
	
	def build() {
    	Preconditions.checkState(inputModel != null, "Input model cannot be null")
    	Preconditions.checkState(newProfile != null, "New profile cannot be null")
    	Preconditions.checkState(seedString != null, "Seed string cannot be null")
    	val SimpleUMLProfileReplacer matcher = new SimpleUMLProfileReplacer()
    	matcher.inputModel = inputModel
    	matcher.newProfile = newProfile
    	matcher.seedString = seedString
    	matcher.stringObfuscator = new StringObfuscator(seedString, "", "o")
    	return matcher
	}
}