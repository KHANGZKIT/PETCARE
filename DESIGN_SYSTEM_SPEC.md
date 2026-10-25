# PET CARE — HỆ THỐNG THIẾT KẾ & ÁNH XẠ ANDROID XML
> **Single Source of Truth** dùng để chuyển đổi giao diện từ nguyên mẫu React JSX (`petcare-ui`) sang mã nguồn Android Java + XML Views (`fe/app/src/main/res/`).

---

## 1. Token Màu (Color Tokens)
*Ánh xạ trực tiếp vào `fe/app/src/main/res/values/colors.xml`*

### 1.1. Màu Thương hiệu (Brand Teal)
| Tên JSX / CSS Variable | Mã HEX | Tên trong `colors.xml` | Ý nghĩa sử dụng |
|---|---|---|---|
| `--primary` | `#238C82` | `brand_primary` | Màu chủ đạo, nút bấm CTA chính, tab đang chọn, viền focus |
| `--brand-deep` | `#14534E` | `brand_deep` | Trạng thái nhấn (pressed/ripple), tiêu đề trên nền màu |
| `--brand-soft` | `#EAF7F4` | `brand_soft` | Nền chip mềm, nền icon, nền thẻ dịch vụ/ngày đang chọn |
| `--brand-ink` | `#16544C` | `brand_ink` | Màu chữ và icon tương phản cao trên nền `brand_soft` |

### 1.2. Màu Bề mặt & Nền (Surfaces & Text)
| Tên JSX / CSS Variable | Mã HEX | Tên trong `colors.xml` | Ý nghĩa sử dụng |
|---|---|---|---|
| `--background` | `#FBFAF7` | `surface_background` | Nền màn hình toàn app (trắng ngà ấm nhẹ) |
| `--card` / Popover | `#FFFFFF` | `surface_card` | Nền thẻ, card, bottom sheet, ô nhập liệu |
| `--muted` | `#F2F1EE` | `surface_muted` | Nền skeleton shimmer, chip phụ, khung giờ đóng |
| `--border` | `#E4E9E7` | `stroke_border` | Đường viền thẻ, divider, viền ô nhập mặc định |
| `--foreground` | `#1B2226` | `text_foreground` | Màu chữ chính (tiêu đề, nội dung quan trọng) |
| `--muted-foreground` | `#68757B` | `text_muted` | Màu chữ phụ, subtitle, thông tin meta, icon mờ |

### 1.3. Màu Trạng thái (Status Colors — 3 sắc độ)
Mỗi trạng thái bắt buộc có 3 cấp: **Chủ đạo** (viền/chấm tròn) · **Soft** (nền huy hiệu) · **Ink** (chữ hiển thị).

| Trạng thái | Mã Chủ đạo (`_main`) | Mã Nền nhạt (`_soft`) | Mã Chữ (`_ink`) | Áp dụng cho |
|---|---|---|---|---|
| **PENDING** (Chờ duyệt) | `#B57610` (`status_warning`) | `#FDF3E2` (`status_warning_soft`) | `#7A4E06` (`status_warning_ink`) | Lịch chờ phòng khám duyệt, cảnh báo |
| **CONFIRMED / COMPLETED / OPEN** | `#25885C` (`status_success`) | `#E8F6EE` (`status_success_soft`) | `#1E6B49` (`status_success_ink`) | Đã duyệt, hoàn thành, khung giờ còn chỗ |
| **REJECTED / DANGER** | `#C43B3B` (`status_destructive`) | `#FDEDED` (`status_destructive_soft`) | `#9A2B2B` (`status_destructive_ink`) | Lịch bị từ chối, nút xóa/hủy, lỗi nhập liệu |
| **NO_SHOW** (Không đến) | `#2072B4` (`status_info`) | `#EAF3FC` (`status_info_soft`) | `#1B5C92` (`status_info_ink`) | Khách vắng mặt, nhắc hẹn, thông tin |
| **CANCELLED / CLOSED** | `#68757B` (`status_neutral`) | `#F2F1EE` (`status_neutral_soft`) | `#4C5559` (`status_neutral_ink`) | Đã hủy, khung giờ đóng/hết chỗ |

---

