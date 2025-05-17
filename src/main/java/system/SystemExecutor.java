package system;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SystemExecutor {
    // Default command timeout (in seconds)
    private static final int DEFAULT_TIMEOUT = 30;
    
    /**
     * Execute a command with default timeout
     * @param command List of command arguments
     * @return Output of the command
     */
    public static String executeCommand(List<String> command) {
        return executeCommand(command, DEFAULT_TIMEOUT);
    }
    
    /**
     * Execute a command with specified timeout
     * @param command List of command arguments
     * @param timeoutSeconds Timeout in seconds
     * @return Output of the command
     */
    public static String executeCommand(List<String> command, int timeoutSeconds) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            
            // Read output asynchronously
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            // Wait for process to complete with timeout
            boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            
            if (!completed) {
                process.destroyForcibly();
                return "Command timed out after " + timeoutSeconds + " seconds";
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                return output.toString() + "\nCommand exited with code " + exitCode;
            }
            
            return output.toString();
        } catch (Exception e) {
            return "Error executing command: " + e.getMessage();
        }
    }
    
    /**
     * Parses a command string into a list of arguments, preserving quoted strings
     * This is more robust than simple splitting by spaces
     */
    public static List<String> parseCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        boolean escapeNext = false;
        
        for (char c : command.toCharArray()) {
            if (escapeNext) {
                currentToken.append(c);
                escapeNext = false;
            } else if (c == '\\') {
                escapeNext = true;
            } else if (c == '\'' && !inDoubleQuotes) {
                inSingleQuotes = !inSingleQuotes;
            } else if (c == '"' && !inSingleQuotes) {
                inDoubleQuotes = !inDoubleQuotes;
            } else if (c == ' ' && !inSingleQuotes && !inDoubleQuotes) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
            } else {
                currentToken.append(c);
            }
        }
        
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }
        
        // Clean up any quote characters that might be left in the tokens
        List<String> cleanedTokens = new ArrayList<>();
        for (String token : tokens) {
            if ((token.startsWith("\"") && token.endsWith("\"")) || 
                (token.startsWith("'") && token.endsWith("'"))) {
                cleanedTokens.add(token.substring(1, token.length() - 1));
            } else {
                cleanedTokens.add(token);
            }
        }
        
        return cleanedTokens;
    }
    
    /**
     * Execute a command string, parsing it correctly with quoted arguments
     * @param commandStr Command string to execute
     * @return Output of the command
     */
    public static String executeCommandString(String commandStr) {
        return executeCommandString(commandStr, DEFAULT_TIMEOUT);
    }
    
    /**
     * Execute a command string with specified timeout
     * @param commandStr Command string to execute
     * @param timeoutSeconds Timeout in seconds
     * @return Output of the command
     */
    public static String executeCommandString(String commandStr, int timeoutSeconds) {
        // Handle shell operators for complex commands
        if (commandStr.contains("|") || commandStr.contains(">") || 
            commandStr.contains("&&") || commandStr.contains(";")) {
            return executeShellCommand(commandStr, timeoutSeconds);
        }
        
        List<String> command = parseCommand(commandStr);
        if (command.isEmpty()) {
            return "Error: Empty command";
        }
        
        return executeCommand(command, timeoutSeconds);
    }
    
    /**
     * Execute a command using the system shell for complex commands with pipes, redirects, etc.
     * @param commandStr Command string to execute
     * @param timeoutSeconds Timeout in seconds
     * @return Output of the command
     */
    private static String executeShellCommand(String commandStr, int timeoutSeconds) {
        try {
            // Determine which shell to use based on OS
            List<String> command = new ArrayList<>();
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                command.add("cmd.exe");
                command.add("/c");
            } else {
                String shell = System.getenv("SHELL");
                if (shell == null || shell.isEmpty()) {
                    shell = "/bin/sh";
                }
                command.add(shell);
                command.add("-c");
            }
            
            command.add(commandStr);
            return executeCommand(command, timeoutSeconds);
        } catch (Exception e) {
            return "Error executing shell command: " + e.getMessage();
        }
    }
    
    /**
     * Check if a command exists in the system PATH
     * @param command The command to check
     * @return true if the command exists and is executable
     */
    public static boolean commandExists(String command) {
        try {
            String checkCommand = System.getProperty("os.name").toLowerCase().contains("win") 
                ? "where " + command 
                : "which " + command;
            
            Process process = Runtime.getRuntime().exec(checkCommand);
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
