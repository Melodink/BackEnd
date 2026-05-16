package com.example.melodink.domain.community.repository;

import com.example.melodink.domain.community.dto.request.PostSearchRequest;
import com.example.melodink.domain.community.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface PostRepositoryCustom {
    Page<Post> searchPosts(PostSearchRequest condition, PageRequest pageable);
}

