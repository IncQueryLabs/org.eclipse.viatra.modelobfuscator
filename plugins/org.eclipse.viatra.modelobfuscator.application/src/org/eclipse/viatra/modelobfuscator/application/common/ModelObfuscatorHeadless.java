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
package org.eclipse.viatra.modelobfuscator.application.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.viatra.modelobfuscator.emf.simple.AbstractModelObfuscator;
import org.eclipse.viatra.modelobfuscator.emf.simple.EMFModelObfuscatorBuilder;
import org.eclipse.viatra.modelobfuscator.xml.XMLModelObfuscator;
import org.eclipse.viatra.modelobfuscator.xml.XMLModelObfuscatorBuilder;
import org.eclipse.viatra.modelobfuscator.xml.XMLSchemaConfiguration;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * @author Abel Hegedus
 *
 */
public class ModelObfuscatorHeadless {

    /*
     * Command line parameters 
     */
    private static String configParam = "-c";
    private static String configLongParam = "--config";
    private static String seedParam   = "-s";
    private static String saltParam   = "--salt";

    /*
     * Property keys
     */
    private static final String PROPERTY_PREFIX = "org.eclipse.viatra.modelobfuscator/";
    private static final String OBFUSCATION_MODE_PROPERTY = PROPERTY_PREFIX + "mode";
    private static final String XML_OBFUSCATION_MODE = "xml";
    private static final String EMF_OBFUSCATION_MODE= "emf";
    private static final String OBFUSCATION_INPUT_PROPERTY = PROPERTY_PREFIX + "input";
    private static final String OBFUSCATION_OUTPUT_PROPERTY = PROPERTY_PREFIX + "output";
    private static final String OBFUSCATION_ECORE_PROPERTY = PROPERTY_PREFIX + "ecore";
    private static final String OBFUSCATION_TAGS_PROPERTY = PROPERTY_PREFIX + "tags";
    private static final String OBFUSCATION_ATTRIBUTES_PROPERTY = PROPERTY_PREFIX + "attributes";

    /**
     * @param args
     * @return
     */
    public Integer performHeadlessObfuscation(String[] args) {
        String config = null;
        String seed = null;
        String salt = null;
        
        if (args == null || args.length == 0) {
            reportError("Configuration parameter not set");
        }
        int i = 0;
        
        while (i < args.length) {
           if (args[i].equals(configLongParam)) {
               config = args[i + 1];
                i += 2;
            } else if (args[i].equals(configParam)) {
                config = args[i + 1];
                i += 2;
            } else if (args[i].equals(seedParam)) {
                seed = args[i + 1];
                i += 2;
            } else if (args[i].equals(saltParam)) {
                salt = args[i + 1];
                i += 2;
            } else {
                i++;
            }
        }

        System.out.println("Obfuscation called with:\n"
                + "  Config : " + Strings.nullToEmpty(config) + "\n"
                + "  Seed   : " + Strings.nullToEmpty(seed) + "\n"
                + "  Salt   : " + Strings.nullToEmpty(salt));
        
        if (config == null) {
            reportError("Configuration parameter not set");
        }

        Properties configuration = null;
        // load configuration file
        try {
            configuration = loadConfigurationPropertyFile(config);
        } catch (FileNotFoundException e) {
            reportError("Could not find configuration file");
        } catch (IOException e) {
            reportError("Could not read configuration file");
        }

        String mode = getPropertyValue(configuration, OBFUSCATION_MODE_PROPERTY);
        
        if(mode.equals(EMF_OBFUSCATION_MODE)) {
            
            performEMFObfuscation(seed, salt, configuration);
            
        } else if(mode.equals(XML_OBFUSCATION_MODE)) {

            performXMLObfuscation(seed, salt, configuration);
            
        } else {
            reportError("Unknown mode " + mode + " selected in configuration");
        }
        
        return IApplication.EXIT_OK;
    }

    /**
     * @param seed
     * @param salt
     * @param configuration
     */
    private void performEMFObfuscation(String seed, String salt, Properties configuration) {
        // ensure output directory existence
        File outputDirectory = checkOutputDirectory(configuration);
        
        // check input file existence
        Map<String, URI> inputs = processInput(configuration);
        
        Map<String, Object> extensionToFactoryMap = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
        ResourceSetImpl ecoreRS = performEMFSetup(extensionToFactoryMap);
        
        // load ecore models into registry
        loadEcorePackagesIntoRegistry(configuration, EPackage.Registry.INSTANCE, ecoreRS);
        
        // load inputs into resource set
        ResourceSetImpl resourceSet = loadInputModels(inputs, extensionToFactoryMap);
        
        // initialize obfuscator
        EMFModelObfuscatorBuilder obfuscatorBuilder = EMFModelObfuscatorBuilder.create();
        obfuscatorBuilder.setInput(resourceSet);
        
        if(seed != null) {
            obfuscatorBuilder.setSeed(new BigInteger(seed, 36));
        }
        System.out.println("Obfuscating using seed: " + obfuscatorBuilder.getSeed());
        
        if(salt != null) {
            obfuscatorBuilder.setSalt(salt);
            System.out.println("Obfuscating using salt: " + obfuscatorBuilder.getSalt());
        }
        
        // perform obfuscation
        performObfuscation(obfuscatorBuilder);
        
        // save models to output directory
        saveObfuscatedModels(outputDirectory, inputs, resourceSet);
    }

