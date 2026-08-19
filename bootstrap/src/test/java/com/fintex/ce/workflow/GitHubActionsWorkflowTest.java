package com.fintex.ce.workflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GitHub Actions CI Workflow Tests")
public class GitHubActionsWorkflowTest {

    private static final String WORKFLOW_PATH = ".github/workflows/ci.yml";

    @Test
    @DisplayName("Workflow file exists at expected location")
    public void testWorkflowFileExists() {
        Path workflowFile = Paths.get(WORKFLOW_PATH);
        assertTrue(Files.exists(workflowFile),
            "Workflow file should exist at " + WORKFLOW_PATH);
    }

    @Test
    @DisplayName("Workflow file is readable")
    public void testWorkflowFileIsReadable() throws Exception {
        Path workflowFile = Paths.get(WORKFLOW_PATH);
        assertTrue(Files.isReadable(workflowFile),
            "Workflow file should be readable");
    }

    @Test
    @DisplayName("Workflow contains pull_request trigger")
    public void testWorkflowHasPullRequestTrigger() throws Exception {
        String content = Files.readString(Paths.get(WORKFLOW_PATH));
        assertTrue(content.contains("pull_request"),
            "Workflow should trigger on pull_request events");
    }

    @Test
    @DisplayName("Workflow contains spotless-check job")
    public void testWorkflowHasSpotlessCheckJob() throws Exception {
        String content = Files.readString(Paths.get(WORKFLOW_PATH));
        assertTrue(content.contains("spotless-check"),
            "Workflow should contain spotless-check job");
        assertTrue(content.contains("spotless:check"),
            "Workflow should run spotless:check command");
    }

    @Test
    @DisplayName("Workflow contains unit-tests job")
    public void testWorkflowHasUnitTestsJob() throws Exception {
        String content = Files.readString(Paths.get(WORKFLOW_PATH));
        assertTrue(content.contains("unit-tests"),
            "Workflow should contain unit-tests job");
        assertTrue(content.contains("./mvnw test"),
            "Workflow should run mvnw test command");
    }

    @Test
    @DisplayName("Workflow contains package job")
    public void testWorkflowHasPackageJob() throws Exception {
        String content = Files.readString(Paths.get(WORKFLOW_PATH));
        assertTrue(content.contains("package"),
            "Workflow should contain package job");
        assertTrue(content.contains("./mvnw package"),
            "Workflow should run mvnw package command");
    }

    @Test
    @DisplayName("Workflow publishes test results")
    public void testWorkflowPublishesTestResults() throws Exception {
        String content = Files.readString(Paths.get(WORKFLOW_PATH));
        assertTrue(content.contains("publish-unit-test-result-action"),
            "Workflow should publish test results");
        assertTrue(content.contains("surefire-reports"),
            "Workflow should reference surefire test reports");
    }

    @Test
    @DisplayName("Workflow uses JDK 21")
    public void testWorkflowUsesJdk21() throws Exception {
        String content = Files.readString(Paths.get(WORKFLOW_PATH));
        assertTrue(content.contains("java-version: '21'"),
            "Workflow should use JDK 21");
    }

    @Test
    @DisplayName("Workflow has job dependencies configured")
    public void testWorkflowHasJobDependencies() throws Exception {
        String content = Files.readString(Paths.get(WORKFLOW_PATH));
        assertTrue(content.contains("needs: spotless-check"),
            "unit-tests job should depend on spotless-check");
        assertTrue(content.contains("needs: unit-tests"),
            "package job should depend on unit-tests");
    }
}
