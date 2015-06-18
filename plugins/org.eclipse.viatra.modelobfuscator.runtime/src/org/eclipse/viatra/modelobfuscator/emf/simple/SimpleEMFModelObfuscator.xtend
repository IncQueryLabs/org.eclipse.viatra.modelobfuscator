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
package org.eclipse.viatra.modelobfuscator.emf.simple

import org.eclipse.viatra.modelobfuscator.util.StringObfuscator

/**
 * This simple EMF obfuscator uses the TreeIterator of EcoreUtil to traverse all
 * resources in the resource set. Note that only resources loaded already are traversed!
 * 
 * <p/>Use {@link EMFModelObfuscatorBuilder} to create an instance of this class. 

 * <p/>The obfuscation modifies resources that are not filtered out by a {@link ResourceFilter}
 * and changes all values of (changeable, non-derived, non-volatile) String attributes using a {@link StringObfuscator}. 
 * 
 * <p/>Note that changes are done directly on the model, users must ensure correct invocation in case of editing domains, or 
 * transactional environments. 
 * 
 * @author Abel Hegedus
 *
 */
class SimpleEMFModelObfuscator extends AbstractModelObfuscator {

  protected override modifyData(String data, boolean obfuscate) {
  	var String retValue = ""
    if (obfuscate) {
      retValue = data.obfuscateData
    } else {
      retValue = data.restoreData
    }
    if(trace && data!=retValue) {
    	obfuscationMap.put(data, retValue)
    }
    return retValue
  }
}
