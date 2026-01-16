package io.github.kodymoodley.owlapilite;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.Stream;

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
 * Tests SelectedReasoner enumeration class
 * 
 * @author Kody Moodley
 * @author https://kodymoodley.github.io
 * @version 1.0.1
 */

public class SelectedReasonerTest {
    
    private static Stream<SelectedReasoner> reasonerProvider() {
        return SelectedReasoner.REASONERS.stream();
    }

    @ParameterizedTest
    @MethodSource("reasonerProvider")
    @DisplayName("Should have all required reasoners available")
    void reasoners_shouldHaveRequiredProperties(SelectedReasoner reasoner) {
        assertThat(reasoner.getName()).isNotBlank();
        assertThat(reasoner.getProfile()).isNotBlank();
        assertThat(reasoner.toString()).contains(reasoner.getName());
    }
    
    @Test
    @DisplayName("Should contain all expected reasoners")
    void REASONERS_shouldContainAllSupported() {
        assertThat(SelectedReasoner.REASONERS)
            .hasSize(3)
            .containsExactlyInAnyOrder(
                SelectedReasoner.JFACT,
                SelectedReasoner.ELK,
                SelectedReasoner.HERMIT
            );
    }
}