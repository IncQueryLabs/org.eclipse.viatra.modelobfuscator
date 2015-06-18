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
import org.eclipse.viatra.modelobfuscator.emf.simple.ResourceFilter
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.viatra.modelobfuscator.util.ObfuscatorUtil
import org.eclipse.viatra.modelobfuscator.util.StringObfuscator

class UMLObfuscatorBuilder {

  private ResourceSet inputRS
  private ResourceFilter filter
  private String saltString = ""
  private String seedString = ObfuscatorUtil.generateHexSeed(32)
  private Map<String,String> obfuscationMap = null

  /**
   * Hiding constructor of builder
   */
  protected new() {}

  /**
   * Creates a new, unconfigured builder for UML model obfuscators.
   */
  def static create() {
    new UMLObfuscatorBuilder
  }

  /**
   * Sets the input of the built obfuscator
   */
  def setInput(ResourceSet inputResourceSet) {
    inputRS = inputResourceSet
    return this
  }

  /**
   * Sets the filter used in the built obfuscator.
   * See {@link ResourceFilter} for details.
   */
  def setFilter(ResourceFilter filter) {
    this.filter = filter
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
  
  def setTraceMap(Map<String,String> obfuscationMap) {
  	this.obfuscationMap = obfuscationMap
  	return this
  }

  def getSeed(){
    return seedString
  }
  
  def getSalt(){
    return saltString
  }

  /**
   * Sets the seed used for obfuscating String values.
   * See {@link StringObfuscator} for details.
   */
  def setSeed(String seed) {
    seedString = seed
    return this
  }

  /**
   * Returns the {@link SimpleEMFModelObfuscator} instance built using the current configuration of the builder. 
   */
  def build() {
    Preconditions.checkState(inputRS != null, "Input resource set cannot be null")
    Preconditions.checkState(seedString != null, "Seed cannot be null")
    Preconditions.checkState(saltString != null, "Salt cannot be null (empty string allowed)")
    val SimpleUMLObfuscator obfuscator = new SimpleUMLObfuscator()
    obfuscator.inputResourceSet = inputRS
    obfuscator.filter = filter
    obfuscator.stringObfuscator = new StringObfuscator(seedString, saltString, "o")
    if(obfuscationMap!=null) {
    	obfuscator.obfuscationMap = obfuscationMap
    	obfuscator.trace = true
    }
    return obfuscator
  }
}