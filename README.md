# Cashflowin - Native Android Application

Welcome to the **Cashflowin** mobile repository. This application is the Android interface for the personal financial management ecosystem, focusing on speed, clean architecture, and reliable financial planning.

## 📱 Tech Stack
- **Language**: Kotlin
- **Networking**: Retrofit, OkHttp, Coroutines
- **UI Framework**: Material Design 3, XML Layouts
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Dependency Management**: Gradle (Kotlin DSL)

## 🏗️ Architecture & DDD (Domain-Driven Design)
The project adheres to **DDD** principles within the **Cashflow Management** Bounded Context:

1.  **Domain Layer (Core)**: Contains the business logic and definitions of money management (Transactions, Assets, Goals). It is independent of UI and Database.
2.  **Infrastructure Layer**: Handles external communication (Retrofit API, DB) and maps them into Domain Models.
3.  **UI/Presentation Layer**: Renders the state and handles User interactions via ViewModels.

## 🧪 TDD (Test-Driven Development) Strategy
We strive for high test coverage following the TDD philosophy:

- **Unit Tests (`app/src/test`)**: Test all business logic in ViewModels and data mapping in Repositories.
- **Instrumented Tests (`app/src/androidTest`)**: Test UI flows and critical user journeys (e.g., successful login).
- **Mocking**: Use `MockK` or `Mockito` to isolate components during testing.

## 📚 Documentation
- [AI Advisor Concepts](documentation/concepts/AI_ADVISOR.md)
- [Recurring Transactions Feature](documentation/features/RECURRING_TRANSACTIONS.md)
- [Backend Export Capability](documentation/history/BACKEND_EXPORT_FEATURE.md)
- [Archived Crash Logs](documentation/logs/CRASH_LOG_20260318.md)

## 🚀 Getting Started
1. Clone the repository.
2. Open in **Android Studio** (Koala or later).
3. Sync Project with Gradle Files.
4. Run on a physical device or emulator (API 29+).

---
**Maintained by**: Rachmad & AI Advisor  
**Version**: 1.1.0 (Stable)