## 2. Kiểu Chữ (Typography)
*Font chính:* **Plus Jakarta Sans** (Weights: 400 Regular, 500 Medium, 600 SemiBold, 700 Bold, 800 ExtraBold).  
*Ánh xạ vào `fe/app/src/main/res/values/styles.xml` hoặc `type.xml`*

| Cấp bậc chữ | Size (sp) | Weight | Line Height | Tracking | Tên Style đề xuất |
|---|---|---|---|---|---|
| **Tiêu đề màn hình** | `26sp` | 800 (ExtraBold) | ~32dp | `-0.02em` | `TextAppearance.PetCare.ScreenTitle` |
| **Tiêu đề App Bar** | `17sp` | 800 (ExtraBold) | ~22dp | tight | `TextAppearance.PetCare.AppBarTitle` |
| **Tiêu đề nhóm (Section)** | `16sp` | 800 (ExtraBold) | ~20dp | tight | `TextAppearance.PetCare.SectionTitle` |
| **Nội dung chính (Body)** | `14sp` (`13.5`) | 500 (Medium) | ~22dp (1.6) | normal | `TextAppearance.PetCare.Body` |
| **Nội dung phụ / Meta** | `12sp` (`12.5`) | 500 (Medium) | ~16dp | normal | `TextAppearance.PetCare.SubBody` |
| **Nhãn nhỏ / Kicker** | `11sp` | 800 (ExtraBold) | ~14dp | `+0.14em` (Uppercase) | `TextAppearance.PetCare.Kicker` |

---

## 3. Bo Góc, Khoảng Cách & Đổ Bóng (Radius, Spacing & Elevation)
*Ánh xạ vào `fe/app/src/main/res/values/dimens.xml`*

### 3.1. Thang Bo góc (Corner Radius)
- `radius_full`: `999dp` (Pill chip, Badge huy hiệu tròn, nút bo tròn hoàn toàn)
- `radius_sm`: `8dp`
- `radius_md`: `12dp` (Ô khung giờ `SlotButton`)
- `radius_lg`: `16dp` (`rounded-2xl` — Ô nhập liệu EditText, Nút bấm chuẩn, Avatar vuông)
- `radius_xl`: `24dp` (`rounded-3xl` — Thẻ Card phòng khám, Dialog giữa màn hình)
- `radius_2xl`: `28dp` (Header cover, Bottom Sheet đỉnh tròn)

### 3.2. Thang Khoảng cách (Spacing Scale)
- `spacing_2xs`: `4dp` (Khoảng cách Icon ↔ Text trong cùng hàng)
- `spacing_xs`: `8dp` (Padding của pill/badge, khoảng cách dòng nhỏ)
- `spacing_sm`: `12dp` (Khoảng cách giữa các chip lọc, danh sách nhỏ)
- `spacing_md`: `16dp` (Padding trong thẻ Card, khoảng cách giữa các ô nhập liệu)
- `spacing_lg`: `20dp` (Lề màn hình chuẩn 2 bên — Horizontal Screen Margin)
- `spacing_xl`: `32dp` (Khoảng cách nhịp giữa các Section nội dung lớn)

### 3.3. Đổ bóng & Nổi (Elevation & Shadow)
- **Phẳng (Flat):** Stroke viền `1dp` màu `#E4E9E7`, elevation `0dp` (cho phần lớn thẻ danh sách, input).
- **Hành động (Action CTA):** Elevation `4dp` – `6dp`, ambient tint theo màu `@color/brand_primary`.
- **Nổi (Raised / Floating):** Elevation `8dp` – `12dp` (Bottom sheet, floating action, modal dialog).

---

## 4. Nút & Ô Nhập (Buttons & Inputs)

### 4.1. Nút Bấm (`Btn`)
- **Chiều cao chuẩn:**
  - Lớn (CTA màn hình): `52dp` (Bo góc `16dp`, font `15sp` bold)
  - Vừa (Hành động phụ / Dialog): `44dp` (Bo góc `16dp`, font `14sp` bold)
  - Nhỏ: `36dp` (Bo góc `12dp`, font `13sp` bold)
