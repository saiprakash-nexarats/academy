import React, { useState } from 'react'
import {
  EnrollModal,
  DemoModal,
  LoginModal,
  CertificateModal,
} from '@/components/modals'
import { ModalContext } from './modalContextBase'

export const ModalProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [enrollOpen, setEnrollOpen] = useState(false)
  const [selectedProgram, setSelectedProgram] = useState<string | undefined>()
  const [demoOpen, setDemoOpen] = useState(false)
  const [loginOpen, setLoginOpen] = useState(false)
  const [certificateOpen, setCertificateOpen] = useState(false)

  const openEnrollModal = (programId?: string) => {
    setSelectedProgram(programId)
    setEnrollOpen(true)
  }

  const openDemoModal = () => setDemoOpen(true)
  const openLoginModal = () => setLoginOpen(true)
  const openCertificateModal = () => setCertificateOpen(true)

  return (
    <ModalContext.Provider
      value={{
        openEnrollModal,
        openDemoModal,
        openLoginModal,
        openCertificateModal,
      }}
    >
      {children}

      {/* Global Modals */}
      <EnrollModal
        isOpen={enrollOpen}
        onClose={() => setEnrollOpen(false)}
        defaultProgram={selectedProgram}
      />
      <DemoModal
        isOpen={demoOpen}
        onClose={() => setDemoOpen(false)}
      />
      <LoginModal
        isOpen={loginOpen}
        onClose={() => setLoginOpen(false)}
      />
      <CertificateModal
        isOpen={certificateOpen}
        onClose={() => setCertificateOpen(false)}
      />
    </ModalContext.Provider>
  )
}
