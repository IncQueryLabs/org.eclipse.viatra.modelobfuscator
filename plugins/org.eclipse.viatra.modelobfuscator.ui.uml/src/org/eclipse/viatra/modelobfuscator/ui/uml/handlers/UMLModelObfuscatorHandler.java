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
package org.eclipse.viatra.modelobfuscator.ui.uml.handlers;

import java.util.HashMap;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.viatra.modelobfuscator.api.ModelObfuscator;
import org.eclipse.viatra.modelobfuscator.emf.simple.ResourceFilter;
import org.eclipse.viatra.modelobfuscator.emf.uml.UMLObfuscatorBuilder;
import org.eclipse.viatra.modelobfuscator.ui.handlers.AbstractModelObfuscatorHandler;

public class UMLModelObfuscatorHandler extends AbstractModelObfuscatorHandler {

    @Override
	protected ModelObfuscator createModelObfuscator(ResourceSet resourceSet, final EditingDomain editingDomain, ExecutionEvent event) {
		final Resource selectedResource = getSelectedResource(event);
		if(selectedResource==null) {
		    MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Model obfuscation", "Sorry, there is no selected resource. You may need to open a diagram.");
		    return null;
		}
		return UMLObfuscatorBuilder.create().setInput(resourceSet)
				.setTraceMap(new HashMap<String,String>())
                .setFilter(new ResourceFilter() {
                    @Override
                    public boolean avoidObfuscation(Resource resource) {
                        return (editingDomain.isReadOnly(resource) || resource!=selectedResource);
                    }
                }).build();
	}

}
