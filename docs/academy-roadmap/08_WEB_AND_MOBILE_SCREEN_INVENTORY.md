# NEXARATS ACADEMY — WEB & MOBILE SCREEN INVENTORY
## Complete Catalog of Web Portals, Views, Routes, and Mobile App Screens

- **Document ID:** `08_WEB_AND_MOBILE_SCREEN_INVENTORY`
- **Execution Date:** 2026-10-13
- **Classification:** UI/UX Screen Architecture & Route Inventory

---

## 1. Screen Architecture Summary

The complete Nexarats Academy ecosystem spans **9 distinct application surfaces**:
1. **Public Academy Website (Preserved & Enhanced):** 8 routes (Guest / Public)
2. **Student Learning Portal (LMS):** 30 screens (Protected `ROLE_STUDENT`)
3. **Mentor & Instructor Portal:** 20 screens (Protected `ROLE_MENTOR`)
4. **Academy Administration Portal:** 35 screens (Protected `ROLE_ACADEMY_ADMIN`)
5. **Corporate Organization Admin Portal:** 25 screens (Protected `ROLE_CORP_ADMIN`)
6. **Corporate Employee L&D Portal:** 20 screens (Protected `ROLE_EMPLOYEE`)
7. **Parent & Guardian Portal:** 10 screens (Protected `ROLE_GUARDIAN`)
8. **Platform Super Admin Operations Portal:** 25 screens (Protected `ROLE_SUPER_ADMIN`)
9. **Android & iOS Native Mobile Application:** 42 screens (Cross-Platform Flutter Native)

---

## 2. Public Academy Website (Existing Preserved Routes)

| Screen Name | Route Path | Access | Primary UI Components | Data Requirements |
| :--- | :--- | :--- | :--- | :--- |
| **Home Landing Page** | `/` | Public | HeroSection, StatsStrip, PopularProgramsSection, JobAssuranceSection, WhyChooseSection, ExperienceSection, TestimonialsSection, CtaBannerSection | Featured programs, impact stats, testimonials, platform previews |
| **Programs Catalog** | `/programs` | Public | CategoryFilterTabs, SearchBar, ProgramCardGrid, ProgramCompareDrawer | Full program list with fees, duration, syllabi |
| **Program Detail** | `/programs/:programId` | Public | ProgramHero, SyllabusAccordion, MentorPillars, CareerOutcomes, TuitionCalculator | Single program deep-dive, cohort dates, curriculum |
| **Courses Library** | `/courses` | Public | DomainTabs, LevelFilter, CourseCardGrid, InteractiveSyllabusModal | Modular skill courses, module list, instructor bios |
| **Mentors Directory** | `/mentors` | Public | DomainFilter, MentorCardGrid, MentorBioModal, BookingSlotCalendar | 80+ mentor profiles, upcoming slots, discussion topics |
| **Success Stories** | `/success-stories` | Public | OutcomesHero, SalaryGrowthStats, HiringPartnersTicker, StoryModal | Alumni case studies, capstones, salary multipliers |
| **About Institutional** | `/about` | Public | MissionBanner, GlobalImpactCards, FourPillars, MethodologyStepper, FAQAccordion | Company history, pedagogy methodology, accreditation |
| **Contact Admissions** | `/contact` | Public | AdmissionsOfficeCard, DirectHelpline, CampusMap, InquiryForm | Contact info, campus hours, admissions FAQ |
| **404 Not Found** | `*` | Public | ErrorCodeGraphic, BackToHomeButton | Static route fallback |

---

## 3. Student Learning Portal (LMS) — 30 Screens

```text
/portal/student/
```

