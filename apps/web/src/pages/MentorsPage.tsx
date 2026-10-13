import React, { useState } from 'react'
import {
  Star,
  Search,
  Filter,
  Sparkles,
  Clock,
  Building,
  CheckCircle2,
  X,
  ArrowRight,
  HelpCircle,
  Video,
  FileCode,
  Users,
} from 'lucide-react'
import { motion, AnimatePresence } from 'framer-motion'
import {
  MENTORS_DATA,
  MENTORSHIP_BENEFITS,
  MENTOR_FAQS,
  type MentorItemExtended,
} from '@/data'
import { MentorAvatar, CompanyIcon } from '@/components/common'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { useModals } from '@/context'

export const MentorsPage: React.FC = () => {
  const { openEnrollModal } = useModals()

  const [selectedDomain, setSelectedDomain] = useState<string>('all')
  const [searchQuery, setSearchQuery] = useState<string>('')
  const [selectedMentorForModal, setSelectedMentorForModal] = useState<MentorItemExtended | null>(null)
  const [selectedSlot, setSelectedSlot] = useState<string>('')
  const [selectedTopic, setSelectedTopic] = useState<string>('')
  const [bookingSubmitted, setBookingSubmitted] = useState<boolean>(false)
  const [bookingForm, setBookingForm] = useState({
    name: '',
    email: '',
    notes: '',
  })

  const domainTabs = [
    { id: 'all', label: 'All Mentors' },
    { id: 'Software Engineering', label: 'Software & Architecture' },
    { id: 'Artificial Intelligence', label: 'AI & Machine Learning' },
    { id: 'Design', label: 'UI/UX & Product Design' },
    { id: 'Cloud & DevOps', label: 'Cloud & DevOps' },
    { id: 'Marketing', label: 'Growth & Marketing' },
  ]

  const filteredMentors = MENTORS_DATA.filter((mentor) => {
    const matchesDomain =
      selectedDomain === 'all' || mentor.domain === selectedDomain

    const matchesSearch =
      mentor.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      mentor.company.toLowerCase().includes(searchQuery.toLowerCase()) ||
      mentor.role.toLowerCase().includes(searchQuery.toLowerCase()) ||
      mentor.bio.toLowerCase().includes(searchQuery.toLowerCase()) ||
      mentor.specialization.some((s) => s.toLowerCase().includes(searchQuery.toLowerCase()))

    return matchesDomain && matchesSearch
  })

  const handleOpenBooking = (mentor: MentorItemExtended) => {
    setSelectedMentorForModal(mentor)
    setSelectedSlot(mentor.upcomingSlots[0] || '')
    setSelectedTopic(mentor.featuredTopics[0] || '')
    setBookingSubmitted(false)
    setBookingForm({ name: '', email: '', notes: '' })
  }

  const handleConfirmBooking = (e: React.FormEvent) => {
    e.preventDefault()
    setBookingSubmitted(true)
  }

  return (
    <div className="py-10 sm:py-16">
      <div className="academy-container space-y-12 sm:space-y-16">
        {/* 1. Header Hero */}
        <div className="max-w-3xl space-y-4">
          <Badge variant="primary" icon={<Sparkles className="w-3.5 h-3.5 text-brand-600" />}>
            World-Class Mentorship Network
          </Badge>
          <h1 className="font-heading font-extrabold text-3xl sm:text-4xl lg:text-5xl text-slate-900 tracking-tight leading-tight">
            Learn 1-on-1 From Staff Engineers at <br className="hidden sm:inline" />
            <span className="text-brand-600">Leading Global Tech Companies</span>
          </h1>
          <p className="text-sm sm:text-base text-slate-600 leading-relaxed">
            Every Nexarats learner is matched with dedicated industry leaders for weekly personalized code reviews, live architecture critiques, mock system design defenses, and direct hiring referrals.
          </p>
        </div>

        {/* 2. Mentorship Benefits Value Strip */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {MENTORSHIP_BENEFITS.map((benefit, i) => (
            <div
              key={benefit.title}
              className="p-5 rounded-2xl bg-white border border-slate-200/90 shadow-sm flex flex-col justify-between"
            >
              <div className="space-y-2">
                <div className="w-9 h-9 rounded-xl bg-brand-50 text-brand-600 flex items-center justify-center font-bold text-sm">
                  {i === 0 && <Video className="w-4.5 h-4.5" />}
                  {i === 1 && <FileCode className="w-4.5 h-4.5" />}
                  {i === 2 && <Building className="w-4.5 h-4.5" />}
                  {i === 3 && <Users className="w-4.5 h-4.5" />}
                </div>
                <h3 className="font-heading font-bold text-sm text-slate-900">{benefit.title}</h3>
                <p className="text-xs text-slate-600 leading-relaxed">{benefit.description}</p>
              </div>
            </div>
          ))}
        </div>

        {/* 3. Filter & Search Controls */}
        <div className="space-y-4">
          <div className="flex flex-col md:flex-row items-center justify-between gap-4 p-4 rounded-2xl bg-white border border-slate-200/90 shadow-sm">
            {/* Domain Tabs */}
            <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar w-full md:w-auto pb-1 md:pb-0">
              <Filter className="w-4 h-4 text-slate-400 mr-1 shrink-0 hidden sm:inline" />
              {domainTabs.map((d) => (
                <button
                  key={d.id}
                  type="button"
                  onClick={() => setSelectedDomain(d.id)}
                  className={`px-3.5 py-2 rounded-xl text-xs font-bold transition-all shrink-0 cursor-pointer ${
                    selectedDomain === d.id
                      ? 'bg-brand-600 text-white shadow-sm'
                      : 'bg-slate-50 text-slate-600 hover:bg-slate-100 hover:text-slate-900'
                  }`}
                >
                  {d.label}
                </button>
              ))}
            </div>

            {/* Search Input */}
            <div className="relative w-full md:w-72">
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="text"
                placeholder="Search mentor, company, tech..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-9 pr-3.5 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-brand-500"
              />
            </div>
          </div>
        </div>

        {/* 4. Mentors Card Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {filteredMentors.map((mentor) => (
            <motion.div
              key={mentor.id}
              initial={{ opacity: 0, y: 15 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3 }}
              className="group bg-white rounded-3xl border border-slate-200/90 shadow-card hover:shadow-card-hover hover:border-brand-300 transition-all p-5 sm:p-6 flex flex-col justify-between"
            >
              <div>
                {/* Header: MentorAvatar + Info */}
                <div className="flex items-start gap-4 mb-4">
                  <MentorAvatar
                    src={mentor.avatar}
                    alt={mentor.name}
                    company={mentor.company}
                    size="lg"
                  />
                  <div className="min-w-0 flex-1">
                    <h3 className="font-heading font-bold text-base text-slate-900 group-hover:text-brand-600 transition-colors truncate">
                      {mentor.name}
                    </h3>
                    <p className="text-xs text-slate-500 font-medium truncate mt-0.5">
                      {mentor.role}
                    </p>
                    <div className="inline-flex items-center gap-1.5 mt-1 px-2.5 py-1 rounded-lg bg-blue-50/80 border border-blue-200/60 text-brand-700 font-bold text-xs shadow-2xs">
                      <CompanyIcon name={mentor.company} className="w-3.5 h-3.5 shrink-0" />
                      <span>{mentor.company}</span>
                    </div>
                  </div>
                </div>

                {/* Experience & Domain Tag */}
                <div className="flex items-center justify-between gap-2 mb-3 text-xs">
                  <span className="px-2.5 py-1 rounded-md bg-slate-100 text-slate-700 font-semibold text-[11px]">
                    {mentor.experienceYears}+ Years Exp
                  </span>
                  <span className="text-[11px] text-slate-500 font-medium truncate">
                    Ex: {mentor.pastCompanies.join(', ')}
                  </span>
                </div>

                {/* Bio */}
                <p className="text-xs text-slate-600 leading-relaxed mb-4 line-clamp-3">
                  {mentor.bio}
                </p>

                {/* Specialties Chips */}
                <div className="space-y-1.5 mb-4">
                  <div className="text-[10px] font-bold uppercase tracking-wider text-slate-400">
                    Specializations
                  </div>
                  <div className="flex flex-wrap gap-1">
                    {mentor.specialization.slice(0, 3).map((spec) => (
                      <span
                        key={spec}
                        className="px-2 py-0.5 rounded-md bg-slate-50 border border-slate-200/60 text-slate-600 text-[10px] font-medium"
                      >
                        {spec}
                      </span>
                    ))}
                    {mentor.specialization.length > 3 && (
                      <span className="px-1.5 py-0.5 rounded-md bg-slate-50 text-slate-400 text-[10px] font-medium">
                        +{mentor.specialization.length - 3}
                      </span>
                    )}
                  </div>
                </div>

                {/* Next Available Slot Badge */}
                <div className="p-2.5 rounded-xl bg-emerald-50/70 border border-emerald-100/80 mb-4 flex items-center gap-2 text-xs text-emerald-800">
                  <Clock className="w-3.5 h-3.5 text-emerald-600 shrink-0" />
                  <span className="text-[11px] font-semibold truncate">
                    Next: {mentor.upcomingSlots[0]}
                  </span>
                </div>
              </div>

              {/* Card Footer: Rating & Action Buttons */}
              <div className="pt-3.5 border-t border-slate-100 flex items-center justify-between gap-2">
                <div className="flex items-center gap-1 text-xs">
                  <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400 shrink-0" />
                  <span className="font-bold text-slate-900">{mentor.rating}</span>
                  <span className="text-[11px] text-slate-400">({mentor.studentsMentored}+)</span>
                </div>

                <div className="flex items-center gap-1.5">
                  <button
                    type="button"
                    onClick={() => handleOpenBooking(mentor)}
                    className="px-3 py-1.5 rounded-xl bg-brand-600 hover:bg-brand-700 text-white font-bold text-xs shadow-sm transition-all cursor-pointer"
                  >
                    Book 1:1
                  </button>
                </div>
              </div>
            </motion.div>
          ))}
        </div>

        {/* Empty Search Fallback */}
        {filteredMentors.length === 0 && (
          <div className="p-12 text-center bg-white rounded-3xl border border-slate-200/90 space-y-3">
            <Search className="w-8 h-8 text-slate-400 mx-auto" />
            <h3 className="font-heading font-bold text-lg text-slate-900">
              No mentors found matching your search
            </h3>
            <p className="text-xs text-slate-500 max-w-sm mx-auto">
              Try adjusting your domain filters or search query to see other industry leaders.
            </p>
            <button
              type="button"
              onClick={() => {
                setSelectedDomain('all')
                setSearchQuery('')
              }}
              className="px-4 py-2 rounded-xl bg-brand-50 text-brand-600 font-bold text-xs hover:bg-brand-100"
            >
              Reset Filters
            </button>
          </div>
        )}

        {/* 5. Mentorship FAQs */}
        <div className="space-y-6 pt-6">
          <div className="flex items-center gap-2">
            <HelpCircle className="w-5 h-5 text-brand-600" />
            <h2 className="text-2xl sm:text-3xl font-heading font-extrabold text-slate-900">
              Frequently Asked Mentorship Questions
            </h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {MENTOR_FAQS.map((faq, i) => (
              <div
                key={i}
                className="p-5 rounded-2xl bg-white border border-slate-200/90 shadow-sm space-y-2"
              >
                <h4 className="font-bold text-sm text-slate-900">{faq.q}</h4>
                <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">{faq.a}</p>
              </div>
            ))}
          </div>
        </div>

        {/* 6. Bottom CTA */}
        <div className="relative overflow-hidden p-8 sm:p-12 rounded-3xl bg-blue-50/80 border border-blue-200/80 text-slate-900 flex flex-col sm:flex-row items-center justify-between gap-6 shadow-sm text-center sm:text-left">
          <div className="space-y-2 max-w-xl relative z-10">
            <h2 className="text-2xl sm:text-3xl font-heading font-extrabold text-slate-900">
              Want personalized mentor guidance?
            </h2>
            <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
              Join the next upcoming cohort to get matched with your dedicated senior engineer mentor.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3 shrink-0 relative z-10">
            <button
              type="button"
              onClick={() => openEnrollModal()}
              className="inline-flex items-center gap-2 px-6 py-3 rounded-full bg-brand-600 hover:bg-brand-700 text-white font-bold text-sm shadow-button-glow hover:shadow-lg transition-all cursor-pointer active:scale-95"
            >
              <span>Enroll in Cohort</span>
              <ArrowRight className="w-4 h-4 text-white" />
            </button>
          </div>
        </div>
      </div>

      {/* 7. Interactive 1:1 Booking Modal */}
      <AnimatePresence>
        {selectedMentorForModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="bg-white rounded-3xl max-w-xl w-full p-6 sm:p-8 shadow-2xl border border-slate-200 relative overflow-hidden max-h-[90vh] overflow-y-auto"
            >
              <button
                type="button"
                onClick={() => setSelectedMentorForModal(null)}
                className="absolute top-5 right-5 p-2 rounded-full hover:bg-slate-100 text-slate-400 hover:text-slate-700 cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>

              {bookingSubmitted ? (
                <div className="py-8 text-center space-y-4">
                  <div className="w-16 h-16 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center mx-auto">
                    <CheckCircle2 className="w-8 h-8" />
                  </div>
                  <h3 className="font-heading font-extrabold text-2xl text-slate-900">
                    Session Confirmed!
                  </h3>
                  <p className="text-xs sm:text-sm text-slate-600 max-w-md mx-auto leading-relaxed">
                    Your 1:1 Mentorship Session with{' '}
                    <strong className="text-slate-900">{selectedMentorForModal.name}</strong> has been scheduled for{' '}
                    <strong className="text-brand-600">{selectedSlot}</strong>.
                  </p>
                  <p className="text-xs text-slate-500">
                    We've dispatched calendar invite & Google Meet links to{' '}
                    <strong className="text-slate-900">{bookingForm.email || 'your email'}</strong>.
                  </p>
                  <div className="pt-4">
                    <Button
                      variant="primary"
                      onClick={() => setSelectedMentorForModal(null)}
                      className="bg-brand-600 hover:bg-brand-700 font-bold rounded-xl"
                    >
                      Done
                    </Button>
                  </div>
                </div>
              ) : (
                <form onSubmit={handleConfirmBooking} className="space-y-5">
                  {/* Mentor Header Profile */}
                  <div className="flex items-center gap-4 pb-4 border-b border-slate-100">
                    <MentorAvatar
                      src={selectedMentorForModal.avatar}
                      alt={selectedMentorForModal.name}
                      company={selectedMentorForModal.company}
                      size="xl"
                    />
                    <div>
                      <h3 className="font-heading font-black text-xl text-slate-900">
                        {selectedMentorForModal.name}
                      </h3>
                      <p className="text-xs text-slate-500 font-medium">
                        {selectedMentorForModal.role} @{' '}
                        <strong className="text-slate-800 font-bold">{selectedMentorForModal.company}</strong>
                      </p>
                      <div className="flex items-center gap-1 text-amber-500 font-bold text-xs mt-1">
                        <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                        <span>{selectedMentorForModal.rating}</span>
                        <span className="text-slate-400 font-normal">
                          ({selectedMentorForModal.studentsMentored}+ learners coached)
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Step 1: Select Topic */}
                  <div className="space-y-2">
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider">
                      1. Select Discussion Objective
                    </label>
                    <div className="grid grid-cols-1 gap-2">
                      {selectedMentorForModal.featuredTopics.map((topic) => (
                        <button
                          key={topic}
                          type="button"
                          onClick={() => setSelectedTopic(topic)}
                          className={`p-3 rounded-xl text-left text-xs font-semibold border transition-all cursor-pointer ${
                            selectedTopic === topic
                              ? 'bg-brand-50 border-brand-500 text-brand-700 shadow-xs'
                              : 'bg-white border-slate-200 text-slate-700 hover:bg-slate-50'
                          }`}
                        >
                          {topic}
                        </button>
                      ))}
                    </div>
                  </div>

                  {/* Step 2: Select Slot */}
                  <div className="space-y-2">
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider">
                      2. Choose Available Time Slot
                    </label>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                      {selectedMentorForModal.upcomingSlots.map((slot) => (
                        <button
                          key={slot}
                          type="button"
                          onClick={() => setSelectedSlot(slot)}
                          className={`p-2.5 rounded-xl text-left text-xs font-semibold border transition-all cursor-pointer flex items-center gap-2 ${
                            selectedSlot === slot
                              ? 'bg-emerald-50 border-emerald-500 text-emerald-800 shadow-xs'
                              : 'bg-white border-slate-200 text-slate-700 hover:bg-slate-50'
                          }`}
                        >
                          <Clock className="w-3.5 h-3.5 text-emerald-600 shrink-0" />
                          <span className="truncate">{slot}</span>
                        </button>
                      ))}
                    </div>
                  </div>

                  {/* Step 3: Contact Info */}
                  <div className="space-y-3 pt-1">
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider">
                      3. Your Learner Details
                    </label>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <div>
                        <input
                          type="text"
                          required
                          placeholder="Your Full Name *"
                          value={bookingForm.name}
                          onChange={(e) => setBookingForm({ ...bookingForm, name: e.target.value })}
                          className="w-full px-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-brand-500 focus:bg-white"
                        />
                      </div>
                      <div>
                        <input
                          type="email"
                          required
                          placeholder="Your Email Address *"
                          value={bookingForm.email}
                          onChange={(e) => setBookingForm({ ...bookingForm, email: e.target.value })}
                          className="w-full px-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-brand-500 focus:bg-white"
                        />
                      </div>
                    </div>
                    <div>
                      <textarea
                        rows={2}
                        placeholder="Specific questions or repo link you'd like the mentor to review..."
                        value={bookingForm.notes}
                        onChange={(e) => setBookingForm({ ...bookingForm, notes: e.target.value })}
                        className="w-full px-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-brand-500 focus:bg-white resize-none"
                      />
                    </div>
                  </div>

                  {/* Submit Button */}
                  <div className="pt-2">
                    <Button
                      type="submit"
                      variant="primary"
                      size="md"
                      className="w-full justify-center bg-brand-600 hover:bg-brand-700 font-bold rounded-xl py-3 shadow-button-glow"
                    >
                      Confirm 1:1 Mentorship Session
                    </Button>
                  </div>
                </form>
              )}
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  )
}
