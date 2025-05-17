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
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandLine.Command(name = "sysai", mixinStandardHelpOptions = true, description = "System-Aware AI CLI Assistant")
public class Main implements Runnable {
    private static final String CONFIG_PATH = System.getProperty("user.home") + "/.sysai_config.json";

    // ANSI color codes for better terminal output
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_BLUE = "\u001B[34m";
    
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

        System.out.println(ANSI_GREEN + "Welcome to System-Aware AI CLI Assistant!" + ANSI_RESET);
        setupConfig(scanner, config);

        while (true) {
            System.out.println("\nCurrent provider: " + ANSI_BLUE + config.getString("provider") + ANSI_RESET + 
                              ", model: " + ANSI_BLUE + config.getString("model") + ANSI_RESET);
            System.out.println("Type " + ANSI_YELLOW + "'change'" + ANSI_RESET + 
                              " to update provider/model/API key, or " + ANSI_YELLOW + "'exit'" + ANSI_RESET + " to quit.");
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
                System.out.println(ANSI_BLUE + "\n[Diagnostic Phase]" + ANSI_RESET + " Analyzing your problem...");
                
                String diagPrompt = "You are a system diagnostic expert. " +
                    "I need to " + query + ". " +
                    "What Linux bash commands should I run to gather sufficient information about the current system state to understand " +
                    "and diagnose this problem effectively? Consider checking relevant logs, processes, system information, " +
                    "configurations, etc. that could help diagnose this specific issue. " +
                    "For each command, provide a brief explanation of why it's useful. " +
                    "Format your response as follows:\n" +
                    "COMMAND: the_command\n" +
                    "PURPOSE: why this command helps diagnose the problem\n" +
                    "(Repeat for 3-5 most useful diagnostic commands)";
                    
                String response = aiHandler.sendQuery(diagPrompt);
                
                // Extract commands and their explanations
                List<String> diagnosticCommands = new ArrayList<>();
                List<String> commandPurposes = new ArrayList<>();
                
                Pattern cmdPattern = Pattern.compile("COMMAND:\\s*([^\\n]+)");
                Pattern purposePattern = Pattern.compile("PURPOSE:\\s*([^\\n]+)");
                
                Matcher cmdMatcher = cmdPattern.matcher(response);
                Matcher purposeMatcher = purposePattern.matcher(response);
                
                while (cmdMatcher.find()) {
                    String cmd = cmdMatcher.group(1).trim();
                    diagnosticCommands.add(cmd);
                    
                    String purpose = "No explanation provided";
                    if (purposeMatcher.find()) {
                        purpose = purposeMatcher.group(1).trim();
                    }
                    commandPurposes.add(purpose);
                }
                
                // If no commands were found in the structured format, fall back to extracting commands line by line
                if (diagnosticCommands.isEmpty()) {
                    diagnosticCommands = Arrays.asList(extractCommands(response));
                    for (int i = 0; i < diagnosticCommands.size(); i++) {
                        commandPurposes.add("Diagnostic command");
                    }
                }
                
                System.out.println(ANSI_GREEN + "AI: I need to gather information about your system to diagnose the problem." + ANSI_RESET);
                System.out.println("I'll run the following diagnostic commands:");
                
                // Execute diagnostic commands
                for (int i = 0; i < diagnosticCommands.size(); i++) {
                    String command = diagnosticCommands.get(i);
                    String purpose = commandPurposes.get(i);
                    
                    if (command.trim().isEmpty()) continue;
                    
                    System.out.println("\n" + ANSI_BLUE + "Diagnostic Command " + (i+1) + ":" + ANSI_RESET + " " + command);
                    System.out.println(ANSI_YELLOW + "Purpose:" + ANSI_RESET + " " + purpose);
                    
                    if (SafetyChecker.isDangerous(command)) {
                        String reason = SafetyChecker.getDangerReason(command);
                        System.out.println(ANSI_RED + "[WARNING] This diagnostic command is considered potentially dangerous." + ANSI_RESET);
                        if (reason != null) {
                            System.out.println(ANSI_RED + "Reason: " + reason + ANSI_RESET);
                        }
                        System.out.print("Do you want to proceed? (yes/no): ");
                        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                            System.out.println("Command skipped.");
                            contextHistory.append("\nCommand (skipped - flagged as dangerous): ").append(command);
                            continue;
                        }
                    }
                    
                    try {
                        System.out.println(ANSI_BLUE + "Executing: " + ANSI_RESET + command);
                        String output = SystemExecutor.executeCommandString(command);
                        
                        // Format and limit the output
                        String formattedOutput = formatCommandOutput(output);
                        System.out.println(formattedOutput);
                        
                        contextHistory.append("\nCommand: ").append(command)
                                     .append("\nOutput: ").append(output);
                    } catch (Exception e) {
                        String errorMsg = "Error executing command: " + e.getMessage();
                        System.out.println(ANSI_RED + errorMsg + ANSI_RESET);
                        contextHistory.append("\nCommand (failed): ").append(command)
                                     .append("\nError: ").append(errorMsg);
                    }
                }
                
