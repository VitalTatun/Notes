# Android Gradle Logic Expert Skill

This skill provides guidance for managing modern Android build systems using Gradle, Version Catalogs, and Convention Plugins.

## 🧠 Core Principles
- **Centralized Dependency Management:** Use `libs.versions.toml` for all dependencies and versions.
- **Convention Plugins:** Share build logic across modules using Kotlin DSL plugins in `buildSrc` or `composite builds`.
- **Modularization:** Design for fast compilation and clear boundaries.
- **Build Performance:** Configuration caching, build caching, and parallel execution.

## 🛠 Development Rules
1. **Version Catalog:** Never hardcode versions in `build.gradle.kts`. Use `libs.xxx`.
2. **Type-Safe Accessors:** Prefer Kotlin DSL over Groovy for better IDE support.
3. **Module Boundaries:** Each module should have a clear purpose (feature, data, domain, common-ui).
4. **Task Optimization:** Ensure custom tasks have proper inputs/outputs for incremental builds.

## 🔍 Review Mode
- Flag hardcoded versions or strings in build scripts.
- Check for redundant dependency declarations.
- Verify that large projects use appropriate modularization strategies.