| Screen ID | Sub-Route | Screen Name & Purpose | Primary Components | Auth Level |
| :--- | :--- | :--- | :--- | :--- |
| `STU-01` | `dashboard` | **Student Home Overview:** Active streaks, upcoming classes, next lesson | StreakCounter, LiveClassBanner, ContinueLearningCard, ProgressDonut | `ROLE_STUDENT` |
| `STU-02` | `my-courses` | **Enrolled Courses Grid:** Filter by in-progress, completed, paused | CourseProgressCard, FilterChips, CertificateQuickLink | `ROLE_STUDENT` |
| `STU-03` | `courses/:id/player` | **Curriculum Video Player:** Video with speed control, transcripts | HLSVideoPlayer, TranscriptDrawer, LessonNotesEditor, ResourceDownloads | `ROLE_STUDENT` |
| `STU-04` | `courses/:id/sandbox` | **In-Browser IDE Lab:** Monaco code editor with live test runner | SplitPaneEditor, TerminalOutput, TestRunnerConsole, ResetCodeModal | `ROLE_STUDENT` |
| `STU-05` | `live-sessions` | **Live Schedule Calendar:** Calendar of live cohort lectures | MonthWeekCalendar, JoinRoomButton, AddToGoogleCalendar | `ROLE_STUDENT` |
| `STU-06` | `live-sessions/:id/room` | **WebRTC Live Classroom:** Interactive live video with instructor | LiveKitVideoMesh, ChatSidebar, WhiteboardCanvas, PollModal | `ROLE_STUDENT` |
| `STU-07` | `live-sessions/recordings` | **Class Recordings Archive:** Past lecture recordings with search | RecordingList, SearchFilter, TranscribeSearchInput | `ROLE_STUDENT` |
| `STU-08` | `assignments` | **Assignment Tracker:** Deadlines, submitted, graded items | AssignmentStatusTable, DueDateBadges, GradePill | `ROLE_STUDENT` |
| `STU-09` | `assignments/:id/submit` | **Assignment Submission:** Git repo link / file upload / text entry | RepoInputForm, FileDropzone, RubricChecklist | `ROLE_STUDENT` |
| `STU-10` | `assignments/:id/feedback` | **Mentor Grading Feedback:** Line-by-line code review comments | GitDiffViewer, InlineCodeComment, RubricScorecard, MentorAudioClip | `ROLE_STUDENT` |
| `STU-11` | `exams` | **Assessments & Quizzes:** Upcoming exams and past scores | ExamScheduleCard, ProctoringStatusPill, RetakePolicyModal | `ROLE_STUDENT` |
| `STU-12` | `exams/:id/proctored` | **Proctored Examination:** Secure exam room with anti-cheat checks | FullscreenExamInterface, WebcamPreview, QuestionTimer, CodeSandbox | `ROLE_STUDENT` |
| `STU-13` | `exams/:id/results` | **Exam Score Analysis:** Detailed breakdown by sub-topic competency | TopicRadarChart, CorrectAnswersModal, ImprovementRoadmap | `ROLE_STUDENT` |
| `STU-14` | `mentorship` | **1:1 Mentorship Hub:** Assigned mentor overview, credits balance | MentorBioCard, SessionCreditsCounter, BookNewSessionButton | `ROLE_STUDENT` |
| `STU-15` | `mentorship/book` | **Schedule 1:1 Session:** Select date, time slot, discussion topic | SlotPickerGrid, TopicSelector, PreSessionNotesInput | `ROLE_STUDENT` |
| `STU-16` | `mentorship/:id/call` | **1:1 Video Consultation:** Dedicated WebRTC private video room | 1on1VideoStage, SharedNotesPad, ScreenShareButton | `ROLE_STUDENT` |
| `STU-17` | `ai-tutor` | **24/7 Course AI Tutor:** Chatbot grounded in syllabus materials | StreamingChatWindow, CodeSnippetExpander, SourceCitationPill | `ROLE_STUDENT` |
| `STU-18` | `community` | **Cohort Discussion Forum:** Channels, discussions, Q&A threads | ChannelSidebar, ThreadList, UpvoteCounter, MarkdownEditor | `ROLE_STUDENT` |
| `STU-19` | `community/threads/:id` | **Thread Detail & Replies:** Code snippets, verified instructor answers | QuestionHeader, AnswerCard, BestAnswerBadge, ReplyBox | `ROLE_STUDENT` |
| `STU-20` | `messages` | **Direct Messaging:** Private chat with mentors and peers | UserContactList, ChatMessageThread, FileAttachmentTrigger | `ROLE_STUDENT` |
| `STU-21` | `credentials` | **My Certificates & Badges:** List of earned verified credentials | CertificatePreviewCard, DownloadPDFButton, LinkedInShareButton | `ROLE_STUDENT` |
| `STU-22` | `credentials/:id/view` | **Digital Credential Viewer:** Vector certificate display with QR | CertificateRenderer, CredentialHashPill, VerificationQR | `ROLE_STUDENT` |
| `STU-23` | `transcripts` | **Official Academic Transcript:** GPA, hours completed, skills | OfficialTranscriptTable, StampGraphic, PrintTranscriptButton | `ROLE_STUDENT` |
| `STU-24` | `career-hub` | **Placement & Career Center:** Job board, applications, drives | JobOpeningGrid, ApplicationTracker, JobAssuranceBadge | `ROLE_STUDENT` |
| `STU-25` | `career-hub/resume` | **ATS Resume Builder:** Auto-imports capstones & verified skills | ResumeTemplateSelector, LiveResumePreview, ExportPDF | `ROLE_STUDENT` |
| `STU-26` | `career-hub/mock-crucible` | **Mock Interview Scheduler:** Book Big Tech mock interview | InterviewTypePicker, InterviewerProfile, ScorecardHistory | `ROLE_STUDENT` |
| `STU-27` | `profile` | **Student Profile:** Bio, GitHub/LinkedIn links, portfolio URL | ProfileForm, AvatarUploadDropzone, SkillTagEditor | `ROLE_STUDENT` |
| `STU-28` | `settings/security` | **Account Security:** Password change, MFA setup, active sessions | PasswordForm, AuthenticatorQRCode, SessionDeviceList | `ROLE_STUDENT` |
| `STU-29` | `settings/notifications` | **Notification Preferences:** Email, SMS, WhatsApp, In-app toggles | NotificationToggleList, QuietHoursPicker | `ROLE_STUDENT` |
| `STU-30` | `finance/installments` | **Tuition & Payment Ledger:** Invoices, upcoming dues, receipts | InvoiceHistoryTable, PayInstallmentButton, TaxReceiptDownload | `ROLE_STUDENT` |

---

## 4. Mentor & Instructor Portal — 20 Screens

```text
/portal/mentor/
```

