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
package org.eclipse.viatra.modelobfuscator.xml

import com.google.common.base.Preconditions
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import java.util.Set

/**
 * Schema configuration is used to select which attributes and text content
 * is obfuscated in an XML document.
 * 
 * <p/>Text content of tags in the obfuscateableTags set are obfuscated.
 * <p/>Values of the specified attributes of specified tags in the obfuscateableAttributesOfTags
 * multimap are obfiscated. 
 * 
 * @author Abel Hegedus
 *
 */
class XMLSchemaConfiguration {
  
  private Multimap<String,String> obfuscateableAttributesOfTags
  private Set<String> obfuscateableTagContents
  
  new(Multimap<String,String> tagAttributes, Set<String> tags){
    obfuscateableAttributesOfTags = HashMultimap.create
    obfuscateableAttributesOfTags.putAll(tagAttributes)
    obfuscateableTagContents = newHashSet()
    obfuscateableTagContents.addAll(tags)
  }
  
  /**
   * Collection of attributes that should be obfuscated in the given tag.
   */
  def getObfuscateableAttributeNames(String tagName){
    Preconditions.checkArgument(tagName != null, "Tag name cannot be null!")
    return obfuscateableAttributesOfTags.get(tagName)
  }
  
  /**
   * Returns true if the text content of the tag should be obfuscated.
   */
  def isTagContentObfuscateable(String tagName){
    Preconditions.checkArgument(tagName != null, "Tag name cannot be null!")
    return obfuscateableTagContents.contains(tagName)
  }
}