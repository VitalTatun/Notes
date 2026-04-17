# Android Architecture Expert Skill

This skill provides guidance for building scalable, maintainable, and testable Android applications using Clean Architecture and MVVM.

## 🧠 Core Principles
- **Separation of Concerns:** Keep UI, Business Logic, and Data Logic in distinct layers.
- **Unidirectional Data Flow (UDF):** UI -> Events -> ViewModel -> State -> UI.
- **Offline-First:** Data is always read from the local database (Source of Truth), and the repository handles sync with the network.
- **Dependency Inversion:** Depend on abstractions, not implementations. Use Hilt for DI.

## 🛠 Development Rules
1. **ViewModel Responsibilities:** ViewModels should only manage UI state and handle UI events. Avoid logic that doesn't belong to the UI.
2. **Repository Pattern:** Centralize data access in Repositories. Repositories coordinate between local DB (Room) and remote API (Retrofit).
3. **Use Cases / Interactors:** For complex business logic that spans multiple repositories, use UseCase classes.
4. **State Management:** Use `StateFlow` and `SharedFlow`. Always expose immutable state to the UI.
5. **Dagger/Hilt:** Use constructor injection everywhere. Avoid manual dependency management.

## 🏗 Data Layer
- **Room:** Define clear entities and DAOs. Use Flow for reactive updates.
- **Mappers:** Use dedicated mapper functions to convert between Data Entities, Domain Models, and UI Models.

## 🔍 Review Mode
When reviewing architecture:
- Flag logic in Fragments/Activities/Composables.
- Check for proper error handling in the data layer.
- Verify that UI doesn't have direct access to DAOs or APIs.
- Ensure ViewModels don't hold references to Android Framework classes (Context, View, etc.).
