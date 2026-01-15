package org.semanticweb.owl.simpleowlapi;

import java.util.Set;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.api.ExplanationGenerator;
import org.semanticweb.owl.explanation.impl.blackbox.checker.InconsistentOntologyExplanationGeneratorFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator;
import com.clarkparsia.owlapi.explanation.util.SilentExplanationProgressMonitor;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

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
 * Class for OWLAPI-Lite which provides access to methods for reasoning with OWL
 * ontologies.
 * 
 * @author Kody Moodley
 * @author https://kodymoodley.github.io
 * @version 1.0.1
 */
public class SimpleOWLReasoner {
	/**
	 * the OWLOntology object being reasoned with
	 */
	public OWLOntology ontology;
	/**
	 * the IRI of the ontology being reasoned with
	 */
	public IRI ontologyIRI;
	/**
	 * the OWLReasoner object which is responsible for reasoning
	 */
	public OWLReasoner reasoner;
	/**
	 * Parser instance
	 */
	public Parser parser;
	/**
	 * OWLDataFactory instance
	 */
	public static OWLDataFactory dataFactory = new OWLDataFactoryImpl();
	/**
	 * SelectedReasoner instance
	 */
	public SelectedReasoner selectedReasoner;
	/**
	 * OWLReasonerFactory instance
	 */
	public OWLReasonerFactory reasonerFactory;
	/**
	 * An instance of a default explanation generator for computing justifications
	 * for entailments
	 */
	public DefaultExplanationGenerator explanationGenerator;
	/**
	 * An instance of Matthew Horridge's explanation generator factory for
	 * inconsistent ontologies
	 */
	public InconsistentOntologyExplanationGeneratorFactory inconsistencyExpFac;
	/**
	 * An instance of Matthew Horridge's explanation generator for inconsistent
	 * ontologies
	 */
	public ExplanationGenerator<OWLAxiom> inconsistencyExplanationGenerator;

	/**
	 * SimpleOWLReasoner constructor
	 * 
	 * @param reasonerFactory  reference to a specific OWLReasonerFactory
	 *                         implementation
	 * @param ontology         an OWLOntology object representing the ontology to be
	 *                         reasoned with
	 * @param parser           a Parser instance (Manchester OWL Syntax)
	 * @param selectedReasoner a SelectedReasoner instance holding metadata about
	 *                         the selected OWL reasoner
	 */
	public SimpleOWLReasoner(OWLReasonerFactory reasonerFactory, OWLOntology ontology, Parser parser,
			SelectedReasoner selectedReasoner) {
		this.selectedReasoner = selectedReasoner;
		this.reasonerFactory = reasonerFactory;
		this.ontology = ontology;
		this.ontologyIRI = this.ontology.getOntologyID().getDefaultDocumentIRI().get();
		this.reasoner = reasonerFactory.createNonBufferingReasoner(this.ontology);
		try {
			this.reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		} catch (org.semanticweb.owlapi.reasoner.InconsistentOntologyException ioe2) {
			System.out.println();
			System.out.println("OWLAPI-Lite REASONING ERROR: ontology classification is not possible because <"
					+ ontologyIRI.toString() + ">" + " is inconsistent!");
		}
		this.parser = parser;
	}

	// public static synchronized SimpleOWLReasoner getInstance(OWLReasonerFactory
	// reasonerFactory, OWLOntology ontology, Parser parser, SelectedReasoner
	// selectedReasoner)
	// {
	// if (obj==null){
	// obj = new SimpleOWLReasoner(reasonerFactory, ontology, parser,
	// selectedReasoner);
	// }
	// return obj;
	// }
	//
	// public void updateReasoner(OWLOntology ontology) {
	// reasoner.dispose();
	// this.reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
	// this.ontologyIRI = ontology.getOntologyID().getDefaultDocumentIRI().get();
	// reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
	// }

	// Get all atomic subclasses for the given class expression (both inferred by
	// the reasoner and asserted)

