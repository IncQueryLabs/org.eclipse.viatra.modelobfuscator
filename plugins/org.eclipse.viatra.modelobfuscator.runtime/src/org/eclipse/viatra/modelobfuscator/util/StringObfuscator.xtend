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

import org.eclipse.viatra.modelobfuscator.api.DataTypeObfuscator
import com.google.common.io.BaseEncoding
import com.google.common.base.Preconditions

/**
 * @author Abel Hegedus
 *
 */
class StringObfuscator implements DataTypeObfuscator<String> {
  
  private String seed
  private String salt
  private String prefix
  
  new(String seed, String salt){
    Preconditions.checkArgument(seed != null, "Seed cannot be null")
    Preconditions.checkArgument(salt != null, "Salt cannot be null")
    this.seed = seed
    this.salt = salt
    this.prefix = ""
  }
  
  new(String seed, String salt, String prefix){
    Preconditions.checkArgument(seed != null, "Seed cannot be null")
    Preconditions.checkArgument(salt != null, "Salt cannot be null")
    Preconditions.checkArgument(prefix != null, "Prefix cannot be null")
    this.seed = seed
    this.salt = salt
    this.prefix = prefix
  }
  
  override obfuscateData(String original) {
    if(original != null){
      val salted = salt + original
      val obfuscatedBytes = ObfuscatorUtil.xorWithSeed(salted.bytes, seed.bytes)
      return addPrefix(BaseEncoding.base16.encode(obfuscatedBytes))
    }
  }
  
  override restoreData(String obfuscated) {
    if(obfuscated != null){
      val obfuscatedBytes = BaseEncoding.base16.decode(removePrefix(obfuscated))
      val salted = new String(ObfuscatorUtil.xorWithSeed(obfuscatedBytes, seed.bytes))
      return salted.substring(salt.length)
    }
  }
  
  private def String addPrefix(String data) {
  	return prefix+data
  }
  
  private def String removePrefix(String data) {
  	return data.substring(prefix.length)
  }
  
  def getSeed(){
    seed
  }
  
  def getSalt(){
    salt
  }
  
  def getPrefix(){
  	prefix
  }
  
}