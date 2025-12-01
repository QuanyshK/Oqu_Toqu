# CONTRIBUTING.md

# Contributing to Oqu Toqu

Thank you for your interest in contributing to Oqu Toqu! We welcome contributions from everyone. By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md).

## How to Contribute

### Reporting Bugs

If you find a bug, please open an issue on GitHub. Include as much detail as possible:
- A clear and descriptive title.
- Steps to reproduce the issue.
- Expected vs. actual behavior.
- Screenshots or logs if applicable.
- Your environment details (e.g., Android version, device model).

### Suggesting Enhancements

We welcome ideas for new features or improvements! Open an issue and describe:
- The feature you'd like to see.
- Why it would be useful.
- Any design or implementation ideas you have.

### Pull Requests

1. **Fork the Repository**: Click the "Fork" button on the top right of the repository page.
2. **Clone Your Fork**: 
   ```bash
   git clone https://github.com/YOUR_USERNAME/Oqu_Toqu.git
   ```
3. **Create a Branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```
4. **Make Changes**: Implement your feature or fix. Ensure your code follows the project's style and conventions.
5. **Test Your Changes**: Run existing tests and add new ones if necessary.
   ```bash
   ./gradlew test
   ```
6. **Commit Your Changes**:
   ```bash
   git commit -m "Add feature: description of your changes"
   ```
7. **Push to Your Fork**:
   ```bash
   git push origin feature/your-feature-name
   ```
8. **Open a Pull Request**: Go to the original repository and click "New Pull Request". Describe your changes and reference any related issues.

## Development Setup

### Prerequisites
- Android Studio (latest stable version recommended)
- JDK 17 or higher
- Android SDK API level 24 or higher

### Building the Project
1. Open the project in Android Studio.
2. Let Gradle sync and download dependencies.
3. Build the project:
   ```bash
   ./gradlew build
   ```

## Code Style
- Follow standard [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Use meaningful variable and function names.
- Keep functions small and focused.
- Comment complex logic, but strive for self-documenting code.

## License
By contributing, you agree that your contributions will be licensed under the [GNU General Public License v3.0](LICENSE).
