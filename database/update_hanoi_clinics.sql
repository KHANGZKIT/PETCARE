ALTER TABLE public.clinics ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;
ALTER TABLE public.clinics ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

UPDATE public.clinics SET 
    name = 'Phòng khám Thú y An Nhiên',
    address = 'Số 12 Chùa Hà, Phường Quan Hoa',
    area = 'Cầu Giấy',
    phone = '02438335566',
    working_hours_note = '08:00 - 20:00 (Thứ 2 - Chủ Nhật)',
    latitude = 21.0360,
    longitude = 105.7950
WHERE id = 1;

UPDATE public.clinics SET 
    name = 'Bệnh viện Thú y PetCare HaUI',
    address = 'Số 298 Đường Cầu Diễn, Phường Minh Khai',
    area = 'Bắc Từ Liêm',
    phone = '02437655121',
    working_hours_note = '24/7 Cấp cứu & Khám chữa',
    latitude = 21.0537,
    longitude = 105.7351
WHERE id = 2;

UPDATE public.clinics SET 
    name = 'Bệnh viện Thú y PetCare Đống Đa',
    address = 'Số 88 Thái Hà, Phường Trung Liệt',
    area = 'Đống Đa',
    phone = '02438561122',
    working_hours_note = '08:00 - 18:00 (Thứ 2 - Thứ 7)',
    latitude = 21.0125,
    longitude = 105.8190
WHERE id = 3;

SELECT id, name, area, address, latitude, longitude FROM public.clinics;

