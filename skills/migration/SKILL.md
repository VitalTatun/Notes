# Android Migration Expert Skill

This skill provides guidance for modernizing Android applications by migrating from legacy technologies to current standards.

## 🧠 Core Principles
- **Incremental Migration:** Use `ComposeView` for gradual UI updates and interoperability.
- **Data Safety:** Ensure data integrity during database migrations (Room).
- **Concurrency Bridge:** Use `asLiveData()` or `collectAsState()` when bridging Flow and legacy UI.

## 🛠 Development Rules
1. **XML to Compose:** Replace one screen or component at a time. Use `AndroidViewBinding` if necessary.
2. **RxJava to Coroutines:** Convert `Single`/`Observable` to `suspend` functions and `Flow`. Use `kotlinx-coroutines-rx2` bridge.
3. **Room Migration:** Implement `Migration` classes with proper tests. Use `autoMigrations` where possible.
4. **Hilt Migration:** Transition from manual DI or Dagger 2 to Hilt using `@AndroidEntryPoint`.

## 🔍 Review Mode
- Flag logic duplication between old and new layers.
- Check for performance regressions in the `ComposeView` interop layer.
- Verify that legacy listeners are properly removed after migration.
