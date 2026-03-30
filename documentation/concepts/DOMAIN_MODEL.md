# Domain Model: Cashflow Management

This document defines the core business logic of the Cashflowin application to ensure consistent behavior across all features.

## 1. Bounded Context
**Context Name**: Cashflow Management  
**Responsibility**: Tracking user income, expenses, assets, and financial goals.

## 2. Core Entities (The "What")
Entities are defined by their identity, not just their data.

- **Transaction**: Any movement of money (Income, Expense, Transfer).
  - *Rules*: Must have an amount > 0, a date, and be linked to an Asset.
- **Asset**: A location where money is stored (Bank, Cash, E-Wallet).
  - *Rules*: Total balance cannot be manually updated except through Transactions or specific Adjustments.
- **Category**: A classification for Transactions (Food, Rent, Salary).
- **Goal**: A financial milestone the user wants to reach.
- **Debt**: A financial obligation to another party.

## 3. Value Objects (The "How")
Immutables that describe a characteristic of an entity.

- **Amount**: Composed of `Value` (Decimal) and `Currency` (Standard: IDR).
- **TransactionType**: `Income`, `Expense`.
- **Frequency**: `Daily`, `Weekly`, `Monthly`, `Yearly`.

## 4. Domain Services (Business Logic)
Complex operations that don't belong to a single entity.

- **ForecastService**: Calculates the projected balance based on current Assets and upcoming Recurring Transactions.
- **BudgetAnalyzer**: Evaluates if a transaction will exceed the user's defined budget for a category.

## 5. Ubiquitous Language (Jargon)
Always use these terms in code, tests, and documentation:
- **Asset** (NOT Account/Dompet)
- **Income** (NOT Pemasukan)
- **Expense** (NOT Pengeluaran)
- **Recurring** (NOT Auto/Berulang)

---
**Version:** 1.0  
**Last Updated:** March 30, 2026
