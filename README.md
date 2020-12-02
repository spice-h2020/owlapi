# OWLAPI-Jena Integration

This repository contains an exemplary application integrating OWLAPI and Jena libraries. In particular the application:
1. loads an ontology passed as argument;
2. loads the ontology imported using ``owl:imports`` assertions;
3. infers all implicit assertions using HermiT reasoner;
4. transforms the inferred ontology into a Jena model;
5. evaluates a query (i.e. ``SELECT ?i ?c {?i a ?c}``) on the Jena model and prints the results.

### Installation and usage

You can install (i.e. ``mvn clean install``) and run (i.e. ``mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="it.cnr.istc.stlab.App" -Dexec.args="[ONTOLOGY IRI]"`` ) the application using maven.

You can also use this tool to derive the inferred version of any ontology using HermiT reasoner. To do so:

1. Compile the project ``mvn clean install``
2. Run the main in  GetInferredOntology class ``mvn exec:java -Dexec.cleanupDaemonThreads=false -Dexec.mainClass="it.cnr.istc.stlab.GetInferredOntology" -Dexec.args="[ONTOLOGY IRI] /path/to/the/output/ontology.ttl"`` (the inferred ontology will be written in Turtle format).

Note: [ONTOLOGY IRI] can be either local paths (e.g. file:///path/to/the/input/ontology.owl) or URLs (e.g.http://www.ontologydesignpatterns.org/ont/dul/DUL.owl)

### License

The code within this repository is distributed under [Apache 2.0 License](LICENSE)
