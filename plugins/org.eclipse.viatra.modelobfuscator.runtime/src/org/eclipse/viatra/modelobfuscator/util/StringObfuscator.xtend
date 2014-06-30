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
  
  new(String seed, String salt){
    Preconditions.checkArgument(seed != null, "Seed cannot be null")
    Preconditions.checkArgument(salt != null, "Salt cannot be null")
    this.seed = seed
    this.salt = salt
  }
  
  override obfuscateData(String original) {
    if(original != null){
      val salted = salt + original
      val obfuscatedBytes = ObfuscatorUtil.xorWithSeed(salted.bytes, seed.bytes)
      return BaseEncoding.base64.encode(obfuscatedBytes)
    }
  }
  
  override restoreData(String obfuscated) {
    if(obfuscated != null){
      val obfuscatedBytes = BaseEncoding.base64.decode(obfuscated)
      val salted = new String(ObfuscatorUtil.xorWithSeed(obfuscatedBytes, seed.bytes))
      return salted.substring(salt.length)
    }
  }
  
  def getSeed(){
    seed
  }
  
  def getSalt(){
    salt
  }
  
}