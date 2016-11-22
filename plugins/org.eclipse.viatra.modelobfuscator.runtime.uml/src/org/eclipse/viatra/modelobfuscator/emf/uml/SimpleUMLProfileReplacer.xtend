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
import java.util.Map
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.uml2.uml.Element
import org.eclipse.uml2.uml.Enumeration
import org.eclipse.uml2.uml.EnumerationLiteral
import org.eclipse.uml2.uml.Model
import org.eclipse.uml2.uml.Profile
import org.eclipse.uml2.uml.Stereotype
import org.eclipse.viatra.modelobfuscator.util.StringObfuscator

class SimpleUMLProfileReplacer {
	protected Model inputModel
	protected Profile oldProfile
	protected Profile newProfile
	protected String saltString = ""
	protected extension StringObfuscator stringObfuscator
	private boolean toOriginal = false

	def replace() {
		replace(false)
	}

	def replace(boolean toOriginal) {
		Preconditions.checkState(inputModel != null, "Input resource set must not be null")
		Preconditions.checkState(newProfile != null, "New profile must not be null")
		Preconditions.checkState(stringObfuscator != null, "String obfuscator must not be null")
		
		val prevDir = this.toOriginal
		this.toOriginal = toOriginal
		if(toOriginal!=prevDir) {
			val temp = newProfile
			newProfile = oldProfile
			oldProfile = temp
		}
		// Search for replaceable profile
		oldProfile = inputModel.appliedProfiles.findFirst[(it.name.modify)==newProfile.name]
		if(oldProfile==null) {
			throw new IllegalArgumentException("Cannot find old profile for selected new profile!")
		}
		inputModel.eResource.resourceSet.resources.add(newProfile.eResource)
		
		// Collect replaceable stereotypes, their pair and new enumerations
		val oldStereotypes = newArrayList()
		EcoreUtil.getAllContents(oldProfile, true).forEach[obj | if(obj instanceof Stereotype) { oldStereotypes+=obj } ]
		val newStereotypes = newArrayList()
		val newEnums = newArrayList()
		EcoreUtil.getAllContents(newProfile, true).forEach[obj | 
			if(obj instanceof Stereotype) { 
				newStereotypes+=obj
			} else if (obj instanceof Enumeration) {
				newEnums+=obj
			}
		]
		// Map the old and new stereotypes
		val Map<Stereotype,Stereotype> stereotypeMap = newHashMap()
		oldStereotypes.forEach[old|
			val obfuscatedNameOfOldSt = old.name.modify
			val newS = newStereotypes.findFirst[news|
				val newSName = news.name
				obfuscatedNameOfOldSt.equals(newSName)
			]
			stereotypeMap.put(old, newS)
		]
		
		// Apply the new profile
		inputModel.applyProfile(newProfile)
		
		EcoreUtil.getAllContents(inputModel, true).forEach[obj | 
			if(obj instanceof Element) {
				val deletableStereotypes = newArrayList()
				
				obj.appliedStereotypes.filter[oldStereotypes.contains(it)].forEach[oldStereotype |
					val newStereotype = stereotypeMap.get(oldStereotype)
					obj.applyStereotype(newStereotype)
					
					// Save the old stereotype's attributes to the new one
					oldStereotype.attributes.filter[!readOnly && !derived].forEach[attr | 
						if(!attr.name.contains("base_") && !attr.name.contains("extension_")) {
							val value = obj.getValue(oldStereotype, attr.name)
							
							if(value instanceof EnumerationLiteral) {
								// Copy the correct literal value
								val oldEnum = attr.type
								val newEnum = newEnums.findFirst[it.name.equals(oldEnum.name.modify)]
								val newValue = newEnum.getOwnedLiteral(value.name.modify)
								obj.setValue(newStereotype, attr.name.modify, newValue)
							} else {
								// Works only with values which type has not been obfuscated
								obj.setValue(newStereotype, attr.name.modify, value)
							}
							
						}
					]
					
					deletableStereotypes+=oldStereotype
				]
				deletableStereotypes.forEach[dSt|
					obj.unapplyStereotype(dSt)
				]
			
			}
		]
		
		// Unapply the old profile
		inputModel.unapplyProfile(oldProfile)
		return
	}
	
	def modify(String data) {
		if(toOriginal) {
			return data.restoreData
		} else {
			return data.obfuscateData
		}
		
	}
	
	def getStringObfuscator() {
		return this.stringObfuscator
	}
}