package io.github.kodymoodley.owlapilite;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.CachingBidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

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
 * Represents a Provider class providing a default implementation for getting a
 * short form, human-readable label for an entity with an IRI from an ontology.
 * 
 * @author Kody Moodley
 * @author https://kodymoodley.github.io
 * @version 1.0.1
 */
public class Provider extends CachingBidirectionalShortFormProvider {

    /**
     * Creates a new {@code MyClass}.
     */
    public Provider(){}

    /**
     * SimpleShortFormProvider instance to return the resource name (after the last
     * hash or slash in the IRI for the input entity).
     */
    private SimpleShortFormProvider provider = new SimpleShortFormProvider();

    /**
     * Gets the human-readable label from the OWLEntity
     * 
     * @param entity An OWLEntity object for which to generate the human-readable
     *               label
     * @return A string representation of the human-readable label.
     */
    protected String generateShortForm(OWLEntity entity) {
        return provider.getShortForm(entity);
    }
}