-- ============================================================
-- PET CARE - PostgreSQL Seed Data Script
-- Dữ liệu mẫu khởi tạo phục vụ kiểm thử, demo và phát triển (Tuần 3 - 5)
-- Mật khẩu mặc định cho tất cả tài khoản mẫu: 123456
-- BCrypt cost 10: $2a$10$Jky0mIwJGHnDiWsATQ1yjeK662dTo9buOoKITz1Tr6jmT3PcB8K42
-- ============================================================

BEGIN;

-- 1. Phòng khám (Clinics)
INSERT INTO public.clinics (id, name, address, area, phone, working_hours_note, is_active, latitude, longitude)
VALUES
    (1, 'Phòng khám Thú y An Nhiên', 'Số 12 Chùa Hà, Phường Quan Hoa', 'Cầu Giấy', '02438335566', '08:00 - 20:00 (Thứ 2 - Chủ Nhật)', true, 21.0360, 105.7950),
    (2, 'Bệnh viện Thú y PetCare HaUI', 'Số 298 Đường Cầu Diễn, Phường Minh Khai', 'Bắc Từ Liêm', '02437655121', '24/7 Cấp cứu & Khám chữa', true, 21.0537, 105.7351),
    (3, 'Bệnh viện Thú y PetCare Đống Đa', 'Số 88 Thái Hà, Phường Trung Liệt', 'Đống Đa', '02438561122', '08:00 - 18:00 (Thứ 2 - Thứ 7)', true, 21.0125, 105.8190)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    address = EXCLUDED.address,
    area = EXCLUDED.area,
    phone = EXCLUDED.phone,
    working_hours_note = EXCLUDED.working_hours_note,
    latitude = EXCLUDED.latitude,
    longitude = EXCLUDED.longitude;

-- 2. Người dùng (Users: Customers và Clinic Staff)
-- Mật khẩu đăng nhập: 123456
INSERT INTO public.users (id, full_name, email, phone, password_hash, role, clinic_id, is_active)
VALUES
    -- Nhân viên thuộc các phòng khám
    (1, 'BS. Trần Minh Tâm', 'staff1@petcare.com', '0911000001', '$2a$10$Jky0mIwJGHnDiWsATQ1yjeK662dTo9buOoKITz1Tr6jmT3PcB8K42', 'STAFF', 1, true),
    (2, 'BS. Nguyễn Phương Thảo', 'staff2@petcare.com', '0911000002', '$2a$10$Jky0mIwJGHnDiWsATQ1yjeK662dTo9buOoKITz1Tr6jmT3PcB8K42', 'STAFF', 2, true),
    (3, 'BS. Hoàng Gia Bảo', 'staff3@petcare.com', '0911000003', '$2a$10$Jky0mIwJGHnDiWsATQ1yjeK662dTo9buOoKITz1Tr6jmT3PcB8K42', 'STAFF', 3, true),
    -- Khách hàng
    (4, 'Lê Thị Thu Hà', 'customer@petcare.com', '0988000001', '$2a$10$Jky0mIwJGHnDiWsATQ1yjeK662dTo9buOoKITz1Tr6jmT3PcB8K42', 'CUSTOMER', NULL, true),
    (5, 'Phạm Quốc Huy', 'customer2@petcare.com', '0988000002', '$2a$10$Jky0mIwJGHnDiWsATQ1yjeK662dTo9buOoKITz1Tr6jmT3PcB8K42', 'CUSTOMER', NULL, true)
ON CONFLICT (id) DO NOTHING;

-- 3. Thú cưng của khách hàng (Pets)
INSERT INTO public.pets (id, owner_user_id, name, species)
VALUES
    (1, 4, 'Mochi', 'Chó Poodle'),
    (2, 4, 'Miu Miu', 'Mèo Anh Lông Ngắn'),
    (3, 5, 'Bơ', 'Chó Corgi'),
    (4, 5, 'Lu Lu', 'Mèo Ba Tư')
ON CONFLICT (id) DO NOTHING;

