package com.example.melodink.domain.artist.dto.request;

import com.example.melodink.domain.artist.entity.SkillType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SkillRequest(
    @NotNull
    SkillType skillType,

    @NotBlank
    @Size(max = 50)
    String name){
}