package ai;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class AIHandler {
    private final String provider;
    private final String model;
    private final String apiKey;
    private boolean initialPrompted = false;

    public AIHandler(String provider, String model, String apiKey) {
        this.provider = provider;
        this.model = model;
        this.apiKey = apiKey;
    }

    /**
     * Simulate sending a query to the AI provider and getting a response.
     * In production, this should call the real API using the provider/model/apiKey.
     */
    public String sendQuery(String query) {
        if (!initialPrompted) {
            initialPrompted = true;
            return "To help you, I need some details about your system. Please provide: 1) OS type, 2) Shell, 3) What do you want to accomplish?";
        }
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

    private String callOpenAI(String query) throws IOException {
        URL url = new URL("https://api.openai.com/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
                return resp.substring(start, end).replaceAll("\\n", "\n");
            }
        }
        return "[ERROR] Could not parse AI response: " + resp;
    }

    private String callGemini(String query) throws IOException {
        // Placeholder: Gemini API integration should be added here
        return "[ERROR] Gemini API integration not implemented. Please use OpenAI for now.";
    }
}
