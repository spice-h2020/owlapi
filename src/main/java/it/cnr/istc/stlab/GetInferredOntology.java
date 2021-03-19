package it.cnr.istc.stlab;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetInferredOntology {

	private static Logger logger = LoggerFactory.getLogger(GetInferredOntology.class);

	private static void getInferredModel(String ontologyIRI, String filepath, Map<String, String> iriMap)
			throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {

		logger.trace("Creating OWL ontology manager");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		iriMap.forEach((k, v) -> {
			SimpleIRIMapper l0Mapper = new SimpleIRIMapper(IRI.create(k), IRI.create(new File(v)));
			manager.getIRIMappers().add(l0Mapper);
		});

		logger.trace("Loading ontology {}", ontologyIRI);
		OWLOntology ontology = manager.loadOntology(IRI.create(ontologyIRI));

		logger.trace("Ontology loaded, axioms {} ", ontology.axioms().count());
		ontology.importsClosure().forEach(ont -> {
			logger.trace("Importing " + ont.getOntologyID().getOntologyIRI().toString());
			ChangeApplied ca = ontology.addAxioms(ont.axioms());
			logger.trace("Changed applied {}", ca.toString());
			logger.trace("Axioms " + ont.axioms().count());
		});

		logger.trace("Axioms {} " , ontology.axioms().count());
		
		ontology.saveOntology(new TurtleDocumentFormat(), IRI.create("file:" + filepath+"_1"));

		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		Configuration c = new Configuration();
		c.reasonerProgressMonitor = progressMonitor;
		c.ignoreUnsupportedDatatypes = true;

		logger.trace("Creating owl reasoner");
		OWLReasoner reasoner = new org.semanticweb.HermiT.ReasonerFactory().createReasoner(ontology, c);
		logger.trace("Reasoner created");

//		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
//		reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
//		reasoner.precomputeInferences(InferenceType.DATA_PROPERTY_ASSERTIONS);
//		reasoner.precomputeInferences(InferenceType.DATA_PROPERTY_HIERARCHY);
////		reasoner.precomputeInferences(InferenceType.DIFFERENT_INDIVIDUALS);
//		reasoner.precomputeInferences(InferenceType.DISJOINT_CLASSES);
//		reasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_ASSERTIONS);
//		reasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_HIERARCHY);
////		reasoner.precomputeInferences(InferenceType.SAME_INDIVIDUAL);
//
		logger.info("Is the ontology consistent? {}", reasoner.isConsistent());
//
//		// To generate an inferred ontology we use implementations of inferred
//		// axiom generators to generate the parts of the ontology we want (e.g.
//		// subclass axioms, equivalent classes axioms, class assertion axiom
//		// etc. - see the org.semanticweb.owlapi.util package for more
//		// implementations). Set up our list of inferred axiom generators
//		List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
//		gens.add(new InferredClassAssertionAxiomGenerator());
//		gens.add(new InferredDataPropertyCharacteristicAxiomGenerator());
//		gens.add(new InferredDisjointClassesAxiomGenerator());
//		gens.add(new InferredEquivalentClassAxiomGenerator());
//		gens.add(new InferredEquivalentDataPropertiesAxiomGenerator());
//		gens.add(new InferredEquivalentObjectPropertyAxiomGenerator());
//		gens.add(new InferredInverseObjectPropertiesAxiomGenerator());
//		gens.add(new InferredObjectPropertyCharacteristicAxiomGenerator());
//		gens.add(new InferredPropertyAssertionGenerator());
//		gens.add(new InferredSubClassAxiomGenerator());
//		gens.add(new InferredSubDataPropertyAxiomGenerator());
//		gens.add(new InferredSubObjectPropertyAxiomGenerator());

		// Put the inferred axioms into a fresh empty ontology - note that there
		// is nothing stopping us stuffing them back into the original asserted
		// ontology if we wanted to do this.
		OWLDataFactory factory = manager.getOWLDataFactory();
		logger.trace("OWLData factory created");

		// Now get the inferred ontology generator to generate some inferred
		// axioms for us (into our fresh ontology). We specify the reasoner that
		// we want to use and the inferred axiom generators that we want to use.
//		InferredOntologyGenerator gen = new InferredOntologyGenerator(reasoner, gens);
		InferredOntologyGenerator gen = new InferredOntologyGenerator(reasoner);
		logger.trace("Inferred ontology generator created");

		OWLOntology newOntology = manager.createOntology();
		logger.trace("Filling new ontology");
		gen.fillOntology(factory, newOntology);
		logger.trace("New ontology filled -> number of axioms {}", newOntology.axioms().count());

		logger.trace("Axioms " + ontology.axioms().count());
		ChangeApplied ca = ontology.addAxioms(newOntology.axioms());
		logger.trace("Changed applied {}", ca.toString());
		logger.trace("Axioms " + ontology.axioms().count());

		ontology.saveOntology(new TurtleDocumentFormat(), IRI.create("file:" + filepath));

	}

	public static void main(String[] args)
			throws OWLOntologyStorageException, FileNotFoundException, OWLOntologyCreationException {
		Map<String, String> iriMap = new HashMap<>();
//		iriMap.put("https://w3id.org/italia/onto/l0",
//				"/Users/lgu/workspace/others/daf-ontologie-vocabolari-controllati/Ontologie/l0/latest/l0-AP_IT.rdf");
//		iriMap.put("https://w3id.org/italia/onto/TI/0.6",
//				"/Users/lgu/workspace/others/daf-ontologie-vocabolari-controllati/Ontologie/TI/latest/TI-AP_IT.rdf");
//		iriMap.put("https://w3id.org/italia/onto/TI",
//				"/Users/lgu/workspace/others/daf-ontologie-vocabolari-controllati/Ontologie/TI/latest/TI-AP_IT.rdf");
//		iriMap.put("https://w3id.org/arco/ontology/cataloguing-campaign",
//				"/Users/lgu/workspace/arco/ArCo/ArCo-release/ontologie/cataloguing-campaign/cataloguing-campaign.owl");
//		iriMap.put("https://w3id.org/italia/onto/COV",
//				"/Users/lgu/workspace/others/daf-ontologie-vocabolari-controllati/Ontologie/COV/v0.11/COV-AP_IT.rdf");
//		iriMap.put("https://w3id.org/italia/onto/COV/0.11",
//				"/Users/lgu/workspace/others/daf-ontologie-vocabolari-controllati/Ontologie/COV/v0.11/COV-AP_IT.rdf");
//		iriMap.put("https://w3id.org/italia/onto/MU",
//				"/Users/lgu/workspace/others/daf-ontologie-vocabolari-controllati/Ontologie/MU/latest/MU-AP_IT.rdf");
//		iriMap.put("https://w3id.org/arco/ontology/arco/1.2",
//				"/Users/lgu/workspace/arco/ArCo/ArCo-release/ontologie/arco/1.2/arco.owl");
//		iriMap.put("https://w3id.org/arco/ontology/catalogue/1.2",
//				"/Users/lgu/workspace/arco/ArCo/ArCo-release/ontologie/catalogue/1.2/catalogue.owl");
//		iriMap.put("https://w3id.org/arco/ontology/cataloguing-campaign/0.1",
//				"/Users/lgu/workspace/arco/ArCo/ArCo-release/ontologie/cataloguing-campaign/cataloguing-campaign.owl");
//		iriMap.put("https://w3id.org/arco/ontology/cataloguing-campaign",
//				"/Users/lgu/workspace/arco/ArCo/ArCo-release/ontologie/cataloguing-campaign/cataloguing-campaign.owl");
		getInferredModel(args[0], args[1], iriMap);

	}
}
