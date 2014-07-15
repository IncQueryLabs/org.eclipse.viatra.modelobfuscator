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
