package corejavasamples.jdk12.gc;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.function.Consumer;

import static java.util.List.of;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
Run with below JVM args
--enable-preview
 */
public class EpsilonGCTest {

    String oomErrorPattern = "OutOfMemoryError".toLowerCase();

    @Test
    @DisplayName("Pass when default GC is used")
    void should_pass_when_default_gc_is_used() throws IOException {

        var command = of(
                "java",
                "--enable-preview",
                "-Xlog:gc",
                "-cp target/classes/;",
                "corejavasamples.jdk12.gc.MemoryAllocator");

        var runCommand = command
                .stream()
                .collect(joining(" "));

        var errors = new ArrayList<>();
        Consumer<String> outputConsumer = line -> {
            System.out.println(line);
            if (isOutOfMemory(line)) {
                errors.add(line);
            }
        };
        Consumer<String> errorConsumer = line -> System.err.println(line);

        execute(runCommand, outputConsumer, errorConsumer);
        assertTrue(errors.isEmpty());

    }

    @Test
    @DisplayName("Fail when epsilon GC is used and memory is exhausted")
    void should_fail_when_epsilon_gc_is_used__and_memory_exhausted() throws IOException {


        var command = of(
                "java",
                "--enable-preview",
                "-Xlog:gc",
                "-XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC -Xmx2g",
                "-Dmb=5000",
                "-cp target/classes/;",
                "corejavasamples.jdk12.gc.MemoryAllocator");

        var runCommand = command
                .stream()
                .collect(joining(" "));


        var errors = new ArrayList<>();
        Consumer<String> outputConsumer = line -> {
            System.out.println(line);
            if (isOutOfMemory(line)) {
                errors.add(line);
            }
        };
        Consumer<String> errorConsumer = line -> System.err.println(line);

        execute(runCommand, outputConsumer, errorConsumer);
        assertFalse(errors.isEmpty());

    }

    @Test
    @DisplayName("Pass when epsilon GC is used and memory is not exhausted")
    void epsilon_gc_is_used_but_memory_not_exhausted() throws IOException {


        var command = of(
                "java",
                "--enable-preview",
                "-Xlog:gc",
                "-XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC -Xmx2g",
                "-Dmb=1000",
                "-cp target/classes/;",
                "corejavasamples.jdk12.gc.MemoryAllocator");

        var runCommand = command.stream()
                .collect(joining(" "));


        var errors = new ArrayList<>();
        Consumer<String> outputConsumer = line -> {
            System.out.println(line);
            if (isOutOfMemory(line)) {
                errors.add(line);
            }
        };
        Consumer<String> errorConsumer = line -> System.err.println(line);

        execute(runCommand, outputConsumer, errorConsumer);
        assertTrue(errors.isEmpty());

    }

    @Test
    @DisplayName("fail when memory is exhausted using multiple thread")
    void multiple_thread_allocation_using_epsilon_gc() throws IOException {


        var command = of(
                "java",
                "--enable-preview",
                "-Xlog:gc",
                "-XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC -Xmx2g",
                "-Dmb=5000",
                "-cp target/classes/;",
                "corejavasamples.jdk12.gc.MultiThreadMemoryAllocator");

        var runCommand = command
                .stream()
                .collect(joining(" "));


        var errors = new ArrayList<>();
        Consumer<String> outputConsumer = line -> {
            System.out.println(line);
            if (isOutOfMemory(line)) {
                errors.add(line);
            }
        };
        Consumer<String> errorConsumer = line -> System.err.println(line);

        execute(runCommand, outputConsumer, errorConsumer);
        assertFalse(errors.isEmpty());

    }

    private boolean isOutOfMemory(String line) {
        return line.toLowerCase().indexOf(oomErrorPattern) > -1;
    }

    public static void execute(String runCommand, Consumer<String> outputStreamConsumer, Consumer<String> errorConsumer) throws IOException {

        System.out.println(String.format("Running %s", runCommand));
        var process = Runtime.getRuntime().exec(runCommand);
        System.out.println("Launched and PID is " + process.pid());

        read(process.getInputStream(), outputStreamConsumer);
        read(process.getErrorStream(), errorConsumer);

    }

    private static void read(InputStream in, Consumer<String> processor) throws IOException {
        if (in != null) {
            var reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                processor.accept(line);
            }
        }
    }


}
