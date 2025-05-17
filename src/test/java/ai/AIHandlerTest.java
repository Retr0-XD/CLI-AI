package ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AIHandlerTest {
    
    @Test
    public void testModelNameCorrection() {
        // Test automatic correction of model name
        AIHandler handler = new AIHandler("Gemini", "gemini", "fake-api-key");
        
        // With our enhanced implementation, we now correct the model name internally
        assertEquals("Gemini", handler.getProvider());
        assertEquals("gemini-pro", handler.getModel());
        
        // Test OpenAI model correction
        AIHandler openAIHandler = new AIHandler("OpenAI", "gpt4", "fake-api-key");
        assertEquals("gpt-4", openAIHandler.getModel());
    }
    
    @Test
    public void testProviderSelection() {
        // Test OpenAI provider
        AIHandler openAIHandler = new AIHandler("OpenAI", "gpt-4", "fake-api-key");
        assertEquals("OpenAI", openAIHandler.getProvider());
        
        // Test Gemini provider
        AIHandler geminiHandler = new AIHandler("Gemini", "gemini-pro", "fake-api-key");
        assertEquals("Gemini", geminiHandler.getProvider());
        
        // Test unknown provider
        AIHandler unknownHandler = new AIHandler("Unknown", "model", "fake-api-key");
        String response = unknownHandler.sendQuery("test");
        assertTrue(response.startsWith("Unknown provider:"), "Expected unknown provider message");
    }
}
