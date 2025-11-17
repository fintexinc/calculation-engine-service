package performance.testing;

import lombok.extern.log4j.Log4j2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class ParserUtil {

    public static List<StockParseDTO> parseStocks(final String fileName) {
        return parse(fileName).stream().map(str -> {
            final String[] split = str.split("-");
            return new StockParseDTO().setTicker(split[0]).setExchangeId(split[1]);
        }).collect(Collectors.toList());
    }

    public static List<String> parse(final String fileName) {
        final Path path;
        final Stream<String> lines;
        try {
            log.info("Start parsing {}", fileName);
            path = Paths.get(ParserUtil.class.getResource("/performance-test-payloads/" + fileName).toURI());
            lines = Files.lines(path);
            final String data = lines.collect(Collectors.joining("\n"));
            lines.close();
            return Arrays.asList(data.split(","));
        } catch (final Exception e) {
            log.error("Fail by parsing file {}", fileName);
            throw new RuntimeException(e);
        }
    }

}
