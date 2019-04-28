/*******************************************************************************
 * Copyright (c) 2010-2015, Tamas Borbas, Istvan Rath and Daniel Varro
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-v20.html.
 * 
 * SPDX-License-Identifier: EPL-2.0
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