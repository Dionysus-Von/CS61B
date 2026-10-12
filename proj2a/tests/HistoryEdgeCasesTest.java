import browser.NgordnetQuery;
import main.HistoryTextHandler;
import ngrams.NGramMap;
import ngrams.TimeSeries;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static utils.Utils.SHORT_WORDS_FILE;
import static utils.Utils.TOTAL_COUNTS_FILE;

public class HistoryEdgeCasesTest {
    @Test
    public void rangeCopyIncludesEndpointsAndIsIndependent() {
        TimeSeries source = new TimeSeries();
        source.put(1999, 1.0);
        source.put(2000, 2.0);
        source.put(2002, 3.0);
        source.put(2003, 4.0);
        TimeSeries copy = new TimeSeries(source, 2000, 2002);
        assertThat(copy.years()).containsExactly(2000, 2002).inOrder();
        assertThat(copy.data()).containsExactly(2.0, 3.0).inOrder();
        copy.put(2000, 99.0);
        assertThat(source.get(2000)).isEqualTo(2.0);
    }

    @Test
    public void divisionUsesOnlyNumeratorYearsAndPreservesInputs() {
        TimeSeries numerator = new TimeSeries();
        numerator.put(2000, 12.0);
        TimeSeries denominator = new TimeSeries();
        denominator.put(2000, 4.0);
        denominator.put(2001, 5.0);
        TimeSeries quotient = numerator.dividedBy(denominator);
        assertThat(quotient.years()).containsExactly(2000);
        assertThat(quotient.get(2000)).isEqualTo(3.0);
        assertThat(numerator.get(2000)).isEqualTo(12.0);
        assertThat(denominator.get(2000)).isEqualTo(4.0);
        assertThrows(IllegalArgumentException.class,
                () -> numerator.dividedBy(new TimeSeries()));
        assertThat(new TimeSeries().dividedBy(denominator)).isEmpty();
    }

    @Test
    public void sumPreservesInputsAndKeepsOnlyPresentYears() {
        TimeSeries left = new TimeSeries();
        left.put(2000, 2.0);
        TimeSeries right = new TimeSeries();
        right.put(2000, 3.0);
        right.put(2002, 4.0);
        TimeSeries sum = left.plus(right);
        assertThat(sum.years()).containsExactly(2000, 2002).inOrder();
        assertThat(sum.data()).containsExactly(5.0, 4.0).inOrder();
        assertThat(left.get(2000)).isEqualTo(2.0);
        assertThat(right.get(2000)).isEqualTo(3.0);
    }

    @Test
    public void mapReturnsCopiesAndHandlesMissingData() {
        NGramMap map = new NGramMap(SHORT_WORDS_FILE, TOTAL_COUNTS_FILE);
        map.countHistory("request").put(2006, -1.0);
        map.countHistory("request", 2006, 2007).clear();
        map.totalCountHistory().clear();
        assertThat(map.countHistory("request").get(2006)).isEqualTo(677820.0);
        assertThat(map.totalCountHistory()).isNotEmpty();
        assertThat(map.countHistory("unknown_word")).isEmpty();
        assertThat(map.weightHistory("unknown_word")).isEmpty();
        assertThat(map.countHistory("request", 1900, 1901)).isEmpty();
        assertThat(map.summedWeightHistory(List.of())).isEmpty();
        assertThat(map.summedWeightHistory(List.of("request", "unknown_word"), 2006, 2007))
                .isEqualTo(map.weightHistory("request", 2006, 2007));
    }

    @Test
    public void handlerKeepsQueryOrderAndEndsEveryLineWithNewline() {
        NGramMap map = new NGramMap(SHORT_WORDS_FILE, TOTAL_COUNTS_FILE);
        HistoryTextHandler handler = new HistoryTextHandler(map);
        assertThat(handler.handle(new NgordnetQuery(
                List.of("airport", "unknown_word", "request"), 2007, 2007, 10)))
                .isEqualTo("airport: {2007=6.2068176510855946E-6}\n"
                        + "unknown_word: {}\n"
                        + "request: {2007=2.464488338318067E-5}\n");
        assertThat(handler.handle(new NgordnetQuery(List.of(), 2006, 2007, 0)))
                .isEmpty();
    }
}
