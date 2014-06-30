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

import java.io.InputStream
import java.io.OutputStream
import java.util.ArrayDeque
import javax.xml.stream.XMLEventFactory
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.XMLEvent
import org.eclipse.viatra.modelobfuscator.api.ModelObfuscator
import org.eclipse.viatra.modelobfuscator.util.StringObfuscator

/**
 * This simple XML obfuscator uses the event-based StaX parser implementation to traverse 
 * the XML document on the input stream.
 * 
 * <p/>Use {@link XMLModelObfuscatorBuilder} to create an instance of this class. 

 * <p/>The obfuscation modifies values of attributes and content set in the provided {@link XMLSchemaConfiguration}
 *  using a {@link StringObfuscator}.
 * 
 * <p/>Note that changes are done on the output stream by piping (and sometimes modifying) events from the input reader. 
 * 
 * @author Abel Hegedus
 *
 */
class XMLModelObfuscator implements ModelObfuscator {

  protected InputStream inputStream
  protected OutputStream outputStream
  protected extension XMLSchemaConfiguration config
  protected extension StringObfuscator stringObfuscator
  protected boolean debug = false

  private extension XMLInputFactory = XMLInputFactory.newInstance
  private extension XMLOutputFactory = XMLOutputFactory.newInstance
  private extension XMLEventFactory = XMLEventFactory.newInstance

  override obfuscate() {
    modifyModel(true)
  }

  override restore() {
    modifyModel(false)
  }

  protected def modifyModel(boolean obfuscate) {
    val inputReader = createXMLEventReader(inputStream)
    val outputWriter = createXMLEventWriter(outputStream)

    // we need a stack to know which tag contains some text content  
    val tagStack = new ArrayDeque()
    while (inputReader.hasNext) {
      val event = inputReader.nextEvent

      switch (event.eventType) {
        case XMLEvent.START_ELEMENT: {
          val startEvent = event.asStartElement
          val tagName = startEvent.name.localPart
          tagStack.push(tagName)
          if (debug) {
            println("> start: " + tagName)
          }
          
          // see if there are attributes to obfuscate for this tag
          val attrsToObfuscate = tagName.toString.obfuscateableAttributeNames
          if (attrsToObfuscate.empty) {
            outputWriter.add(event)
          } else {
            val attributes = newArrayList()
            startEvent.attributes.filter(Attribute).forEach [
              if (attrsToObfuscate.contains(name.localPart)) {
                // attributes that need to be modified are dealt with
                val obfuscatedAttr = value.modifyData(obfuscate)
                if (debug) {
                  println(
                    " ! altering attribute: " + name + " from: " + value + " to: " + obfuscatedAttr)
                }
                attributes += createAttribute(name, obfuscatedAttr)
              } else {
                // no change needed
                attributes += it
              }
            ]
            
            // create new event for changed element
            val obfuscatedStart = createStartElement(startEvent.name, attributes.iterator,
              startEvent.namespaces)
            outputWriter.add(obfuscatedStart)
          }
        }
        case XMLEvent.CHARACTERS: {
          val charEvent = event.asCharacters
          val data = charEvent.data.trim
          if (!data.empty) {
            // the stack is not modified but we need to know what the current tag is
            val currentTag = tagStack.peek
            if (debug) {
              println("  - characters in " + currentTag + ": " + data)
            }

            if (currentTag.tagContentObfuscateable) {
              // contents of tag must be obfuscated
              val obfuscatedData = data.modifyData(obfuscate)
              if (debug) {
                println("  + change content to " + obfuscatedData)
              }
              // create modified event
              val obfuscatedEvent = createCharacters(obfuscatedData)
              outputWriter.add(obfuscatedEvent)
            } else {
              // content unmodified
              outputWriter.add(event)
            }
          } else {
            // even empty string events are needed on the output
            outputWriter.add(event)
          }
        }
        case XMLEvent.END_ELEMENT: {
          // just remove top from stack as tag is finished
          val tagName = tagStack.pop
          // NOTE: we assume correct input XML, so no check on tag name correctness is done!
          if (debug) {
            println("< end: " + tagName)
          }
          outputWriter.add(event)
        }
        default:
          // other events (such as control <? ?> are added)
          outputWriter.add(event)
      }
    }
    
    outputWriter.close
  }

  def getStringObfuscator() {
    stringObfuscator
  }

  protected def modifyData(String data, boolean obfuscate) {
    if (obfuscate) {
      data.obfuscateData
    } else {
      data.restoreData
    }
  }
}
