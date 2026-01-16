package io.github.kodymoodley.owlapilite;

import org.junit.jupiter.api.*;
import io.github.kodymoodley.owlapilite.testutils.TestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

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
 * Tests creating entities, reasoning and complex axioms
 * 
 * @author Kody Moodley
 * @author https://kodymoodley.github.io
 * @version 1.0.1
 */

@DisplayName("CreateOntologyEntities Integration Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreateOntologyEntitiesIntegrationTest {
    
    private SimpleOWLAPIFactory factory;
    
    @BeforeEach
    void setUp() {
        factory = SimpleOWLAPIFactory.getInstance();
    }
    
    @AfterEach
    void tearDown() {
        TestUtils.cleanupTestFiles();
    }
    
    @Test
    @DisplayName("Should execute main example without exceptions")
    void mainExample_shouldExecuteWithoutErrors() {
        
        assertThatNoException().isThrownBy(() -> {
            // Create ontology
            factory.createOntology("http://test.com/ontology1#");
            
            // Create entities
            factory.createClasses("Penguin Peacock Bird Robin FlyingOrganism Fish Wing");
            factory.createObjectProperties("hasPart isPartOf hasGender knows eats hunts");
            factory.createIndividuals("tweety woody nemo sharky sheila");
            factory.createDataProperties("hasWeight name bornOn");
            
            // Set property characteristics
            factory.makeTransitive("eats");
            factory.makeSymmetric("knows");
            factory.makeIRReflexive("hasGender");
            factory.makeAntiSymmetric("hasGender");
            
            // Create axioms
            factory.createAxiom("tweety Type: hasWeight value \"300.56\"^^xsd:double");
            factory.createAxiom("tweety Type: Penguin");
            factory.createAxiom("Penguin subClassOf Bird");
            factory.createAxiom("Bird subClassOf FlyingOrganism");
            
            // Switch reasoner
            factory.setOWLReasoner(SelectedReasoner.JFACT);
            
            // Perform reasoning
            factory.owlReasoner.isConsistent();
            factory.owlReasoner.getSuperClasses("Penguin");
            factory.owlReasoner.getTypes("tweety");
        });
    }
    
    @Test
    @DisplayName("Should handle complex ontology operations")
    void complexOperations_shouldWorkCorrectly() {
        assertThatNoException().isThrownBy(() -> {
            // Setup
            factory.createOntology("http://test.com/ontology2#");        

            // Test property chains
            factory.createObjectProperties("hasParent hasAncestor");
            factory.makeTransitive("hasAncestor");
            factory.createAxiom("hasParent subPropertyOf: hasAncestor");
            
            // Test class expressions
            factory.createClasses("Person Parent Student");
            factory.createAxiom("Parent equivalentTo: Person and hasParent some Person");
            factory.createAxiom("Student subClassOf: Person and (not Parent)");
            
            // Test different individuals
            factory.createIndividuals("alice bob charlie");
            factory.differentIndividuals("alice bob charlie");
            
            assertThat(factory.getOntology().getAxiomCount()).isGreaterThan(0);
        });
    }
    
    @Test
    @DisplayName("Should save and load ontology")
    void saveAndLoadOntology_shouldPreserveContent() throws Exception {
        // Arrange
        factory.createOntology("http://test.com/ontology3#");
        factory.createClasses("A B C");
        factory.createAxiom("A subClassOf B");
        factory.createAxiom("B subClassOf C");
        
        // Save
        factory.saveOntology(TestUtils.TEST_FILE_PATH);
        
        // Load in new factory
        java.lang.reflect.Field instanceField = SimpleOWLAPIFactory.class.getDeclaredField("obj");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
        SimpleOWLAPIFactory newFactory = SimpleOWLAPIFactory.getInstance();
        newFactory.loadFromFile(TestUtils.TEST_FILE_PATH);
        
        assertThat(newFactory.getOntology().classesInSignature().count()).isEqualTo(3);
        assertThat(newFactory.getOntology().getAxiomCount()).isEqualTo(5); // 3 declarations + 2 axioms
    }
}