package main;

import edu.princeton.cs.algs4.In;

import java.util.*;

public class WordNet {
    private final Map<String, Set<Integer>> WordtoIds;
    private final Graph idGraph;
    private final Map<Integer,Set<String>> idToWords;

    public WordNet(String synsetsFilename,String hyponymsFilename ) {
        WordtoIds = new HashMap<>();
        this.idGraph = new Graph();
        this.idToWords = new HashMap<>();

        In synsetsInput = new In(synsetsFilename);
        while(synsetsInput.hasNextLine()){
            String line = synsetsInput.readLine();
            String[] parts = line.split(",",3);
            int id = Integer.parseInt(parts[0]);
            String[] words = parts[1].split(" ");

            Set<String> wordsInNode = new HashSet<>();
            for (String word : words) {
                wordsInNode.add(word);
                if (!WordtoIds.containsKey(word)) {
                    WordtoIds.put(word, new HashSet<>());
                }
                WordtoIds.get(word).add(id);


            }
            idToWords.put(id, wordsInNode);


        }
        synsetsInput.close();


        In hyponymsInput = new In(hyponymsFilename);
        while (hyponymsInput.hasNextLine()) {
            String line = hyponymsInput.readLine();
            String[] parts = line.split(",");

            int from = Integer.parseInt(parts[0]);

            for (int i = 1; i < parts.length; i++) {
                int to = Integer.parseInt(parts[i]);
                idGraph.addEdge(from, to);
            }
        }

        hyponymsInput.close();

    }

    public Set<String> hyponyms(String Word){
        Set<String> res = new TreeSet<>();
        if(!WordtoIds.containsKey(Word)){
            return res;
        }
        for(int start: WordtoIds.get(Word)){
            Set<Integer> reachableIds = idGraph.reachable(start);

            for(int id : reachableIds){
                res.addAll(idToWords.get(id));
            }
        }
        return res;
    }
    public Set<String> hyponyms(List<String> words) {
        Set<String> result = new TreeSet<>();

        if (words.isEmpty()) {
            return result;
        }

        result.addAll(hyponyms(words.get(0)));

        for (int i = 1; i < words.size(); i++) {
            result.retainAll(hyponyms(words.get(i)));
        }

        return result;
    }

}