                // Now ask AI for solution based on gathered information
                System.out.println(ANSI_BLUE + "\n[Solution Phase]" + ANSI_RESET + " Analyzing diagnostic information...");
                
                String solutionPrompt = "Based on the diagnostic information gathered:\n" + contextHistory + 
                    "\n\nWhat commands should I run to solve the following problem: " + query + 
                    "\n\nFor each recommended command, explain clearly why it helps solve the problem and what it does." +
                    "\nProvide your response in this format:" +
                    "\nCOMMAND: the_command_to_run" +
                    "\nEXPLANATION: detailed explanation of what this command does and why it helps" +
                    "\n(repeat for each command)" +
                    "\nFinally, end with either \"PROBLEM_RESOLVED: YES\" or \"PROBLEM_RESOLVED: NO, because...\"";
                
                response = aiHandler.sendQuery(solutionPrompt);
                
                System.out.println(ANSI_GREEN + "AI: Based on the diagnostic information, here's my solution:" + ANSI_RESET);
                System.out.println(response);
                
                // Execute solution commands
                Pattern commandPattern = Pattern.compile("COMMAND:\\s*([^\\n]+)");
                Pattern explanationPattern = Pattern.compile("EXPLANATION:\\s*([^\\n]+)");
                Pattern resolvedPattern = Pattern.compile("PROBLEM_RESOLVED:\\s*(YES|NO[^\\n]*)");
                
                Matcher commandMatcher = commandPattern.matcher(response);
                Matcher explanationMatcher = explanationPattern.matcher(response);
                Matcher resolvedMatcher = resolvedPattern.matcher(response);
                
                List<String> commands = new ArrayList<>();
                List<String> explanations = new ArrayList<>();
                
                while (commandMatcher.find()) {
                    commands.add(commandMatcher.group(1).trim());
                    
                    String explanation = "No explanation provided";
                    if (explanationMatcher.find()) {
                        explanation = explanationMatcher.group(1).trim();
                    }
                    explanations.add(explanation);
                }
                
                for (int i = 0; i < commands.size(); i++) {
                    String command = commands.get(i);
                    String explanation = explanations.get(i);
                    
                    System.out.println("\n" + ANSI_BLUE + "Solution Command " + (i+1) + ":" + ANSI_RESET + " " + command);
                    System.out.println(ANSI_YELLOW + "Explanation:" + ANSI_RESET + " " + explanation);
                    
                    if (SafetyChecker.isDangerous(command)) {
                        String reason = SafetyChecker.getDangerReason(command);
                        System.out.println(ANSI_RED + "[WARNING] This command is considered potentially dangerous." + ANSI_RESET);
                        if (reason != null) {
                            System.out.println(ANSI_RED + "Reason: " + reason + ANSI_RESET);
                        }
                        System.out.print("Do you want to proceed? (yes/no): ");
                        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                            System.out.println("Command skipped.");
                            contextHistory.append("\nSkipped: ").append(command);
                            continue;
                        }
                    }
                    
