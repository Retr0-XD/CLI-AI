#!/bin/bash
# Demo script for System-Aware AI CLI Assistant
# This script simulates a user interaction with the CLI

echo "=================================="
echo "🧠 System-Aware AI CLI Assistant Demo"
echo "=================================="
echo ""
echo "Welcome to System-Aware AI CLI Assistant!"
echo "Current provider: OpenAI, model: gpt-4"
echo "Type 'change' to update provider/model/API key, or 'exit' to quit."
echo "> Check disk space usage"
echo ""
sleep 1

echo -e "\033[34m[Diagnostic Phase]\033[0m Analyzing your problem..."
echo -e "\033[32mAI: I need to gather information about your system to diagnose the problem.\033[0m"
echo "I'll run the following diagnostic commands:"
echo ""
sleep 1

echo -e "\033[34mDiagnostic Command 1:\033[0m df -h"
echo -e "\033[33mPurpose:\033[0m Shows disk space usage in human-readable format"
echo -e "\033[34mExecuting:\033[0m df -h"
df -h | head -6
echo ""
sleep 2

echo -e "\033[34mDiagnostic Command 2:\033[0m du -sh /var/log"
echo -e "\033[33mPurpose:\033[0m Check the size of log files which might be filling disk space"
echo -e "\033[34mExecuting:\033[0m du -sh /var/log"
echo "1.2G    /var/log"
echo ""
sleep 2

echo -e "\033[34m[Solution Phase]\033[0m Analyzing diagnostic information..."
echo -e "\033[32mAI: Based on the diagnostic information, here's my solution:\033[0m"
echo ""
echo "COMMAND: find /var/log -type f -name \"*.log\" -size +100M"
echo "EXPLANATION: This command finds log files larger than 100MB in the /var/log directory, which could be consuming significant disk space."
echo ""
sleep 1

echo -e "\033[34mSolution Command 1:\033[0m find /var/log -type f -name \"*.log\" -size +100M"
echo -e "\033[33mExplanation:\033[0m This command finds log files larger than 100MB in the /var/log directory, which could be consuming significant disk space."
echo "Execute this command? (yes/no): yes"
echo -e "\033[34mExecuting:\033[0m find /var/log -type f -name \"*.log\" -size +100M"
echo "/var/log/syslog.1"
echo "/var/log/kern.log.1"
echo ""
sleep 2

echo "COMMAND: sudo journalctl --vacuum-size=500M"
echo "EXPLANATION: This reduces the size of the systemd journal logs to a maximum of 500MB, freeing up disk space."
echo ""
sleep 1

echo -e "\033[34mSolution Command 2:\033[0m sudo journalctl --vacuum-size=500M"
echo -e "\033[33mExplanation:\033[0m This reduces the size of the systemd journal logs to a maximum of 500MB, freeing up disk space."
echo -e "\033[31m[WARNING]\033[0m This command is considered potentially dangerous."
echo -e "\033[31mReason: Executing with elevated privileges\033[0m"
echo "Do you want to proceed? (yes/no): yes"
echo "Execute this command? (yes/no): yes"
echo -e "\033[34mExecuting:\033[0m sudo journalctl --vacuum-size=500M"
echo "Vacuuming done, freed 700MB of archived journals."
echo ""
sleep 1

echo "PROBLEM_RESOLVED: YES"
echo ""
echo -e "\033[32m✅ AI indicates the problem has been resolved.\033[0m"
echo "> exit"
echo "Goodbye!"
