package com.nexarats.academy.learning.dto;

import java.util.UUID;

public class PlaybackCheckpointRequest {

    private int playbackPositionSeconds;
    private int watchTimeDeltaSeconds;
    private String idempotencyKey;

    public PlaybackCheckpointRequest() {}

    public PlaybackCheckpointRequest(int playbackPositionSeconds, int watchTimeDeltaSeconds, String idempotencyKey) {
        this.playbackPositionSeconds = playbackPositionSeconds;
        this.watchTimeDeltaSeconds = watchTimeDeltaSeconds;
        this.idempotencyKey = idempotencyKey;
    }

    public int getPlaybackPositionSeconds() { return playbackPositionSeconds; }
    public void setPlaybackPositionSeconds(int playbackPositionSeconds) { this.playbackPositionSeconds = playbackPositionSeconds; }
    public int getWatchTimeDeltaSeconds() { return watchTimeDeltaSeconds; }
    public void setWatchTimeDeltaSeconds(int watchTimeDeltaSeconds) { this.watchTimeDeltaSeconds = watchTimeDeltaSeconds; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
