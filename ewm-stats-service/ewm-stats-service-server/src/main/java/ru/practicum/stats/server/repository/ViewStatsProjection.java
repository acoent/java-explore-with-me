package ru.practicum.stats.server.repository;

public interface ViewStatsProjection {

    String getApp();

    String getUri();

    Long getHits();
}