- **Biến thể (Variants):**
  - `Primary`: Nền `#238C82`, chữ trắng, shadow teal nhạt.
  - `Secondary`: Nền `#EAF7F4`, chữ `#16544C`.
  - `Outline`: Nền trắng, viền `#E4E9E7`, chữ `#1B2226`.
  - `Ghost`: Không nền, không viền, chữ `#16544C`.
  - `Danger`: Nền `#C43B3B`, chữ trắng (Hủy lịch, Đăng xuất).
  - `Disabled`: Nền `#F2F1EE`, chữ `#68757B`.
- **Mẫu đặc thù:** Nút chính `full-width` dính sát bottom kèm dòng text chú thích nhỏ phía dưới (`11.5sp`, căn giữa).

### 4.2. Ô Nhập Liệu (`Field`)
- **Chiều cao chuẩn:** `52dp`, bo góc `16dp`, nền trắng (`#FFFFFF`).
- **Trạng thái:**
  - *Mặc định:* Viền `1dp` màu `#E4E9E7`.
  - *Focus:* Viền `2dp` màu `#238C82` (hoặc stroke 1dp + ring).
  - *Error:* Viền đỏ `#C43B3B`, text thông báo lỗi `12sp` màu `#9A2B2B` kèm icon `AlertCircle` bên dưới.
  - *Disabled:* Nền xám `#F2F1EE`, viền xám mờ.
- **Biến thể:**
  - `PasswordField`: Kèm nút toggle text "Ẩn / Hiện" (hoặc icon mắt).
  - `SearchField`: Icon kính lúp bên trái, icon filter bên phải.

### 4.3. Bộ chọn phân đoạn & Chip
- `Chip`: Chiều cao `36dp`, bo tròn `999dp`, active (nền đen/teal, chữ trắng) vs inactive (nền trắng, viền mờ).
- `SegmentedControl`: Thanh chia đều các tab con (ví dụ: Chó / Mèo / Khác), chiều cao `44dp`, bo góc `16dp`.

---

## 5. Chọn Ngày & Giờ (Date & Time Picker)

### 5.1. Bộ chọn Ngày (Horizontal Calendar Strip)
- Danh sách cuộn ngang (`RecyclerView` horizontal).
- Kích thước mỗi item: Rộng `54dp`, Cao `72dp`, bo góc `16dp`.
- 3 dòng nội dung:
  1. Thứ (`T2`, `T3`, `CN`) — `10.5sp` uppercase
  2. Ngày trong tháng (`27`, `28`) — `17sp` extra bold
  3. Tình trạng (`6 trống`, `Hết chỗ`) — `9.5sp` bold
- Trạng thái:
  - *Đang chọn:* Nền `#238C82`, chữ trắng.
  - *Còn chỗ:* Nền trắng, viền `#E4E9E7`.
  - *Hết chỗ / Ngày đã qua:* Opacity `45%`, không cho click.

### 5.2. Khung Giờ (`SlotButton`)
- Bố cục dạng Grid 3 đến 4 cột (`GridLayoutManager`).
- Chiều cao mỗi nút: `44dp`, bo góc `12dp`.
- 3 trạng thái:
  - `Available` (Còn chỗ): Nền trắng, viền `#E4E9E7`, chữ `#1B2226`.
  - `Selected` (Đang chọn): Nền `#238C82`, chữ trắng, bóng mờ teal.
  - `Unavailable` (Hết chỗ/Đã đặt): Nền `#F2F1EE`, chữ xám gạch ngang (`STRIKE_THRU`).

---

## 6. Thẻ, Huy Hiệu & Điều Hướng (Cards & Navigation)

### 6.1. Các Loại Thẻ (Cards)
- `ClinicCard` (Rail ngang): Rộng `244dp`, ảnh bìa cao `118dp`, bo góc `24dp`, tag mở cửa nổi trên ảnh, rating + số đánh giá + số km.
- `ClinicRow` (Danh sách dọc): Full-width, ảnh đại diện `76x76dp` bo góc `16dp`, tên phòng khám, địa chỉ, chevron điều hướng bên phải.
- `ServiceRow`: Dòng dịch vụ có radio/checkbox tròn xanh teal, tên dịch vụ, mô tả ngắn, meta thời lượng + giá tiền.
- `PetCard`: Thẻ chọn thú cưng khi đặt lịch, avatar pet bo góc `16dp`, tên, giống loài, giới tính, cân nặng.
- `ApptCard`: Thẻ lịch hẹn, có mã hẹn góc trái, huy hiệu trạng thái góc phải, vạch màu cam `4dp` ở cạnh trái nếu cần ưu tiên xử lý.