	/**
	 * prints all atomic equivalent classes of a given class expression string to
	 * console output
	 * 
	 * @param classEx a class expression string in Manchester OWL Syntax
	 */
	public void getEquivalentClasses(String classEx) {
		System.out.println();
		reasoner.flush();
		try {
			System.out.println("All equivalent classes of " + classEx);
			for (int i = 0; i < classEx.length() + 25; i++)
				System.out.print("-");
			System.out.println();
			Node<OWLClass> subclasses = reasoner.getEquivalentClasses(parser.createClassExpression(classEx));
			int idx = 1;
			for (OWLClass nc : subclasses) {
				if (!nc.isOWLNothing() && !nc.isOWLThing()) {
					System.out.println(idx + ". " + Parser.renderer.render(nc));
					idx++;
				}
			}
			System.out.println();
		} catch (InconsistentOntologyException ioe) {
			System.out.println();
			System.out.println("OWLAPI-Lite REASONING ERROR: computing all equivalent classes of " + classEx
					+ " is not possible because <" + ontology.getOntologyID().getDefaultDocumentIRI().get().toString()
					+ ">" + " is inconsistent!");
		}
	}

	/**
	 * prints all atomic subclasses (both direct and indirect) of a given class
	 * expression string to console output
	 * 
	 * @param classEx a class expression string in Manchester OWL Syntax
	 */
	public void getSubClasses(String classEx) {
		System.out.println();
		reasoner.flush();
		try {
			System.out.println("All subclasses of '" + classEx + "'");
			for (int i = 0; i < classEx.length() + 18; i++)
				System.out.print("-");
			System.out.println();
			NodeSet<OWLClass> subclasses = reasoner.getSubClasses(parser.createClassExpression(classEx), false);
			int idx = 1;
			for (Node<OWLClass> nc : subclasses) {
				for (OWLClass c : nc) {
					if (!c.isOWLNothing() && !c.isOWLThing()) {
						System.out.println(idx + ". " + Parser.renderer.render(c));
						idx++;
					}
				}
			}
			System.out.println();
		} catch (InconsistentOntologyException ioe) {
			System.out.println();
			System.out.println("OWLAPI-Lite REASONING ERROR: computing all subclasses of " + classEx
					+ " is not possible because <" + ontology.getOntologyID().getDefaultDocumentIRI().get().toString()
					+ ">" + " is inconsistent!");
		}
	}

	/**
	 * prints all atomic superclasses (both direct and indirect) of a given class
	 * expression string to console output
	 * 
	 * @param classEx a class expression string in Manchester OWL Syntax
	 */
	public void getSuperClasses(String classEx) {
		System.out.println();
		reasoner.flush();
		try {
			System.out.println("All superclasses of '" + classEx + "'");
			for (int i = 0; i < classEx.length() + 20; i++)
				System.out.print("-");
			System.out.println();
			NodeSet<OWLClass> supclasses = reasoner.getSuperClasses(parser.createClassExpression(classEx), false);
			int idx = 1;
			for (Node<OWLClass> nc : supclasses) {
				for (OWLClass c : nc) {
					if (!c.isOWLNothing() && !c.isOWLThing()) {
						System.out.println(idx + ". " + Parser.renderer.render(c));
						idx++;
					}
				}
			}
			System.out.println();
		} catch (InconsistentOntologyException ioe) {
			System.out.println();
			System.out.println("OWLAPI-Lite REASONING ERROR: computing all superclasses of " + classEx
					+ " is not possible because <" + ontology.getOntologyID().getDefaultDocumentIRI().get().toString()
					+ ">" + " is inconsistent!");
		}
	}

	/**
	 * prints all unsatisfiable class names in the ontology associated with this
	 * SimpleOWLReasoner instance to console output
	 */
	public void getUnsatisfiableClasses() {
		System.out.println();
		reasoner.flush();
		try {
			System.out.println("All unsatisfiable classes in <"
					+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">:");
			for (int i = 0; i < ontology.getOntologyID().getDefaultDocumentIRI().get().toString().length() + 33; i++)
				System.out.print("-");
			System.out.println();
			Node<OWLClass> classes = reasoner.getUnsatisfiableClasses();
			// System.out.println("classes.getSize());
			int idx = 1;
			for (OWLClass c : classes) {
				if (!c.isOWLNothing()) {
					System.out.println(idx + ". " + Parser.renderer.render(c));
					idx++;
				}
			}
			System.out.println();
		} catch (InconsistentOntologyException ioe) {
			System.out.println();
			System.out.println(
					"OWLAPI-Lite REASONING ERROR: computing all unsatisfiable classes is not possible because <"
							+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">"
							+ " is inconsistent!");
		}
	}

