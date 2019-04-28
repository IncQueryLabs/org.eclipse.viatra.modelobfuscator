# VIATRA Model Obfuscator

*Obfuscate: To deliberately make more confusing in order to conceal the truth* (http://en.wiktionary.org/wiki/obfuscate)

The VIATRA Model Obfuscator is a small utility for obfuscating structured graph-like models (e.g. XML documents, EMF models) by altering all data values (such as names, identifiers or other strings) in a way that the structure of the model remains the same. Two data values that were identical before the obfuscation will also be identical after it, but the obfuscated value computed based on an input obfuscation string will be completely different (e.g. "Info1" may become "K18DWVQ=").

For more details, read the [wiki page](https://wiki.eclipse.org/VIATRA/ModelObfuscator).

## Repository structure

* plugins: Eclipse plugins that contain the main source code and tests for the obfuscator
* features: Eclipse feature definitions
* maven: Maven modules to bundle the obfuscator as Maven artifacts
* releng: parent POM for Maven build, target platformn definition, update site 

## Contributing to VIATRA

Please read the [Contributing](http://wiki.eclipse.org/VIATRA/Contributing) wiki page to understand our contribution process.

## License

All code in this repository is available under the Eclipse Public License v2.0: [http://www.eclipse.org/legal/epl-v20.html](http://www.eclipse.org/legal/epl-v20.html)
