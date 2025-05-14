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

    /**
     * Simulate sending a query to the AI provider and getting a response.
     * In production, this should call the real API using the provider/model/apiKey.
     */
    public String sendQuery(String query) {
        // Simulated response for demo/testing
        if (query.toLowerCase().contains("list files")) {
            return "Run command: ls -l";
        } else if (query.toLowerCase().contains("hello")) {
            return "Run command: echo Hello from system!";
        } else if (query.toLowerCase().contains("dangerous")) {
            return "Run command: rm -rf /";
        }
        // Default simulated response
        return "Run command: echo Hello from system! && ls -l";
    }
}
