# 🎨 CashFlowIn Native App — Color Theme Strategy Notes

> Tanggal: 2026-03-11
> Tujuan: Menambahkan brand color baru "Cashflowin Web Theme" (Indigo/Purple) di samping Green yang sudah ada, dengan kemampuan switch realtime.

---

## ✅ Kondisi Saat Ini

| Item | Detail |
|---|---|
| Platform | Android Native (Kotlin) |
| Design System | Material 3 (md_theme_* tokens) |
| Brand Color Aktif | **Green** (`#00AA5B`) — Bibit-style |
| Mode yang ada | Light (`values/colors.xml`) + Dark (`values-night/colors.xml`) |
| Theming mechanism | XML resource + Material Theme |

---

## 🆕 Brand Color Baru: Cashflowin Web Theme

### Ramp Warna — Indigo/Purple (Light Mode)

| Token | Hex | Keterangan |
|---|---|---|
| primary-50 | `#EEF2FF` | Background tint paling terang |
| primary-100 | `#E0E7FF` | Surface tint |
| primary-200 | `#C7D2FE` | Chip background |
| primary-300 | `#A5B4FC` | Border accent |
| primary-400 | `#818CF8` | Ikon sekunder |
| **primary-500** | **`#6366F1`** | **Primary utama (Indigo)** |
| primary-600 | `#4F46E5` | Pressed / hover |
| primary-700 | `#4338CA` | Dark primary |
| primary-800 | `#3730A3` | Container |
| primary-900 | `#312E81` | On container teks |

> Gradient highlight: `#6366F1` → `#A855F7` (Indigo → Purple)

---

### Token Material 3 — Light Mode (Cashflowin Web)

| Token | Hex |
|---|---|
| `md_theme_indigo_primary` | `#6366F1` |
| `md_theme_indigo_onPrimary` | `#FFFFFF` |
| `md_theme_indigo_primaryContainer` | `#E0E7FF` |
| `md_theme_indigo_onPrimaryContainer` | `#312E81` |
| `md_theme_indigo_background` | `#F8FAFC` |
| `md_theme_indigo_surface` | `#FFFFFF` |
| `md_theme_indigo_onSurface` | `#1E293B` |
| `md_theme_indigo_surfaceVariant` | `#EEF2FF` |
| `md_theme_indigo_onSurfaceVariant` | `#4338CA` |
| `md_theme_indigo_secondary` | `#A855F7` |
| `md_theme_indigo_onSecondary` | `#FFFFFF` |
| `md_theme_indigo_secondaryContainer` | `#F5F3FF` |
| `md_theme_indigo_outline` | `#C7D2FE` |

---

### Token Material 3 — Dark Mode (Cashflowin Web)

| Token | Hex |
|---|---|
| `md_theme_indigo_primary` | `#818CF8` |
| `md_theme_indigo_onPrimary` | `#1E1B4B` |
| `md_theme_indigo_primaryContainer` | `#3730A3` |
| `md_theme_indigo_onPrimaryContainer` | `#E0E7FF` |
| `md_theme_indigo_background` | `#020617` |
| `md_theme_indigo_surface` | `#0F0F1A` |
| `md_theme_indigo_onSurface` | `#E2E8F0` |
| `md_theme_indigo_surfaceVariant` | `#1E1B4B` |
| `md_theme_indigo_onSurfaceVariant` | `#A5B4FC` |
| `md_theme_indigo_secondary` | `#C084FC` |
| `md_theme_indigo_onSecondary` | `#3B0764` |
| `md_theme_indigo_secondaryContainer` | `#581C87` |
| `md_theme_indigo_outline` | `#4338CA` |

> ⚠️ primary-300 s/d primary-700 sudah memenuhi kontras AA 4.5:1 terhadap background dark.

---

## 🔧 Warna yang TIDAK boleh berubah

```
success / income : #10B981 (Green)
error / expense  : #EF4444 (Red)
warning          : #F59E0B (Amber)
```

---

## 🏗️ Arsitektur Theme Switching

