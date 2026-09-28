package com.bdbank.service;

import com.bdbank.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.file.*;

public class FileManager {
    private static final FileManager INSTANCE = new FileManager();
    public static FileManager get() { return INSTANCE; }

    private static final Path DATA_DIR = Paths.get("data");

    private FileManager() {
        try { Files.createDirectories(DATA_DIR); } catch (IOException e) { throw new RuntimeException(e); }
    }

    public synchronized void appendLog(String fileName, String line) {
        try {
            Path logPath = DATA_DIR.resolve(fileName);
            Files.writeString(logPath, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("[FileManager] Failed to append log " + fileName + ": " + e.getMessage());
        }
    }

    public synchronized Path writeReport(String baseName, String content) {
        try {
            String fname = baseName + "_" + System.currentTimeMillis() + ".txt";
            Path p = DATA_DIR.resolve(fname);
            Files.writeString(p, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return p;
        } catch (IOException e) {
            System.err.println("[FileManager] Failed to write report " + baseName + ": " + e.getMessage());
            return null;
        }
    }

    public synchronized Path writeJsonReport(String baseName, JsonNode node) {
        try {
            String fname = baseName + "_" + System.currentTimeMillis() + ".json";
            Path p = DATA_DIR.resolve(fname);
            String pretty = JsonUtil.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node);
            Files.writeString(p, pretty, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return p;
        } catch (IOException e) {
            System.err.println("[FileManager] Failed to write JSON report " + baseName + ": " + e.getMessage());
            return null;
        }
    }
}
