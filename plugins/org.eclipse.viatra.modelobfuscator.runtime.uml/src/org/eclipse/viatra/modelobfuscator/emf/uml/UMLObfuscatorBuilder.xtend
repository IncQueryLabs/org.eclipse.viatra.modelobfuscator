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

import org.eclipse.viatra.modelobfuscator.emf.simple.EMFModelObfuscatorBuilder

class UMLObfuscatorBuilder extends EMFModelObfuscatorBuilder {

    /**
     * Hiding constructor of builder
     */
    protected new() {
        super()
    }

    /**
     * Creates a new, unconfigured builder for UML model obfuscators.
     */
    def static create() {
        new UMLObfuscatorBuilder
    }

    override protected createObfuscator() {
        return new SimpleUMLObfuscator()
    }
    
}