	/**
	 * prints Yes to console output if the ontology associated with this
	 * SimpleOWLReasoner instance is consistent, prints No otherwise
	 */
	public void isConsistent() {
		reasoner.flush();
		System.out.println();
		if (reasoner.isConsistent())
			System.out.println(
					"Yes - <" + ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + "> is consistent!");
		else
			System.out.println("No - <" + ontology.getOntologyID().getDefaultDocumentIRI().get().toString()
					+ "> is INconsistent!");
	}

	/**
	 * prints all atomic classes to console output, such that the individual
	 * (represented by the given string) is an instance of these classes
	 * 
	 * @param ind string representation of an individual name in the ontology
	 */
	public void getTypes(String ind) {
		System.out.println();
		this.reasoner.flush();
		try {
			System.out.println("Types for individual: '" + ind + "'");
			for (int i = 0; i < ind.length() + 22; i++)
				System.out.print("-");
			System.out.println();
			NodeSet<OWLClass> typesC = this.reasoner
					.getTypes(dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRI.toString() + ind)), false);
			int idx = 1;
			for (Node<OWLClass> c : typesC) {
				for (OWLClass c2 : c) {
					if (!c2.isOWLThing()) {
						System.out.println(idx + ". " + Parser.renderer.render(c2));
						idx++;
					}
				}
			}
			System.out.println();
		} catch (InconsistentOntologyException ioe) {
			System.out.println();
			System.out.println("OWLAPI-Lite REASONING ERROR: checking for entailed types is not possible because <"
					+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">" + " is inconsistent!");
		}
	}

	/**
	 * for each individual in the ontology, prints all atomic classes to console
	 * output, such that the individual is an instance of these classes
	 */
	public void getAllTypes() {
		this.reasoner.flush();
		try {
			System.out.println(
					"All Types in <" + ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">:");
			for (int i = 0; i < ontology.getOntologyID().getDefaultDocumentIRI().get().toString().length() + 16; i++)
				System.out.print("-");
			System.out.println();
			for (OWLIndividual i : ontology.getIndividualsInSignature(Imports.EXCLUDED)) {
				System.out.println(Parser.renderer.render(i));
				System.out.println("-----------");
				getTypes(Parser.renderer.render(i));
				System.out.println();
			}
			System.out.println();
		} catch (InconsistentOntologyException ioe) {
			System.out.println();
			System.out.println("OWLAPI-Lite REASONING ERROR: checking for entailed types is not possible because <"
					+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">" + " is inconsistent!");
		}
	}

	/**
	 * for a given object property, print all object property assertions it is
	 * involved in, to console output
	 * 
	 * @param opropStr string representing an object property
	 */
	public void getObjectPropertyAssertions(String opropStr) {
		System.out.println();
		this.reasoner.flush();
		try {
			System.out.println("Object Property Assertions for: " + opropStr);
			for (int i = 0; i < opropStr.length() + 32; i++)
				System.out.print("-");
			System.out.println();
			Set<OWLNamedIndividual> inds = ontology.getIndividualsInSignature(Imports.EXCLUDED);
			int idx = 1;
			for (OWLNamedIndividual i : inds) {
				NodeSet<OWLNamedIndividual> indP = this.reasoner.getObjectPropertyValues(i,
						dataFactory.getOWLObjectProperty(IRI.create(ontologyIRI.toString() + opropStr)));
				for (Node<OWLNamedIndividual> n : indP) {
					for (OWLNamedIndividual ai : n) {
						System.out.println(idx + ". " + Parser.renderer.render(i) + "," + Parser.renderer.render(ai));
						idx++;
					}
				}
			}
			if (inds.size() > 0)
				System.out.println();
		} catch (InconsistentOntologyException ioe) {
			System.out.println();
			System.out.println(
					"OWLAPI-Lite REASONING ERROR: checking for entailed object property assertions is not possible because <"
							+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">"
							+ " is inconsistent!");
		}
	}

	/**
	 * Alias for getObjectPropertyAssertions method. for a given object property, print all object property assertions it is
	 * involved in, to console output
	 * 
	 * @param opropStr string representing an object property
	 */
	public void getOPropertyAssertions(String opropStr) {
		this.reasoner.flush();

		try {
			System.out.println("Object Property Assertions for: " + opropStr);
			for (int i = 0; i < opropStr.length() + 32; i++)
				System.out.print("-");
			System.out.println();
			Set<OWLNamedIndividual> inds = ontology.getIndividualsInSignature(Imports.EXCLUDED);
			for (OWLNamedIndividual i : inds) {
				NodeSet<OWLNamedIndividual> indP = this.reasoner.getObjectPropertyValues(i,
						dataFactory.getOWLObjectProperty(IRI.create(ontologyIRI.toString() + opropStr)));
				for (Node<OWLNamedIndividual> n : indP) {
					for (OWLNamedIndividual ai : n) {
						System.out.println(Parser.renderer.render(i) + "," + Parser.renderer.render(ai));
					}
				}
			}
			if (inds.size() > 0)
				System.out.println();
		} catch (InconsistentOntologyException ioe) {
			System.out.println();
			System.out.println(
					"OWLAPI-Lite REASONING ERROR: checking for entailed object property assertions is not possible because <"
							+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">"
							+ " is inconsistent!");
		}
	}

	/**
	 * for a given class expression, print all its instances to console output
	 * 
	 * @param clsStr a class expression string in Manchester OWL Syntax
	 */
	public void getInstances(String clsStr) {
		System.out.println();
		OWLClassExpression cls = parser.createClassExpression(clsStr);
		reasoner.flush();
		try {
			System.out.println("Individuals of: '" + clsStr + "'");
			for (int i = 0; i < clsStr.length() + 16; i++)
				System.out.print("-");
			System.out.println();
			NodeSet<OWLNamedIndividual> inds = reasoner.getInstances(cls, false);
			int idx = 1;
			for (OWLNamedIndividual i : inds.getFlattened()) {
				System.out.println(idx + ". " + Parser.renderer.render(i));
				idx++;
			}

			if (inds.getFlattened().size() > 0)
				System.out.println();
		} catch (InconsistentOntologyException ioe) {
			System.out.println();
			System.out.println("OWLAPI-Lite REASONING ERROR: checking for instances is not possible because <"
					+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">" + " is inconsistent!");
		}
	}

	/**
	 * for each object property in the ontology, print all object property
	 * assertions they are involved in to console output
	 */
	public void getAllObjectPropertyAssertions() {
		this.reasoner.flush();
		try {
			System.out.println("All Object Property Assertions in <"
					+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">:");
			for (int i = 0; i < ontology.getOntologyID().getDefaultDocumentIRI().get().toString().length() + 37; i++)
				System.out.print("-");
			System.out.println();
			for (OWLObjectProperty o : ontology.getObjectPropertiesInSignature(Imports.EXCLUDED)) {
				getOPropertyAssertions(Parser.renderer.render(o));
				System.out.println();
			}
			System.out.println();
		} catch (InconsistentOntologyException ioe) {
			System.out.println();
			System.out.println(
					"OWLAPI-Lite REASONING ERROR: checking for entailed object property assertions is not possible because <"
							+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">"
							+ " is inconsistent!");
		}
	}

	/**
	 * Alias for getAllObjectPropertyAssertions method. for each object property in the ontology, print all object property
	 * assertions they are involved in to console output
	 */
	public void getAllOPropertyAssertions() {
		this.reasoner.flush();
		try {
			System.out.println("All Object Property Assertions in <"
					+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">:");
			for (int i = 0; i < ontology.getOntologyID().getDefaultDocumentIRI().get().toString().length() + 37; i++)
				System.out.print("-");
			System.out.println();
			for (OWLObjectProperty o : ontology.getObjectPropertiesInSignature(Imports.EXCLUDED)) {
				getOPropertyAssertions(Parser.renderer.render(o));
				System.out.println();
			}
			System.out.println();
		} catch (InconsistentOntologyException ioe) {
			System.out.println();
			System.out.println(
					"OWLAPI-Lite REASONING ERROR: checking for entailed object property assertions is not possible because <"
							+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">"
							+ " is inconsistent!");
		}
	}

	/**
	 * print the name of the selected OWL reasoner to console output
	 */
	public void getName() {
		System.out.println(selectedReasoner.getName());
	}

	/**
	 * print the name of the OWL 2 profile that this reasoner supports to console
	 * output
	 */
	public void getOWLProfile() {
		System.out.println(selectedReasoner.getProfile());
	}

	/**
	 * prints Yes to console output if the given string represents an OWLAxiom in
	 * Manchester OWL Syntax that is entailed by the ontology. Prints No, otherwise
	 * 
	 * @param axiomStr a string representation of an OWLAxiom in Manchester OWL
	 *                 Syntax
	 */
	public void isEntailed(String axiomStr) {
		parser.setString(axiomStr);
		System.out.println();
		OWLAxiom axiom = parser.getParser().parseAxiom();
		this.reasoner.flush();
		try {
			if (this.reasoner.isEntailed(axiom))
				System.out.println("Yes - Axiom: '" + axiomStr + "' is entailed by <"
						+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">!");
			else
				System.out.println("No - Axiom: '" + axiomStr + "' is not entailed by <"
						+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">!");
			System.out.println();
		} catch (InconsistentOntologyException ioe) {
			System.out.println();
			System.out.println("OWLAPI-Lite REASONING ERROR: checking if Axiom: " + axiomStr
					+ " is entailed is not possible because <"
					+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">" + " is inconsistent!");
		}
	}

	/**
	 * prints Yes to console output if the given string represents an
	 * OWLClassExpression in Manchester OWL Syntax that is satisfiable w.r.t. the
	 * ontology. Prints No, otherwise
	 * 
	 * @param clsStr a string representation of an OWLClassExpression in Manchester
	 *               OWL Syntax
	 */
	public void isSatisfiable(String clsStr) {
		System.out.println();
		OWLClassExpression cls = parser.createClassExpression(clsStr);
		reasoner.flush();
		try {
			if (reasoner.isSatisfiable(cls))
				System.out.println("Yes - Class: '" + clsStr + "' is satisfiable with respect to <"
						+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">!");
			else
				System.out.println("No - Class: '" + clsStr + "' is UNsatisfiable with respect to <"
						+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">!");
			System.out.println();
		} catch (InconsistentOntologyException ioe) {
			System.out.println();
			System.out.println("OWLAPI-Lite REASONING ERROR: checking if Class: " + clsStr
					+ " is satisfiable is not possible because <"
					+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">" + " is inconsistent!");
		}
	}

	/**
	 * prints to console output the explanations (justifications) for the
	 * unsatisfiability of a class expression (represented by the given string in
	 * Manchester OWL Syntax). Prints 'NOT unsatisfiable' if the class expression is
	 * satisfiable.
	 * 
	 * @param clsStr a string representation of an OWLClassExpression in Manchester
	 *               OWL Syntax
	 */
	public void explainUnsatisfiability(String clsStr) {
		System.out.println();
		reasoner.flush();
		try {
			explanationGenerator = new DefaultExplanationGenerator(ontology.getOWLOntologyManager(), reasonerFactory,
					ontology, new SilentExplanationProgressMonitor());
			OWLClassExpression cls = parser.createClassExpression(clsStr);
			if (!reasoner.isSatisfiable(cls)) {
				System.out.println("Explanation for unsatisfiability of '" + clsStr + "'");
				for (int i = 0; i < clsStr.length() + 37; i++)
					System.out.print("-");
				System.out.println();
				System.out.println();
				Set<Set<OWLAxiom>> explanations = explanationGenerator.getExplanations(cls);

				int count = 1;
				for (Set<OWLAxiom> exp : explanations) {
					System.out.println("Explanation " + count);
					System.out.println("--------------");
					for (OWLAxiom ex : exp) {
						System.out.println(Parser.renderer.render(ex));
					}
					count++;
					System.out.println();
				}
			} else {
				System.out.println();
				System.out.println(
						"OWLAPI-Lite REASONING ERROR: explanation of class UNsatisfiability is not possible because Class: "
								+ Parser.renderer.render(cls) + " is satisfiable!");
				System.out.println();
			}
		} catch (InconsistentOntologyException ioe) {
			System.out.println();
			System.out.println(
					"OWLAPI-Lite REASONING ERROR: explanation of class unsatisfiability is not possible because <"
							+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">"
							+ " is inconsistent!");
		}
	}

	/**
	 * prints to console output the explanations (justifications) for the
	 * inconsistency of the ontology. Prints 'NOT inconsistent' if the ontology is
	 * consistent
	 */
	public void explainInconsistency() {
		System.out.println();
		reasoner.flush();
		inconsistencyExpFac = new InconsistentOntologyExplanationGeneratorFactory(reasonerFactory, dataFactory, null,
				Long.MAX_VALUE);
		inconsistencyExplanationGenerator = inconsistencyExpFac.createExplanationGenerator(ontology);
		if (!reasoner.isConsistent()) {
			System.out.println("Explanation for inconsistency of <"
					+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString() + ">:");
			for (int i = 0; i < ontology.getOntologyID().getDefaultDocumentIRI().get().toString().length() + 36; i++)
				System.out.print("-");
			System.out.println();
			System.out.println();
			OWLAxiom axiom = dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLThing(), dataFactory.getOWLNothing());
			Set<Explanation<OWLAxiom>> explanations = inconsistencyExplanationGenerator.getExplanations(axiom);
			int count = 1;
			for (Explanation<OWLAxiom> exp : explanations) {
				printExplanation(exp, count);
				count++;
			}
		} else {
			System.out.println();
			System.out.println("OWLAPI-Lite REASONING ERROR: explanation for inconsistency of <"
					+ ontology.getOntologyID().getDefaultDocumentIRI().get().toString()
					+ "> is not possible because it " + " is consistent!");
			System.out.println();
		}
	}

	/**
	 * prints to console output the explanations (justifications) for the entailment
	 * of the axiom (represented by the given string in Manchester OWL Syntax)
	 * 
	 * @param axiomStr a string representation of an OWLAxiom in Manchester OWL
	 *                 Syntax
	 */
	public void explainEntailment(String axiomStr) {
		System.out.println();
		reasoner.flush();
		try {
			explanationGenerator = new DefaultExplanationGenerator(ontology.getOWLOntologyManager(), reasonerFactory,
					ontology, new SilentExplanationProgressMonitor());
			OWLAxiom axiom = parser.createAxiom(axiomStr);
			Set<Set<OWLAxiom>> explanations = explanationGenerator.getExplanations(axiom);

			System.out.println("Explanation for entailment of '" + axiomStr + "':");
			for (int i = 0; i < axiomStr.length() + 31; i++)
				System.out.print("-");
			System.out.println();
			System.out.println();

			int count = 1;
			for (Set<OWLAxiom> exp : explanations) {
				printExplanation(exp, count);
				count++;
			}
		} catch (InconsistentOntologyException ioe) {
			System.out.println();
			System.out.println("OWLAPI-Lite REASONING ERROR: explanation for entailment of " + axiomStr
					+ " is not possible because <" + ontology.getOntologyID().getDefaultDocumentIRI().get().toString()
					+ ">" + " is inconsistent!");
		}
	}

	/**
	 * prints a single explanation with a given integer id
	 * 
	 * @param explanation a set of OWLAxiom objects
	 * @param idx         an integer representing an ID or number for this
	 *                    explanation in a sequence
	 */
	public void printExplanation(Set<OWLAxiom> explanation, int idx) {
		System.out.println("Explanation " + idx);
		System.out.println("--------------");
		for (OWLAxiom axiom : explanation) {
			System.out.println(Parser.renderer.render(axiom));
		}
		System.out.println();
	}

	/**
	 * prints a single explanation with a given integer id
	 * 
	 * @param explanation an Explanation object which contains a set of OWLAxiom
	 *                    objects
	 * @param idx         an integer representing an ID or number for this
	 *                    explanation in a sequence
	 */
	public void printExplanation(Explanation<OWLAxiom> explanation, int idx) {
		System.out.println("Explanation " + idx);
		System.out.println("--------------");
		for (OWLAxiom axiom : explanation.getAxioms()) {
			System.out.println(Parser.renderer.render(axiom));
		}
		System.out.println();
	}
}
