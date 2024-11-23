package derms.replica.replica1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public abstract class CLI implements Runnable {
	protected Map<String, Command> commands = new HashMap<String, Command>();
	protected List<Description> cmdDescriptions = new ArrayList<Description>();
	protected List<Description> argDescriptions = new ArrayList<Description>();

	protected CLI() {
		commands.put("quit", new Quit());
		cmdDescriptions.add(new Description("quit", "Exit the program"));

		commands.put("help", new Help());
		cmdDescriptions.add(new Description("help", "List commands"));
	}

	@Override
	public void run() {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Type 'help' for a list of commands.");
		for (;;) {
			System.out.print("Command: ");
			String input = scanner.nextLine();
			String[] fields = input.split(" ");
			if (fields.length < 1 || fields[0] == "") {
				continue;
			}
			Command cmd = commands.get(fields[0]);
			if (cmd == null) {
				System.out.println("Invalid command '"+fields[0]+"'");
				System.out.println("Type 'help' for a list of commands.");
				continue;
			}
			String[] args = null;
			if (fields.length < 2) {
				args = new String[0];
			} else {
				args = Arrays.copyOfRange(fields, 1, fields.length);
			}
			cmd.exec(args);
		}
	}

	protected interface Command {
		public void exec(String[] args);
	}

	protected class Quit implements Command {
		@Override
		public void exec(String[] args) {
			System.out.println("Shutting down...");
			System.exit(1);
		}
	}

	protected class Help implements Command {
		@Override
		public void exec(String[] args) {
			System.out.println("\nCommands:");
			for (Description d : cmdDescriptions) {
				System.out.println(d);
			}
			System.out.println("\nArguments:");
			for (Description d : argDescriptions) {
				System.out.println(d);
			}
			System.out.println();
		}
	}

	protected class Description {
		String object; /// The thing being described
		String description;

		protected Description(String object, String description) {
			this.object = object;
			this.description = description;
		}

		@Override
		public String toString() {
			return object+"\n\t"+description;
		}
	}
}
