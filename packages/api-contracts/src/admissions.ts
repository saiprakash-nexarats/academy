/**
 * Nexarats Academy - Admissions, Applications & Enrollment API Contracts
 * Enterprise Type Definitions for Web (React) and Mobile (Flutter/Dart compatible)
 */

export type ApplicationStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'UNDER_REVIEW'
  | 'APPROVED'
  | 'REJECTED'
  | 'WITHDRAWN'

export type AdmissionOfferStatus =
  | 'ISSUED'
  | 'ACCEPTED'
  | 'DECLINED'
  | 'EXPIRED'

export type EnrollmentStatus =
  | 'PENDING'
  | 'ENROLLED'
  | 'ACTIVE'
  | 'COMPLETED'
  | 'WITHDRAWN'
  | 'SUSPENDED'

export type CohortStatus =
  | 'UPCOMING'
  | 'OPEN_FOR_ENROLLMENT'
  | 'FULL'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED'

export type SeatReservationStatus =
  | 'RESERVED'
  | 'CONFIRMED'
  | 'EXPIRED'
  | 'CANCELLED'

export interface SubmitApplicationRequest {
  fullName: string
  email: string
  phone: string
  targetProgramOrCourse?: string
  experienceLevel?: string
  statementOfPurpose?: string
}

export interface ApplicationSummaryDto {
  id: string
  applicationNumber: string
  fullName: string
  email: string
  phone: string
  programId?: string | null
  courseId?: string | null
  cohortId?: string | null
  experienceLevel: string
  status: ApplicationStatus
  submittedAt: string
}

export interface ReviewApplicationRequest {
  decision: 'APPROVED' | 'REJECTED'
  reviewNotes?: string
  rejectionReason?: string
  targetCohortId?: string
  customTuitionCents?: number
  currency?: string
}

export interface AdmissionOfferDto {
  id: string
  offerNumber: string
  applicationId: string
  cohortId: string
  offeredAmountCents: number
  currency: string
  status: AdmissionOfferStatus
  validUntil: string
  issuedAt: string
}

export interface SeatReservationDto {
  id: string
  cohortId: string
  reservationToken: string
  status: SeatReservationStatus
  expiresAt: string
}

export interface CohortDto {
  id: string
  cohortCode: string
  name: string
  programId?: string | null
  courseId?: string | null
  startDate: string
  endDate?: string | null
  enrollmentEndDate: string
  capacity: number
  availableSeats: number
  status: CohortStatus
}

export interface EnrollmentDto {
  id: string
  enrollmentNumber: string
  learnerUserId: string
  programId?: string | null
  courseId?: string | null
  cohortId: string
  applicationId?: string | null
  status: EnrollmentStatus
  enrolledAt: string
}
