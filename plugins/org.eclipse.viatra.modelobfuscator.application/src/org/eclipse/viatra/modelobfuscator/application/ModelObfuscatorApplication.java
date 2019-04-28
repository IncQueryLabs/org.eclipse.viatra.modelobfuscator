/*******************************************************************************
 * Copyright (c) 2010-2014, Abel Hegedus, Istvan Rath and Daniel Varro
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-v20.html.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.viatra.modelobfuscator.application;

import java.util.Map;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.viatra.modelobfuscator.application.common.ModelObfuscatorHeadless;

/**
 * @author Abel Hegedus
 *
 */
public class ModelObfuscatorApplication implements IApplication {

    @Override
    public Object start(IApplicationContext context) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = context.getArguments();
        String[] args = (String[]) arguments.get("application.args");
        
        try {
            return new ModelObfuscatorHeadless().performHeadlessObfuscation(args);
        } catch (IllegalArgumentException e) {
            System.out.println("Obfuscation failed");
            return IApplication.EXIT_OK;
        }
        
    }

    @Override
    public void stop() {
    
    }


}
