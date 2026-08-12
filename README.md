# WAR-KIT

**Idle Warung Indonesia** — game simulasi idle mengelola warung menjadi jaringan terbesar di Indonesia.

| Item | Keterangan |
|------|-----------|
| Nama Aplikasi | **WAR-KIT** |
| Package Name | `com.altomedia.warkit` |
| Developer | **ALTOMEDIA** |
| Contact | altomediaindonesia@gmail.com |
| Platform | Android 2D (native Kotlin + Canvas) |
| minSdk | 21 (Android 5.0) |
| targetSdk | 37 |

## Build

```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

## Struktur Proyek

```
app/src/main/java/com/altomedia/warkit/
├── core/      GameState, CustomerSpawner, SaveManager
├── data/      ProductCatalog, ShelfConfig, MissionBank
├── game/      GameEngine, GameView (Canvas 2D), CircleButton
├── model/     Product, Customer, SellerCharacter, Mission
└── ui/        MainActivity, dialogs (Intro, Character, Warehouse, Upgrade, Product, Mission)
```

## Fitur (GDD BAB 1–10)

- **BAB 1–2**: Intro cerita, kondisi awal (Rp500.000, 2 rak, 5 produk), tombol Buka Warung
- **BAB 3**: AI pelanggan (kecepatan, daftar belanja, kesabaran, kepuasan)
- **BAB 4**: Sistem stok & gudang, beli dari supplier, isi rak
- **BAB 5**: Keuntungan + pendapatan idle (kasir bekerja walau offline)
- **BAB 6**: Upgrade rak (kapasitas, estetika, reputasi; visual kayu→besi)
- **BAB 7**: 5 tipe pelanggan (Ibu RT, anak sekolah, ojol, petani, karyawan) + VIP
- **BAB 8**: Sistem level & unlock (produk, rak, pegawai, dekorasi, cabang)
- **BAB 9**: Misi utama & harian dengan reward (uang, diamond, EXP, booster, chest)
- **BAB 10**: Transisi warung dikenal

## Karakter Penjual

Bu Sari, Andi, Pak Budi, Nek Wati — masing-masing dengan bonus pasif unik.

---


INTRODUCTION KARAKTER PENJUAL

1. IBU RUMAH TANGGA

Nama: Bu Sari

Selama bertahun-tahun, Bu Sari mengelola warung kecil di depan rumah untuk membantu memenuhi kebutuhan keluarganya. Dengan keramahan dan senyumnya, ia mengenal hampir semua pelanggan yang datang. Baginya, warung bukan sekadar tempat berjualan, tetapi juga tempat berkumpulnya warga sekitar.

Keunggulan:

- Pelayanan lebih ramah.
- Pelanggan lebih sering datang kembali.
- Bonus reputasi lebih tinggi.

---

2. PEMUDA

Nama: Andi

Andi adalah pemuda yang penuh semangat dan memiliki mimpi membangun usaha besar dari sebuah warung sederhana. Ia cepat belajar, suka mencoba hal baru, dan tidak takut menghadapi tantangan. Dengan kerja keras, ia ingin membuktikan bahwa usaha kecil bisa berkembang menjadi bisnis besar.

Keunggulan:

- Kecepatan kerja tinggi.
- Upgrade lebih cepat.
- Bonus pengalaman (EXP) lebih besar.

---

3. BAPAK-BAPAK

Nama: Pak Budi

Pak Budi memiliki pengalaman panjang dalam berdagang. Ia teliti mengatur stok, pandai menghitung keuntungan, dan selalu menjaga kualitas pelayanan. Pelanggan percaya karena kejujuran dan pengalamannya.

Keunggulan:

- Keuntungan penjualan meningkat.
- Pengelolaan stok lebih efisien.
- Biaya operasional lebih hemat.

---

4. NENEK

Nama: Nek Wati

Warung milik Nek Wati sudah dikenal sejak puluhan tahun lalu. Banyak pelanggan datang bukan hanya untuk berbelanja, tetapi juga untuk berbincang dan menikmati suasana hangat yang ia ciptakan. Pengalaman panjang membuatnya tahu kebutuhan setiap pelanggan.

Keunggulan:

- Pelanggan VIP lebih sering muncul.
- Reputasi meningkat lebih cepat.
- Pendapatan offline lebih besar.

---

MEMILIH KARAKTER

Setiap karakter memiliki kemampuan unik yang memengaruhi cara bermain, tetapi seluruh karakter tetap dapat membuka semua fitur, produk, dan cabang warung. Pilih karakter yang paling sesuai dengan strategi bisnismu, lalu kembangkan warung kecil menjadi jaringan toko terbesar di Indonesia.



INTRODUCTION

Selamat Datang di Idle Warung Indonesia!

Di sebuah desa kecil yang damai, berdirilah sebuah warung sederhana yang telah melayani warga selama bertahun-tahun. Namun seiring berjalannya waktu, warung itu mulai sepi, bangunannya menua, dan stok barang semakin berkurang. Kini, warung tersebut berada di ambang penutupan.

Suatu hari, kamu mendapatkan kesempatan untuk mengambil alih dan menghidupkan kembali warung itu. Dengan modal yang sangat terbatas, kamu harus bekerja keras melayani pelanggan, mengisi stok barang, mengembangkan usaha, serta mengelola keuangan dengan bijak.

Mulailah dari warung kecil di pinggir desa. Jual kebutuhan sehari-hari, tingkatkan kualitas pelayanan, rekrut pegawai, buka cabang baru, dan perluas bisnismu hingga menjangkau berbagai kota di seluruh Indonesia.

Perjalananmu tidak akan selalu mudah. Harga barang dapat berubah, pelanggan memiliki kebutuhan yang berbeda, musim dan hari besar akan memengaruhi penjualan, serta persaingan bisnis akan semakin ketat. Setiap keputusan yang kamu ambil akan menentukan seberapa cepat warungmu berkembang.

Bangun reputasi terbaik, ciptakan pelayanan yang memuaskan, dan jadikan warungmu sebagai pilihan utama masyarakat. Dari sebuah warung sederhana, buktikan bahwa kerja keras dan strategi yang tepat dapat mengantarkanmu menjadi pemilik jaringan warung terbesar di Indonesia.

Kini, perjalananmu dimulai.

Buka pintu warung, sambut pelanggan pertama, dan tuliskan kisah suksesmu!
