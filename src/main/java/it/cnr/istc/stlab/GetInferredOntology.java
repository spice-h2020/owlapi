package it.cnr.istc.stlab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.jena.rdf.model.Model;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.owlcs.ontapi.OntManagers;
import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.OntologyManager;

public class GetInferredOntology {

	private static Logger logger = LoggerFactory.getLogger(GetInferredOntology.class);

	private static void getInferredModel(String ontologyIRI, String filepath)
			throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {
		logger.info("Loading {}", ontologyIRI);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntology(IRI.create(ontologyIRI));
		OntologyManager ontManager = OntManagers.createManager();

//		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
//		OWLReasonerConfiguration reasonerConfiguration = new SimpleConfiguration(progressMonitor);

		Configuration c = new Configuration();
		c.ignoreUnsupportedDatatypes = true;

		OWLReasoner reasoner = new org.semanticweb.HermiT.ReasonerFactory().createReasoner(ontology, c);

		OWLDataFactory factory = manager.getOWLDataFactory();
		InferredOntologyGenerator gen = new InferredOntologyGenerator(reasoner);
		OWLOntology newOntology = manager.createOntology();
		gen.fillOntology(factory, newOntology);

		Ontology ontOntology = ontManager.copyOntology(newOntology, OntologyCopy.DEEP);
		ontology.axioms().forEach(a->{
			ontOntology.addAxiom(a);
		});

		Model model = ((com.github.owlcs.ontapi.Ontology) ontOntology).asGraphModel();
//		model.add(((com.github.owlcs.ontapi.Ontology) ontology).asGraphModel());

		logger.info("Saving inferred ontology in {} in Turtle format", filepath);
		model.write(new FileOutputStream(new File(filepath)), "TTL");
		logger.info("End");

	}

	public static void main(String[] args)
			throws OWLOntologyStorageException, FileNotFoundException, OWLOntologyCreationException {
		getInferredModel(args[0], args[1]);

	}
}
