package io.github.kodymoodley.owlapilite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;

import static org.assertj.core.api.Assertions.*;

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
 * Tests parsing class and axiom expressions from string 
 * using Manchester syntax
 * 
 * @author Kody Moodley
 * @author https://kodymoodley.github.io
 * @version 1.0.1
 */

class ParserTest {

    private Parser parser;
    private SimpleOWLAPIFactory factory;

    @BeforeEach
    void setUp() throws Exception {
        parser = Parser.getInstance();
        factory = SimpleOWLAPIFactory.getInstance();
    }

    @Test
    void createClassExpression_withValidExpression_shouldParse() throws Exception {
        // Factory
        factory.createOntology("http://test.com/classparsertest#");
        // Classes
        factory.createClasses("Car Engine Piston Steel Wheel Battery LowCharge Alloy Composite");
        // Object Properties
        factory.createObjectProperties("hasPart partOf hasCharge hasEngine hasMaterial madeOf");
        // Data Properties
        factory.createDataProperties("hasDiameter hasVoltage");

        // Part to test
        OWLClassExpression expr = parser.createClassExpression(
                "(hasPart some (Car and (hasEngine some Engine))) and (hasPart some (Piston and (madeOf some Steel))) or (partOf some (Wheel and (hasDiameter value 17))) and (hasMaterial some (Alloy or Composite)) and (not (hasPart some (Battery and (hasCharge some LowCharge)))) and (hasVoltage only xsd:double[< 11.0])");

        // Assert
        assertThat(expr).isNotNull();
    }

    @Test
    void createAxiom_withValidAxiom_shouldParse() throws Exception {
        // Factory
        factory.createOntology("http://test.com/axiomparsertest#");
        // Classes
        factory.createClasses("Car Engine Piston Steel Wheel Battery LowCharge Alloy Composite A B");
        // Object Properties
        factory.createObjectProperties("hasPart partOf hasCharge hasEngine hasMaterial madeOf");
        // Data Properties
        factory.createDataProperties("hasDiameter hasVoltage");
        
        // Part to test
        OWLAxiom axiom = parser.createAxiom("(hasPart some (Car and (hasEngine some Engine))) subClassOf (B and not A)");

        // Assert
        assertThat(axiom).isNotNull();
    }
}