| Screen ID | Sub-Route | Screen Name & Purpose | Primary Components | Auth Level |
| :--- | :--- | :--- | :--- | :--- |
| `MEN-01` | `dashboard` | **Mentor Studio Home:** Upcoming classes, pending grading count | TeachingScheduleCard, GradingQueueBadge, RatingSummary | `ROLE_MENTOR` |
| `MEN-02` | `schedule` | **Instructor Calendar:** Assigned cohorts, office hours, 1:1 calls | MultiViewCalendar, AddOfficeHoursModal, SyncCalendarButton | `ROLE_MENTOR` |
| `MEN-03` | `availability` | **1:1 Slot Availability Manager:** Define weekly recurring slots | RecurringSlotGrid, DateOverridePicker, MinimumNoticeSelector | `ROLE_MENTOR` |
| `MEN-04` | `cohorts` | **My Cohorts:** List of cohorts assigned to mentor | CohortCardGrid, StudentRosterCount, ProgressMeter | `ROLE_MENTOR` |
| `MEN-05` | `cohorts/:id` | **Cohort Roster & Hub:** Student roster, attendance logs, risk flags | StudentTable, AttendanceRatePills, FlagAtRiskStudentModal | `ROLE_MENTOR` |
| `MEN-06` | `cohorts/:id/live-studio` | **Instructor Live Studio:** Host WebRTC session with screen share | HostVideoControls, ScreenShareToggle, PollCreator, MuteAllButton | `ROLE_MENTOR` |
| `MEN-07` | `cohorts/:id/polls` | **Live Polls & Quizzes:** Real-time poll launcher for live sessions | PollQuestionForm, LiveBarChartResults, PublishResultsButton | `ROLE_MENTOR` |
| `MEN-08` | `grading/queue` | **Grading Inbox:** Submissions requiring mentor evaluation | FilterableQueueTable, UrgencyBadge, CourseFilter | `ROLE_MENTOR` |
| `MEN-09` | `grading/:id/workbench` | **Code Review Workbench:** Side-by-side Git diff with line comments | GitDiffViewer, InlineCommentBubble, RubricScoringDrawer | `ROLE_MENTOR` |
| `MEN-10` | `grading/:id/audio-feedback` | **Voice / Video Feedback Recorder:** Record 2-minute review video | AudioRecorderWidget, WebcamRecorder, UploadFeedbackButton | `ROLE_MENTOR` |
| `MEN-11` | `mentorship/sessions` | **1:1 Session History:** Past and upcoming 1:1 mentorship calls | SessionHistoryTable, StudentNotesModal, VideoRecordingLink | `ROLE_MENTOR` |
| `MEN-12` | `mentorship/:id/notes` | **Post-Session Feedback:** Mentor writes summary notes & goals | GoalChecklist, ActionItemsTextarea, RecommendResources | `ROLE_MENTOR` |
| `MEN-13` | `capstones` | **Capstone Project Defenses:** Scheduled final project defenses | DefenseScheduleList, CapstonePRLink, FinalPassFailModal | `ROLE_MENTOR` |
| `MEN-14` | `curriculum/resources` | **Faculty Curriculum Materials:** Slide decks, solutions, notes | ResourceDownloadGrid, InstructorNotesAccordion | `ROLE_MENTOR` |
| `MEN-15` | `forum` | **Instructor Q&A Forum:** Questions flagged for instructor response | UnansweredQuestionsFilter, CodeSnippetEditor, MarkAsAnswer | `ROLE_MENTOR` |
| `MEN-16` | `earnings` | **Mentor Compensation Wallet:** Hours taught, 1:1 session payouts | EarningsSummaryCard, MonthlyPayoutHistory, BankDetailsForm | `ROLE_MENTOR` |
| `MEN-17` | `ratings` | **Student Feedback & NPS:** Student evaluations and review quotes | AverageRatingStars, CategoryBreakdown, AnonymousReviewQuotes | `ROLE_MENTOR` |
| `MEN-18` | `profile` | **Public Mentor Profile:** Edit bio, specializations, company role | ProfileEditor, CompanyLogoSelector, SocialLinksForm | `ROLE_MENTOR` |
| `MEN-19` | `settings` | **Mentor Settings:** Timezone, booking lead times, notifications | TimezoneDropdown, BufferTimeSelector, EmailDigestToggles | `ROLE_MENTOR` |
| `MEN-20` | `contracts` | **Faculty Agreement & Tax Forms:** Signed agreements, 1099/W-9 | DocumentViewer, SignNewAgreementButton, TaxDocDownload | `ROLE_MENTOR` |

---

## 5. Academy Administration Portal — 35 Screens

```text
/portal/admin/
```

