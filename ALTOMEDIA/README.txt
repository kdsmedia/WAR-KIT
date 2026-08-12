================================================================================
              PAKET RILIS WARKIT — INDEKS & RINGKASAN
================================================================================
Aplikasi   : WARKIT — Idle Warung Indonesia
Package    : com.altomedia.warkit
Developer  : ALTOMEDIA
Kontak     : altomediaindonesia@gmail.com
Versi      : 1.0 (versionCode 1, versionName "1.0")
Tanggal    : 12 Agustus 2026
================================================================================

STRUKTUR PAKET:

ALTOMEDIA/
├── README.txt                        (file ini — indeks paket rilis)
├── generate_assets.py                (script generate gambar store)
│
├── release/                          (ARTIFAK BUILD — siap upload)
│   ├── WARKIT-v1.0-release.aab       (Android App Bundle — untuk Play Store)
│   ├── WARKIT-v1.0-release.apk       (APK signed — untuk testing)
│   └── WARKIT-v1.0-debug.apk         (APK debug — untuk testing internal)
│
├── store-listing/                    (ASET GRAFIS UNTUK PLAY CONSOLE)
│   ├── ic_launcher_512.png           (Icon aplikasi 512x512)
│   ├── ic_launcher_round_512.png     (Icon bulat 512x512)
│   ├── feature_graphic_1024x500.png  (Feature graphic)
│   ├── screenshot_1_mobile.png       (Screenshot 1 — 1080x1920)
│   ├── screenshot_2_mobile.png       (Screenshot 2 — 1080x1920)
│   ├── screenshot_3_mobile.png       (Screenshot 3 — 1080x1920)
│   └── promo_graphic_180x120.png     (Promo graphic opsional)
│
├── legal/                            (DOKUMEN LEGAL)
│   ├── Privacy_Policy.txt            (Kebijakan Privasi — panjang & detail)
│   └── Terms_of_Service.txt          (Syarat & Ketentuan Layanan)
│
├── release-notes/                    (CATATAN RILIS)
│   └── Release_Notes_v1.0.txt        (Catatan rilis v1.0 + template)
│
└── docs/                             (PANDUAN & DOKUMENTASI)
    ├── Upload_Guide_PlayConsole.txt  (Panduan upload langkah-demi-langkah)
    ├── Store_Listing_Details.txt     (Konten listing siap copy-paste)
    └── Blog_Article_WARKIT.txt       (Artikel blog 7.500+ karakter)

================================================================================

VERIFIKASI TEKNIS BUILD:

✓ Build SUCCESSFUL (assembleDebug, assembleRelease, bundleRelease)
✓ compileSdk 37, targetSdk 37, minSdk 21
✓ AAB signed (V2 signer) dengan keystore ALTOMEDIA.jks
  - Alias: kdsmedia
  - Subject: CN=ALTOMEDIA, OU=Developer, O=ALTOMEDIA, L=Karawang, ST=Jawa Barat, C=ID
  - Validitas: 10000 hari (sampai 28 Des 2053)
  - SHA-1:  4A:F3:CA:1E:D9:94:0D:D8:72:E8:C8:76:E7:AD:AE:05:46:E6:C4:14
  - SHA-256: D6:1B:98:A9:D9:0B:DD:FB:A1:2D:3D:0A:F8:6D:63:AF:9B:26:1E:7A:9F:51:76:E2:D7:F5:3B:2D:D3:E6:01:F3
✓ Tidak ada bug/error compile
✓ Tidak ada kode dummy/simulasi/placeholder (seluruh fitur fungsional)
✓ Aplikasi: WARKIT (di strings.xml)
✓ Package: com.altomedia.warkit (applicationId)
✓ Orientasi: landscape
✓ Native Kotlin + Canvas 2D, tanpa engine berat
✓ Offline, tanpa iklan & tanpa IAP pada v1.0

UKURAN BUILD:
  - APK debug:   ~3.3 MB
  - APK release: ~2.6 MB
  - AAB release: ~2.5 MB (akan lebih kecil di perangkat karena AAB)

================================================================================

IDENTITAS APP (SEPERTI DIMINTAKAN):
  Nama Aplikasi    : WARKIT
  Package Name     : com.altomedia.warkit
  Developer        : ALTOMEDIA
  Contact          : altomediaindonesia@gmail.com

KEYSTORE:
  File             : ALTOMEDIA.jks (di /workspace, di-gitignore, TIDAK di-commit)
  Keystore Password: Kdsmedia@123
  Alias            : kdsmedia
  Key Password     : Kdsmedia@123
  Validity         : 10000 hari
  CN=ALTOMEDIA, OU=Developer, O=ALTOMEDIA, L=Karawang, ST=Jawa Barat, C=ID

CATATAN: File ALTOMEDIA.jks sengaja di-exclude dari repository (.gitignore:
*.jks *.keystore) demi keamanan. Simpan file keystore di tempat aman dan
jangan pernah membagikan password. Untuk build di mesin lain, salin
ALTOMEDIA.jks ke /workspace/ (path absolut di app/build.gradle:
file("/workspace/ALTOMEDIA.jks")).

================================================================================

LANGKAH RILIS BERIKUTNYA:
  1. Hosting Privacy_Policy.txt & Terms_of_Service.txt di URL publik.
  2. Login Play Console (altomediaindonesia@gmail.com).
  3. Ikuti Upload_Guide_PlayConsole.txt.
  4. Upload WARKIT-v1.0-release.aab.
  5. Isi store listing (Store_Listing_Details.txt) + unggah gambar.
  6. Isi content rating, data safety, target audience, ads (No).
  7. Submit untuk review.

Untuk build berikutnya, naikkan versionCode/versionName, rebuild, dan
tulis release notes baru (lihat template di Release_Notes_v1.0.txt).

================================================================================
                       AKHIR README PAKET RILIS
================================================================================
