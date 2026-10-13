import React from 'react'
import { useParams } from 'react-router-dom'
import { ProgramDetailView } from './ProgramDetailView'

export const ProgramDetailPage: React.FC = () => {
  const { programId } = useParams<{ programId?: string }>()
  return <ProgramDetailView slug={programId || 'full-stack'} />
}
