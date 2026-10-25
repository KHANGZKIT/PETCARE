package com.petcare.booking;

import com.petcare.entity.AppointmentEntity;
import com.petcare.entity.AppointmentStatus;
import com.petcare.entity.ClinicEntity;
import com.petcare.entity.ClinicSlotEntity;
import com.petcare.entity.PetEntity;
import com.petcare.entity.ServiceEntity;
import com.petcare.entity.SlotStatus;
import com.petcare.entity.UserEntity;
import com.petcare.entity.UserRole;
import com.petcare.repository.AppointmentRepository;
import com.petcare.repository.ClinicRepository;
import com.petcare.repository.ClinicSlotRepository;
import com.petcare.repository.PetRepository;
import com.petcare.repository.ServiceRepository;
import com.petcare.repository.UserRepository;
import com.petcare.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test cho POST /api/v1/appointments với PostgreSQL thật chạy trong
 * Testcontainers, khởi tạo bằng chính file schema của dự án. ddl-auto: validate
 * (theo application.yml) nên context chỉ khởi động được khi entity khớp schema.
 * <p>
 * Container dùng chung cho cả class, nên mọi email/phone seed sinh unique
 * theo counter để không đụng unique constraint giữa các test method.
 * Tự động skip trên máy không có Docker (disabledWithoutDocker).
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = "jwt.secret-key=petcare-booking-it-test-key-0123456789abcdef")
@AutoConfigureMockMvc
class BookingApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withCopyFileToContainer(
                    MountableFile.forHostPath(
                            Paths.get("..", "database", "PETCARE_PostgreSQL_5weeks.sql")
                                    .toAbsolutePath()),
                    "/docker-entrypoint-initdb.d/init.sql");

    private static final String BOOKING_URL = "/api/v1/appointments";
    /** Giá trị chỉ dùng trong test, không phải secret thật. */
    private static final AtomicLong SEQ = new AtomicLong(System.nanoTime() % 1_000_000);

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;
    @Autowired private ClinicRepository clinicRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private ClinicSlotRepository clinicSlotRepository;
    @Autowired private AppointmentRepository appointmentRepository;

    private ClinicEntity clinic;
    private ClinicEntity otherClinic;
    private UserEntity customer;
    private UserEntity otherCustomer;
    private UserEntity staff;
    private PetEntity pet;
    private ServiceEntity service;
    private ClinicSlotEntity slot;

    @BeforeEach
    void seed() {
        clinic = newActiveClinic("Phòng khám IT");
        otherClinic = newActiveClinic("Phòng khám Khác");

        customer = saveUser("Khách Hàng IT", "it-customer", UserRole.CUSTOMER, null);
        otherCustomer = saveUser("Khách Khác", "it-other", UserRole.CUSTOMER, null);
        staff = saveUser("Nhân Viên IT", "it-staff", UserRole.STAFF, clinic);

        pet = newPet(customer);
        service = newService(clinic);
        slot = newAvailableSlot(clinic, 9, 0, 9, 30);
    }

    /** Entity dùng constructor protected — dựng qua reflection chỉ trong test. */
    private static <T> T instantiate(Class<T> type) {
        try {
            var ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Không dựng được entity test: " + type.getSimpleName(), e);
        }
    }

    private ClinicEntity newActiveClinic(String name) {
        ClinicEntity c = instantiate(ClinicEntity.class);
        c.setName(name);
        c.setAddress("123 Đường Test");
        c.setArea("Quận 1");
        c.setPhone(String.format("0900%07d", SEQ.incrementAndGet()));
        c.setActive(true);
        return clinicRepository.save(c);
    }

    private UserEntity saveUser(String name, String emailPrefix, UserRole role, ClinicEntity clinic) {
        long seq = SEQ.incrementAndGet();
        UserEntity u = new UserEntity(
                name,
                emailPrefix + "-" + seq + "@test.local",
                String.format("09%09d", seq),
                "not-a-real-hash",
                role);
        if (clinic != null) {
            u.setClinic(clinic);
        }
        return userRepository.save(u);
    }

    private PetEntity newPet(UserEntity owner) {
        PetEntity p = instantiate(PetEntity.class);
        p.setOwner(owner);
        p.setName("Thú cưng IT");
        p.setSpecies("Cat");
        return petRepository.save(p);
    }

    private ServiceEntity newService(ClinicEntity clinic) {
        ServiceEntity s = instantiate(ServiceEntity.class);
        s.setClinic(clinic);
        s.setName("Khám tổng quát");
        s.setDurationMinutes(30);
        s.setActive(true);
        return serviceRepository.save(s);
    }

    private ClinicSlotEntity newAvailableSlot(ClinicEntity clinic, int fromH, int fromM, int toH, int toM) {
        ClinicSlotEntity s = instantiate(ClinicSlotEntity.class);
        s.setClinic(clinic);
        s.setSlotDate(LocalDate.now().plusDays(1));
        s.setStartTime(LocalTime.of(fromH, fromM));
        s.setEndTime(LocalTime.of(toH, toM));
        s.setStatus(SlotStatus.AVAILABLE);
        return clinicSlotRepository.save(s);
    }

    private String bearer(UserEntity user) {
        return "Bearer " + jwtService.generateToken(user);
    }

    private String bookingBody(PetEntity pet, ClinicEntity clinic, ServiceEntity service, ClinicSlotEntity slot) {
        return """
                {"petId":%d,"clinicId":%d,"serviceId":%d,"slotId":%d}
                """.formatted(pet.getId(), clinic.getId(), service.getId(), slot.getId());
    }

    private ResultSender sendBooking(String body) {
        return new ResultSender(body);
    }

    private class ResultSender {
        private final String body;
        private String token;

        ResultSender(String body) { this.body = body; }

        ResultSender as(UserEntity user) {
            this.token = bearer(user);
            return this;
        }

        ResultSender withRawToken(String raw) {
            this.token = raw;
            return this;
        }

        ResultActions go() throws Exception {
            var request = post(BOOKING_URL).contentType(MediaType.APPLICATION_JSON).content(body);
            if (token != null) {
                request = request.header("Authorization", token);
            }
            return mockMvc.perform(request);
        }
    }

    @Test
    void validCustomer_booksAvailableSlot_returns201AndPersistsConsistently() throws Exception {
        sendBooking(bookingBody(pet, clinic, service, slot)).as(customer).go()
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.petId").value(pet.getId()))
                .andExpect(jsonPath("$.clinicId").value(clinic.getId()))
                .andExpect(jsonPath("$.serviceId").value(service.getId()))
                .andExpect(jsonPath("$.slotId").value(slot.getId()))
                .andExpect(jsonPath("$.createdAt").exists());

        ClinicSlotEntity after = clinicSlotRepository.findById(slot.getId()).orElseThrow();
        assertThat(after.getStatus()).isEqualTo(SlotStatus.BOOKED);

        List<AppointmentEntity> created =
                appointmentRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId());
        assertThat(created).hasSize(1);
        assertThat(created.get(0).getStatus()).isEqualTo(AppointmentStatus.PENDING);
        assertThat(created.get(0).getCreatedAt()).isNotNull();
    }

    @Test
    void noToken_returns401() throws Exception {
        sendBooking(bookingBody(pet, clinic, service, slot)).go()
                .andExpect(status().isUnauthorized());
    }

    @Test
    void garbageToken_returns401() throws Exception {
        sendBooking(bookingBody(pet, clinic, service, slot))
                .withRawToken("Bearer khong-phai-jwt").go()
                .andExpect(status().isUnauthorized());
    }

    @Test
    void staffToken_returns403() throws Exception {
        sendBooking(bookingBody(pet, clinic, service, slot)).as(staff).go()
                .andExpect(status().isForbidden());

        assertThat(appointmentRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId()))
                .isEmpty();
        assertThat(clinicSlotRepository.findById(slot.getId()).orElseThrow().getStatus())
                .isEqualTo(SlotStatus.AVAILABLE);
    }

    @Test
    void missingPetField_returns400() throws Exception {
        String body = """
                {"clinicId":%d,"serviceId":%d,"slotId":%d}
                """.formatted(clinic.getId(), service.getId(), slot.getId());
        sendBooking(body).as(customer).go()
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.petId").exists());
    }

    @Test
    void petBelongingToAnotherCustomer_returns400() throws Exception {
        PetEntity othersPet = newPet(otherCustomer);
        sendBooking(bookingBody(othersPet, clinic, service, slot)).as(customer).go()
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Không tìm thấy thú cưng thuộc tài khoản của bạn"));

        assertThat(appointmentRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId()))
                .isEmpty();
        assertThat(clinicSlotRepository.findById(slot.getId()).orElseThrow().getStatus())
                .isEqualTo(SlotStatus.AVAILABLE);
    }

    @Test
    void slotFromAnotherClinic_returns400() throws Exception {
        ClinicSlotEntity foreignSlot = newAvailableSlot(otherClinic, 10, 0, 10, 30);
        sendBooking(bookingBody(pet, clinic, service, foreignSlot)).as(customer).go()
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Khung giờ không thuộc phòng khám đã chọn"));
    }

    @Test
    void secondBookingOnBookedSlot_returns409() throws Exception {
        sendBooking(bookingBody(pet, clinic, service, slot)).as(customer).go()
                .andExpect(status().isCreated());

        PetEntity secondPet = newPet(customer);
        sendBooking(bookingBody(secondPet, clinic, service, slot)).as(customer).go()
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Khung giờ đã được đặt hoặc đã đóng, vui lòng chọn khung giờ khác"));

        assertThat(appointmentRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId()))
                .hasSize(1);
    }

    @Test
    void twoConcurrentBookingsOnSameSlot_exactlyOneWins() throws Exception {
        UserEntity customerA = saveUser("Khách A", "it-race-a", UserRole.CUSTOMER, null);
        UserEntity customerB = saveUser("Khách B", "it-race-b", UserRole.CUSTOMER, null);
        PetEntity petA = newPet(customerA);
        PetEntity petB = newPet(customerB);
        ClinicSlotEntity raceSlot = newAvailableSlot(clinic, 15, 0, 15, 30);

        CountDownLatch startGate = new CountDownLatch(1);
        Callable<Integer> requestA = () -> {
            startGate.await();
            return sendBooking(bookingBody(petA, clinic, service, raceSlot)).as(customerA).go()
                    .andReturn().getResponse().getStatus();
        };
        Callable<Integer> requestB = () -> {
            startGate.await();
            return sendBooking(bookingBody(petB, clinic, service, raceSlot)).as(customerB).go()
                    .andReturn().getResponse().getStatus();
        };

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> futureA = pool.submit(requestA);
            Future<Integer> futureB = pool.submit(requestB);
            startGate.countDown();

            int statusA = futureA.get(60, TimeUnit.SECONDS);
            int statusB = futureB.get(60, TimeUnit.SECONDS);

            //Đúng một request 201 Created, request còn lại 409 Conflict
            assertThat(List.of(statusA, statusB)).containsExactlyInAnyOrder(201, 409);
        } finally {
            pool.shutdownNow();
        }

        //Trạng thái cuối nhất quán: slot BOOKED và đúng 1 appointment active trên slot
        ClinicSlotEntity after = clinicSlotRepository.findById(raceSlot.getId()).orElseThrow();
        assertThat(after.getStatus()).isEqualTo(SlotStatus.BOOKED);
        List<AppointmentEntity> all = appointmentRepository.findAll();
        long activeOnSlot = all.stream()
                .filter(a -> a.getSlot().getId().equals(raceSlot.getId()))
                .filter(a -> a.getStatus() == AppointmentStatus.PENDING
                        || a.getStatus() == AppointmentStatus.CONFIRMED)
                .count();
        assertThat(activeOnSlot).isEqualTo(1);
    }

    @Test
    void databasePartialIndex_blocksSecondActiveAppointmentPerSlot() {
        //Lớp bảo vệ cuối: chèn trực tiếp 2 appointment active cùng slot qua repository
        //phải vi phạm unique index uq_appointments_one_active_per_slot của schema thật.
        AppointmentEntity first = new AppointmentEntity();
        first.setCustomer(customer);
        first.setPet(pet);
        first.setClinic(clinic);
        first.setService(service);
        first.setSlot(slot);
        first.setStatus(AppointmentStatus.PENDING);
        appointmentRepository.saveAndFlush(first);

        PetEntity secondPet = newPet(customer);
        AppointmentEntity second = new AppointmentEntity();
        second.setCustomer(customer);
        second.setPet(secondPet);
        second.setClinic(clinic);
        second.setService(service);
        second.setSlot(slot);
        second.setStatus(AppointmentStatus.CONFIRMED);

        assertThatThrownBy(() -> appointmentRepository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);

        //Chỉ appointment đầu tiên của customer này tồn tại; appointment thứ hai bị DB chặn
        assertThat(appointmentRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId()))
                .hasSize(1);
    }
}
