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
package org.eclipse.viatra.modelobfuscator.ui.util;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

public class ModelObfuscatorUIHelper {
    protected ModelObfuscatorUIHelper() {}
    
    public static Resource getResource(Object element) {
        if (element instanceof EObject){
            EObject obj = (EObject) element;
            return obj.eResource();
        } else if (element instanceof IAdaptable){
            IAdaptable adaptableElement = (IAdaptable)element;
            EObject obj = (EObject)adaptableElement.getAdapter(EObject.class);
            if(obj==null) {
                obj = (EObject)Platform.getAdapterManager().getAdapter(adaptableElement, EObject.class);
            }
            if(obj!=null) {
                return obj.eResource();
            }
        } else if (element instanceof Resource) {
            return (Resource) element;
        }
        return null;
    }
}
