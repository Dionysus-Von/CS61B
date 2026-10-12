import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BSTMapRegressionTest {
    @Test
    public void equivalentKeysReplaceValues() {
        BSTMap<String, Integer> map = new BSTMap<>();
        map.put(new String("cat"), 1);
        map.put(new String("ant"), 2);
        map.put(new String("dog"), 3);
        map.put(new String("cat"), 4);
        map.put(new String("ant"), 5);
        map.put(new String("dog"), 6);
        assertEquals(3, map.size());
        assertEquals(4, map.get("cat"));
        assertEquals(5, map.get("ant"));
        assertEquals(6, map.get("dog"));
    }
}
