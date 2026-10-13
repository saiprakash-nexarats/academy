package com.nexarats.academy.admissions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexarats.academy.admissions.dto.ReviewApplicationRequest;
import com.nexarats.academy.admissions.dto.SubmitApplicationRequest;
import com.nexarats.academy.admissions.model.AdmissionOffer;
import com.nexarats.academy.admissions.model.AdmissionOfferStatus;
import com.nexarats.academy.admissions.model.Application;
import com.nexarats.academy.admissions.model.ApplicationStatus;
import com.nexarats.academy.admissions.model.Enrollment;
import com.nexarats.academy.admissions.model.EnrollmentStatus;
import com.nexarats.academy.admissions.repository.AdmissionOfferRepository;
import com.nexarats.academy.admissions.repository.ApplicationRepository;
import com.nexarats.academy.admissions.repository.EnrollmentRepository;
import com.nexarats.academy.admissions.service.ApplicationService;
import com.nexarats.academy.cohort.model.Cohort;
import com.nexarats.academy.cohort.model.CohortStatus;
import com.nexarats.academy.cohort.model.SeatReservation;
import com.nexarats.academy.cohort.model.SeatReservationStatus;
import com.nexarats.academy.cohort.repository.CohortRepository;
import com.nexarats.academy.cohort.repository.SeatReservationRepository;
import com.nexarats.academy.cohort.service.CohortSeatService;
import com.nexarats.academy.finance.dto.CreatePaymentOrderRequest;
import com.nexarats.academy.finance.dto.VerifyPaymentRequest;
import com.nexarats.academy.finance.model.Invoice;
import com.nexarats.academy.finance.model.InvoiceStatus;
import com.nexarats.academy.finance.model.PaymentOrder;
import com.nexarats.academy.finance.model.PaymentOrderStatus;
import com.nexarats.academy.finance.model.PaymentProvider;
import com.nexarats.academy.finance.repository.InvoiceRepository;
import com.nexarats.academy.finance.repository.PaymentOrderRepository;
import com.nexarats.academy.finance.service.PaymentOrderService;
import com.nexarats.academy.identity.dto.BootstrapAdminRequest;
import com.nexarats.academy.identity.dto.LoginRequest;
import com.nexarats.academy.identity.dto.LoginResponse;
import com.nexarats.academy.identity.dto.RegisterRequest;
import com.nexarats.academy.identity.service.AuthService;
import com.nexarats.academy.identity.service.RateLimitingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
class AdmissionsAndEnrollmentIntegrationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AuthService authService;

    @Autowired
    private RateLimitingService rateLimitingService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AdmissionOfferRepository admissionOfferRepository;

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private SeatReservationRepository seatReservationRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CohortSeatService cohortSeatService;

    @Autowired
    private PaymentOrderService paymentOrderService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private String studentToken;
    private UUID studentUserId;
    private String otherStudentToken;
    private UUID otherStudentUserId;
    private String adminToken;
    private String testIp;
    private Cohort testCohort;

    @BeforeEach
    void setUp() {
        rateLimitingService.resetRateLimits();
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        testIp = "10.0." + (int)(Math.random() * 200) + "." + (int)(Math.random() * 200);

        // 1. Register Student 1
        String studentEmail = "admissions.student1." + suffix + "@nexarats.academy";
        RegisterRequest regReq = new RegisterRequest(studentEmail, "StudentPass2026!", "Aarav", "Patel", null);
        LoginResponse studentLogin = authService.register(regReq, testIp, "TestAgent");
        studentToken = studentLogin.accessToken();
        studentUserId = studentLogin.user().id();

        // 2. Register Student 2
        String otherEmail = "admissions.student2." + suffix + "@nexarats.academy";
        RegisterRequest otherReq = new RegisterRequest(otherEmail, "StudentPass2026!", "Diya", "Menon", null);
        LoginResponse otherLogin = authService.register(otherReq, testIp, "TestAgent");
        otherStudentToken = otherLogin.accessToken();
        otherStudentUserId = otherLogin.user().id();

        // 3. Bootstrap Admin
        String adminEmail = "admissions.admin." + suffix + "@nexarats.academy";
        BootstrapAdminRequest bootReq = new BootstrapAdminRequest(
                "dev-bootstrap-secret-2026",
                adminEmail,
                "SuperAdminPass2026!",
                "Admissions",
                "Dean",
                "nexarats-main",
                "Nexarats Academy"
        );
        authService.bootstrapAdmin(bootReq, testIp, "TestAgent");

        LoginRequest adminLoginReq = new LoginRequest(adminEmail, "SuperAdminPass2026!", "nexarats-main", "WEB");
        LoginResponse adminLogin = authService.login(adminLoginReq, testIp, "TestAgent");
        adminToken = adminLogin.accessToken();

        // 4. Ensure an active test cohort exists
        testCohort = cohortRepository.findAll().stream()
                .filter(c -> c.getStatus() == CohortStatus.OPEN_FOR_ENROLLMENT)
                .findFirst()
                .orElseGet(() -> {
                    Cohort newCohort = new Cohort(
                            "COHORT-TEST-" + suffix,
                            "Full-Stack Web Dev Test Cohort",
                            LocalDate.now().plusDays(10),
                            Instant.now().plus(30, ChronoUnit.DAYS),
                            30
                    );
                    return cohortRepository.save(newCohort);
                });
    }

    @Test
    @DisplayName("Admissions: Public guest inquiry submission succeeds and creates SUBMITTED application")
    void testGuestApplicationSubmission_Success() throws Exception {
        SubmitApplicationRequest request = new SubmitApplicationRequest(
                "Guest Learner",
                "guest." + UUID.randomUUID().toString().substring(0, 6) + "@example.com",
                "+19876543210",
                "full-stack-web-development",
                "beginner",
                "I want to become a full-stack software engineer."
        );

        mockMvc.perform(post("/api/v1/admissions/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.applicationNumber", startsWith("APP-")))
                .andExpect(jsonPath("$.fullName").value("Guest Learner"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    @DisplayName("Admissions: Authenticated student submits application successfully linking learner identity")
    void testAuthenticatedStudentApplicationSubmission_Success() throws Exception {
        SubmitApplicationRequest request = new SubmitApplicationRequest(
                "Aarav Patel",
                "aarav.linked." + UUID.randomUUID().toString().substring(0, 6) + "@nexarats.academy",
                "+15551234567",
                "full-stack-web-development",
                "intermediate",
                "Aspiring enterprise developer."
        );

        mockMvc.perform(post("/api/v1/admissions/applications")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicationNumber", startsWith("APP-")))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    @DisplayName("Admissions: Duplicate active application for the same target program returns HTTP 409 Conflict")
    void testDuplicateApplication_Conflict409() throws Exception {
        String email = "duplicate.check." + UUID.randomUUID().toString().substring(0, 6) + "@nexarats.academy";
        SubmitApplicationRequest request = new SubmitApplicationRequest(
                "Duplicate Applicant",
                email,
                "+15559876543",
                "full-stack-web-development",
                "beginner",
                "Statement 1"
        );

        // First submission succeeds
        mockMvc.perform(post("/api/v1/admissions/applications")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Duplicate submission for same program is rejected with 409
        mockMvc.perform(post("/api/v1/admissions/applications")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Admissions: Student can retrieve own applications, but is denied other student's application with 403")
    void testApplicationOwnershipAndAccessControl() throws Exception {
        // 1. Student 1 submits application
        SubmitApplicationRequest request = new SubmitApplicationRequest(
                "Aarav Patel",
                "student1.access." + UUID.randomUUID().toString().substring(0, 6) + "@nexarats.academy",
                "+15551112233",
                "full-stack-web-development",
                "beginner",
                "My application"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/admissions/applications")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String appJson = result.getResponse().getContentAsString();
        String appId = objectMapper.readTree(appJson).get("id").asText();

        // 2. Student 1 can view own applications list
        mockMvc.perform(get("/api/v1/admissions/applications/mine")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        // 3. Student 1 can view own application by ID
        mockMvc.perform(get("/api/v1/admissions/applications/" + appId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appId));

        // 4. Student 2 is DENIED viewing Student 1's application with HTTP 403 Forbidden
        mockMvc.perform(get("/api/v1/admissions/applications/" + appId)
                        .header("Authorization", "Bearer " + otherStudentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admissions Review: Admin approval creates admission offer and seat reservation")
    void testAdmissionsReviewWorkflow_Approve_GeneratesOfferAndReservation() throws Exception {
        // 1. Submit application
        SubmitApplicationRequest request = new SubmitApplicationRequest(
                "Admissions Review Candidate",
                "candidate.review." + UUID.randomUUID().toString().substring(0, 6) + "@nexarats.academy",
                "+15553334455",
                "full-stack-web-development",
                "beginner",
                "Eager to learn enterprise engineering"
        );

        MvcResult submitResult = mockMvc.perform(post("/api/v1/admissions/applications")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String appId = objectMapper.readTree(submitResult.getResponse().getContentAsString()).get("id").asText();

        // 2. Admin reviews and approves application
        ReviewApplicationRequest reviewReq = new ReviewApplicationRequest(
                "APPROVED",
                "Candidate meets academic prerequisites and interview standards.",
                null,
                testCohort.getId(),
                null,
                "USD"
        );

        MvcResult reviewResult = mockMvc.perform(post("/api/v1/admissions/applications/" + appId + "/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application.status").value("APPROVED"))
                .andExpect(jsonPath("$.offer.offerNumber", startsWith("OFFER-")))
                .andExpect(jsonPath("$.offer.status").value("ISSUED"))
                .andExpect(jsonPath("$.reservation.reservationToken", startsWith("RES-")))
                .andExpect(jsonPath("$.reservation.status").value("RESERVED"))
                .andReturn();

        // 3. Student retrieves the generated admission offer
        mockMvc.perform(get("/api/v1/admissions/applications/" + appId + "/offer")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ISSUED"))
                .andExpect(jsonPath("$.offeredAmountCents", greaterThan(0)));
    }

    @Test
    @DisplayName("Admissions Review: Admin rejection sets REJECTED status with governed reason")
    void testAdmissionsReviewWorkflow_Reject_GeneratesRejectionReason() throws Exception {
        SubmitApplicationRequest request = new SubmitApplicationRequest(
                "Incomplete Applicant",
                "incomplete." + UUID.randomUUID().toString().substring(0, 6) + "@nexarats.academy",
                "+15557778899",
                "full-stack-web-development",
                "beginner",
                "Needs more prerequisite coursework."
        );

        MvcResult submitResult = mockMvc.perform(post("/api/v1/admissions/applications")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String appId = objectMapper.readTree(submitResult.getResponse().getContentAsString()).get("id").asText();

        ReviewApplicationRequest rejectReq = new ReviewApplicationRequest(
                "REJECTED",
                null,
                "Prerequisite academic requirements not fulfilled for this cohort.",
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/admissions/applications/" + appId + "/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application.status").value("REJECTED"));
    }

    @Test
    @DisplayName("Payment & Enrollment: Full end-to-end flow from payment verification to confirmed enrollment and invoice")
    void testPaymentVerificationAndEnrollmentSettlement() throws Exception {
        // 1. Create a payment order with Test Gateway
        String idempotencyKey = UUID.randomUUID().toString();
        CreatePaymentOrderRequest orderReq = new CreatePaymentOrderRequest(
                null,
                null,
                testCohort.getId(),
                null,
                "USD",
                PaymentProvider.TEST_GATEWAY,
                idempotencyKey
        );

        MvcResult orderResult = mockMvc.perform(post("/api/v1/payments/orders")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber", startsWith("ORD-")))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amountCents", greaterThan(0)))
                .andReturn();

        String orderNumber = objectMapper.readTree(orderResult.getResponse().getContentAsString()).get("orderNumber").asText();

        // 2. Verify payment with valid test signature -> settles order, activates enrollment, generates invoice
        VerifyPaymentRequest verifyReq = new VerifyPaymentRequest(orderNumber, "test_tx_" + UUID.randomUUID(), "valid_test_sig");

        mockMvc.perform(post("/api/v1/payments/orders/verify")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        // 3. Verify learner can retrieve confirmed enrollment
        mockMvc.perform(get("/api/v1/admissions/enrollments/mine")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].enrollmentNumber", startsWith("ENR-")))
                .andExpect(jsonPath("$[0].status").value("ENROLLED"));

        // 4. Verify learner can view paid tuition invoice
        mockMvc.perform(get("/api/v1/billing/invoices/mine")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].invoiceNumber", startsWith("INV-")))
                .andExpect(jsonPath("$[0].status").value("PAID"));

        // 5. Verify idempotent settlement: repeated verification call does not fail or duplicate enrollment
        mockMvc.perform(post("/api/v1/payments/orders/verify")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    @DisplayName("Seat Concurrency: Atomic pessimistic locking prevents overselling when multiple threads compete for final seats")
    void testConcurrentSeatReservation_PreventsOverselling() throws Exception {
        // Create a small cohort with capacity = 2
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Cohort scarceCohort = new Cohort(
                "SCARCE-" + suffix,
                "High Demand AI Seminar",
                LocalDate.now().plusDays(5),
                Instant.now().plus(10, ChronoUnit.DAYS),
                2 // Exactly 2 seats available!
        );
        Cohort savedCohort = cohortRepository.save(scarceCohort);
        UUID scarceCohortId = savedCohort.getId();

        int numberOfThreads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Synchronize all threads to fire simultaneously
                    cohortSeatService.reserveSeat(
                            scarceCohortId,
                            null,
                            null,
                            null,
                            15
                    );
                    successCount.incrementAndGet();
                } catch (ResponseStatusException e) {
                    if (e.getStatusCode().value() == 409) {
                        conflictCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Fire all threads at once
        startLatch.countDown();
        boolean completed = finishLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).isTrue();
        // EXACTLY 2 threads succeeded because capacity = 2!
        assertThat(successCount.get()).isEqualTo(2);
        // The other 6 threads received HTTP 409 Conflict
        assertThat(conflictCount.get()).isEqualTo(6);

        // Cohort status should now be marked FULL or available seats 0
        int remaining = cohortSeatService.getAvailableSeats(scarceCohortId);
        assertThat(remaining).isEqualTo(0);
    }

    @Test
    @DisplayName("Payment Webhook: Replay attack defense returns HTTP 200 idempotently without reprocessing")
    void testWebhookReplayDefense_IdempotentHandling() throws Exception {
        String eventId = "evt_test_" + UUID.randomUUID();
        String payload = "{\"id\":\"" + eventId + "\",\"type\":\"payment_intent.succeeded\",\"data\":{\"orderNumber\":\"ORD-NONE\"}}";

        // First delivery: processes successfully
        PaymentOrderService.PaymentWebhookResult r1 = paymentOrderService.handleWebhook("TEST_GATEWAY", payload, "valid_test_sig");
        assertThat(r1.success()).isTrue();

        // Second delivery (replay): handled idempotently
        PaymentOrderService.PaymentWebhookResult r2 = paymentOrderService.handleWebhook("TEST_GATEWAY", payload, "valid_test_sig");
        assertThat(r2.success()).isTrue();
        assertThat(r2.message()).contains("Duplicate event processed idempotently");
    }

    @Test
    @DisplayName("Billing Security: Student cannot access invoice belonging to another student")
    void testCrossUserInvoiceAccess_Forbidden403() throws Exception {
        // Create an order and invoice for Student 1
        String orderNum = "ORD-SEC-" + UUID.randomUUID().toString().substring(0, 8);
        PaymentOrder order = new PaymentOrder(orderNum, 99900L, "USD", PaymentProvider.TEST_GATEWAY, UUID.randomUUID().toString());
        order.setLearnerUserId(studentUserId);
        order.setStatus(PaymentOrderStatus.PAID);
        order.setPaidAt(Instant.now());
        PaymentOrder savedOrder = paymentOrderRepository.save(order);

        Invoice invoice = new Invoice("INV-SEC-" + UUID.randomUUID().toString().substring(0, 8), studentUserId, savedOrder, 99900L, "USD");
        invoice.setStatus(InvoiceStatus.PAID);
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Student 1 can access own invoice -> 200 OK
        mockMvc.perform(get("/api/v1/billing/invoices/" + savedInvoice.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedInvoice.getId().toString()));

        // Student 2 is DENIED access to Student 1's invoice -> 403 Forbidden
        mockMvc.perform(get("/api/v1/billing/invoices/" + savedInvoice.getId())
                        .header("Authorization", "Bearer " + otherStudentToken))
                .andExpect(status().isForbidden());
    }
}
