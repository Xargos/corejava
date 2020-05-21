package mavenplugin;

import mavenplugin.io.IOFunctions;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@Mojo(name = "inc", defaultPhase = LifecyclePhase.PRE_CLEAN)
public class IncrementalMojo extends AbstractMojo {

    public static int test = 0;
    private static final List<String> sourceComponents = Arrays.asList("java", "scala", "resources");

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.compileSourceRoots}", readonly = true, required = true)
    private List<String> compileSourceRoots;

    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true, required = true)
    private File outputDirectory;

    private Optional<File> timestampFile = Optional.empty();

    public void execute() {

        test = 1;
        long start = System.currentTimeMillis();
        checkForModification();
        long total = System.currentTimeMillis() - start;
        info(String.format("Total time %s ms", total));
    }

    private void checkForModification() {
        Optional<Meta> codeCompileAt = classCompileTime(project.getBasedir());
        Meta codeChangedAt = codeChangeTime(compileSourceRoots);

        if (codeCompileAt.isPresent()) {
            info("Code compiled at %s", codeCompileAt.get());
        } else {
            info("Code never compiled");
        }
        info("Code changed at %s", codeChangedAt);

        if (!codeCompileAt.isPresent() || codeChangedAt.compareTo(codeCompileAt.get()) != 0) {
            prepareForCompilation(outputDirectory, codeChangedAt);
        } else {
            nothingToClean();
        }
    }

    private void prepareForCompilation(File targetLocation, Meta codeChangedAt) {

        Path rootTarget = targetLocation.getParentFile().toPath();
        info("Changed detected - cleaning %s", rootTarget);

        cleanTargetLocation(rootTarget);

        storeTimestamp(codeChangedAt);
    }

    private void storeTimestamp(Meta codeChangedAt) {
        this.timestampFile.ifPresent(file -> {
            info("delete: %s", file);
            file.delete();
        });
        TimestampFileService.storeTimestamp(codeChangedAt.getHash());
    }

    private void cleanTargetLocation(Path rootTarget) {
        Stream.of(rootTarget)
                .filter(Files::exists)
                .forEach(IOFunctions::deleteFiles);
    }

    private void nothingToClean() {
        info("Nothing to clean.");
        project.getProperties().setProperty("skipTests", "true");
        project.getProperties().setProperty("maven.main.skip", "true");
    }

    private Meta codeChangeTime(List<String> compileSourceRoots) {

        Stream<File> javaSourceLocation = compileSourceRoots.stream()
                .filter(this::isJavaLocation)
                .map(File::new);

        Stream<File> rootSourceLocation = javaSourceLocation.map(File::getParentFile);

        List<File> resourceToScan = rootSourceLocation
                .flatMap(this::sourceLocations)
                .filter(File::exists)
                .collect(Collectors.toList());

        return mostRecentUpdateTime(resourceToScan);
    }

    private boolean isJavaLocation(String location) {
        return location.endsWith("java");
    }

    private Stream<File> sourceLocations(File parentLocation) {

        Stream<File> sourceCode = sourceCodeLocation(parentLocation);
        Stream<File> testCode = testCodeLocation(parentLocation);
        Stream<File> configCode = configFilesLocation(parentLocation);

        Stream<File> codes = Stream.concat(sourceCode, testCode);
        return Stream.concat(codes, configCode);
    }

    private Stream<File> configFilesLocation(File parentLocation) {
        String twoLevelUp = parentLocation.getParentFile().getParent();
        return Stream.of(Paths.get(twoLevelUp, "pom.xml"))
                .map(Path::toFile);
    }

    private Stream<File> testCodeLocation(File parentLocation) {
        String oneLevelUp = parentLocation.getParent();

        return sourceComponents.stream()
                .map(component -> Paths.get(oneLevelUp, "test", component))
                .map(Path::toFile);
    }

    private Stream<File> sourceCodeLocation(File parentLocation) {
        return sourceComponents
                .stream()
                .map(component -> new File(parentLocation, component));
    }

    private Optional<Meta> classCompileTime(File rootPath) {
        File[] matchedFile = rootPath.listFiles(TimestampFileService::isTimeStampFile);
        Optional<File[]> timeStampFile = ofNullable(matchedFile);

        return timeStampFile
                .filter(this::hasFile)
                .map(Arrays::asList)
                .map(this::getTimestampMeta);
    }

    private Meta getTimestampMeta(List<File> files) {
        this.timestampFile = Optional.of(files.get(0));
        return new Meta(Integer.parseInt(timestampFile.get().getName().split("\\.")[2]));
    }

    private boolean hasFile(File[] x) {
        return x.length > 0;
    }

    private Meta mostRecentUpdateTime(List<File> files) {
        Stream<File> filesToCheck = files.stream()
                .peek(file -> info("Checking %s", file))
                .filter(File::exists);

        List<Long> accessibleFiles = filesToCheck
                .flatMap(IOFunctions::walkFile)
                .map(Path::toFile)
                .map(File::lastModified)
                .collect(Collectors.toList());

        if (accessibleFiles.isEmpty()) {
            return Meta.empty();
        }

        return new Meta(accessibleFiles.hashCode());
    }

    private void info(String template, Object... args) {
        getLog().info(String.format(template, args));
    }
}
