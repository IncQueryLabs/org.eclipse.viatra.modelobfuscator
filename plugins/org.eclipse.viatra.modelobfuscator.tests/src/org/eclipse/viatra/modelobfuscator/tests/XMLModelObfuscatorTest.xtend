/*******************************************************************************
 * Copyright (c) 2010-2014, Abel Hegedus, Istvan Rath and Daniel Varro
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-v20.html.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.viatra.modelobfuscator.tests

import com.google.common.collect.HashMultimap
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.eclipse.viatra.modelobfuscator.xml.XMLModelObfuscatorBuilder
import org.eclipse.viatra.modelobfuscator.xml.XMLSchemaConfiguration
import org.junit.Test

/**
 * @author Abel Hegedus
 *
 */
class XMLModelObfuscatorTest {
  
  @Test
  def void invokeObfuscatorTest() {
    
    val input = '''
      <process name="MyProcess">
        <task id="T1">
          <someTag>Test</someTag>
        </task>
        <task id="MyTask" somaAttr="myAttrValue">
          <someTag>tst</someTag>
          <otherTag>1</otherTag>
        </task>
        someContent
        <task id="T1" otherAttr="2">
          <someTag>ts2</someTag>
          <someTag>ts3</someTag>
        </task>
      </process>
    '''
    val inputStream = new ByteArrayInputStream(input.bytes)
    val outputBytes = new ByteArrayOutputStream()
    val outputStream = new BufferedOutputStream(outputBytes)
    val attributeMultimap = HashMultimap.create => [
      put("process", "name")
      put("task","id")
      put("task","someAttr")
    ]
    val config = new XMLSchemaConfiguration(attributeMultimap, #{"someTag"})
    
    val obfuscator = XMLModelObfuscatorBuilder.create.setInput(inputStream).setOutput(outputStream).setSchemaConfiguration(config).build
    obfuscator.obfuscate
    
    println(outputBytes)
  }
  
  @Test
  def void xmiObfuscationTest(){
    val input = '''
      <?xml version="1.0" encoding="UTF-8"?>
      <root>
        <process:Process xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:process="http://process/1.0" id="rootProcess" name="Root">
          <contents xsi:type="process:Task" id="st2" name="Other Service Task" kind="service"/>
          <contents xsi:type="process:Task" id="st1" name="First Service Task" kind="service"/>
        </process:Process>
        <system:System xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:system="http://system/1.0" name="Simple">
          <contains name="Job1" runsOn="/">
            <taskIds>st1</taskIds>
          </contains>
          <contains name="Job2" runsOn="/">
            <taskIds>st2</taskIds>
          </contains>
          <data name="Data">
            <readingTaskIds>st1</readingTaskIds>
            <writingTaskIds>st2</writingTaskIds>
          </data>
        </system:System>
        <operation:Checklist xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:operation="http://operation/1.0" id="chl1" name="First Checklist" processId="rootProcess">
          <entries id="cle1" name="First CLE" taskId="st1">
            <info id="" name="Info1"/>
            <jobPaths>Simple/Job1</jobPaths>
          </entries>
        </operation:Checklist>
      </root>
    '''
    
    val inputStream = new ByteArrayInputStream(input.bytes)
    val outputBytes = new ByteArrayOutputStream()
    val outputStream = new BufferedOutputStream(outputBytes)
    val attributeMultimap = HashMultimap.create => [
      // process
      putAll("Process", #{"id","name"})
      putAll("contents",#{"id","name"})
      // system
      put("System","name")
      put("contains","name")
      put("data","name")
      // operation
      putAll("Checklist",#{"id","name","processId"})
      putAll("entries",#{"id","name","taskId"})
      putAll("info",#{"id","name"})
    ]
    val config = new XMLSchemaConfiguration(attributeMultimap, #{"taskIds","readingTaskIds","writingTaskIds","jobPaths"})
    
    // TODO jobPaths includes "/" which must be kept!
    
    val obfuscator = XMLModelObfuscatorBuilder.create.setInput(inputStream).setOutput(outputStream).setSchemaConfiguration(config).build
    obfuscator.obfuscate
    
    println(outputBytes)
  }
  
}