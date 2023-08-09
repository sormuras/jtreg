package api;

import org.junit.jupiter.api.TestInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public record SystemTest(String name) {
    public static SystemTest of(TestInfo info) {
        var name = info.getTestClass().orElseThrow().getSimpleName() + '.' + info.getTestMethod().orElseThrow().getName();
        return new SystemTest(name);
    }

    public Result run(String... args) {
        // System tests require a pre-built jtreg image
        var imageDirectory = Path.of("build/images/jtreg");
        var jar = imageDirectory.resolve( "lib/jtreg.jar");
        assertTrue(Files.isRegularFile(jar), "JAR file not found: " + jar);

        // Launch jtreg via Java's command-line program
        var command = Stream.concat(Stream.of("java", "-jar", jar.toString()), Stream.of(args));
        try {
            var process = new ProcessBuilder(command.toList()).redirectErrorStream(true).start();
            if (!process.waitFor(9, TimeUnit.SECONDS)) fail("Timeout!");
            var output = new String(process.getInputStream().readAllBytes());
            return new Result(process, output);
        } catch (Exception exception) {
            throw new AssertionError("Running system self-test failed", exception);
        }
    }

    public record Result(Process process, String output) {}
}
