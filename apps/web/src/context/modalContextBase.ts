import { createContext, useContext } from 'react'

export interface ModalContextType {
  openEnrollModal: (programId?: string) => void
  openDemoModal: () => void
  openLoginModal: () => void
  openCertificateModal: () => void
}

export const ModalContext = createContext<ModalContextType | undefined>(undefined)

export const useModals = (): ModalContextType => {
  const context = useContext(ModalContext)
  if (!context) {
    throw new Error('useModals must be used within a ModalProvider')
  }
  return context
}
