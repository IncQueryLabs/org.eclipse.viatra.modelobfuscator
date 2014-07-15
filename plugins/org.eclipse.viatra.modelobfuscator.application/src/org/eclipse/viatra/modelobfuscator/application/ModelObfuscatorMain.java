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
