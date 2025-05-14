package cli;

import picocli.CommandLine;
import java.util.Scanner;
import java.nio.file.*;
import java.io.*;
import ai.AIHandler;
import org.json.JSONObject;
import system.SystemExecutor;
import safety.SafetyChecker;
import java.util.Arrays;

@CommandLine.Command(name = "sysai", mixinStandardHelpOptions = true, description = "System-Aware AI CLI Assistant")
public class Main implements Runnable {
    private static final String CONFIG_PATH = System.getProperty("user.home") + "/.sysai_config.json";

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        JSONObject config = loadConfig();
        boolean configChanged = false;

        if (config == null) {
            config = new JSONObject();
            configChanged = true;
        }

        System.out.println("Welcome to System-Aware AI CLI Assistant!");
        if (!config.has("provider") || !config.has("model") || !config.has("apiKey")) {
            System.out.println("Choose your AI provider:");
            System.out.println("1. OpenAI");
            System.out.println("2. Gemini");
            System.out.print("Enter choice [1-2]: ");
            int providerChoice = Integer.parseInt(scanner.nextLine().trim());
            String provider = providerChoice == 2 ? "Gemini" : "OpenAI";
            config.put("provider", provider);

            System.out.print("Enter model name (e.g., gpt-4, gemini-pro): ");
            config.put("model", scanner.nextLine().trim());

            System.out.print("Enter API key: ");
            config.put("apiKey", scanner.nextLine().trim());
            configChanged = true;
        }

        if (configChanged) saveConfig(config);

        while (true) {
            System.out.println("\nCurrent provider: " + config.getString("provider") + ", model: " + config.getString("model"));
            System.out.println("Type 'change' to update provider/model/API key, or 'exit' to quit.");
            System.out.print("> ");
            String query = scanner.nextLine();
            if (query.equalsIgnoreCase("exit")) break;
            if (query.equalsIgnoreCase("change")) {
                config.remove("provider");
                config.remove("model");
                config.remove("apiKey");
                configChanged = true;
                continue;
            }
            AIHandler aiHandler = new AIHandler(
                config.getString("provider"),
                config.getString("model"),
                config.getString("apiKey")
            );
            String response = aiHandler.sendQuery(query);
            System.out.println("AI: " + response);

            if (response.toLowerCase().contains("run command")) {
                String[] commands = {"echo Hello from system!", "ls -l"};
                for (String command : commands) {
                    if (SafetyChecker.isDangerous(command)) {
                        System.out.println("[WARNING] This command is considered dangerous: " + command);
                        System.out.print("Do you want to proceed? (yes/no): ");
                        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                            System.out.println("Command skipped.");
                            continue;
                        }
                    }
                    System.out.println("Executing: " + command);
                    String output = SystemExecutor.executeCommand(Arrays.asList(command.split(" ")));
                    System.out.println(output);
                }
            }
        }
        System.out.println("Goodbye!");
    }

    private JSONObject loadConfig() {
        try {
            String content = Files.readString(Path.of(CONFIG_PATH));
            return new JSONObject(content);
        } catch (Exception e) {
            return null;
        }
    }

    private void saveConfig(JSONObject config) {
        try {
            Files.writeString(Path.of(CONFIG_PATH), config.toString(2));
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
