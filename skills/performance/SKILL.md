# Android Performance Expert Skill

This skill provides guidance for auditing and optimizing Android application performance, focusing on Jetpack Compose and system resource usage.

## 🧠 Core Principles
- **Deferred State Reading:** Read state as late as possible (e.g., in the Draw phase) to avoid unnecessary recompositions.
- **Stability:** Ensure all data classes used in Compose are stable or immutable. Use `@Stable` and `@Immutable` where appropriate.
- **Efficient Lists:** Use `items(key = { ... })` and `contentType` in LazyLayouts.
- **Fast Builds:** Optimize Gradle, use Version Catalogs, and avoid heavy logic in build scripts.

## 🛠 Development Rules
1. **Compose Audit:** Use Layout Inspector to find recomposition counts. Avoid reading unstable state in the composition phase.
2. **DerivedStateOf:** Use `derivedStateOf` to buffer high-frequency state changes (like scroll position) before they reach the UI.
3. **Lambda Stability:** Use method references or `remember`ed lambdas to avoid breaking stability in sub-composables.
4. **Memory Management:** Avoid memory leaks by using `DisposableEffect` for cleanup.
5. **Image Loading:** Use Coil with proper crossfade and sizing to avoid UI stutters.

## 🏗 Tooling & Automation
- **Baseline Profiles:** Use Baseline Profiles to improve startup time.
- **R8/ProGuard:** Ensure shrinking and obfuscation are configured for release builds.

## 🔍 Review Mode
When reviewing performance:
- Look for complex calculations in `@Composable` bodies.
- Check for unnecessary `State` objects.
- Verify that `LazyColumn` items have unique keys.
- Flag uses of `mutableStateOf` without `remember`.
