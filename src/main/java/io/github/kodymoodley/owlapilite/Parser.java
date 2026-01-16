package io.github.kodymoodley.owlapilite;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

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
 * Represents a Parser wrapper class for working with the Manchester OWL syntax.
 * 
 * @author Kody Moodley
 * @author https://kodymoodley.github.io
 * @version 1.0.1
 */
public class Parser {
    /**
     * static reference to the current instance of Parser
     */
    private static Parser obj;
    /**
     * instance of Provider class which implements the chosen way of generating
     * short human-readable labels for the entities in the ontology
     */
    private static final Provider shortFormProvider = new Provider();
    /**
     * instance of OWLEntityChecker which is used by the ManchesterOWLSyntaxParser
     * to parse OWL entities, expressions and axioms from string expressions
     */
    private static final OWLEntityChecker entityChecker = new ShortFormEntityChecker(shortFormProvider);
    /**
     * instance of ManchesterOWLSyntaxParser
     */
    private static ManchesterOWLSyntaxParser parser = OWLManager.createManchesterParser();
    /**
     * a Manchester OWL Syntax renderer instance for rendering OWL entities in short
     * form
     */
    protected static ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();

    /**
     * Parser class constructor
     */
    private Parser() {
        parser.setOWLEntityChecker(entityChecker);
    }

    /**
     * Returns Parser instance.
     * 
     * @return A reference to a Parser instance
     */
    public static synchronized Parser getInstance() {
        if (obj == null) {
            obj = new Parser();
        }
        return obj;
    }

    /**
     * Sets the working string for the parser
     * 
     * @param str A string for the parser to parse
     */
    public void setString(String str) {
        parser.setStringToParse(str);
    }

    /**
     * Gets the core Manchester OWL parser instance
     * 
     * @return a reference to a ManchesterOWLSyntaxParser instance
     */
    public ManchesterOWLSyntaxParser getParser() {
        return parser;
    }

    /**
     * Adds a new OWLEntity instance to the parser i.e., a new class, property or
     * individual name to the parsers vocabulary so that the user can construct
     * OWLAxiom instances using this vocabulary from strings
     * 
     * @param entity A reference to an OWLEntity instance
     */
    public void addVocab(OWLEntity entity) {
        shortFormProvider.add(entity);
    }

    /**
     * Returns an OWLClassExpression constructed from a string representation of the
     * expression in Manchester OWL Syntax
     * 
     * @param classExpressionStr A string to parse into an OWLClassExpression object
     * @return An OWLClassExpression object constructed from the input string
     */
    public OWLClassExpression createClassExpression(String classExpressionStr) {
        parser.setStringToParse(classExpressionStr);
        OWLClassExpression clsEx = parser.parseClassExpression();
        return clsEx;
    }

    /**
     * Returns an OWLAxiom constructed from a string representation of the axiom in
     * Manchester OWL Syntax
     * 
     * @param axiomStr A string to parse into an OWLAxiom object
     * @return An OWLAxiom object constructed from the input string
     */
    public OWLAxiom createAxiom(String axiomStr) {
        parser.setStringToParse(axiomStr);
        OWLAxiom axiom = parser.parseAxiom();
        return axiom;
    }
}
