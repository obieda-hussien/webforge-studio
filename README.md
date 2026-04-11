# WebForge Studio

**WebForge Studio** is an Android-first visual web builder IDE built with Kotlin, Jetpack Compose, and a shared Kotlin Multiplatform module.

It lets you create web projects, compose UI blocks visually on a canvas, manage pages, and generate starter source code.

---

## Overview

WebForge Studio combines:
- A **project dashboard** (create, browse, search, delete projects)
- A **3-step project creation wizard**
- A **visual canvas editor** with draggable elements
- A **properties panel** for editing element attributes/styles
- **Undo/redo history**
- **Page management** (multi-page projects)
- **Code generation** (currently HTML generator implementation)

---

## Key Features

### 1) Project Management
- Create projects with:
  - Name + slug
  - Description
  - Target platform (HTML / React / React+TS / PWA)
  - Theme preferences (color seed, font pair, dark mode)
- Search projects from home screen
- Delete existing projects

### 2) Visual Canvas
- Add elements from palette (`CONTAINER`, `TEXT`, `IMAGE`, `BUTTON`, `INPUT`, `LINK`, `DIVIDER`, `CUSTOM`)
- Select, move, relabel, and delete elements
- Edit custom style/attribute properties
- Zoom and pan support
- Toggle element palette and properties panel visibility

### 3) Editing Productivity
- Command-based **undo/redo** (history limit: 50 actions)
- Real-time state updates via `StateFlow` + lifecycle-aware collection

### 4) Multi-Page Project Support
- Add/select pages per project
- Persist and observe pages reactively

### 5) Code Generation
- `CodeGenerator` abstraction with pluggable backends
- Current default backend: `HtmlCodeGenerator`
- Generates output bundle (`GeneratedCode`) as file path → file content map

---

## Tech Stack

### Core
- **Kotlin** `2.0.21`
- **Gradle** `8.9`
- **AGP** `8.5.2`
- **Java** `17`

### UI / Android
- Jetpack Compose + Material 3
- Navigation Compose
- Lifecycle ViewModel + Compose lifecycle integration

### Architecture / DI
- Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`)
- MVI-style sealed UI states in ViewModels
- Use-case-driven domain layer

### Data / Networking
- Room (`v2` DB schema currently exported)
- Coroutines + Flow
- kotlinx.serialization
- Ktor client (Android engine)

### Quality
- ktlint
- detekt
- Android lint
- GitHub Actions CI pipeline

---

## Project Structure

```text
webforge-studio/
├─ androidApp/                       # Android application module (UI, navigation, DI, domain use cases)
│  └─ src/main/kotlin/com/webforge/studio/
│     ├─ ui/                         # Compose screens/components/theme
│     ├─ domain/usecase/             # Application use cases
│     ├─ di/                         # Hilt module/providers
│     └─ network/                    # Ktor HttpClient factory
├─ shared/                           # Kotlin Multiplatform shared module
│  ├─ src/commonMain/kotlin/com/webforge/studio/
│  │  ├─ model/                      # Shared data models
│  │  ├─ engine/                     # Code generation contracts/implementations
│  │  └─ repository/                 # Repository interfaces
│  └─ src/androidMain/kotlin/com/webforge/studio/repository/
│     └─ ProjectRepositoryImpl.kt    # Room entities, DAOs, DB, Android repository impls
├─ shared/schemas/                   # Exported Room schema JSON files
└─ .github/workflows/build.yml       # CI/CD pipeline
```

---

## Architecture Notes

- **Single-activity Compose app** (`MainActivity`)
- **Unidirectional state flow** from ViewModel to UI
- **Repository pattern** with Room-backed Android implementations
- **Use cases** separate domain operations from UI state management
- **DI graph** assembled in `AppModule`

Main navigation routes:
- `home`
- `new_project`
- `canvas/{projectId}`

---

## Prerequisites

- JDK 17
- Android SDK (compile/target SDK 35)
- Android Studio (latest stable recommended)

---

## Getting Started

1. Clone the repository.
2. Open it in Android Studio.
3. Ensure Android SDK + JDK 17 are configured.
4. Sync Gradle.
5. Run the `androidApp` configuration on an emulator/device.

CLI build:

```bash
./gradlew :androidApp:assembleDebug
```

---

## Quality Checks

Run formatting/lint/static analysis:

```bash
./gradlew ktlintCheck detekt :androidApp:lint :shared:lint --no-configuration-cache --continue
```

Run unit tests:

```bash
./gradlew :androidApp:testDebugUnitTest :shared:testDebugUnitTest --no-configuration-cache --continue
```

---

## CI Pipeline

GitHub Actions workflow (`.github/workflows/build.yml`) includes:
- Code quality (ktlint, detekt, lint)
- Unit test stage + coverage artifact publishing
- Security/dependency scan stages
- Build/release oriented jobs and artifacts

Triggers:
- Push to `main` / `develop`
- Pull requests to `main` / `develop`
- Manual dispatch (`workflow_dispatch`) with optional inputs

---

## Current Scope & Notes

- The code generator contract supports multiple targets, while the provided concrete generator is HTML.
- Room DB currently uses destructive migration fallback during active development.
- Room schemas are exported under `shared/schemas`.

---

## License

No license file is currently included in this repository.  
If needed, add a `LICENSE` file to define usage terms.
