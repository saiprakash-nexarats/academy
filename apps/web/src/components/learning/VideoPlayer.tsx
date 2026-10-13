import React, { useRef, useState, useEffect, useCallback } from 'react'
import {
  Play,
  Pause,
  Volume2,
  VolumeX,
  Maximize,
  RotateCcw,
  CheckCircle,
  AlertCircle,
  Loader2,
} from 'lucide-react'
import { cn } from '@/lib/utils'

interface VideoPlayerProps {
  streamUrl: string | null
  durationSeconds: number
  initialPositionSeconds: number
  isCompleted: boolean
  isLoadingSession: boolean
  error?: string | null
  onCheckpoint: (positionSeconds: number, deltaSeconds: number) => void
  onComplete: () => void
  onRetrySession?: () => void
}

export const VideoPlayer: React.FC<VideoPlayerProps> = ({
  streamUrl,
  durationSeconds,
  initialPositionSeconds,
  isCompleted,
  isLoadingSession,
  error,
  onCheckpoint,
  onComplete,
  onRetrySession,
}) => {
  const videoRef = useRef<HTMLVideoElement | null>(null)
  const containerRef = useRef<HTMLDivElement | null>(null)

  const [isPlaying, setIsPlaying] = useState<boolean>(false)
  const [currentTime, setCurrentTime] = useState<number>(initialPositionSeconds)
  const [duration, setDuration] = useState<number>(durationSeconds)
  const [volume, setVolume] = useState<number>(1)
  const [isMuted, setIsMuted] = useState<boolean>(false)
  const [playbackRate, setPlaybackRate] = useState<number>(1)
  const [showControls, setShowControls] = useState<boolean>(true)
  const [hasResumed, setHasResumed] = useState<boolean>(false)
  const [isBuffering, setIsBuffering] = useState<boolean>(false)

  const lastCheckpointTimeRef = useRef<number>(initialPositionSeconds)
  const controlsTimeoutRef = useRef<number | null>(null)

  // Initialize playback position upon initial load
  useEffect(() => {
    if (videoRef.current && initialPositionSeconds > 0 && !hasResumed) {
      videoRef.current.currentTime = initialPositionSeconds
      setCurrentTime(initialPositionSeconds)
      setHasResumed(true)
    }
  }, [initialPositionSeconds, hasResumed])

  // Periodic progress checkpoint ticker (every 10 seconds of active playback)
  useEffect(() => {
    if (!isPlaying) return

    const interval = window.setInterval(() => {
      if (videoRef.current) {
        const cur = Math.floor(videoRef.current.currentTime)
        const delta = Math.max(0, cur - lastCheckpointTimeRef.current)
        if (delta >= 5) {
          onCheckpoint(cur, delta)
          lastCheckpointTimeRef.current = cur
        }
      }
    }, 10000)

    return () => clearInterval(interval)
  }, [isPlaying, onCheckpoint])

  // Hide controls after 3 seconds of inactivity
  const handleMouseMove = () => {
    setShowControls(true)
    if (controlsTimeoutRef.current) clearTimeout(controlsTimeoutRef.current)
    controlsTimeoutRef.current = window.setTimeout(() => {
      if (isPlaying) setShowControls(false)
    }, 3500)
  }

  const togglePlay = useCallback(() => {
    if (!videoRef.current) return
    if (videoRef.current.paused) {
      videoRef.current.play().then(() => setIsPlaying(true)).catch((err) => {
        console.warn('Playback play request prevented:', err)
      })
    } else {
      videoRef.current.pause()
      setIsPlaying(false)
      // Send checkpoint on pause
      const cur = Math.floor(videoRef.current.currentTime)
      const delta = Math.max(0, cur - lastCheckpointTimeRef.current)
      if (delta > 0) {
        onCheckpoint(cur, delta)
        lastCheckpointTimeRef.current = cur
      }
    }
  }, [onCheckpoint])

  const handleTimeUpdate = () => {
    if (!videoRef.current) return
    const cur = videoRef.current.currentTime
    setCurrentTime(cur)

    // Trigger complete if reached 90%
    const total = duration || durationSeconds
    if (total > 0 && cur >= total * 0.90 && !isCompleted) {
      onComplete()
    }
  }

  const handleSeek = (e: React.ChangeEvent<HTMLInputElement>) => {
    const target = parseFloat(e.target.value)
    if (videoRef.current) {
      videoRef.current.currentTime = target
      setCurrentTime(target)
      lastCheckpointTimeRef.current = Math.floor(target)
    }
  }

  const handleVolumeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = parseFloat(e.target.value)
    setVolume(val)
    if (videoRef.current) {
      videoRef.current.volume = val
      setIsMuted(val === 0)
    }
  }

  const toggleMute = () => {
    if (!videoRef.current) return
    const nextMuted = !isMuted
    setIsMuted(nextMuted)
    videoRef.current.muted = nextMuted
  }

  const handleSpeedChange = (rate: number) => {
    setPlaybackRate(rate)
    if (videoRef.current) {
      videoRef.current.playbackRate = rate
    }
  }

  const toggleFullscreen = () => {
    if (!containerRef.current) return
    if (!document.fullscreenElement) {
      containerRef.current.requestFullscreen().catch((err) => console.warn(err))
    } else {
      document.exitFullscreen().catch((err) => console.warn(err))
    }
  }

  const formatTime = (secs: number) => {
    const total = Math.floor(secs)
    const m = Math.floor(total / 60)
    const s = total % 60
    return `${m}:${s < 10 ? '0' : ''}${s}`
  }

  if (isLoadingSession) {
    return (
      <div className="relative aspect-video w-full rounded-2xl bg-slate-950 flex flex-col items-center justify-center text-white border border-slate-800 shadow-2xl">
        <Loader2 className="w-10 h-10 animate-spin text-brand-500 mb-3" />
        <p className="text-sm font-semibold tracking-wide text-slate-300">
          Authorizing secure video playback session...
        </p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="relative aspect-video w-full rounded-2xl bg-slate-950 flex flex-col items-center justify-center text-white border border-rose-900/50 p-6 text-center shadow-2xl">
        <AlertCircle className="w-12 h-12 text-rose-500 mb-3" />
        <h3 className="text-base font-bold text-white mb-1">Playback Authorization Denied</h3>
        <p className="text-xs text-slate-400 max-w-md mb-4">{error}</p>
        {onRetrySession && (
          <button
            onClick={onRetrySession}
            className="inline-flex items-center gap-2 px-4 py-2 bg-brand-600 hover:bg-brand-500 text-white text-xs font-semibold rounded-full transition-all"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            Retry Authorization
          </button>
        )}
      </div>
    )
  }

  return (
    <div
      ref={containerRef}
      onMouseMove={handleMouseMove}
      className="relative aspect-video w-full rounded-2xl bg-slate-950 overflow-hidden border border-slate-800 shadow-2xl group select-none"
    >
      {/* Video Media Element */}
      {streamUrl && (
        <video
          ref={videoRef}
          src={streamUrl}
          onTimeUpdate={handleTimeUpdate}
          onLoadedMetadata={() => {
            if (videoRef.current) {
              setDuration(videoRef.current.duration || durationSeconds)
            }
          }}
          onWaiting={() => setIsBuffering(true)}
          onPlaying={() => {
            setIsBuffering(false)
            setIsPlaying(true)
          }}
          onPause={() => setIsPlaying(false)}
          onEnded={() => {
            setIsPlaying(false)
            onComplete()
          }}
          onClick={togglePlay}
          className="w-full h-full object-contain cursor-pointer"
          playsInline
        />
      )}

      {/* Buffering Indicator */}
      {isBuffering && (
        <div className="absolute inset-0 flex items-center justify-center bg-black/40 pointer-events-none">
          <Loader2 className="w-10 h-10 animate-spin text-brand-400" />
        </div>
      )}

      {/* Resume Banner */}
      {hasResumed && initialPositionSeconds > 0 && (
        <div className="absolute top-4 left-4 bg-slate-900/90 backdrop-blur-md border border-brand-500/30 px-3 py-1.5 rounded-full text-[11px] font-semibold text-brand-300 flex items-center gap-2 shadow-lg transition-opacity duration-500">
          <RotateCcw className="w-3 h-3 text-brand-400" />
          <span>Resumed from {formatTime(initialPositionSeconds)}</span>
        </div>
      )}

      {/* Completed Badge */}
      {isCompleted && (
        <div className="absolute top-4 right-4 bg-emerald-950/80 backdrop-blur-md border border-emerald-500/40 px-3 py-1.5 rounded-full text-[11px] font-semibold text-emerald-300 flex items-center gap-1.5 shadow-lg">
          <CheckCircle className="w-3.5 h-3.5 text-emerald-400" />
          <span>Completed</span>
        </div>
      )}

      {/* Controls Overlay */}
      <div
        className={cn(
          'absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/90 via-black/50 to-transparent p-4 transition-opacity duration-300 flex flex-col gap-2',
          showControls || !isPlaying ? 'opacity-100' : 'opacity-0 pointer-events-none'
        )}
      >
        {/* Scrubber Bar */}
        <div className="flex items-center gap-2 w-full">
          <input
            type="range"
            min={0}
            max={duration || durationSeconds || 100}
            step={0.5}
            value={currentTime}
            onChange={handleSeek}
            aria-label="Seek video progress"
            className="w-full h-1.5 bg-slate-700/80 rounded-lg appearance-none cursor-pointer accent-brand-500 hover:h-2 transition-all"
          />
        </div>

        {/* Action Controls Row */}
        <div className="flex items-center justify-between text-white text-xs">
          <div className="flex items-center gap-3">
            {/* Play/Pause */}
            <button
              onClick={togglePlay}
              aria-label={isPlaying ? 'Pause video' : 'Play video'}
              className="p-1.5 hover:bg-white/10 rounded-full transition-colors cursor-pointer"
            >
              {isPlaying ? <Pause className="w-5 h-5 fill-white" /> : <Play className="w-5 h-5 fill-white" />}
            </button>

            {/* Volume */}
            <div className="flex items-center gap-1.5 group/vol">
              <button
                onClick={toggleMute}
                aria-label={isMuted ? 'Unmute' : 'Mute'}
                className="p-1.5 hover:bg-white/10 rounded-full transition-colors cursor-pointer"
              >
                {isMuted || volume === 0 ? (
                  <VolumeX className="w-4 h-4 text-slate-400" />
                ) : (
                  <Volume2 className="w-4 h-4 text-white" />
                )}
              </button>
              <input
                type="range"
                min={0}
                max={1}
                step={0.05}
                value={isMuted ? 0 : volume}
                onChange={handleVolumeChange}
                aria-label="Volume slider"
                className="w-16 h-1 bg-slate-700 rounded-lg appearance-none cursor-pointer accent-brand-500 hidden group-hover/vol:block transition-all"
              />
            </div>

            {/* Time Stamp */}
            <span className="font-mono text-[11px] text-slate-300">
              {formatTime(currentTime)} / {formatTime(duration || durationSeconds)}
            </span>
          </div>

          <div className="flex items-center gap-2">
            {/* Speed Selector */}
            <div className="flex items-center gap-1 bg-white/10 px-2 py-1 rounded-full text-[11px] font-semibold">
              {[1, 1.25, 1.5, 2].map((rate) => (
                <button
                  key={rate}
                  onClick={() => handleSpeedChange(rate)}
                  className={cn(
                    'px-1.5 py-0.5 rounded cursor-pointer transition-colors',
                    playbackRate === rate ? 'bg-brand-600 text-white font-bold' : 'text-slate-400 hover:text-white'
                  )}
                >
                  {rate}x
                </button>
              ))}
            </div>

            {/* Fullscreen */}
            <button
              onClick={toggleFullscreen}
              aria-label="Toggle fullscreen"
              className="p-1.5 hover:bg-white/10 rounded-full transition-colors cursor-pointer"
            >
              <Maximize className="w-4 h-4 text-slate-300 hover:text-white" />
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
