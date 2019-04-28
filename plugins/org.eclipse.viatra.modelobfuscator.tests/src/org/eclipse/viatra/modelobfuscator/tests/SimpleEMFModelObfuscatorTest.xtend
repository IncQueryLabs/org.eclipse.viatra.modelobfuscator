/*******************************************************************************
 * Copyright (c) 2010-2014, Abel Hegedus, Istvan Rath and Daniel Varro
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-v20.html.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.viatra.modelobfuscator.tests

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.viatra.modelobfuscator.emf.simple.EMFModelObfuscatorBuilder
import org.junit.Test
import org.eclipse.viatra.examples.bpm.process.Process
import org.eclipse.viatra.examples.bpm.process.ProcessFactory

import static org.junit.Assert.*
import org.eclipse.viatra.modelobfuscator.util.ObfuscatorUtil
import org.junit.Ignore

/**
 * @author Abel Hegedus
 *
 */
class SimpleEMFModelObfuscatorTest {

  extension ProcessFactory processFactory = ProcessFactory.eINSTANCE

  @Test
  def void invokeObfuscatorNoSaltTest() {

    println("Salt NOT used for obfuscation")

    val rs = new ResourceSetImpl()
    val r = rs.createResource(URI.createURI("dummy"))

    val process = createProcess => [
      name = "MyProcess"
    ]
    r.contents += process
    val taskName = "T1"
    val task1 = createTaskWithNameInProcess(taskName, process)
    val task2 = createTaskWithNameInProcess(taskName, process)
    createTaskWithNameInProcess("MyTask", process)
    createTaskWithNameInProcess("MyOtherTask", process)
    val taskWithEmptyName = createTaskWithNameInProcess("", process)
    val taskWithNullName = createTaskWithNameInProcess(null, process)

    printProcessModel(process)

    val obfuscator = EMFModelObfuscatorBuilder.create.setInput(rs).setSalt("").setPrefix("").build
    println("Seed: " + obfuscator.stringObfuscator.seed)
    obfuscator.obfuscate

    printProcessModel(process)

    assertFalse("Obfuscator did not change process name", process.name == "MyProcess")
    assertTrue("Task names different", task1.name == task2.name)
    assertTrue("Empty task name changed", taskWithEmptyName.name == "")
    assertTrue("Null task name changed", taskWithNullName.name == null)

    assertTrue("Restored process name incorrect", "MyProcess" == obfuscator.stringObfuscator.restoreData(process.name))

  }

  @Test
  def void invokeObfuscatorWithSaltTest() {

    println("Salt used for obfuscation")

    val rs = new ResourceSetImpl()
    val r = rs.createResource(URI.createURI("dummy"))

    val process = createProcess => [
      name = "MyProcess"
    ]
    r.contents += process
    val taskName = "T1"
    val task1 = createTaskWithNameInProcess(taskName, process)
    val task2 = createTaskWithNameInProcess(taskName, process)
    createTaskWithNameInProcess("MyTask", process)
    createTaskWithNameInProcess("MyOtherTask", process)
    val taskWithEmptyName = createTaskWithNameInProcess("", process)
    val taskWithNullName = createTaskWithNameInProcess(null, process)

    printProcessModel(process)

    val obfuscator = EMFModelObfuscatorBuilder.create.setInput(rs).setSalt(ObfuscatorUtil.generateHexSeed(6)).build
    println("Seed: " + obfuscator.stringObfuscator.seed)
    println("Salt: " + obfuscator.stringObfuscator.salt)
    obfuscator.obfuscate

    printProcessModel(process)

    assertFalse("Obfuscator did not change process name", process.name == "MyProcess")
    assertTrue("Task names different", task1.name == task2.name)
    assertFalse("Empty task name not changed", taskWithEmptyName.name == "")
    assertTrue("Null task name changed", taskWithNullName.name == null)

    assertTrue("Restored process name incorrect", "MyProcess" == obfuscator.stringObfuscator.restoreData(process.name))
    assertTrue("Restored empty task name", obfuscator.stringObfuscator.restoreData(taskWithEmptyName.name) == "")
  }
  
  @Test
  def void invokeObfuscatorWithFilterTest() {

    println("Filter used for obfuscation")

    val rs = new ResourceSetImpl()
    val r = rs.createResource(URI.createURI("dummy"))
    val process = createProcess => [
      name = "MyProcess"
    ]
    r.contents += process
    val r2 = rs.createResource(URI.createURI("dummy2"))
    val process2 = createProcess => [
      name = "MyOtherProcess"
    ]
    r2.contents += process2

    printProcessModel(process)
    printProcessModel(process2)

    // do not obfuscate second resource
    val obfuscator = EMFModelObfuscatorBuilder.create.setInput(rs).setFilter[
      it == r2
    ].build
    println("Seed: " + obfuscator.stringObfuscator.seed)
    obfuscator.obfuscate

    printProcessModel(process)
    printProcessModel(process2)

    assertFalse("Obfuscator did not change process name", process.name == "MyProcess")
    assertTrue("Obfuscator changed process name", process2.name == "MyOtherProcess")

  }

  @Ignore
  @Test
  def void seedGeneratorTest() {
    println(ObfuscatorUtil.generateHexSeed(6))
    println(ObfuscatorUtil.generateHexSeed(6))
    println(ObfuscatorUtil.generateHexSeed(6))
    println(ObfuscatorUtil.generateHexSeed(6))
    println(ObfuscatorUtil.generateHexSeed(6))
    println(ObfuscatorUtil.generateHexSeed(6))
    println(ObfuscatorUtil.generateHexSeed(6))
    println(ObfuscatorUtil.generateHexSeed(10))
    println(ObfuscatorUtil.generateHexSeed(16))
    println(ObfuscatorUtil.generateHexSeed(21))
    println(ObfuscatorUtil.generateHexSeed(32))
    println(ObfuscatorUtil.generateHexSeed(36))
    println(ObfuscatorUtil.generateHexSeed(10))
    println(ObfuscatorUtil.generateHexSeed(16))
    println(ObfuscatorUtil.generateHexSeed(21))
    println(ObfuscatorUtil.generateHexSeed(32))
    println(ObfuscatorUtil.generateHexSeed(36))
  }

  def printProcessModel(Process process) {
    println(
      '''
        Model:
          Process: «process.name»
          Tasks:
            «FOR t : process.contents»
              Name: «t.name»«if(t.name == null) "[null]"»
            «ENDFOR»
      ''')
  }

  def createTaskWithNameInProcess(String taskName, Process process) {
    val task2 = createTask => [
      name = taskName
    ]
    process.contents += task2
    task2
  }

}
