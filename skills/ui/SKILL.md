# Android General UI Expert Skill

This skill provides guidance for building accessible, responsive, and beautiful Android UIs, focusing on Material 3 and navigation.

## 🧠 Core Principles
- **Accessibility (a11y):** Ensure all elements have meaningful `contentDescription`. Check for 48dp+ touch targets.
- **Material 3 Tokens:** Use standard design tokens (colors, typography, shapes) to ensure consistency.
- **Adaptive UI:** Design for different screen sizes (phones, tablets, foldables).
- **Navigation Flow:** Use type-safe navigation and manage the backstack efficiently.

## 🛠 Development Rules
1. **Coil:** Use `AsyncImage` for loading network and local images. Handle error and placeholder states.
2. **Text Semantics:** Use `MaterialTheme.typography` styles. Avoid hardcoding text sizes and colors.
3. **Touch Targets:** Minimum 48x48 dp for interactive elements, even if the icon is smaller.
4. **Contrast:** Ensure all text and icons meet WCAG AA contrast standards.

## 🔍 Review Mode
- Flag missing `contentDescription` on non-decorative images.
- Check for small touch targets.
- Verify that standard Material 3 components are used instead of custom implementations where possible.
