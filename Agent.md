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
1. **State Hoisting:** Keep composables stateless where possible.
2. **UDF (Unidirectional Data Flow):** Events go up, state goes down.
3. **Edge-to-Edge:** Use `WindowInsets` for full-screen experience.
4. **Performance:** Use `derivedStateOf` and `remember` to optimize recompositions.
5. **Stability:** Always use `StateFlow` in ViewModels with `stateIn`.

## 📁 Project Structure
- `com.example.notes.data`: Repositories and Local DB.
- `com.example.notes.di`: Hilt modules.
- `com.example.notes.ui.navigation`: Routes and NavGraph.
- `com.example.notes.ui.screens`: Screen-level composables.
- `com.example.notes.ui.viewmodel`: Hilt ViewModels.
- `com.example.notes.ui.components`: Reusable UI elements.
