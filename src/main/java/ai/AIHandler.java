package ai;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;

public class AIHandler {
    private final String provider;
    private String model;
    private final String apiKey;
    
    // Valid OpenAI models
    private static final List<String> VALID_OPENAI_MODELS = Arrays.asList(
        "gpt-4", "gpt-4-turbo", "gpt-4-0125-preview", "gpt-4-1106-preview", "gpt-4-vision-preview",
        "gpt-3.5-turbo", "gpt-3.5-turbo-0125", "gpt-3.5-turbo-1106"
    );
    
    // Valid Gemini models
    private static final List<String> VALID_GEMINI_MODELS = Arrays.asList(
        "gemini-pro", "gemini-1.0-pro", "gemini-1.5-pro", "gemini-1.5-flash", "gemini-ultra"
    );
    
    // Model autocorrection mappings
    private static final Map<String, String> MODEL_CORRECTIONS = new HashMap<>();
    static {
        // Gemini corrections
        MODEL_CORRECTIONS.put("gemini", "gemini-pro");
        MODEL_CORRECTIONS.put("gemini-1.5", "gemini-1.5-pro");
        MODEL_CORRECTIONS.put("gemini-1.0", "gemini-pro");
        MODEL_CORRECTIONS.put("gemini-ultra-vision", "gemini-ultra");
        
        // OpenAI corrections
        MODEL_CORRECTIONS.put("gpt4", "gpt-4");
        MODEL_CORRECTIONS.put("gpt-4-latest", "gpt-4-turbo");
        MODEL_CORRECTIONS.put("gpt-3.5", "gpt-3.5-turbo");
        MODEL_CORRECTIONS.put("gpt3.5", "gpt-3.5-turbo");
        MODEL_CORRECTIONS.put("gpt3", "gpt-3.5-turbo");
    }

    public AIHandler(String provider, String model, String apiKey) {
        this.provider = provider;
        this.apiKey = apiKey;
        
        // Apply model name corrections if necessary
        if (MODEL_CORRECTIONS.containsKey(model.toLowerCase())) {
            String correctedModel = MODEL_CORRECTIONS.get(model.toLowerCase());
            System.out.println("Note: Corrected model name from '" + model + "' to '" + correctedModel + "'");
            this.model = correctedModel;
        } else {
            this.model = model;
        }
    }
    
    public String getProvider() {
        return provider;
    }
    
    public String getModel() {
        return model;
    }
    
    public String getApiKey() {
        // Return last 4 characters masked with asterisks for security
        if (apiKey.length() > 4) {
            return "*".repeat(apiKey.length() - 4) + apiKey.substring(apiKey.length() - 4);
        }
        return "*".repeat(apiKey.length());
    }

    /**
     * Send a query to the configured AI provider
     * @param query The query text to send
     * @return The AI's response
     */
    public String sendQuery(String query) {
        try {
            if (provider.equalsIgnoreCase("OpenAI")) {
                // Validate OpenAI model
                validateOpenAIModel();
                return callOpenAI(query);
            } else if (provider.equalsIgnoreCase("Gemini")) {
                // Validate Gemini model
                validateGeminiModel();
                return callGemini(query);
            } else {
                return "Unknown provider: " + provider + ". Supported providers are OpenAI and Gemini.";
            }
        } catch (IllegalArgumentException e) {
            return "[ERROR] Invalid configuration: " + e.getMessage();
        } catch (Exception e) {
            return "[ERROR] Failed to contact AI provider: " + e.getMessage() + 
                   ". Please check your internet connection and API key.";
        }
    }
    
    /**
     * Validate that the OpenAI model is valid
     */
    private void validateOpenAIModel() {
        if (!VALID_OPENAI_MODELS.contains(model.toLowerCase())) {
            // Check if it's a known model but with wrong casing
            for (String validModel : VALID_OPENAI_MODELS) {
                if (validModel.equalsIgnoreCase(model)) {
                    model = validModel;
                    System.out.println("Note: Corrected model casing to '" + model + "'");
                    return;
                }
            }
            
            // If not in list but looks like a valid format, just warn but continue
            if (model.startsWith("gpt-")) {
                System.out.println("Warning: Using unofficial OpenAI model '" + model + "'. " +
                                  "This may not work correctly.");
            } else {
                throw new IllegalArgumentException("Invalid OpenAI model: " + model + 
                                                 ". Valid models include: " + String.join(", ", VALID_OPENAI_MODELS));
            }
        }
    }
    
    /**
     * Validate that the Gemini model is valid
     */
    private void validateGeminiModel() {
        if (!VALID_GEMINI_MODELS.contains(model.toLowerCase())) {
            // Check if it's a known model but with wrong casing
            for (String validModel : VALID_GEMINI_MODELS) {
                if (validModel.equalsIgnoreCase(model)) {
                    model = validModel;
                    System.out.println("Note: Corrected model casing to '" + model + "'");
                    return;
                }
            }
            
            // If not in list but looks like a valid format, just warn but continue
            if (model.startsWith("gemini-")) {
                System.out.println("Warning: Using unofficial Gemini model '" + model + "'. " +
                                  "This may not work correctly.");
            } else {
                throw new IllegalArgumentException("Invalid Gemini model: " + model + 
                                                 ". Valid models include: " + String.join(", ", VALID_GEMINI_MODELS));
            }
        }
    }

