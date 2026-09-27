package daa;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.nio.file.Path;
import java.util.List;

public final class TestScreenshotExtension implements TestWatcher, AfterAllCallback {
    private int passed;
    private int failed;
    private int aborted;
    private int skipped;

    @Override
    public void testSuccessful(ExtensionContext context) {
        passed++;
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        failed++;
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        aborted++;
    }

    @Override
    public void testDisabled(ExtensionContext context, java.util.Optional<String> reason) {
        skipped++;
    }

    @Override
    public void afterAll(ExtensionContext context) {
        int total = passed + failed + aborted + skipped;
        ScreenshotGenerator.writeTerminalScreenshot(
                Path.of("docs", "screenshots", "test-results.png"),
                "Assignment 1 - Correctness test results",
                List.of(
                        "JUnit test methods: " + total,
                        "Passed: " + passed + "   Failed: " + failed + "   Aborted: " + aborted
                                + "   Skipped: " + skipped,
                        "Sorting: Arrays.sort reference over six input shapes",
                        "Selection: 250 randomized rank checks",
                        "Closest pair: brute force, including n = 2,000"
                ));
    }
}