### 6.2. Huy Hiệu Trạng Thái (`Badge`)
- Chiều cao `26dp`, bo tròn `999dp`, padding ngang `10dp`.
- Gồm: **Chấm tròn `6dp`** màu chủ đạo + **Nhãn tiếng Việt** in đậm `11sp`.
  - `PENDING`: "Chờ xác nhận"
  - `CONFIRMED`: "Đã xác nhận"
  - `COMPLETED`: "Đã hoàn thành"
  - `REJECTED`: "Đã từ chối"
  - `CANCELLED`: "Đã hủy"
  - `NO_SHOW`: "Không đến"
  - `OPEN` / `BOOKED` / `CLOSED`: "Còn chỗ" / "Đã đặt" / "Đã đóng"

### 6.3. Điều Hướng Android (Device Chrome)
- **App Bar:** Chiều cao `56dp`.
  - Nút Back: Vuông `40x40dp`, bo góc `16dp`, viền mờ `#E4E9E7`, nền trắng.
  - Tiêu đề chính `17sp` bold, phụ đề `12.5sp` mờ.
- **Bottom Navigation:**
  - Chiều cao ~`64dp` (+ padding navigation bar).
  - Khách hàng (4 tab): *Trang chủ*, *Lịch hẹn*, *Thú cưng*, *Tài khoản*.
  - Nhân viên (3 tab): *Hôm nay*, *Lịch hẹn*, *Khung giờ*.
  - Tab active: Có pill nền màu `#EAF7F4` bọc quanh icon, text đổi màu `#238C82`.

---

## 7. Phản Hồi, Hộp Thoại & Trạng Thái Rỗng

### 7.1. Thông Báo Nội Tuyến (`Notice`)
- Card banner bo góc `16dp`, padding `14dp`.
- 4 loại tone: Warning (vàng nhạt), Success (xanh lá nhạt), Danger (đỏ nhạt), Brand (teal nhạt).

### 7.2. Hộp Thoại Quyết Định (`Dialog`)
- **Center Dialog:** Bo góc `24dp`, căn giữa màn hình, nền overlay đen mờ `45%`. Icon tròn lớn `48dp`, tiêu đề `17sp`, 2 nút (Nút chính xác nhận full-width + Nút phụ huỷ).
- **Bottom Sheet Dialog:** Bo 2 góc trên `28dp`, có thanh kéo handle bar xám (`width 48dp, height 6dp`).

### 7.3. `Snackbar`
- Nổi cách cạnh đáy `16dp` – `24dp`, bo góc `16dp`, nền đen hoặc màu status. Có icon trái và nút action "Hoàn tác" in hoa bên phải. Tự tắt sau 4 giây.

### 7.4. Trạng Thái Màn Hình
- **Loading State:** Shimmer skeleton (giả lập thẻ với các khối xám nhạt bo tròn nhấp nháy).
- **Empty State:** Vòng tròn nét đứt `80dp` nền `#EAF7F4` chứa icon to `32dp`, tiêu đề `16.5sp`, nội dung phụ max `260dp`, nút CTA chính bên dưới.
- **Error State:** Icon tam giác đỏ nền đỏ nhạt, tiêu đề lỗi, nút bấm "Thử lại" viền outline.

---

## 8. Ma Trận Độ Phủ Trạng Thái (17 Màn Hình)

