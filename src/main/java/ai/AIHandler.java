package ai;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class AIHandler {
    private final String provider;
    private final String model;
    private final String apiKey;

    public AIHandler(String provider, String model, String apiKey) {
        this.provider = provider;
        this.model = model;
        this.apiKey = apiKey;
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

    public String sendQuery(String query) {
        try {
            if (provider.equalsIgnoreCase("OpenAI")) {
                return callOpenAI(query);
            } else if (provider.equalsIgnoreCase("Gemini")) {
                return callGemini(query);
            } else {
                return "Unknown provider: " + provider;
            }
        } catch (Exception e) {
            return "[ERROR] Failed to contact AI provider: " + e.getMessage();
        }
    }

    private String callOpenAI(String query) throws Exception {
        URI uri = new URI("https://api.openai.com/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        String payload = "{" +
                "\"model\": \"" + model + "\"," +
                "\"messages\": [{\"role\": \"user\", \"content\": \"" +
                query.replace("\"", "\\\"") +
                "\"}]," +
                "\"max_tokens\": 256," +
                "\"temperature\": 0.2" +
                "}";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes());
        }
        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        String resp = response.toString();
        // Very basic extraction of the AI's reply (assumes OpenAI format)
        int idx = resp.indexOf("\"content\":");
        if (idx != -1) {
            int start = resp.indexOf('"', idx + 11) + 1;
            int end = resp.indexOf('"', start);
            if (start > 0 && end > start) {
                return resp.substring(start, end).replaceAll("\\\\n", "\n");
            }
        }
        return "[ERROR] Could not parse AI response: " + resp;
    }

    private String callGemini(String query) throws Exception {
        // Ensure model name is in correct format, if it's just "gemini", change to "gemini-pro"
        String actualModel = model;
        if (model.equalsIgnoreCase("gemini")) {
            actualModel = "gemini-pro";
            System.out.println("Note: Changed generic 'gemini' model to 'gemini-pro'");
        } else if (model.equalsIgnoreCase("gemini-1.5")) {
            actualModel = "gemini-1.5-pro";
            System.out.println("Note: Changed 'gemini-1.5' to 'gemini-1.5-pro'");
        }
        
        URI uri = new URI("https://generativelanguage.googleapis.com/v1beta/models/" + actualModel + ":generateContent?key=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        String payload = "{" +
                "\"contents\": [{\"parts\":[{\"text\": \"" + 
                query.replace("\"", "\\\"").replace("\n", "\\n") + 
                "\"}]}]," +
                "\"generationConfig\": {" +
                "  \"temperature\": 0.2," +
                "  \"maxOutputTokens\": 256" +
                "}" +
                "}";
                
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes());
        }
        
        int status = conn.getResponseCode();
        
        if (status < 200 || status >= 300) {
            // Print detailed error information
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
                int end = resp.lastIndexOf('"', resp.indexOf('}', start));
                if (start > 0 && end > start) {
                    return resp.substring(start, end).replaceAll("\\\\n", "\n");
                }
            }
            
            // Second pattern: try to find any "content" field
            idx = resp.indexOf("\"content\":");
            if (idx != -1) {
                int start = resp.indexOf('"', idx + 11) + 1;
                int end = resp.indexOf('"', start);
                if (start > 0 && end > start) {
                    return resp.substring(start, end).replaceAll("\\\\n", "\n");
                }
            }
            
            // Last resort: find any field that might contain the response text
            idx = resp.indexOf("\"value\":");
            if (idx != -1) {
                int start = resp.indexOf('"', idx + 8) + 1;
                int end = resp.indexOf('"', start);
                if (start > 0 && end > start) {
                    return resp.substring(start, end).replaceAll("\\\\n", "\n");
                }
            }
            
            // If all else fails, return the full response for debugging
            return "[DEBUG] Raw Gemini response: " + resp;
        }
    }
}
