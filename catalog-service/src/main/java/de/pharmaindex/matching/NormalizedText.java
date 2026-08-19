package de.pharmaindex.matching;

import java.text.Normalizer;
import java.util.Locale;

public final class NormalizedText {

    private NormalizedText() {
    }

    public static String of(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String value = raw.toLowerCase(Locale.GERMAN)
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss");
        value = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        value = value.replaceAll("[^a-z0-9]+", " ");
        return value.trim().replaceAll("\\s+", " ");
    }
}
