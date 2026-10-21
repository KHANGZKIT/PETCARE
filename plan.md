# PET CARE — Kế hoạch thực hiện trong 5 tuần

**Đề tài:** Xây dựng ứng dụng Android đặt lịch khám cho thú cưng PET CARE  
**Thời gian dự kiến:** 12/10/2026 – 15/11/2026  
**Nhóm:** 08 — 5 thành viên  
**Công nghệ:** Android Java + XML Views + MVVM, Spring Boot REST API, Spring Data JPA, Spring Security/JWT, PostgreSQL, Git  
**Phạm vi:** Ứng dụng Android cho khách hàng và nhân viên phòng khám; không xây dựng website quản trị riêng.

## 1. Nguyên tắc phân chia công việc

Ba thành viên **Vũ Tiến Khang, Nguyễn Huy Hoàng và Nguyễn Bá Mạnh đều tham gia lập trình nghiệp vụ**, không chia theo kiểu một người làm toàn bộ backend còn một người chỉ làm giao diện. Mỗi người sở hữu một nhóm chức năng cụ thể, gồm backend/service và phần Android liên quan khi phù hợp. Hai thành viên còn lại phụ trách database, phân tích nghiệp vụ, kiểm thử và báo cáo; họ phối hợp trực tiếp với lập trình viên để xác minh chức năng.

### Phân chia module code

| Thành viên | Module chịu trách nhiệm | Công việc code cụ thể |
|---|---|---|
| **Vũ Tiến Khang** | Phòng khám, dịch vụ, khung giờ và nền tảng tích hợp | Khởi tạo cấu trúc backend; Entity/Repository/Service/Controller cho `clinics`, `services`, `clinic_slots`; API danh sách/chi tiết phòng khám, dịch vụ, xem slot trống và quản lý slot ở mức MVP; xử lý phần cốt lõi tạo lịch trong giao dịch và chống đặt trùng; Android XML/Java cho danh sách, chi tiết phòng khám, dịch vụ và chọn slot; tích hợp/review toàn hệ thống. |
| **Nguyễn Huy Hoàng** | Xác thực, phân quyền, tài khoản và thú cưng | API đăng ký/đăng nhập, BCrypt, JWT, Spring Security, quyền `CUSTOMER`/`STAFF`; API xem/thêm thú cưng của khách đang đăng nhập; Android XML/Java cho đăng nhập/đăng ký và danh sách/thêm thú cưng; kiểm thử bảo mật endpoint thuộc module. |
| **Nguyễn Bá Mạnh** | Vòng đời lịch hẹn và giao diện luồng đặt lịch | API xem lịch của khách, hủy lịch theo chính sách, nhân viên xác nhận/từ chối và cập nhật kết quả lịch; phần Repository/ViewModel và XML/Java cho xác nhận đặt lịch, lịch của tôi, chi tiết/trạng thái lịch và màn hình nhân viên xử lý lịch; xử lý loading/lỗi/mất mạng và tích hợp API liên quan. Phối hợp Khang ở API tạo lịch và quy tắc chiếm slot. |
| **Vũ Thành Nam** | Database và tài liệu kỹ thuật | Hoàn thiện ERD 6 bảng; chuẩn bị/chạy thử SQL PostgreSQL và dữ liệu mẫu; kiểm tra khóa chính, khóa ngoại, index, ràng buộc; cập nhật sơ đồ và mô tả thuộc tính theo schema thật; hỗ trợ nhóm kiểm tra dữ liệu khi tích hợp. Không nhận module code nghiệp vụ lớn. |
| **Lại Hải Nam** | Phân tích nghiệp vụ, kiểm thử và báo cáo | Viết Use Case/SRS, sơ đồ quy trình đặt/hủy/xử lý lịch, quy tắc trạng thái; chuẩn bị test case và collection Postman; kiểm thử API và luồng end-to-end, ghi nhận lỗi/minh chứng; tổng hợp báo cáo tuần, kết quả kiểm thử và hỗ trợ slide bảo vệ. Không nhận module code nghiệp vụ lớn. |

