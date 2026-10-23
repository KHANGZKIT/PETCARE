# PET CARE — Backend

Backend API cho ứng dụng đặt lịch khám thú cưng PET CARE.

## Công nghệ

| Thành phần | Phiên bản |
|---|---|
| Java (JDK) | 25 LTS |
| Spring Boot | 4.1.1 |
| Spring Framework | 7 |
| Jakarta EE | 11 |
| Maven | 3.9.x (Maven Wrapper đi kèm) |
| PostgreSQL | 12+ |

## Yêu cầu

- **JDK 25** — tải từ [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) hoặc [Adoptium](https://adoptium.net/).
- **PostgreSQL** đang chạy với database `pet_care` đã được tạo.

## Cấu hình

### 1. Tạo database PostgreSQL

```sql
CREATE DATABASE pet_care;
```

Chạy file `database/PETCARE_PostgreSQL_5weeks.sql` (ở thư mục gốc repository) vào database `pet_care` để tạo schema:

```sh
psql -U postgres -d pet_care -f ../database/PETCARE_PostgreSQL_5weeks.sql
```

### 2. Thiết lập biến môi trường

Backend đọc cấu hình PostgreSQL từ biến môi trường. Tham khảo file `.env.example` để biết danh sách biến.

#### Windows PowerShell (phiên hiện tại)

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/pet_care"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your_password_here"
```

#### Windows PowerShell (user-level, lưu vĩnh viễn)

```powershell
[System.Environment]::SetEnvironmentVariable("DB_URL", "jdbc:postgresql://localhost:5432/pet_care", "User")
[System.Environment]::SetEnvironmentVariable("DB_USERNAME", "postgres", "User")
[System.Environment]::SetEnvironmentVariable("DB_PASSWORD", "your_password_here", "User")
```

Sau khi đặt biến vĩnh viễn, mở lại terminal để có hiệu lực.

#### IDE (IntelliJ IDEA / VS Code)

Thêm biến môi trường vào Run Configuration hoặc `launch.json`.

### 3. Đảm bảo JAVA_HOME trỏ sang JDK 25

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.5"
```

Kiểm tra:

```powershell
java -version
# Kết quả mong đợi: java version "25..."
```

## Chạy ứng dụng

```powershell
cd be
.\mvnw.cmd spring-boot:run
```

Hoặc build JAR rồi chạy:

```powershell
.\mvnw.cmd -DskipTests clean package
java -jar target\petcare-backend-0.0.1-SNAPSHOT.jar
```

Server mặc định khởi động tại `http://localhost:8080`.

### Chạy bằng Docker

Build Docker image:

```sh
# Từ thư mục be/
docker build -t petcare-backend .
```

Chạy container (kết nối database local của máy host):

```sh
docker run -d -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://host.docker.internal:5432/pet_care" \
  -e DB_USERNAME="postgres" \
  -e DB_PASSWORD="your_password_here" \
  -e JWT_SECRET="your_jwt_secret_here" \
  --name petcare-backend \
  petcare-backend
```


## Build và kiểm tra biên dịch

```powershell
.\mvnw.cmd -DskipTests clean package
```

Lệnh này biên dịch mã nguồn, chạy packaging và tạo file JAR mà không cần PostgreSQL đang chạy.

## Chạy test

```powershell
.\mvnw.cmd test
```

> **Lưu ý:** Hiện tại chưa có test khởi động ứng dụng (`@SpringBootTest`) vì cần PostgreSQL đang chạy. Test placeholder chỉ kiểm tra biên dịch.

## Cấu trúc package

```text
com.petcare
├── PetCareApplication.java   # Entry point
├── config/                    # Cấu hình CORS, beans, etc.
├── security/                  # Spring Security, JWT
├── controller/                # REST controllers
├── service/                   # Business logic
├── repository/                # Spring Data JPA repositories
├── entity/                    # JPA entities
├── dto/
│   ├── request/               # Request DTOs
│   └── response/              # Response DTOs
├── exception/                 # Custom exceptions, global handler
└── common/                    # Utilities, constants
```

## Hướng triển khai tiếp theo

- **Entity JPA:** Ánh xạ 6 bảng PostgreSQL (`clinics`, `users`, `pets`, `services`, `clinic_slots`, `appointments`).
- **Security:** Triển khai JWT filter, `UserDetailsService`, `SecurityFilterChain` với phân quyền `CUSTOMER`/`STAFF`.
- **Controller / Service:** Các API nghiệp vụ theo phân công trong `plan.md`.

## Lưu ý quan trọng

- `ddl-auto` đã đặt là `none` — Hibernate **không** tự tạo hoặc sửa bảng. Schema do file SQL quản lý.
- Không commit file `.env`, mật khẩu hoặc JWT secret vào Git.
- Sử dụng Maven Wrapper (`mvnw` / `mvnw.cmd`) thay vì cài Maven toàn cục.
