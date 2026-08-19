package de.pharmaindex.matching;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NormalizedTextTest {

    @Test
    void mapsGermanUmlautsAndPunctuation() {
        assertThat(NormalizedText.of("Ibuprofen-ratiopharm 400 mg")).isEqualTo("ibuprofen ratiopharm 400 mg");
        assertThat(NormalizedText.of("Wärme-Pflaster")).isEqualTo("waerme pflaster");
        assertThat(NormalizedText.of("Süßholz")).isEqualTo("suessholz");
    }
}

class LevenshteinTest {

    @Test
    void scoresTypoCloseToOriginal() {
        assertThat(Levenshtein.similarity("paracetamol", "paracetmol")).isGreaterThan(0.8);
        assertThat(Levenshtein.distance("ibuprofen", "ibuprofen")).isZero();
    }
}

class TrigramIndexTest {

    @Test
    void findsCandidateByPartialOverlap() {
        TrigramIndex index = new TrigramIndex();
        index.put(1L, "ibuprofen abz 400 mg filmtabletten");
        index.put(2L, "amoxicillin ratiopharm 1000 mg");

        assertThat(index.candidateIds("ibuflam 400")).contains(1L);
        assertThat(index.dice(1L, "ibuprofen 400")).isGreaterThan(index.dice(2L, "ibuprofen 400"));
    }
}
