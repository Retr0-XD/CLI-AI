package ai;

public class AIHandler {
    private final String provider;
    private final String model;
    private final String apiKey;

    public AIHandler(String provider, String model, String apiKey) {
        this.provider = provider;
        this.model = model;
        this.apiKey = apiKey;
    }

    public String sendQuery(String query) {
        // Placeholder: In production, route to the correct provider/model
        // and use the API key for authentication.
        // Here, just echo the input for demonstration.
        return String.format("[Simulated %s/%s] You asked: '%s' (API key: %s)", provider, model, query, apiKey.substring(0, Math.min(4, apiKey.length())) + "***");
    }
}
