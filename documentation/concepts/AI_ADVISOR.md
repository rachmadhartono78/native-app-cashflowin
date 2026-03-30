# AI Financial Advisor - Concepts & Prompts

This document outlines the implementation and testing standards for the **AI Advice** feature in the Cashflowin application.

## 1. System Role (Identity)
Define the AI's persona with clear boundaries to ensure consistent advice.

> "You are **Cashflowin AI**, a Certified Financial Planner (CFP) who is smart, friendly, and solution-oriented. Provide concise financial advice (max 3 paragraphs) based on user transaction data. Use a relaxed yet professional tone, provide motivation, and focus on savings and safe investments. If spending is excessive, provide firm yet polite warnings."

## 2. Structured Prompt Engineering (TDD Approach)
To ensure reliability, all prompts must follow this structure. This allows for "Unit Testing" of AI responses by varying the input data.

### Input Template (Unit Test Inputs)
```text
Halo Cashflowin AI, please analyze my finances this month:
- Total Income: [NOMINAL_INCOME]
- Total Expense: [NOMINAL_EXPENSE]
- Largest Category: [CATEGORY_NAME] ([PERCENTAGE]%)
- Balance: [BALANCE]
- Current Debt: [TOTAL_DEBT]

Question: [USER_QUESTION]
```

## 3. Expected Behavior (Success Criteria)
These examples serve as the "Integration Tests" for the model's output quality.

- **Case: High Food Spending**
    - *Input*: Food category at 40%.
    - *Expected*: Encouragement to cook at home and a calculation of potential savings.
- **Case: High Debt**
    - *Input*: Debt > 30% of income.
    - *Expected*: Urgent but polite warning with a focus on debt repayment before investing.

## 4. Implementation Guidelines (Clean Code)
- **Model Selection**: Use `gemini-1.5-flash` for high performance with low latency.
- **Data Privacy**: 
  - [x] Sanitize user data (remove account numbers/PII) before sending to Gemini SDK.
  - [x] Use a dedicated `AiService` or `AdvisorRepository` to handle API interactions.

## 5. Testing & Verification (TDD)
Before deploying prompt changes, verify with:
1. **Mock Responses**: Use local mock JSON to test the UI's handling of diverse advice lengths.
2. **Prompt Variance**: Test with extreme financial situations (zero income, massive debt) to ensure the system role remains stable.

---
**Version:** 1.1  
**Last Updated:** March 30, 2026
