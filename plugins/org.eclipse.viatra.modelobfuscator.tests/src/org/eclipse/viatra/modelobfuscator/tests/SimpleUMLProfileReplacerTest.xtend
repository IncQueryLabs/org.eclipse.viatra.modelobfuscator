/*******************************************************************************
 * Copyright (c) 2010-2015, Tamas Borbas, Istvan Rath and Daniel Varro
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-v20.html.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.viatra.modelobfuscator.tests

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.uml2.uml.Class
import org.eclipse.uml2.uml.Model
import org.eclipse.uml2.uml.Profile
import org.eclipse.viatra.modelobfuscator.emf.uml.UMLProfileReplacerBuilder
import org.junit.Test

import static org.junit.Assert.*
import org.eclipse.uml2.uml.Stereotype
import java.math.BigInteger

class SimpleUMLProfileReplacerTest {
	private static final String SEED = "990d6121017dd960315eca3176a73bde"
	private static final String MODEL_PATH = "/org.eclipse.viatra.modelobfuscator.tests/model.uml"
	private static final String PROFILE_OBFUSCATED_PATH = "/org.eclipse.viatra.modelobfuscator.tests/profile_obfuscated.profile.uml"
	
	private static final String CLASS1_NAME = "ClassWithoutStereotype"
	private static final String CLASS2_NAME = "ClassWithStereotype"
	private static final String CLASS3_NAME = "ClassWithStereotypeAndModifiedAttributes"
	
	@Test
	def void invokeUMLProfileReplacer() {
		// Initialization
		val ResourceSet resourceSet = new ResourceSetImpl()
		val model = resourceSet.getRootObjectFromResource(MODEL_PATH) as Model
		val profileOriginal = model.allAppliedProfiles.head
		val profileObfuscated = resourceSet.getRootObjectFromResource(PROFILE_OBFUSCATED_PATH) as Profile
		
		if(model==null) {
			fail("Model cannot be loaded")
		}
		if(profileObfuscated==null) {
			fail("Obfuscated profile cannot be loaded")
		}
		if(profileOriginal==null) {
			fail("Original profile cannot be loaded")
		}
		
		
		// Testing
		val replacer = UMLProfileReplacerBuilder.create
												.setInput(model)
												.setNewProfile(profileObfuscated)
												.setSeed(new BigInteger(SEED,16)).build
		replacer.replace
		val stringObfuscator = replacer.stringObfuscator
		val myStereotype = profileOriginal.getMember("MyStereotype") as Stereotype
		val oStereotypeName = stringObfuscator.obfuscateData(myStereotype.name)
		
		
		// Checking result
		assertFalse("Replacer did not unapply old profile", model.allAppliedProfiles.contains(profileOriginal))
		assertTrue("Replacer did not apply new profile", model.allAppliedProfiles.contains(profileObfuscated))
		for(member : model.ownedMembers) {
			if(member instanceof Class) {
				switch member.name {
					case CLASS1_NAME: {
						assertTrue(CLASS1_NAME + " could not contain any stereotype", 
							member.appliedStereotypes.size==0
						)
					}
					case CLASS2_NAME: {
						assertFalse(CLASS2_NAME + " need to contain at least one stereotype", 
							member.appliedStereotypes.size<1
						)
						assertFalse(CLASS2_NAME + " need to contain at most one stereotype", 
							member.appliedStereotypes.size>1
						)
						val stereotype = member.appliedStereotypes.get(0)
						assertTrue((CLASS2_NAME + "'s stereotype's name should be " + oStereotypeName), 
							stereotype.name==oStereotypeName
						)
						val intAttrName = stringObfuscator.obfuscateData("MyIntegerValue")
						assertTrue(CLASS2_NAME + "'s int value should be 0",
							member.getValue(stereotype, intAttrName)==0)
						val stringAttrName = stringObfuscator.obfuscateData("MyStringValue")
						assertTrue(CLASS2_NAME + "'s string value should be 'Default string value.'",
							member.getValue(stereotype, stringAttrName)=="Default string value.")
					}
					case CLASS3_NAME: {
						assertFalse(CLASS3_NAME + " need to contain at least one stereotype", 
							member.appliedStereotypes.size<1
						)
						assertFalse(CLASS3_NAME + " need to contain at most one stereotype", 
							member.appliedStereotypes.size>1
						)
						val stereotype = member.appliedStereotypes.get(0)
						assertTrue((CLASS3_NAME + "'s stereotype's name should be " + oStereotypeName), 
							stereotype.name==oStereotypeName
						)
						val intAttrName = stringObfuscator.obfuscateData("MyIntegerValue")
						assertTrue(CLASS3_NAME + "'s int value should be 1",
							member.getValue(stereotype, intAttrName)==1)
						val stringAttrName = stringObfuscator.obfuscateData("MyStringValue")
						assertTrue(CLASS3_NAME + "'s string value should be 'Not default string value.'",
							member.getValue(stereotype, stringAttrName)=="Not default string value.")
					}
						
				}
			}
		}
	}
	
	private def EObject getRootObjectFromResource(ResourceSet rs, String path) {
		val URI resourceUri = URI.createPlatformResourceURI(path, true)
		val Resource selectedResource = rs.getResource(resourceUri, true)
		val root = selectedResource.getContents().get(0)
		return root
	}
}