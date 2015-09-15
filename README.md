# VIATRA Model Obfuscator

*Obfuscate: To deliberately make more confusing in order to conceal the truth* (http://en.wiktionary.org/wiki/obfuscate)

The VIATRA Model Obfuscator is a small utility for obfuscating structured graph-like models (e.g. XML documents, EMF models) by altering all data values (such as names, identifiers or other strings) in a way that the structure of the model remains the same. Two data values that were identical before the obfuscation will also be identical after it, but the obfuscated value computed based on an input obfuscation string will be completely different (e.g. "Info1" may become "K18DWVQ=").

For more details, read this [blog post](http://modeling-languages.com/model-obfuscator/).

## Overview

The current version of the model obfuscator (0.7.0) only obfuscates text content in XML files and String values in EMF models. The obfuscator API receives an obfuscation string (seed) and the input model, it traverses the model and replaces all data values with an obfuscated value that is deterministically computed based on the data value and the obfuscation string.

There are several different ways for using the obfuscator:

* Eclipse UI contribution
* Command line application
* Maven plugin for integration through API usage
* OSGi bundle for integration through API usage

## Eclipse UI contribution

The EMF model obfuscator can be installed from the [update site](http://download.eclipse.org/viatra2/modelobfuscator/updates/integration) and the user can perform the obfuscation on any EMF model loaded into the generic or reflective EMF editors. The obfuscator does not save the models and it is possible to undo the changes. In addition, the error log will contain an entry that provides the random obfuscation string used. Resources that are not writable are not modified.

## Command line application

Both XML and EMF model obfuscators can be used with a command line application, that can be downloaded from [here](https://hudson.eclipse.org/viatra/job/viatra-modelobfuscator-master/lastSuccessfulBuild/artifact/releng/org.eclipse.viatra.modelobfuscator.product/target/products/). The application uses a few command line arguments and a properties file for configuration, examples for both XML and EMF are provided [here](http://git.eclipse.org/c/viatra2/org.eclipse.viatra.modelobfuscator.git/tree/plugins/org.eclipse.viatra.modelobfuscator.application). Calling the application without arguments displays the usage guide.

## Maven plugin

The model obfuscator is available as a [Maven plugin](http://git.eclipse.org/c/viatra2/org.eclipse.viatra.modelobfuscator.git/tree/maven/viatra-modelobfuscator-runtime) for projects that are POM first. The plugin simply wraps the OSGi runtime bundle into a Maven plugin and is available from repo.eclipse.org.

## OSGi bundle

The core runtime of the model obfuscator is a simple OSGi bundle that can be added as a dependency of plugin projects. The API of the runtime is documented in JavaDoc and there are a set of [JUnit tests](http://git.eclipse.org/c/viatra2/org.eclipse.viatra.modelobfuscator.git/tree/plugins/org.eclipse.viatra.modelobfuscator.tests) that show the usage of the API for both XML and EMF.

## Contributing to VIATRA

Please read the [Contributing](http://wiki.eclipse.org/VIATRA/Contributing) wiki page to understand our contribution process.

## License

All code in this repository is available under the Eclipse Public License v1.0: [http://www.eclipse.org/legal/epl-v10.html](http://www.eclipse.org/legal/epl-v10.html)