| Screen ID | Sub-Route | Screen Name & Purpose | Primary Components | Auth Level |
| :--- | :--- | :--- | :--- | :--- |
| `ADM-01` | `dashboard` | **Academy Operations Overview:** Revenue, enrollments, completions | MetricStatsGrid, EnrollmentLineChart, ChurnRiskHeatmap | `ROLE_ACADEMY_ADMIN` |
| `ADM-02` | `admissions/pipeline` | **Admissions CRM Pipeline:** Kanban board of prospective leads | KanbanBoard, DragDropLeadCards, QuickFilterPills | `ROLE_ACADEMY_ADMIN` |
| `ADM-03` | `admissions/leads/:id` | **Lead Details & Activity:** Call logs, notes, counseling records | ActivityTimeline, LogCallModal, IssueAdmissionOfferModal | `ROLE_ACADEMY_ADMIN` |
| `ADM-04` | `admissions/counselors` | **Counselor Team Performance:** Inquiries converted per counselor | CounselorLeaderboard, ConversionRateMetrics | `ROLE_ACADEMY_ADMIN` |
| `ADM-05` | `programs` | **Programs Management:** List of programs with active batch count | ProgramTable, NewProgramButton, EditPricingModal | `ROLE_ACADEMY_ADMIN` |
| `ADM-06` | `programs/builder` | **Program Curriculum Builder:** Drag-and-drop course sequence | SequenceBuilder, PrerequisiteLinker, SaveDraftButton | `ROLE_ACADEMY_ADMIN` |
| `ADM-07` | `courses` | **Courses Catalog Manager:** Create, update, or archive skill courses | CourseTable, CategoryDropdownFilter, StatusBadge | `ROLE_ACADEMY_ADMIN` |
| `ADM-08` | `courses/:id/curriculum` | **Course Module & Lesson Editor:** Rich text, video upload, labs | ModuleTree, LessonTypeSelector, VideoUploaderDropzone | `ROLE_ACADEMY_ADMIN` |
| `ADM-09` | `batches` | **Cohort Batch Operations:** Create batches, assign lead mentors | BatchListTable, CreateBatchModal, EnrollmentLimitSlider | `ROLE_ACADEMY_ADMIN` |
| `ADM-10` | `batches/:id/schedule` | **Batch Timetable & Room Planner:** Schedule lecture sessions | TimetableGrid, RoomConflictDetector, SaveScheduleButton | `ROLE_ACADEMY_ADMIN` |
| `ADM-11` | `batches/:id/students` | **Batch Student Roster:** Enrolled students, attendance, status | StudentRosterTable, ChangeBatchModal, DropStudentModal | `ROLE_ACADEMY_ADMIN` |
| `ADM-12` | `students` | **Global Student Directory:** Search all students across academy | SearchInput, FilterByStatus, StudentDetailDrawer | `ROLE_ACADEMY_ADMIN` |
| `ADM-13` | `students/:id` | **Comprehensive Student Record:** Full academic, financial, attendance history | TabbedProfile, AttendanceLog, GradebookSummary, FinancialLedger | `ROLE_ACADEMY_ADMIN` |
| `ADM-14` | `mentors` | **Faculty & Mentor Directory:** Manage instructors and applicants | MentorTable, ApplicationReviewModal, AssignBatchDrawer | `ROLE_ACADEMY_ADMIN` |
| `ADM-15` | `mentors/applications` | **Mentor Application Review:** Screen resumes, schedule tech interviews | ApplicantTable, ResumePreviewer, ApproveApplicantButton | `ROLE_ACADEMY_ADMIN` |
| `ADM-16` | `exams` | **Exam & Question Bank Manager:** Manage MCQ and coding questions | QuestionBankTable, QuestionEditorModal, TagSelector | `ROLE_ACADEMY_ADMIN` |
| `ADM-17` | `exams/proctoring-logs` | **Proctoring Incident Center:** Review flagged anti-cheat incidents | IncidentTable, WebcamSnapshotViewer, ConfirmCheatFlag | `ROLE_ACADEMY_ADMIN` |
| `ADM-18` | `certificates` | **Credential Issuance Center:** Approve, generate, or revoke certs | CertificateTable, BatchIssueCertificatesButton, RevokeModal | `ROLE_ACADEMY_ADMIN` |
| `ADM-19` | `certificates/templates` | **Certificate Template Designer:** Custom SVG/Canvas credential designs | CanvasTemplateEditor, FieldPlaceholderTags, PreviewPDF | `ROLE_ACADEMY_ADMIN` |
| `ADM-20` | `finance/overview` | **Financial Operations Dashboard:** Monthly revenue, AR, refunds | RevenueCards, AccountsReceivableTable, GatewayStatus | `ROLE_ACADEMY_ADMIN` |
| `ADM-21` | `finance/invoices` | **Invoice Ledger:** Manage all student invoices and status | InvoiceTable, GenerateInvoiceModal, MarkPaidModal | `ROLE_ACADEMY_ADMIN` |
| `ADM-22` | `finance/scholarships` | **Scholarship & Financial Aid:** Review fee discount applications | ScholarshipApplicationList, ApproveDiscountModal | `ROLE_ACADEMY_ADMIN` |
| `ADM-23` | `finance/refunds` | **Refund Requests Queue:** Process refund requests per policy | RefundQueueTable, PolicyComplianceCheck, ExecuteRefund | `ROLE_ACADEMY_ADMIN` |
| `ADM-24` | `placement/partners` | **Hiring Partner CRM:** 500+ employer accounts and drive history | PartnerTable, AddPartnerModal, ActiveJobOpeningsCount | `ROLE_ACADEMY_ADMIN` |
| `ADM-25` | `placement/drives` | **Campus Placement Drives:** Schedule interview days with partners | DriveCalendar, ShortlistedStudentTable, OfferLogger | `ROLE_ACADEMY_ADMIN` |
| `ADM-26` | `placement/outcomes` | **Placement Analytics & Outcomes:** Average salary, time-to-hire | SalaryHistogram, PlacementPercentGauge, ExportReport | `ROLE_ACADEMY_ADMIN` |
| `ADM-27` | `communications/announcements`| **Broadcast Announcements:** Send mass email/SMS/in-app notices | RichTextComposer, AudienceSelector, ScheduleBroadcast | `ROLE_ACADEMY_ADMIN` |
| `ADM-28` | `communications/email-templates`| **Automated Email Templates:** Edit transactional email copy | TemplateEditor, VariablePills, SendTestEmailModal | `ROLE_ACADEMY_ADMIN` |
| `ADM-29` | `analytics/academics` | **Academic Performance Analytics:** Completion rates, drop-offs | RetentionFunnel, ModuleDifficultyHeatmap, ExportCSV | `ROLE_ACADEMY_ADMIN` |
| `ADM-30` | `analytics/mentors` | **Faculty Performance Metrics:** Average ratings, feedback response time | RatingBarChart, TurnaroundMetricsTable | `ROLE_ACADEMY_ADMIN` |
| `ADM-31` | `staff/users` | **Academy Staff Accounts:** Manage counselors, TAs, admins | StaffTable, RoleSelectorDropdown, DeactivateUserModal | `ROLE_ACADEMY_ADMIN` |
| `ADM-32` | `settings/branding` | **Academy Branding Settings:** Logo, favicon, primary theme colors | ImageDropzones, ColorHexPicker, SaveBrandingButton | `ROLE_ACADEMY_ADMIN` |
| `ADM-33` | `settings/integrations` | **Third-Party Integrations:** Stripe, LiveKit, SendGrid, Zoom API keys | KeyInputForm, TestConnectionButton, WebhookUrlDisplay | `ROLE_ACADEMY_ADMIN` |
| `ADM-34` | `settings/policies` | **Terms & Refund Policies:** Manage enrollment terms and agreements | MarkdownLegalEditor, RequireReSignToggle | `ROLE_ACADEMY_ADMIN` |
| `ADM-35` | `audit-logs` | **Operational Audit Trail:** Searchable activity history | AuditLogTable, JsonDiffDrawer, ExportAuditLog | `ROLE_ACADEMY_ADMIN` |

---

## 6. Corporate Organization Admin Portal — 25 Screens

```text
/portal/corporate/
```

