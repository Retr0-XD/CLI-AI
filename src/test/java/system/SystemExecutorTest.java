package system;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Arrays;

public class SystemExecutorTest {
    @Test
    public void testExecuteCommand() {
        String output = SystemExecutor.executeCommand(Arrays.asList("echo", "hello"));
        assertEquals("hello", output.trim());
    }
    
    @Test
    public void testParseCommand() {
        // Test simple command
        List<String> result = SystemExecutor.parseCommand("ls -l");
        assertEquals(Arrays.asList("ls", "-l"), result);
        
        // Test command with double quotes
        result = SystemExecutor.parseCommand("echo \"Hello World\"");
        assertEquals(Arrays.asList("echo", "Hello World"), result);
        
        // Test command with single quotes
        result = SystemExecutor.parseCommand("echo 'Hello World'");
        assertEquals(Arrays.asList("echo", "Hello World"), result);
        
        // Test command with mixed quotes
        result = SystemExecutor.parseCommand("find . -name \"*.txt\" -exec echo 'Found: {}' \\;");
        assertEquals(Arrays.asList("find", ".", "-name", "*.txt", "-exec", "echo", "Found: {}", ";"), result);
        
        // Test empty command
        result = SystemExecutor.parseCommand("");
        assertEquals(0, result.size());
        
        // Test null command
        result = SystemExecutor.parseCommand(null);
        assertEquals(0, result.size());
    }
}
