# UI & Navigation Skill

Always follow these principles for UI development:

- **Compose Only:** All new UI must be built with Jetpack Compose. No XML layouts.
- **Type-Safe Navigation:** Use the `Screen` sealed class for all navigation routes. Avoid hardcoded strings.
- **State Hoisting:** Keep UI components stateless. Pass state down and events up.
- **Edge-to-Edge:** Always handle `WindowInsets`. Use `statusBarsPadding()`, `navigationBarsPadding()`, and `imePadding()` appropriately.
- **Material 3:** Follow M3 design guidelines. Use the project's `NotesTheme`.
- **Performance:** Use `remember` and `derivedStateOf` to avoid unnecessary recompositions.