                    System.out.print("Execute this command? (yes/no): ");
                    if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                        try {
                            System.out.println(ANSI_BLUE + "Executing: " + ANSI_RESET + command);
                            String output = SystemExecutor.executeCommandString(command);
                            
                            // Format and limit the output
                            String formattedOutput = formatCommandOutput(output);
                            System.out.println(formattedOutput);
                            
                            contextHistory.append("\nExecuted: ").append(command)
                                         .append("\nOutput: ").append(output);
                        } catch (Exception e) {
                            String errorMsg = "Error executing command: " + e.getMessage();
                            System.out.println(ANSI_RED + errorMsg + ANSI_RESET);
                            contextHistory.append("\nCommand (failed): ").append(command)
                                         .append("\nError: ").append(errorMsg);
                        }
                    } else {
                        System.out.println("Command skipped.");
                        contextHistory.append("\nSkipped: ").append(command);
                    }
                }
                
                // Check if problem is resolved
                if (resolvedMatcher.find()) {
                    String resolution = resolvedMatcher.group(1);
                    if (resolution.startsWith("YES")) {
                        System.out.println("\n" + ANSI_GREEN + "✅ AI indicates the problem has been resolved." + ANSI_RESET);
                        problemResolved = true;
                    } else {
                        System.out.println("\n" + ANSI_YELLOW + "⚠️ Problem not yet resolved: " + resolution + ANSI_RESET);
                        System.out.print("Continue with next iteration? (yes/no): ");
                        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                            System.out.println("Process terminated by user.");
                            problemResolved = true;
                        } else {
                            System.out.println("\nStarting next iteration...");
                        }
                    }
                } else {
                    System.out.print("\n" + ANSI_YELLOW + "⚠️ AI didn't clearly indicate if the problem is resolved. Continue? (yes/no): " + ANSI_RESET);
                    if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                        System.out.println("Process terminated by user.");
                        problemResolved = true;
                    } else {
                        System.out.println("\nStarting next iteration...");
                    }
                }
            }
        }
        
        System.out.println(ANSI_GREEN + "Goodbye!" + ANSI_RESET);
    }
    
    /**
     * Format command output to be more readable
     * - Limit length if too long
     * - Add indicators for truncation
     */
    private String formatCommandOutput(String output) {
        final int MAX_LINES = 20;
        final int MAX_LINE_LENGTH = 100;
        
        if (output == null || output.trim().isEmpty()) {
            return "(No output)";
        }
        
        String[] lines = output.split("\\n");
        StringBuilder formatted = new StringBuilder();
        
        int linesToShow = Math.min(lines.length, MAX_LINES);
        
        for (int i = 0; i < linesToShow; i++) {
            String line = lines[i];
            if (line.length() > MAX_LINE_LENGTH) {
                line = line.substring(0, MAX_LINE_LENGTH) + "...";
            }
            formatted.append(line).append("\n");
        }
        
        if (lines.length > MAX_LINES) {
            formatted.append("... (").append(lines.length - MAX_LINES).append(" more lines not shown)");
        }
        
        return formatted.toString();
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
                line.startsWith("The following") || line.startsWith("COMMAND:") || 
                line.startsWith("PURPOSE:") || line.contains("```")) {
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
            System.out.println(ANSI_BLUE + "Choose your AI provider:" + ANSI_RESET);
            System.out.println("1. OpenAI");
            System.out.println("2. Gemini");
            System.out.print("Enter choice [1-2]: ");
            int providerChoice = Integer.parseInt(scanner.nextLine().trim());
            String provider = providerChoice == 2 ? "Gemini" : "OpenAI";
            config.put("provider", provider);

            System.out.print("Enter model name (e.g., " + 
                             (provider.equals("OpenAI") ? "gpt-4, gpt-3.5-turbo" : "gemini-pro, gemini-1.5-pro") + 
                             "): ");
            config.put("model", scanner.nextLine().trim());

            System.out.print("Enter API key: ");
            config.put("apiKey", scanner.nextLine().trim());
            saveConfig(config);
            System.out.println(ANSI_GREEN + "Configuration saved successfully!" + ANSI_RESET);
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
            System.err.println(ANSI_RED + "Failed to save config: " + e.getMessage() + ANSI_RESET);
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
