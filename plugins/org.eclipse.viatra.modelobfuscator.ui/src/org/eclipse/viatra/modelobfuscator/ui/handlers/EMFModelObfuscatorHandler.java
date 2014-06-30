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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.viatra.modelobfuscator.emf.simple.EMFModelObfuscatorBuilder;
import org.eclipse.viatra.modelobfuscator.emf.simple.ResourceFilter;
import org.eclipse.viatra.modelobfuscator.emf.simple.SimpleEMFModelObfuscator;
import org.eclipse.viatra.modelobfuscator.ui.ModelObfuscatorUIPlugin;

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
public class EMFModelObfuscatorHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
        ResourceSet resourceSet = null;
        if (editorPart instanceof IEditingDomainProvider) {
            IEditingDomainProvider editingDomainProvider = (IEditingDomainProvider) editorPart;
            final EditingDomain editingDomain = editingDomainProvider.getEditingDomain();
            resourceSet = editingDomain.getResourceSet();
            if (resourceSet != null) {
                ILog logger = ModelObfuscatorUIPlugin.getDefault().getLog();
                final SimpleEMFModelObfuscator obfuscator = EMFModelObfuscatorBuilder.create().setInput(resourceSet)
                        .setFilter(new ResourceFilter() {
                            @Override
                            public boolean avoidObfuscation(Resource resource) {
                                return editingDomain.isReadOnly(resource);
                            }
                        }).build();
                String seed = obfuscator.getStringObfuscator().getSeed();

                boolean confirmed = MessageDialog
                        .openConfirm(
                                HandlerUtil.getActiveShell(event),
                                "Model obfuscation",
                                "This tool replaces all string attribute values with obfuscated values.\n"
                                        + " You can Undo the operation or restore the original values with this seed:\n"
                                        + seed + "\n(logged also as an Info level event)"
                                        + "Do you want to perform model obuscation?");
                if (confirmed) {
                    logger.log(new Status(Status.INFO, ModelObfuscatorUIPlugin.PLUGIN_ID, "Obfuscating with seed: "
                            + seed));
                    AbstractCommand obfuscationCommand = new AbstractCommand("Obfuscate model") {

                        @Override
                        public void redo() {
                            // obfuscation with the same seed is deterministic
                            obfuscator.obfuscate();
                        }

                        @Override
                        public void undo() {
                            obfuscator.restore();
                        }

                        @Override
                        public void execute() {
                            obfuscator.obfuscate();
                        }

                        @Override
                        protected boolean prepare() {
                            super.prepare();
                            return true;
                        }
                    };
                    editingDomain.getCommandStack().execute(obfuscationCommand);
                }
            }
        }
        return null;
    }

}
