package io.github.kodymoodley.owlapilite.testutils;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.apibinding.OWLManager;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;

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
 * Test Utilities class for OWLAPI-Lite.
 * 
 * @author Kody Moodley
 * @author https://kodymoodley.github.io
 * @version 1.0.1
 */

public class TestUtils {
    
    public static final String TEST_ONTOLOGY_IRI = "http://test.ontology#";
    public static final String TEST_FILE_PATH = "test-ontology.owl";
    
    public static OWLOntology createTestOntology() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        return manager.createOntology(IRI.create(TEST_ONTOLOGY_IRI));
    }
    
    public static void cleanupTestFiles() {
        File file = new File(TEST_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }
    
    public static String captureSystemOut(Runnable code) {
        PrintStream originalOut = System.out;
        try {
            OutputStream os = new java.io.ByteArrayOutputStream();
            PrintStream ps = new PrintStream(os);
            System.setOut(ps);
            code.run();
            return os.toString();
        } finally {
            System.setOut(originalOut);
        }
    }
}
