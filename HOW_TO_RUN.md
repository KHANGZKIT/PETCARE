# HƯỚNG DẪN CHẠY DỰ ÁN PET CARE

Tài liệu hướng dẫn khởi chạy toàn bộ hệ thống **PET CARE** gồm: **Database (PostgreSQL)**, **Backend (Spring Boot)** và **Frontend (Android)**.

---

## 1. Yêu cầu môi trường

- **Docker Desktop** (để chạy PostgreSQL & pgAdmin qua Docker Compose).
- **JDK 25** (cấu hình sẵn trong biến môi trường `JAVA_HOME`).
- **Android Studio** (có cài đặt **Android SDK Platform 35**).

---

## 2. Khởi chạy Cơ sở dữ liệu (PostgreSQL)

1. Tạo file `.env` tại thư mục gốc từ file mẫu `.env.example` (nếu chưa có):
   ```powershell
   Copy-Item .env.example .env
   ```
2. Khởi chạy container PostgreSQL bằng Docker Compose tại thư mục gốc của dự án:
   ```powershell
   docker compose up -d
   ```
   > Khi chạy lần đầu, Docker sẽ tự động tạo database `pet_care` và nạp cấu trúc bảng (`database/PETCARE_PostgreSQL_5weeks.sql`) cùng dữ liệu mẫu (`database/seed_data.sql`).

- **PostgreSQL**: chạy tại `localhost:5432`
- **pgAdmin 4**: chạy tại `http://localhost:5050`

---

## 3. Khởi chạy Backend (Spring Boot)

Di chuyển vào thư mục `be` và chạy ứng dụng bằng Maven Wrapper:

```powershell
cd be
.\mvnw.cmd spring-boot:run
```

> Nếu đặt mật khẩu hoặc thông tin kết nối khác với giá trị mặc định (`postgres` / `postgres`), hãy thiết lập các biến môi trường `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` trước khi chạy.

- **Backend API**: chạy tại `http://localhost:8080`
- **Kiểm tra nhanh API**: truy cập `http://localhost:8080/api/v1/clinics`

---

## 4. Khởi chạy Frontend (Android)

1. Mở **Android Studio** -> chọn **Open** -> chọn thư mục **`fe`** của dự án.
2. Đợi Android Studio đồng bộ **Gradle Sync** hoàn tất.
3. Chọn thiết bị máy ảo (Emulator) hoặc thiết bị thật:
   - **Trên Máy ảo (Emulator)**: Ứng dụng tự động kết nối tới Backend qua địa chỉ `http://10.0.2.2:8080/api/v1/`.
   - **Trên Điện thoại thật (cắm cáp USB)**: Chạy lệnh `adb reverse tcp:8080 tcp:8080` để chuyển tiếp cổng về `http://localhost:8080/api/v1/`.
4. Nhấn **Run `'app'`** (`Shift + F10`) để biên dịch và mở ứng dụng.

---

## 5. Tài khoản đăng nhập mẫu

Tất cả tài khoản trong dữ liệu mẫu (`database/seed_data.sql`) đều sử dụng mật khẩu chung: **`Password123`**

| Vai trò | Email | Mật khẩu | Ghi chú |
| :--- | :--- | :--- | :--- |
| **Khách hàng (Customer)** | `customer@petcare.com` | `Password123` | Có sẵn thú cưng & lịch hẹn mẫu (`customer2` -> `customer50@petcare.com`) |
| **Nhân viên (Staff)** | `staff1@petcare.com` | `Password123` | Quản lý cơ sở phòng khám số 1 (`staff1` -> `staff10@petcare.com`) |
