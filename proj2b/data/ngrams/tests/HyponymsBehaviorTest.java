import browser.NgordnetQuery;
import browser.NgordnetQueryHandler;
import main.AutograderBuddy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class HyponymsBehaviorTest {

    @TempDir
    Path tempDir;

    private NgordnetQueryHandler handler;

    @BeforeEach
    public void setUp() throws IOException {
        Path synsets = tempDir.resolve("synsets.txt");
        Path hyponyms = tempDir.resolve("hyponyms.txt");
        Path words = tempDir.resolve("words.csv");
        Path totals = tempDir.resolve("totals.csv");

        Files.writeString(synsets, """
                0,animal beast,root with a comma, in its definition
                1,dog canine,dog meaning
                2,cat feline,cat meaning
                3,poodle,a kind of dog
                4,pet,pet root
                5,bank depository,financial meaning
                6,bank shore,geographical meaning
                7,vault,financial descendant
                8,coast,geographical descendant
                9,canine tooth,tooth meaning
                10,enamel,tooth descendant
                11,entity,larger root
                12,ghost,missing from frequency data
                13,silent,explicitly zero frequency
                14,tie,tie test root
                15,alpha,first tied word
                16,beta,second tied word
                17,gamma,lower frequency word
                """);

        Files.writeString(hyponyms, """
                0,1
                0,2
                0,1
                1,3
                4,1
                5,7
                6,8
                9,10
                11,0,12,13
                14,15,16,17
                """);

        // 单词文件用制表符分隔。
        Files.writeString(words, """
                animal\t2000\t1\t1
                canine\t2000\t3\t1
                canine\t2001\t2\t1
                cat\t2001\t12\t1
                dog\t2000\t9\t1
                dog\t2001\t1\t1
                dog\t2002\t1000\t1
                feline\t2000\t2\t1
                poodle\t2000\t6\t1
                poodle\t2001\t6\t1
                silent\t2000\t0\t1
                alpha\t2000\t5\t1
                beta\t2000\t5\t1
                gamma\t2000\t1\t1
                """);

        // 各年的总次数不同，用来区分原始次数与相对频率。
        Files.writeString(totals, """
                2000,100,1,1
                2001,10000,1,1
                2002,10000,1,1
                """);

        handler = AutograderBuddy.getHyponymsHandler(
                words.toString(),
                totals.toString(),
                synsets.toString(),
                hyponyms.toString()
        );
    }

    private String query(int k, int startYear, int endYear,
                         String... words) {
        NgordnetQuery q = new NgordnetQuery(
                List.of(words), startYear, endYear, k
        );
        return handler.handle(q);
    }

    @Test
    public void testSingleWordK0() {
        // 包括起点同义词、多层后代；去重并按字母排序。
        // 同时检查同一个起点分多行记录的边没有被覆盖。
        assertThat(query(0, 2000, 2001, "animal"))
                .isEqualTo(
                        "[animal, beast, canine, cat, dog, feline, poodle]"
                );
    }

    @Test
    public void testAllMeaningsOfInputWord() {
        // bank 对应两个不同节点，两边都必须遍历。
        assertThat(query(0, 2000, 2001, "bank"))
                .isEqualTo("[bank, coast, depository, shore, vault]");
    }

    @Test
    public void testDoNotExpandOtherMeaningsOfDiscoveredWords() {
        // 查询 dog 时，发现 canine 后不能跳到 canine 的牙齿含义。
        assertThat(query(0, 2000, 2001, "dog"))
                .isEqualTo("[canine, dog, poodle]");

        // 但直接查询 canine 时，必须从它的两个含义出发。
        assertThat(query(0, 2000, 2001, "canine"))
                .isEqualTo("[canine, dog, enamel, poodle, tooth]");
    }

    @Test
    public void testLeafIncludesItself() {
        assertThat(query(0, 2000, 2001, "poodle"))
                .isEqualTo("[poodle]");
    }

    @Test
    public void testMultipleWordsK0() {
        assertThat(query(0, 2000, 2001, "animal", "pet"))
                .isEqualTo("[canine, dog, poodle]");

        assertThat(query(0, 2000, 2001, "animal", "pet", "dog"))
                .isEqualTo("[canine, dog, poodle]");
    }

    @Test
    public void testIntersectionUsesWordsNotNodeIds() {
        // animal 与 tooth 的可达节点不重合，
        // 但不同节点都包含单词 canine。
        assertThat(query(0, 2000, 2001, "animal", "tooth"))
                .isEqualTo("[canine]");
    }

    @Test
    public void testRepeatedInputWords() {
        assertThat(query(0, 2000, 2001, "dog", "dog"))
                .isEqualTo("[canine, dog, poodle]");
    }

    @Test
    public void testUnknownWordAndEmptyIntersection() {
        for (int k : List.of(0, 5)) {
            assertThat(query(k, 2000, 2001, "unknown_xyz"))
                    .isEqualTo("[]");

            assertThat(query(k, 2000, 2001, "animal", "unknown_xyz"))
                    .isEqualTo("[]");

            assertThat(query(k, 2000, 2001, "animal", "bank"))
                    .isEqualTo("[]");
        }
    }

    @Test
    public void testK0IgnoresYearsAndFrequency() {
        // 1800 年没有词频数据，但 k = 0 仍返回所有候选词。
        assertThat(query(0, 1800, 1800, "animal"))
                .isEqualTo(
                        "[animal, beast, canine, cat, dog, feline, poodle]"
                );
    }

    @Test
    public void testTopKUsesSumOfCountsWithinRange() {
        // 2000—2001：
        // cat = 12，poodle = 12，dog = 10，canine = 5。
        // dog 在 2002 年的 1000 次不能计入。
        // 使用相对频率也会得到错误结果。
        assertThat(query(2, 2000, 2001, "animal"))
                .isEqualTo("[cat, poodle]");
    }

    @Test
    public void testSingleYearAndInclusiveBoundaries() {
        assertThat(query(1, 2000, 2000, "animal"))
                .isEqualTo("[dog]");

        assertThat(query(1, 2001, 2001, "animal"))
                .isEqualTo("[cat]");

        assertThat(query(1, 2002, 2002, "animal"))
                .isEqualTo("[dog]");
    }

    @Test
    public void testMultipleWordsTopK() {
        // 共同候选词：canine、dog、poodle。
        // 总次数：5、10、12。选中后按字母顺序输出。
        assertThat(query(2, 2000, 2001, "animal", "pet"))
                .isEqualTo("[dog, poodle]");
    }

    @Test
    public void testLargeKAndZeroFrequency() {
        // beast、entity、ghost 没有词频记录；
        // silent 有记录但次数为零，均应排除。
        // 不足 100 个词时，只返回符合条件的词。
        assertThat(query(100, 2000, 2001, "entity"))
                .isEqualTo("[animal, canine, cat, dog, feline, poodle]");

        assertThat(query(5, 2000, 2001, "ghost"))
                .isEqualTo("[]");

        assertThat(query(5, 2000, 2001, "silent"))
                .isEqualTo("[]");
    }

    @Test
    public void testNoCountsInRequestedYears() {
        assertThat(query(5, 1800, 1800, "animal"))
                .isEqualTo("[]");
    }

    @Test
    public void testEqualCountsDoNotLoseWords() {
        // alpha 和 beta 都是 5 次，不能因次数相同而覆盖彼此。
        assertThat(query(2, 2000, 2000, "tie"))
                .isEqualTo("[alpha, beta]");
    }

    @Test
    public void testRepeatedQueriesDoNotChangeStoredData() {
        assertThat(query(0, 2000, 2001, "animal", "pet"))
                .isEqualTo("[canine, dog, poodle]");

        assertThat(query(1, 2000, 2000, "animal"))
                .isEqualTo("[dog]");

        assertThat(query(0, 2000, 2001, "animal"))
                .isEqualTo(
                        "[animal, beast, canine, cat, dog, feline, poodle]"
                );

        assertThat(query(2, 2000, 2001, "animal"))
                .isEqualTo("[cat, poodle]");
    }
}