package safety;

import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class SafetyChecker {
    // List of dangerous command patterns
    private static final String[] DANGEROUS_KEYWORDS = {
        "rm -rf /", "rm -r /", "rm -f /", "mkfs", "shutdown", "reboot", ":(){:|:&};:", 
        "> /dev/sd", "> /dev/hd", "kill -9 1", "dd if=", "shred", "wipe",
        "halt", "poweroff", "init 0", "init 6", "chmod -r 777 /", "chmod -R 777 /",
        "wget", "curl", ";", "&&", "||", "|", "format", "fdisk", "mkswap"
    };
    
    // Regex patterns for more complex matching
    private static final Pattern[] DANGEROUS_PATTERNS = {
        // Disk operations
        Pattern.compile("dd\\s+\\w*.*\\s+of=/dev/sd[a-z][0-9]*"),
        Pattern.compile("mkfs\\.\\w+\\s+/dev/\\w+"),
        Pattern.compile("fdisk\\s+/dev/\\w+"),
        
        // Recursive deletion
        Pattern.compile("rm\\s+(-[a-zA-Z]*[rf][a-zA-Z]*\\s+)*/"),
        Pattern.compile("rm\\s+(-[a-zA-Z]*[rf][a-zA-Z]*\\s+)*/etc"),
        Pattern.compile("rm\\s+(-[a-zA-Z]*[rf][a-zA-Z]*\\s+)*/var"),
        Pattern.compile("rm\\s+(-[a-zA-Z]*[rf][a-zA-Z]*\\s+)*/usr"),
        Pattern.compile("rm\\s+(-[a-zA-Z]*[rf][a-zA-Z]*\\s+)*/boot"),
        Pattern.compile("rm\\s+(-[a-zA-Z]*[rf][a-zA-Z]*\\s+)*/bin"),
        
        // Privilege escalation
        Pattern.compile("sudo\\s+.*"),
        Pattern.compile("su\\s+(-)?\\w*"),
        Pattern.compile("pkexec\\s+.*"),
        
        // System file modifications
        Pattern.compile("chmod\\s+([0-7]{3}|[+-][rwx])\\s+/\\w*"),
        Pattern.compile("chown\\s+.*\\s+/\\w*"),
        Pattern.compile("mv\\s+.*/\\s+/"),
        
        // Network exposure
        Pattern.compile("nc\\s+-l\\s+\\d+"),
        Pattern.compile("netcat\\s+-l\\s+\\d+"),
        
        // Downloading and executing
        Pattern.compile("(wget|curl)\\s+.*(\\|\\s*(bash|sh|zsh|csh|python|perl|php))"),
        
        // Dangerous package operations
        Pattern.compile("apt(-get)?\\s+(remove|purge)\\s+\\w+"),
        Pattern.compile("yum\\s+remove\\s+\\w+"),
        
        // Kernel module operations
        Pattern.compile("rmmod\\s+\\w+"),
        Pattern.compile("insmod\\s+\\w+"),
        
        // User management
        Pattern.compile("userdel\\s+\\w+"),
        Pattern.compile("deluser\\s+\\w+"),
        
        // Firewall modifications
        Pattern.compile("iptables\\s+-F"),
        Pattern.compile("ufw\\s+disable")
    };
    
    // System directories that should be protected
    private static final String[] SYSTEM_DIRECTORIES = {
        "/bin", "/sbin", "/usr/bin", "/usr/sbin", "/etc", "/var", "/boot", 
        "/lib", "/lib64", "/usr/lib", "/usr/lib64", "/dev", "/proc", "/sys"
    };
    
    /**
     * Check if a command is potentially dangerous
     * @param command The command to check
     * @return true if the command is potentially dangerous
     */
    public static boolean isDangerous(String command) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }
        
        String lower = command.toLowerCase().trim();
        
        // Check for dangerous keywords
        for (String keyword : DANGEROUS_KEYWORDS) {
            if (lower.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        
        // Check regex patterns
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(command).find()) {
                return true;
            }
        }
        
        // Check if command is writing to system directories
        if (isWritingToSystemDirs(command)) {
            return true;
        }
        
        // Check for usage of sensitive files
        if (isTouchingSensitiveFiles(command)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if a command is writing to system directories
     */
    private static boolean isWritingToSystemDirs(String command) {
        String lower = command.toLowerCase();
        
        // Check for command patterns that involve writing to system directories
        for (String dir : SYSTEM_DIRECTORIES) {
            if ((lower.contains("> " + dir) || lower.contains(">>" + dir) || 
                 lower.contains("touch " + dir) || 
                 (lower.contains("echo") && lower.contains(dir))) || 
                (lower.matches(".*(mv|cp)\\s+.*\\s+" + dir + ".*"))) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if a command is touching sensitive system files
     */
    private static boolean isTouchingSensitiveFiles(String command) {
        String lower = command.toLowerCase();
        String[] sensitiveFiles = {
            "/etc/passwd", "/etc/shadow", "/etc/sudoers", "/etc/hosts", 
            "/etc/ssh", "/etc/pam.d", "/etc/security", "/etc/fstab", 
            "/boot/grub", "/boot/initramfs", "/var/log/auth", 
            "/proc/kcore", "/dev/mem", "/dev/kmem"
        };
        
        for (String file : sensitiveFiles) {
            if (lower.contains(file)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get an explanation of why a command is considered dangerous
     * @param command The command to check
     * @return A string explaining why the command is dangerous, or null if it's not dangerous
     */
    public static String getDangerReason(String command) {
        if (!isDangerous(command)) {
            return null;
        }
        
        List<String> reasons = new ArrayList<>();
        String lower = command.toLowerCase();
        
        // Check for specific dangerous operations
        if (lower.contains("rm") && (lower.contains("-rf") || lower.contains("-r") || lower.contains("-f")) && 
            (lower.contains("/") || matchesAnySystemDir(lower))) {
            reasons.add("Recursive file deletion near system directories");
        }
        
        if (lower.contains("dd") && lower.contains("of=/dev")) {
            reasons.add("Direct writing to disk device");
        }
        
        if (lower.contains("mkfs") || lower.contains("format") || lower.contains("fdisk") || lower.contains("mkswap")) {
            reasons.add("Disk formatting or partition manipulation");
        }
        
        if (lower.contains("shutdown") || lower.contains("reboot") || lower.contains("halt") || 
            lower.contains("poweroff") || lower.contains("init 0") || lower.contains("init 6")) {
            reasons.add("System shutdown/reboot");
        }
        
        if (lower.contains(":(){:|:&};:")) {
            reasons.add("Fork bomb");
        }
        
        if (lower.contains("> /dev/") || lower.contains(">/dev/")) {
            reasons.add("Writing to device files");
        }
        
        if (lower.contains("sudo") || lower.contains("su -") || lower.contains("pkexec")) {
            reasons.add("Executing with elevated privileges");
        }
        
        if ((lower.contains("chmod") || lower.contains("chown") || lower.contains("chgrp")) && 
            (matchesAnySystemDir(lower) || lower.contains("/"))) {
            reasons.add("Changing permissions or ownership of system files");
        }
        
        if ((lower.contains("wget") || lower.contains("curl")) && 
            (lower.contains("| bash") || lower.contains("|bash") || 
             lower.contains("| sh") || lower.contains("|sh") || 
             lower.contains("|python") || lower.contains("| python"))) {
            reasons.add("Downloading and executing scripts");
        }
        
        if (lower.contains("apt") && (lower.contains("remove") || lower.contains("purge")) || 
            lower.contains("yum") && lower.contains("remove")) {
            reasons.add("Uninstalling system packages");
        }
        
        if (lower.contains("userdel") || lower.contains("deluser")) {
            reasons.add("Deleting user accounts");
        }
        
        if (lower.contains("iptables -F") || lower.contains("ufw disable")) {
            reasons.add("Disabling firewall protection");
        }
        
        if (lower.contains("/etc/passwd") || lower.contains("/etc/shadow") || lower.contains("/etc/sudoers")) {
            reasons.add("Modifying critical system configuration files");
        }
        
        // If no specific reason was found, provide a generic message
        if (reasons.isEmpty()) {
            reasons.add("Command contains potentially dangerous operations");
        }
        
        return String.join(", ", reasons);
    }
    
    /**
     * Check if a command interacts with any system directory
     */
    private static boolean matchesAnySystemDir(String command) {
        for (String dir : SYSTEM_DIRECTORIES) {
            if (command.contains(dir)) {
                return true;
            }
        }
        return false;
    }
}