    /**
     * Call the OpenAI API
     * @param query The query to send
     * @return The AI's response
     */
    private String callOpenAI(String query) throws IOException, URISyntaxException {
        URI uri = new URI("https://api.openai.com/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        // Clean the query to prevent JSON errors
        String cleanedQuery = query.replace("\\", "\\\\")
                               .replace("\"", "\\\"")
                               .replace("\n", "\\n")
                               .replace("\r", "\\r")
                               .replace("\t", "\\t");
        
        String payload = "{" +
                "\"model\": \"" + model + "\"," +
                "\"messages\": [{\"role\": \"user\", \"content\": \"" + cleanedQuery + "\"}]," +
                "\"max_tokens\": 1024," +
                "\"temperature\": 0.2" +
                "}";
                
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes());
        }
        
        int status = conn.getResponseCode();
        
        // Handle error responses
        if (status < 200 || status >= 300) {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                return "[ERROR] OpenAI API error (status " + status + "): " + errorResponse.toString();
            }
        }
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            
            String resp = response.toString();
            
            // Extract the content from the OpenAI response
            int idx = resp.indexOf("\"content\":");
            if (idx != -1) {
                int start = resp.indexOf('"', idx + 11) + 1;
                int end = findMatchingClosingQuote(resp, start);
                if (start > 0 && end > start) {
                    return resp.substring(start, end)
                           .replaceAll("\\\\n", "\n")
                           .replaceAll("\\\\\"", "\"")
                           .replaceAll("\\\\t", "\t")
                           .replaceAll("\\\\r", "\r");
                }
            }
            
            // If we couldn't extract the response, return the full response for debugging
            return "[DEBUG] OpenAI response parsing error. Raw response: " + resp;
        }
    }

    /**
     * Call the Gemini API
     * @param query The query to send
     * @return The AI's response
     */
    private String callGemini(String query) throws IOException, URISyntaxException {
        URI uri = new URI("https://generativelanguage.googleapis.com/v1beta/models/" + 
                          model + ":generateContent?key=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        // Clean the query to prevent JSON errors
        String cleanedQuery = query.replace("\\", "\\\\")
                               .replace("\"", "\\\"")
                               .replace("\n", "\\n")
                               .replace("\r", "\\r")
                               .replace("\t", "\\t");
        
        String payload = "{" +
                "\"contents\": [{\"parts\":[{\"text\": \"" + cleanedQuery + "\"}]}]," +
                "\"generationConfig\": {" +
                "  \"temperature\": 0.2," +
                "  \"maxOutputTokens\": 1024" +
                "}" +
                "}";
                
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes());
        }
        
        int status = conn.getResponseCode();
        
        // Handle error responses
        if (status < 200 || status >= 300) {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                return "[ERROR] Gemini API error (status " + status + "): " + errorResponse.toString();
            }
        }
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            
            String resp = response.toString();
            
            // Try multiple patterns to extract the response text
            
            // First pattern: look for "text" field in the response
            int idx = resp.indexOf("\"text\":");
            if (idx != -1) {
                int start = resp.indexOf('"', idx + 7) + 1;
                int end = findMatchingClosingQuote(resp, start);
                if (start > 0 && end > start) {
                    return resp.substring(start, end)
                           .replaceAll("\\\\n", "\n")
                           .replaceAll("\\\\\"", "\"")
                           .replaceAll("\\\\t", "\t")
                           .replaceAll("\\\\r", "\r");
                }
            }
            
            // Second pattern: try to find any "content" field
            idx = resp.indexOf("\"content\":");
            if (idx != -1) {
                int start = resp.indexOf('"', idx + 11) + 1;
                int end = findMatchingClosingQuote(resp, start);
                if (start > 0 && end > start) {
                    return resp.substring(start, end)
                           .replaceAll("\\\\n", "\n")
                           .replaceAll("\\\\\"", "\"")
                           .replaceAll("\\\\t", "\t")
                           .replaceAll("\\\\r", "\r");
                }
            }
            
            // Last resort: find any field that might contain the response text
            idx = resp.indexOf("\"value\":");
            if (idx != -1) {
                int start = resp.indexOf('"', idx + 8) + 1;
                int end = findMatchingClosingQuote(resp, start);
                if (start > 0 && end > start) {
                    return resp.substring(start, end)
                           .replaceAll("\\\\n", "\n")
                           .replaceAll("\\\\\"", "\"")
                           .replaceAll("\\\\t", "\t")
                           .replaceAll("\\\\r", "\r");
                }
            }
            
            // If all else fails, return the full response for debugging
            return "[DEBUG] Raw Gemini response: " + resp;
        }
    }
    
    /**
     * Find the matching closing quote in a string, properly handling escaped quotes
     * @param str The string to search
     * @param startPos The position after the opening quote
     * @return The position of the closing quote
     */
    private int findMatchingClosingQuote(String str, int startPos) {
        for (int i = startPos; i < str.length(); i++) {
            if (str.charAt(i) == '"' && (i == 0 || str.charAt(i - 1) != '\\')) {
                return i;
            }
        }
        return str.length() - 1;
    }
}
