package de.pharmaindex.catalog.atc;

/**
 * WHO-ATC-Kapitel aus der ersten Stelle des Codes.
 * Reicht für Gruppierung, Suche und Dashboard – ohne Lizenzdaten.
 */
public final class AtcClassifier {

    private AtcClassifier() {
    }

    public static String chapterCode(String atc) {
        if (atc == null || atc.isBlank()) {
            return null;
        }
        return atc.substring(0, 1).toUpperCase();
    }

    public static String chapterName(String atc) {
        String code = chapterCode(atc);
        if (code == null) {
            return "Ohne ATC";
        }
        return switch (code) {
            case "A" -> "A · Alimentäres System / Stoffwechsel";
            case "B" -> "B · Blut und blutbildende Organe";
            case "C" -> "C · Kardiovaskuläres System";
            case "D" -> "D · Dermatika";
            case "G" -> "G · Urogenitalsystem";
            case "H" -> "H · Hormone";
            case "J" -> "J · Antiinfektiva";
            case "L" -> "L · Antineoplastika / Immunmodulatoren";
            case "M" -> "M · Muskel- und Skelettsystem";
            case "N" -> "N · Nervensystem";
            case "P" -> "P · Antiparasitika";
            case "R" -> "R · Respirationstrakt";
            case "S" -> "S · Sinnesorgane";
            case "V" -> "V · Varia";
            default -> code + " · Unbekanntes Kapitel";
        };
    }
}