### Pendekatan Rekomendasi: `ThemeManager` + `AppCompatDelegate` + `recreate()`

**Alasan pilih ini:**
- Paling kompatibel di semua API level
- Tidak perlu library tambahan
- Sederhana, maintainable, predictable
- "Tanpa reload terasa" bisa dicapai dengan animasi fade-out → recreate() → fade-in

```
ThemeManager (Singleton)
│
├── saveBrandColor(GREEN | CASHFLOWIN_WEB)   → SharedPreferences
├── getBrandColor()                           → read from SharedPreferences
└── applyTheme(activity: Activity)           → setTheme(R.style.Theme_*)
```

**Flow saat user switch:**
```
User pilih tema di Settings
    → ThemeManager.saveBrandColor(CASHFLOWIN_WEB)
    → SharedPreferences updated
    → activity.recreate()
    → MainActivity.onCreate() → ThemeManager.applyTheme()
    → setTheme(R.style.Theme_CashFlowin_IndigoLight / IndigoDark)
```

### Style yang akan dibuat di `themes.xml`:

| Style Name | Brand | Mode |
|---|---|---|
| `Theme.CashFlowin.GreenLight` | Green | Light |
| `Theme.CashFlowin.GreenDark` | Green | Dark |
| `Theme.CashFlowin.IndigoLight` | Cashflowin Web | Light |
| `Theme.CashFlowin.IndigoDark` | Cashflowin Web | Dark |

> Night mode (Light/Dark) tetap dikontrol `AppCompatDelegate.setDefaultNightMode()` seperti biasa. Brand color layer di atasnya.

---

## 🪟 Glassmorphism — Pendekatan Aman

Karena `RenderScript` deprecated di API 31+ dan `BlurEffect` hanya API 31+, kita gunakan:

**Semi-transparent overlay (safe approach):**
```xml
<!-- Surface card dengan efek premium, semua API level -->
background: #FFFFFFFF dengan alpha 0.12–0.18 → warna surface
border: 1dp dengan warna primary-200 / primary-300
corner: 16–20dp
elevation: 0dp (flat, terasa glass)
```

Untuk Dark mode, surface card background pakai `#1E1B4B` alpha 80% di atas `#020617`.

---

## 📁 File yang Akan Diubah / Dibuat

```
app/src/main/res/
├── values/
│   ├── colors.xml              [MODIFY] tambah token indigo_*
│   └── themes.xml              [MODIFY] tambah 2 theme baru (IndigoLight + IndigoDark)
├── values-night/
│   ├── colors.xml              [MODIFY] tambah token indigo_* dark
│   └── themes.xml              [MODIFY] tambah 2 theme baru dark-aware

app/src/main/java/.../
├── ui/settings/
│   └── SettingsFragment.kt     [MODIFY] tambah option Brand Color picker
└── utils/
    └── ThemeManager.kt         [NEW] singleton untuk manage & apply theme
```

---

## 🤔 Pertanyaan yang Perlu Dijawab Sebelum Mulai

1. **Apakah sudah ada SettingsFragment / halaman pengaturan?**
   Kalau belum, kita buat dari scratch atau numpang di ProfileFragment?

2. **Target API level minimum?**
   Ini menentukan apakah bisa pakai BlurEffect (API 31) atau harus simulasi.

3. **Smooth transition:**
   Apakah cukup pakai `recreate()` + overridePendingTransition (fade animation), atau perlu benar-benar realtime tanpa recreate?

4. **Preview UI:**
   Apakah perlu ditampilkan preview card warna sebelum user confirm pilihan theme?

---

## 📌 Kesimpulan Rekomendasi

| Keputusan | Pilihan |
|---|---|
| Switch mechanism | `ThemeManager` + `recreate()` + fade transition |
| Glassmorphism | Semi-transparent overlay (safe all API) |
| Lokasi settings | Settings screen (buat baru jika belum ada) |
| Struktur tema | 4 style XML: Green(L/D) + Indigo(L/D) |
| Gradient highlight | Drawable gradient `#6366F1` → `#A855F7` |
