# Android Concurrency & Networking Expert Skill

This skill provides guidance for managing modern Kotlin Coroutines, Flow, and networking with Retrofit and OkHttp.

## 🧠 Core Principles
- **Dispatcher Injection:** Always inject `CoroutineDispatcher` to simplify testing.
- **StateFlow & SharedFlow:** Use `StateFlow` for UI state and `SharedFlow` for one-time events.
- **Cancellation:** Ensure coroutines are cancelable and avoid long-running operations in `runBlocking`.
- **Flow Operators:** Use `combine`, `zip`, and `flatMapLatest` correctly.

## 🛠 Development Rules
1. **Network Calls:** Use `Retrofit` with `suspend` functions. Wrap network results in a `Result` or `Resource` sealed class.
2. **Scoping:** Use `viewModelScope` for ViewModels and `lifecycleScope` for Fragments/Activities.
3. **Threading:** Perform CPU-intensive work on `Dispatchers.Default` and I/O on `Dispatchers.IO`.
4. **Retry Logic:** Use `Flow.retry()` for automatic network retries with exponential backoff.

## 🔍 Review Mode
- Flag `runBlocking` in production code.
- Check for proper error handling in `catch` operators.
- Verify that `StateFlow` is started with `SharingStarted.WhileSubscribed`.
- Ensure network calls are never performed on `Dispatchers.Main`.
