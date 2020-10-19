# OWLAPI-Jena Integration

This repository contains an exemplary application integrating OWLAPI and Jena libraries. In particular the application:
1. loads an ontology specified in the configuration file (available at ``src/main/resources/config.properties``);
2. loads the ontology imported using ``owl:imports`` assertions;
3. infers all implicit assertions using HermiT reasoner;
4. transforms the inferred ontology into a Jena model;
5. evaluates a query (i.e. ``SELECT ?i ?c {?i a ?c}``) on the Jena model and prints the results.

### Installation and usage

You can install (i.e. ``mvn clean install``) and run (i.e. ``mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="it.cnr.istc.stlab.App" -Dexec.args="file:///path/to/the/input/ontology.owl"`` ) the application using maven.

### License

The code within this repository is distributed under [Apache 2.0 License](LICENSE)
