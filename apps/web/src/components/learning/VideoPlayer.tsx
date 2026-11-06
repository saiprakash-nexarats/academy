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

  // Accurately decouple cumulative total watched time from unsubmitted checkpoint delta
  const totalWatchSecondsRef = useRef<number>(0)
  const unsubmittedDeltaSecondsRef = useRef<number>(0)
  const lastRealTimeRef = useRef<number>(0)
  const controlsTimeoutRef = useRef<number | null>(null)
  const hasTriggeredCompleteRef = useRef<boolean>(isCompleted)

  // Sync completion ref when prop changes
  useEffect(() => {
    if (isCompleted) {
      hasTriggeredCompleteRef.current = true
    }
  }, [isCompleted])

  // Initialize playback position upon initial load
  useEffect(() => {
    if (videoRef.current && initialPositionSeconds > 0 && !hasResumed) {
      videoRef.current.currentTime = initialPositionSeconds
      setCurrentTime(initialPositionSeconds)
      setHasResumed(true)
    }
  }, [initialPositionSeconds, hasResumed])

  // Flush unsubmitted checkpoint delta to parent callback
  const flushCheckpoint = useCallback(() => {
    if (!videoRef.current) return
    const cur = Math.floor(videoRef.current.currentTime)
    const watchDelta = Math.floor(unsubmittedDeltaSecondsRef.current)

    if (watchDelta >= 1) {
      const safeDelta = Math.min(300, watchDelta)
      onCheckpoint(cur, safeDelta)
      // Deduct flushed delta from unsubmitted accumulator (totalWatchSecondsRef remains intact!)
      unsubmittedDeltaSecondsRef.current = Math.max(0, unsubmittedDeltaSecondsRef.current - watchDelta)
    }
  }, [onCheckpoint])

  // Active watch-time tracker: increments strictly while video is actively playing and not buffering
  useEffect(() => {
    if (!isPlaying || isBuffering) return

    lastRealTimeRef.current = performance.now()

    const ticker = window.setInterval(() => {
      const video = videoRef.current
      if (!video || video.paused || video.seeking || isBuffering) {
        lastRealTimeRef.current = performance.now()
        return
      }

      const now = performance.now()
      const elapsedRealSec = (now - lastRealTimeRef.current) / 1000
      lastRealTimeRef.current = now

      // Safely clamp tick duration to avoid browser background-tab throttling bursts
      if (elapsedRealSec > 0 && elapsedRealSec <= 2) {
        totalWatchSecondsRef.current += elapsedRealSec
        unsubmittedDeltaSecondsRef.current += elapsedRealSec

        // Auto-complete only when cumulative watch threshold (>= 80% duration) is reached
        const total = duration || durationSeconds
        if (
          total > 0 &&
          !isCompleted &&
          !hasTriggeredCompleteRef.current &&
          totalWatchSecondsRef.current >= total * 0.80
        ) {
          hasTriggeredCompleteRef.current = true
          flushCheckpoint()
          onComplete()
        }
      }
    }, 250)

    return () => clearInterval(ticker)
  }, [isPlaying, isBuffering, duration, durationSeconds, isCompleted, flushCheckpoint, onComplete])

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
    setCurrentTime(videoRef.current.currentTime)
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

      if (e.code === 'Space') {
        e.preventDefault()
        togglePlay()
      } else if (e.code === 'ArrowLeft') {
        e.preventDefault()
        seekBy(-5)
      } else if (e.code === 'ArrowRight') {
        e.preventDefault()
        seekBy(5)
      } else if (e.code === 'KeyM') {
        e.preventDefault()
        toggleMute()
      } else if (e.code === 'KeyF') {
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
            if (videoRef.current && !Number.isNaN(videoRef.current.duration) && videoRef.current.duration > 0) {
              setDuration(videoRef.current.duration)
            }
          }}
          onSeeking={() => {
            lastRealTimeRef.current = performance.now()
          }}
          onSeeked={() => {
            lastRealTimeRef.current = performance.now()
          }}
          onWaiting={() => {
            setIsBuffering(true)
            lastRealTimeRef.current = performance.now()
          }}
          onPlaying={() => {
            setIsBuffering(false)
            setIsPlaying(true)
            lastRealTimeRef.current = performance.now()
          }}
          onPause={() => {
            setIsPlaying(false)
            flushCheckpoint()
          }}
          onEnded={() => {
            setIsPlaying(false)
            flushCheckpoint()
            const total = duration || durationSeconds
            if (!isCompleted && !hasTriggeredCompleteRef.current && totalWatchSecondsRef.current >= total * 0.80) {
              hasTriggeredCompleteRef.current = true
              onComplete()
            }
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

      {/* Big Center Play Button when Paused */}
      {!isPlaying && streamUrl && (
        <div
          onClick={togglePlay}
          className="absolute inset-0 flex items-center justify-center bg-black/20 cursor-pointer"
        >
          <div className="w-16 h-16 rounded-full bg-brand-600/90 hover:bg-brand-500 text-white flex items-center justify-center shadow-elevated transform group-hover:scale-105 transition-all">
            <Play className="w-7 h-7 fill-white ml-1" />
          </div>
        </div>
      )}

      {/* Custom Control Bar Overlay */}
      <div
        className={cn(
          'absolute bottom-0 inset-x-0 bg-gradient-to-t from-slate-950/95 via-slate-950/60 to-transparent p-4 transition-opacity duration-300 flex flex-col gap-2',
          showControls || !isPlaying ? 'opacity-100' : 'opacity-0 pointer-events-none'
        )}
      >
        {/* Scrubber / Progress Slider */}
        <div className="w-full flex items-center gap-2 group/scrub">
          <input
            type="range"
            min={0}
            max={duration || durationSeconds || 100}
            step={0.1}
            value={currentTime}
            onChange={handleSeekSlider}
            aria-label="Seek time scrubber"
            className="w-full h-1.5 bg-slate-700/60 rounded-lg appearance-none cursor-pointer accent-brand-500 hover:h-2 transition-all"
          />
        </div>

        {/* Playback Controls Row */}
        <div className="flex items-center justify-between text-xs font-medium text-slate-200">
          <div className="flex items-center gap-3">
            <button
              onClick={togglePlay}
              className="p-1 hover:text-white transition-colors cursor-pointer"
              aria-label={isPlaying ? 'Pause video' : 'Play video'}
            >
              {isPlaying ? <Pause className="w-4 h-4" /> : <Play className="w-4 h-4 fill-current" />}
            </button>

            <button
              onClick={() => seekBy(-5)}
              className="p-1 hover:text-white transition-colors cursor-pointer text-[10px] font-bold"
              aria-label="Seek backward 5 seconds"
              title="Seek -5s (Left Arrow)"
            >
              -5s
            </button>

            <button
              onClick={() => seekBy(5)}
              className="p-1 hover:text-white transition-colors cursor-pointer text-[10px] font-bold"
              aria-label="Seek forward 5 seconds"
              title="Seek +5s (Right Arrow)"
            >
              +5s
            </button>

            <div className="flex items-center gap-1 group/vol">
              <button
                onClick={toggleMute}
                className="p-1 hover:text-white transition-colors cursor-pointer"
                aria-label={isMuted ? 'Unmute' : 'Mute'}
                title="Mute / Unmute (M)"
              >
                {isMuted || volume === 0 ? <VolumeX className="w-4 h-4 text-rose-400" /> : <Volume2 className="w-4 h-4" />}
              </button>
              <input
                type="range"
                min={0}
                max={1}
                step={0.05}
                value={isMuted ? 0 : volume}
                onChange={handleVolumeChange}
                aria-label="Volume controller"
                className="w-14 h-1 bg-slate-700 rounded-lg appearance-none cursor-pointer accent-brand-400"
              />
            </div>

            <span className="text-[11px] text-slate-300 font-mono ml-2">
              {formatTime(currentTime)} / {formatTime(duration || durationSeconds)}
            </span>
          </div>

          <div className="flex items-center gap-3">
            {/* Playback Speed Selector */}
            <div className="flex items-center gap-1 bg-slate-900/80 px-2 py-0.5 rounded-md border border-slate-800">
              {[0.75, 1, 1.25, 1.5, 2].map((rate) => (
                <button
                  key={rate}
                  onClick={() => handleSpeedChange(rate)}
                  className={cn(
                    'px-1.5 py-0.5 rounded text-[10px] font-bold transition-all cursor-pointer',
                    playbackRate === rate
                      ? 'bg-brand-600 text-white'
                      : 'text-slate-400 hover:text-white'
                  )}
                >
                  {rate}x
                </button>
              ))}
            </div>

            <button
              onClick={toggleFullscreen}
              className="p-1 hover:text-white transition-colors cursor-pointer"
              aria-label="Toggle Fullscreen"
              title="Fullscreen (F)"
            >
              <Maximize className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
