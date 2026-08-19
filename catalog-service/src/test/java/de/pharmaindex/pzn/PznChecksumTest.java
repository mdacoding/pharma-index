package de.pharmaindex.pzn;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PznChecksumTest {

    @Test
    void assignsAndValidatesKnownStem() {
        String pzn = PznChecksum.withCheckDigit("9900001");
        assertThat(pzn).hasSize(8);
        assertThat(PznChecksum.isValid(pzn)).isTrue();
        assertThat(pzn.charAt(7)).isEqualTo('9');
    }

    @Test
    void rejectsTamperedCheckDigit() {
        String pzn = PznChecksum.withCheckDigit("9900001");
        String broken = pzn.substring(0, 7) + ((pzn.charAt(7) == '0') ? '1' : '0');
        assertThat(PznChecksum.isValid(broken)).isFalse();
    }

    @Test
    void rejectsRemainderTen() {
        assertThat(PznChecksum.checkDigit("9900008")).isEqualTo(-1);
        assertThatThrownBy(() -> PznChecksum.withCheckDigit("9900008"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void padsShorterNumbers() {
        assertThat(PznChecksum.normalize("123")).isEqualTo("00000123");
    }
}