-- 4. Dịch vụ khám chữa (Services)
INSERT INTO public.services (id, clinic_id, name, duration_minutes, is_active)
VALUES
    -- Dịch vụ Phòng khám An Nhiên (Clinic 1)
    (1, 1, 'Khám sức khỏe tổng quát', 30, true),
    (2, 1, 'Tiêm vắc-xin 7 bệnh phòng dại', 20, true),
    (3, 1, 'Tẩy giun & nhỏ gáy trị ve rận', 15, true),
    (4, 1, 'Cắt móng, vệ sinh tai & chăm sóc lông', 30, true),
    -- Dịch vụ Phòng khám Gia Định (Clinic 2)
    (5, 2, 'Khám tổng quát & tư vấn dinh dưỡng', 30, true),
    (6, 2, 'Tiêm phòng dại & cấp sổ thú y', 20, true),
    (7, 2, 'Cạo vôi răng & vệ sinh răng miệng', 45, true),
    -- Dịch vụ Bệnh viện Tân Bình (Clinic 3)
    (8, 3, 'Khám chuyên khoa ngoại & chỉnh hình', 45, true),
    (9, 3, 'Siêu âm chẩn đoán hình ảnh', 30, true),
    (10, 3, 'Xét nghiệm máu tổng quát', 30, true)
ON CONFLICT (id) DO NOTHING;

-- 5. Khung giờ khám (Clinic Slots)
INSERT INTO public.clinic_slots (id, clinic_id, slot_date, start_time, end_time, status)
VALUES
    -- Clinic 1 - Khung giờ hôm nay & tương lai gần
    (1, 1, CURRENT_DATE + INTERVAL '1 day', '08:30:00', '09:00:00', 'AVAILABLE'),
    (2, 1, CURRENT_DATE + INTERVAL '1 day', '09:00:00', '09:30:00', 'BOOKED'),
    (3, 1, CURRENT_DATE + INTERVAL '1 day', '09:30:00', '10:00:00', 'AVAILABLE'),
    (4, 1, CURRENT_DATE + INTERVAL '1 day', '10:00:00', '10:30:00', 'AVAILABLE'),
    (5, 1, CURRENT_DATE + INTERVAL '1 day', '14:00:00', '14:30:00', 'AVAILABLE'),
    (6, 1, CURRENT_DATE + INTERVAL '1 day', '14:30:00', '15:00:00', 'AVAILABLE'),
    (7, 1, CURRENT_DATE + INTERVAL '2 day', '09:00:00', '09:30:00', 'AVAILABLE'),
    (8, 1, CURRENT_DATE + INTERVAL '2 day', '09:30:00', '10:00:00', 'AVAILABLE'),

    -- Clinic 2
    (9, 2, CURRENT_DATE + INTERVAL '1 day', '08:30:00', '09:00:00', 'AVAILABLE'),
    (10, 2, CURRENT_DATE + INTERVAL '1 day', '09:00:00', '09:30:00', 'BOOKED'),
    (11, 2, CURRENT_DATE + INTERVAL '1 day', '10:00:00', '10:30:00', 'AVAILABLE'),
    (12, 2, CURRENT_DATE + INTERVAL '1 day', '14:00:00', '14:30:00', 'AVAILABLE'),

    -- Clinic 3
    (13, 3, CURRENT_DATE + INTERVAL '1 day', '09:00:00', '09:30:00', 'AVAILABLE'),
    (14, 3, CURRENT_DATE + INTERVAL '1 day', '10:00:00', '10:30:00', 'AVAILABLE')
ON CONFLICT (id) DO NOTHING;

-- 6. Lịch hẹn mẫu (Appointments)
INSERT INTO public.appointments (id, customer_user_id, pet_id, clinic_id, service_id, slot_id, status)
VALUES
    -- Lịch hẹn đã đặt ở Clinic 1, slot 2 (status: PENDING để nhân viên duyệt)
    (1, 4, 1, 1, 1, 2, 'PENDING'),
    -- Lịch hẹn ở Clinic 2, slot 10 (status: CONFIRMED)
    (2, 5, 3, 2, 5, 10, 'CONFIRMED')
ON CONFLICT (id) DO NOTHING;

-- Cập nhật chuỗi auto-increment sequence nếu cần
SELECT setval('public.clinics_id_seq', (SELECT COALESCE(MAX(id), 1) FROM public.clinics));
SELECT setval('public.users_id_seq', (SELECT COALESCE(MAX(id), 1) FROM public.users));
SELECT setval('public.pets_id_seq', (SELECT COALESCE(MAX(id), 1) FROM public.pets));
SELECT setval('public.services_id_seq', (SELECT COALESCE(MAX(id), 1) FROM public.services));
SELECT setval('public.clinic_slots_id_seq', (SELECT COALESCE(MAX(id), 1) FROM public.clinic_slots));
SELECT setval('public.appointments_id_seq', (SELECT COALESCE(MAX(id), 1) FROM public.appointments));

COMMIT;
