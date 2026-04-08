# Rangkuman Dokumentasi Proyek Cashflowin

Dokumen ini berisi kumpulan catatan penting dan rangkuman fitur dari berbagai file dokumentasi teknis di proyek Cashflowin.

## 1. Domain Model (Inti Bisnis)
- **Konteks**: Pengelolaan arus kas (Cashflow Management).
- **Entitas Utama**:
    - **Transaction**: Pemasukan, Pengeluaran, Transfer (Jumlah > 0, terkait ke Asset).
    - **Asset**: Lokasi penyimpanan uang (Bank, Cash, E-Wallet). Saldo hanya diperbarui melalui Transaksi.
    - **Category**: Klasifikasi transaksi (Makanan, Sewa, Gaji).
    - **Recurring**: Transaksi berulang (Harian, Mingguan, Bulanan, Tahunan).
- **Istilah Baku (Ubiquitous Language)**: Gunakan "Asset" (bukan Dompet), "Income" (bukan Pemasukan), "Expense" (bukan Pengeluaran).

## 2. Fitur AI Financial Advisor
- **Persona**: "Cashflowin AI", Certified Financial Planner (CFP) yang ramah dan solutif.
- **Model**: `gemini-1.5-flash`.
- **Fungsi**: Menganalisis data transaksi (Total Income, Expense, Kategori terbesar, Saldo, Hutang) dan memberikan saran finansial maksimal 3 paragraf.

## 3. Fitur Recurring Transactions (Transaksi Berulang)
- **Status**: Terintegrasi dan Terverifikasi.
- **Endpoint API**:
    - `GET /recurring-transactions`
    - `POST /recurring-transactions`
    - `POST /recurring-transactions/{recurring}/pause` atau `/resume`
- **Validasi**: Perhitungan `next_execution_date` harus tepat sesuai frekuensi.

## 4. Fitur Export Laporan (Backend Integration)
- **Jenis Laporan**: Bulanan dan Tahunan.
- **Format**: PDF dan CSV.
- **Endpoint**:
    - `/api/reports/export/pdf?year=2024`
    - Parameter `month=all` digunakan untuk laporan tahunan.

## 5. Catatan Teknis & Bug (History)
- **Crash Log (18 Maret 2026)**:
    - Terdeteksi peringatan *Choreographer* (Skipped 31 frames).
    - **Rekomendasi**: Pastikan parsing JSON dan pemetaan data dilakukan di background thread (Coroutines) agar tidak memberatkan Main Thread.
- **Keamanan**: Selalu bersihkan PII (Personal Identifiable Information) sebelum mengirim data ke SDK Gemini.

## 6. Standar Pengembangan (TDD & Clean Code)
- Mengikuti pola **Red-Green-Refactor**.
- Arsitektur: **MVVM + Repository Pattern**.
- Mapping data dari DTO API ke Domain Model wajib dilakukan di layer Repository.

---
*Terakhir Diperbarui: 30 Maret 2026*
