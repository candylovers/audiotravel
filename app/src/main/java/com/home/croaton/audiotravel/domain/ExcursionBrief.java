package com.home.croaton.audiotravel.domain;

import java.util.UUID;

public class ExcursionBrief {
    private final UUID excursionId;
    private final String name;
    private final String thumbnailFilePath;

    public ExcursionBrief(UUID excursionId, String name, String thumbnailFilePath) {
        this.excursionId = excursionId;
        this.name = name;
        this.thumbnailFilePath = thumbnailFilePath;
    }

    public UUID getExcursionId() {
        return excursionId;
    }

    public String getName() {
        return name;
    }

    public String getThumbnailFilePath() {
        return thumbnailFilePath;
    }
}
