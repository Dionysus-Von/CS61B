package main;

import java.util.*;

public class Graph {
    private Map<Integer, Set<Integer>> edges;
    public Graph(){
        edges = new HashMap<>();
    }

    public void addEdge(int from,int to){
        if(!edges.containsKey(from)){
            edges.put(from,new HashSet<>());
        }
        edges.get(from).add(to);
    }

    public Set<Integer> reachable(int start){
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> f = new ArrayDeque<>();
        visited.add(start);
        f.add(start);

        while(!f.isEmpty()){
            int curr = f.remove();
            if(!edges.containsKey(curr)){
                continue;
            }
            for(int neighbor:edges.get(curr)){
                if(visited.add(neighbor)){
                    f.add(neighbor);
                }
            }
        }
        return visited;
    }
}
