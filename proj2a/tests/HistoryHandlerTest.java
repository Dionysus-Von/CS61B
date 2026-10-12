import browser.NgordnetQuery;
import main.HistoryHandler;
import ngrams.NGramMap;
import ngrams.TimeSeries;
import org.junit.jupiter.api.Test;
import plotting.Plotter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static utils.Utils.SHORT_WORDS_FILE;
import static utils.Utils.TOTAL_COUNTS_FILE;

public class HistoryHandlerTest {
    @Test
    public void plotsExpectedRelativeFrequenciesWithinQueryRange() throws IOException {
        NGramMap map = new NGramMap(SHORT_WORDS_FILE, TOTAL_COUNTS_FILE);
        HistoryHandler handler = new HistoryHandler(map);
        TimeSeries request = new TimeSeries();
        request.put(2006, 2.44740192927834E-5);
        request.put(2007, 2.464488338318067E-5);
        TimeSeries airport = new TimeSeries();
        airport.put(2007, 6.2068176510855946E-6);
        String expected = Plotter.encodeChartAsString(Plotter.generateTimeSeriesChart(
                List.of("request", "airport"), List.of(request, airport)));
        String actual = handler.handle(new NgordnetQuery(
                List.of("request", "airport"), 2006, 2007, 0));
        assertThat(actual).isEqualTo(expected);
        assertPng(actual);
    }

    @Test
    public void skipsMissingWordsAndAvoidsDuplicatePlotLabels() {
        HistoryHandler handler = new HistoryHandler(
                new NGramMap(SHORT_WORDS_FILE, TOTAL_COUNTS_FILE));
        String baseline = handler.handle(new NgordnetQuery(
                List.of("request"), 2006, 2007, 0));
        assertThat(handler.handle(new NgordnetQuery(
                List.of("unknown_word", "request", "request"), 2006, 2007, 0)))
                .isEqualTo(baseline);
    }

    @Test
    public void emptyQueriesAndEmptyRangesProduceValidImages() throws IOException {
        HistoryHandler handler = new HistoryHandler(
                new NGramMap(SHORT_WORDS_FILE, TOTAL_COUNTS_FILE));
        assertPng(handler.handle(new NgordnetQuery(List.of(), 2006, 2007, 0)));
        assertPng(handler.handle(new NgordnetQuery(
                List.of("unknown_word"), 2006, 2007, 0)));
        assertPng(handler.handle(new NgordnetQuery(
                List.of("request"), 1900, 1901, 0)));
    }

    private void assertPng(String encoded) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(
                Base64.getDecoder().decode(encoded)));
        assertThat(image).isNotNull();
        assertThat(image.getWidth()).isEqualTo(800);
        assertThat(image.getHeight()).isEqualTo(600);
    }
}
