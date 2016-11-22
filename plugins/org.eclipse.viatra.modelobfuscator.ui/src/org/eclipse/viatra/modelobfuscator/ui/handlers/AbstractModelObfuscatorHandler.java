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
                    return null;
                }
                final String seed = obfuscator.getStringObfuscator().getSeed();
                final String salt = obfuscator.getStringObfuscator().getSalt();
                final String prefix = obfuscator.getStringObfuscator().getPrefix();

                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append("This tool replaces all string attribute values with obfuscated values.\n");
                messageBuilder.append("You can Undo the operation or restore the original values with\n");
                messageBuilder.append("- seed:").append(seed);
                if(!salt.isEmpty()){
                    messageBuilder.append("\n- salt:").append(salt);
                }
                if(!prefix.isEmpty()){
                    messageBuilder.append("\n- prefix:").append(prefix);
                }
                String logMessage = messageBuilder.toString();
                messageBuilder.append("\n(logged also as an Info level event)");
                messageBuilder.append("Do you want to perform model obuscation?");
                String message = messageBuilder.toString();
                boolean confirmed = MessageDialog.openConfirm(
                                HandlerUtil.getActiveShell(event),
                                "Model obfuscation",
                                message);
                if (confirmed) {
                	logger.log(new Status(Status.INFO, ModelObfuscatorUIPlugin.PLUGIN_ID, logMessage));
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
                            printout(oMap);
                        }

                        @Override
                        protected boolean prepare() {
                            super.prepare();
                            return true;
                        }
                        
                        private void printout(Map<String,String> oMap) {
                        	if(oMap!=null) {
	                        	StringBuffer mapContent = new StringBuffer("Original;Modified;Obfuscation seed: " + seed + ";");
	                      		if(!salt.isEmpty()){
	                      		    mapContent.append("Salt: " + salt + ";");
	                      		}
	                      		if(!prefix.isEmpty()){
	                      		    mapContent.append("Prefix: " + prefix + ";");
	                      		}
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
	
	/**
	 * Subclasses should override this method to store the printed content to file
	 */
	protected void print(String content) {
	    System.out.println(content);
	}
}