### Quy ước phối hợp giữa 3 người code

- **Khang** phụ trách phần tạo lịch và chiếm slot nguyên tử; **Mạnh** phụ trách các thao tác trên lịch sau khi tạo (xem, hủy, xác nhận/từ chối, cập nhật kết quả). Hai người phải thống nhất DTO, trạng thái và hợp đồng API trước khi code.
- **Hoàng** phụ trách xác thực/phân quyền; các API do Khang và Mạnh viết phải dùng chung cơ chế bảo vệ endpoint và thông tin người dùng hiện tại.
- Cả ba thống nhất JSON request/response, HTTP status và cách trả lỗi trước khi Android tích hợp; không tự đổi tên endpoint/field mà không báo nhóm.
- Giao diện Android sử dụng **Java + XML Views**. Activity/Fragment hiển thị màn hình, ViewModel quản lý trạng thái, Repository gọi REST API; không sử dụng Jetpack Compose trong phạm vi này.

## 2. Kế hoạch theo tuần

### Tuần 1 — Khởi động và chốt phạm vi
**Thời gian:** 12/10–18/10/2026  
**Mục tiêu:** Thống nhất bài toán, chức năng tối thiểu và cách cộng tác.

- **Vũ Tiến Khang:** tổ chức họp; chốt MVP; tạo repository Git, quy tắc branch/commit và cấu trúc dự kiến backend.
- **Nguyễn Huy Hoàng:** liệt kê yêu cầu đăng ký/đăng nhập, vai trò `CUSTOMER`/`STAFF` và các trường hợp lỗi xác thực.
- **Nguyễn Bá Mạnh:** phác thảo luồng màn hình Android: đăng nhập, phòng khám, chọn slot, đặt lịch, lịch của tôi và xử lý lịch phía nhân viên.
- **Vũ Thành Nam:** liệt kê thực thể dữ liệu và phác thảo 6 bảng, quan hệ ban đầu.
- **Lại Hải Nam:** mô tả bài toán, tác nhân, luồng đặt/hủy/xử lý lịch; bắt đầu báo cáo tuần 1.

**Đầu ra:** Mô tả bài toán, danh sách chức năng MVP, vai trò người dùng, Git repository và bản phân công nhiệm vụ.

### Tuần 2 — Phân tích yêu cầu và thiết kế
**Thời gian:** 19/10–25/10/2026  
**Mục tiêu:** Chốt thiết kế đủ rõ để ba lập trình viên triển khai các module song song.

- **Vũ Tiến Khang:** xác định kiến trúc Android MVVM → Spring Boot REST API → PostgreSQL; thiết kế API phòng khám/dịch vụ/slot và API tạo lịch; thống nhất cách xử lý giao dịch chống đặt trùng.
- **Nguyễn Huy Hoàng:** thiết kế luồng Auth/JWT, phân quyền; xác định API tài khoản và thú cưng, request/response và các lỗi cần xử lý.
- **Nguyễn Bá Mạnh:** hoàn thiện wireframe lịch hẹn khách hàng và màn hình nhân viên; xác định API xem/hủy/xử lý lịch; tạo project Android Java + XML Views và khung MVVM/điều hướng nếu các hợp đồng đã thống nhất.
- **Vũ Thành Nam:** hoàn thiện ERD 6 bảng `clinics`, `users`, `pets`, `services`, `clinic_slots`, `appointments`; chạy thử SQL PostgreSQL trên database trống.
- **Lại Hải Nam:** viết Use Case/SRS và Activity Diagram cho đặt, hủy, xác nhận/từ chối lịch; lập quy tắc nghiệp vụ và test case dự kiến.

**Đầu ra:** Use Case, kiến trúc tổng thể, ERD và SQL bản đầu, quy tắc nghiệp vụ, danh sách API sơ bộ, wireframe và báo cáo tuần 2.

