# Contributing to Cashflowin (Clean Code & TDD Guidelines)

To maintain a high-quality codebase, all contributions must follow these "Clean Code" and **TDD (Test-Driven Development)** standards.

## 🧼 Clean Code & DDD Standards

### 1. Ubiquitous Language (Core Jargon)
Always use consistent terms in code, tests, and documentation to avoid confusion. See [DOMAIN_MODEL.md](documentation/concepts/DOMAIN_MODEL.md) for definitions:
- Use **Asset** (NOT Account/Dompet)
- Use **Income** (NOT Pemasukan)
- Use **Expense** (NOT Pengeluaran)
- Use **Recurring** (NOT Auto/Berulang)

### 2. Naming Conventions & Structure
- **Classes**: `PascalCase` (e.g., `TransactionRepository`)
- **Functions/Parameters**: `camelCase` (e.g., `getTransactionsByDate`)
- **Entities**: Keep entities in a `domain.model` package, decoupled from API responses.
- **Layouts**: `snake_case` (e.g., `activity_main.xml`)
- **Resources**: `snake_case` (e.g., `id_btn_save`)

### 3. Layered Protection
- **Domain Integrity**: Never pass an `ApiRequest` directly into a `ViewModel`. Always map it to a **Domain Model** first in the Repository.
- **S (Single Responsibility)**: A class/fragment should only have one reason to change.
- **O (Open/Closed)**: Use interfaces in repositories for easy switching in the future (e.g., swapping Retrofit for Ktor).

### 3. Function Design
- Functions should be short and do only one thing.
- Prefer `val` over `var` for immutability whenever possible.

## 🧪 TDD (Test-Driven Development) Workflow
We follow the **Red-Green-Refactor** cycle:

1.  **RED**: Write a failing unit test for the new functionality.
2.  **GREEN**: Write the *minimum* amount of code to make the test pass.
3.  **REFACTOR**: Clean up the code while ensuring the test still passes.

### Requirements
- [ ] No new feature should be merged without matching unit tests in `app/src/test`.
- [ ] Logic that interacts with the Android framework (Context, Views) should be isolated using the **ViewModel** pattern for easier testing.

## 🔨 Code Review Checklist
Before submitting a Pull Request, ensure:
- [x] All unit tests pass locally.
- [x] No `TODO` comments are left in the main codebase.
- [x] Strings are externalized to `res/values/strings.xml`.
- [x] API calls are asynchronous using Kotlin Coroutines.

---
**Standard Created:** March 30, 2026