| Mã màn hình | Tên màn hình | Các trạng thái bắt buộc trên Android XML & ViewModel |
|---|---|---|
| **C01** | Đăng nhập | Mặc định · Đang đăng nhập (loading button) · Lỗi sai thông tin · Chuyển đăng ký |
| **C02** | Tạo tài khoản | Form trống · Đang kiểm tra · Lỗi validate từng trường (mật khẩu/email) |
| **C03** | Trang chủ khách | Danh sách phòng khám gần nhất · Banner chào · Đang tải dữ liệu |
| **C04** | Danh sách phòng khám | Kết quả tìm kiếm · Đang lọc · Empty (Không tìm thấy) · Lỗi mạng (kèm nút thử lại) |
| **C05** | Chi tiết phòng khám | Header ảnh bìa lớn · Tab thông tin / dịch vụ · Nút đặt lịch cố định đáy |
| **C06** | Chọn dịch vụ | Danh sách dịch vụ · Đã chọn 1 dịch vụ · Nút "Tiếp tục" bị khóa nếu chưa chọn |
| **C07** | Chọn ngày & giờ | Scroll ngày · Grid giờ (Available/Selected/Unavailable) · Nút tiếp tục bị khóa |
| **C08** | Hồ sơ thú cưng | Danh sách bé · Bé đang chọn · Empty state (Khách mới chưa có bé nào) |
| **C09** | Thêm thú cưng | Form nhập thông tin bé (tên, loài, tuổi, cân nặng) · Lỗi nhập liệu |
| **C10** | Xác nhận đặt lịch | Tóm tắt lịch hẹn · Nút gửi · Đang gửi · Báo lỗi nếu khung giờ vừa bị người khác chiếm |
| **C11** | Đặt lịch thành công | Icon checkmark thành công · Trạng thái "Chờ xác nhận" · Nút về trang chủ |
| **C12** | Lịch hẹn của tôi | Tab "Sắp tới" & "Lịch sử" · Danh sách hẹn · Empty state khi chưa có lịch |
| **C13** | Chi tiết lịch hẹn | Chi tiết status · Dialog xác nhận huỷ lịch · Trạng thái đã huỷ |
| **C14** | Thông báo | Danh sách thông báo chưa đọc/đã đọc · Empty state khi đã đọc hết |
| **C15** | Tài khoản | Thông tin cá nhân · Chuyển chế độ · Dialog đăng xuất |
| **S01** | Bảng làm việc NV | Thống kê hôm nay · Danh sách hẹn trong ngày · Empty state hôm nay không có lịch |
| **S02** | Danh sách lịch hẹn NV | Lọc theo trạng thái · Đang tải · Không có kết quả phù hợp |
| **S03** | Chi tiết lịch hẹn NV | Nút "Xác nhận lịch" · Bottom sheet "Từ chối" kèm chọn lý do · Cập nhật kết quả khám |
| **S04** | Quản lý khung giờ NV | Lịch làm việc theo ngày · Sheet tạo thêm slot · Đổi trạng thái đóng/mở slot |

---

## 9. Chuyển Động & Hiệu Ứng (Motion Specs)
*Dùng cho Animation XML (`res/anim/` hoặc Transition Navigation)*

- **Mở màn hình tới (Forward):** Trượt từ phải sang trái (`slide_in_right`), thời lượng **280ms**, interpolator `decelerate / ease-out`.
- **Quay lại (Pop / Back):** Trượt ra sang phải (`slide_out_right`), thời lượng **240ms**.
- **Dialog & Bottom Sheet:**
  - Dialog: Fade in `120ms` kết hợp scale `0.98 -> 1.0`.
  - Bottom Sheet: Trượt từ đáy màn hình lên (`slide_in_bottom`) `250ms`.
- **Đổi trạng thái Huy hiệu / Button:** Cross-fade mờ dần `180ms`, không giật layout.
- **Snackbar xuất hiện:** Nổi lên `16dp` + Fade in `200ms`, tự ẩn sau `4000ms`.
- **Card được chọn:** Đổi màu viền và hiện tick xanh mờ dần `150ms`.

---

## 10. Sơ Đồ Điều Hướng Màn Hình (Navigation Graph & Screen Routing)
*Quy chuẩn cấu trúc điều hướng cho Android `res/navigation/mobile_navigation.xml` hoặc `FragmentManager`*

### 10.1. Cấu Trúc Khách Hàng (Customer Hub & Spoke)

#### A. 4 Màn Hình Gốc (Bottom Navigation Destinations):
1. **`C03` Trang chủ (Home):** Màn hình khởi đầu sau khi đăng nhập.
2. **`C12` Lịch hẹn của tôi (Appointments):** Quản lý lịch sắp tới & lịch sử.
3. **`C08` Thú cưng của tôi (My Pets):** Danh sách hồ sơ các bé.
4. **`C15` Tài khoản (Profile):** Thông tin cá nhân & cài đặt.

