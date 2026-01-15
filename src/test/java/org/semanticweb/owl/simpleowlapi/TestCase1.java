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

public class TestCase1 {
	public static void main(String[] args)
			throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {

		SimpleOWLAPIFactory s = new SimpleOWLAPIFactory();

		s.createOntology("http://ids.maastrichtuniversity.nl/ontologies/dsm-workshop/s12/s12firstontology#");
		s.setFullIRIRendering(true);
		s.setOWLReasoner(SelectedReasoner.HERMIT);
		// -- Task A: answer here --
		s.createClasses("CurdlingProcess ChemicalReaction BioChemicalReaction");

		s.setNamespace("http://schema.org/");
		s.createClasses(
				"Enzyme Protein Catalyst ChemicalCompound Rennet Protease Cheese Nutrient Amylase Starch Ribozyme");
		s.createClasses("BiologicalCell Mammal MammaryGland");

		// -- Task B: answer here --
		s.createObjectProperties(
				"playsARoleIn accelerates occursIn ingredientOf originatesIn hasPart breaksDown breaksDownProtein breaksDownStarch");

		// -- Task C: answer here --
		s.createDataProperties("has_mol_mass description");

		// -- Task D: answer here --
		s.createIndividuals("chymosin casein dolly amylopectin glycogenase");

		s.owlReasoner.isEntailed("Rennet subClassOf Protein");

		// -- Task E: answer here --
		s.createAxiom("CurdlingProcess SubClassOf ChemicalReaction");
		s.createAxiom("Enzyme EquivalentTo Protein and (playsARoleIn some BioChemicalReaction)");
		s.createAxiom("Catalyst EquivalentTo ChemicalCompound and (accelerates some ChemicalReaction)");
		s.createAxiom("BioChemicalReaction EquivalentTo ChemicalReaction and (occursIn some BiologicalCell)");
		s.createAxiom("Protein SubClassOf ChemicalCompound and Nutrient");
		s.createAxiom("Rennet EquivalentTo Protease and (ingredientOf only (Cheese and (originatesIn some Mammal)))");
		s.createAxiom("Rennet SubClassOf playsARoleIn some CurdlingProcess");
		s.createAxiom("Protease or Amylase SubClassOf Enzyme");
		s.createAxiom("Mammal subClassOf hasPart exactly 2 MammaryGland");
		s.createAxiom("Starch SubClassOf Nutrient");
		s.createAxiom("Ribozyme SubClassOf Enzyme and not Protein");

		// s.createAxiom("dolly Type: Mammal and hasPart exactly 3 MammaryGland");

		s.owlReasoner.isEntailed("Rennet subClassOf Protein");
		s.owlReasoner.explainEntailment("Rennet subClassOf Protein");

		// -- Task F: answer here --
		s.createAxiom("breaksDownProtein subPropertyOf: breaksDown");
		s.createAxiom("breaksDownStarch subPropertyOf: breaksDown");
		s.createObjectPropertyAssertion("chymosin breaksDownProtein casein");
		s.createObjectPropertyAssertion("glycogenase breaksDownStarch amylopectin");

		s.owlReasoner.getTypes("casein");
		s.owlReasoner.getTypes("chymosin");
		s.owlReasoner.getTypes("glycogenase");
		s.owlReasoner.getTypes("amylopectin");

		// -- Task G: answer here --
		s.createAxiom("breaksDown Domain: Enzyme");
		s.createAxiom("breaksDown Range: Nutrient");
		s.createAxiom("breaksDownProtein Domain: Protease");
		s.createAxiom("breaksDownProtein Range: Protein");
		s.createAxiom("breaksDownStarch Domain: Amylase");
		s.createAxiom("breaksDownStarch Range: Starch");

		s.printOntology();

		s.owlReasoner.getTypes("casein");
		s.owlReasoner.getTypes("chymosin");
		s.owlReasoner.getTypes("glycogenase");
		s.owlReasoner.getTypes("amylopectin");
		s.owlReasoner.getInstances("breaksDown some (Nutrient and not Protein)");

		s.createAxiom("Protein disjointWith Starch");

		s.owlReasoner.getInstances("breaksDown some (Nutrient and not Protein)");

		// -- Task H: answer here --
		s.createAxiom("chymosin Type: has_mol_mass value \"35600.00\"^^xsd:float");
		s.createAxiom(
				"chymosin Type: description value \"een enzym dat voorkomt in maagsap en ervoor zorgt dat melk stolt\"@nl");

		s.createAxiom("dolly Type: Mammal and hasPart exactly 3 MammaryGland");

		s.owlReasoner.explainInconsistency();

		s.removeAxiom("dolly Type: Mammal and hasPart exactly 3 MammaryGland");

		s.owlReasoner.getSuperClasses("Rennet");
		s.owlReasoner.getSubClasses("Protein");

		s.printOntologyStats();

		s.printOntology();

		s.owlReasoner.explainInconsistency();

		s.owlReasoner.getUnsatisfiableClasses();

		s.owlReasoner.explainUnsatisfiability("Ribozyme");

		s.saveOntology("test-ken3140.owl");

	}
}