| Screen ID | Sub-Route | Screen Name & Purpose | Primary Components | Auth Level |
| :--- | :--- | :--- | :--- | :--- |
| `COR-01` | `dashboard` | **Enterprise L&D Overview:** Total seats, active learners, compliance % | ActiveSeatsGauge, ComplianceRateMeter, HoursLearnedChart | `ROLE_CORP_ADMIN` |
| `COR-02` | `departments` | **Department Hierarchy:** Manage tree structure of corporate units | TreeOrgChart, AddDepartmentModal, MoveEmployeesModal | `ROLE_CORP_ADMIN` |
| `COR-03` | `employees` | **Employee Roster:** List of enrolled staff, assigned tracks, progress | EmployeeTable, InviteStaffModal, RevokeSeatModal | `ROLE_CORP_ADMIN` |
| `COR-04` | `employees/bulk-upload` | **Bulk Employee Import:** Upload CSV or sync via SCIM / Okta | CSVUploadDropzone, ColumnMappingModal, DryRunResults | `ROLE_CORP_ADMIN` |
| `COR-05` | `employees/:id` | **Employee Learning Record:** Courses, certifications, compliance status | TabbedRecord, CourseProgressList, CertBadgesGrid | `ROLE_CORP_ADMIN` |
| `COR-06` | `learning-tracks` | **Assigned Learning Tracks:** Manage technical tracks for company | TrackCardGrid, CreateCustomTrackButton, CourseSelector | `ROLE_CORP_ADMIN` |
| `COR-07` | `learning-tracks/assign` | **Bulk Track Assignment Engine:** Assign tracks to departments/roles | TargetSelector, DeadlinePicker, MandatoryToggle | `ROLE_CORP_ADMIN` |
| `COR-08` | `compliance` | **Compliance & Governance:** Mandatory regulatory training tracker | ComplianceStatusTable, OverdueAlertsBanner, ExportAuditPDF | `ROLE_CORP_ADMIN` |
| `COR-09` | `compliance/reminders` | **Automated Reminder Cadence:** Configure reminder frequency | ReminderScheduleForm, CustomNotificationCopy | `ROLE_CORP_ADMIN` |
| `COR-10` | `skills-matrix` | **Enterprise Skill Competency Matrix:** Heatmap of team skills | SkillHeatmapMatrix, BenchmarkTargetPill, GapAnalysis | `ROLE_CORP_ADMIN` |
| `COR-11` | `assessments/results` | **Technical Assessment Results:** Employee scores in coding tests | ScoreTable, TeamComparisonChart, DownloadExcel | `ROLE_CORP_ADMIN` |
| `COR-12` | `approvals` | **Employee Course Requests:** Approve elective upskilling requests | PendingRequestsTable, ApproveButton, RejectModal | `ROLE_CORP_ADMIN` |
| `COR-13` | `certificates` | **Corporate Certifications Earned:** Team credentials repository | CredentialTable, VerificationStatusPill, BulkDownloadPDF | `ROLE_CORP_ADMIN` |
| `COR-14` | `licenses` | **Seats & License Utilization:** Active vs unassigned licenses | LicenseProgressBar, PurchaseMoreSeatsModal, UsageHistory | `ROLE_CORP_ADMIN` |
| `COR-15` | `billing/subscription` | **Enterprise Subscription:** Contract tier, renewal date, POs | ContractSummaryCard, DownloadPastInvoices, PaymentMethod | `ROLE_CORP_ADMIN` |
| `COR-16` | `analytics/engagement` | **Employee Engagement Analytics:** Weekly active users, drop-offs | ActiveUsersChart, TimeOfDayHeatmap, TopCoursesList | `ROLE_CORP_ADMIN` |
| `COR-17` | `analytics/departments` | **Department Comparison:** Benchmarking departments against each other | DepartmentBarChart, CompletionLeaderboardTable | `ROLE_CORP_ADMIN` |
| `COR-18` | `reports/scheduled` | **Automated Report Digests:** Scheduled weekly/monthly PDF reports | ScheduledReportsList, AddNewScheduleModal, RecipientEmails | `ROLE_CORP_ADMIN` |
| `COR-19` | `managers` | **Department Managers:** Assign manager permissions over teams | ManagerTable, AssignTeamsModal, AccessScopeSelector | `ROLE_CORP_ADMIN` |
| `COR-20` | `sso-configuration` | **SAML 2.0 / SSO Setup:** Configure Okta, Azure AD, or PingIdentity | SAMLMetadataUploader, EntityIdInput, TestSSOButton | `ROLE_CORP_ADMIN` |
| `COR-21` | `scim-provisioning` | **SCIM Directory Sync:** Automated employee provisioning | SCIMTokenGenerator, EndpointUrlDisplay, SyncStatusLog | `ROLE_CORP_ADMIN` |
| `COR-22` | `branding` | **White-Label Portal Theme:** Custom corporate logo & welcome banner | LogoUploadForm, BannerImageDropzone, PreviewPortal | `ROLE_CORP_ADMIN` |
| `COR-23` | `notifications` | **Organization Broadcasts:** Send internal alerts to employees | BroadcastComposer, TargetDepartmentCheckboxes | `ROLE_CORP_ADMIN` |
| `COR-24` | `settings` | **General Corporate Settings:** Company name, domain whitelists | CompanyDetailsForm, EmailDomainTags, TimezoneDropdown | `ROLE_CORP_ADMIN` |
| `COR-25` | `audit-logs` | **Corporate Activity Logs:** Administrative changes & seat reallocations | AuditLogTable, UserFilter, ExportCSVButton | `ROLE_CORP_ADMIN` |

---

## 7. Corporate Employee L&D Portal — 20 Screens

```text
/portal/employee/
```

