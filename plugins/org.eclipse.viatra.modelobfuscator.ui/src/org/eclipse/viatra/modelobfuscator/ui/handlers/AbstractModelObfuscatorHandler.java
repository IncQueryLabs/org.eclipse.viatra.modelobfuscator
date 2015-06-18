/*******************************************************************************
 * Copyright (c) 2010-2015, Abel Hegedus, Tamas Borbas, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abel Hegedus - initial API and implementation
 *   Tamas Borbas - adapting
 *******************************************************************************/
package org.eclipse.viatra.modelobfuscator.ui.handlers;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.viatra.modelobfuscator.api.ModelObfuscator;
import org.eclipse.viatra.modelobfuscator.ui.ModelObfuscatorUIPlugin;

public abstract class AbstractModelObfuscatorHandler extends AbstractHandler {
	
	protected abstract ModelObfuscator createModelObfuscator(ResourceSet resourceSet, final EditingDomain editingDomain, ExecutionEvent event);
	
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
                final ModelObfuscator obfuscator = createModelObfuscator(resourceSet, editingDomain, event);
                if(obfuscator==null) {
                    System.out.println("No obfuscator.");
                    return null;
                }
                final String seed = obfuscator.getStringObfuscator().getSeed();

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
                            Map<String,String> oMap = obfuscator.getObfuscationMap();
                            printout(oMap, seed);
                        }

                        @Override
                        protected boolean prepare() {
                            super.prepare();
                            return true;
                        }
                        
                        private void printout(Map<String,String> oMap, String seed) {
                        	if(oMap!=null) {
	                        	StringBuffer mapContent = new StringBuffer("Original;Modified;Obfuscation seed: "+seed+";");
	                      		for (String key : oMap.keySet()) {
									mapContent.append("\n"+key+";"+oMap.get(key)+";");
								}
	                      		print(mapContent.toString());
                        	}
                        }
                    };
                    editingDomain.getCommandStack().execute(obfuscationCommand);
                }
            }
        }
        return null;
    }
	
	protected void print(String content) {
	    System.out.println(content);
	}
}
