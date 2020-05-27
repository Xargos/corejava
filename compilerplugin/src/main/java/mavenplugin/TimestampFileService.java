package mavenplugin;

import mavenplugin.io.IOFunctions;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class TimestampFileService {

    private static final String TIMESTAMP_FILE = "buildcheck.timestamp.";
    private static final ReentrantLock lock = new ReentrantLock();
    private static TimestampFileService INSTANCE;
    private final ConcurrentHashMap<String, Consumer<Path>> storeFile = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, File> getFile = new ConcurrentHashMap<>();
    private final Log log;

    public TimestampFileService(Log log) {
        this.log = log;
    }

    static TimestampFileService instance(Log log) {
        lock.lock();
        if (INSTANCE == null) {
            INSTANCE = new TimestampFileService(log);
        }
        lock.unlock();
        return INSTANCE;
    }

    void storeTimestamp(String module, int hashCode) {
        deleteOldTimestamp(module);
        storeFile.put(module, path -> storeFile(path, hashCode));
    }

    private void deleteOldTimestamp(String module) {
        Optional.ofNullable(getFile.get(module))
                .ifPresent(file -> {
                    info("delete: %s", file);
                    file.delete();
                });
    }

    void createTimeStampFile(String module, Path path) {
        storeFile.getOrDefault(module, p -> {
        }).accept(path);
    }

    private void storeFile(Path path, Integer hashCode) {
        path.toFile().mkdir();
        Path timeStampFile = new File(path.toFile(), String.format(TIMESTAMP_FILE + "%d", hashCode)).toPath();
        IOFunctions.touch(timeStampFile);
    }

    boolean isTimeStampFile(File file) {
        return file.getName().startsWith(TIMESTAMP_FILE);
    }


    Meta getTimestampMeta(String module, File file) {
        getFile.put(module, file);
        return new Meta(Integer.parseInt(file.getName().split("\\.")[2]));
    }

    private void info(String template, Object... args) {
        log.info(String.format(template, args));
    }
}
