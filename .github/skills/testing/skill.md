# Testing Skill

- **Hilt for Testing:** Use `@HiltAndroidTest` for instrumented tests.
- **ViewModel Testing:** Always unit test ViewModels. Mock repositories using `io.mockk:mockk`.
- **Flow Testing:** Use `app.cash.turbine:turbine` to test Coroutine Flows.
- **Compose Testing:** Use `createComposeRule()` for UI tests.
- **Naming:** Follow the `given_when_then` or `methodName_stateUnderTest_expectedBehavior` convention.
