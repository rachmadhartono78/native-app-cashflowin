Aplikasi mobile sudah memiliki Design System berbasis Antigravity Native dengan dua mode: Light dan Dark, serta satu brand color utama yaitu Green (lengkap dengan primary-50 sampai primary-900). Tugasnya adalah menambahkan satu pilihan brand color baru di pengaturan yaitu “Cashflowin Web Theme Color”, yang mengikuti gaya warna dari dashboard Cashflowin versi web.

Spesifikasi brand color baru ini:

Aksen utama: Indigo (#6366F1) → Purple (#A855F7) sebagai primary-500 range

Gaya premium, modern, clean

Warna dibuat dalam token ramp: primary-50, 100, 200 … 900

Rendah saturasi tapi tetap vivid, seperti trend indigo/purple fintech modern

Tone utama mengikuti karakter warna dari web Cashflowin

Tetap mempertahankan accent gradient Indigo → Purple untuk komponen highlight

Untuk Light Mode (Cashflowin Color):

Background: #F8FAFC

Surface: glassmorphism frosted (opa 0.1–0.2 + blur)

Primary ranges dari indigo-purplish dengan primary-500 = #6366F1

Secondary adaptif ke ungu muda

Untuk Dark Mode (Cashflowin Color):

Background: #020617 (navy gelap)

Surface: frosted dark (opa 0.12–0.18)

Primary ranges dibuat lebih deep & richer

Pastikan primary-300 sampai primary-700 tetap kontras AA 4.5:1

Sistem tema harus:

Mampu switch antara Green dan Cashflowin Web Color secara realtime

Tidak merusak tema Green yang sudah ada

Tetap mendukung Light/Dark mode

Menerapkan brand color baru pada seluruh komponen:
Appbar, Buttons, FAB, Tabs, Cards, Inputs, Chips, Bottom Navigation, Snackbar, Modal, BottomSheet

Tidak mengubah warna success (#10B981), error (#EF4444), warning

Tidak mengubah accent gradient default

Output yang diperlukan:

Ramp warna Cashflowin 50–900

Struktur tema lengkap untuk kedua mode (Light & Dark)

Mapping brand color ke komponen

Fallback colors untuk dark mode

Contoh JSON theme yang siap dipakai

Penjelasan integrasi switching brand color

Cara memastikan glassmorphism aman di mobile

Tujuan akhirnya:
User aplikasi dapat memilih Green atau Cashflowin Web Style sebagai warna aplikasi, dan seluruh UI menyesuaikan otomatis tanpa reload.
