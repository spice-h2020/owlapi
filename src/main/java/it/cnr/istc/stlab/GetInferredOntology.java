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
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
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

		logger.trace("Creating OWL ontology manager");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		logger.trace("Loading ontology");
		OWLOntology ontology = manager.loadOntology(IRI.create(ontologyIRI));
		logger.trace("Ontology loaded -> number of axioms {}", ontology.axioms().count());
		logger.trace("Creating ont manager");
		OntologyManager ontManager = OntManagers.createManager();

//		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
//		OWLReasonerConfiguration reasonerConfiguration = new SimpleConfiguration(progressMonitor);

		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
//		OWLReasonerConfiguration c = new SimpleConfiguration(progressMonitor);
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
//		reasoner.precomputeInferences(InferenceType.DIFFERENT_INDIVIDUALS);
//		reasoner.precomputeInferences(InferenceType.DISJOINT_CLASSES);
//		reasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_ASSERTIONS);
//		reasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_HIERARCHY);
//		reasoner.precomputeInferences(InferenceType.SAME_INDIVIDUAL);
		// To generate an inferred ontology we use implementations of inferred
		// axiom generators to generate the parts of the ontology we want (e.g.
		// subclass axioms, equivalent classes axioms, class assertion axiom
		// etc. - see the org.semanticweb.owlapi.util package for more
		// implementations). Set up our list of inferred axiom generators
//		List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
//		gens.add(new InferredSubClassAxiomGenerator());
		// Put the inferred axioms into a fresh empty ontology - note that there
		// is nothing stopping us stuffing them back into the original asserted
		// ontology if we wanted to do this.
//		OWLOntology infOnt = man.createOntology();
		// Now get the inferred ontology generator to generate some inferred
		// axioms for us (into our fresh ontology). We specify the reasoner that
		// we want to use and the inferred axiom generators that we want to use.
//		InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, gens);
//		iog.fillOntology(man, infOnt);
		// Save the inferred ontology. (Replace the URI with one that is
		// appropriate for your setup)
//		man.saveOntology(infOnt, new StringDocumentTarget());

		OWLDataFactory factory = manager.getOWLDataFactory();
		logger.trace("OWLData factory created");
		InferredOntologyGenerator gen = new InferredOntologyGenerator(reasoner);
		logger.trace("Inferred ontology generator created");
		OWLOntology newOntology = manager.createOntology();
		logger.trace("Filling new ontology");
		gen.fillOntology(factory, newOntology);
		logger.trace("New ontology filled -> number of axioms {}", newOntology.axioms().count());

		logger.trace("Copying to ontapi ontology");
		Ontology ontOntology = ontManager.copyOntology(newOntology, OntologyCopy.DEEP);
		ontology.axioms().forEach(a -> {
			ontOntology.addAxiom(a);
		});
		logger.trace("Ontapi ontology copied {}", ontOntology.axioms().count());

		Model model = ((com.github.owlcs.ontapi.Ontology) ontOntology).asGraphModel();
		logger.trace("Copied to Jena model, number of triples {}", model.size());
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
