# PET CARE

Ứng dụng Android tìm kiếm phòng khám thú y và đặt lịch khám cho thú cưng, kèm phân hệ quản lý lịch hẹn và khung giờ dành cho nhân viên phòng khám.

## Công nghệ sử dụng

- **Backend (`be/`):** Java 25, Spring Boot 4.1.1, Spring Data JPA, Spring Security + JWT (`jjwt`), Bean Validation, Maven Wrapper, JUnit 5 + Mockito + Testcontainers (PostgreSQL 17).
- **Frontend (`fe/`):** Android (Java 11, XML Views, ViewBinding, kiến trúc MVVM với `Repository` / `ViewModel` / `LiveData`, `RecyclerView`, Retrofit 2 + OkHttp).
- **Cơ sở dữ liệu (`database/`):** PostgreSQL 17 với schema 6 bảng (`clinics`, `users`, `pets`, `services`, `clinic_slots`, `appointments`), ràng buộc composite FK, partial unique index chống đặt trùng slot và bộ seed data ngày động.

## Cấu trúc repository

```text
PETCARE/
├── be/                         # Backend Spring Boot REST API + Unit/Integration tests
├── fe/                         # Ứng dụng Android (Customer & Staff)
├── database/
│   ├── PETCARE_PostgreSQL_5weeks.sql   # Schema PostgreSQL chuẩn
│   └── seed_data.sql                   # Dữ liệu mẫu (10 phòng khám, 10 staff, 50 khách hàng)
├── docker-compose.yml          # PostgreSQL 17 & pgAdmin 4 cho môi trường local
├── HOW_TO_RUN.md               # Hướng dẫn khởi chạy hệ thống chi tiết
├── DESIGN_SYSTEM_SPEC.md       # Quy chuẩn giao diện Android
├── plan.md                     # Kế hoạch & phân công nhóm 5 tuần
└── .env.example                # Mẫu biến môi trường local
```

## Chức năng chính

- **Khách hàng (`CUSTOMER`):**
  - Đăng ký, đăng nhập, quản lý hồ sơ tài khoản và cài đặt thông báo.
  - Tìm kiếm, lọc, sắp xếp phòng khám thú y (có tính khoảng cách GPS) và xem chi tiết dịch vụ.
  - Quản lý danh sách thú cưng (thêm, sửa, xóa an toàn, xem lịch sử khám).
  - Đặt lịch khám 4 bước (chọn dịch vụ, chọn ngày/giờ trống theo thời gian thực, chọn thú cưng, xác nhận & chỉnh sửa trực tiếp từng mục, kèm ghi chú).
  - Theo dõi trạng thái lịch hẹn (`PENDING`, `CONFIRMED`, `COMPLETED`, `NO_SHOW`, `CANCELLED`, `REJECTED`), hủy lịch và xem thông báo.
- **Nhân viên phòng khám (`STAFF`):**
  - Bảng tổng quan ca trực trong ngày và danh sách yêu cầu chờ duyệt.
  - Tìm kiếm, lọc và xử lý vòng đời lịch hẹn thuộc cơ sở mình (`confirm`, `reject`, `complete`, `no-show`).
  - Quản lý khung giờ khám theo ngày: tạo khung giờ mới, đóng/mở khung giờ trống trực tiếp qua API.

## Khởi chạy & Kiểm thử nhanh

Xem hướng dẫn chi tiết và tài khoản mẫu tại [HOW_TO_RUN.md](HOW_TO_RUN.md).

```powershell
# 1. Khởi chạy PostgreSQL + pgAdmin (sau khi tạo .env từ .env.example)
docker compose up -d

# 2. Chạy kiểm thử & khởi động Backend (yêu cầu JDK 25)
cd be
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run

# 3. Biên dịch & chạy unit test Android
cd ..\fe
.\gradlew.bat testDebugUnitTest assembleDebug
```

## Quy tắc bảo mật & cộng tác

- Không commit mật khẩu, JWT secret, token, private key, file `.env` thật hoặc file backup (`.bak`).
- Sử dụng `.env.example` để chia sẻ tên biến môi trường mẫu và cấu hình biến thật tại máy local/CI.
- Mọi thay đổi schema database phải được thống nhất nhóm và cập nhật đồng bộ giữa `database/PETCARE_PostgreSQL_5weeks.sql`, Entity và test.
