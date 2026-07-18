package common;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
/*
 * ---------------
 * Small utility class responsible for writing every chat event to a
 * log file on disk (logs/chat_log.txt). This gives you:
 *   - Proof of a working chat history for your GitHub screenshots
 *   - A simple example of file I/O in Java
 *
 * The class is "synchronized" internally because MULTIPLE client
 * handler threads will try to write to the same file at the same time.
 * Without synchronization, lines from different clients could get
 * interleaved or corrupted.
 */
 */
public class ChatLogger {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = LOG_DIR + "/chat_log.txt";
    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // A private lock object dedicated to guarding file writes.
    private static final Object LOCK = new Object();

    static {
        // Make sure the logs/ folder exists before anyone tries to write to it.
        new java.io.File(LOG_DIR).mkdirs();
    }

    /**
     * Appends a single line to the chat log file.
     * Synchronized on a shared lock so only one thread writes at a time.
     */
    public static void log(String line) {
        synchronized (LOCK) {
            try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                out.println("[" + LocalDateTime.now().format(STAMP) + "] " + line);
            } catch (IOException e) {
                System.err.println("ChatLogger: failed to write log -> " + e.getMessage());
            }
        }
    }
}
