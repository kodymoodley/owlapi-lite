// File: src/test/java/io/github/kodymoodley/owlapilite/SimpleOWLReasonerTest.java
package io.github.kodymoodley.owlapilite;

import org.junit.jupiter.api.*;

import io.github.kodymoodley.owlapilite.testutils.TestUtils;
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
 * Tests reasoning
 * 
 * @author Kody Moodley
 * @author https://kodymoodley.github.io
 * @version 1.0.1
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimpleOWLReasonerTest {
    
    private SimpleOWLAPIFactory factory;
    private SimpleOWLReasoner reasoner;
    
    private void createTestOntology(String iriStr) throws Exception {
        factory.createOntology(iriStr);
        factory.createClasses("Animal Mammal Cat Dog Contradiction Paradox");
        factory.createIndividuals("felix max");
        factory.createAxiom("Cat subClassOf Animal");
        factory.createAxiom("Dog subClassOf Animal");
        factory.createAxiom("Mammal subClassOf Animal");
        factory.createAxiom("Cat subClassOf Mammal");
        factory.createAxiom("Dog subClassOf Mammal");
        factory.createAxiom("Cat disjointWith Dog");
        factory.createAxiom("felix Type: Cat");
        factory.createAxiom("max Type: Dog");
        reasoner = factory.owlReasoner;
    }

    @BeforeEach
    void setUp() throws Exception {
        factory = SimpleOWLAPIFactory.getInstance();
    }
    
    @Test
    @DisplayName("Should check ontology consistency")
    void isConsistent_withConsistentOntology_shouldReturnYes() throws Exception {
        createTestOntology("http://test.com/reasonerstest1#");

        // Act
        String output = TestUtils.captureSystemOut(() -> reasoner.isConsistent());
        
        // Assert
        assertThat(output).contains("Yes").contains("consistent");
    }
    
    @Test
    @DisplayName("Should detect inconsistent ontology")
    void isConsistent_withInconsistentOntology_shouldReturnNo() throws Exception {
        createTestOntology("http://test.com/reasonerstest2#");
        // Arrange - Create inconsistency
        factory.createAxiom("Cat subClassOf (not Animal)");
        reasoner.isConsistent();
        
        // Act
        String output = TestUtils.captureSystemOut(() -> reasoner.isConsistent());
        
        // Assert
        assertThat(output).contains("No").contains("INconsistent");
    }
    
    @Test
    @DisplayName("Should get superclasses of a class")
    void getSuperClasses_withValidClass_shouldReturnHierarchy() throws Exception {
        createTestOntology("http://test.com/reasonerstest3#");
        // Act
        String output = TestUtils.captureSystemOut(() -> 
            reasoner.getSuperClasses("Cat"));
        
        // Assert
        assertThat(output)
            .contains("All superclasses")
            .contains("Mammal")
            .contains("Animal");
    }
    
    @Test
    @DisplayName("Should get subclasses of a class")
    void getSubClasses_withValidClass_shouldReturnHierarchy() throws Exception {
        createTestOntology("http://test.com/reasonerstest4#");
        // Act
        String output = TestUtils.captureSystemOut(() -> 
            reasoner.getSubClasses("Animal"));
        
        // Assert
        assertThat(output)
            .contains("All subclasses")
            .contains("Mammal")
            .contains("Cat")
            .contains("Dog");
    }
    
    @Test
    @DisplayName("Should check class satisfiability")
    void isSatisfiable_withSatisfiableClass_shouldReturnYes() throws Exception {
        createTestOntology("http://test.com/reasonerstest5#");
        // Act
        String output = TestUtils.captureSystemOut(() -> 
            reasoner.isSatisfiable("Cat"));
        
        // Assert
        assertThat(output).contains("Yes").contains("satisfiable");
    }
    
    @Test
    @DisplayName("Should detect unsatisfiable class")
    void isSatisfiable_withUnsatisfiableClass_shouldReturnNo() throws Exception{
        createTestOntology("http://test.com/reasonerstest6#");
        // Arrange
        factory.createAxiom("Contradiction equivalentTo: Cat and not Cat");
        
        // Act
        String output = TestUtils.captureSystemOut(() -> 
            reasoner.isSatisfiable("Contradiction"));
        
        // Assert
        assertThat(output).contains("No").contains("UNsatisfiable");
    }
    
    @Test
    @DisplayName("Should check axiom entailment")
    void isEntailed_withEntailedAxiom_shouldReturnYes() throws Exception {
        createTestOntology("http://test.com/reasonerstest7#");
        // Act
        String output = TestUtils.captureSystemOut(() -> 
            reasoner.isEntailed("Cat subClassOf Animal"));
        
        // Assert
        assertThat(output).contains("Yes").contains("entailed");
    }
    
    @Test
    @DisplayName("Should detect non-entailed axiom")
    void isEntailed_withNonEntailedAxiom_shouldReturnNo() throws Exception {
        createTestOntology("http://test.com/reasonerstest8#");
        // Act
        String output = TestUtils.captureSystemOut(() -> 
            reasoner.isEntailed("Cat subClassOf Dog"));
        
        // Assert
        assertThat(output).contains("No").contains("not entailed");
    }
    
    @Test
    @DisplayName("Should get types of individual")
    void getTypes_withValidIndividual_shouldReturnTypes() throws Exception {
        createTestOntology("http://test.com/reasonerstest9#");
        // Act
        String output = TestUtils.captureSystemOut(() -> 
            reasoner.getTypes("felix"));
        
        // Assert
        assertThat(output)
            .contains("Types for individual")
            .contains("Cat")
            .contains("Mammal")
            .contains("Animal");
    }
    
    @Test
    @DisplayName("Should get all types for all individuals")
    void getAllTypes_shouldReturnAllIndividualTypes() throws Exception {
        createTestOntology("http://test.com/reasonerstest10#");
        // Act
        String output = TestUtils.captureSystemOut(() -> 
            reasoner.getAllTypes());
        
        // Assert
        assertThat(output)
            .contains("All Types")
            .contains("felix")
            .contains("max");
    }
    
    @Test
    @DisplayName("Should get instances of a class")
    void getInstances_withValidClass_shouldReturnIndividuals() throws Exception {
        createTestOntology("http://test.com/reasonerstest12#");
        // Act
        String output = TestUtils.captureSystemOut(() -> 
            reasoner.getInstances("Cat"));
        
        // Assert
        assertThat(output)
            .contains("Individuals of")
            .contains("felix");
    }
    
    @Test
    @DisplayName("Should explain unsatisfiability")
    void explainUnsatisfiability_withUnsatisfiableClass_shouldProvideExplanation() throws Exception {
        // Arrange
        createTestOntology("http://test.com/reasonerstest13#");
        factory.createAxiom("Paradox equivalentTo: Cat and not Cat");
        
        // Act
        String output = TestUtils.captureSystemOut(() -> 
            reasoner.explainUnsatisfiability("Paradox"));
        
        // Assert
        assertThat(output)
            .contains("Explanation for unsatisfiability")
            .contains("Paradox");
    }
    
    @Test
    @DisplayName("Should explain inconsistency")
    void explainInconsistency_withInconsistentOntology_shouldProvideExplanation() throws Exception {
        // Arrange
        createTestOntology("http://test.com/reasonerstest14#");
        factory.createAxiom("Cat subClassOf not Animal");
        
        // First check consistency to ensure reasoner is initialized
        System.err.print("DECISION: " + reasoner.isConsistent());

        // Act
        String output = TestUtils.captureSystemOut(() -> 
            reasoner.explainInconsistency());

        System.err.print("MESSAGE: " + output);
        
        // Assert
        assertThat(output).contains("Explanation for inconsistency");
    }
    
    @Test
    @DisplayName("Should get reasoner name")
    void getName_shouldReturnReasonerName() throws Exception {
        // Act
        String output = TestUtils.captureSystemOut(() -> 
            reasoner.getName());
        
        // Assert
        assertThat(output.strip()).isIn("ELK", "JFACT", "HERMIT");
    }
    
    @Test
    @DisplayName("Should get OWL profile")
    void getOWLProfile_shouldReturnProfile() {
        // Act
        String output = TestUtils.captureSystemOut(() -> 
            reasoner.getOWLProfile());
        
        // Assert
        assertThat(output.strip()).isIn("OWL 2 EL", "OWL 2 DL");
    }
}