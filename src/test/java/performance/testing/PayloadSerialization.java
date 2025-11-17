package performance.testing;

import com.fintex.ce.util.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class PayloadSerialization {

    private final Map<String, List<Object>> calculationNamesAndPayloads;
    final Path rootPath = Paths.get("target").resolve("jmeter");

    public PayloadSerialization(final Map<String, List<Object>> calculationNamesAndPayloads) {
        this.calculationNamesAndPayloads = calculationNamesAndPayloads;
    }

    public void serialize() {
        removeOldFiles();
        calculationNamesAndPayloads.forEach(this::serialize);
    }

    private void serialize(final String calculationName, final List<Object> requests) {
        try {
            final String calculationDirPath = createDirFor(calculationName);
            createJsonFiles(calculationDirPath, requests);
            createCsvFile(calculationName);
        } catch (final Exception e) {
            log.error("Exception when serializing, exception ", e);
        }
    }

    private void createCsvFile(final String calculationName) throws IOException {
        final Path path = rootPath.resolve(calculationName);
        final File directoryPath = new File(path.toString());
        final List<String> fileNames = getFileNames(directoryPath);
        Files.write(path.resolve("file_names.csv"), fileNames, Charset.defaultCharset());
    }

    private List<String> getFileNames(final File directoryPath) {
        return List.of(Objects.requireNonNull(directoryPath.list()))
                .stream().map(e -> e.replace(".txt", ""))
                .collect(Collectors.toList());
    }

    private String createDirFor(final String calculationName) throws IOException {
        final Path path = rootPath.resolve(calculationName);
        return Files.createDirectories(path).toString();
    }

    private void removeOldFiles() {
        try {
            if (Files.exists(rootPath)) {
                FileUtils.cleanDirectory(rootPath.toFile());
            }
        } catch (IOException e) {
            log.error("Exception when deleting old files from {}, exception ", rootPath, e);
        }
    }

    void createJsonFiles(final String calculationDirPath, final List<Object> requests) throws IOException {
        for (final Object request : requests) {
            createJsonFile(calculationDirPath, request);
        }
    }

    void createJsonFile(final String calculationDirPath, final Object request) throws IOException {
        final String requestJson = JacksonUtil.serialize(request);
        final Path path = Paths.get(calculationDirPath).resolve(UUID.randomUUID().toString() + ".txt");
        Files.write(path, requestJson.getBytes());
    }

}
