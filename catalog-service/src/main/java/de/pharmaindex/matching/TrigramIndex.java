package de.pharmaindex.matching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Invertiertes Trigramm-Register für Kandidatensuche in O(k) statt Full-Scan.
 */
public final class TrigramIndex {

    private final Map<String, List<Long>> inverted = new HashMap<>();
    private final Map<Long, Set<String>> documentTrigrams = new HashMap<>();

    public void put(long id, String normalizedText) {
        remove(id);
        Set<String> grams = trigrams(normalizedText);
        documentTrigrams.put(id, grams);
        for (String gram : grams) {
            inverted.computeIfAbsent(gram, key -> new ArrayList<>()).add(id);
        }
    }

    public void remove(long id) {
        Set<String> previous = documentTrigrams.remove(id);
        if (previous == null) {
            return;
        }
        for (String gram : previous) {
            List<Long> posting = inverted.get(gram);
            if (posting != null) {
                posting.remove(id);
                if (posting.isEmpty()) {
                    inverted.remove(gram);
                }
            }
        }
    }

    public Set<Long> candidateIds(String normalizedQuery) {
        Set<String> grams = trigrams(normalizedQuery);
        Map<Long, Integer> hits = new HashMap<>();
        for (String gram : grams) {
            List<Long> posting = inverted.get(gram);
            if (posting == null) {
                continue;
            }
            for (Long id : posting) {
                hits.merge(id, 1, Integer::sum);
            }
        }
        Set<Long> ids = new HashSet<>();
        int minHits = Math.max(1, grams.size() / 4);
        hits.forEach((id, count) -> {
            if (count >= minHits) {
                ids.add(id);
            }
        });
        return ids;
    }

    public int size() {
        return documentTrigrams.size();
    }

    public double dice(long id, String normalizedQuery) {
        Set<String> document = documentTrigrams.getOrDefault(id, Set.of());
        Set<String> query = trigrams(normalizedQuery);
        if (document.isEmpty() || query.isEmpty()) {
            return 0.0;
        }
        int intersection = 0;
        for (String gram : query) {
            if (document.contains(gram)) {
                intersection++;
            }
        }
        return (2.0 * intersection) / (document.size() + query.size());
    }

    static Set<String> trigrams(String normalized) {
        String padded = "  " + normalized + " ";
        Set<String> grams = new HashSet<>();
        for (int i = 0; i <= padded.length() - 3; i++) {
            grams.add(padded.substring(i, i + 3));
        }
        return grams;
    }
}
