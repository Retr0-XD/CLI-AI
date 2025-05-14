package safety;

public class SafetyChecker {
    // List of dangerous command patterns (expand as needed)
    private static final String[] DANGEROUS_KEYWORDS = {
        "rm -rf /", "dd if=", ":(){:|:&};:", "mkfs", "shutdown", "reboot", ":(){:|:&};:", "> /dev/sda", "kill -9 1"
    };

    public static boolean isDangerous(String command) {
        String lower = command.toLowerCase();
        for (String keyword : DANGEROUS_KEYWORDS) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
