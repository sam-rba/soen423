package derms.replica3;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.Date;

public class Logger {
    private FileWriter fileWriter = null;
    private BufferedWriter bufferedWriter = null;
    private PrintWriter printWriter = null;

    public Logger(final String activityLoggerFile) throws IOException {
        fileWriter = new FileWriter(activityLoggerFile, true);
        bufferedWriter = new BufferedWriter(fileWriter);
        printWriter = new PrintWriter(bufferedWriter);
    }

    public synchronized void log( String action, String status, String res) {
        try {
            final String dataLog =  DateFormat.getDateTimeInstance().format(new Date()) + " [" +
                    action + "] : [" + status + "] - " + res;

            printWriter.println(dataLog);
            System.out.println(dataLog);
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void clientLog(final String userId, final String action, final String message) {
        try {
            final String dataLog = DateFormat.getDateTimeInstance().format(new Date()) + " [" + userId + "] [" +
                    action + "] - " + message;
            printWriter.println(dataLog);
            System.out.println(dataLog);
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void clientLog(final String userId, final String action) {
        try {
            final String dataLog = DateFormat.getDateTimeInstance().format(new Date()) +  " [" + userId + "] " + " [" +
                    action + "] ";
            printWriter.println(dataLog);
            System.out.println(dataLog);
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