    /**
     * @param extensionToFactoryMap
     * @return
     */
    private ResourceSetImpl performEMFSetup(Map<String, Object> extensionToFactoryMap) {
        if(!extensionToFactoryMap.containsKey("ecore")) {
            extensionToFactoryMap.put("ecore", new EcoreResourceFactoryImpl());
        }
        ResourceSetImpl ecoreRS = new ResourceSetImpl();
        final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(EPackage.Registry.INSTANCE);
        ecoreRS.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, extendedMetaData);
        return ecoreRS;
    }

    /**
     * @param configuration
     * @return
     */
    private Map<String, URI> processInput(Properties configuration) {
        Map<String,URI> inputs = Maps.newHashMap();
        List<String> resultList = processFileListProperty(configuration, OBFUSCATION_INPUT_PROPERTY);
        for (String filePath : resultList) {
            URI fileURI = URI.createFileURI(filePath);
            inputs.put(filePath,fileURI);
        }
        return inputs;
    }

    /**
     * @param inputs
     * @param extensionToFactoryMap
     * @return
     */
    private ResourceSetImpl loadInputModels(Map<String, URI> inputs, Map<String, Object> extensionToFactoryMap) {
        ResourceSetImpl resourceSet = new ResourceSetImpl();
        for (Entry<String, URI> inputEntry : inputs.entrySet()) {
            URI uri = inputEntry.getValue();
            // XXX we only support XMI resources in this way
            if(!extensionToFactoryMap.containsKey(uri.fileExtension())) {
                extensionToFactoryMap.put(uri.fileExtension(), new EcoreResourceFactoryImpl());
            }
            System.out.println("Loading resource: " + inputEntry.getKey());
            Stopwatch stopwatch = Stopwatch.createStarted();
            resourceSet.getResource(uri, true);
            stopwatch.stop();
            String elapsedTime = stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms (" + stopwatch.elapsed(TimeUnit.NANOSECONDS) + " ns)";
            System.out.println("Loaded resource: " + inputEntry.getKey() + " in " + elapsedTime);
        }
        return resourceSet;
    }

    /**
     * @param configuration
     * @param ePackageRegistryInstance
     * @param ecoreRS
     */
    private void loadEcorePackagesIntoRegistry(Properties configuration,
            org.eclipse.emf.ecore.EPackage.Registry ePackageRegistryInstance, ResourceSetImpl ecoreRS) {
        String ecore = getPropertyValue(configuration, OBFUSCATION_ECORE_PROPERTY);
        StringTokenizer ecoreTokenizer = new StringTokenizer(ecore,";");
        while (ecoreTokenizer.hasMoreTokens()) {
            String ecorePath = (String) ecoreTokenizer.nextToken();
            // create input stream for input files
            Resource ecoreResource = ecoreRS.getResource(URI.createFileURI(ecorePath), true);
            EObject root = ecoreResource.getContents().get(0);
            if(root instanceof EPackage) {
                EPackage ePackage = (EPackage) root;
                ePackageRegistryInstance.put(ePackage.getNsURI(), ePackage);
                System.out.println("Registered metamodel: " + ePackage.getName() + "(nsURI: " + ePackage.getNsURI() + ")");
            }
        }
    }

    /**
     * @param obfuscatorBuilder
     */
    private void performObfuscation(EMFModelObfuscatorBuilder obfuscatorBuilder) {
        AbstractModelObfuscator obfuscator = obfuscatorBuilder.build();
        System.out.println("Obfuscating EMF resource set");
        Stopwatch stopwatch = Stopwatch.createStarted();
        obfuscator.obfuscate();
        stopwatch.stop();
        String elapsedTime = stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms (" + stopwatch.elapsed(TimeUnit.NANOSECONDS) + " ns)";
        System.out.println("Obfuscation finished in: " + elapsedTime);
    }

    /**
     * @param outputDirectory
     * @param inputs
     * @param resourceSet
     */
    private void saveObfuscatedModels(File outputDirectory, Map<String, URI> inputs, ResourceSetImpl resourceSet) {
        URI outputDirUri = URI.createFileURI(outputDirectory.getPath());
        for (Entry<String, URI> entry : inputs.entrySet()) {
            URI uri = entry.getValue();
            String fileSegment = uri.lastSegment();
            URI outputUri = outputDirUri.appendSegment(fileSegment);
            Resource resource = resourceSet.getResource(uri, false);
            resource.setURI(outputUri);
            try {
                System.out.println("Saving resource: " + fileSegment);
                Stopwatch stopwatch2 = Stopwatch.createStarted();
                resource.save(null);
                stopwatch2.stop();
                String elapsedTime2 = stopwatch2.elapsed(TimeUnit.MILLISECONDS) + " ms (" + stopwatch2.elapsed(TimeUnit.NANOSECONDS) + " ns)";
                System.out.println("Saved resource: " + fileSegment + " in " + elapsedTime2);
            } catch (IOException e) {
                reportError("Could not save output " + fileSegment);
            }
        }
    }

    private List<String> processFileListProperty(Properties configuration, String obfuscationInputProperty) {
        List<String> resultList = Lists.newArrayList();
        String input = getPropertyValue(configuration, obfuscationInputProperty);
        StringTokenizer inputTokenizer = new StringTokenizer(input,";");
        while (inputTokenizer.hasMoreTokens()) {
            String inputPath = (String) inputTokenizer.nextToken();
            // create input stream for input files
            File file = new File(inputPath);
            if(!file.exists()) {
                reportError("Input " + file.getPath() + " specified in configuration could not be found");
            }
            if(file.isFile()) {
                String fileName = file.getName();
                if(resultList.contains(fileName)) {
                    reportError("Multiple files with the same name " + fileName + " in configuration");
                }
                resultList.add(inputPath);
            } else if(file.isDirectory()) {
                /*
                 * TODO we could support directories, either processing only directly contained files or all transitive
                 * files. However, we would need to take care in putting the directory structure into the output, handle
                 * symbolic and hard links, etc.
                 */
                reportError("Input" + file.getPath() + " specified in configuration is a directory");
            }
        }
        return resultList;
    }

    /**
     * @param seed
     * @param salt
     * @param configuration
     */
    private void performXMLObfuscation(String seed, String salt, Properties configuration) {
        // ensure output directory existence
        File outputDirectory = checkOutputDirectory(configuration);
        
        // check input file existence
        Map<String,FileInputStream> inputs = Maps.newHashMap();
        prepareInputStreams(configuration, inputs);
        
        // parse schema configuration
        XMLSchemaConfiguration schemaConfiguration = prepareSchemaConfiguration(configuration);
        
        // initialize obfuscator
        XMLModelObfuscatorBuilder obfuscatorBuilder = XMLModelObfuscatorBuilder.create().setSchemaConfiguration(schemaConfiguration);
        
        if(seed != null) {
            obfuscatorBuilder.setSeed(new BigInteger(seed, 36));
        }
        System.out.println("Obfuscating using seed: " + obfuscatorBuilder.getSeed());
        
        if(salt != null) {
            obfuscatorBuilder.setSalt(salt);
            System.out.println("Obfuscating using salt: " + obfuscatorBuilder.getSalt());
        }
        
        performObfuscation(outputDirectory, inputs, obfuscatorBuilder);
    }

    private Properties loadConfigurationPropertyFile(String configPath) throws FileNotFoundException, IOException {
        Properties bundle = new Properties();
        FileInputStream fis = new FileInputStream(configPath);
        try {
            bundle.load(fis);
        } finally {
            fis.close();
        }
        return bundle;
    }

    /**
     * @param configuration
     */
    private File checkOutputDirectory(Properties configuration) {
        String output = getPropertyValue(configuration, OBFUSCATION_OUTPUT_PROPERTY);
        File outputFile = new File(output);
        if(!outputFile.exists()) {
            boolean directoryCreated = outputFile.mkdir();
            if(!directoryCreated) {
                reportError("Output " + output + " specified in configuration could not be created");
            }
        } else if(!outputFile.isDirectory()) {
            reportError("Output " + output + " specified in configuration is not a directory");
        }
        return outputFile;
    }

    private void prepareInputStreams(Properties configuration, Map<String, FileInputStream> inputs) {
        List<String> resultList = processFileListProperty(configuration, OBFUSCATION_INPUT_PROPERTY);
        // create input stream for input files
        for (String fileName : resultList) {
            File file = new File(fileName);
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                inputs.put(file.getName(),fileInputStream);
            } catch (FileNotFoundException e) {
                reportError("Input " +  file.getPath() + " specified in configuration could not be found, although it exists");
            }
        }
    }

    /**
     * @param configuration
     * @return
     */
    private XMLSchemaConfiguration prepareSchemaConfiguration(Properties configuration) {
        Optional<String> tags = getOptionalPropertyValue(configuration, OBFUSCATION_TAGS_PROPERTY);
        Optional<String> attributes = getOptionalPropertyValue(configuration, OBFUSCATION_ATTRIBUTES_PROPERTY);
        if(!tags.isPresent() && !attributes.isPresent()) {
            reportError("No schema configuration provided, nothing to obfuscate");
        }
        
        Set<String> tagSet = Sets.newHashSet();
        if(tags.isPresent()) {
            StringTokenizer tagTokenizer = new StringTokenizer(tags.get(), ";");
            while (tagTokenizer.hasMoreTokens()) {
                String tag = tagTokenizer.nextToken();
                tagSet.add(tag);
            }
        }
        
        Multimap<String, String> attributeMultimap = HashMultimap.create();
        if(attributes.isPresent()) {
            StringTokenizer attributeTokenizer = new StringTokenizer(attributes.get(), ";");
            while (attributeTokenizer.hasMoreTokens()) {
                String tagAttributes = attributeTokenizer.nextToken();
                try {
                    StringTokenizer tagAttributeTokenizer = new StringTokenizer(tagAttributes, ":");
                    String tagName = tagAttributeTokenizer.nextToken();
                    String attributeList = tagAttributeTokenizer.nextToken();
                    StringTokenizer attributeListTokenizer = new StringTokenizer(attributeList, ",");
                    while (attributeListTokenizer.hasMoreTokens()) {
                        String attributeName = attributeListTokenizer.nextToken();
                        attributeMultimap.put(tagName, attributeName);
                    }
                } catch (NoSuchElementException e) {
                    reportError("Incorrect syntax in attributes value of schema configuration when processing: " + tagAttributes);
                }
            }
        }
        
        XMLSchemaConfiguration schemaConfiguration = new XMLSchemaConfiguration(attributeMultimap, tagSet);
        return schemaConfiguration;
    }

    /**
     * @param outputDirectory
     * @param inputs
     * @param obfuscatorBuilder
     */
    private void performObfuscation(File outputDirectory, Map<String, FileInputStream> inputs,
            XMLModelObfuscatorBuilder obfuscatorBuilder) {
        for (Entry<String, FileInputStream> input : inputs.entrySet()) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(input.getValue());
            obfuscatorBuilder.setInput(bufferedInputStream);
            String fileName = input.getKey();
            File output = new File(outputDirectory, fileName);
            BufferedOutputStream bufferedOutputStream;
            try {
                bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(output));
                obfuscatorBuilder.setOutput(bufferedOutputStream);
                XMLModelObfuscator obfuscator = obfuscatorBuilder.build();
                System.out.println("Obfuscating " + fileName);
                Stopwatch stopwatch = Stopwatch.createStarted();
                obfuscator.obfuscate();
                stopwatch.stop();
                System.out.println("Obfuscation finished in: " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms (" + stopwatch.elapsed(TimeUnit.NANOSECONDS) + " ns)");
                bufferedOutputStream.close();
                bufferedInputStream.close();
            } catch (FileNotFoundException e) {
                reportError("Could not ouput to file " + output.getPath());
            } catch (IOException e) {
                reportError("Could not close output file " + output.getPath());
            }
        }
    }

    /**
     * @param configuration
     * @return
     */
    private String getPropertyValue(Properties configuration, String propertyName) {
        Optional<String> optionalPropertyValue = getOptionalPropertyValue(configuration, propertyName);
        if(!optionalPropertyValue.isPresent()) {
            reportError(propertyName + " undefined in configuration");
        }
        return optionalPropertyValue.get();
    }
    
    /**
     * @param configuration
     * @return
     */
    private Optional<String> getOptionalPropertyValue(Properties configuration, String propertyName) {
        String propertyValue = configuration.getProperty(propertyName);
        return Optional.fromNullable(propertyValue);
    }

    /**
     * @return
     */
    private Integer reportError(String message) {
        System.out.println(message);
        displayHelp();
        throw new IllegalArgumentException(message);
    }

    private void displayHelp() {
        System.out.println("Usage:\n"
                + "<call> -c <configurationFilePath> [-s <seed>] [-salt <salt>]\n"
                + "  -c       : Required, the configuration that describes what to obfuscate.\n"
                + "  --config : Same as -c.\n"
                + "  -s       : Optional, the seed used for the obfuscation. Must be base 36 number as string\n"
                + "  --seed   : Same as -s.\n"
                + "  --salt   : Optional, the salt used for the obfuscation.");
    }
    
}
