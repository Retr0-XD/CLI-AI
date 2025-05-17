package safety;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SafetyCheckerTest {
    @Test
    void testDangerousCommandDetection() {
        assertTrue(SafetyChecker.isDangerous("rm -rf /"));
        assertTrue(SafetyChecker.isDangerous("dd if=/dev/zero of=/dev/sda"));
        assertTrue(SafetyChecker.isDangerous("rm -r /etc"));
        assertTrue(SafetyChecker.isDangerous("sudo apt-get purge"));
        assertTrue(SafetyChecker.isDangerous("chmod -R 777 /"));
        assertTrue(SafetyChecker.isDangerous("mkfs.ext4 /dev/sda1"));
        
        assertFalse(SafetyChecker.isDangerous("ls -l"));
        assertFalse(SafetyChecker.isDangerous("echo hello"));
        assertFalse(SafetyChecker.isDangerous("cd /home/user"));
        assertFalse(SafetyChecker.isDangerous("mkdir test"));
        assertFalse(SafetyChecker.isDangerous("cat file.txt"));
    }
    
    @Test
    void testGetDangerReason() {
        // Test that danger reasons are provided
        assertNotNull(SafetyChecker.getDangerReason("rm -rf /"));
        assertNotNull(SafetyChecker.getDangerReason("sudo apt-get install"));
        assertNotNull(SafetyChecker.getDangerReason("chmod 777 /etc/passwd"));
        
        // Test that no reason is provided for safe commands
        assertNull(SafetyChecker.getDangerReason("ls -la"));
        assertNull(SafetyChecker.getDangerReason("echo hello"));
    }
}
