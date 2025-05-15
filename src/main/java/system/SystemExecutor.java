package system;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SystemExecutor {
    public static String executeCommand(List<String> command) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.lines().collect(Collectors.joining("\n"));
            process.waitFor();
            return output;
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
     */
    public static String executeCommandString(String commandStr) {
        List<String> command = parseCommand(commandStr);
        if (command.isEmpty()) {
            return "Error: Empty command";
        }
        return executeCommand(command);
    }
}
