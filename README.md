# PET CARE

Ứng dụng Android tìm kiếm phòng khám thú y và đặt lịch khám cho thú cưng. Repository được chuẩn bị cho nhóm 5 thành viên phát triển trong 5 tuần.

## Trạng thái hiện tại

Repository mới có cấu trúc thư mục, kế hoạch và SQL thiết kế. Chưa khởi tạo project backend hoặc Android. Kế hoạch và phân công chi tiết nằm trong [plan.md](plan.md).

## Công nghệ dự kiến

- Backend: Java 21, Spring Boot, Maven, REST API.
- Frontend: Android Java, XML Views, kiến trúc MVVM.
- Cơ sở dữ liệu: PostgreSQL.
- Quản lý mã nguồn và phối hợp: Git, GitHub và Pull Request.

## Cấu trúc repository

```text
PETCARE/
├── plan.md
├── README.md
├── .gitignore
├── be/
│   └── .gitkeep
├── fe/
│   └── .gitkeep
└── database/
    └── PETCARE_PostgreSQL_5weeks.sql
```

`PETCARE/` biểu thị thư mục gốc repository, hiện là `D:\CODE\PetCare`; không cần tạo thêm thư mục PETCARE lồng bên trong.

- `plan.md`: kế hoạch và phân công công việc của 5 thành viên.
- `be/`: dành cho backend Spring Boot trong giai đoạn triển khai.
- `fe/`: dành cho ứng dụng Android Java và XML Views.
- `database/`: lưu SQL PostgreSQL đã chốt; không tự sửa schema khi chưa thống nhất nhóm.

## Tham gia phát triển

1. Đọc `plan.md` và thống nhất hợp đồng API, schema, phân quyền trước khi lập trình module.
2. Sau khi nhóm thiết lập remote GitHub, clone repository và chuyển vào thư mục dự án.
3. Cập nhật nhánh `main` trước khi tạo nhánh công việc:

   ```sh
   git switch main
   git pull --ff-only origin main
   git switch -c feature/ten-chuc-nang
   ```

4. Làm việc trên nhánh riêng, kiểm tra `git status` và `git diff` trước khi stage/commit. Chỉ stage các file đã rà soát.
5. Push nhánh riêng và tạo Pull Request để thành viên khác review trước khi merge.
6. Thống nhất thay đổi ảnh hưởng module khác qua Pull Request; giữ tài liệu và dữ liệu thiết kế đồng bộ với quyết định của nhóm.

Chưa có lệnh chạy backend hoặc Android vì chưa có project tương ứng. Không chạy SQL vào database dùng chung nếu chưa thống nhất môi trường và người phụ trách.

## Cấu hình và thông tin nhạy cảm

- Không commit mật khẩu, JWT secret, token, private key, file `.env` thật hoặc cấu hình máy cá nhân.
- Khi triển khai, dùng biến môi trường hoặc cấu hình local đã được ignore cho thông tin nhạy cảm.
- Có thể chia sẻ `.env.example` chỉ chứa tên biến và giá trị minh họa không nhạy cảm.
- Giữ các cấu hình nhóm cần chia sẻ, như cấu hình ứng dụng không chứa secret, Maven/Gradle Wrapper và cấu hình VS Code được thống nhất.
- `.gitignore` không thay thế việc rà soát nội dung trước mỗi commit.
