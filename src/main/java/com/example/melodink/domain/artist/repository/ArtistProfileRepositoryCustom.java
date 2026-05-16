package com.example.melodink.domain.artist.repository;

import com.example.melodink.domain.artist.dto.request.ArtistSearchRequest;
import com.example.melodink.domain.artist.entity.ArtistProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ArtistProfileRepositoryCustom {
    Page<ArtistProfile> searchArtists(ArtistSearchRequest condition, PageRequest pageable);
}
