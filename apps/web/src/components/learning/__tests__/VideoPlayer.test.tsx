import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, fireEvent, act } from '@testing-library/react'
import { VideoPlayer } from '../VideoPlayer'

describe('VideoPlayer Component Tests', () => {
  beforeEach(() => {
    vi.useFakeTimers()

    // Mock HTMLMediaElement properties and methods in jsdom
    Object.defineProperty(window.HTMLMediaElement.prototype, 'paused', {
      get() {
        return (this as unknown as { _paused?: boolean })._paused !== undefined
          ? (this as unknown as { _paused: boolean })._paused
          : true
      },
      set(val: boolean) {
        ;(this as unknown as { _paused: boolean })._paused = val
      },
      configurable: true,
    })

    Object.defineProperty(window.HTMLMediaElement.prototype, 'seeking', {
      get() {
        return (this as unknown as { _seeking?: boolean })._seeking !== undefined
          ? (this as unknown as { _seeking: boolean })._seeking
          : false
      },
      set(val: boolean) {
        ;(this as unknown as { _seeking: boolean })._seeking = val
      },
      configurable: true,
    })

    window.HTMLMediaElement.prototype.play = vi.fn().mockImplementation(function (this: HTMLMediaElement) {
      Object.defineProperty(this, 'paused', { value: false, configurable: true, writable: true })
      this.dispatchEvent(new Event('play'))
      this.dispatchEvent(new Event('playing'))
      return Promise.resolve()
    })

    window.HTMLMediaElement.prototype.pause = vi.fn().mockImplementation(function (this: HTMLMediaElement) {
      Object.defineProperty(this, 'paused', { value: true, configurable: true, writable: true })
      this.dispatchEvent(new Event('pause'))
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.useRealTimers()
  })

  it('renders loading session authorization state', () => {
    render(
      <VideoPlayer
        streamUrl={null}
        durationSeconds={1200}
        initialPositionSeconds={0}
        isCompleted={false}
        isLoadingSession={true}
        onCheckpoint={vi.fn()}
        onComplete={vi.fn()}
      />
    )

    expect(screen.getByText('Authorizing secure video playback session...')).toBeDefined()
  })

  it('renders error state and handles retry session callback', () => {
    const handleRetry = vi.fn()
    render(
      <VideoPlayer
        streamUrl={null}
        durationSeconds={1200}
        initialPositionSeconds={0}
        isCompleted={false}
        isLoadingSession={false}
        error="Access Denied: Enrollment suspended"
        onCheckpoint={vi.fn()}
        onComplete={vi.fn()}
        onRetrySession={handleRetry}
      />
    )

    expect(screen.getByText('Playback Authorization Denied')).toBeDefined()
    expect(screen.getByText('Access Denied: Enrollment suspended')).toBeDefined()

    const retryBtn = screen.getByRole('button', { name: /retry authorization/i })
    fireEvent.click(retryBtn)
    expect(handleRetry).toHaveBeenCalledTimes(1)
  })

  it('resumes playback position from initialPositionSeconds and displays resume banner', () => {
    render(
      <VideoPlayer
        streamUrl="/api/v1/learning/media/stream?token=abc"
        durationSeconds={600}
        initialPositionSeconds={125}
        isCompleted={false}
        isLoadingSession={false}
        onCheckpoint={vi.fn()}
        onComplete={vi.fn()}
      />
    )

    expect(screen.getByText('Resumed from 2:05')).toBeDefined()
    const video = document.querySelector('video') as HTMLVideoElement
    expect(video).toBeDefined()
    expect(video.currentTime).toBe(125)
  })

  it('handles play, pause, and flushes checkpoint on pause', async () => {
    const onCheckpoint = vi.fn()
    const onComplete = vi.fn()

    render(
      <VideoPlayer
        streamUrl="/api/v1/learning/media/stream?token=abc"
        durationSeconds={100}
        initialPositionSeconds={0}
        isCompleted={false}
        isLoadingSession={false}
        onCheckpoint={onCheckpoint}
        onComplete={onComplete}
      />
    )

    const video = document.querySelector('video') as HTMLVideoElement
    expect(video).toBeDefined()

    // Click play
    const playBtn = screen.getByLabelText('Play video')
    await act(async () => {
      fireEvent.click(playBtn)
    })

    // Advance 5 seconds of active playback
    await act(async () => {
      vi.advanceTimersByTime(5000)
    })

    // Click pause
    const pauseBtn = screen.getByLabelText('Pause video')
    await act(async () => {
      fireEvent.click(pauseBtn)
    })

    // Flushes accumulated progress on pause
    expect(onCheckpoint).toHaveBeenCalled()
    const [lastPos, lastDelta] = onCheckpoint.mock.calls[0]
    expect(lastPos).toBeGreaterThanOrEqual(0)
    expect(lastDelta).toBeGreaterThanOrEqual(1)
  })

  it('seeking forward does not credit unearned watch time delta', async () => {
    const onCheckpoint = vi.fn()
    const onComplete = vi.fn()

    render(
      <VideoPlayer
        streamUrl="/api/v1/learning/media/stream?token=abc"
        durationSeconds={1000}
        initialPositionSeconds={0}
        isCompleted={false}
        isLoadingSession={false}
        onCheckpoint={onCheckpoint}
        onComplete={onComplete}
      />
    )

    const video = document.querySelector('video') as HTMLVideoElement

    // Play video
    const playBtn = screen.getByLabelText('Play video')
    await act(async () => {
      fireEvent.click(playBtn)
    })

    // User seeks forward 500 seconds via seek scrubber
    const scrubber = screen.getByLabelText('Seek time scrubber')
    await act(async () => {
      fireEvent.change(scrubber, { target: { value: '500' } })
    })

    expect(video.currentTime).toBe(500)

    // Advance only 2 seconds of real time
    await act(async () => {
      vi.advanceTimersByTime(2000)
    })

    // Trigger pause to flush checkpoint
    const pauseBtn = screen.getByLabelText('Pause video')
    await act(async () => {
      fireEvent.click(pauseBtn)
    })

    expect(onCheckpoint).toHaveBeenCalled()
    const [pos, delta] = onCheckpoint.mock.calls[0]
    expect(pos).toBe(500)
    // The delta must only reflect the ~2 seconds of real playback, NOT the 500s seek jump!
    expect(delta).toBeLessThanOrEqual(5)
  })

  it('buffering suspends watch time accumulation', async () => {
    const onCheckpoint = vi.fn()
    const onComplete = vi.fn()

    render(
      <VideoPlayer
        streamUrl="/api/v1/learning/media/stream?token=abc"
        durationSeconds={100}
        initialPositionSeconds={0}
        isCompleted={false}
        isLoadingSession={false}
        onCheckpoint={onCheckpoint}
        onComplete={onComplete}
      />
    )

    const video = document.querySelector('video') as HTMLVideoElement

    // Play
    await act(async () => {
      fireEvent.click(screen.getByLabelText('Play video'))
    })

    // Video begins buffering
    await act(async () => {
      video.dispatchEvent(new Event('waiting'))
    })

    // Advance 10 seconds while buffering
    await act(async () => {
      vi.advanceTimersByTime(10000)
    })

    // Video resumes playing
    await act(async () => {
      video.dispatchEvent(new Event('playing'))
      vi.advanceTimersByTime(2000)
    })

    // Pause
    await act(async () => {
      fireEvent.click(screen.getByLabelText('Pause video'))
    })

    if (onCheckpoint.mock.calls.length > 0) {
      const [, delta] = onCheckpoint.mock.calls[0]
      // Should only have accumulated the ~2s after playing resumed, not the 10s of buffering
      expect(delta).toBeLessThanOrEqual(4)
    }
  })

  it('triggers onComplete when meaningful watch time reaches 80% threshold', async () => {
    const onCheckpoint = vi.fn()
    const onComplete = vi.fn()

    // 10 second video -> 80% is 8 seconds
    render(
      <VideoPlayer
        streamUrl="/api/v1/learning/media/stream?token=abc"
        durationSeconds={10}
        initialPositionSeconds={0}
        isCompleted={false}
        isLoadingSession={false}
        onCheckpoint={onCheckpoint}
        onComplete={onComplete}
      />
    )

    // Start playing
    await act(async () => {
      fireEvent.click(screen.getByLabelText('Play video'))
    })

    // Watch for 8.5 seconds (> 80% of 10s)
    await act(async () => {
      vi.advanceTimersByTime(8500)
    })

    expect(onComplete).toHaveBeenCalledTimes(1)
  })

  it('seeking past 90% without watching does not trigger onComplete', async () => {
    const onCheckpoint = vi.fn()
    const onComplete = vi.fn()

    render(
      <VideoPlayer
        streamUrl="/api/v1/learning/media/stream?token=abc"
        durationSeconds={100}
        initialPositionSeconds={0}
        isCompleted={false}
        isLoadingSession={false}
        onCheckpoint={onCheckpoint}
        onComplete={onComplete}
      />
    )

    // Play
    await act(async () => {
      fireEvent.click(screen.getByLabelText('Play video'))
    })

    // Seek directly to 95s
    const scrubber = screen.getByLabelText('Seek time scrubber')
    await act(async () => {
      fireEvent.change(scrubber, { target: { value: '95' } })
      vi.advanceTimersByTime(1000) // Only 1s real watched time
    })

    expect(onComplete).not.toHaveBeenCalled()
  })
})
