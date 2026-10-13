/**
 * Nexarats Academy - Financial Operations, Payments & Invoices API Contracts
 * Enterprise Type Definitions for Web (React) and Mobile (Flutter/Dart compatible)
 */

export type PaymentProvider = 'STRIPE' | 'RAZORPAY' | 'TEST_GATEWAY'

export type PaymentOrderStatus =
  | 'CREATED'
  | 'PENDING'
  | 'PAID'
  | 'FAILED'
  | 'CANCELLED'
  | 'REFUNDED'

export type InvoiceStatus =
  | 'DRAFT'
  | 'ISSUED'
  | 'PAID'
  | 'VOID'
  | 'REFUNDED'

export interface CreatePaymentOrderRequest {
  applicationId?: string
  offerId?: string
  cohortId?: string
  reservationId?: string
  currency?: string
  provider?: PaymentProvider
  idempotencyKey?: string
}

export interface VerifyPaymentRequest {
  orderNumber: string
  providerPaymentId?: string
  signature: string
}

export interface PaymentOrderDto {
  id: string
  orderNumber: string
  amountCents: number
  currency: string
  provider: PaymentProvider
  providerOrderId?: string | null
  clientSecret?: string | null
  checkoutUrl?: string | null
  status: PaymentOrderStatus
  idempotencyKey?: string | null
  paidAt?: string | null
}

export interface InvoiceDto {
  id: string
  invoiceNumber: string
  learnerUserId: string
  paymentOrderId?: string | null
  enrollmentId?: string | null
  subtotalCents: number
  taxCents: number
  totalCents: number
  currency: string
  status: InvoiceStatus
  issuedAt: string
  paidAt?: string | null
  pdfReceiptUrl?: string | null
}
