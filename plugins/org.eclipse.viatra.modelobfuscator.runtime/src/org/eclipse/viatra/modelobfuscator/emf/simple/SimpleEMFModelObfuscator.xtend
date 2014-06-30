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

import com.google.common.base.Preconditions
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.viatra.modelobfuscator.api.ModelObfuscator
import org.eclipse.viatra.modelobfuscator.util.StringObfuscator
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.resource.Resource

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
class SimpleEMFModelObfuscator implements ModelObfuscator {

  protected ResourceSet inputResourceSet
  protected ResourceFilter filter
  protected extension StringObfuscator stringObfuscator

  override obfuscate() {
    modifyModel(true)
  }

  override restore() {
    modifyModel(false)
  }

  protected def modifyModel(boolean obfuscate) {
    Preconditions.checkState(inputResourceSet != null, "Input resourceset must not be null")
    val resources = newArrayList
    resources += inputResourceSet.resources
    // resource list filtered first
    resources.filter[filter == null || !filter.avoidObfuscation(it)].forEach [
      // tree iterator filtered for instances of EObject
      EcoreUtil.getAllContents(it, true).filter(EObject).forEach [ obj |
        // only attributes modified
        obj.eClass.EAllAttributes.filter [
          // we don't need (or can't) to modify such attributes
          changeable && !derived && !volatile &&
          // the value must be String then
          EAttributeType.instanceClass == String
        ].forEach [
          val old = obj.eGet(it)
          if (many) {
            // handle EList values
            val oldValues = old as EList<String>
            val newValues = newArrayList
            oldValues.forEach [
              newValues += it.modifyData(obfuscate)
            ]
            oldValues.clear
            oldValues += newValues
          } else {
            // set single value
            val oldString = old as String
            obj.eSet(it, oldString.modifyData(obfuscate))
          }
        ]
      ]
    ]
  }

  /**
   * Returns the obfuscator that stores the seed and salt values as well.
   */
  def getStringObfuscator() {
    stringObfuscator
  }

  protected def modifyData(String data, boolean obfuscate) {
    if (obfuscate) {
      data.obfuscateData
    } else {
      data.restoreData
    }
  }
}

/**
 * Simple interface that can be used to filter out resources, which should
 * not be obfuscated in a resource set.
 * 
 * @author Abel Hegedus
 */
interface ResourceFilter {

  /**
   * Return true if resource should NOT be obfuscated
   */
  def boolean avoidObfuscation(Resource resource)

}
