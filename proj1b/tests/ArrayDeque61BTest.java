import deque.ArrayDeque61B;

import deque.Deque61B;
import jh61b.utils.Reflection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

public class ArrayDeque61BTest {

     @Test
     @DisplayName("ArrayDeque61B has no fields besides backing array and primitives")
     void noNonTrivialFields() {
         List<Field> badFields = Reflection.getFields(ArrayDeque61B.class)
                 .filter(f -> !(f.getType().isPrimitive() || f.getType().equals(Object[].class) || f.isSynthetic()))
                 .toList();

         assertWithMessage("Found fields that are not array or primitives").that(badFields).isEmpty();
     }
     @Test
     public void addFirstTestBasic() {
        Deque61B<String> lld1 = new ArrayDeque61B<>();

        lld1.addFirst("back"); // after this call we expect: ["back"]
        assertThat(lld1.toList()).containsExactly("back").inOrder();

        lld1.addFirst("middle"); // after this call we expect: ["middle", "back"]
        assertThat(lld1.toList()).containsExactly("middle", "back").inOrder();

        lld1.addFirst("front"); // after this call we expect: ["front", "middle", "back"]
        assertThat(lld1.toList()).containsExactly("front", "middle", "back").inOrder();

         /* Note: The first two assertThat statements aren't really necessary. For example, it's hard
            to imagine a bug in your code that would lead to ["front"] and ["front", "middle"] failing,
            but not ["front", "middle", "back"].
          */
    }

    @Test

    public void addLastTestBasic() {
        Deque61B<String> lld1 = new ArrayDeque61B<>();

        lld1.addLast("front"); // after this call we expect: ["front"]
        lld1.addLast("middle"); // after this call we expect: ["front", "middle"]
        lld1.addLast("back"); // after this call we expect: ["front", "middle", "back"]
        assertThat(lld1.toList()).containsExactly("front", "middle", "back").inOrder();
    }

    @Test

    public void addFirstAndAddLastTest() {
        Deque61B<Integer> lld1 = new ArrayDeque61B<>();

         /* I've decided to add in comments the state after each call for the convenience of the
            person reading this test. Some programmers might consider this excessively verbose. */
        lld1.addLast(0);   // [0]
        lld1.addLast(1);   // [0, 1]
        lld1.addFirst(-1); // [-1, 0, 1]
        lld1.addLast(2);   // [-1, 0, 1, 2]
        lld1.addFirst(-2); // [-2, -1, 0, 1, 2]

        assertThat(lld1.toList()).containsExactly(-2, -1, 0, 1, 2).inOrder();
    }



    @Test
    public void isEmpty(){
        Deque61B<String> lld1=new ArrayDeque61B<>();
        assertThat(lld1.isEmpty()).isTrue();
        lld1.addFirst("Never gonna give you up.");
        assertThat(lld1.isEmpty()).isFalse();
    }
    @Test
    public void sizeTest(){
        Deque61B<Integer> lld1 = new ArrayDeque61B<>();
        assertThat(lld1.size()).isEqualTo(0);
        lld1.addFirst(985);
        lld1.addLast(211);
        assertThat(lld1.size()).isEqualTo(2);
        lld1.removeFirst();
        assertThat(lld1.size()).isEqualTo(1);
        lld1.removeLast();
        assertThat(lld1.size()).isEqualTo(0);
    }
    @Test
    public void getTest(){
        Deque61B<String> lld1 = new ArrayDeque61B<>();
        lld1.addLast("Never make you cry");
        assertThat(lld1.get(360)).isEqualTo(null);
        assertThat(lld1.get(-11)).isEqualTo(null);
        assertThat(lld1.get(0)).isEqualTo("Never make you cry");
    }
    @Test
    public void getRecursiveTest(){
        Deque61B<String> lld1 = new  ArrayDeque61B<>();
        lld1.addLast("Never make you cry");
        assertThat(lld1.get(360)).isEqualTo(null);
        assertThat(lld1.get(-11)).isEqualTo(null);
        assertThat(lld1.get(0)).isEqualTo("Never make you cry");
    }
    @Test
    public void removeFirst(){
        Deque61B<String> lld1 = new ArrayDeque61B<>();
        lld1.addFirst("985");
        lld1.addLast("211");
        lld1.addLast("沙坡路电工培训");
        assertThat(lld1.size()).isEqualTo(3);
        assertThat(lld1.toList()).containsExactly("985","211","沙坡路电工培训").inOrder();
        String s=lld1.removeFirst();
        assertThat(s).isEqualTo("985");
        assertThat(lld1.size()).isEqualTo(2);
        assertThat(lld1.toList()).containsExactly("211","沙坡路电工培训").inOrder();
        lld1.removeFirst();
        assertThat(lld1.size()).isEqualTo(1);
        assertThat(lld1.toList()).containsExactly("沙坡路电工培训").inOrder();
    }

    @Test
    public void removeLast(){
        Deque61B<String> lld1 = new ArrayDeque61B<>();
        lld1.addFirst("985");
        lld1.addLast("211");
        lld1.addLast("沙坡路电工培训");
        assertThat(lld1.size()).isEqualTo(3);
        assertThat(lld1.toList()).containsExactly("985","211","沙坡路电工培训").inOrder();
        String s = lld1.removeLast();
        assertThat(s).isEqualTo("沙坡路电工培训");
        assertThat(lld1.size()).isEqualTo(2);
        assertThat(lld1.toList()).containsExactly("985","211").inOrder();
        lld1.removeLast();
        assertThat(lld1.size()).isEqualTo(1);
        assertThat(lld1.toList()).containsExactly("985").inOrder();
    }
}
