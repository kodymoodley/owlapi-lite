package io.github.kodymoodley.owlapilite;

import org.junit.jupiter.api.*;
import org.semanticweb.owlapi.model.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import io.github.kodymoodley.owlapilite.testutils.TestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

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
 * Tests each ontology editing operation separately
 * 
 * @author Kody Moodley
 * @author https://kodymoodley.github.io
 * @version 1.0.1
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimpleOWLAPIFactoryTest {
    
    private SimpleOWLAPIFactory factory;
    private PrintStream originalOut;
    private ByteArrayOutputStream testOutput;
    
    @BeforeEach
    void setUp() throws Exception {
        resetSingleton();
        factory = SimpleOWLAPIFactory.getInstance();
        
        // Capture System.out for testing console output
        originalOut = System.out;
        testOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOutput));
    }
    
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        TestUtils.cleanupTestFiles();
    }
    
    private void resetSingleton() throws Exception {
        java.lang.reflect.Field instanceField = SimpleOWLAPIFactory.class.getDeclaredField("obj");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
    
    @Test
    @DisplayName("Factory creates singleton instance")
    void getInstance_createsSingleton() {
        SimpleOWLAPIFactory instance1 = SimpleOWLAPIFactory.getInstance();
        SimpleOWLAPIFactory instance2 = SimpleOWLAPIFactory.getInstance();
        
        assertThat(instance1).isSameAs(instance2);
    }
    
    @Test
    @DisplayName("Create ontology with valid IRI")
    void createOntology_withValidIRI_createsOntology() throws Exception {
        OWLOntology ontology = factory.createOntology("http://test.com/ontology#");
        
        assertThat(ontology).isNotNull();
        assertThat(ontology.getOntologyID().getOntologyIRI())
            .contains(IRI.create("http://test.com/ontology#"));
    }
    
    @Test
    @DisplayName("Create multiple classes successfully")
    void createClasses_addsClassesToOntology() throws Exception {
        // Need an ontology first
        factory.createOntology("http://test.com/classes#");
        
        factory.createClasses("Person Student Teacher");
        
        String output = testOutput.toString();
        assertThat(output)
            .contains("Class: Person")
            .contains("Class: Student")
            .contains("Class: Teacher");
    }
    
    @Test
    @DisplayName("Create object property with characteristics")
    void createObjectProperty_withTransitive_makesTransitive() throws Exception {
        factory.createOntology("http://test.com/props#");
        
        factory.createObjectProperty("hasPart", 1, 0, 0); // transitive
        
        String output = testOutput.toString();
        assertThat(output).contains("ObjectProperty: hasPart");
    }
    
    @Test
    @DisplayName("Create axiom from Manchester syntax")
    void createAxiom_withValidSyntax_addsToOntology() throws Exception {
        factory.createOntology("http://test.com/axioms#");
        factory.createClasses("Cat Animal");
        
        OWLAxiom axiom = factory.createAxiom("Cat subClassOf Animal");
        
        assertThat(axiom).isNotNull();
        assertThat(factory.getOntology().getAxiomCount()).isEqualTo(3);
    }
    
    @Test
    @DisplayName("Handle invalid axiom gracefully")
    void createAxiom_withInvalidSyntax_handlesGracefully() throws Exception {
        factory.createOntology("http://test.com/errors#");
        
        OWLAxiom axiom = factory.createAxiom("Invalid $#@! Syntax");
        
        assertThat(axiom).isNull();
        String output = testOutput.toString();
        assertThat(output).contains("PARSER ERROR");
    }
    
    @Test
    @DisplayName("Save and load ontology preserves content")
    void saveAndLoad_preservesOntology() throws Exception {
        // Create and save
        factory.createOntology("http://test.com/saveload#");
        factory.createClasses("A B");
        factory.createAxiom("A subClassOf B");
        factory.saveOntology("test-save.owl");

        // Reset and load
        resetSingleton();
        SimpleOWLAPIFactory newFactory = SimpleOWLAPIFactory.getInstance();
        newFactory.loadFromFile("test-save.owl");

        assertThat(newFactory.getOntology().classesInSignature().count()).isEqualTo(2);
        assertThat(newFactory.getOntology().getAxiomCount()).isEqualTo(3);
    }
    
    @Test
    @DisplayName("Set different reasoners successfully")
    void setOWLReasoner_changesReasoner() {
        assertDoesNotThrow(() -> {
            factory.setOWLReasoner(SelectedReasoner.JFACT);
            factory.setOWLReasoner(SelectedReasoner.ELK);
            factory.setOWLReasoner(SelectedReasoner.HERMIT);
        });
    }
    
    @Test
    @DisplayName("Print ontology statistics")
    void printOntologyStats_outputsMetrics() throws Exception {
        factory.createOntology("http://test.com/teststats#");
        factory.createClasses("X Y Z");
        
        factory.printOntologyStats();
        
        String output = testOutput.toString();
        assertThat(output)
            .contains("Number of classes: 3")
            .contains("Number of axioms");
    }
}