### Tuần 3 — Code nền tảng và chức năng đầu tiên
**Thời gian:** 26/10–01/11/2026  
**Mục tiêu:** Backend chạy được, xác thực hoạt động và các module đầu tiên có thể kiểm thử.

- **Vũ Tiến Khang:** khởi tạo cấu trúc `controller/service/repository/entity/dto/config/exception`; code API xem phòng khám, dịch vụ và slot; bắt đầu API tạo lịch với kiểm tra slot và giao dịch.
- **Nguyễn Huy Hoàng:** code đăng ký/đăng nhập, BCrypt, JWT, Spring Security, phân quyền `CUSTOMER`/`STAFF`; code API xem/thêm thú cưng của khách đang đăng nhập.
- **Nguyễn Bá Mạnh:** code màn hình đăng nhập/đăng ký và các màn hình lịch hẹn bằng XML/Java theo MVVM; dựng luồng điều hướng; chuẩn bị Repository/ViewModel gọi API hoặc dùng dữ liệu giả cho endpoint chưa hoàn thành.
- **Vũ Thành Nam:** kiểm tra SQL, khóa ngoại và constraint; tạo dữ liệu phòng khám, nhân viên, dịch vụ và slot mẫu; cập nhật ERD theo schema thực tế.
- **Lại Hải Nam:** tạo collection Postman cho Auth và API dữ liệu nền; kiểm thử các endpoint đã có, ghi request/response và minh chứng.

**Đầu ra:** Backend khởi động được, PostgreSQL kết nối thành công, Auth cơ bản hoạt động, API phòng khám/dịch vụ/slot có thể kiểm thử, ứng dụng Android mở và điều hướng được.

### Tuần 4 — Hoàn thiện nghiệp vụ và tích hợp
**Thời gian:** 02/11–08/11/2026  
**Mục tiêu:** Chạy xuyên suốt luồng khách đặt lịch và nhân viên xử lý.

- **Vũ Tiến Khang:** hoàn thiện API tạo lịch; kiểm tra phòng khám/dịch vụ/slot hợp lệ; xử lý chiếm slot trong giao dịch, chống đặt trùng và giải phóng slot khi hủy/từ chối theo hợp đồng với Mạnh; hoàn thiện màn hình phòng khám/dịch vụ/chọn slot.
- **Nguyễn Huy Hoàng:** hoàn thiện bảo vệ endpoint, xác thực token và phân quyền; hoàn thiện API thú cưng cùng màn hình chọn/thêm thú cưng; kiểm tra truy cập sai vai trò.
- **Nguyễn Bá Mạnh:** hoàn thiện API xem lịch của khách, hủy lịch, xác nhận/từ chối và cập nhật kết quả; tích hợp các API lịch hẹn vào Android; hoàn thiện màn hình xác nhận đặt, lịch của tôi và nhân viên xử lý lịch; xử lý trạng thái tải/lỗi/hết chỗ.
- **Vũ Thành Nam:** đối chiếu database với code; kiểm tra dữ liệu mẫu và khóa ngoại; cập nhật mô tả bảng, ERD và hướng dẫn tạo database.
- **Lại Hải Nam:** kiểm thử end-to-end bằng tài khoản khách và nhân viên; kiểm tra chuyển trạng thái, hủy lịch và đặt trùng; ghi nhận lỗi và theo dõi sửa lỗi.

**Đầu ra:** Demo luồng đăng nhập → chọn phòng khám/thú cưng/dịch vụ/slot → đặt lịch → nhân viên xác nhận/từ chối → khách xem trạng thái.

### Tuần 5 — Kiểm thử, sửa lỗi và hoàn thiện báo cáo/demo
**Thời gian:** 09/11–15/11/2026  
**Mục tiêu:** Ổn định chức năng MVP, hoàn thiện tài liệu và chuẩn bị bảo vệ.

