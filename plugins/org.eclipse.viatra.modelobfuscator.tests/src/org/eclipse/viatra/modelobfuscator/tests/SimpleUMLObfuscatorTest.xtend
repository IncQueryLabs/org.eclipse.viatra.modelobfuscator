/*******************************************************************************
 * Copyright (c) 2010-2015, Tamas Borbas, Istvan Rath and Daniel Varro
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-v20.html.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.viatra.modelobfuscator.tests

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.uml2.uml.Model
import org.eclipse.uml2.uml.UMLFactory
import org.eclipse.viatra.modelobfuscator.emf.uml.UMLObfuscatorBuilder
import org.eclipse.viatra.modelobfuscator.util.ObfuscatorUtil
import org.junit.Test

import static org.junit.Assert.*

class SimpleUMLObfuscatorTest {
	extension UMLFactory umlFactory = UMLFactory.eINSTANCE
	private static final String TESTMODEL_NAME = "MyModel"
  
  @Test
  def void invokeObfuscatorNoSaltTest() {

    println("Salt NOT used for obfuscation")

    val rs = new ResourceSetImpl()
    val r = rs.createResource(URI.createURI("dummy"))

    val model = createModel => [
      name = TESTMODEL_NAME
    ]
    r.contents += model
    val className = "C1"
    val class1 = model.createOwnedClass(className, false)
    val class2 = model.createOwnedClass(className, false)
    model.createOwnedClass("MyClass", false)
    model.createOwnedClass("MyAbstractClass", true)
    val classWithEmptyName = model.createOwnedClass("", true)
    val classWithNullName = model.createOwnedClass(null, true)

    printUMLModel(model)

    val obfuscator = UMLObfuscatorBuilder.create.setInput(rs).setSalt("").setPrefix("").build
    println("Seed: " + obfuscator.stringObfuscator.seed)
    obfuscator.obfuscate

    printUMLModel(model)

    assertFalse("Obfuscator did not change model name", model.name == TESTMODEL_NAME)
    assertTrue("Class names different", class1.name == class2.name)
    assertTrue("Empty class name changed", classWithEmptyName.name == obfuscator.stringObfuscator.prefix)
    assertTrue("Null class name changed", classWithNullName.name == null)

    assertTrue("Restored model name incorrect", TESTMODEL_NAME == obfuscator.stringObfuscator.restoreData(model.name))

  }

  @Test
  def void invokeObfuscatorWithSaltTest() {

    println("Salt used for obfuscation")

    val rs = new ResourceSetImpl()
    val r = rs.createResource(URI.createURI("dummy"))

    val model = createModel => [
      name = TESTMODEL_NAME
    ]
    r.contents += model
    val className = "C1"
    val class1 = model.createOwnedClass(className, false)
    val class2 = model.createOwnedClass(className, false)
    model.createOwnedClass("MyClass", false)
    model.createOwnedClass("MyAbstractClass", true)
    val classWithEmptyName = model.createOwnedClass("", true)
    val classWithNullName = model.createOwnedClass(null, true)

    printUMLModel(model)

    val obfuscator = UMLObfuscatorBuilder.create.setInput(rs).setSalt(ObfuscatorUtil.generateHexSeed(6)).build
    println("Seed: " + obfuscator.stringObfuscator.seed)
    println("Salt: " + obfuscator.stringObfuscator.salt)
    obfuscator.obfuscate

    printUMLModel(model)

    assertFalse("Obfuscator did not change model name", model.name == TESTMODEL_NAME)
    assertTrue("Class names different", class1.name == class2.name)
    assertFalse("Empty class name not changed", classWithEmptyName.name == "")
    assertTrue("Null class name changed", classWithNullName.name == null)

    assertTrue("Restored model name incorrect", TESTMODEL_NAME == obfuscator.stringObfuscator.restoreData(model.name))
    assertTrue("Restored empty class name", obfuscator.stringObfuscator.restoreData(classWithEmptyName.name) == "")
  }
  
  @Test
  def void invokeObfuscatorWithFilterTest() {

    println("Filter used for obfuscation")

    val rs = new ResourceSetImpl()
    val r = rs.createResource(URI.createURI("dummy"))
    val model = createModel => [
      name = TESTMODEL_NAME
    ]
    r.contents += model
    val r2 = rs.createResource(URI.createURI("dummy2"))
    val model2 = createModel => [
      name = "MyOtherModel"
    ]
    r2.contents += model2

    printUMLModel(model)
    printUMLModel(model2)

    // do not obfuscate second resource
    val obfuscator = UMLObfuscatorBuilder.create.setInput(rs).setFilter[
      it == r2
    ].build
    println("Seed: " + obfuscator.stringObfuscator.seed)
    obfuscator.obfuscate

    printUMLModel(model)
    printUMLModel(model2)

    assertFalse("Obfuscator did not change model name", model.name == TESTMODEL_NAME)
    assertTrue("Obfuscator changed model2 name", model2.name == "MyOtherModel")

  }

  def printUMLModel(Model model) {
    println(
      '''
        Model:
          Name: «model.name»
          Classes:
            «FOR ne : model.ownedMembers»
              Name: «ne.name»«if(ne.name == null) "[null]"»
            «ENDFOR»
      ''')
  }
}