package com.example.melodink.global.s3;

import com.example.melodink.domain.artist.dto.response.ProfileImageUploadResponse;
import com.example.melodink.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final S3Service s3Service;

    /**
     * POST /api/v1/files/presigned
     * Presigned URL 발급 (음원·영상·악보용 대용량 파일)
     *
     * 사용 흐름:
     * 1. 클라이언트 → 이 API로 presignedUrl + objectKey 수령
     * 2. 클라이언트 → presignedUrl로 S3에 직접 PUT 업로드
     * 3. 클라이언트 → objectKey를 작품 등록 API에 포함해서 전송
     */
    @PostMapping("/presigned")
    public ResponseEntity<PresignedUploadResponse> getPresignedUrl(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam S3FileType fileType,
            @RequestParam String filename,
            @RequestParam String contentType
    ) {
        PresignedUploadResponse response =
                s3Service.generatePresignedUrl(fileType, filename, contentType);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/files/profile-image
     * 프로필 이미지 서버 직접 업로드 (소용량, 즉시 CDN URL 반환)
     */
    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileImageUploadResponse> uploadProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart MultipartFile file
    ) {
        String cdnUrl = s3Service.upload(file, S3FileType.PROFILE_IMAGE);
        return ResponseEntity.ok(new ProfileImageUploadResponse(cdnUrl));
    }


}