import React from 'react'
import { Link } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { Button } from '@/components/ui/Button'

export const NotFoundPage: React.FC = () => {
  return (
    <div className="py-24 text-center academy-container">
      <div className="max-w-md mx-auto space-y-4">
        <span className="text-6xl font-extrabold text-brand-600 font-heading">404</span>
        <h2 className="text-2xl font-bold text-slate-900 font-heading">Page Not Found</h2>
        <p className="text-slate-600 text-sm">
          The page you are looking for does not exist or has been moved.
        </p>
        <div className="pt-4 flex justify-center">
          <Link to="/">
            <Button variant="primary" iconLeft={<ArrowLeft className="w-4 h-4" />}>
              Back to Home
            </Button>
          </Link>
        </div>
      </div>
    </div>
  )
}
