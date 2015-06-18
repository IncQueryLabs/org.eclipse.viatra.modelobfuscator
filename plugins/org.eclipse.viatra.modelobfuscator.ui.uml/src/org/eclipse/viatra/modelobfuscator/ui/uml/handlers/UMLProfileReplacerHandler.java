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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.viatra.modelobfuscator.emf.uml.SimpleUMLProfileReplacer;
import org.eclipse.viatra.modelobfuscator.emf.uml.UMLProfileReplacerBuilder;
import org.eclipse.viatra.modelobfuscator.ui.uml.util.UMLHelper;

public class UMLProfileReplacerHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
		if (editorPart instanceof IEditingDomainProvider) {
			IEditingDomainProvider editingDomainProvider = (IEditingDomainProvider) editorPart;
			final EditingDomain editingDomain = editingDomainProvider.getEditingDomain();
			
			// Select obfuscated profile
			List<ViewerFilter> viewerFilters = new ArrayList<ViewerFilter>();
			viewerFilters.add(new ViewerFilter() {
				
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					if(element instanceof IFile)
						return "uml".equals(((IFile)element).getFileExtension()) && ((IFile)element).getName().contains("profile");
					return true;
				}
			});
			Shell shell = Display.getCurrent().getActiveShell();
			IFile[] selectedFiles = WorkspaceResourceDialog.openFileSelection(
																	shell, 
																	"Profile replacer", 
																	"Select the obfuscated profile", 
																	false, null, viewerFilters);
			if(selectedFiles.length<1) {
				return null;
			}
			ResourceSet resourceSet = new ResourceSetImpl();
			URI resourceUri = URI.createPlatformResourceURI(selectedFiles[0].getFullPath().toString(), true);
			Resource selectedResource = resourceSet.getResource(resourceUri, true);
			EObject obj = selectedResource.getContents().get(0);
			Profile selectedProfile = (Profile) obj;
			
			// Set seedString
			InputDialog seedReader = new InputDialog(Display.getCurrent().getActiveShell(), 
							"Profile replacer", 
							"The obfuscated profile's seed", 
							"", new IInputValidator() {
								
								@Override
								public String isValid(String newText) {
									if (newText.length()>0) {
										return null;
									}
									return "Invalid seed";
								}
							});
			if(seedReader.open()!=Window.OK) {
				return null;
			}
			String seedString = seedReader.getValue();
			
			Model selectedModel = getSelectedModel(event);
			if(selectedModel==null) {
				MessageDialog.openInformation(
						Display.getCurrent().getActiveShell(), 
						"Profile replacer", 
						"Sorry, no model found to the selection (maybe you try to select a Profile).");
				return null;
			}
			final SimpleUMLProfileReplacer matcher = UMLProfileReplacerBuilder.create()
					.setInput(selectedModel)
					.setNewProfile(selectedProfile)
					.setSeed(seedString).build();
			
			AbstractCommand replaceCommand = new AbstractCommand("Replacing profile") {

                @Override
                public void redo() {
                    matcher.replace(false);
                }

                @Override
                public void undo() {
                    matcher.replace(true);
                }

                @Override
                public void execute() {
                    matcher.replace(false);
                }

                @Override
                protected boolean prepare() {
                    super.prepare();
                    return true;
                }
            };
            editingDomain.getCommandStack().execute(replaceCommand);
		}
		return null;
	}

	private Model getSelectedModel(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection
				&& ((IStructuredSelection) selection).toList().size() > 0) {
			Object selectedElement = ((IStructuredSelection) selection).toList().get(0);
			Resource selectedResource = UMLHelper.getResource(selectedElement);
			EObject eObject = selectedResource.getContents().get(0);
            if(eObject instanceof Model)
			    return (Model) eObject;
		}
		return null;
	}

}
