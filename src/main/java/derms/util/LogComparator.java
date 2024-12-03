package derms.util;

import java.io.*;
import java.nio.file.*;

public class LogComparator {
    public static boolean compareLineCounts(String actualFilePath, String expectedFilePath) throws IOException {
        long actualLineCount = Files.lines(Paths.get(actualFilePath)).count();
        long expectedLineCount = Files.lines(Paths.get(expectedFilePath)).count();
        System.out.println("XXXXXXXXX ACTUAL LINE: " + actualLineCount);
        System.out.println("XXXXXXXXX EXPECTED: " + expectedLineCount);
        return actualLineCount == expectedLineCount;
    }
public static boolean containsSuccess(String filePath) throws IOException {
    return Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("SUCCESS"));
}

public static boolean compareFiles(String actualFilePath, String expectedFilePath) throws IOException {
    boolean lineCountsMatch = compareLineCounts(actualFilePath, expectedFilePath);
    boolean actualContainsSuccess = containsSuccess(actualFilePath);
    System.out.println("XXXXXXXXX ACTUAL SUCCESS: " + actualContainsSuccess);
    boolean expectedContainsSuccess = containsSuccess(expectedFilePath);
    System.out.println("XXXXXXXXX EXPECTED SUCCESS: " + expectedContainsSuccess);
    
    return lineCountsMatch && actualContainsSuccess && expectedContainsSuccess;
}
}