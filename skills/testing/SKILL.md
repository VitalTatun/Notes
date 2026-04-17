# Android Testing & Automation Expert Skill

This skill provides guidance for implementing a robust testing strategy, covering Unit, UI, and Screenshot testing.

## 🧠 Core Principles
- **Test Pyramid:** Focus on high-volume Unit tests, medium-volume Integration tests, and low-volume UI tests.
- **Flakiness Avoidance:** Use `TestDispatcher` for Coroutines and avoid `Thread.sleep()`.
- **Hermetic Testing:** Tests should be independent and not rely on external services or shared state.

## 🛠 Development Rules
1. **Unit Testing:** Use MockK for dependencies. Test business logic in ViewModels and UseCases.
2. **Compose UI Testing:** Use `composeTestRule`. Find elements by `Semantics` (text, contentDescription, tag), not by layout position.
3. **Dependency Injection in Tests:** Use Hilt in instrumented tests to swap production modules with test doubles.
4. **Dispatcher Injection:** Always inject `CoroutineDispatcher` into ViewModels/Repositories to allow swapping with `UnconfinedTestDispatcher`.
5. **Screenshot Testing:** (Optional) Use libraries like Paparazzi or Showkase for regression testing.

## 🏗 Automation
- **CI/CD:** Ensure tests run automatically on every PR.
- **Robot Pattern:** Use the Robot pattern for UI tests to keep them readable and maintainable.

## 🔍 Review Mode
When reviewing tests:
- Flag tests that don't assert anything.
- Check for proper cleanup in `@After`.
- Verify that UI tests are not too fragile.
- Ensure Coroutine scopes are properly handled in tests.
