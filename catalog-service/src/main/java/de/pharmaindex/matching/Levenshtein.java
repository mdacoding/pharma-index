package de.pharmaindex.matching;

public final class Levenshtein {

    private Levenshtein() {
    }

    public static int distance(String left, String right) {
        if (left.isEmpty()) {
            return right.length();
        }
        if (right.isEmpty()) {
            return left.length();
        }
        int[] previous = new int[right.length() + 1];
        int[] current = new int[right.length() + 1];
        for (int j = 0; j <= right.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= left.length(); i++) {
            current[0] = i;
            char leftChar = left.charAt(i - 1);
            for (int j = 1; j <= right.length(); j++) {
                int cost = leftChar == right.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(
                        Math.min(current[j - 1] + 1, previous[j] + 1),
                        previous[j - 1] + cost
                );
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[right.length()];
    }

    public static double similarity(String left, String right) {
        if (left.isEmpty() && right.isEmpty()) {
            return 1.0;
        }
        int max = Math.max(left.length(), right.length());
        return 1.0 - ((double) distance(left, right) / max);
    }
}
