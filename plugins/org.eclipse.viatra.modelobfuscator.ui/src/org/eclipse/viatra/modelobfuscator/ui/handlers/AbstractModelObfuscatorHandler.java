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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.viatra.modelobfuscator.api.ModelObfuscator;
import org.eclipse.viatra.modelobfuscator.ui.ModelObfuscatorUIPlugin;
import org.eclipse.viatra.modelobfuscator.ui.util.ModelObfuscatorUIHelper;

public abstract class AbstractModelObfuscatorHandler extends AbstractHandler {
	
	protected String mapFilePath;

    protected abstract ModelObfuscator createModelObfuscator(ResourceSet resourceSet, final EditingDomain editingDomain, ExecutionEvent event);
	
	@Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
        final Resource selectedResource = getSelectedResource(event);
        if(selectedResource==null) {
            MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Model obfuscation", "Sorry, there is no selected resource. You may need to open a diagram.");
            return null;
        }
        final String mapFilePath = selectedResource.getURI().toPlatformString(true)+".mapping_"+getDate();
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
                messageBuilder.append("- seed: ").append(seed);
                if(!salt.isEmpty()){
                    messageBuilder.append("\n- salt: ").append(salt);
                }
                if(!prefix.isEmpty()){
                    messageBuilder.append("\n- prefix: ").append(prefix);
                }
                String logMessage = messageBuilder.toString();
                messageBuilder.append("\n(logged also as an Info level event)");
                messageBuilder.append("\nDo you want to perform model obfuscation?");
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
	                      		print(mapFilePath, mapContent.toString());
                        	}
                        }
                    };
                    editingDomain.getCommandStack().execute(obfuscationCommand);
                }
            }
        }
        return null;
    }
	
    private String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
        Date date = new Date();
        return dateFormat.format(date);
    }
    
    protected Resource getSelectedResource(ExecutionEvent event) {
    	ISelection selection = HandlerUtil.getCurrentSelection(event);
    	
    	if (selection instanceof IStructuredSelection && ((IStructuredSelection)selection).toList().size()>0){
    		Object selectedElement = ((IStructuredSelection)selection).toList().get(0); 
    		return ModelObfuscatorUIHelper.getResource(selectedElement);
    	}
    	return null;
    }

    protected void print(String mapFilePath,String content) {
        try {
    	    IFile mapFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(mapFilePath+".csv"));
    	    int i = 0;
    	    while(mapFile.exists()) {
    	        i++;
    	        mapFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(mapFilePath+"_"+i+".csv"));
    	    }
    	    try {
                mapFile.create(new ByteArrayInputStream(content.getBytes("UTF-8")), true, null);
            } catch (CoreException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(content);
        }
    }
}
