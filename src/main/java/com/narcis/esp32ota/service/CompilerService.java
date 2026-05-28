package com.narcis.esp32ota.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.List;

@Service
public class CompilerService {

    private static final String ARDUINO_CLI = "C:\\arduino-cli\\arduino-cli.exe";
    private static final String FQBN        = "esp32:esp32:esp32";
    private static final String BUILD_DIR   = "C:\\arduino-cli\\builds\\";

    public byte[] compile(Long programId, String code) throws Exception {
        sanitize(code);

        String sketchDir  = BUILD_DIR + programId + "\\sketch_" + programId;
        String sketchFile = sketchDir + "\\sketch_" + programId + ".ino";

        Files.createDirectories(Paths.get(sketchDir));
        Files.writeString(Paths.get(sketchFile), code);


        ProcessBuilder pb = new ProcessBuilder(
                ARDUINO_CLI, "compile",
                "--fqbn", FQBN,
                "--output-dir", BUILD_DIR + programId,
                sketchDir
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();


        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("Compilation failed:\n" + output);
        }


        String binPath = BUILD_DIR + programId + "\\sketch_" + programId + ".ino.bin";
        return Files.readAllBytes(Paths.get(binPath));
    }

    public void cleanup(Long programId) {
        try {
            Path dir = Paths.get(BUILD_DIR + programId);
            Files.walk(dir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (Exception ignored) {}
    }

    private static final List<String> FORBIDDEN = List.of(
            "system(", "exec(", "popen(",
            "Runtime.getRuntime",
            "#include <cstdlib>",
            "FILE *", "fopen(", "fwrite(", "fread(",
            "socket(", "connect(", "bind(",
            "format(", "mkfs(",
            "nvs_flash_erase",
            "esp_partition_erase_range",
            "Update.begin", "Update.write"
    );

    public void sanitize(String code) throws Exception {
        String lower = code.toLowerCase();
        for (String forbidden : FORBIDDEN) {
            if (lower.contains(forbidden.toLowerCase())) {
                throw new Exception("Forbidden code detected: " + forbidden);
            }
        }

        if (code.length() > 50000) {
            throw new Exception("Code too large!");
        }
    }
}