# System-Aware AI CLI Assistant

A production-ready Java CLI tool that connects to AI providers (OpenAI, Gemini, etc.), lets users select provider/model/API key, accepts natural language queries, receives AI-generated step-by-step system commands, checks command safety, prompts for user approval, executes/logs commands, and is installable as a global command.

## Features
- Interactive CLI for natural language system queries
- Supports multiple AI providers (OpenAI, Gemini, etc.)
- Persistent configuration in `~/.sysai_config.json`
- Safety checks for dangerous system commands
- User approval before executing commands
- Logs command output to the console
- JUnit tests for core components
- GitHub Actions pipeline for CI/CD

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+

### Build the Project

```
git clone <your-repo-url>
cd CLI-AI
mvn clean package
```

The built JAR will be in `target/system-ai-cli-1.0.0-shaded.jar`.

### Run the CLI

```
java -jar target/system-ai-cli-1.0.0-shaded.jar
```

### First Run Setup
- On first run, you'll be prompted to select an AI provider, model, and enter your API key.
- This configuration is saved in `~/.sysai_config.json` for future runs.

### Usage
- Type your natural language query (e.g., "List files in this directory").
- The AI will suggest system commands to run.
- Each command is checked for safety. Dangerous commands require explicit approval.
- Output is shown in the terminal.
- Type `change` to update provider/model/API key.
- Type `exit` to quit.

### Gemini API Usage
When using Google's Gemini API:
- You'll need a valid API key from Google AI Studio or Google Cloud.
- If you enter just "gemini" as the model name, it will automatically be corrected to "gemini-pro".
- Similarly, "gemini-1.5" will be corrected to "gemini-1.5-pro".
- The tool handles the proper formatting of requests to the Gemini API.

### Command Parsing
The tool uses intelligent command parsing to handle:
- Commands with quoted arguments (both single and double quotes)
- Commands with spaces in arguments
- Properly escaped characters

For example, the following commands will be parsed correctly:
```
ls -l "/path with spaces"
find . -name "*.txt" -exec echo "Found: {}" \;
```

### Example Session
```
Welcome to System-Aware AI CLI Assistant!
Current provider: OpenAI, model: gpt-4
Type 'change' to update provider/model/API key, or 'exit' to quit.
> List files in this directory
AI: Run command: ls -l
Executing: ls -l
(total output...)
> exit
Goodbye!
```

## Testing

Run all tests with:
```
mvn test
```

## Continuous Integration

A GitHub Actions workflow is included. On every push or pull request to `main`, the project is built and tested, and the JARs are uploaded as artifacts.

## Customization
- Extend `ai/AIHandler.java` to integrate real API calls.
- Add more safety rules in `safety/SafetyChecker.java` as needed.

## License
MIT
