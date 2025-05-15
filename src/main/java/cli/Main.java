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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandLine.Command(name = "sysai", mixinStandardHelpOptions = true, description = "System-Aware AI CLI Assistant")
public class Main implements Runnable {
    private static final String CONFIG_PATH = System.getProperty("user.home") + "/.sysai_config.json";

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        JSONObject config = loadConfig();
        boolean configChanged = false;
        String osType = System.getProperty("os.name");
        String shell = System.getenv("SHELL");
        if (shell == null) shell = "bash";
        String systemDetails = "OS: " + osType + ", Shell: " + shell;

        if (config == null) {
            config = new JSONObject();
            configChanged = true;
        }

        System.out.println("Welcome to System-Aware AI CLI Assistant!");
        setupConfig(scanner, config);

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
                setupConfig(scanner, config);
                continue;
            }

            // Create AI handler with current config
            AIHandler aiHandler = new AIHandler(
                config.getString("provider"),
                config.getString("model"),
                config.getString("apiKey")
            );

            // Start the iterative process
            boolean problemResolved = false;
            StringBuilder contextHistory = new StringBuilder(systemDetails);
            contextHistory.append("\nUser query: ").append(query);
            
            while (!problemResolved) {
                // First, ask AI what commands are needed to understand the situation
                String diagPrompt = "I need to " + query + ". What bash commands should I run to gather enough information about the current system state to understand the situation? Reply with ONLY the commands, one per line.";
                String response = aiHandler.sendQuery(diagPrompt);
                System.out.println("AI: I need to gather some information about your system. Running diagnostic commands...");
                
                // Execute diagnostic commands
                String[] commands = extractCommands(response);
                for (String command : commands) {
                    if (command.trim().isEmpty()) continue;
                    
                    if (SafetyChecker.isDangerous(command)) {
                        System.out.println("[WARNING] This diagnostic command is considered dangerous: " + command);
                        System.out.print("Do you want to proceed? (yes/no): ");
                        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                            System.out.println("Command skipped.");
                            continue;
                        }
                    }
                    
                    System.out.println("Executing: " + command);
                    String output = SystemExecutor.executeCommandString(command);
                    System.out.println(output);
                    contextHistory.append("\nCommand: ").append(command).append("\nOutput: ").append(output);
                }
                
                // Now ask AI for solution based on gathered information
                String solutionPrompt = "Based on the information gathered:\n" + contextHistory + 
                    "\n\nWhat commands should I run to solve the following problem: " + query + 
                    "\n\nProvide your response in this format:" +
                    "\nCOMMAND: the_command_to_run" +
                    "\nEXPLANATION: why this command helps" +
                    "\n(repeat for each command)" +
                    "\nFinally, end with either \"PROBLEM_RESOLVED: YES\" or \"PROBLEM_RESOLVED: NO, because...\"";
                
                response = aiHandler.sendQuery(solutionPrompt);
                System.out.println("AI: " + response);
                
                // Execute solution commands
                Pattern commandPattern = Pattern.compile("COMMAND:\\s*([^\\n]+)");
                Pattern explanationPattern = Pattern.compile("EXPLANATION:\\s*([^\\n]+)");
                Pattern resolvedPattern = Pattern.compile("PROBLEM_RESOLVED:\\s*(YES|NO[^\\n]*)");
                
                Matcher commandMatcher = commandPattern.matcher(response);
                Matcher explanationMatcher = explanationPattern.matcher(response);
                Matcher resolvedMatcher = resolvedPattern.matcher(response);
                
                while (commandMatcher.find()) {
                    String command = commandMatcher.group(1).trim();
                    String explanation = "No explanation provided";
                    
                    if (explanationMatcher.find()) {
                        explanation = explanationMatcher.group(1).trim();
                    }
                    
                    System.out.println("\nCommand: " + command);
                    System.out.println("Explanation: " + explanation);
                    
                    if (SafetyChecker.isDangerous(command)) {
                        System.out.println("[WARNING] This command is considered dangerous: " + command);
                        System.out.print("Do you want to proceed? (yes/no): ");
                        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                            System.out.println("Command skipped.");
                            continue;
                        }
                    }
                    
                    System.out.print("Execute this command? (yes/no): ");
                    if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                        System.out.println("Executing: " + command);
                        String output = SystemExecutor.executeCommandString(command);
                        System.out.println(output);
                        contextHistory.append("\nExecuted: ").append(command).append("\nOutput: ").append(output);
                    } else {
                        System.out.println("Command skipped.");
                        contextHistory.append("\nSkipped: ").append(command);
                    }
                }
                
                // Check if problem is resolved
                if (resolvedMatcher.find()) {
                    String resolution = resolvedMatcher.group(1);
                    if (resolution.startsWith("YES")) {
                        System.out.println("\nAI indicates the problem has been resolved.");
                        problemResolved = true;
                    } else {
                        System.out.println("\nProblem not yet resolved: " + resolution);
                        System.out.print("Continue with next iteration? (yes/no): ");
                        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                            System.out.println("Process terminated by user.");
                            problemResolved = true;
                        }
                    }
                } else {
                    System.out.print("\nAI didn't clearly indicate if the problem is resolved. Continue? (yes/no): ");
                    if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                        System.out.println("Process terminated by user.");
                        problemResolved = true;
                    }
                }
            }
        }
        
        System.out.println("Goodbye!");
    }
    
    private String[] extractCommands(String response) {
        // Remove any explanatory text and keep only lines that look like commands
        String[] lines = response.split("\\n");
        StringBuilder commandsBuilder = new StringBuilder();
        
        for (String line : lines) {
            line = line.trim();
            // Skip empty lines or lines that are clearly not commands
            if (line.isEmpty() || line.startsWith("Here") || line.startsWith("First") || 
                line.contains("explanation") || line.startsWith("These commands") ||
                line.startsWith("The following")) {
                continue;
            }
            
            // Only include lines that look like shell commands
            if (line.matches("^[a-zA-Z0-9_.\\-/\\s]+.*")) {
                commandsBuilder.append(line).append("\n");
            }
        }
        
        return commandsBuilder.toString().split("\\n");
    }
    
    private void setupConfig(Scanner scanner, JSONObject config) {
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
            saveConfig(config);
        }
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
