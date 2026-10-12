package main;

import browser.NgordnetQuery;
import browser.NgordnetQueryHandler;
import ngrams.NGramMap;

public class HistoryTextHandler extends NgordnetQueryHandler {
    private final NGramMap map;

    public HistoryTextHandler(NGramMap map) {
        this.map = map;
    }

    @Override
    public String handle(NgordnetQuery q) {
        StringBuilder response = new StringBuilder();
        for (String word : q.words()) {
            response.append(word).append(": ")
                    .append(map.weightHistory(word, q.startYear(), q.endYear()))
                    .append("\n");
        }
        return response.toString();
    }
}
