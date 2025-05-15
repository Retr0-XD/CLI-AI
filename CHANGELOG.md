# Changelog

## Version 1.1.0 (2025-05-15)

### Major Improvements
- Fixed Gemini API integration issues
  - Added automatic correction of model names (e.g., "gemini" → "gemini-pro")
  - Improved error handling and response parsing
  - Added support for "gemini-1.5" model correction
- Enhanced command execution capabilities
  - Added robust command parsing with proper handling of quoted arguments
  - Implemented better escape character handling
  - Improved error reporting for command execution

### Technical Improvements
- Fixed deprecated URL constructor warnings by using URI
- Added more robust error handling throughout the codebase
- Enhanced response parsing for Gemini API
- Added support for multi-line commands in the user input

### Documentation
- Updated README.md with info about Gemini API support
- Added Gemini API usage instructions to USAGE.md
- Added command line examples with quoted arguments

### Testing
- Added unit tests for command parsing
- Added unit tests for model name correction
- Expanded test coverage for the AIHandler class

## Version 1.0.0 (Initial Release)

- Basic CLI interface with picocli
- Support for OpenAI API
- Command execution with ProcessBuilder
- Simple safety checking for dangerous commands
- Configuration storage in ~/.sysai_config.json
