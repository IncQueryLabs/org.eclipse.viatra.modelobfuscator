/*******************************************************************************
 * Copyright (c) 2010-2014, Abel Hegedus, Istvan Rath and Daniel Varro
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-v20.html.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.viatra.modelobfuscator.api

import org.eclipse.viatra.modelobfuscator.util.StringObfuscator

/**
 * Obfuscator interface for a single data type (see {@link StringObfuscator} for example).
 * 
 * @author Abel Hegedus
 *
 */
interface DataTypeObfuscator<DataType> {
  
  /**
   * Takes the original data and returns its obfuscated form
   */
  def DataType obfuscateData(DataType original)
  
  /**
   * Takes the obfuscated data and returns its original form (as long as the 
   * obfuscation was done with an obfuscator set up with the same values).
   */
  def DataType restoreData(DataType obfuscated)
  
}