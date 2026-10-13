package com.nexarats.academy.learning.dto;

import java.time.Instant;
import java.util.UUID;

public class PlaybackSessionResponse {

    private String sessionToken;
    private String streamUrl;
    private UUID lessonId;
    private int durationSeconds;
    private int lastPlaybackPositionSeconds;
    private Instant expiresAt;

    public PlaybackSessionResponse() {}

    public PlaybackSessionResponse(String sessionToken, String streamUrl, UUID lessonId,
                                   int durationSeconds, int lastPlaybackPositionSeconds, Instant expiresAt) {
        this.sessionToken = sessionToken;
        this.streamUrl = streamUrl;
        this.lessonId = lessonId;
        this.durationSeconds = durationSeconds;
        this.lastPlaybackPositionSeconds = lastPlaybackPositionSeconds;
        this.expiresAt = expiresAt;
    }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
    public String getStreamUrl() { return streamUrl; }
    public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }
    public UUID getLessonId() { return lessonId; }
    public void setLessonId(UUID lessonId) { this.lessonId = lessonId; }
    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
    public int getLastPlaybackPositionSeconds() { return lastPlaybackPositionSeconds; }
    public void setLastPlaybackPositionSeconds(int lastPlaybackPositionSeconds) { this.lastPlaybackPositionSeconds = lastPlaybackPositionSeconds; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
