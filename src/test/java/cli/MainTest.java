package cli;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import org.json.JSONObject;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    private static final String CONFIG_PATH = System.getProperty("user.home") + "/.sysai_config.json";
    private JSONObject backupConfig;

    @BeforeEach
    void backupConfig() throws IOException {
        if (Files.exists(Path.of(CONFIG_PATH))) {
            backupConfig = new JSONObject(Files.readString(Path.of(CONFIG_PATH)));
        }
    }

    @AfterEach
    void restoreConfig() throws IOException {
        if (backupConfig != null) {
            Files.writeString(Path.of(CONFIG_PATH), backupConfig.toString(2));
        } else {
            Files.deleteIfExists(Path.of(CONFIG_PATH));
        }
    }

    @Test
    void testConfigPersistence() throws IOException {
        JSONObject config = new JSONObject();
        config.put("provider", "OpenAI");
        config.put("model", "gpt-4");
        config.put("apiKey", "sk-test");
        Files.writeString(Path.of(CONFIG_PATH), config.toString(2));
        String content = Files.readString(Path.of(CONFIG_PATH));
        JSONObject loaded = new JSONObject(content);
        assertEquals("OpenAI", loaded.getString("provider"));
        assertEquals("gpt-4", loaded.getString("model"));
        assertEquals("sk-test", loaded.getString("apiKey"));
    }
}
