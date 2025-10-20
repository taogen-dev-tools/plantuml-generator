package com.taogen.docs2uml.commons.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 *
 * @author taogen
 */
@Slf4j
public class CommandLineUtil {
    public static void executeCommandLine(String commandLine) {
        log.info("Executing command line: {}", commandLine);
        try {
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", commandLine);
            // On Windows: new ProcessBuilder("cmd", "/c", "echo Hello from Java");

            builder.redirectErrorStream(true); // merge stderr with stdout

            Process process = builder.start();

            // Read combined output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("Output: {}", line);
                }
            }
            int exitCode = process.waitFor();
            log.info("Exited with code: " + exitCode);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
