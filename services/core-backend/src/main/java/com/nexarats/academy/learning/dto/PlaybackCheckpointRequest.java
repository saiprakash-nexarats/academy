package com.nexarats.academy.learning.dto;

public class PlaybackCheckpointRequest {

    private int playbackPositionSeconds;
    private int watchTimeDeltaSeconds;
    private String idempotencyKey;
    private String sessionToken;

    public PlaybackCheckpointRequest() {}

    public PlaybackCheckpointRequest(int playbackPositionSeconds, int watchTimeDeltaSeconds, String idempotencyKey) {
        this.playbackPositionSeconds = playbackPositionSeconds;
        this.watchTimeDeltaSeconds = watchTimeDeltaSeconds;
        this.idempotencyKey = idempotencyKey;
    }

    public PlaybackCheckpointRequest(int playbackPositionSeconds, int watchTimeDeltaSeconds, String idempotencyKey, String sessionToken) {
        this.playbackPositionSeconds = playbackPositionSeconds;
        this.watchTimeDeltaSeconds = watchTimeDeltaSeconds;
        this.idempotencyKey = idempotencyKey;
        this.sessionToken = sessionToken;
    }

    public int getPlaybackPositionSeconds() { return playbackPositionSeconds; }
    public void setPlaybackPositionSeconds(int playbackPositionSeconds) { this.playbackPositionSeconds = playbackPositionSeconds; }
    public int getWatchTimeDeltaSeconds() { return watchTimeDeltaSeconds; }
    public void setWatchTimeDeltaSeconds(int watchTimeDeltaSeconds) { this.watchTimeDeltaSeconds = watchTimeDeltaSeconds; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
}
