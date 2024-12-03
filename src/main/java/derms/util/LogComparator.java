package derms.util;

import java.io.*;
import java.nio.file.*;

public class LogComparator {

public static boolean containsSuccess(String filePath) throws IOException {
    boolean a = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("RESPONDER SUCCESS"));
    boolean b = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("COORDINATOR SUCCESS"));

    return a && b;
}

public static boolean containsCrashTrue(String filePath) throws IOException {
    boolean a = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 1: {CRASH: TRUE}"));
    boolean b = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 1: {CRASH: DETECTED}"));
    boolean c = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 1: {RESTARTED}"));
    boolean d = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("RESPONDER SUCCESS"));
    boolean e = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("COORDINATOR SUCCESS"));

    System.out.println("XXXXXXXXX REPLICA 1: {CRASH: TRUE}: " + a);
    System.out.println("XXXXXXXXX REPLICA 1: {CRASH: DETECTED}: " + b);
    System.out.println("XXXXXXXXX REPLICA 1: {RESTARTED}: " + c);
    System.out.println("XXXXXXXXX REPLICA 1: RESPONDER SUCCESS: " + d);
    System.out.println("XXXXXXXXX REPLICA 1: COORDINATOR SUCCESS: " + e);

    return a && b && c && d && e;
}

public static boolean containsByzTrue(String filePath) throws IOException {
    boolean a = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 1: {BYZANTINE: TRUE}"));
    boolean b = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 1: {BYZANTINE: DETECTED}"));
    boolean c = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 1: {RESTARTED}"));
    boolean d = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("RESPONDER SUCCESS"));
    boolean e = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("COORDINATOR SUCCESS"));

    System.out.println("XXXXXXXXX REPLICA 1: {BYZANTINE: TRUE}: " + a);
    System.out.println("XXXXXXXXX REPLICA 1: {BYZANTINE: DETECTED}: " + b);
    System.out.println("XXXXXXXXX REPLICA 1: {RESTARTED}: " + c);
    System.out.println("XXXXXXXXX REPLICA 1: RESPONDER SUCCESS: " + d);
    System.out.println("XXXXXXXXX REPLICA 1: COORDINATOR SUCCESS: " + e);

    return a && b && c && d && e;
}

public static boolean containsCombTrue(String filePath) throws IOException {
    boolean a = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 1: {BYZANTINE: TRUE}"));
    boolean b = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 1: {BYZANTINE: DETECTED}"));
    boolean c = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 1: {RESTARTED}"));
    boolean d = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 3: {CRASH: TRUE}"));
    boolean e = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 3: {CRASH: DETECTED}"));
    boolean f = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("REPLICA 3: {RESTARTED}"));
    boolean g = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("RESPONDER SUCCESS"));
    boolean h = Files.lines(Paths.get(filePath)).anyMatch(line -> line.contains("COORDINATOR SUCCESS"));

    System.out.println("XXXXXXXXX REPLICA 1: {BYZANTINE: TRUE}: " + a);
    System.out.println("XXXXXXXXX REPLICA 1: {BYZANTINE: DETECTED}: " + b);
    System.out.println("XXXXXXXXX REPLICA 1: {RESTARTED}: " + c);
    System.out.println("XXXXXXXXX REPLICA 3: {CRASH: TRUE}: " + d);
    System.out.println("XXXXXXXXX REPLICA 3: {CRASH: DETECTED}: " + e);
    System.out.println("XXXXXXXXX REPLICA 3: {RESTARTED}: " + f);
    System.out.println("XXXXXXXXX RESPONDER SUCCESS: " + g);
    System.out.println("XXXXXXXXX COORDINATOR SUCCESS: " + h);

    return a && b && c && d && e && f && g && h;
}
}