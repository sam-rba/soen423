package derms.replica.replica1;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DermsLogger {
	public static final String logFile = "server.log";

	private static Logger log = null;

	public static Logger getLogger(Class clazz) throws IOException {
		if (log == null) {
			log = Logger.getLogger(clazz.getName());
			Handler fileHandler = new FileHandler(logFile);
			fileHandler.setFormatter(new SimpleFormatter());
			log.addHandler(fileHandler);
		}
		return log;
	}
}
