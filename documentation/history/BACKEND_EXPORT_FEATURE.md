# Historical Report: Backend Export Integration (PDF/CSV)

This feature enables annual and monthly financial report exports (PDF and CSV) for the Cashflowin application.

## Overview
Successfully integrated financial reporting capabilities by enhancing the `ReportController.php` within the Laravel backend.

## 1. Feature Specifications
- **Report Types**: (1) Monthly Report, (2) Annual Report.
- **Formats**: PDF and CSV.
- **Default Behavior**: If month = empty, default to annual report (month = 'all').

## 2. API Endpoints (Android Integration)
The mobile app can now request reports with the following parameters:

```http
GET /api/reports/export/pdf?year=2024
GET /api/reports/export/csv?year=2024
```

### Standard Monthly Export
```http
GET /api/reports/export/pdf?month=03&year=2024
GET /api/reports/export/csv?month=03&year=2024
```

## 3. Integration & Testing (TDD)
Before calling these from the mobile app, ensure:
- [x] Backend authentication token is included in the header.
- [x] Error handling for 403 (Unauthorized) scenarios.
- [x] File stream handling in Android (Retrofit/OkHttp).

Manual verification was performed by triggering the endpoints and verifying the output files' integrity.

---
**Status:** Completed  
**Last Updated:** March 30, 2026
