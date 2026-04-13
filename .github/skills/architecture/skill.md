# Architecture Skill

Always follow these principles when modifying the project:

- **Hilt First:** Every new ViewModel must be annotated with `@HiltViewModel` and use constructor injection.
- **UDF Pattern:** Maintain a single source of truth. UI should only observe state and emit events.
- **Repository Pattern:** Do not access DAOs directly from ViewModels. Always go through a Repository.
- **Kotlin First:** Use Coroutines (Suspend functions) for one-shot operations and Flow for streams of data.
