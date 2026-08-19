package de.pharmaindex.catalog.atc;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AtcClassifierTest {

    @Test
    void mapsNervousSystemAndMissingCode() {
        assertThat(AtcClassifier.chapterCode("N02BE01")).isEqualTo("N");
        assertThat(AtcClassifier.chapterName("N02BE01")).contains("Nervensystem");
        assertThat(AtcClassifier.chapterName(null)).isEqualTo("Ohne ATC");
    }
}
