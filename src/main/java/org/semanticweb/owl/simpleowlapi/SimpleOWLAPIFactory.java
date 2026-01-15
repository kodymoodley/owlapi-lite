package org.semanticweb.owl.simpleowlapi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyDocumentAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.model.parameters.Imports;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

// import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

// import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;

import uk.ac.manchester.cs.jfact.JFactFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDeclarationAxiomImpl;

/**
OWLAPI-Lite is a light-weight wrapper for the OWLAPI enabling more concise OWL ontology development.

Copyright (C) <2020>  Kody Moodley

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

/**
 * Core class for OWLAPI-Lite which provides access to all methods for
 * constructing and editing OWL ontologies.
 * 
 * @author Kody Moodley
 * @author https://kodymoodley.github.io
 * @version 1.0.1
 */
public class SimpleOWLAPIFactory {
	/**
	 * reference to current instance of SimpleOWLAPIFactory
	 */
	private static SimpleOWLAPIFactory obj;
	/**
	 * instance of OWLOntologyManager
	 */
	private static OWLOntologyManager ontologyManager;
	/**
	 * flag to keep track of whether the parser should render OWL entities using
	 * their full IRI or just a short form human-readable label.
	 */
	private static boolean fullIRIRendering;
	/**
	 * reference to currently selected OWL reasoner: default is JFACT
	 */
	private SelectedReasoner selectedReasoner = SelectedReasoner.JFACT; // default reasoner is JFact
	/**
	 * reference to an OWLReasonerFactory object
	 */
	private OWLReasonerFactory reasonerFactory;
	/**
	 * reference to the currently selected OWLOntology
	 */
	private static OWLOntology selectedOntology;
	/**
	 * reference to the IRI of the currently selected OWLOntology
	 */
	private static IRI selectedOntologyIRI;
	/**
	 * OWLDataFactory instance
	 */
	private static OWLDataFactoryImpl dataFactory;
	/**
	 * Parser instance
	 */
	private static Parser parser = Parser.getInstance();
	/**
	 * SimpleOWLReasoner instance
	 */
	public SimpleOWLReasoner owlReasoner;
	/**
	 * ManchesterOWLSyntaxOWLObjectRendererImpl instance (allows rendering of OWL
	 * entities using short form human-readable label)
	 */
	private static ManchesterOWLSyntaxOWLObjectRendererImpl renderer = Parser.renderer;

	private static ReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * Private constructor for SimpleOWLAPIFactory
	 * 
	 * @param selectedReasoner a SelectedReasoner instance
	 */
	private SimpleOWLAPIFactory(SelectedReasoner selectedReasoner) {
		ontologyManager = new OWLOntologyManagerImpl(OWLManager.getOWLDataFactory(), lock);
		setOWLReasoner(selectedReasoner);
		fullIRIRendering = false;
		dataFactory = new OWLDataFactoryImpl();
	}

	/**
	 * Public constructor for SimpleOWLAPIFactory
	 */
	public SimpleOWLAPIFactory() {
		ontologyManager = OWLManager.createOWLOntologyManager();
		setOWLReasoner(selectedReasoner);
		fullIRIRendering = false;
		dataFactory = new OWLDataFactoryImpl();
	}

	/**
	 * Static method to get hold of a SimpleOWLAPIFactory instance
	 * 
	 * @return Returns an instance of SimpleOWLAPIFactory using default reasoner
	 *         JFACT
	 */
	public static synchronized SimpleOWLAPIFactory getInstance() {
		if (obj == null) {
			obj = getInstance(SelectedReasoner.JFACT);
		}
		return obj;
	}

	/**
	 * Static method to get hold of a SimpleOWLAPIFactory instance
	 * 
	 * @param selectedReasoner the reasoner to use with this SimpleOWLAPIFactory
	 *                         instance
	 * @return Returns an instance of SimpleOWLAPIFactory that uses the specified
	 *         reasoner
	 */
	public static synchronized SimpleOWLAPIFactory getInstance(SelectedReasoner selectedReasoner) {
		if (obj == null) {
			obj = new SimpleOWLAPIFactory(selectedReasoner);
		}
		return obj;
	}

	/**
	 * Set or initialise the OWLReasonerFactory implementation to use for this
	 * SimpleOWLAPIFactory instance, based on the reasoner selected
	 * 
	 * @param selectedReasoner the reasoner to use with this SimpleOWLAPIFactory
	 *                         instance
	 */
	public void setOWLReasoner(SelectedReasoner selectedReasoner) {
		// if (selectedReasoner == SelectedReasoner.HERMIT) {
		// this.selectedReasoner = SelectedReasoner.HERMIT;
		// this.reasonerFactory = new ReasonerFactory();
		// }
		if (selectedReasoner == SelectedReasoner.JFACT) {
			this.selectedReasoner = SelectedReasoner.JFACT;
			this.reasonerFactory = new JFactFactory();
		}
		// if (selectedReasoner == SelectedReasoner.PELLET) {
		// this.selectedReasoner = SelectedReasoner.PELLET;
		// this.reasonerFactory = PelletReasonerFactory.getInstance();
		// }
		if (selectedReasoner == SelectedReasoner.ELK) {
			this.selectedReasoner = SelectedReasoner.ELK;
			this.reasonerFactory = new ElkReasonerFactory();
		}

		if (selectedOntology != null)
			owlReasoner = new SimpleOWLReasoner(reasonerFactory, selectedOntology, parser, selectedReasoner);
	}

	/**
	 * Set the type of rendering for OWL entities whenever the client prints a
	 * class, property, axiom etc. Either the full IRI or the short form label
	 * 
	 * @param option true if the client wishes to print full IRIs for OWL entities,
	 *               false if the client wants to print only short form labels for
	 *               OWL entities
	 */
	public void setFullIRIRendering(boolean option) {
		fullIRIRendering = option;
	}

	/**
	 * Creates a new OWL ontology given a string representation of an IRI
	 * 
	 * @param iriStr a string representation of an IRI
	 * @return an OWLOntology object
	 * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException if the IRI
	 *                                                                   is invalid
	 */
	public OWLOntology createOntology(String iriStr) throws OWLOntologyCreationException {
		System.out.println();
		// Ontology IRI
		IRI ontologyIRI = IRI.create(iriStr);
		// Create a fresh ontology
		OWLOntology ontology = null;

		try {
			ontology = ontologyManager.createOntology(ontologyIRI);
			System.out.println("Created ontology: " + ontologyIRI);
		} catch (OWLOntologyAlreadyExistsException ooae) {
			System.out.println("OWLAPI-Lite ERROR: ontology <" + ontologyIRI + "> already exists in workspace!");
		} catch (OWLOntologyDocumentAlreadyExistsException oodaee) {
			System.out.println("OWLAPI-Lite ERROR: ontology <" + ontologyIRI + "> already exists in workspace!");
		}

		if (ontology != null) {
			selectedOntology = ontology;
			selectedOntologyIRI = ontologyIRI;
			owlReasoner = null;
			owlReasoner = new SimpleOWLReasoner(reasonerFactory, selectedOntology, parser, selectedReasoner);
		}

		return ontology;
	}

	/**
	 * Sets the currently selected (active) ontology to be the one denoted by the
	 * input IRI string
	 * 
	 * @param iriStr a string representation of an IRI for an ontology
	 */
	public void setOntology(String iriStr) {
		System.out.println();
		selectedOntology = ontologyManager.getOntology(IRI.create(iriStr));
		if (selectedOntology != null) {
			System.out.println(
					"Selected ontology is: " + selectedOntology.getOntologyID().getOntologyIRI().get().toString());
			selectedOntologyIRI = selectedOntology.getOntologyID().getDefaultDocumentIRI().get();
			owlReasoner = null;
			owlReasoner = new SimpleOWLReasoner(reasonerFactory, selectedOntology, parser, selectedReasoner);
		} else {
			System.out.println("OWLAPI-Lite ERROR: Ontology <" + iriStr + "> does not exist!");
		}
	}

