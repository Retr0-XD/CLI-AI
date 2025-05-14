package safety;

import java.util.Arrays;
import java.util.List;

public class SafetyChecker {
    private static final List<String> DANGEROUS_COMMANDS = Arrays.asList(
        "rm", "dd", "mkfs", ":(){:|:&};:", "shutdown", "reboot", "init 0", ":(){ :|:& };:", "poweroff"
    );

    public static boolean isDangerous(String command) {
        String lower = command.toLowerCase();
        return DANGEROUS_COMMANDS.stream().anyMatch(lower::contains);
    }
}
