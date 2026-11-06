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

  // Accurately decouple watch time duration from playback position
  const accumulatedWatchSecondsRef = useRef<number>(0)
  const lastRealTimeRef = useRef<number>(0)
  const controlsTimeoutRef = useRef<number | null>(null)

  // Initialize playback position upon initial load
  useEffect(() => {
    if (videoRef.current && initialPositionSeconds > 0 && !hasResumed) {
      videoRef.current.currentTime = initialPositionSeconds
      setCurrentTime(initialPositionSeconds)
      setHasResumed(true)
    }
  }, [initialPositionSeconds, hasResumed])

  // Active watch-time tracker: increments strictly while video is actively playing
  useEffect(() => {
    if (!isPlaying) return

    lastRealTimeRef.current = performance.now()

    const ticker = window.setInterval(() => {
      if (!videoRef.current || videoRef.current.paused || videoRef.current.seeking) {
        lastRealTimeRef.current = performance.now()
        return
      }

      const now = performance.now()
      const elapsedRealSec = (now - lastRealTimeRef.current) / 1000
      lastRealTimeRef.current = now

      // Safely clamp tick elapsed duration to avoid browser background-tab throttling bursts
      if (elapsedRealSec > 0 && elapsedRealSec <= 2) {
        accumulatedWatchSecondsRef.current += elapsedRealSec
      }
    }, 500)

    return () => clearInterval(ticker)
  }, [isPlaying])

  // Helper to submit accumulated watch time checkpoint
  const flushCheckpoint = useCallback(() => {
    if (!videoRef.current) return
    const cur = Math.floor(videoRef.current.currentTime)
    const watchDelta = Math.floor(accumulatedWatchSecondsRef.current)

    if (watchDelta >= 1) {
      // Clamped to backend boundary (<= 300s)
      const safeDelta = Math.min(300, watchDelta)
      onCheckpoint(cur, safeDelta)
      accumulatedWatchSecondsRef.current = Math.max(0, accumulatedWatchSecondsRef.current - watchDelta)
    }
  }, [onCheckpoint])

  // Periodic progress checkpoint ticker (every 10 seconds of active playback)
  useEffect(() => {
    if (!isPlaying) return

    const interval = window.setInterval(() => {
      flushCheckpoint()
    }, 10000)

    return () => clearInterval(interval)
  }, [isPlaying, flushCheckpoint])

  // Hide controls after 3.5 seconds of inactivity
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
      lastRealTimeRef.current = performance.now()
      videoRef.current.play().then(() => setIsPlaying(true)).catch((err) => {
        console.warn('Playback play request prevented:', err)
      })
    } else {
      videoRef.current.pause()
      setIsPlaying(false)
      // Submit accumulated progress on pause
      flushCheckpoint()
    }
  }, [flushCheckpoint])

  const seekTo = useCallback((targetTime: number) => {
    if (!videoRef.current) return
    const total = duration || durationSeconds || 100
    const clampedTarget = Math.max(0, Math.min(total, targetTime))

    videoRef.current.currentTime = clampedTarget
    setCurrentTime(clampedTarget)
    // Reset real-time reference so seek jump does not pollute watch-time
    lastRealTimeRef.current = performance.now()
  }, [duration, durationSeconds])

  const seekBy = useCallback((offsetSec: number) => {
    if (!videoRef.current) return
    seekTo(videoRef.current.currentTime + offsetSec)
  }, [seekTo])

  const handleSeekSlider = (e: React.ChangeEvent<HTMLInputElement>) => {
    const target = parseFloat(e.target.value)
    seekTo(target)
  }

  const handleVolumeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = parseFloat(e.target.value)
    setVolume(val)
    if (videoRef.current) {
      videoRef.current.volume = val
      setIsMuted(val === 0)
    }
  }

  const toggleMute = useCallback(() => {
    if (!videoRef.current) return
    const nextMuted = !isMuted
    setIsMuted(nextMuted)
    videoRef.current.muted = nextMuted
  }, [isMuted])

  const handleSpeedChange = (rate: number) => {
    setPlaybackRate(rate)
    if (videoRef.current) {
      videoRef.current.playbackRate = rate
    }
  }

  const toggleFullscreen = useCallback(() => {
    if (!containerRef.current) return
    if (!document.fullscreenElement) {
      containerRef.current.requestFullscreen().catch((err) => console.warn(err))
    } else {
      document.exitFullscreen().catch((err) => console.warn(err))
    }
  }, [])

  const handleTimeUpdate = () => {
    if (!videoRef.current) return
    const cur = videoRef.current.currentTime
    setCurrentTime(cur)

    // Trigger complete if reached 90%
    const total = duration || durationSeconds
    if (total > 0 && cur >= total * 0.90 && !isCompleted) {
      flushCheckpoint()
      onComplete()
    }
  }

  // Keyboard accessibility handler
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      const activeEl = document.activeElement as HTMLElement | null
      const isInput =
        activeEl &&
        (activeEl.tagName === 'INPUT' ||
          activeEl.tagName === 'TEXTAREA' ||
          activeEl.tagName === 'SELECT' ||
          activeEl.isContentEditable)

      if (isInput) return

      // Space: Toggle Play/Pause
      if (e.code === 'Space') {
        e.preventDefault()
        togglePlay()
      }
      // ArrowLeft: Seek backward 5 seconds
      else if (e.code === 'ArrowLeft') {
        e.preventDefault()
        seekBy(-5)
      }
      // ArrowRight: Seek forward 5 seconds
      else if (e.code === 'ArrowRight') {
        e.preventDefault()
        seekBy(5)
      }
      // KeyM: Toggle Mute
      else if (e.code === 'KeyM') {
        e.preventDefault()
        toggleMute()
      }
      // KeyF: Toggle Fullscreen
      else if (e.code === 'KeyF') {
        e.preventDefault()
        toggleFullscreen()
      }
    }

    const container = containerRef.current
    if (container) {
      container.addEventListener('keydown', handleKeyDown)
    }

    return () => {
      if (container) {
        container.removeEventListener('keydown', handleKeyDown)
      }
    }
  }, [togglePlay, seekBy, toggleMute, toggleFullscreen])

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
            className="inline-flex items-center gap-2 px-4 py-2 bg-brand-600 hover:bg-brand-500 text-white text-xs font-semibold rounded-full transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-brand-400"
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
      tabIndex={0}
      role="region"
      aria-label="Course Lesson Video Player"
      onMouseMove={handleMouseMove}
      className="relative aspect-video w-full rounded-2xl bg-slate-950 overflow-hidden border border-slate-800 shadow-2xl group select-none focus:outline-none focus-visible:ring-2 focus-visible:ring-brand-500"
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
            flushCheckpoint()
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
            onChange={handleSeekSlider}
            aria-label="Seek video progress"
            className="w-full h-1.5 bg-slate-700/80 rounded-lg appearance-none cursor-pointer accent-brand-500 hover:h-2 transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-brand-400"
          />
        </div>

        {/* Action Controls Row */}
        <div className="flex items-center justify-between text-white text-xs">
          <div className="flex items-center gap-3">
            {/* Play/Pause */}
            <button
              onClick={togglePlay}
              aria-label={isPlaying ? 'Pause video (Space)' : 'Play video (Space)'}
              className="p-1.5 hover:bg-white/10 rounded-full transition-colors cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-brand-400"
            >
              {isPlaying ? <Pause className="w-5 h-5 fill-white" /> : <Play className="w-5 h-5 fill-white" />}
            </button>

            {/* Volume */}
            <div className="flex items-center gap-1.5 group/vol">
              <button
                onClick={toggleMute}
                aria-label={isMuted ? 'Unmute (M)' : 'Mute (M)'}
                className="p-1.5 hover:bg-white/10 rounded-full transition-colors cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-brand-400"
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
                className="w-16 h-1 bg-slate-700 rounded-lg appearance-none cursor-pointer accent-brand-500 hidden group-hover/vol:block transition-all focus:outline-none focus-visible:ring-2 focus-visible:ring-brand-400"
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
                  aria-label={`Set speed to ${rate}x`}
                  className={cn(
                    'px-1.5 py-0.5 rounded cursor-pointer transition-colors focus:outline-none focus-visible:ring-1 focus-visible:ring-brand-400',
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
              aria-label="Toggle fullscreen (F)"
              className="p-1.5 hover:bg-white/10 rounded-full transition-colors cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-brand-400"
            >
              <Maximize className="w-4 h-4 text-slate-300 hover:text-white" />
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
