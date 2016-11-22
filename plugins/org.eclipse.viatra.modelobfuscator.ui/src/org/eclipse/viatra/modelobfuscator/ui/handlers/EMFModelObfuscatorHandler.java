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
package org.eclipse.viatra.modelobfuscator.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.viatra.modelobfuscator.emf.simple.AbstractModelObfuscator;
import org.eclipse.viatra.modelobfuscator.emf.simple.EMFModelObfuscatorBuilder;
import org.eclipse.viatra.modelobfuscator.emf.simple.ResourceFilter;
import org.eclipse.viatra.modelobfuscator.emf.simple.SimpleEMFModelObfuscator;

/**
 * This command handler executes the model obfuscator in a way that only writable resources are modified in the resource
 * set of the editing domain provided by the editor.
 * 
 * <p/>The seed used for the obfuscator is displayed in a confirmation dialog and logged to the Eclips logger with Info
 * level.
 * 
 * <p/>The obfuscation is performed with a {@link SimpleEMFModelObfuscator} in an EMF command, therefore it supports undo/redo and correctly notifies the editor to
 * show a dirty state.
 * 
 * @author Abel Hegedus
 *
 */
public class EMFModelObfuscatorHandler extends AbstractModelObfuscatorHandler {

	@Override
	protected AbstractModelObfuscator createModelObfuscator(ResourceSet resourceSet, final EditingDomain editingDomain, ExecutionEvent event ) {
		return EMFModelObfuscatorBuilder.create().setInput(resourceSet)
                .setFilter(new ResourceFilter() {
                    @Override
                    public boolean avoidObfuscation(Resource resource) {
                        return editingDomain.isReadOnly(resource);
                    }
                }).build();
	}

}
