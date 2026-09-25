package com.bdbank.service;

import com.bdbank.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.file.*;

/**
 * File-based side outputs that sit ALONGSIDE the SQLite database (the database now holds all
 * of the system's real records - see Db.java). This class still earns its place for:
 *  - a human-readable, timestamped, append-only audit trail (account_opening_log.txt)
 *  - on-demand report exports the user/admin explicitly asks for (Monthly Statement -> Export,
 *    Bank Statement -> Export), as both plain text and JSON files.
 */
public class FileManager {
    private static final FileManager INSTANCE = new FileManager();
    public static FileManager get() { return INSTANCE; }

    private static final Path DATA_DIR = Paths.get("data");

    private FileManager() {
        try { Files.createDirectories(DATA_DIR); } catch (IOException e) { throw new RuntimeException(e); }
    }

    /** Appends a timestamped plain-text line to a human-readable audit log. */
    public synchronized void appendLog(String fileName, String line) {
        try {
            Path logPath = DATA_DIR.resolve(fileName);
            Files.writeString(logPath, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("[FileManager] Failed to append log " + fileName + ": " + e.getMessage());
        }
    }

    /** Writes a full report/statement out as its own timestamped plain-text file. */
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

    /** Writes a JSON report (any Jackson JsonNode - typically an ObjectNode) to its own timestamped
     *  .json file - a direct, visible demonstration of JSON serialization alongside the JSON
     *  parsing used elsewhere (requests, bank config, the exchange-rate API response). */
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
