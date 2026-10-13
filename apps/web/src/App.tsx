import React from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { MainLayout } from '@/layouts'
import {
  HomePage,
  ProgramsPage,
  CoursesPage,
  MentorsPage,
  SuccessStoriesPage,
  AboutPage,
  ContactPage,
  NotFoundPage,
} from '@/pages'
import { ModalProvider, AuthProvider } from '@/context'
import { ScrollToTop } from '@/components/common'

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <ScrollToTop />
      <AuthProvider>
        <ModalProvider>
          <Routes>
            <Route path="/" element={<MainLayout />}>
              <Route index element={<HomePage />} />
              <Route path="programs" element={<ProgramsPage />} />
              <Route path="programs/:programId" element={<ProgramsPage />} />
              <Route path="courses" element={<CoursesPage />} />
              <Route path="mentors" element={<MentorsPage />} />
              <Route path="success-stories" element={<SuccessStoriesPage />} />
              <Route path="about" element={<AboutPage />} />
              <Route path="contact" element={<ContactPage />} />
              <Route path="*" element={<NotFoundPage />} />
            </Route>
          </Routes>
        </ModalProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