| Screen ID | Sub-Route | Screen Name & Purpose | Primary Components | Auth Level |
| :--- | :--- | :--- | :--- | :--- |
| `EMP-01` | `dashboard` | **Employee Learning Home:** Assigned tracks, mandatory compliance | ComplianceAlertBanner, NextLessonCard, ProgressMeters | `ROLE_EMPLOYEE` |
| `EMP-02` | `my-tracks` | **My Learning Tracks:** Mandatory and elective tracks in progress | TrackCardGrid, DeadlineCountdownBadges, ResumeButton | `ROLE_EMPLOYEE` |
| `EMP-03` | `tracks/:id/player` | **Curriculum Video Player:** Modular lessons with practical labs | VideoStage, LessonOutlineDrawer, NotesTab, DownloadAssets | `ROLE_EMPLOYEE` |
| `EMP-04` | `tracks/:id/lab` | **Cloud Hands-On Sandbox:** Practical sandbox for enterprise tech | InteractiveCloudTerminal, ArchitectureDiagram, SubmitTask | `ROLE_EMPLOYEE` |
| `EMP-05` | `compliance` | **Mandatory Compliance Modules:** Annual security, data privacy | ComplianceChecklist, UrgentDeadlineIndicator, StartExam | `ROLE_EMPLOYEE` |
| `EMP-06` | `catalog` | **Elective Course Catalog:** Browse available company skill tracks | CourseCardGrid, SkillFilters, RequestManagerApprovalButton | `ROLE_EMPLOYEE` |
| `EMP-07` | `catalog/:id/request` | **Request Elective Approval:** Submit justification note to manager | RequestFormModal, JustificationTextarea, SubmitRequest | `ROLE_EMPLOYEE` |
| `EMP-08` | `my-requests` | **Track Approval Status:** Status of requested elective courses | RequestHistoryTable, StatusPill (Pending/Approved) | `ROLE_EMPLOYEE` |
| `EMP-09` | `assessments` | **Skill Assessments & Tests:** Mandatory skill benchmark quizzes | AssessmentCardList, PassingCriteriaPill, StartTestButton | `ROLE_EMPLOYEE` |
| `EMP-10` | `assessments/:id/take` | **Take Competency Assessment:** Timed technical evaluation | TimedQuizStage, QuestionNavigator, FinishAssessmentModal | `ROLE_EMPLOYEE` |
| `EMP-11` | `skills-radar` | **Personal Skill Benchmark:** Visual radar comparing to role benchmark | RadarChart, StrengthAreasList, RecommendedCoursesList | `ROLE_EMPLOYEE` |
| `EMP-12` | `credentials` | **My Corporate Certifications:** Verified badges and credentials | BadgeGallery, DownloadPDFCertificate, ShareOnInternalProfile | `ROLE_EMPLOYEE` |
| `EMP-13` | `learning-history` | **Completed Training Transcript:** Log of completed training hours | HistoryTable, HoursCompletedSum, ExportPDFTranscript | `ROLE_EMPLOYEE` |
| `EMP-14` | `live-workshops` | **Corporate Live Workshops:** Live internal company training sessions | WorkshopCalendar, RegisterButton, JoinLiveStream | `ROLE_EMPLOYEE` |
| `EMP-15` | `community` | **Internal Knowledge Hub:** Ask questions and share solutions | InternalForumFeed, PostQuestionModal, UpvoteButton | `ROLE_EMPLOYEE` |
| `EMP-16` | `ai-coach` | **Enterprise AI Skills Coach:** Get instant answers on tech stacks | AIChatInterface, PromptLibrary, SyntaxHighlighting | `ROLE_EMPLOYEE` |
| `EMP-17` | `bookmarks` | **Saved Lessons & Job Aids:** Quick access to saved cheat sheets | BookmarkedItemsGrid, RemoveBookmarkTrigger | `ROLE_EMPLOYEE` |
| `EMP-18` | `profile` | **Employee Profile:** Role, department, manager, completed skills | ProfileSummaryCard, DepartmentPill, SkillBadges | `ROLE_EMPLOYEE` |
| `EMP-19` | `notifications` | **Learning Alerts:** Approvals, compliance deadlines, new courses | NotificationFeed, MarkAllReadButton | `ROLE_EMPLOYEE` |
| `EMP-20` | `settings` | **Preferences:** Notification frequency, language, playback speed | PreferenceToggles, LanguageDropdown | `ROLE_EMPLOYEE` |

---

## 8. Parent & Guardian Portal — 10 Screens

```text
/portal/guardian/
```

| Screen ID | Sub-Route | Screen Name & Purpose | Primary Components | Auth Level |
| :--- | :--- | :--- | :--- | :--- |
| `GUA-01` | `dashboard` | **Guardian Overview:** Linked student's attendance & grades | StudentSummaryCard, OverallAttendanceGauge, CurrentGPA | `ROLE_GUARDIAN` |
| `GUA-02` | `attendance` | **Attendance Record:** Live session attendance percentage & absences | CalendarAttendanceView, AbsenceAlerts, ExportAttendance | `ROLE_GUARDIAN` |
| `GUA-03` | `academic-progress`| **Curriculum Progress:** Completed modules, upcoming exam dates | ModuleProgressBars, UpcomingDeadlinesList | `ROLE_GUARDIAN` |
| `GUA-04` | `gradebook` | **Exam & Assignment Grades:** Scores across quizzes and capstones | GradeTable, PassingThresholdBadges, MentorFeedbackView | `ROLE_GUARDIAN` |
| `GUA-05` | `mentor-notes` | **Faculty Remarks:** Feedback notes submitted by lead mentor | NotesTimeline, PerformanceRatingPill | `ROLE_GUARDIAN` |
| `GUA-06` | `counselor-chat` | **Direct Counselor Messaging:** Chat with assigned academic advisor | CounselorBioCard, ChatMessageWindow, SendMessageInput | `ROLE_GUARDIAN` |
| `GUA-07` | `finance/installments`| **Fee Installments:** Upcoming tuition dues, invoices, receipts | InstallmentTable, PayNowButton, DownloadReceiptPDF | `ROLE_GUARDIAN` |
| `GUA-08` | `finance/pay` | **Tuition Payment Checkout:** Secure payment gateway integration | StripeCardForm, PaymentBreakdownSummary, ConfirmPayment | `ROLE_GUARDIAN` |
| `GUA-09` | `credentials` | **Verified Certificates:** View earned credentials and transcripts | CertificatePreview, OfficialTranscriptDownload | `ROLE_GUARDIAN` |
| `GUA-10` | `settings` | **Guardian Settings:** Emergency contact info, SMS alert settings | ContactForm, SMSAlertToggles, SavePreferences | `ROLE_GUARDIAN` |

