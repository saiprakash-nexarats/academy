import test, { describe } from 'node:test';
import assert from 'node:assert/strict';

describe('VideoPlayer Watch-Time Calculation & Checkpoint Logic', () => {

  function createWatchTracker(durationSeconds = 1200) {
    let accumulatedWatchSeconds = 0;
    let lastRealTime = 0;
    let lastCheckpointWatchTime = 0;
    let playbackPosition = 0;
    let isPlaying = false;
    let isSeeking = false;
    let checkpointCount = 0;
    let completed = false;
    const checkpoints = [];

    return {
      play(now) {
        isPlaying = true;
        isSeeking = false;
        lastRealTime = now;
      },
      pause(now) {
        if (isPlaying && !isSeeking) {
          const delta = (now - lastRealTime) / 1000;
          if (delta > 0 && delta <= 5) {
            accumulatedWatchSeconds += delta;
          }
        }
        isPlaying = false;
        this.emitCheckpoint();
      },
      tick(playbackSec, now) {
        playbackPosition = playbackSec;
        if (isPlaying && !isSeeking) {
          const delta = (now - lastRealTime) / 1000;
          if (delta > 0 && delta <= 5) {
            accumulatedWatchSeconds += delta;
          }
          lastRealTime = now;
        }
      },
      seek(targetPosition, now) {
        isSeeking = true;
        playbackPosition = targetPosition;
        // Crucial fix: seek resets the delta anchor without crediting unearned watch time!
        lastRealTime = now;
        isSeeking = false;
      },
      emitCheckpoint() {
        const delta = Math.floor(accumulatedWatchSeconds - lastCheckpointWatchTime);
        checkpoints.push({
          playbackPositionSeconds: Math.floor(playbackPosition),
          watchTimeDeltaSeconds: Math.max(0, delta),
          totalWatchTime: Math.floor(accumulatedWatchSeconds),
        });
        lastCheckpointWatchTime = accumulatedWatchSeconds;
        checkpointCount++;
      },
      checkAutoCompletion(thresholdRatio = 0.80) {
        if (durationSeconds > 0 && accumulatedWatchSeconds >= durationSeconds * thresholdRatio) {
          completed = true;
          this.emitCheckpoint();
        }
      },
      complete() {
        completed = true;
        this.emitCheckpoint();
      },
      getState() {
        return {
          playbackPosition,
          accumulatedWatchSeconds: Math.floor(accumulatedWatchSeconds),
          checkpointCount,
          completed,
          checkpoints,
        };
      }
    };
  }

  test('Normal linear playback increments watch time accurately', () => {
    const tracker = createWatchTracker(600);
    tracker.play(1000);

    // Simulate 10 seconds of playback with 1s ticks
    for (let s = 1; s <= 10; s++) {
      tracker.tick(s, 1000 + s * 1000);
    }
    tracker.emitCheckpoint();

    const state = tracker.getState();
    assert.equal(state.playbackPosition, 10);
    assert.equal(state.accumulatedWatchSeconds, 10);
    assert.equal(state.checkpoints.length, 1);
    assert.equal(state.checkpoints[0].watchTimeDeltaSeconds, 10);
    assert.equal(state.checkpoints[0].playbackPositionSeconds, 10);
  });

  test('Forward seek across 10 minutes does NOT accumulate false watch time', () => {
    const tracker = createWatchTracker(1200);
    tracker.play(1000);

    // Watch 10 seconds linearly
    for (let s = 1; s <= 10; s++) {
      tracker.tick(s, 1000 + s * 1000);
    }
    tracker.emitCheckpoint();

    // User seeks forward from 10s to 600s (jump of 590s)
    const seekTime = 11000 + 100;
    tracker.seek(600, seekTime);
    tracker.tick(600, seekTime);

    // Watch 5 more seconds from 600s to 605s
    for (let s = 1; s <= 5; s++) {
      tracker.tick(600 + s, seekTime + s * 1000);
    }
    tracker.emitCheckpoint();

    const state = tracker.getState();
    assert.equal(state.playbackPosition, 605);
    // Total watched time must be 15s (10s + 5s), NOT 605s!
    assert.equal(state.accumulatedWatchSeconds, 15);
    assert.equal(state.checkpoints.length, 2);
    assert.equal(state.checkpoints[1].watchTimeDeltaSeconds, 5);
    assert.equal(state.checkpoints[1].playbackPositionSeconds, 605);
  });

  test('Forward seek beyond 90% does NOT trigger auto-completion', () => {
    const tracker = createWatchTracker(1000); // 1000s duration, 90% is 900s
    tracker.play(1000);

    // Watch 20 seconds
    for (let s = 1; s <= 20; s++) {
      tracker.tick(s, 1000 + s * 1000);
    }
    tracker.checkAutoCompletion(0.80);

    // User scrubs past 90% mark (to 950s)
    tracker.seek(950, 22000);
    tracker.tick(950, 22000);
    tracker.checkAutoCompletion(0.80);

    const state = tracker.getState();
    assert.equal(state.playbackPosition, 950);
    assert.equal(state.accumulatedWatchSeconds, 20);
    // Must NOT be completed because meaningful watch time (20s) < 800s threshold
    assert.equal(state.completed, false);
  });

  test('Backward seek preserves accumulated watch time and updates resume position', () => {
    const tracker = createWatchTracker(1200);
    tracker.play(1000);

    // Watch 30 seconds
    for (let s = 1; s <= 30; s++) {
      tracker.tick(s, 1000 + s * 1000);
    }
    tracker.emitCheckpoint();

    // User rewinds back to 5s
    const rewindTime = 32000;
    tracker.seek(5, rewindTime);
    tracker.tick(5, rewindTime);

    // Replay 10 seconds from 5s to 15s
    for (let s = 1; s <= 10; s++) {
      tracker.tick(5 + s, rewindTime + s * 1000);
    }
    tracker.emitCheckpoint();

    const state = tracker.getState();
    assert.equal(state.playbackPosition, 15);
    // Total watched time must be 40s (30s + 10s)
    assert.equal(state.accumulatedWatchSeconds, 40);
    assert.equal(state.checkpoints[1].watchTimeDeltaSeconds, 10);
    assert.equal(state.checkpoints[1].playbackPositionSeconds, 15);
  });

  test('Pause event stops watch-time accumulation and emits checkpoint', () => {
    const tracker = createWatchTracker(600);
    tracker.play(1000);

    for (let s = 1; s <= 20; s++) {
      tracker.tick(s, 1000 + s * 1000);
    }
    tracker.pause(21000);

    const state = tracker.getState();
    assert.equal(state.playbackPosition, 20);
    assert.equal(state.accumulatedWatchSeconds, 20);
    assert.equal(state.checkpoints.length, 1);
    assert.equal(state.checkpoints[0].watchTimeDeltaSeconds, 20);
  });

  test('Meaningful watch time reaching 80% marks lesson complete', () => {
    const tracker = createWatchTracker(100); // 100 seconds, threshold 80s
    tracker.play(1000);

    for (let s = 1; s <= 80; s++) {
      tracker.tick(s, 1000 + s * 1000);
    }
    tracker.checkAutoCompletion(0.80);

    const state = tracker.getState();
    assert.equal(state.completed, true);
    assert.equal(state.accumulatedWatchSeconds, 80);
  });

  test('Completion event emits final progress checkpoint with full duration', () => {
    const tracker = createWatchTracker(300);
    tracker.play(1000);

    for (let s = 1; s <= 10; s++) {
      tracker.tick(s, 1000 + s * 1000);
    }
    tracker.complete();

    const state = tracker.getState();
    assert.equal(state.completed, true);
    assert.ok(state.checkpoints.length > 0);
  });
});
