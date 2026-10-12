import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EmptyRemovalTest {
    @Test
    public void emptyRemovalsPreserveQueue() {
        LinkedListDeque61B<Integer> deque = new LinkedListDeque61B<>();
        for (int i = 0; i < 3; i++) {
            assertNull(deque.removeFirst());
            assertNull(deque.removeLast());
            assertEquals(0, deque.size());
            assertTrue(deque.isEmpty());
            deque.addLast(i);
            assertEquals(i, deque.removeFirst());
            deque.addFirst(i);
            assertEquals(i, deque.removeLast());
        }
    }
}
