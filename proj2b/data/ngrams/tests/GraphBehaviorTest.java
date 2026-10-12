import main.Graph;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class GraphBehaviorTest {

    @Test
    public void testMultipleChildrenAndMultipleLevels() {
        Graph graph = new Graph();
        graph.addEdge(2, 5);
        graph.addEdge(2, 7);
        graph.addEdge(5, 8);

        assertThat(graph.reachable(2))
                .containsExactly(2, 5, 7, 8);
    }

    @Test
    public void testEdgesAreDirected() {
        Graph graph = new Graph();
        graph.addEdge(2, 5);

        assertThat(graph.reachable(5)).containsExactly(5);
        assertThat(graph.reachable(2)).containsExactly(2, 5);
    }

    @Test
    public void testDuplicateEdgesCyclesAndSelfLoop() {
        Graph graph = new Graph();
        graph.addEdge(2, 5);
        graph.addEdge(2, 5);
        graph.addEdge(5, 8);
        graph.addEdge(8, 2);
        graph.addEdge(5, 5);

        assertThat(graph.reachable(2)).containsExactly(2, 5, 8);
    }

    @Test
    public void testSharedDescendant() {
        Graph graph = new Graph();
        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 3);
        graph.addEdge(2, 3);

        assertThat(graph.reachable(0)).containsExactly(0, 1, 2, 3);
    }

    @Test
    public void testQueriesAndInstancesAreIndependent() {
        Graph first = new Graph();
        first.addEdge(1, 2);
        first.addEdge(10, 20);

        assertThat(first.reachable(1)).containsExactly(1, 2);
        assertThat(first.reachable(10)).containsExactly(10, 20);
        assertThat(first.reachable(1)).containsExactly(1, 2);

        Graph second = new Graph();
        second.addEdge(1, 99);

        assertThat(second.reachable(1)).containsExactly(1, 99);
        assertThat(first.reachable(1)).containsExactly(1, 2);
    }
}