---

## 9. Platform Super Admin Operations Portal — 25 Screens

```text
/portal/platform/
```

| Screen ID | Sub-Route | Screen Name & Purpose | Primary Components | Auth Level |
| :--- | :--- | :--- | :--- | :--- |
| `SUP-01` | `dashboard` | **Global Fleet Overview:** Total tenants, active users, MRR, uptime | FleetHealthMap, MRRGauge, GlobalActiveUsersChart | `ROLE_SUPER_ADMIN` |
| `SUP-02` | `tenants` | **Tenant Registry:** Manage all enterprise and academy tenants | TenantTable, ProvisionNewTenantButton, SuspendTenantModal | `ROLE_SUPER_ADMIN` |
| `SUP-03` | `tenants/provision` | **Provision New Tenant:** Setup tenant slug, domain, plan, DB schema | ProvisioningWizard, DomainMappingInput, AdminAccountForm | `ROLE_SUPER_ADMIN` |
| `SUP-04` | `tenants/:id` | **Tenant Inspector:** Inspect single tenant metrics, users, storage | TenantMetricsCards, DatabaseIsolationBadge, QuotaSliders | `ROLE_SUPER_ADMIN` |
| `SUP-05` | `subscriptions` | **SaaS Subscription Plans:** Manage pricing tiers, feature packages | PricingPlanCards, FeatureMatrixEditor, QuotaInputs | `ROLE_SUPER_ADMIN` |
| `SUP-06` | `billing/invoices` | **Platform Invoicing:** B2B SaaS invoices issued to corporate tenants | SaaSInvoiceTable, PaymentStatusFilter, RetryPaymentModal | `ROLE_SUPER_ADMIN` |
| `SUP-07` | `telemetry/api` | **API Gateway Health:** Request throughput, P95/P99 latency, errors | LiveLatencyLineChart, ErrorRateGauge, RouteBreakdownTable | `ROLE_SUPER_ADMIN` |
| `SUP-08` | `telemetry/database` | **PostgreSQL Cluster Health:** Active connections, replication lag | ConnectionPoolBar, ReplicationLagMonitor, SlowQueryTable | `ROLE_SUPER_ADMIN` |
| `SUP-09` | `telemetry/webrtc` | **LiveKit Media SFU Clusters:** Active rooms, participants, bitrate | SFUNodeMap, ActiveRoomsGrid, BandwidthThroughputChart | `ROLE_SUPER_ADMIN` |
| `SUP-10` | `telemetry/queues` | **Job Queue Depth:** RabbitMQ / Kafka message queues, dead-letters | QueueDepthGauges, FailedJobsTable, RetryAllJobsButton | `ROLE_SUPER_ADMIN` |
| `SUP-11` | `ai-telemetry` | **AI Cluster Telemetry:** Token usage, cost per tenant, latency | TokenConsumptionChart, LLMCostEstimator, CacheHitRatio | `ROLE_SUPER_ADMIN` |
| `SUP-12` | `feature-flags` | **Global Feature Flags:** Toggle features per tenant / progressive rollouts | FeatureFlagTable, ToggleSwitches, RolloutPercentageSlider | `ROLE_SUPER_ADMIN` |
| `SUP-13` | `security/waf` | **Security WAF & DDoS:** Blocked IP addresses, rate limit triggers | ThreatWorldMap, BlocklistTable, AddIPBlockModal | `ROLE_SUPER_ADMIN` |
| `SUP-14` | `security/audit-trail` | **Global Security Audit Stream:** Immutable event log across fleet | RealtimeLogStream, SeverityFilter, DownloadAuditArchive | `ROLE_SUPER_ADMIN` |
| `SUP-15` | `security/vulnerabilities`| **Vulnerability & Compliance:** Automated dependency scans, CVEs | CVEAlertCards, SeverityPills, PatchStatusTracker | `ROLE_SUPER_ADMIN` |
| `SUP-16` | `users` | **Global User Registry:** Cross-tenant user lookup & impersonation | GlobalUserSearch, ImpersonateSessionButton, BanUserModal | `ROLE_SUPER_ADMIN` |
| `SUP-17` | `storage/s3` | **Object Storage Metrics:** Total S3 storage, video GBs, bandwidth | StoragePieChart, BucketListTable, CleanupOrphanedFiles | `ROLE_SUPER_ADMIN` |
| `SUP-18` | `integrations` | **Global Third-Party Credentials:** Stripe Connect, SendGrid, Twilio | IntegrationConfigCards, GlobalWebhookDispatcher | `ROLE_SUPER_ADMIN` |
| `SUP-19` | `notifications/system` | **System Maintenance Notices:** Broadcast downtime banners to users | MaintenanceNoticeComposer, TargetTenantFilter, ActiveBanner | `ROLE_SUPER_ADMIN` |
| `SUP-20` | `backups` | **Database Snapshots & DR:** Verify daily backups, point-in-time recovery | BackupListTable, TriggerManualBackup, RestoreDrillModal | `ROLE_SUPER_ADMIN` |
| `SUP-21` | `sandbox-clusters` | **Judge0 / MicroVM Sandboxes:** Code execution container pool health | ContainerPoolGauges, ActiveExecutionCounter, ResetCluster | `ROLE_SUPER_ADMIN` |
| `SUP-22` | `localization/i18n` | **Global Translations & Languages:** Multi-lingual dictionary editor | TranslationKeyTable, LanguageSelector, ExportLanguagePacks | `ROLE_SUPER_ADMIN` |
| `SUP-23` | `platform-admins` | **Super Admin Access Governance:** Manage platform administrator users | AdminUserTable, AddSuperAdminModal, RequireMFAEnforcement | `ROLE_SUPER_ADMIN` |
| `SUP-24` | `settings` | **Platform Global Constants:** Session timeouts, default storage limits | GlobalConfigForm, SavePlatformSettingsButton | `ROLE_SUPER_ADMIN` |
| `SUP-25` | `system/logs` | **Aggregated Log Viewer:** Search logs across Spring Boot & FastAPI | LogViewerTerminal, LogLevelFilter, RegexSearchInput | `ROLE_SUPER_ADMIN` |

