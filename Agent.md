# Notes Project Architecture & Agent Skills

This project follows the modern Android development practices outlined in the [Awesome Android Agent Skills](https://github.com/new-silvermoon/awesome-android-agent-skills) repository.

## 🏗 Architecture
- **Pattern:** MVVM + Clean Architecture principles.
- **UI:** 100% Jetpack Compose with Material 3.
- **Dependency Injection:** Hilt.
- **Asynchronous Work:** Kotlin Coroutines & Flow.
- **Data Layer:** Room (Local Database) + Repository Pattern (Offline-first).
- **Navigation:** Type-safe Compose Navigation with centralized `NotesNavGraph`.

## 🛠 Tech Stack
- **Language:** Kotlin
- **Build System:** Gradle (Kotlin DSL) with Version Catalogs.
- **DI:** `com.google.dagger:hilt-android`
- **Navigation:** `androidx.navigation:navigation-compose`
- **Database:** `androidx.room:room-ktx`

## 🧠 Development Philosophy
This project follows the **Compose Expert** guidelines (see `skills/compose-expert/SKILL.md`):
1. **Three Phases:** Composition -> Layout -> Drawing.
2. **State Hoisting:** Keep composables stateless where possible.
3. **Performance:** Use `derivedStateOf`, `remember`, and stable types to minimize recompositions.
4. **Modifier Order:** Follow the "Layout-then-Action" rule (e.g., `padding` before `clickable`).
5. **Modern Navigation:** 100% Type-safe navigation using `@Serializable` routes.
6. **Stability:** Always use `StateFlow` in ViewModels with `stateIn`.
7. **Atomic Design:** Build components as Atoms, Molecules, or Organisms with standard contracts (Modifier, Slots, Tokens).

## 📁 Project Structure
- `com.example.notes.data`: Repositories and Local DB.
- `com.example.notes.di`: Hilt modules.
- `com.example.notes.ui.navigation`: Routes and NavGraph.
- `com.example.notes.ui.screens`: Screen-level composables.
- `com.example.notes.ui.viewmodel`: Hilt ViewModels.
- `com.example.notes.ui.components`: Reusable UI elements.
