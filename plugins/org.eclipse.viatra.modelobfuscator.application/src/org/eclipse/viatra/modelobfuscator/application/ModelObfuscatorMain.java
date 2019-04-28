/*******************************************************************************
 * Copyright (c) 2010-2014, Abel Hegedus, Istvan Rath and Daniel Varro
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-v20.html.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.viatra.modelobfuscator.application;

import org.eclipse.viatra.modelobfuscator.application.common.ModelObfuscatorHeadless;

/**
 * @author Abel Hegedus
 *
 */
public class ModelObfuscatorMain {

    public static void main(String[] args) {
        
        try {
            new ModelObfuscatorHeadless().performHeadlessObfuscation(args);
        } catch (IllegalArgumentException e) {
            System.out.println("Obfuscation failed");
        }
        
    }
    
}
