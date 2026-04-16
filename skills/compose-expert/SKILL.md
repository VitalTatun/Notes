# Jetpack Compose Expert Skill

This skill provides professional guidance for Jetpack Compose and Compose Multiplatform (CMP) development, following patterns from the `androidx` source code.

## 🧠 Core Principles
- **Three Phases:** Thinking in Composition → Layout → Drawing.
- **Recomposition:** Ensuring it remains cheap by using stable types and avoiding allocations in bodies.
- **Modifier Order:** Layout-then-Action rule. Sequence changes the visual outcome.
- **State Hoisting:** Keep state as low as possible. Use `derivedStateOf` for complex calculations.
- **Side Effects:** Using the correct effect (`LaunchedEffect` vs `DisposableEffect`) with proper keys.

## 🛠 Development Rules
1. **Consult References First:** Prioritize stable patterns and modern APIs.
2. **Verify Against Source:** Check `androidx` source for API parameters and behavior.
3. **Prioritize Stability:** Use stability annotations and avoid unnecessary recompositions.
4. **Enforce Modifier Order:** Place `clickable` before `padding` for correct touch targets.
5. **Modern Navigation:** Strictly use Navigation 2.8.0+ type-safe approach with `@Serializable`.
6. **Material 3:** Use M3 design tokens and semantic components.

## 🏗 Component Building (Atomic Design)
- **Atom Contract:** Every component must include a `Modifier`, slots for content, design tokens, and sensible defaults.
- Classify components as Atoms, Molecules, or Organisms.

## 🔍 Review Mode
When reviewing code:
- Flag anti-patterns (e.g., heavy logic in `@Composable`, unstable types in LazyLists).
- Check for accessibility (content descriptions, touch targets).
- Verify performance (deferred state reads, `key {}` usage).