---

## 10. Native Mobile App Screen Inventory (Android & iOS) — 42 Screens

```text
Cross-Platform Flutter + Dart App (Android & iOS)
```

| Screen ID | Mobile Screen Title | Screen Purpose & Key Components |
| :--- | :--- | :--- |
| `MOB-01` | **Splash / Onboarding Screen** | App brand launch, 3 swipeable value propositions, "Get Started" / "Log In" buttons. |
| `MOB-02` | **Login Screen** | Email/Password login, Biometric Auth trigger (FaceID / Fingerprint), "Use Demo" button. |
| `MOB-03` | **Enterprise SSO Screen** | Input corporate company domain; redirects to Okta / Azure AD WebView login. |
| `MOB-04` | **Forgot Password Screen** | Email input for password reset link with verification code timer. |
| `MOB-05` | **Home / Learn Tab** | Bottom-bar Tab 1: Current lesson progress bar, upcoming live session pill, streak flame icon. |
| `MOB-06` | **My Courses List** | Filterable list of enrolled courses with progress percentages and offline downloaded indicators. |
| `MOB-07` | **Course Curriculum Overview** | Accordion list of course modules, lock/unlock indicators, lesson duration pills. |
| `MOB-08` | **Mobile Video Lesson Player** | Native video player with fullscreen orientation toggle, speed selector, picture-in-picture. |
| `MOB-09` | **Audio-Only Mode Player** | Low-bandwidth mode for listening to lecture audio on commutes with lock-screen controls. |
| `MOB-10` | **Lesson Notes & Reading** | Markdown lesson reading view with syntax-highlighted code snippets and bookmark button. |
| `MOB-11` | **Mobile Coding Quiz** | Quick multiple choice questions with immediate explanation feedback. |
| `MOB-12` | **Offline Downloads Manager** | Manage downloaded video lessons, storage space indicator, "Delete All Downloads" button. |
| `MOB-13` | **Live Classroom Tab** | Bottom-bar Tab 2: Calendar of scheduled live sessions with countdown clocks and "Join" button. |
| `MOB-14` | **Live Video Room** | Native LiveKit WebRTC video stage, speaker grid, hand-raising button, chat overlay drawer. |
| `MOB-15` | **Live Classroom In-Call Chat** | Drawer showing real-time chat messages, emoji reactions, and poll popups. |
| `MOB-16` | **Recorded Classrooms Library** | Archive of past recorded live sessions with search and download for offline viewing. |
| `MOB-17` | **Assignments Tab** | List of pending assignments, due dates, submission statuses, and mentor grades. |
| `MOB-18` | **Assignment Submission Sheet** | Bottom sheet allowing GitHub repository URL entry or camera document upload. |
| `MOB-19` | **Assignment Mentor Review** | View mentor feedback notes, score breakdown, and listen to mentor audio review clips. |
| `MOB-20` | **Exams Overview** | List of upcoming quizzes and comprehensive exams with proctoring requirements. |
| `MOB-21` | **Exam Taking Screen** | Mobile-optimized exam interface with countdown timer and question navigation carousel. |
| `MOB-22` | **Exam Score Breakdown** | Visual score card with passing badge, percentage, and topic-by-topic strengths. |
| `MOB-23` | **Mentorship Tab** | Bottom-bar Tab 3: Mentor profile, remaining 1:1 session credits, "Book Session" button. |
| `MOB-24` | **1:1 Slot Picker Screen** | Interactive calendar to choose available mentor time slot and select discussion topic. |
| `MOB-25` | **1:1 Video Consultation Room** | Private 1:1 video call with mentor using front camera with in-call notes. |
| `MOB-26` | **Post-Session Action Items** | Checklist of action items and code tasks assigned by mentor during 1:1 session. |
| `MOB-27` | **AI Tutor Chat Screen** | Dedicated chat interface with AI course assistant, suggested prompts, voice-to-text input. |
| `MOB-28` | **Community Forums Tab** | Bottom-bar Tab 4: Cohort discussion channels, recent thread feed, search bar. |
| `MOB-29` | **Community Thread Detail** | Thread discussion with replies, code syntax blocks, and reply input bar. |
| `MOB-30` | **Create New Thread Screen** | Title input, channel picker, rich markdown body, code snippet insert helper. |
| `MOB-31` | **Direct Messages List** | List of 1:1 direct conversations with peers, TAs, and mentors with unread badges. |
| `MOB-32` | **Direct Chat Conversation** | 1:1 real-time messaging screen with file sharing and read receipts. |
| `MOB-33` | **My Credentials & Badges** | Gallery of earned digital certificates with verified badge overlays. |
| `MOB-34` | **Certificate Detail Sheet** | High-res certificate preview with "Save Image", "Download PDF", "Share to LinkedIn". |
| `MOB-35` | **Career Drives & Jobs** | Partner job listings, application status tracker, resume download button. |
| `MOB-36` | **Notifications Screen** | Push notification history list with filter tabs (Classes, Mentors, Grades, System). |
| `MOB-37` | **Profile & Account Tab** | Bottom-bar Tab 5: Profile picture, GitHub/LinkedIn links, enrolled batch, streaks. |
| `MOB-38` | **Edit Profile Screen** | Change avatar, update phone, bio, and portfolio link. |
| `MOB-39` | **Security Settings Screen** | Enable FaceID / Biometric login, change password, view active devices. |
| `MOB-40` | **Download & Storage Settings** | Toggle "Download over Wi-Fi only", choose video download quality (720p / 1080p). |
| `MOB-41` | **Tuition & Installments Screen** | View fee installments, receipt downloads, and mobile payment gateway checkout. |
| `MOB-42` | **Help & Support Screen** | FAQ accordion, live chat with admissions counselor, report app bug form. |
