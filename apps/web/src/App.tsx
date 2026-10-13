import React, { useState } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MainLayout } from '@/layouts'
import {
  HomePage,
  ProgramsPage,
  ProgramDetailPage,
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
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 5 * 60 * 1000,
            refetchOnWindowFocus: false,
            retry: 1,
          },
        },
      })
  )

  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <ScrollToTop />
        <AuthProvider>
          <ModalProvider>
          <Routes>
            <Route path="/" element={<MainLayout />}>
              <Route index element={<HomePage />} />
              <Route path="programs" element={<ProgramsPage />} />
              <Route path="programs/:programId" element={<ProgramDetailPage />} />
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
  </QueryClientProvider>
  )
}

export default App
