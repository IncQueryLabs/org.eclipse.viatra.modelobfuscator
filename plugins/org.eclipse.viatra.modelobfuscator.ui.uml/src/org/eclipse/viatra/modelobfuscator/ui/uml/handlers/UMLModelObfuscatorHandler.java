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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.viatra.modelobfuscator.api.ModelObfuscator;
import org.eclipse.viatra.modelobfuscator.emf.simple.ResourceFilter;
import org.eclipse.viatra.modelobfuscator.emf.uml.UMLObfuscatorBuilder;
import org.eclipse.viatra.modelobfuscator.ui.handlers.AbstractModelObfuscatorHandler;
import org.eclipse.viatra.modelobfuscator.ui.uml.util.UMLHelper;

public class UMLModelObfuscatorHandler extends AbstractModelObfuscatorHandler {

    private String mapFilePath;
    
	@Override
	protected ModelObfuscator createModelObfuscator(ResourceSet resourceSet, final EditingDomain editingDomain, ExecutionEvent event) {
		final Resource selectedResource = getSelectedResource(event);
		if(selectedResource==null) {
		    MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Model obfuscation", "Sorry, there is no selected resource. You may need to open a diagram.");
		    return null;
		}
		mapFilePath = deleteUmlExtension(selectedResource.getURI().toPlatformString(true))+".mapping_"+getDate();
		return UMLObfuscatorBuilder.create().setInput(resourceSet)
				.setTraceMap(new HashMap<String,String>())
                .setFilter(new ResourceFilter() {
                    @Override
                    public boolean avoidObfuscation(Resource resource) {
                        return (editingDomain.isReadOnly(resource) || resource!=selectedResource);
                    }
                }).build();
	}

    private Resource getSelectedResource(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		
		if (selection instanceof IStructuredSelection && ((IStructuredSelection)selection).toList().size()>0){
			Object selectedElement = ((IStructuredSelection)selection).toList().get(0); 
			return UMLHelper.getResource(selectedElement);
		}
		return null;
	}

	private String deleteUmlExtension(String platformString) {
        return platformString.substring(0, platformString.lastIndexOf(".uml"));
    }

    private String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
        Date date = new Date();
        return dateFormat.format(date);
    }
	
	@Override
	protected void print(String content) {
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
	        super.print(content);
	    }
	}

}
