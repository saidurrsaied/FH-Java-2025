package logging;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;

public class LoggerSelfTest {
    private static int passed=0, failed=0;

    public static void main(String[] args) throws Exception {
        clean();
        testRobotLogCreated();
        clean();
        testChargingLogCreated();
        clean();
        testSystemAndComponentWritten();
        clean();
        testFallbackOnIOException();
        testFallbackAppends();
        System.out.println("PASSED: "+passed+"  FAILED: "+failed);
        if (failed>0) System.exit(1);
    }

    static void ok(boolean cond, String name) {
        if (cond) { passed++; System.out.println("✔ " + name); }
        else { failed++; System.out.println("✘ " + name); }
    }

    static String today() {
        return java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yy"));
    }

    static void testRobotLogCreated() {
        Logger lg = new Logger();
        lg.log_print("INFO","robot","start");
        ok(Files.isDirectory(Path.of("Logging/Robot")), "Robot folder created");
        ok(Files.exists(Path.of("Logging/Robot/"+today()+".txt")), "Robot file created");
    }

    static void testChargingLogCreated() {
        Logger lg = new Logger();
        lg.log_print("INFO","charging","ready");
        ok(Files.exists(Path.of("Logging/Charging_Station/"+today()+".txt")), "Charging file created");
    }

    static void testSystemAndComponentWritten() throws Exception {
        Logger lg = new Logger();
        lg.log_print("INFO","inventory","scan");
        Path comp = Path.of("Logging/Inventory/"+today()+".txt");
        Path sys  = Path.of("Logging/System/"+today()+".txt");
        ok(Files.exists(comp),"Inventory file exists");
        ok(Files.exists(sys),"System file exists");
        ok(Files.size(comp)>0 && Files.size(sys)>0,"Both non-empty");
    }
    
    //abnormal TM
    // Simulate IO error: make "Logging" a normal file instead of a directory
    static void testFallbackOnIOException() throws Exception {
        Path logging = Path.of("Logging");
        if (Files.exists(logging)) deleteRec(logging);
        Files.writeString(logging,"not a folder");
        Logger lg = new Logger();
        lg.log_print("ERROR","robot","boom");
        ok(Files.exists(Path.of("logger_error.txt")),"Fallback file created");
    }
    
    //abnormal TM
    static void testFallbackAppends() throws Exception {
        Files.writeString(Path.of("logger_error.txt"),"Appends error\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        Logger lg = new Logger();
        
        Path logging = Path.of("Logging");
        if (Files.exists(logging)) deleteRec(logging);
        Files.writeString(logging,"not a folder");
        lg.log_print("ERROR","charging","again");
        long lines = Files.lines(Path.of("logger_error.txt")).count();
        ok(lines>=2,"Fallback appends");
    }

    // helpers
    static void clean() throws Exception {
        deleteRec(Path.of("Logging"));
        Files.deleteIfExists(Path.of("logger_error.txt"));
    }
    static void deleteRec(Path p) throws Exception {
        if (!Files.exists(p)) return;
        Files.walk(p).sorted((a,b)->b.compareTo(a)).forEach(q->{ try { Files.delete(q);} catch(Exception ignored){} });
    }
}
