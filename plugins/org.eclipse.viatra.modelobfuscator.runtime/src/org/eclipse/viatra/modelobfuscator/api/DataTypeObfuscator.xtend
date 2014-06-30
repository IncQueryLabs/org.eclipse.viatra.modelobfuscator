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