- **Vũ Tiến Khang:** sửa lỗi nghiệp vụ phòng khám/slot/đặt lịch; kiểm tra cạnh tranh đặt slot và giao dịch; review tích hợp cuối; chuẩn bị hướng dẫn chạy backend và kịch bản demo.
- **Nguyễn Huy Hoàng:** kiểm thử Auth/JWT, mật khẩu sai, token không hợp lệ và truy cập sai vai trò; sửa lỗi xác thực/phân quyền; mô tả phần bảo mật trong báo cáo kỹ thuật.
- **Nguyễn Bá Mạnh:** sửa lỗi màn hình và điều hướng; kiểm thử luồng đặt/xem/hủy lịch và luồng nhân viên; hoàn thiện APK, ảnh màn hình và hướng dẫn chạy Android.
- **Vũ Thành Nam:** hoàn thiện chương database, ERD, mô tả bảng, file SQL và hướng dẫn khởi tạo/khôi phục dữ liệu.
- **Lại Hải Nam:** tổng hợp test case và kết quả kiểm thử; hoàn thiện Use Case/SRS, báo cáo tuần/cuối kỳ, danh sách hạn chế còn lại và slide bảo vệ.
- **Cả nhóm:** chạy thử demo đầu-cuối, kiểm tra code/tài liệu đã push lên Git và thống nhất nội dung trình bày.

**Đầu ra:** Luồng MVP ổn định, file SQL và dữ liệu mẫu, kết quả kiểm thử, hướng dẫn chạy, APK/demo và báo cáo cuối kỳ.

## 3. Phạm vi MVP để kịp tiến độ

### Bắt buộc
- Đăng ký/đăng nhập và phân quyền `CUSTOMER`/`STAFF`.
- Xem danh sách/chi tiết phòng khám và dịch vụ.
- Thêm/xem thú cưng của khách hàng.
- Xem slot trống, đặt lịch, xem lịch và hủy lịch theo chính sách đã thống nhất.
- Nhân viên xác nhận/từ chối lịch; cập nhật `COMPLETED`/`NO_SHOW` nếu kịp.
- Backend kiểm tra quyền sở hữu dữ liệu và ngăn hai khách cùng chiếm một slot.

### Để sau nếu còn thời gian
- GPS/bản đồ tìm phòng khám gần nhất.
- Thông báo tự động, đánh giá phòng khám, thanh toán trực tuyến.
- Lịch làm việc lặp tự động, hồ sơ bệnh án và website quản trị riêng.

## 4. Cấu trúc Android đề xuất

```text
app/src/main/
├── java/.../
│   ├── data/          # API client, repository, model
│   ├── ui/            # Activity, Fragment, Adapter
│   ├── viewmodel/     # ViewModel
│   └── utils/         # Validator, constants, helper
└── res/
    ├── layout/        # Các file XML layout
    ├── drawable/      # Hình nền, shape, icon tự tạo
    └── values/        # colors.xml, strings.xml, themes.xml
```

- **XML:** khai báo layout, input, button, danh sách và thành phần giao diện.
- **Java:** xử lý sự kiện, điều hướng, gọi API và cập nhật trạng thái hiển thị.
- **MVVM:** Activity/Fragment là View; ViewModel quản lý state; Repository gọi REST API.
- Sử dụng Git branch riêng theo module; không commit mật khẩu, JWT secret hoặc thông tin kết nối thật vào repository.

## 5. Tiêu chí nghiệm thu cuối kỳ

- Khách đặt được lịch hợp lệ và không xem/sửa dữ liệu của người khác.
- Nhân viên chỉ xử lý lịch thuộc phòng khám mình.
- Hệ thống ngăn hai khách cùng đặt một slot; lịch hẹn và trạng thái slot được cập nhật nhất quán.
- Các trạng thái lịch chỉ chuyển theo quy tắc đã thống nhất.
- Có test case/Postman, dữ liệu mẫu, hướng dẫn chạy, APK/demo và báo cáo.
