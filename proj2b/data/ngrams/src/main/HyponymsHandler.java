package main;
import ngrams.NGramMap;
import browser.NgordnetQuery;
import browser.NgordnetQueryHandler;

import java.util.*;

public class HyponymsHandler extends NgordnetQueryHandler {
    private final WordNet wordNet;
    private final NGramMap ngm;

    public HyponymsHandler(WordNet w,NGramMap n) {
        wordNet = w;
        ngm = n;
    }
    @Override
    public String handle(NgordnetQuery q) {
        Set<String> candidates = wordNet.hyponyms(q.words());

        if (q.k() == 0) {
            return new TreeSet<>(candidates).toString();
        }
        Map<String, Double> counts = new HashMap<>();

        for (String word : candidates) {
            double total = 0.0;

            for (double count :
                    ngm.countHistory(word, q.startYear(), q.endYear()).values()) {
                total += count;
            }

            if (total > 0) {
                counts.put(word, total);
            }
        }

        List<String> ranked = new ArrayList<>(counts.keySet());
        ranked.sort((a, b) -> {
            int comparison = Double.compare(counts.get(b), counts.get(a));
            if (comparison != 0) {
                return comparison;
            }
            return a.compareTo(b);
        });
        Set<String> result = new TreeSet<>();
        for (int i = 0; i < Math.min(q.k(), ranked.size()); i++) {
            result.add(ranked.get(i));
        }

        return result.toString();
    }
}
