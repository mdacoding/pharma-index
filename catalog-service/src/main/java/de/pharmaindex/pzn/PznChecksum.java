package de.pharmaindex.pzn;

/**
 * Prüfziffer der deutschen Pharmazentralnummer (PZN-8).
 *
 * <p>Die ersten sieben Ziffern werden mit den Gewichten 2..8 multipliziert.
 * Die Prüfziffer ist die Summe modulo 11. Rest 10 ist unzulässig.
 */
public final class PznChecksum {

    private PznChecksum() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() > 8) {
            digits = digits.substring(digits.length() - 8);
        }
        return digits.length() < 8 ? "0".repeat(8 - digits.length()) + digits : digits;
    }

    public static boolean isValid(String pzn) {
        String normalized = normalize(pzn);
        if (!normalized.matches("\\d{8}")) {
            return false;
        }
        int expected = checkDigit(normalized.substring(0, 7));
        return expected >= 0 && expected == Character.getNumericValue(normalized.charAt(7));
    }

    /**
     * @return Prüfziffer 0-9 oder -1, falls Rest 10 (unzulässige PZN)
     */
    public static int checkDigit(String sevenDigits) {
        if (sevenDigits == null || !sevenDigits.matches("\\d{7}")) {
            throw new IllegalArgumentException("PZN-Stamm muss genau 7 Ziffern haben");
        }
        int sum = 0;
        for (int i = 0; i < 7; i++) {
            int digit = Character.getNumericValue(sevenDigits.charAt(i));
            sum += digit * (i + 2);
        }
        int remainder = sum % 11;
        return remainder == 10 ? -1 : remainder;
    }

    public static String withCheckDigit(String sevenDigits) {
        int digit = checkDigit(sevenDigits);
        if (digit < 0) {
            throw new IllegalArgumentException("Unzulässiger PZN-Stamm (Rest 10): " + sevenDigits);
        }
        return sevenDigits + digit;
    }
}
