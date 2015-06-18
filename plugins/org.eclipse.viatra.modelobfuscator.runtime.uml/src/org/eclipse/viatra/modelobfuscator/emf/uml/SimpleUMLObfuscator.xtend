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

import java.util.List
import java.util.Map
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.viatra.modelobfuscator.emf.simple.AbstractModelObfuscator
import org.eclipse.viatra.modelobfuscator.emf.simple.ResourceFilter
import org.eclipse.viatra.modelobfuscator.util.StringObfuscator

class SimpleUMLObfuscator extends AbstractModelObfuscator {
	
  protected List<String> ignoreableStartings = newArrayList(
  	"base_",
  	"http://www"
  )

	protected override modifyData(String data, boolean obfuscate) {
		var String retValue = ""
		
		if(data.isIgnored) {
			retValue = data
		} else if (obfuscate) {
			if(data.startsWith("extension_")) {
				retValue = "extension_"+data.replace("extension_","").obfuscateData
			} else if(data.startsWith("http:///schemas/")) {
				var splitted = data.replace("http:///schemas/","").split("/",2)
				retValue = "http:///schemas/"+splitted.get(0).obfuscateData+"/"+splitted.get(1)
			} else {
				retValue = data.obfuscateData
			}
		} else {
			if(data.startsWith("extension_")) {
				retValue = "extension_"+data.replace("extension_","").restoreData
			} else if(data.startsWith("http:///schemas/")) {
				var splitted = data.replace("http:///schemas/","").split("/",2)
				retValue = "http:///schemas/"+splitted.get(0).restoreData+"/"+splitted.get(1)
			} else {
				retValue = data.restoreData
			}
		}
		
	    if(trace && data!=retValue) {
	    	obfuscationMap.put(data, retValue)
	    }
	    
	    return retValue
	}
	
	def isIgnored(String data) {
		if(data==null)
			return true
		for(starting : ignoreableStartings) {
			if(data.startsWith(starting)) {
				return true
			}
		}
		return false
	}
	
	protected def setObfuscationMap(Map<String,String> obfuscationMap) {
		this.obfuscationMap = obfuscationMap
	}
	
	protected def setTrace(boolean trace) {
		this.trace = trace
	}
	
	protected def setInputResourceSet(ResourceSet inputResourceSet) {
		this.inputResourceSet = inputResourceSet
	}
	
	protected def setFilter(ResourceFilter filter) {
		this.filter = filter
	}

	protected def setStringObfuscator(StringObfuscator stringObfuscator) {
		this.stringObfuscator = stringObfuscator
	}
}