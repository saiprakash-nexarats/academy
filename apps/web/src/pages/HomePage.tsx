import React from 'react'
import {
  HeroSection,
  StatsStrip,
  PopularProgramsSection,
  JobAssuranceSection,
  WhyChooseSection,
  ExperienceSection,
  TestimonialsSection,
  CtaBannerSection,
} from '@/sections'

export const HomePage: React.FC = () => {
  return (
    <div className="pb-4 sm:pb-6 overflow-x-hidden">
      {/* 1. Hero Section */}
      <div id="hero" className="scroll-mt-24">
        <HeroSection />
      </div>

      {/* 2. Stats Strip */}
      <div id="stats" className="scroll-mt-24">
        <StatsStrip />
      </div>

      {/* 3. Main Content Container (Tight, Harmonious Spacing) */}
      <div className="academy-container space-y-6 sm:space-y-8 lg:space-y-10">
        {/* Popular Programs */}
        <div id="programs" className="scroll-mt-24">
          <PopularProgramsSection />
        </div>

        {/* 100% Job Assurance & Hiring Guarantee */}
        <div id="job-assurance" className="scroll-mt-24">
          <JobAssuranceSection />
        </div>

        {/* Why Choose Nexarats? */}
        <div id="why-choose" className="scroll-mt-24">
          <WhyChooseSection />
        </div>

        {/* The Nexarats Experience Platform */}
        <div id="experience" className="scroll-mt-24">
          <ExperienceSection />
        </div>

        {/* Testimonials (Student Success Stories) */}
        <div id="testimonials" className="scroll-mt-24">
          <TestimonialsSection />
        </div>

        {/* Bottom CTA Banner (Start Learning With Confidence) */}
        <div id="cta" className="scroll-mt-24">
          <CtaBannerSection />
        </div>
      </div>
    </div>
  )
}
