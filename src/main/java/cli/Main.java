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
        boolean initialDetailsSent = false;
        String osType = System.getProperty("os.name");
        String shell = System.getenv("SHELL");
        if (shell == null) shell = "bash";
        String systemDetails = "OS: " + osType + ", Shell: " + shell;

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
                saveConfig(config);
            }
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
                saveConfig(config);
                continue;
            }
            AIHandler aiHandler = new AIHandler(
                config.getString("provider"),
                config.getString("model"),
                config.getString("apiKey")
            );
            if (!initialDetailsSent) {
                // Ask AI: What commands should I run to gather system details for your task?
                String askForCommands = "I want to accomplish: '" + query + "'. What bash commands should I run to collect all the system details you need? Please reply with only the commands, separated by newlines.";
                String aiResponse = aiHandler.sendQuery(askForCommands);
                System.out.println("AI: " + aiResponse);
                // Extract commands (each line is a command)
                String[] lines = aiResponse.split("\\n");
                StringBuilder detailsBuilder = new StringBuilder(systemDetails);
                // Only execute lines that look like shell commands (skip plain text)
                for (String line : lines) {
                    String cmd = line.trim();
                    if (cmd.isEmpty() || cmd.toLowerCase().contains("explanation") || !cmd.matches("[a-zA-Z0-9_./ -]+")) continue;
                    System.out.println("Executing: " + cmd);
                    String output = SystemExecutor.executeCommand(Arrays.asList(cmd.split(" ")));
                    System.out.println(output);
                    detailsBuilder.append("\nCommand: ").append(cmd).append("\nOutput: ").append(output);
                }
                // Now send the system details and user goal to the AI
                String detailsPrompt = "Here are my system details and outputs of your requested commands:\n" + detailsBuilder + "\nNow, what bash commands should I run to accomplish: '" + query + "'? Please reply with commands and explanations.";
                String response = aiHandler.sendQuery(detailsPrompt);
                System.out.println("AI: " + response);
                initialDetailsSent = true;
                // Continue to normal command execution below
                if (response.toLowerCase().contains("run command")) {
                    String[] commandBlocks = response.split("Run command:");
                    for (int i = 1; i < commandBlocks.length; i++) {
                        String block = commandBlocks[i].trim();
                        String[] parts = block.split("Explanation:");
                        String commandText = parts[0].trim();
                        String explanation = parts.length > 1 ? parts[1].trim() : "No explanation provided.";
                        String[] commands = commandText.split("&&|\n");
                        for (String command : commands) {
                            command = command.trim();
                            if (command.isEmpty()) continue;
                            System.out.println("Explanation: " + explanation);
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
                continue;
            }
            String response = aiHandler.sendQuery(query);
            System.out.println("AI: " + response);

            if (response.toLowerCase().contains("run command")) {
                String[] parts = response.split("Explanation:");
                String commandText = parts[0].substring(parts[0].indexOf(":") + 1).trim();
                String explanation = parts.length > 1 ? parts[1].trim() : "No explanation provided.";
                String[] commands = commandText.split("&&|\n");
                for (String command : commands) {
                    command = command.trim();
                    if (command.isEmpty()) continue;
                    System.out.println("Explanation: " + explanation);
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
