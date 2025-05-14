package system;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class SystemExecutor {
    public static String executeCommand(List<String> command) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
        } catch (Exception e) {
            output.append("[ERROR] ").append(e.getMessage());
        }
        return output.toString();
    }
}
