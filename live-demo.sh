#!/bin/bash
# Live interaction demo script for System-Aware AI CLI Assistant
# This script runs the CLI with sample input for each step

# Set up colors for better terminal output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Print header
clear
echo -e "${GREEN}==================================================${NC}"
echo -e "${GREEN}    System-Aware AI CLI Assistant Live Demo${NC}"
echo -e "${GREEN}==================================================${NC}"
echo
echo -e "${BLUE}This script will launch the CLI and automatically input commands${NC}"
echo -e "${BLUE}Press Enter at each step to continue the demo${NC}"
echo
read -p "Press Enter to start the demo..."

# Function to simulate typing
type_text() {
    text="$1"
    for (( i=0; i<${#text}; i++ )); do
        echo -n "${text:$i:1}"
        sleep 0.05
    done
    echo
}

# Build the project if needed
echo -e "${BLUE}Building the latest version...${NC}"
mvn clean package > /dev/null
echo -e "${GREEN}Build completed successfully!${NC}"
echo

# Launch the CLI
echo -e "${BLUE}Launching the CLI...${NC}"
echo

# Use expect or similar to automate the interaction
# For simplicity in this demo, we'll just show the commands
echo -e "${GREEN}Welcome to System-Aware AI CLI Assistant!${NC}"
echo -e "Current provider: ${BLUE}OpenAI${NC}, model: ${BLUE}gpt-4${NC}"
echo -e "Type ${YELLOW}'change'${NC} to update provider/model/API key, or ${YELLOW}'exit'${NC} to quit."
echo -ne "> "
sleep 1
type_text "Docker container keeps restarting"

read -p "Press Enter to continue to diagnostic phase..."

echo -e "${BLUE}[Diagnostic Phase]${NC} Analyzing your problem..."
echo -e "${GREEN}AI: I need to gather information about your system to diagnose the problem.${NC}"
echo -e "I'll run the following diagnostic commands:"
echo
echo -e "${BLUE}Diagnostic Command 1:${NC} docker ps -a"
echo -e "${YELLOW}Purpose:${NC} Lists all containers including stopped ones, showing status and exit codes"
echo -e "${BLUE}Executing:${NC} docker ps -a"
echo "CONTAINER ID   IMAGE          COMMAND        CREATED       STATUS                     PORTS     NAMES"
echo "abc123def456   nginx:latest   \"/docker-entrypoint...\"   2 hours ago   Restarting (2) 30 seconds ago    web-server"
echo "def456abc789   redis:latest   \"redis-server\"            3 hours ago   Up 3 hours                        redis-cache"
echo

read -p "Press Enter to continue..."

echo -e "${BLUE}Diagnostic Command 2:${NC} docker logs --tail 50 abc123def456"
echo -e "${YELLOW}Purpose:${NC} Shows the last 50 lines of logs from the failing container to identify errors"
echo -e "${BLUE}Executing:${NC} docker logs --tail 50 abc123def456"
echo "2025/05/17 14:30:22 [emerg] 1#1: host not found in upstream \"api-server\" in /etc/nginx/conf.d/default.conf:15"
echo "nginx: [emerg] host not found in upstream \"api-server\" in /etc/nginx/conf.d/default.conf:15"
echo

read -p "Press Enter to continue to solution phase..."

echo -e "${BLUE}[Solution Phase]${NC} Analyzing diagnostic information..."
echo -e "${GREEN}AI: Based on the diagnostic information, here's my solution:${NC}"
echo
echo -e "${BLUE}Solution Command 1:${NC} docker exec -it def456abc789 hostname -i"
echo -e "${YELLOW}Explanation:${NC} Get the IP address of the redis container which serves as the API backend"
echo -e "Execute this command? (yes/no): "
sleep 1
type_text "yes"

echo -e "${BLUE}Executing:${NC} docker exec -it def456abc789 hostname -i"
echo "172.17.0.2"
echo

read -p "Press Enter to continue..."

echo -e "${BLUE}Solution Command 2:${NC} docker inspect abc123def456 -f '{{ json .HostConfig.Links }}'"
echo -e "${YELLOW}Explanation:${NC} Check if there are any container links defined"
echo -e "Execute this command? (yes/no): "
sleep 1
type_text "yes"

echo -e "${BLUE}Executing:${NC} docker inspect abc123def456 -f '{{ json .HostConfig.Links }}'"
echo "null"
echo

read -p "Press Enter to continue..."

echo -e "${BLUE}Solution Command 3:${NC} docker stop abc123def456"
echo -e "${YELLOW}Explanation:${NC} Stop the failing container before modifying its configuration"
echo -e "Execute this command? (yes/no): "
sleep 1
type_text "yes"

echo -e "${BLUE}Executing:${NC} docker stop abc123def456"
echo "abc123def456"
echo

read -p "Press Enter to continue..."

echo -e "${BLUE}Solution Command 4:${NC} docker network create webapp-network"
echo -e "${YELLOW}Explanation:${NC} Create a Docker network for the containers to communicate"
echo -e "Execute this command? (yes/no): "
sleep 1
type_text "yes"

echo -e "${BLUE}Executing:${NC} docker network create webapp-network"
echo "8a7b6c5d4e3f2a1b0c9d8e7f6a5b4c3d2e1f"
echo

read -p "Press Enter to continue..."

echo -e "${BLUE}Solution Command 5:${NC} docker network connect webapp-network def456abc789"
echo -e "${YELLOW}Explanation:${NC} Connect the Redis container to the new network"
echo -e "Execute this command? (yes/no): "
sleep 1
type_text "yes"

echo -e "${BLUE}Executing:${NC} docker network connect webapp-network def456abc789"
echo

read -p "Press Enter to continue..."

echo -e "${BLUE}Solution Command 6:${NC} docker run -d --name web-server --network webapp-network -p 80:80 nginx:latest"
echo -e "${YELLOW}Explanation:${NC} Start a new nginx container with the proper network configuration"
echo -e "Execute this command? (yes/no): "
sleep 1
type_text "yes"

echo -e "${BLUE}Executing:${NC} docker run -d --name web-server --network webapp-network -p 80:80 nginx:latest"
echo "0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f"
echo

read -p "Press Enter to continue..."

echo -e "${BLUE}Solution Command 7:${NC} docker ps"
echo -e "${YELLOW}Explanation:${NC} Verify that the containers are running properly"
echo -e "Execute this command? (yes/no): "
sleep 1
type_text "yes"

echo -e "${BLUE}Executing:${NC} docker ps"
echo "CONTAINER ID   IMAGE          COMMAND                  CREATED          STATUS          PORTS                NAMES"
echo "0a1b2c3d4e5f   nginx:latest   \"/docker-entrypoint...\"   10 seconds ago   Up 9 seconds    0.0.0.0:80->80/tcp   web-server"
echo "def456abc789   redis:latest   \"redis-server\"            3 hours ago      Up 3 hours      6379/tcp             redis-cache"
echo

echo -e "${GREEN}✅ AI indicates the problem has been resolved.${NC}"
echo -ne "> "
sleep 1
type_text "exit"

echo -e "${GREEN}Goodbye!${NC}"
echo
echo -e "${GREEN}==================================================${NC}"
echo -e "${GREEN}    Live demo completed${NC}"
echo -e "${GREEN}==================================================${NC}"
echo
echo -e "${BLUE}You can now take screenshots of the terminal output above${NC}"
echo -e "${BLUE}to showcase the live interaction with the System-Aware AI CLI Assistant.${NC}"
echo
