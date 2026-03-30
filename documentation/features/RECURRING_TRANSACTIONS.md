# Feature: Recurring Transactions (Android Integration)

This document describes the implementation and testing of the **Recurring Transactions** feature in the native Android application.

## 1. Feature Definition
A **Recurring Transaction** is a primary entity in the **Cashflow Management** domain that represents a template for future financial activities.

## 2. Domain & Application Layers
The implementation follows a strict separation between the **Domain Model** and outer infrastructure layers.

- **UI**: Fragments/Activities for viewing and configuring recurring transaction templates.
- **ViewModel**: Application service that coordinates UI and Domain logic.
- **Repository**: Infrastructure adaptation that maps API-specific DTOs into the `Recurring` domain entity.
- **Domain Entity**: [RecurringTransaction](documentation/concepts/DOMAIN_MODEL.md).

## 3. API Integration
The feature coordinates with the following Laravel API endpoints:

- `GET /recurring-transactions` (List retrieval)
- `POST /recurring-transactions` (Transaction creation)
- `POST /recurring-transactions/{recurring}/pause` (Pause execution)
- `POST /recurring-transactions/{recurring}/resume` (Resume execution)

## 4. Verification & Testing Strategy (TDD)
Before manual testing, the following "Unit Test" scenarios were identified:

### 4.1 Unit Tests (Proposed)
- [ ] **Date Calculation**: Ensure the `next_execution_date` is correctly calculated for each frequency type (e.g., month + 1).
- [ ] **Data Mapping**: Verify that the JSON response matches the `RecurringTransaction` data model correctly.
- [ ] **Validation Logic**: Test the form's ability to catch missing required fields (Amount, Type, Start Date) before submitting to the API.

### 4.2 Integration Tests (Manual)
- [x] **403 Error Fix**: Verified that the route parameter `{recurring}` matches the backend resource binding correctly.
- [x] **State Sync**: Verified that calling "Pause" updates the UI status and color badges instantly.
- [x] **Navigation**: Confirm that the "Berulang" card navigates to the correct activity with all features functional.

## 5. UI/UX Polishing
- Uses **Material Design 3** components.
- Responsive handling for empty states and loading indicators.

---
**Status:** Integrated & Verified  
**Last Updated:** March 30, 2026
