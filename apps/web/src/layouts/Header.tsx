import React, { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { Search, ArrowRight, Menu, X } from 'lucide-react'
import { motion, AnimatePresence } from 'framer-motion'
import { Logo } from '@/components/common/Logo'
import { PRIMARY_NAV_ITEMS, POPULAR_PROGRAMS } from '@/data'
import { useModals, useAuth } from '@/context'
import { cn } from '@/lib/utils'

export const Header: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const [searchOpen, setSearchOpen] = useState(false)
  const [searchQuery, setSearchQuery] = useState('')
  const location = useLocation()
  const { openLoginModal, openEnrollModal } = useModals()
  const { isAuthenticated, user, logout } = useAuth()

  const searchResults = searchQuery.trim()
    ? POPULAR_PROGRAMS.filter(
        (p) =>
          p.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
          p.description.toLowerCase().includes(searchQuery.toLowerCase())
      )
    : []

  return (
    <header className="sticky top-0 z-40 bg-white/95 backdrop-blur-md border-b border-slate-100 transition-all duration-200">
      <div className="academy-container">
        <div className="flex items-center justify-between h-20">
          {/* Left Brand Logo */}
          <div className="flex items-center shrink-0">
            <Logo />
          </div>

          {/* Center Navigation Links (Laptop & Desktop) */}
          <nav className="hidden lg:flex items-center gap-5 xl:gap-7">
            {PRIMARY_NAV_ITEMS.map((item) => {
              const isActive = location.pathname === item.href
              const handleNavClick = (e: React.MouseEvent) => {
                if (item.href.includes('#')) {
                  const hash = item.href.includes('/#')
                    ? item.href.replace('/', '')
                    : item.href
                  if (location.pathname === '/' || item.href.startsWith('#')) {
                    e.preventDefault()
                    const target = document.querySelector(hash)
                    if (target) target.scrollIntoView({ behavior: 'smooth' })
                  }
                }
              }

              return (
                <Link
                  key={item.label}
                  to={item.href}
                  onClick={handleNavClick}
                  className={cn(
                    'relative py-2 text-xs xl:text-sm font-semibold transition-colors duration-200 select-none cursor-pointer',
                    isActive
                      ? 'text-brand-600'
                      : 'text-slate-600 hover:text-brand-600'
                  )}
                >
                  {item.label}
                  {isActive && (
                    <motion.span
                      layoutId="nav-active-pill"
                      className="absolute bottom-0 left-0 right-0 h-0.5 bg-brand-600 rounded-full"
                      transition={{ type: 'spring', stiffness: 380, damping: 30 }}
                    />
                  )}
                </Link>
              )
            })}
          </nav>

          {/* Right Action Controls */}
          <div className="hidden md:flex items-center gap-3 xl:gap-4 shrink-0">
            {/* Search Icon */}
            <div className="relative">
              <button
                type="button"
                aria-label="Search courses"
                onClick={() => setSearchOpen(!searchOpen)}
                className="p-2 text-slate-600 hover:text-brand-600 hover:bg-slate-50 rounded-xl transition-all cursor-pointer"
              >
                <Search className="w-4 h-4" />
              </button>

              <AnimatePresence>
                {searchOpen && (
                  <motion.div
                    initial={{ opacity: 0, y: 8, scale: 0.95 }}
                    animate={{ opacity: 1, y: 0, scale: 1 }}
                    exit={{ opacity: 0, y: 8, scale: 0.95 }}
                    transition={{ duration: 0.15 }}
                    className="absolute right-0 top-12 w-80 bg-white rounded-2xl shadow-elevated border border-slate-200/90 p-3 z-50"
                  >
                    <div className="relative">
                      <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                      <input
                        type="text"
                        placeholder="Search courses, mentors..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        autoFocus
                        className="w-full pl-9 pr-3 py-2 text-xs border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 bg-slate-50/50"
                      />
                    </div>

                    {/* Live search results */}
                    {searchResults.length > 0 && (
                      <div className="mt-2 pt-2 border-t border-slate-100 max-h-48 overflow-y-auto space-y-1">
                        {searchResults.map((res) => (
                          <div
                            key={res.id}
                            onClick={() => {
                              openEnrollModal(res.id)
                              setSearchOpen(false)
                            }}
                            className="p-2 hover:bg-slate-50 rounded-lg cursor-pointer text-xs"
                          >
                            <div className="font-bold text-slate-900">{res.title}</div>
                            <div className="text-[10px] text-slate-500">{res.duration} • {res.level}</div>
                          </div>
                        ))}
                      </div>
                    )}
                  </motion.div>
                )}
              </AnimatePresence>
            </div>

            {/* Vertical Divider */}
            <div className="h-5 w-px bg-slate-200 mx-1" />

            {/* User status or Log In Button */}
            {isAuthenticated && user ? (
              <div className="flex items-center gap-2">
                <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-slate-50 border border-slate-200 shadow-sm">
                  <div className="w-6 h-6 rounded-full bg-brand-600 text-white flex items-center justify-center text-[10px] font-bold">
                    {user.firstName ? user.firstName.charAt(0).toUpperCase() : 'U'}
                  </div>
                  <span className="text-xs font-bold text-slate-800 max-w-[100px] truncate">
                    {user.firstName || user.email}
                  </span>
                </div>
                <button
                  type="button"
                  id="header-logout-btn"
                  onClick={logout}
                  className="px-3 py-1.5 text-xs font-semibold text-slate-500 hover:text-rose-600 hover:bg-rose-50 border border-slate-200 rounded-full transition-colors cursor-pointer"
                >
                  Sign Out
                </button>
              </div>
            ) : (
              <button
                type="button"
                id="header-login-btn"
                onClick={openLoginModal}
                className="px-5 py-2 text-xs xl:text-sm font-semibold text-slate-700 hover:text-brand-600 border border-slate-300 hover:border-brand-500 rounded-full bg-white hover:bg-slate-50 active:scale-95 transition-all cursor-pointer select-none shadow-sm hover:shadow"
              >
                Log In
              </button>
            )}

            {/* Enroll Now CTA Button */}
            <button
              type="button"
              id="header-enroll-btn"
              onClick={() => openEnrollModal()}
              className="group inline-flex items-center gap-2 px-5 py-2 text-xs xl:text-sm font-bold text-white bg-brand-600 hover:bg-brand-700 active:bg-brand-800 rounded-full shadow-button-glow hover:shadow-lg active:scale-95 transition-all cursor-pointer select-none"
            >
              <span>Enroll Now</span>
              <ArrowRight className="w-3.5 h-3.5 xl:w-4 xl:h-4 text-white transition-transform group-hover:translate-x-1" />
            </button>
          </div>

          {/* Mobile & Tablet Controls (< 1024px) */}
          <div className="flex lg:hidden items-center gap-2">
            <button
              type="button"
              aria-label="Search courses"
              onClick={() => setSearchOpen(!searchOpen)}
              className="p-2 text-slate-600 hover:text-brand-600 rounded-xl hover:bg-slate-100 cursor-pointer"
            >
              <Search className="w-5 h-5" />
            </button>
            <button
              type="button"
              aria-label="Open mobile menu"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 text-slate-600 hover:text-slate-900 rounded-xl hover:bg-slate-100 transition-colors cursor-pointer"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>

        {/* Mobile Search Bar Drawer */}
        <AnimatePresence>
          {searchOpen && (
            <motion.div
              initial={{ height: 0, opacity: 0 }}
              animate={{ height: 'auto', opacity: 1 }}
              exit={{ height: 0, opacity: 0 }}
              className="md:hidden pb-3 overflow-hidden"
            >
              <div className="relative">
                <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                <input
                  type="text"
                  placeholder="Search courses, mentors, topics..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  autoFocus
                  className="w-full pl-9 pr-3 py-2 text-sm border border-slate-200 rounded-xl focus:outline-none focus:border-brand-500 bg-white shadow-sm"
                />
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* Mobile Animated Dropdown Menu */}
      <AnimatePresence>
        {mobileMenuOpen && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            transition={{ duration: 0.2 }}
            className="lg:hidden border-t border-slate-100 bg-white/98 backdrop-blur-md px-4 pt-3 pb-6 shadow-xl overflow-hidden"
          >
            <nav className="flex flex-col space-y-1">
              {PRIMARY_NAV_ITEMS.map((item) => {
                const isActive = location.pathname === item.href
                return (
                  <Link
                    key={item.label}
                    to={item.href}
                    onClick={(e) => {
                      setMobileMenuOpen(false)
                      if (item.href.includes('#')) {
                        const hash = item.href.includes('/#')
                          ? item.href.replace('/', '')
                          : item.href
                        if (location.pathname === '/' || item.href.startsWith('#')) {
                          e.preventDefault()
                          const target = document.querySelector(hash)
                          if (target) target.scrollIntoView({ behavior: 'smooth' })
                        }
                      }
                    }}
                    className={cn(
                      'px-3.5 py-2.5 rounded-xl text-sm font-semibold transition-colors',
                      isActive
                        ? 'bg-brand-50 text-brand-600'
                        : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
                    )}
                  >
                    {item.label}
                  </Link>
                )
              })}
            </nav>
            <div className="mt-4 pt-4 border-t border-slate-100 flex flex-col gap-2.5">
              {isAuthenticated && user ? (
                <div className="flex flex-col gap-2">
                  <div className="flex items-center gap-2.5 px-3 py-2 rounded-xl bg-slate-50 border border-slate-200">
                    <div className="w-8 h-8 rounded-full bg-brand-600 text-white flex items-center justify-center text-xs font-bold shrink-0">
                      {user.firstName ? user.firstName.charAt(0).toUpperCase() : 'U'}
                    </div>
                    <div className="text-xs overflow-hidden">
                      <div className="font-bold text-slate-900 truncate">{user.firstName} {user.lastName}</div>
                      <div className="text-[10px] text-slate-500 truncate">{user.email}</div>
                    </div>
                  </div>
                  <button
                    type="button"
                    id="mobile-header-logout-btn"
                    onClick={() => {
                      setMobileMenuOpen(false)
                      logout()
                    }}
                    className="w-full py-2.5 text-xs font-bold text-rose-600 border border-rose-200 rounded-full hover:bg-rose-50 transition-colors text-center cursor-pointer shadow-sm active:scale-95"
                  >
                    Sign Out
                  </button>
                </div>
              ) : (
                <button
                  type="button"
                  id="mobile-header-login-btn"
                  onClick={() => {
                    setMobileMenuOpen(false)
                    openLoginModal()
                  }}
                  className="w-full py-2.5 text-sm font-bold text-slate-800 border border-slate-300 rounded-full hover:bg-slate-50 transition-colors text-center cursor-pointer shadow-sm active:scale-95"
                >
                  Log In
                </button>
              )}
              <button
                type="button"
                id="mobile-header-enroll-btn"
                onClick={() => {
                  setMobileMenuOpen(false)
                  openEnrollModal()
                }}
                className="w-full flex items-center justify-center gap-2 bg-brand-600 hover:bg-brand-700 text-white py-3 rounded-full font-bold shadow-button-glow cursor-pointer transition-all active:scale-95"
              >
                <span>Enroll Now</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </header>
  )
}
