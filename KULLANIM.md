# LPG Analiz — Kurulum ve Kullanım Kılavuzu

**Toyota Auris 2011 · 1.6 · 132 HP** için LPG tüketim ve maliyet analiz uygulaması.

## 1. Kurulum

1. `LPG-Analiz-v1.0.7.apk` dosyasını telefonunuza kopyalayın (veya doğrudan telefondan indirin).
2. Dosyaya dokunun. Android **"Bilinmeyen uygulamaları yükle"** izni isterse onaylayın.
3. **Yükle**'ye basın. Uygulama simgesi ana ekrana gelir.

Gereksinim: Android 6.0 ve üzeri. İnternet gerekmez; tüm işlemler cihazda yapılır.

## 2. İlk açılış

Uygulama, araç profilinizi "Toyota Auris 2011 · 1.6 · 132 HP" olarak hazır getirir.
Değiştirmek isterseniz: **Ayarlar** (sağ üst dişli simgesi) → araç bilgilerini düzenleyin → Kaydet.

> İpucu: Ayarlar'daki **"Başlangıç km"** alanına LPG takıldığı andaki kilometreyi girerseniz
> toplam yol hesabı daha doğru olur.

## 3. Pompada kayıt (her LPG alımında)

1. Ana ekranda **ana km göstergesini** girin (örn. 184500).
2. **"Fişi Çek"** düğmesine basın → fişi kamerayla çekin.
3. Uygulama fişi cihaz üzerinde okur; **litre, tutar, tarih, istasyon** otomatik dolar.
4. Gerekirse değerleri elle düzeltin, **"Depoyu tam doldurdum"** anahtarını kontrol edin.
5. **Kaydet**'e basın.

Notlar:
- Fiş fotoğrafı hiçbir yere gönderilmez, yalnızca telefonda saklanır.
- Fişi çekmeden de kayıt yapabilirsiniz: **"Elle"** düğmesiyle değerleri yazın.
- OCR okuyamazsa değerleri elle girin; kayıt yine de analizlere katılır.

## 4. Analizler

İkinci fişten itibaren **"Analiz & Grafikler"** ekranında:

| Gösterge | Anlamı |
|---|---|
| Ortalama tüketim (L/100 km) | İki dolum arası mesafeye göre hesaplanan ortalama |
| En iyi / en kötü | Kayıtlarınız arasındaki en düşük/yüksek tüketim |
| ₺/km ve toplam harcama | Kilometre başı maliyet ve toplam LPG gideri |
| Tüketim grafiği | Fiş bazında tüketim değişimi (noktaya dokununca o fişin değeri) |
| LPG litre fiyatı grafiği | Pompa fiyatının zaman içindeki değişimi |
| Aylık tüketim / harcama | Ay ay litre ve TL sütun grafikleri |
| Bir depoyla menzil | Ortalama dolum litresine göre tahmini menzil |

**Geçmiş Fişler** ekranında tüm fişleri, fotoğraflarıyla görür, düzeltebilir veya silebilirsiniz.

## 5. Tüketim nasıl hesaplanır?

Ardışık iki fiş arasındaki km farkı, ikinci fişte alınan litre ile kapatılmış sayılır
(tam dolum / tank-to-tank yöntemi).

Örnek: 100.000 km'de 30 L aldınız, 100.500 km'de 40 L aldınız →
500 km'de 40 L yakılmış → **8,00 L/100 km**.

En doğru sonuç için depoyu her seferinde **tam doldurun**.
Yarım dolumları işaretleyin; analiz ekranında belirtilir.

## 6. Verileriniz

- Tüm fişler ve fotoğraflar **yalnızca bu telefonda** saklanır.
- Uygulamayı kaldırırsanız veriler silinir.
- Ayarlar → **"Tüm kayıtları sil"** ile verileri istediğiniz zaman temizleyebilirsiniz.

## 7. Sorun giderme

| Sorun | Çözüm |
|---|---|
| Fiş okunamadı | Fotoğrafı daha aydınlık ortamda, fişin tamamı görünecek şekilde çekin; gerekirse değerleri elle girin |
| Km uyarısı | Girilen km son kayıttan büyük olmalı; göstergeyi kontrol edin |
| "Bilinmeyen uygulama" uyarısı | Ayarlar → Güvenlik → "Bilinmeyen kaynaklardan yükle" iznini bu yükleme için onaylayın |

---
LPG Analiz 1.0 · Kişisel kullanım
