package com.example.melodink.domain.user.repository;

import java.util.UUID;

public interface UserPublicIdProjection {
    Long getId();
    UUID getPublicId();
}
