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
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.viatra.modelobfuscator.xml.XMLModelObfuscator;
import org.eclipse.viatra.modelobfuscator.xml.XMLModelObfuscatorBuilder;
import org.eclipse.viatra.modelobfuscator.xml.XMLSchemaConfiguration;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
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
            // TODO handle EMF obfuscation
            
            // check input file existence

            // ensure output directory existence
            
            // load ecore models into registry
            
            // load inputs into resource set
            
            // initialize obfuscator
            
            // perform obfuscation
            
            // save models to output directory
            
        } else if(mode.equals(XML_OBFUSCATION_MODE)) {

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
                obfuscatorBuilder.setSeed(seed);
            }
            System.out.println("Obfuscating using seed: " + obfuscatorBuilder.getSeed());
            
            if(salt != null) {
                obfuscatorBuilder.setSalt(salt);
                System.out.println("Obfuscating using salt: " + obfuscatorBuilder.getSalt());
            }
            
            performObfuscation(outputDirectory, inputs, obfuscatorBuilder);
            
        } else {
            reportError("Unknown mode " + mode + " selected in configuration");
        }
        
        return IApplication.EXIT_OK;
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
                obfuscator.obfuscate();
                bufferedOutputStream.close();
                bufferedInputStream.close();
            } catch (FileNotFoundException e) {
                reportError("Could not ouput to file " + output.getPath());
            } catch (IOException e) {
                reportError("Could not close output file " + output.getPath());
            }
        }
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
        String input = getPropertyValue(configuration, OBFUSCATION_INPUT_PROPERTY);
        StringTokenizer inputTokenizer = new StringTokenizer(input,";");
        while (inputTokenizer.hasMoreTokens()) {
            String inputPath = (String) inputTokenizer.nextToken();
            // create input stream for input files
            File file = new File(inputPath);
            createFileInputStreams(inputs, file);
        }
    }

    /**
     * @param inputs
     * @param inputPath
     */
    private void createFileInputStreams(Map<String, FileInputStream> inputs, File file) {
        if(!file.exists()) {
            reportError("Input " + file.getPath() + " specified in configuration could not be found");
        }
        if(file.isFile()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                String fileName = file.getName();
                if(inputs.containsKey(fileName)) {
                    reportError("Multiple files with the same name " + fileName + " in configuration");
                }
                inputs.put(fileName,fileInputStream);
            } catch (FileNotFoundException e) {
                reportError("Input " +  file.getPath() + " specified in configuration could not be found, although it exists");
            }
        } else if(file.isDirectory()) {
            /*
             * TODO we could support directories, either processing only directly contained files or all transitive
             * files. However, we would need to take care in putting the directory structure into the output, handle
             * symbolic and hard links, etc.
             */
            reportError("Input" + file.getPath() + " specified in configuration is a directory");
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
                + "  -s       : Optional, the seed used for the obfuscation.\n"
                + "  --seed   : Same as -s.\n"
                + "  --salt   : Optional, the salt used for the obfuscation.");
    }
    
}
