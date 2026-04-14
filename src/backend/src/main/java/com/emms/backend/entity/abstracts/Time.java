package com.emms.backend.entity.abstracts;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Time {

    @Column(name = "duration", nullable = false)
    protected Long duration = 0L;

    public Long getDuration() {
        return duration == null ? 0L : duration;
    }

    public void setDuration(Long duration) {
        if (duration == null) {
            this.duration = 0L;
            return;
        }

        if (duration < 0) {
            throw new IllegalArgumentException("duration không được âm");
        }

        this.duration = duration;
    }
}