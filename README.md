

# 🧠 System-Aware AI CLI Assistant

A smart command-line tool that connects your system with an AI safely. It diagnoses system issues and suggests step-by-step command fixes, **without risking system stability**.

> ⚠️ All commands are confirmed by the user before execution. High-risk commands are flagged with a warning.

---

## ✨ Features

* Understands user system issues (e.g. "Docker not working").
* AI dynamically requests relevant system info.
* Gathers required data and sends to AI.
* Receives step-by-step fix instructions.
* Executes commands with user approval.
* Interrupts and handles errors safely.

---

## 📦 Tech Stack

| Component     | Technology                               |
| ------------- | ---------------------------------------- |
| Language      | Java                                     |
| CLI Framework | [Picocli](https://picocli.info) or JLine |
| AI Backend    | OpenAI API                               |
| Command Exec  | Java ProcessBuilder                      |
| Config        | JSON or SQLite (optional)                |
| Deployment    | GitHub Codespaces / Local                |

---

## 🚀 Getting Started

### 🔧 Prerequisites

* Java 17+
* Git
* OpenAI API Key
* (Optional) Docker, Node, or other tools to test commands

---

### 🛠 Setup Instructions

```bash
# 1. Clone the repo
git clone https://github.com/yourusername/system-ai-cli.git
cd system-ai-cli

# 2. Add your OpenAI key
echo "OPENAI_API_KEY=your-key-here" > .env

# 3. Run the CLI
./gradlew run --args="help"
```

> You can run this in **GitHub Codespaces** too (Java Dev Container preset).

---

## 🧪 Usage

```bash
$ sysai "docker not working"
```

### 🧠 How it works:

1. You type a system problem in natural language.
2. AI replies with info it needs (e.g., docker logs).
3. CLI collects this info and sends it back.
4. AI returns step-by-step commands to fix.
5. CLI confirms and runs them one-by-one.

---

## 🛡️ Safety Features

* All commands must be **explicitly approved**.
* AI commands are **filtered** for danger (`rm`, `dd`, etc.).
* User is warned before any risky execution.
* Errors pause the execution with choices (retry/skip/abort).
* Logging is done for every session.

---

## 📁 Project Structure

```bash
system-ai-cli/
│
├── src/
│   ├── cli/             # Entry point and command line parsing
│   ├── ai/              # Handles interaction with OpenAI
│   ├── system/          # Executes and logs system commands
│   └── safety/          # Checks risk level of commands
│
├── .env                 # API key config
├── build.gradle         # Build system
└── README.md
```

---

## 🧱 Contributing

1. Fork the repo.
2. Create your feature branch (`git checkout -b feature/xyz`).
3. Commit changes (`git commit -am 'Add feature'`).
4. Push to the branch (`git push origin feature/xyz`).
5. Open a Pull Request.

---

## 📌 Notes

* This CLI **does not auto-fix** problems. User consent is always required.
* Extendable to multiple AI providers and tools.
* Focus is on safe debugging and AI guidance.

---

## 📃 License

MIT License. Use freely with attribution.

