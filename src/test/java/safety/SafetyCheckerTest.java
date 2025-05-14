package safety;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SafetyCheckerTest {
    @Test
    void testDangerousCommandDetection() {
        assertTrue(SafetyChecker.isDangerous("rm -rf /"));
        assertTrue(SafetyChecker.isDangerous("dd if=/dev/zero of=/dev/sda"));
        assertFalse(SafetyChecker.isDangerous("ls -l"));
        assertFalse(SafetyChecker.isDangerous("echo hello"));
    }
}
