import deque.ArrayDeque61B;
import deque.LinkedListDeque61B;
import deque.Deque61B;
import deque.Maximizer61B;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class DequeRegressionTest {
    @Test
    public void wrappedTailRemoval() {
        ArrayDeque61B<Integer> deque = new ArrayDeque61B<>();
        for (int i = 0; i < 7; i++) {
            deque.addLast(i);
        }
        assertEquals(6, deque.removeLast());
        assertEquals(List.of(0, 1, 2, 3, 4, 5), deque.toList());
    }

    @Test
    public void linkedIteratorVisitsEveryElementOnce() {
        LinkedListDeque61B<Integer> deque = new LinkedListDeque61B<>();
        assertFalse(deque.iterator().hasNext());
        deque.addLast(7);
        Iterator<Integer> one = deque.iterator();
        assertTrue(one.hasNext());
        assertEquals(7, one.next());
        assertFalse(one.hasNext());
        assertThrows(NoSuchElementException.class, one::next);
        deque.addLast(9);
        Iterator<Integer> two = deque.iterator();
        assertEquals(7, two.next());
        assertEquals(9, two.next());
        assertFalse(two.hasNext());
        assertEquals(9, Maximizer61B.max(deque));
        ArrayDeque61B<Integer> array = new ArrayDeque61B<>();
        array.addLast(7);
        array.addLast(9);
        assertEquals(array, deque);
        assertEquals(deque, array);
        array.removeLast();
        array.addLast(8);
        assertNotEquals(array, deque);
        assertNotEquals(deque, array);
    }

    @Test
    public void randomOperationsMatchJavaDeque() {
        List<Deque61B<Integer>> implementations = List.of(
                new ArrayDeque61B<>(), new LinkedListDeque61B<>());
        for (Deque61B<Integer> deque : implementations) {
            ArrayDeque<Integer> expected = new ArrayDeque<>();
            Random random = new Random(61);
            for (int i = 0; i < 10000; i++) {
                switch (random.nextInt(4)) {
                    case 0 -> { deque.addFirst(i); expected.addFirst(i); }
                    case 1 -> { deque.addLast(i); expected.addLast(i); }
                    case 2 -> assertEquals(expected.pollFirst(), deque.removeFirst());
                    case 3 -> assertEquals(expected.pollLast(), deque.removeLast());
                    default -> throw new AssertionError();
                }
                assertEquals(expected.size(), deque.size());
                assertEquals(expected.isEmpty(), deque.isEmpty());
                assertEquals(new ArrayList<>(expected), deque.toList());
            }
        }
    }
}
