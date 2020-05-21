package mavenplugin;

import mavenplugin.io.IOFunctions;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public class TimestampFileService {

    private static Optional<Integer> BUILD_HASH_STORE = Optional.empty();
    private static final String TIMESTAMP_FILE = "buildcheck.timestamp.";

    static void storeTimestamp(int timestamp) {
        BUILD_HASH_STORE = Optional.of(timestamp);
    }

    static void createTimeStampFile(Path path) {
        BUILD_HASH_STORE.ifPresent(hashCode -> storeFile(path, hashCode));
    }

    private static void storeFile(Path path, Integer hashCode) {
        path.toFile().mkdir();
        Path timeStampFile = new File(path.toFile(), String.format(TIMESTAMP_FILE + "%d", hashCode)).toPath();
        IOFunctions.touch(timeStampFile);
    }

    static boolean isTimeStampFile(File file) {
        return file.getName().startsWith(TIMESTAMP_FILE);
    }
}