#### B. Các Luồng Rẽ Nhánh Thực Tế Từ `C03` (Trang Chủ):
- **C03 ➔ C14 (Thông báo):** Bấm Icon Chuông ở Header.
- **C03 ➔ C04 (Danh sách phòng khám):**
  - Bấm thanh tìm kiếm hoặc icon bộ lọc.
  - Bấm banner CTA "Bạn muốn đưa bé đi khám vào ngày nào?".
  - Bấm "Xem tất cả" tại nhóm Phòng khám gần bạn.
- **C03 ➔ C05 (Chi tiết phòng khám):**
  - Bấm trực tiếp vào 1 thẻ `ClinicCard` trên danh sách cuộn ngang (truyền `clinic_id`).
- **C03 ➔ C06 (Chọn dịch vụ):**
  - Bấm "Tất cả dịch vụ" hoặc bấm trực tiếp vào 1 dòng dịch vụ gợi ý (truyền `service_id`).

#### C. Luồng Đặt Lịch Hoàn Chỉnh (Core Booking Journey):
```
[C03 Trang chủ / C04 DS phòng khám]
          │ (chọn phòng khám - truyền clinic_id)
          ▼
   [C05 Chi tiết phòng khám]
          │ (bấm "Đặt lịch khám")
          ▼
   [C06 Chọn dịch vụ]
          │ (chọn dịch vụ - truyền service_id)
          ▼
   [C07 Chọn ngày & giờ]
          │ (chọn ngày & slot_id)
          ▼
   [C08 Chọn thú cưng đi khám]
          ├──► (Nếu chưa có bé nào) ──► [C09 Thêm thú cưng mới] ──► quay lại [C08]
          │ (chọn pet_id)
          ▼
   [C10 Xác nhận thông tin]
          │ (bấm "Gửi yêu cầu đặt lịch")
          ▼
   [C11 Đặt lịch thành công]
          ├──► Bấm "Xem lịch hẹn" ──► [C12 Lịch hẹn của tôi] ──► [C13 Chi tiết hẹn]
          └──► Bấm "Về trang chủ" ──► [C03 Trang chủ]
```

#### D. Luồng Hủy Lịch Hẹn (Cancellation Flow):
```
[C12 Lịch hẹn của tôi]
          │ (chọn 1 lịch hẹn - truyền appt_id)
          ▼
   [C13 Chi tiết lịch hẹn]
          │ (bấm nút "Hủy lịch hẹn")
          ▼
[Dialog / Sheet xác nhận]
          │ (xác nhận hủy)
          ▼
[C13 Cập nhật trạng thái "Đã hủy" + Snackbar thông báo hoàn tác]
```

---

### 10.2. Cấu Trúc Phía Nhân Viên (Staff Navigation)

#### A. 3 Màn Hình Gốc (Staff Bottom Navigation):
1. **`S01` Bảng làm việc (Dashboard):** Tổng quan lịch hôm nay & ca trực.
2. **`S02` Danh sách lịch hẹn (Appointment List):** Bộ lọc theo các trạng thái.
3. **`S04` Quản lý khung giờ (Slot Management):** Cấu hình mở/đóng slot làm việc.

#### B. Luồng Duyệt / Từ Chối Lịch Hẹn:
```
[S01 Bảng làm việc / S02 Danh sách lịch]
          │ (chọn lịch chờ duyệt - truyền appt_id)
          ▼
   [S03 Chi tiết yêu cầu]
          ├──► Bấm "Xác nhận" ──► [Dialog xác nhận] ──► Cập nhật trạng thái CONFIRMED
          ├──► Bấm "Từ chối"  ──► [Sheet lý do từ chối] ──► Cập nhật trạng thái REJECTED
          └──► Khám xong       ──► Bấm "Hoàn thành" ──► Cập nhật trạng thái COMPLETED
```

#### C. Luồng Cấu Hình Khung Giờ Làm Việc:
```
[S01 Bảng làm việc] ──► [S04 Quản lý khung giờ]
                               │
                               ├──► Chọn ngày trên thanh lịch
                               ├──► Click nút "Tạo khung giờ mới" ──► [Sheet tạo slot] ──► Thêm slot mới
                               └──► Click vào slot hiện có ──► Toggle Đóng/Mở slot
```

