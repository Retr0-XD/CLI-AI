package system;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;

class SystemExecutorTest {
    @Test
    void testExecuteCommandEcho() {
        String output = SystemExecutor.executeCommand(Arrays.asList("echo", "HelloTest"));
        assertTrue(output.contains("HelloTest"));
    }

    @Test
    void testExecuteCommandInvalid() {
        String output = SystemExecutor.executeCommand(Arrays.asList("nonexistentcommand"));
        assertTrue(output.toLowerCase().contains("error") || output.toLowerCase().contains("not found"));
    }
}
