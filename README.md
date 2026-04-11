# WebForge Studio

WebForge Studio is an Android-first visual web builder built with Kotlin, Jetpack Compose, and a shared Kotlin Multiplatform module.

It provides:
- project management and onboarding
- visual canvas editing for UI elements
- page management
- interaction/block editing with event chains
- code generation for project export

---

## What’s Updated

Recent updates include:
- block editor drag/reorder flow moved to UI-owned drag state
- deferred persistence after reorder animation
- safer JavaScript block generation (typed parameter handling, scoped variable mapping, fetch try/catch wrapping, placeholder fallback handling)
- descriptor-driven event lookup (`BlockDescriptors.descriptorForEvent`)
- motion-token-aligned block editor animations and improved connector rendering

---

## Core Features

### Project Management
- create/search/delete projects
- project metadata + target output settings
- theme configuration support

### Canvas Editor
- drag/drop visual editing of element nodes
- properties and style editing
- undo/redo support
- page-aware editing workflow

### Block / Interaction Editor
- event-driven block chains per element/page context
- nested block support via parent-child relationships
- block enable/disable + collapse behavior
- drag-and-drop top-level reordering flow

### Code Generation
- shared `CodeGenerator` contract
- current concrete generator: `HtmlCodeGenerator`
- Android-side interaction code generation via `BlockJavaScriptGenerator`

---

## Tech Stack

- Kotlin `2.0.21`
- Gradle `8.9`
- Android Gradle Plugin `8.5.2`
- Java `17`
- Compose Multiplatform `1.7.0` + Material 3
- Navigation Compose
- Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`)
- Coroutines + Flow
- Room `2.7.0`
- kotlinx.serialization
- Ktor client
- Coil
- DataStore Preferences
- ktlint + detekt + Android lint

---

## Repository Structure

```text
webforge-studio/
├─ androidApp/
│  └─ src/main/kotlin/com/webforge/studio/
│     ├─ ui/                    # Compose screens/components/theme
│     ├─ engine/                # Android-side JS block generation
│     ├─ di/                    # Hilt modules
│     ├─ domain/usecase/        # Domain use cases
│     └─ network/               # Ktor client setup
├─ shared/
│  ├─ src/commonMain/kotlin/com/webforge/studio/
│  │  ├─ model/                 # Shared models and block descriptors
│  │  ├─ engine/                # Code generation contracts + HTML generator
│  │  └─ repository/            # Shared repository contracts
│  └─ src/androidMain/kotlin/com/webforge/studio/repository/
│     └─ ProjectRepositoryImpl.kt  # Room entities/DAO/DB/repository impl
├─ shared/schemas/              # Exported Room schemas
└─ .github/workflows/           # CI workflows
```

---

## Navigation

Main routes:
- `home`
- `new_project`
- `canvas/{projectId}`
- `block_editor/{projectId}?elementId={elementId}`

---

## Prerequisites

- Android Studio (latest stable recommended)
- JDK 17
- Android SDK 35 (compile/target)

---

## Getting Started

1. Clone this repository.
2. Open in Android Studio.
3. Ensure JDK 17 and Android SDK are configured.
4. Sync Gradle.
5. Run the `androidApp` app configuration on emulator/device.

CLI examples:

```bash
./gradlew :androidApp:assembleDebug
./gradlew check build
```

---

## Quality & Validation

Common checks:

```bash
./gradlew check build
```

You can also run module-specific checks as needed (ktlint, detekt, lint, unit tests).

---

## Data Layer Notes

- Room schemas are exported in `shared/schemas` (`shared/build.gradle.kts`).
- Current Room database version is `3`.
- Development configuration currently uses destructive migration fallback.

---

## License

No LICENSE file is currently included. Add one to define usage terms.
