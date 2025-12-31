# Contributing to ESP32 Bluetooth Companion

Thank you for your interest in contributing to the ESP32 Bluetooth Companion project! This document provides guidelines and instructions for contributing.

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [How Can I Contribute?](#how-can-i-contribute)
3. [Development Setup](#development-setup)
4. [Coding Standards](#coding-standards)
5. [Pull Request Process](#pull-request-process)
6. [Reporting Bugs](#reporting-bugs)
7. [Suggesting Enhancements](#suggesting-enhancements)

## Code of Conduct

This project follows a simple code of conduct:
- Be respectful and inclusive
- Focus on constructive feedback
- Help others learn and grow
- Keep discussions professional and on-topic

## How Can I Contribute?

### Reporting Bugs

Before creating a bug report:
1. Check the [issues](../../issues) to see if it's already reported
2. Try the latest version to see if the bug still exists
3. Collect relevant information (device models, versions, logs)

When reporting a bug, include:
- Clear description of the issue
- Steps to reproduce
- Expected vs actual behavior
- Device information (ESP32 model, Android version)
- MicroPython version
- Logs or error messages

### Suggesting Enhancements

Enhancement suggestions are welcome! When suggesting:
- Use a clear, descriptive title
- Provide detailed explanation of the feature
- Explain why this would be useful
- Consider implementation details if possible

### Code Contributions

Types of contributions we're looking for:
- Bug fixes
- New features
- Documentation improvements
- Performance optimizations
- Test coverage improvements
- UI/UX improvements

## Development Setup

### Android App

1. **Prerequisites**
   - Android Studio Arctic Fox or later
   - JDK 17+
   - Android SDK 34

2. **Setup**
   ```bash
   git clone https://github.com/jamesbiederbeck/esp32-bluetooth-companion.git
   cd esp32-bluetooth-companion/android
   ```

3. **Open in Android Studio**
   - Open the `android` directory
   - Wait for Gradle sync
   - Build and run

### MicroPython Module

1. **Prerequisites**
   - ESP32 with MicroPython
   - Python 3.x
   - ampy, rshell, or mpremote

2. **Development Workflow**
   ```bash
   # Edit the module
   vim micropython/lib/companion.py
   
   # Upload to ESP32
   ampy --port /dev/ttyUSB0 put micropython/lib/companion.py /lib/companion.py
   
   # Test on device
   screen /dev/ttyUSB0 115200
   ```

## Coding Standards

### Android (Kotlin)

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Use Jetpack Compose for UI components
- Follow MVVM architecture pattern
- Use Kotlin coroutines for async operations
- Format code with ktlint

Example:
```kotlin
/**
 * Connects to an ESP32 device via Bluetooth
 *
 * @param deviceAddress The MAC address of the device
 * @return Result indicating success or failure
 */
suspend fun connect(deviceAddress: String): Result<Unit> {
    // Implementation
}
```

### MicroPython (Python)

- Follow [PEP 8](https://pep8.org/) style guide
- Use docstrings for functions and classes
- Keep functions focused and small
- Handle errors gracefully
- Consider memory constraints on ESP32

Example:
```python
def notify(title, message, level="info"):
    """
    Send a notification to the Android app
    
    Args:
        title: Notification title
        message: Notification message
        level: Notification level (info, warning, error, success)
    """
    # Implementation
```

### Documentation

- Use Markdown for all documentation
- Keep language clear and concise
- Include code examples where relevant
- Update documentation with code changes

## Pull Request Process

1. **Fork and Branch**
   ```bash
   git checkout -b feature/my-feature
   ```

2. **Make Changes**
   - Write clean, well-documented code
   - Follow coding standards
   - Add tests if applicable
   - Update documentation

3. **Test Your Changes**
   - Test on real hardware when possible
   - Verify no regressions
   - Check for memory leaks

4. **Commit**
   ```bash
   git add .
   git commit -m "Add feature: descriptive message"
   ```

   Commit message format:
   - Use present tense ("Add feature" not "Added feature")
   - Use imperative mood ("Move cursor to..." not "Moves cursor to...")
   - First line: brief summary (50 chars or less)
   - Blank line, then detailed description if needed

5. **Push and Create PR**
   ```bash
   git push origin feature/my-feature
   ```
   
   In the PR description:
   - Describe what changes you made and why
   - Reference any related issues
   - Include screenshots for UI changes
   - List any breaking changes
   - Note testing performed

6. **Code Review**
   - Respond to feedback promptly
   - Make requested changes
   - Keep the PR focused and manageable
   - Be open to suggestions

7. **Merge**
   - PRs are merged by maintainers after approval
   - Squash commits may be used to keep history clean

## Areas for Contribution

### High Priority

- [ ] Implement actual Bluetooth SPP in MicroPython module
- [ ] Add file upload functionality in Android app
- [ ] Implement file deletion in Android app
- [ ] Add support for BLE in addition to Bluetooth Classic
- [ ] Improve error handling and recovery
- [ ] Add automated tests

### Medium Priority

- [ ] Support for other ESP32 variants (S2, S3, C3)
- [ ] Offline mode documentation
- [ ] Performance optimizations
- [ ] Battery usage optimization
- [ ] More example scripts
- [ ] Internationalization (i18n)

### Nice to Have

- [ ] Dark/light theme toggle
- [ ] Saved device configurations
- [ ] Command history in REPL
- [ ] Syntax highlighting in REPL
- [ ] File editor in app
- [ ] Graph visualization for telemetry

## Testing Guidelines

### Android App

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run lint
./gradlew lint
```

### MicroPython Module

- Test on real ESP32 hardware
- Verify all message types work
- Check memory usage with `gc.mem_free()`
- Test error conditions
- Verify reconnection behavior

## Questions?

- Check the [documentation](README.md)
- Search [existing issues](../../issues)
- Ask in [discussions](../../discussions)
- Contact maintainers

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing! 🎉
