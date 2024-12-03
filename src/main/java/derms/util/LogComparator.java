package derms.util;

import java.io.*;
import java.nio.file.*;

public class LogComparator {

public static boolean containsSuccess(String filePath) throws IOException {
    return Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("SUCCESS"));
}

public static boolean containsCrashTrue(String filePath) throws IOException {
    boolean a = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 1: {CRASH: TRUE}"));
    boolean b = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 1: {CRASH: DETECTED}"));
    boolean c = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 1: {RESTARTED}"));
    boolean d = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("SUCCESS"));

    System.out.println("XXXXXXXXX REPLICA 1: {CRASH: TRUE}: " + a);
    System.out.println("XXXXXXXXX REPLICA 1: {CRASH: DETECTED}: " + b);
    System.out.println("XXXXXXXXX REPLICA 1: {RESTARTED}: " + c);
    System.out.println("XXXXXXXXX REPLICA 1: SUCCESS: " + d);

    return a && b && c && d;
}

public static boolean containsByzTrue(String filePath) throws IOException {
    boolean a = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 1: {BYZANTINE: TRUE}"));
    boolean b = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 1: {BYZANTINE: DETECTED}"));
    boolean c = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 1: {RESTARTED}"));
    boolean d = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("SUCCESS"));

    System.out.println("XXXXXXXXX REPLICA 1: {BYZANTINE: TRUE}: " + a);
    System.out.println("XXXXXXXXX REPLICA 1: {BYZANTINE: DETECTED}: " + b);
    System.out.println("XXXXXXXXX REPLICA 1: {RESTARTED}: " + c);
    System.out.println("XXXXXXXXX REPLICA 1: SUCCESS: " + d);

    return a && b && c && d;
}

public static boolean containsCombTrue(String filePath) throws IOException {
    boolean a = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 1: {BYZANTINE: TRUE}"));
    boolean b = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 1: {BYZANTINE: DETECTED}"));
    boolean c = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 1: {RESTARTED}"));
    boolean d = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 3: {CRASH: TRUE}"));
    boolean e = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 3: {CRASH: DETECTED}"));
    boolean f = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 3: {RESTARTED}"));
    boolean g = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("SUCCESS"));

    System.out.println("XXXXXXXXX REPLICA 1: {BYZANTINE: TRUE}: " + a);
    System.out.println("XXXXXXXXX REPLICA 1: {BYZANTINE: DETECTED}: " + b);
    System.out.println("XXXXXXXXX REPLICA 1: {RESTARTED}: " + c);
    System.out.println("XXXXXXXXX REPLICA 1: {CRASH: TRUE}: " + d);
    System.out.println("XXXXXXXXX REPLICA 1: {BYCRASHZANTINE: DETECTED}: " + e);
    System.out.println("XXXXXXXXX REPLICA 1: {RESTARTED}: " + f);
    System.out.println("XXXXXXXXX REPLICA 1: SUCCESS: " + g);

    return a && b && c && d && e && f && g;
}
}