package it.cnr.istc.stlab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.owlcs.ontapi.OntManagers;
import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.OntologyManager;

public class App {

	private static Logger logger = LoggerFactory.getLogger(App.class);

	private static boolean checkConsistency(String ontologyIRI)
			throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntology(IRI.create(ontologyIRI));
		OntologyManager ontManager = OntManagers.createONT();

		ontology.importsClosure().forEach(ont -> {
			logger.trace("Importing " + ont.getOntologyID().getOntologyIRI().toString());
			ontology.addAxioms(ont.axioms());
		});

		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration reasonerConfiguration = new SimpleConfiguration(progressMonitor);
		OWLReasoner reasoner = new org.semanticweb.HermiT.ReasonerFactory().createReasoner(ontology,
				reasonerConfiguration);

		OWLDataFactory factory = manager.getOWLDataFactory();
		InferredOntologyGenerator gen = new InferredOntologyGenerator(reasoner);
		OWLOntology newOntology = manager.createOntology();
		gen.fillOntology(factory, newOntology);

		Ontology ontOntology = ontManager.copyOntology(newOntology, OntologyCopy.DEEP);
		// Print all triples from the inner graph:
//		ontOntology.asGraphModel().getGraph().find(Triple.ANY).forEachRemaining(System.out::println);

		Model model = ((com.github.owlcs.ontapi.Ontology) ontOntology).asGraphModel();

		String query = "SELECT ?i ?c {?i a ?c}";
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		System.out.println(ResultSetFormatter.asText(qexec.execSelect()));

		manager.saveOntology(newOntology, new FileOutputStream(new File("testOut.owl")));

		return reasoner.isConsistent();
	}

	public static void main(String[] args)
			throws OWLOntologyStorageException, FileNotFoundException, OWLOntologyCreationException {
		String ontologyIRI = args[0];

		logger.info("" + checkConsistency(ontologyIRI));

	}
}
