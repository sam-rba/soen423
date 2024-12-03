package derms.util;

import java.io.*;
import java.nio.file.*;

public class LogComparator {
    public static boolean compareLineCounts(String actualFilePath, String expectedFilePath) throws IOException {
        long actualLineCount = Files.lines(Paths.get(actualFilePath)).count();
        long expectedLineCount = Files.lines(Paths.get(expectedFilePath)).count();

        return actualLineCount == expectedLineCount;
    }
}