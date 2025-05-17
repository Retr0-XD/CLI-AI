# Changelog

## Version 1.3.0 (2025-05-17)

### Major Improvements
- Enhanced command execution system
  - Added timeout functionality for commands
  - Better handling of shell operators (pipes, redirects, etc.)
  - Added support for different shells based on OS
  - Implemented proper command existence checking
- Comprehensive model validation and correction
  - Added support for multiple OpenAI and Gemini models
  - Improved model name correction for both providers
  - Better error handling for API responses

### Technical Improvements
- Enhanced JSON parsing for API responses
  - Added proper handling of escaped quotes in responses
  - Improved extraction of content from complex JSON
- Command execution improvements
  - Added timeout to prevent long-running commands
  - Better environment detection for shell selection
  - Improved error handling for command execution

## Version 1.2.0 (2025-05-17)

### Major Improvements
- Enhanced diagnostic capabilities
  - Improved prompting for diagnostic commands with purpose explanations
  - Better command output formatting with length limits
  - Added color-coded terminal output for better readability
- Expanded safety features
  - More comprehensive dangerous command detection with regex patterns
  - Added detailed explanations for why commands are flagged as dangerous
  - Improved system directory write protection

### Technical Improvements
- Enhanced error handling for command execution
- Better command parsing and extraction
- More informative user interface

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
