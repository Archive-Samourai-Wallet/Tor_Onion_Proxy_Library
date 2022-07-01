package com.samourai.tor.client.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SamouraiTorUtils {
    public static List<String> exec(String cmd) throws Exception {
        List<String> lines = new ArrayList<>();
        Process proc = null;
        Scanner scanner = null;
        try {
            proc = Runtime.getRuntime().exec(cmd);

            scanner = new Scanner(proc.getInputStream());
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }

            int exit = proc.waitFor();
            if (exit != 0) {
                String output = String.join("\n", lines.toArray(new String[]{}));
                throw new RuntimeException(
                        "exec [" + cmd + "] returned error code: " + exit + "\nOutput:\n" + output);
            }
        } finally {
            if (proc != null) {
                proc.destroy();
            }
            if (scanner != null) {
                scanner.close();
            }
        }
        return lines;
    }

    public static void deleteRecursively(File f) {
        File[] allContents = f.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                SamouraiTorUtils.deleteRecursively(file);
            }
        }
        f.delete();
    }
}