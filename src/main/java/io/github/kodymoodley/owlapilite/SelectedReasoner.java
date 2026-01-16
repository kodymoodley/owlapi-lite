package io.github.kodymoodley.owlapilite;

import java.io.Serializable;
import java.util.ArrayList;

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
 * Represents a SelectedReasoner class that catalogues / enumerates metadata
 * about OWL 2 reasoners which OWLAPI-Lite is able to interact with. The client
 * can use instances of this class to indicate their selected reasoner.
 * 
 * @author Kody Moodley
 * @author https://kodymoodley.github.io
 * @version 1.0.1
 */
public class SelectedReasoner implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    /**
     * JFACT OWL 2 DL Reasoner selection instance
     */
    public static final SelectedReasoner JFACT = getInstance("JFACT", "OWL 2 DL");
    /**
     * ELK OWL 2 EL Reasoner selection instance
     */
    public static final SelectedReasoner ELK = getInstance("ELK", "OWL 2 EL");
    /**
     * HermiT OWL 2 DL Reasoner selection instance
     */
    public static final SelectedReasoner HERMIT = getInstance("HERMIT", "OWL 2 DL");
    /**
     * ArrayList of all OWL 2 reasoner selection instances which simpleOWLAPI is
     * able to use
     */
    public static final ArrayList<SelectedReasoner> REASONERS = new ArrayList<SelectedReasoner>();

    /**
     * String representation of the name for the SelectedReasoner instance
     */
    private final String name;
    /**
     * String representation of the OWL 2 profile which the SelectedReasoner
     * instance supports
     */
    private final String profile;

    /**
     * SelectedReasoner constructor
     * 
     * @param name    String representation of the name for the SelectedReasoner
     *                instance
     * @param profile String representation of the OWL 2 profile which the
     *                SelectedReasoner instance supports
     */
    private SelectedReasoner(String name, String profile) {
        this.name = name;
        this.profile = profile;
    }

    /**
     * This is a static initialization block for collecting a list of all supported
     * reasoners by simpleOWLAPI
     */
    static {
        REASONERS.add(JFACT);
        REASONERS.add(ELK);
        REASONERS.add(HERMIT);
    }

    /**
     * static method to generate and return instances of SelectedReasoner
     * 
     * @param name    String representation of the name for the SelectedReasoner
     *                instance
     * @param profile String representation of the OWL 2 profile which the
     *                SelectedReasoner instance supports
     * @return A SelectedReasoner instance with the input name and input supported
     *         profile
     */
    private static final SelectedReasoner getInstance(String name, String profile) {
        return new SelectedReasoner(name, profile);
    }

    /**
     * returns a string representation of the SelectedReasoner instance
     * 
     * @return The string representation of the name of the reasoner concatenated
     *         with the OWL 2 Profile it supports
     */
    public String toString() {
        return name + " - " + profile;
    }

    /**
     * returns a string representation of the SelectedReasoner instance name
     * 
     * @return The string representation of the name of the selected reasoner
     *         instance
     */
    public String getName() {
        return name;
    }

    /**
     * returns a string representation of the OWL 2 Profile supported by the
     * SelectedReasoner instance
     * 
     * @return The string representation of the OWL 2 Profile which the selected
     *         reasoner supports
     */
    public String getOWLProfile() {
        return profile;
    }

    /**
     * Alias for getOWLProfile method. returns a string representation of the OWL 2 Profile supported by the
     * SelectedReasoner instance
     * 
     * @return The string representation of the OWL 2 Profile which the selected
     *         reasoner supports
     */
    public String getProfile() {
        return profile;
    }
}
