package org.semanticweb.owl.simpleowlapi;

import java.io.FileNotFoundException;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 * OWLAPI-Lite is a light-weight wrapper for the OWLAPI enabling more concise
 * OWL ontology development.
 * 
 * Copyright (C) <2020> Kody Moodley
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

public class CreateOntologyEntities {
	public static void main(String[] args)
			throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {
		// create a new SimpleOWLAPIFactory instance which allows the construction and
		// manipulation of OWL ontologies (default OWL reasoner is JFACT)
		SimpleOWLAPIFactory s = SimpleOWLAPIFactory.getInstance();
		// create a new OWL ontology by specifying an IRI string and set it to the
		// currently selected (active) ontology
		// s.createOntology("http://com.kodymoodley/ontologies/2020/testontology#");
		// create multiple class names (each separated by a space) and add them to the
		// currently selected ontology
		s.createClasses("Penguin Peacock Bird Robin FlyingOrganism Fish Wing");
		// create multiple object properties (each separated by a space) and add them to
		// the currently selected ontology
		s.createObjectProperties("hasPart isPartOf hasGender knows eats hunts");
		// create multiple named individuals (each separated by a space) and add them to
		// the currently selected ontology
		s.createIndividuals("tweety woody nemo sharky sheila");
		// create multiple data properties (each separated by a space) and add them to
		// the currently selected ontology
		s.createDataProperties("hasWeight name bornOn");

		s.makeTransitive("eats"); // make an object property transitive
		s.makeSymmetric("knows"); // make an object property symmetric
		s.makeIRReflexive("hasGender"); // make an object property irreflexive
		s.makeAntiSymmetric("hasGender"); // make an object property asymmetric

		s.setFullIRIRendering(true); // set whether to render OWL entities (classes, individuals, properties, axioms
										// etc.) using full IRIs or shortform label

		s.createAxiom("tweety Type: hasWeight value \"300.56\"^^xsd:double"); // create an OWL data property assertion
																				// axiom and add it to the currently
																				// selected ontology
		s.createAxiom("tweety Type: hasGender exactly 1 Gender"); // create an OWL class assertion axiom and add it to
																	// the currently selected ontology
		s.createAxiom("tweety Type: Penguin");
		s.createAxiom("woody Type: knows value nemo"); // create an OWL object property assertion axiom and add it to
														// the currently selected ontology

		s.createAxiom("eats subPropertyOf: hunts"); // create OWL subPropertyOf axiom and add it to the currently
													// selected ontology
		s.createAxiom("isPartOf inverseOf: hasPart"); // create inverse object property axiom and add it to the
														// currently selected ontology
		s.createAxiom("knows Domain: Person"); // create object property domain axiom and add it to the currently
												// selected ontology
		s.createAxiom("knows Range: Person"); // create object property range axiom and add it to the currently selected
												// ontology

		s.createAxiom("Penguin subClassOf eats some Fish"); // create OWL subClassOf axiom and add it to the currently
															// selected ontology
		s.createAxiom("Penguin subClassOf Bird");
		s.createAxiom("Bird subClassOf FlyingOrganism");
		s.createAxiom("Penguin subClassOf not FlyingOrganism");
		s.createAxiom("Gender equivalentTo: Male or Female"); // create OWL equivalent classes axiom and add it to the
																// currently selected ontology
		s.createAxiom("Male disjointWith: Female"); // create OWL equivalent classes axiom and add it to the currently
													// selected ontology
		s.createAxiom("{tweety,woody} subClassOf Bird"); // create an OWL subClassOf axiom using nominals and add it to
															// the currently selected ontology

		s.createOntology("http://com.kodymoodley/ontologies/2020/testontology2#");
		s.setOntology("http://com.kodymoodley/ontologies/2020/testontology2#"); // set / switch the "active" or
																				// currently selected ontology by
																				// specifying the IRI of the ontology to
																				// switch to
		s.getOntology(); // prints to console the IRI of the currently selected ontology
		s.getOntologies(); // prints to console the IRIs of all ontologies created / loaded within the
							// current context (instance of the simpleOWLAPIFactory)
		s.printOntology(); // prints to console a structured representation of the main OWL entities in the
							// ontology
		s.printOntologyStats(); // prints ontology metrics (e.g. number of classes, axioms of a certain type
								// etc.)

		s.saveOntology("testontology.owl"); // save currently selected ontology to local file using Manchester OWL
											// syntax
		s.loadFromURL("https://protege.stanford.edu/ontologies/pizza/pizza.owl"); // load an ontology into this context
																					// from a remote URL
		s.loadFromFile("path/to/ontology.owl"); // load an ontology into this context from a local file path. WARNING:
												// you cannot load multiple ontologies with the same IRI into the same
												// context!
		s.removeOntology(); // remove selected ontology from current context (simpleOWLAPIFactory instance)
		s.removeOntology("http://com.kodymoodley/ontologies/2020/testontology2#"); // remove ontology with specified IRI
																					// from current context
																					// (simpleOWLAPIFactory instance)

		s.resetOntology(); // remove all axioms from the currently selected ontology
		s.removeClasses("Bird Penguin"); // remove multiple class names from currently selected ontology
		s.removeOProperties("knows hasPart"); // remove multiple object properties from currently selected ontology
		s.removeDProperties("hasWeight name"); // remove multiple data properties from currently selected ontology
		s.removeIndividuals("tweety woody"); // remove multiple individual names from currently selected ontology
		s.removeAxiom("Penguin subClassOf eats some Fish"); // remove an axiom from the currently selected ontology

		s.setOWLReasoner(SelectedReasoner.HERMIT); // Switch or set OWL reasoner

		s.owlReasoner.isConsistent(); // prints to console Yes if currently selected ontology is consistent, No
										// otherwise
		s.owlReasoner.explainInconsistency(); // computes and prints to console all explanations for the inconsistency
												// of the selected ontology (provided it is inconsistent)

		s.owlReasoner.isSatisfiable("Penguin"); // prints to console Yes if the given class expression is satisfiable,
												// No otherwiseNo otherwise
		s.owlReasoner.explainUnsatisfiability("Penguin"); // computes and prints to console all explanations for the
															// unsatisfiability of the given class expression w.r.t. the
															// selected ontology (provided it is indeed unsatisfiable)

		s.owlReasoner.isEntailed("Robin subClassOf FlyingOrganism"); // prints to console Yes if the given axiom is
																		// entailed by the currently selected ontology,
																		// No otherwise
		s.owlReasoner.explainEntailment("Robin subClassOf FlyingOrganism"); // computes and prints to console all
																			// explanations for the entailment of the
																			// given axiom w.r.t. the selected ontology
																			// (provided it is indeed entailed)

		s.owlReasoner.getSuperClasses("Robin"); // computes and prints to console all super classes (indirect) for the
												// given class expression
		s.owlReasoner.getSubClasses("FlyingOrganism"); // computes and prints to console all sub classes (indirect) for
														// the given class expression

		s.owlReasoner.getTypes("tweety"); // computes and prints to console all class names for which the given
											// individual is an instance
		s.owlReasoner.getAllTypes(); // for each individual name in the selected ontology, computes and prints to
										// console all class names such that this individual is an instance of the class
										// name

		s.owlReasoner.getOPropertyAssertions("knows"); // given an object property name R, prints to console all
														// individual name pairs (a,b) such that R(a, b) is an object
														// property assertion entailed by the selected ontology
		s.owlReasoner.getAllOPropertyAssertions(); // for each object property R in the ontology, prints to console all
													// individual name pairs (a,b) such that R(a, b) is an object
													// property assertion entailed by the selected ontology

		s.owlReasoner.getName(); // prints to console the name of the OWL reasoner which is currently being used
									// by the simpleOWLAPIFactory instance
		s.owlReasoner.getOWLProfile(); // prints to console the name of the OWL 2 profile which the selected OWL
										// reasoner supports
	}
}