	/**
	 * Sets the currently selected (active) ontology to be the one denoted by the
	 * input OWLOntology reference
	 * 
	 * @param ontology an OWLOntology object
	 */
	public void setOntology(OWLOntology ontology) {
		System.out.println();
		if (ontology != null) {
			selectedOntology = ontology;
			selectedOntologyIRI = selectedOntology.getOntologyID().getOntologyIRI().get();
			System.out.println(
					"Selected ontology is: " + selectedOntology.getOntologyID().getOntologyIRI().get().toString());
			owlReasoner = null;
			owlReasoner = new SimpleOWLReasoner(reasonerFactory, selectedOntology, parser, selectedReasoner);
		} else {
			System.out.println("OWLAPI-Lite ERROR: Invalid or non-existent ontology!");
		}
	}

	/**
	 * Prints the IRI string of the currently selected ontology
	 */
	public void getOntology() {
		System.out.println();
		if (selectedOntology != null) {
			System.out.println(
					"Selected ontology is: " + selectedOntology.getOntologyID().getOntologyIRI().get().toString());
		} else {
			if (ontologyManager.ontologies().collect(Collectors.toCollection(HashSet::new)).size() > 0) {
				Iterator<OWLOntology> ontIter = ontologyManager.ontologies().collect(Collectors.toCollection(HashSet::new)).iterator();
				while (ontIter.hasNext()) {
					OWLOntology currentOnt = ontIter.next();
					if (currentOnt.getOntologyID().getDefaultDocumentIRI().isPresent())
						selectedOntology = currentOnt;
				}
				System.out.println(
						"Selected ontology is: " + selectedOntology.getOntologyID().getOntologyIRI().get().toString());
			} else {
				System.out.println(
						"OWLAPI-Lite Error: There are no ontologies in this context! Use the createOntology(...) method to create one.");
			}
		}
	}

	/**
	 * Prints the IRI strings of each ontology that has been created so far using
	 * this instance of SimpleOWLAPIFactory
	 */
	public void getOntologies() {
		System.out.println();
		System.out.println("List of ontologies in workspace:");
		System.out.println("--------------------------------");
		if (ontologyManager.ontologies().collect(Collectors.toCollection(HashSet::new)).size() > 0) {
			int idx = 1;
			for (OWLOntology o : ontologyManager.ontologies().collect(Collectors.toCollection(HashSet::new))) {
				if (o.getOntologyID().getDefaultDocumentIRI().isPresent()) {
					System.out.println(idx + ". " + o.getOntologyID().getDefaultDocumentIRI().get());
					idx++;
				}
			}
		} else {
			System.out.println("OWLAPI-Lite Error: ontology list is empty! Please create an ontology first.");
		}
	}

	/**
	 * Creates a new class name, adds this class to the parser's vocabulary and the
	 * currently selected ontology, and prints out the class to the console
	 * 
	 * @param classname A string representation of a class name
	 */
	public void createClass(String classname) {
		if (selectedOntology == null) {
			System.out.println("OWLAPI-Lite ERROR: There is no ontology to add classes to! First create an ontology.");
		} else {
			OWLClass c = dataFactory.getOWLClass(IRI.create(selectedOntologyIRI.toString() + classname));
			parser.addVocab(c);
			OWLDeclarationAxiomImpl a = new OWLDeclarationAxiomImpl(c, new HashSet<OWLAnnotation>());
			OWLAxiom class_declaration = a.getAxiomWithoutAnnotations();
			ontologyManager.addAxiom(selectedOntology, class_declaration);
			if (fullIRIRendering)
				System.out.println("Class: " + c);
			else
				System.out.println("Class: " + renderer.render(c));
		}
	}

	/**
	 * Creates a new object property, adds this object property to the parser's
	 * vocabulary and the currently selected ontology, and prints out the object
	 * property to the console
	 * 
	 * @param opropname A string representation of an object property
	 */
	public void createObjectProperty(String opropname) {
		if (selectedOntology == null) {
			System.out
					.println("OWLAPI-Lite ERROR: There is no ontology to add properties to! First create an ontology.");
		} else {
			createObjectProperty(opropname, 0, 0, 0);
		}
	}

	/**
	 * Alias for createObjectProperty method. Creates a new object property, adds
	 * this object property to the parser's vocabulary and the currently selected
	 * ontology, and prints out the object property to the console
	 * 
	 * @param opropname A string representation of an object property
	 */
	public void createOProperty(String opropname) {
		if (selectedOntology == null) {
			System.out
					.println("OWLAPI-Lite ERROR: There is no ontology to add properties to! First create an ontology.");
		} else {
			createObjectProperty(opropname, 0, 0, 0);
		}
	}

	/**
	 * Creates a new object property, adds this object property to the parser's
	 * vocabulary and the currently selected ontology, and prints out the object
	 * property to the console
	 * 
	 * @param opropname A string representation of an object property
	 * @param trans     an integer value representing whether the object property
	 *                  should be made transitive or not. 1 for transitive and 0 for
	 *                  not making it transitive
	 * @param ref       an integer value representing whether the object property
	 *                  should be made reflexive or not. 1 for reflexive and 0 for
	 *                  not making it reflexive, 2 for making it irreflexive
	 * @param sym       an integer value representing whether the object property
	 *                  should be made symmetric or not. 1 for symmetric and 0 for
	 *                  not making it symmetric, 2 for making it asymmetric
	 */
	public void createObjectProperty(String opropname, int trans, int ref, int sym) {
		if (trans == 1) {
			OWLObjectProperty r = dataFactory
					.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + opropname));
			parser.addVocab(r);
			OWLTransitiveObjectPropertyAxiom t = dataFactory.getOWLTransitiveObjectPropertyAxiom(r,
					new HashSet<OWLAnnotation>());
			ontologyManager.addAxiom(selectedOntology, t);
			if (fullIRIRendering)
				System.out.println("ObjectProperty: " + r);
			else
				System.out.println("ObjectProperty: " + renderer.render(r));
		}
		if (ref == 1) {
			OWLObjectProperty r = dataFactory
					.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + opropname));
			parser.addVocab(r);
			OWLReflexiveObjectPropertyAxiom t = dataFactory.getOWLReflexiveObjectPropertyAxiom(r,
					new HashSet<OWLAnnotation>());
			ontologyManager.addAxiom(selectedOntology, t);
			if (fullIRIRendering)
				System.out.println("ObjectProperty: " + r);
			else
				System.out.println("ObjectProperty: " + renderer.render(r));
		}
		if (ref == 2) {
			OWLObjectProperty r = dataFactory
					.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + opropname));
			parser.addVocab(r);
			OWLIrreflexiveObjectPropertyAxiom t = dataFactory.getOWLIrreflexiveObjectPropertyAxiom(r,
					new HashSet<OWLAnnotation>());
			ontologyManager.addAxiom(selectedOntology, t);
			if (fullIRIRendering)
				System.out.println("ObjectProperty: " + r);
			else
				System.out.println("ObjectProperty: " + renderer.render(r));
		}
		if (sym == 1) {
			OWLObjectProperty r = dataFactory
					.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + opropname));
			parser.addVocab(r);
			OWLSymmetricObjectPropertyAxiom t = dataFactory.getOWLSymmetricObjectPropertyAxiom(r,
					new HashSet<OWLAnnotation>());
			ontologyManager.addAxiom(selectedOntology, t);
			if (fullIRIRendering)
				System.out.println("ObjectProperty: " + r);
			else
				System.out.println("ObjectProperty: " + renderer.render(r));
		}
		if (sym == 2) {
			OWLObjectProperty r = dataFactory
					.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + opropname));
			parser.addVocab(r);
			OWLAsymmetricObjectPropertyAxiom t = dataFactory.getOWLAsymmetricObjectPropertyAxiom(r,
					new HashSet<OWLAnnotation>());
			ontologyManager.addAxiom(selectedOntology, t);
			if (fullIRIRendering)
				System.out.println("ObjectProperty: " + r);
			else
				System.out.println("ObjectProperty: " + renderer.render(r));
		}
		if ((trans == 0) && (ref == 0) && (sym == 0)) {
			OWLObjectProperty r = dataFactory
					.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + opropname));
			parser.addVocab(r);
			OWLDeclarationAxiomImpl a = new OWLDeclarationAxiomImpl(r, new HashSet<OWLAnnotation>());
			OWLAxiom role_declaration = a.getAxiomWithoutAnnotations();
			ontologyManager.addAxiom(selectedOntology, role_declaration);
			if (fullIRIRendering)
				System.out.println("ObjectProperty: " + r);
			else
				System.out.println("ObjectProperty: " + renderer.render(r));
		}
	}

	/**
	 * Alias for createOProperty method
	 * Creates a new object property, adds this object property to the parser's
	 * vocabulary and the currently selected ontology, and prints out the object
	 * property to the console
	 * 
	 * @param opropname A string representation of an object property
	 * @param trans     an integer value representing whether the object property
	 *                  should be made transitive or not. 1 for transitive and 0 for
	 *                  not making it transitive
	 * @param ref       an integer value representing whether the object property
	 *                  should be made reflexive or not. 1 for reflexive and 0 for
	 *                  not making it reflexive, 2 for making it irreflexive
	 * @param sym       an integer value representing whether the object property
	 *                  should be made symmetric or not. 1 for symmetric and 0 for
	 *                  not making it symmetric, 2 for making it asymmetric
	 */
	public void createOProperty(String opropname, int trans, int ref, int sym) {
		if (trans == 1) {
			OWLObjectProperty r = dataFactory
					.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + opropname));
			parser.addVocab(r);
			OWLTransitiveObjectPropertyAxiom t = dataFactory.getOWLTransitiveObjectPropertyAxiom(r,
					new HashSet<OWLAnnotation>());
			ontologyManager.addAxiom(selectedOntology, t);
			if (fullIRIRendering)
				System.out.println("ObjectProperty: " + r);
			else
				System.out.println("ObjectProperty: " + renderer.render(r));
		}
		if (ref == 1) {
			OWLObjectProperty r = dataFactory
					.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + opropname));
			parser.addVocab(r);
			OWLReflexiveObjectPropertyAxiom t = dataFactory.getOWLReflexiveObjectPropertyAxiom(r,
					new HashSet<OWLAnnotation>());
			ontologyManager.addAxiom(selectedOntology, t);
			if (fullIRIRendering)
				System.out.println("ObjectProperty: " + r);
			else
				System.out.println("ObjectProperty: " + renderer.render(r));
		}
		if (ref == 2) {
			OWLObjectProperty r = dataFactory
					.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + opropname));
			parser.addVocab(r);
			OWLIrreflexiveObjectPropertyAxiom t = dataFactory.getOWLIrreflexiveObjectPropertyAxiom(r,
					new HashSet<OWLAnnotation>());
			ontologyManager.addAxiom(selectedOntology, t);
			if (fullIRIRendering)
				System.out.println("ObjectProperty: " + r);
			else
				System.out.println("ObjectProperty: " + renderer.render(r));
		}
		if (sym == 1) {
			OWLObjectProperty r = dataFactory
					.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + opropname));
			parser.addVocab(r);
			OWLSymmetricObjectPropertyAxiom t = dataFactory.getOWLSymmetricObjectPropertyAxiom(r,
					new HashSet<OWLAnnotation>());
			ontologyManager.addAxiom(selectedOntology, t);
			if (fullIRIRendering)
				System.out.println("ObjectProperty: " + r);
			else
				System.out.println("ObjectProperty: " + renderer.render(r));
		}
		if (sym == 2) {
			OWLObjectProperty r = dataFactory
					.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + opropname));
			parser.addVocab(r);
			OWLAsymmetricObjectPropertyAxiom t = dataFactory.getOWLAsymmetricObjectPropertyAxiom(r,
					new HashSet<OWLAnnotation>());
			ontologyManager.addAxiom(selectedOntology, t);
			if (fullIRIRendering)
				System.out.println("ObjectProperty: " + r);
			else
				System.out.println("ObjectProperty: " + renderer.render(r));
		}
		if ((trans == 0) && (ref == 0) && (sym == 0)) {
			OWLObjectProperty r = dataFactory
					.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + opropname));
			parser.addVocab(r);
			OWLDeclarationAxiomImpl a = new OWLDeclarationAxiomImpl(r, new HashSet<OWLAnnotation>());
			OWLAxiom role_declaration = a.getAxiomWithoutAnnotations();
			ontologyManager.addAxiom(selectedOntology, role_declaration);
			if (fullIRIRendering)
				System.out.println("ObjectProperty: " + r);
			else
				System.out.println("ObjectProperty: " + renderer.render(r));
		}
	}

	/**
	 * Creates a new data property, adds this data property to the parser's
	 * vocabulary and the currently selected ontology, and prints out the data
	 * property to the console
	 * 
	 * @param dpropname A string representation of a data property
	 */
	public void createDataProperty(String dpropname) {
		if (selectedOntology == null) {
			System.out
					.println("OWLAPI-Lite ERROR: There is no ontology to add properties to! First create an ontology.");
		} else {
			OWLDataProperty dprop = dataFactory
					.getOWLDataProperty(IRI.create(selectedOntologyIRI.toString() + dpropname));
			parser.addVocab(dprop);
			if (fullIRIRendering)
				System.out.println("DataProperty: " + dprop);
			else
				System.out.println("DataProperty: " + renderer.render(dprop));
		}
	}

	/**
	 * Alias for createDataProperty method. Creates a new data property, adds this
	 * data property to the parser's
	 * vocabulary and the currently selected ontology, and prints out the data
	 * property to the console
	 * 
	 * @param dpropname A string representation of a data property
	 */
	public void createDProperty(String dpropname) {
		if (selectedOntology == null) {
			System.out
					.println("OWLAPI-Lite ERROR: There is no ontology to add properties to! First create an ontology.");
		} else {
			OWLDataProperty dprop = dataFactory
					.getOWLDataProperty(IRI.create(selectedOntologyIRI.toString() + dpropname));
			parser.addVocab(dprop);
			if (fullIRIRendering)
				System.out.println("DataProperty: " + dprop);
			else
				System.out.println("DataProperty: " + renderer.render(dprop));
		}
	}

	/**
	 * Creates a new individual name, adds this individual name to the parser's
	 * vocabulary and the currently selected ontology, and prints out the individual
	 * name to the console
	 * 
	 * @param individualname A string representation of a individual name
	 */
	public void createIndividual(String individualname) {
		if (selectedOntology == null) {
			System.out.println(
					"OWLAPI-Lite ERROR: There is no ontology to add individuals to! First create an ontology.");
		} else {
			OWLNamedIndividual i = dataFactory
					.getOWLNamedIndividual(IRI.create(selectedOntologyIRI.toString() + individualname));
			parser.addVocab(i);
			OWLDeclarationAxiomImpl a = new OWLDeclarationAxiomImpl(i, new HashSet<OWLAnnotation>());
			OWLAxiom individual_declaration = a.getAxiomWithoutAnnotations();
			ontologyManager.addAxiom(selectedOntology, individual_declaration);
			if (fullIRIRendering)
				System.out.println("Individual: " + i);
			else
				System.out.println("Individual: " + renderer.render(i));
		}
	}

	/**
	 * Creates a new OWLAxiom, adds this axiom to the currently selected ontology,
	 * and prints out the axiom to the console
	 * 
	 * @param axiomStr A string representation of the axiom in Manchester OWL syntax
	 * @return An OWLAxiom object representing the axiom in the input string
	 */
	public OWLAxiom createAxiom(String axiomStr) {
		if (selectedOntology == null) {
			System.out.println("OWLAPI-Lite ERROR: There is no ontology to add axioms to! First create an ontology.");
			return null;
		} else {
			parser.setString(axiomStr);
			OWLAxiom axiom = null;
			try {
				axiom = parser.getParser().parseAxiom();
			} catch (OWLParserException ope) {
				ope.printStackTrace();
				String[] tokens = ope.getMessage().split(" ");

				int idx = 0;
				int badEntityIdx = 0;
				for (String token : tokens) {
					if (token.equals("Encountered"))
						badEntityIdx = idx + 1;
					idx++;
				}

				System.out.println("OWLAPI-Lite PARSER ERROR: the entity " + tokens[badEntityIdx]
						+ " in its current position in the expression is not recognized by the parser! Either you have not created this entity or it should not appear in this position within the expression.");
			}

			if (axiom != null) {
				ontologyManager.addAxiom(selectedOntology, axiom);
				if (fullIRIRendering)
					System.out.println("OWLAxiom: " + axiom);
				else
					System.out.println("OWLAxiom: " + renderer.render(axiom));
			}
			return axiom;
		}
	}

	/**
	 * Changes the namespace of current context. Future entities created after
	 * executing this method will use this namespace
	 * 
	 * @param iriStr A string representation of the desired namespace IRI to change
	 *               to
	 */
	public void setNamespace(String iriStr) {
		System.out.println();
		selectedOntologyIRI = IRI.create(iriStr);
		System.out.println("Changed the namespace for future entities to: " + selectedOntologyIRI);
	}

	/**
	 * Creates an anonymous class expression and prints out the class expression to
	 * the console
	 * 
	 * @param classExpressionStr A string representation of the class expression in
	 *                           Manchester OWL syntax
	 * @return An OWLClassExpression object representing the class in the input
	 *         string
	 */
	public OWLClassExpression createClassExpression(String classExpressionStr) {
		if (selectedOntology == null) {
			System.out.println("OWLAPI-Lite ERROR: There is no ontology to add classes to! First create an ontology.");
			return null;
		} else {
			parser.setString(classExpressionStr);
			OWLClassExpression clsEx = null;
			try {
				clsEx = parser.getParser().parseClassExpression();
			} catch (OWLParserException ope) {
				String[] tokens = ope.getMessage().split(" ");

				int idx = 0;
				int badEntityIdx = 0;
				for (String token : tokens) {
					if (token.equals("Encountered"))
						badEntityIdx = idx + 1;
					idx++;
				}

				System.out.println("OWLAPI-Lite PARSER ERROR: the entity " + tokens[badEntityIdx]
						+ " in its current position in the expression is not recognized by the parser! Either you have not created this entity or it should not appear in this position within the expression.");
			}

			if (clsEx != null) {
				if (fullIRIRendering)
					System.out.println("OWLClassExpression: " + clsEx);
				else
					System.out.println("OWLClassExpression: " + renderer.render(clsEx));
			}
			return clsEx;
		}
	}

	/**
	 * Creates multiple class names, adds them to the parser vocabulary and the
	 * currently selected ontology
	 * 
	 * @param classnames A single space separated list of class names
	 */
	public void createClasses(String classnames) {
		if (selectedOntology == null) {
			System.out
					.println("OWLAPI-Lite ERROR: There is no ontology to add properties to! First create an ontology.");
		} else {
			String[] classes = classnames.split(" ");
			if (classes.length == 0) {
				System.out.println(
						"OWLAPI-Lite PARSER ERROR: incorrect syntax for creating class names. String requires more than 1 token (class names) each separated by single spaces");
			} else {
				for (String c : classes)
					createClass(c);
			}
		}
	}

	/**
	 * Creates multiple object properties, adds them to the parser vocabulary and
	 * the currently selected ontology
	 * 
	 * @param opropsstr A single space separated list of object properties
	 */
	public void createObjectProperties(String opropsstr) {
		if (selectedOntology == null) {
			System.out
					.println("OWLAPI-Lite ERROR: There is no ontology to add properties to! First create an ontology.");
		} else {
			String[] oprops = opropsstr.split(" ");
			if (oprops.length == 0) {
				System.out.println(
						"OWLAPI-Lite PARSER ERROR: incorrect syntax for creating object property names. String requires more than 1 token (object property names) each separated by single spaces");
			} else {
				for (String o : oprops)
					createObjectProperty(o);
			}
		}
	}

	/**
	 * Alias for createObjectProperties method. Creates multiple object properties,
	 * adds them to the parser vocabulary and
	 * the currently selected ontology
	 * 
	 * @param opropsstr A single space separated list of object properties
	 */
	public void createOProperties(String opropsstr) {
		if (selectedOntology == null) {
			System.out
					.println("OWLAPI-Lite ERROR: There is no ontology to add properties to! First create an ontology.");
		} else {
			String[] oprops = opropsstr.split(" ");
			if (oprops.length == 0) {
				System.out.println(
						"OWLAPI-Lite PARSER ERROR: incorrect syntax for creating object property names. String requires more than 1 token (object property names) each separated by single spaces");
			} else {
				for (String o : oprops)
					createObjectProperty(o);
			}
		}
	}

	/**
	 * Creates multiple individual names, adds them to the parser vocabulary and the
	 * currently selected ontology
	 * 
	 * @param indnames A single space separated list of individual names
	 */
	public void createIndividuals(String indnames) {
		if (selectedOntology == null) {
			System.out
					.println("OWLAPI-Lite ERROR: There is no ontology to add properties to! First create an ontology.");
		} else {
			String[] inds = indnames.split(" ");
			if (inds.length == 0) {
				System.out.println(
						"OWLAPI-Lite PARSER ERROR: incorrect syntax for creating individual names. String requires more than 1 token (individual names) each separated by single spaces");
			} else {
				for (String i : inds)
					createIndividual(i);
			}
		}
	}

	/**
	 * Creates multiple data properties, adds them to the parser vocabulary and the
	 * currently selected ontology
	 * 
	 * @param dpropsstr A single space separated list of data properties
	 */
	public void createDataProperties(String dpropsstr) {
		if (selectedOntology == null) {
			System.out
					.println("OWLAPI-Lite ERROR: There is no ontology to add properties to! First create an ontology.");
		} else {
			String[] dprops = dpropsstr.split(" ");
			if (dprops.length == 0) {
				System.out.println(
						"OWLAPI-Lite PARSER ERROR: incorrect syntax for creating data property names. String requires more than 1 token (data property names) each separated by single spaces");
			} else {
				for (String d : dprops)
					createDataProperty(d);
			}
		}
	}

	/**
	 * Alias for createDataProperties method. Creates multiple data properties, adds
	 * them to the parser vocabulary and the
	 * currently selected ontology
	 * 
	 * @param dpropsstr A single space separated list of data properties
	 */
	public void createDProperties(String dpropsstr) {
		if (selectedOntology == null) {
			System.out
					.println("OWLAPI-Lite ERROR: There is no ontology to add properties to! First create an ontology.");
		} else {
			String[] dprops = dpropsstr.split(" ");
			if (dprops.length == 0) {
				System.out.println(
						"OWLAPI-Lite PARSER ERROR: incorrect syntax for creating data property names. String requires more than 1 token (data property names) each separated by single spaces");
			} else {
				for (String d : dprops)
					createDataProperty(d);
			}
		}
	}

	/**
	 * Makes an existing object property in the ontology transitive
	 * 
	 * @param opropname A string representation of an object property
	 */
	public void makeTransitive(String opropname) {
		if (selectedOntology == null) {
			System.out
					.println("OWLAPI-Lite ERROR: There is no ontology to add properties to! First create an ontology.");
		} else {
			OWLObjectProperty r = dataFactory
					.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + opropname));

			Stream<OWLAxiom> ontAxioms = selectedOntology.axioms();
			Set<OWLEntity> ontEntities = ontAxioms
					.<OWLEntity>flatMap(ax -> ax.signature())
					.collect(Collectors.toCollection(HashSet::new));

			if (ontEntities.contains(r)) {
				OWLTransitiveObjectPropertyAxiom t = dataFactory.getOWLTransitiveObjectPropertyAxiom(r,
						new HashSet<OWLAnnotation>());
				ontologyManager.addAxiom(selectedOntology, t);
				if (fullIRIRendering)
					System.out.println(t);
				else
					System.out.println(renderer.render(t));
			} else {
				System.out.println(
						"OWLAPI-Lite ERROR: " + renderer.render(r) + "does not appear in the selected ontology!");
			}
		}
	}

	/**
	 * Makes an existing object property in the ontology symmetric
	 * 
	 * @param opropname A string representation of an object property
	 */
	public void makeSymmetric(String opropname) {
		if (selectedOntology == null) {
			System.out
					.println("OWLAPI-Lite ERROR: There is no ontology to add properties to! First create an ontology.");
		} else {
			OWLObjectProperty r = dataFactory
					.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + opropname));

			Stream<OWLAxiom> ontAxioms = selectedOntology.axioms();
			Set<OWLEntity> ontEntities = ontAxioms
					.<OWLEntity>flatMap(ax -> ax.signature())
					.collect(Collectors.toCollection(HashSet::new));

			if (ontEntities.contains(r)) {

				OWLSymmetricObjectPropertyAxiom s = dataFactory.getOWLSymmetricObjectPropertyAxiom(r,
						new HashSet<OWLAnnotation>());
				ontologyManager.addAxiom(selectedOntology, s);
				if (fullIRIRendering)
					System.out.println(s);
				else
					System.out.println(renderer.render(s));
			} else {
				System.out.println(
						"OWLAPI-Lite ERROR: " + renderer.render(r) + "does not appear in the selected ontology!");
			}
		}
	}

	/**
	 * Makes an existing object property in the ontology reflexive
	 * 
	 * @param opropname A string representation of an object property
	 */
	public void makeReflexive(String opropname) {
		if (selectedOntology == null) {
			System.out
					.println("OWLAPI-Lite ERROR: There is no ontology to add properties to! First create an ontology.");
		} else {
			OWLObjectProperty r = dataFactory
					.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + opropname));

			Stream<OWLAxiom> ontAxioms = selectedOntology.axioms();
			Set<OWLEntity> ontEntities = ontAxioms
					.<OWLEntity>flatMap(ax -> ax.signature())
					.collect(Collectors.toCollection(HashSet::new));

			if (ontEntities.contains(r)) {
				OWLReflexiveObjectPropertyAxiom re = dataFactory.getOWLReflexiveObjectPropertyAxiom(r,
						new HashSet<OWLAnnotation>());
				ontologyManager.addAxiom(selectedOntology, re);
				if (fullIRIRendering)
					System.out.println(re);
				else
					System.out.println(renderer.render(re));
			} else {
				System.out.println(
						"OWLAPI-Lite ERROR: " + renderer.render(r) + "does not appear in the selected ontology!");
			}
		}
	}

	/**
	 * Makes an existing object property in the ontology irreflexive
	 * 
	 * @param opropname A string representation of an object property
	 */
	public void makeIRReflexive(String opropname) {
		if (selectedOntology == null) {
			System.out
					.println("OWLAPI-Lite ERROR: There is no ontology to add properties to! First create an ontology.");
		} else {
			OWLObjectProperty r = dataFactory
					.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + opropname));

			Stream<OWLAxiom> ontAxioms = selectedOntology.axioms();
			Set<OWLEntity> ontEntities = ontAxioms
					.<OWLEntity>flatMap(ax -> ax.signature())
					.collect(Collectors.toCollection(HashSet::new));

			if (ontEntities.contains(r)) {

				OWLIrreflexiveObjectPropertyAxiom irr = dataFactory.getOWLIrreflexiveObjectPropertyAxiom(r,
						new HashSet<OWLAnnotation>());
				ontologyManager.addAxiom(selectedOntology, irr);
				if (fullIRIRendering)
					System.out.println(irr);
				else
					System.out.println(renderer.render(irr));
			} else {
				System.out.println(
						"OWLAPI-Lite ERROR: " + renderer.render(r) + "does not appear in the selected ontology!");
			}
		}
	}

	/**
	 * Makes an existing object property in the ontology asymmetric
	 * 
	 * @param opropname A string representation of an object property
	 */
	public void makeAntiSymmetric(String opropname) {
		if (selectedOntology == null) {
			System.out
					.println("OWLAPI-Lite ERROR: There is no ontology to add properties to! First create an ontology.");
		} else {
			OWLObjectProperty r = dataFactory
					.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + opropname));

			Stream<OWLAxiom> ontAxioms = selectedOntology.axioms();
			Set<OWLEntity> ontEntities = ontAxioms
					.<OWLEntity>flatMap(ax -> ax.signature())
					.collect(Collectors.toCollection(HashSet::new));

			if (ontEntities.contains(r)) {

				OWLAsymmetricObjectPropertyAxiom a = dataFactory.getOWLAsymmetricObjectPropertyAxiom(r,
						new HashSet<OWLAnnotation>());
				ontologyManager.addAxiom(selectedOntology, a);
				if (fullIRIRendering)
					System.out.println(a);
				else
					System.out.println(renderer.render(a));
			} else {
				System.out.println(
						"OWLAPI-Lite ERROR: " + renderer.render(r) + "does not appear in the selected ontology!");
			}
		}
	}

	/**
	 * Creates role axiom from string
	 * 
	 * @param axiomStr OWL role assertion axiom string expression
	 * @return An OWLAxiom object representing the role assertion axiom in the input
	 */
	public OWLAxiom createObjectPropertyAssertion(String axiomStr) {
		String[] parts = axiomStr.split(" ");
		if (parts.length != 3) {
			System.out.println(
					"Parser error: incorrect syntax for role assertion. requires exactly three tokens separated by single spaces");
			return null;
		} else {
			OWLObjectPropertyAssertionAxiom a = dataFactory.getOWLObjectPropertyAssertionAxiom(
					dataFactory.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + parts[1])),
					dataFactory.getOWLNamedIndividual(IRI.create(selectedOntologyIRI.toString() + parts[0])),
					dataFactory.getOWLNamedIndividual(IRI.create(selectedOntologyIRI.toString() + parts[2])));
			ontologyManager.addAxiom(selectedOntology, a);
			if (fullIRIRendering)
				System.out.println("ObjectPropertyAssertion: " + a);
			else
				System.out.println("ObjectPropertyAssertion: " + renderer.render(a));
			return a;
		}
	}

	/**
	 * Alias for createObjectPropertyAssertion method
	 * 
	 * @param axiomStr OWL role assertion axiom string expression
	 * @return An OWLAxiom object representing the role assertion axiom in the input
	 */
	public OWLAxiom createOPropertyAssertion(String axiomStr) {
		String[] parts = axiomStr.split(" ");
		if (parts.length != 3) {
			System.out.println(
					"Parser error: incorrect syntax for role assertion. requires exactly three tokens separated by single spaces");
			return null;
		} else {
			OWLObjectPropertyAssertionAxiom a = dataFactory.getOWLObjectPropertyAssertionAxiom(
					dataFactory.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + parts[1])),
					dataFactory.getOWLNamedIndividual(IRI.create(selectedOntologyIRI.toString() + parts[0])),
					dataFactory.getOWLNamedIndividual(IRI.create(selectedOntologyIRI.toString() + parts[2])));
			ontologyManager.addAxiom(selectedOntology, a);
			if (fullIRIRendering)
				System.out.println("ObjectPropertyAssertion: " + a);
			else
				System.out.println("ObjectPropertyAssertion: " + renderer.render(a));
			return a;
		}
	}

	/**
	 * Makes all the individual names in the ontology refer to different individuals
	 * (simulate the unique name assumption)
	 * 
	 * @return An OWLAxiom which enforces that all individual names in the ontology
	 *         refer to different individuals (simulating the unique name
	 *         assumption)
	 */
	public OWLAxiom allIndividualsDifferent() {
		Set<OWLNamedIndividual> inds = selectedOntology.individualsInSignature().collect(Collectors.toCollection(HashSet::new));

		if (inds.size() < 2) {
			System.out.println("Error: requires more than 1 individual name in the ontology.");
			return null;
		} else {
			OWLAxiom a = dataFactory.getOWLDifferentIndividualsAxiom(inds, new HashSet<OWLAnnotation>());
			ontologyManager.addAxiom(selectedOntology, a);
			if (fullIRIRendering)
				System.out.println(a);
			else
				System.out.println(renderer.render(a));
			return a;
		}
	}

	/**
	 * Makes the specified individual names in the ontology refer to different
	 * individuals (simulate the unique name assumption for a subset of individual
	 * names)
	 * 
	 * @param differentIndividualsStr a single-space separated list of individual
	 *                                names e.g. "john mary mark" (no quotes).
	 * @return An OWLAxiom which enforces that the specified individual names refer
	 *         to different individuals (simulating the unique name assumption for
	 *         the given individual names)
	 */
	public OWLAxiom differentIndividuals(String differentIndividualsStr) {
		String[] parts = differentIndividualsStr.split(" ");
		Set<OWLIndividual> inds = new HashSet<OWLIndividual>();
		for (String s : parts) {
			inds.add(dataFactory.getOWLNamedIndividual(IRI.create(selectedOntologyIRI.toString() + s)));
		}

		if (parts.length < 2) {
			System.out.println(
					"OWLAPI-Lite PARSER ERROR: incorrect syntax for different individuals axiom. requires more than 1 token (individual name) separated by single spaces");
			return null;
		} else {
			OWLAxiom a = dataFactory.getOWLDifferentIndividualsAxiom(inds, new HashSet<OWLAnnotation>());
			ontologyManager.addAxiom(selectedOntology, a);
			if (fullIRIRendering)
				System.out.println(a);
			else
				System.out.println(renderer.render(a));
			return a;
		}
	}

	/**
	 * Prints a grouped list of the main logical entities in the ontology to console
	 * (classes, properties, individuals, TBox, ABox, RBox etc.)
	 */
	public void printOntology() {
		System.out.println();
		String ontIRI = selectedOntology.getOntologyID().getDefaultDocumentIRI().get().toString();
		System.out.println("Summary of ontology: " + ontIRI);
		for (int i = 0; i < ontIRI.length() + 21; i++)
			System.out.print("-");
		System.out.println();

		Set<OWLEntity> signature = selectedOntology.signature().collect(Collectors.toCollection(HashSet::new));
		Set<OWLAxiom> rbox = selectedOntology.rboxAxioms(Imports.EXCLUDED).collect(Collectors.toCollection(HashSet::new));
		Set<OWLAxiom> tbox = selectedOntology.tboxAxioms(Imports.EXCLUDED).collect(Collectors.toCollection(HashSet::new));
		Set<OWLAxiom> abox = selectedOntology.aboxAxioms(Imports.EXCLUDED).collect(Collectors.toCollection(HashSet::new));

		Set<OWLEntity> clses = new HashSet<OWLEntity>();
		Set<OWLEntity> oprops = new HashSet<OWLEntity>();
		Set<OWLEntity> dprops = new HashSet<OWLEntity>();
		Set<OWLEntity> inds = new HashSet<OWLEntity>();

		for (OWLEntity e : signature) {
			if (e.isOWLClass())
				clses.add(e);
			if (e.isOWLObjectProperty())
				oprops.add(e);
			if (e.isOWLNamedIndividual())
				inds.add(e);
			if (e.isOWLDataProperty())
				dprops.add(e);
		}

		System.out.println();

		System.out.println("Classes:");
		System.out.println("--------");

		int cls_idx = 1;
		for (OWLEntity c : clses) {
			System.out.println(cls_idx + ". " + renderer.render(c));
			cls_idx++;
		}

		System.out.println();

		System.out.println("Object properties:");
		System.out.println("------------------");

		int op_idx = 1;
		for (OWLEntity r : oprops) {
			System.out.println(op_idx + ". " + renderer.render(r));
			op_idx++;
		}

		System.out.println();

		System.out.println("Data properties:");
		System.out.println("----------------");

		int dp_idx = 1;
		for (OWLEntity r : dprops) {
			System.out.println(dp_idx + ". " + renderer.render(r));
			dp_idx++;
		}

		System.out.println();

		System.out.println("Individuals:");
		System.out.println("------------");

		int i_idx = 1;
		for (OWLEntity i : inds) {
			System.out.println(i_idx + ". " + renderer.render(i));
			i_idx++;
		}

		System.out.println();

		System.out.println("TBox:");
		System.out.println("-----");

		int tbox_idx = 1;
		for (OWLAxiom a : tbox) {
			System.out.println(tbox_idx + ". " + renderer.render(a));
			tbox_idx++;
		}

		System.out.println();

		System.out.println("ABox:");
		System.out.println("-----");

		int abox_idx = 1;
		for (OWLAxiom a : abox) {
			System.out.println(abox_idx + ". " + renderer.render(a));
			abox_idx++;
		}

		System.out.println();

		System.out.println("RBox:");
		System.out.println("-----");

		int rbox_idx = 1;
		for (OWLAxiom a : rbox) {
			System.out.println(rbox_idx + ". " + renderer.render(a));
			rbox_idx++;
		}
	}

	/**
	 * Prints an OWLAxiom object to console output
	 * 
	 * @param axiom OWLAxiom object to print to console
	 */
	public void print(OWLAxiom axiom) {
		if (fullIRIRendering)
			System.out.println(axiom);
		else
			System.out.println(renderer.render(axiom));
	}

	/**
	 * Prints OWLOntology metrics to console output e.g. number of axioms, subclass
	 * axioms etc.
	 */
	public void printOntologyStats() {
		System.out.println();
		String ontIRI = selectedOntology.getOntologyID().getDefaultDocumentIRI().get().toString();
		System.out.println("Stats for ontology: " + ontIRI);
		for (int i = 0; i < ontIRI.length() + 20; i++)
			System.out.print("-");
		System.out.println();
		// Number of axioms and constructs in ontology
		System.out.println("Number of axioms: " + selectedOntology.getAxiomCount());
		System.out.println("Number of logical axioms: " + selectedOntology.getLogicalAxiomCount());
		System.out.println("Number of classes: " + selectedOntology.classesInSignature(Imports.EXCLUDED).count());
		System.out.println("Number of object properties: "
				+ selectedOntology.objectPropertiesInSignature(Imports.EXCLUDED).count());
		System.out.println(
				"Number of data properties: " + selectedOntology.dataPropertiesInSignature(Imports.EXCLUDED).count());
		System.out.println(
				"Number of individuals: " + selectedOntology.individualsInSignature(Imports.EXCLUDED).count());
		// Number of axioms of a specific type in ontology
		System.out.println("Number of SubClassOf axioms: " + selectedOntology.axioms(AxiomType.SUBCLASS_OF).count());
		System.out.println("Number of EquivalentClasses axioms: "
				+ selectedOntology.axioms(AxiomType.EQUIVALENT_CLASSES).count());
		System.out.println(
				"Number of DisjointClasses axioms: " + selectedOntology.axioms(AxiomType.DISJOINT_CLASSES).count());
		System.out
				.println("Number of Class assertions: " + selectedOntology.axioms(AxiomType.CLASS_ASSERTION).count());
		System.out.println("Number of Object property assertions: "
				+ selectedOntology.axioms(AxiomType.OBJECT_PROPERTY_ASSERTION).count());
		System.out.println("Number of Data property assertions: "
				+ selectedOntology.axioms(AxiomType.DATA_PROPERTY_ASSERTION).count());
	}

	/**
	 * Removes class name from the parser vocabulary and currently selected ontology
	 * 
	 * @param classname A string representation of the class name to remove
	 */
	public void removeClass(String classname) {
		if (selectedOntology != null) {
			OWLClass c = dataFactory.getOWLClass(IRI.create(selectedOntologyIRI.toString() + classname));
			OWLDeclarationAxiomImpl a = new OWLDeclarationAxiomImpl(c, new HashSet<OWLAnnotation>());
			selectedOntology.removeAxiom(a);
		} else {
			System.out.println("OWLAPI-Lite ERROR: There is no ontology to remove classes from!");
		}
	}

	/**
	 * Removes multiple class names from the parser vocabulary and currently
	 * selected ontology
	 * 
	 * @param classnames A single space separated list of class names to remove from
	 *                   the ontology e.g. "Student Lecturer Person"
	 */
	public void removeClasses(String classnames) {
		if (selectedOntology == null) {
			System.out.println("OWLAPI-Lite ERROR: There is no ontology to remove classes from!");
		} else {
			String[] classes = classnames.split(" ");
			if (classes.length == 0) {
				System.out.println(
						"OWLAPI-Lite PARSER ERROR: incorrect syntax for removing class names. String requires more than 1 token (class names) each separated by single spaces");
			} else {
				for (String c : classes)
					removeClass(c);
			}
		}
	}

	/**
	 * Removes an object property from the parser vocabulary and currently selected
	 * ontology
	 * 
	 * @param opropname A string representation of the object property to remove
	 */
	public void removeObjectProperty(String opropname) {
		if (selectedOntology != null) {
			OWLObjectProperty r = dataFactory
					.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + opropname));
			OWLDeclarationAxiomImpl a = new OWLDeclarationAxiomImpl(r, new HashSet<OWLAnnotation>());
			selectedOntology.removeAxiom(a);
		} else {
			System.out.println("OWLAPI-Lite ERROR: there is no selected ontology to property from!");
		}
	}

	/**
	 * Alias for removeObjectProperty method. Removes an object property from the
	 * parser vocabulary and currently selected
	 * ontology
	 * 
	 * @param opropname A string representation of the object property to remove
	 */
	public void removeOProperty(String opropname) {
		if (selectedOntology != null) {
			OWLObjectProperty r = dataFactory
					.getOWLObjectProperty(IRI.create(selectedOntologyIRI.toString() + opropname));
			OWLDeclarationAxiomImpl a = new OWLDeclarationAxiomImpl(r, new HashSet<OWLAnnotation>());
			selectedOntology.removeAxiom(a);
		} else {
			System.out.println("OWLAPI-Lite ERROR: there is no selected ontology to property from!");
		}
	}

	/**
	 * Removes a data property from the parser vocabulary and currently selected
	 * ontology
	 * 
	 * @param dpropname A string representation of the data property to remove
	 */
	public void removeDataProperty(String dpropname) {
		if (selectedOntology != null) {
			OWLDataProperty r = dataFactory.getOWLDataProperty(IRI.create(selectedOntologyIRI.toString() + dpropname));
			OWLDeclarationAxiomImpl a = new OWLDeclarationAxiomImpl(r, new HashSet<OWLAnnotation>());
			selectedOntology.removeAxiom(a);
		} else {
			System.out.println("OWLAPI-Lite ERROR: there is no selected ontology to remove property from!");
		}
	}

	/**
	 * Alias for removeDataProperty method. Removes a data property from the parser
	 * vocabulary and currently selected
	 * ontology
	 * 
	 * @param dpropname A string representation of the data property to remove
	 */
	public void removeDProperty(String dpropname) {
		if (selectedOntology != null) {
			OWLDataProperty r = dataFactory.getOWLDataProperty(IRI.create(selectedOntologyIRI.toString() + dpropname));
			OWLDeclarationAxiomImpl a = new OWLDeclarationAxiomImpl(r, new HashSet<OWLAnnotation>());
			selectedOntology.removeAxiom(a);
		} else {
			System.out.println("OWLAPI-Lite ERROR: there is no selected ontology to remove property from!");
		}
	}

	/**
	 * Removes multiple object properties from the parser vocabulary and currently
	 * selected ontology
	 * 
	 * @param opropnames A single space separated list of object properties to
	 *                   remove from the ontology e.g. "marriedTo hasTopping
	 *                   teachesCourse"
	 */
	public void removeObjectProperties(String opropnames) {
		if (selectedOntology == null) {
			System.out.println("OWLAPI-Lite ERROR: There is no ontology to remove properties from!");
		} else {
			String[] oprops = opropnames.split(" ");
			if (oprops.length == 0) {
				System.out.println(
						"OWLAPI-Lite PARSER ERROR: incorrect syntax for removing properties. String requires more than 1 token (property names) each separated by single spaces");
			} else {
				for (String o : oprops)
					removeOProperty(o);
			}
		}
	}

	/**
	 * Alias for removeObjectProperties method. Removes multiple object properties
	 * from the parser vocabulary and currently
	 * selected ontology
	 * 
	 * @param opropnames A single space separated list of object properties to
	 *                   remove from the ontology e.g. "marriedTo hasTopping
	 *                   teachesCourse"
	 */
	public void removeOProperties(String opropnames) {
		if (selectedOntology == null) {
			System.out.println("OWLAPI-Lite ERROR: There is no ontology to remove properties from!");
		} else {
			String[] oprops = opropnames.split(" ");
			if (oprops.length == 0) {
				System.out.println(
						"OWLAPI-Lite PARSER ERROR: incorrect syntax for removing properties. String requires more than 1 token (property names) each separated by single spaces");
			} else {
				for (String o : oprops)
					removeOProperty(o);
			}
		}
	}

	/**
	 * Removes multiple data properties from the parser vocabulary and currently
	 * selected ontology
	 * 
	 * @param dpropnames A single space separated list of data properties to remove
	 *                   from the ontology e.g. "hasHeight hasSalary
	 *                   hasConcentration"
	 */
	public void removeDataProperties(String dpropnames) {
		if (selectedOntology == null) {
			System.out.println("OWLAPI-Lite ERROR: There is no ontology to remove properties from!");
		} else {
			String[] dprops = dpropnames.split(" ");
			if (dprops.length == 0) {
				System.out.println(
						"OWLAPI-Lite PARSER ERROR: incorrect syntax for removing properties. String requires more than 1 token (property names) each separated by single spaces");
			} else {
				for (String d : dprops)
					removeDProperty(d);
			}
		}
	}

	/**
	 * Alias for removeDataProperties method. Removes multiple data properties from
	 * the parser vocabulary and currently
	 * selected ontology
	 * 
	 * @param dpropnames A single space separated list of data properties to remove
	 *                   from the ontology e.g. "hasHeight hasSalary
	 *                   hasConcentration"
	 */
	public void removeDProperties(String dpropnames) {
		if (selectedOntology == null) {
			System.out.println("OWLAPI-Lite ERROR: There is no ontology to remove properties from!");
		} else {
			String[] dprops = dpropnames.split(" ");
			if (dprops.length == 0) {
				System.out.println(
						"OWLAPI-Lite PARSER ERROR: incorrect syntax for removing properties. String requires more than 1 token (property names) each separated by single spaces");
			} else {
				for (String d : dprops)
					removeDProperty(d);
			}
		}
	}

	/**
	 * Removes individual name from the parser vocabulary and currently selected
	 * ontology
	 * 
	 * @param individualname A string representation of the object property to
	 *                       remove
	 */
	public void removeIndividual(String individualname) {
		if (selectedOntology == null) {
			System.out.println("OWLAPI-Lite ERROR: There is no ontology to remove individuals from!");
			OWLNamedIndividual i = dataFactory
					.getOWLNamedIndividual(IRI.create(selectedOntologyIRI.toString() + individualname));
			OWLDeclarationAxiomImpl a = new OWLDeclarationAxiomImpl(i, new HashSet<OWLAnnotation>());
			selectedOntology.removeAxiom(a);
		} else {
			System.out.println("OWLAPI-Lite ERROR: There is no ontology to remove individuals from!");
		}
	}

	/**
	 * Removes multiple individual names from the parser vocabulary and currently
	 * selected ontology
	 * 
	 * @param indnames A single space separated list of individual names to remove
	 *                 from the ontology e.g. "john mary mark"
	 */
	public void removeIndividuals(String indnames) {
		if (selectedOntology == null) {
			System.out.println("OWLAPI-Lite ERROR: There is no ontology to remove properties from!");
		} else {
			String[] inds = indnames.split(" ");
			if (inds.length == 0) {
				System.out.println(
						"OWLAPI-Lite PARSER ERROR: incorrect syntax for removing individuals. String requires more than 1 token (individual names) each separated by single spaces");
			} else {
				for (String i : inds)
					removeIndividual(i);
			}
		}
	}

	/**
	 * Removes axiom from the currently selected ontology
	 * 
	 * @param axiomStr A string representation of the axiom to remove in Manchester
	 *                 OWL syntax
	 */
	public void removeAxiom(String axiomStr) {
		parser.setString(axiomStr);
		OWLAxiom axiom = null;

		try {
			axiom = parser.getParser().parseAxiom();
		} catch (OWLParserException ope) {
			System.out.println("OWLAPI-Lite PARSER ERROR: " + ope.getMessage());
		}

		if (axiom != null)
			selectedOntology.removeAxiom(axiom);
	}

	/**
	 * Removes axiom from the currently selected ontology
	 * 
	 * @param a an OWLAxiom object reference for the axiom to remove
	 */
	public void removeAxiom(OWLAxiom a) {
		if (selectedOntology != null) {
			selectedOntology.removeAxiom(a);
		} else {
			System.out.println("OWLAPI-Lite ERROR: there is no selected ontology to remove axiom from!");
		}
	}

	/**
	 * Removes a set of axioms from the currently selected ontology
	 * 
	 * @param a a HashSet of OWLAxiom objects to remove from the ontology
	 */
	public void removeAxioms(Set<OWLAxiom> a) {
		if (selectedOntology != null) {
			selectedOntology.removeAxioms(a);
		} else {
			System.out.println("OWLAPI-Lite ERROR: there is no selected ontology to remove axioms from!");
		}
	}

	/**
	 * Removes all axioms (logical and otherwise) from the ontology
	 */
	public void resetOntology() {
		if (selectedOntology != null) {
			System.out.println(selectedOntology.getOntologyID().getOntologyIRI().get().toString());
			selectedOntology.removeAxioms(selectedOntology.axioms());
			selectedOntology = null;
		} else {
			System.out.println("OWLAPI-Lite ERROR: no ontologies to reset in workspace!");
		}
	}

	/**
	 * Loads an OWL ontology from local file into the SimpleOWLAPIFactory instance.
	 * This ontology becomes the currently selected ontology.
	 * 
	 * @param filepath a string representation of the relative path to the local
	 *                 ontology file on the local machine
	 * @return an OWLOntology instance
	 * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException if the
	 *                                                                   filepath is
	 *                                                                   invalid
	 */
	public OWLOntology loadFromFile(String filepath) throws OWLOntologyCreationException {
		System.out.println();
		File file = new File(filepath);
		OWLOntology ontology = null;

		try {
			ontology = ontologyManager.loadOntologyFromOntologyDocument(file);
			System.out.println("Loaded ontology: <" + ontology.getOntologyID().getDefaultDocumentIRI().get().toString()
					+ "> into workspace.");
		} catch (OWLOntologyCreationException ooce) {
			System.out.println("OWLAPI-Lite LOADING ERROR: either the ontology file " + filepath
					+ " could not be found, it could not be parsed, or it already exists in your workspace.");
		}
		// } catch (OWLOntologyInputSourceException ooise) {
		// System.out.println("OWLAPI-Lite LOADING ERROR: either the ontology file " +
		// filepath
		// + " could not be found, it could not be parsed, or it already exists in your
		// workspace.");
		// }

		if (ontology != null) {
			// add new ontology signature to parser vocabulary so we can use Manchester OWL
			// strings to manipulate and query it
			for (OWLEntity e : ontology.signature().collect(Collectors.toCollection(HashSet::new))) {
				parser.addVocab(e);
			}

			selectedOntology = ontology;
			selectedOntologyIRI = ontology.getOntologyID().getDefaultDocumentIRI().get();
			owlReasoner = null;
			owlReasoner = new SimpleOWLReasoner(reasonerFactory, selectedOntology, parser, selectedReasoner);
		}
		return ontology;
	}

	/**
	 * Loads an OWL ontology from a remote URL into the SimpleOWLAPIFactory
	 * instance. This ontology becomes the currently selected ontology.
	 * 
	 * @param url a string representation of the URL to the ontology on the Web
	 * @return an OWLOntology instance
	 * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException if the
	 *                                                                   remote URL
	 *                                                                   is invalid
	 */
	public OWLOntology loadFromURL(String url) throws OWLOntologyCreationException {
		System.out.println();
		IRI remoteOntologyIRI = IRI.create(url);
		OWLOntology ontology = null;

		try {
			ontology = ontologyManager.loadOntology(remoteOntologyIRI);
			System.out.println("Loaded ontology: <" + ontology.getOntologyID().getDefaultDocumentIRI().get().toString()
					+ "> into workspace.");
		} catch (OWLOntologyCreationException ooce) {
			System.out.println("OWLAPI-Lite LOADING ERROR: either the ontology at URL " + url
					+ " could not be found, it could not be parsed, or it already exists in your workspace.");
		}

		if (ontology != null) {
			// add new ontology signature to parser vocabulary so we can use Manchester OWL
			// strings to manipulate and query it
			for (OWLEntity e : ontology.signature().collect(Collectors.toCollection(HashSet::new))) {
				parser.addVocab(e);
			}

			selectedOntology = ontology;
			selectedOntologyIRI = ontology.getOntologyID().getDefaultDocumentIRI().get();
			owlReasoner = null;
			owlReasoner = new SimpleOWLReasoner(reasonerFactory, selectedOntology, parser, selectedReasoner);
		}
		return ontology;
	}

	/**
	 * Saves the currently selected ontology to a local file in Manchester OWL
	 * syntax
	 * 
	 * @param filepath a string representation of the path and filename to save the
	 *                 ontology to
	 * @throws java.io.FileNotFoundException                            if the
	 *                                                                  filepath is
	 *                                                                  invalid
	 * @throws org.semanticweb.owlapi.model.OWLOntologyStorageException if there is
	 *                                                                  a
	 *                                                                  serialisation
	 *                                                                  error when
	 *                                                                  saving the
	 *                                                                  ontology to
	 *                                                                  disk
	 */
	public void saveOntology(String filepath) throws OWLOntologyStorageException, FileNotFoundException {
		System.out.println();
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(filepath);
		} catch (FileNotFoundException fnfe) {
			System.out
					.println("OWLAPI-Lite SAVING ERROR: the save path for the ontology '" + filepath + "' is invalid.");
		} catch (SecurityException se) {
			System.out.println("OWLAPI-Lite SAVING ERROR: you do not have write access to save the ontology to '"
					+ filepath + "'.");
		}

		if (fout != null) {
			try {
				ontologyManager.saveOntology(selectedOntology, new ManchesterSyntaxDocumentFormat(), fout);
				System.out.println(
						"Saved ontology: <" + selectedOntology.getOntologyID().getDefaultDocumentIRI().get().toString()
								+ "> to '" + filepath + "'");
			} catch (OWLOntologyStorageException oose) {
				System.out.println("OWLAPI-Lite SAVING ERROR: the ontology could not be saved.");
			} catch (UnknownOWLOntologyException uoe) {
				System.out.println("OWLAPI-Lite SAVING ERROR: the ontology could not be saved.");
			}
		}
	}

	/**
	 * Removes an ontology from the current context (simpleOWLAPIFactory instance)
	 * 
	 * @param iriStr the IRI string of the ontology to remove
	 */
	public void removeOntology(String iriStr) {
		System.out.println();
		IRI ontIRI = IRI.create(iriStr);

		if (ontologyManager.contains(ontIRI)) {
			ontologyManager.removeOntology(ontologyManager.getOntology(ontIRI));
			System.out.println("Removed ontology <" + ontIRI + "> from workspace.");
		} else {
			System.out.println("OWLAPI-Lite ERROR: ontology <" + iriStr
					+ "> cannot be removed because it does not exist in workspace!");
		}

		if (ontologyManager.ontologies().count() > 0) {
			Iterator<OWLOntology> ontIter = ontologyManager.ontologies().iterator();
			while (ontIter.hasNext()) {
				OWLOntology currentOnt = ontIter.next();
				if (currentOnt.getOntologyID().getDefaultDocumentIRI().isPresent())
					selectedOntology = currentOnt;
			}
		} else {
			System.out.println("OWLAPI-Lite ERROR: ontology <" + iriStr
					+ "> cannot be removed because it does not exist in workspace - (there are no ontologies in this workspace)!");
		}
	}

	/**
	 * Removes an ontology from the current context (simpleOWLAPIFactory instance)
	 * 
	 * @param ontology the OWLOntology object to remove
	 */
	public void removeOntology(OWLOntology ontology) {
		System.out.println();
		if (ontology != null && ontologyManager.contains(ontology)) {
			ontologyManager.removeOntology(ontology);
			System.out.println("Removed ontology <" + ontology.getOntologyID().getOntologyIRI().get().toString()
					+ "> from workspace.");
		} else {
			System.out.println(
					"OWLAPI-Lite ERROR: ontology <" + ontology.getOntologyID().getOntologyIRI().get().toString()
							+ "> cannot be removed because it does not exist in workspace!");
		}

		if (ontologyManager.ontologies().count() > 0) {
			Iterator<OWLOntology> ontIter = ontologyManager.ontologies().iterator();
			while (ontIter.hasNext()) {
				OWLOntology currentOnt = ontIter.next();
				if (currentOnt.getOntologyID().getDefaultDocumentIRI().isPresent())
					selectedOntology = currentOnt;
			}
		} else {
			System.out.println("OWLAPI-Lite ERROR: ontology <"
					+ ontology.getOntologyID().getOntologyIRI().get().toString()
					+ "> cannot be removed because it does not exist in workspace - (there are no ontologies in this workspace)!");
		}
	}

	/**
	 * Removes selected ontology from the current context (simpleOWLAPIFactory
	 * instance)
	 */
	public void removeOntology() {
		System.out.println();
		if (selectedOntology != null && ontologyManager.contains(selectedOntology)) {
			ontologyManager.removeOntology(selectedOntology);
			System.out.println("Removed ontology <" + selectedOntologyIRI + "> from workspace.");
		} else {
			System.out.println("OWLAPI-Lite ERROR: there is no ontology currently selected - cannot remove one!");
		}

		if (ontologyManager.ontologies().count() > 0) {
			Iterator<OWLOntology> ontIter = ontologyManager.ontologies().iterator();
			while (ontIter.hasNext()) {
				OWLOntology currentOnt = ontIter.next();
				if (currentOnt.getOntologyID().getDefaultDocumentIRI().isPresent())
					selectedOntology = currentOnt;
			}
		} else {
			System.out.println("OWLAPI-Lite ERROR: there is no ontology to remove in the current workspace!");
		}
	}
}