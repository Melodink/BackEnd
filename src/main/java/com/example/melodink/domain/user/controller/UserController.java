package com.example.melodink.domain.user.controller;

import com.example.melodink.domain.user.dto.request.*;
import com.example.melodink.domain.user.dto.response.DupCheckResponse;
import com.example.melodink.domain.user.dto.response.ShowInfoResponse;
import com.example.melodink.domain.user.entity.User;
import com.example.melodink.domain.user.service.UserService;
import com.example.melodink.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public ResponseEntity<ShowInfoResponse> getUserInfo(@AuthenticationPrincipal User user) {
        ShowInfoResponse response = userService.showUserInfo(user);
        ResponseEntity<ShowInfoResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        return responseEntity;
    }

    @GetMapping("/dup-check/verify-email")
    public ResponseEntity<DupCheckResponse> checkVerifyEmail(@RequestParam("email") String email) {
        DupCheckResponse res = userService.checkVerifyingEmail(email);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/dup-check/email")
    public ResponseEntity<DupCheckResponse> checkEmail(@RequestParam("email") String email) {
        DupCheckResponse res = userService.checkEmail(email);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/dup-check/nickname")
    public ResponseEntity<DupCheckResponse> checkNickname(@RequestParam("nickname") String nickname) {
        DupCheckResponse res = userService.checkNickname(nickname);
        return ResponseEntity.ok(res);
    }

    // 로컬 탈퇴
    @PostMapping("/deactivate/local")
    public ResponseEntity<Void> deactivate(@AuthenticationPrincipal CustomUserDetails me,
                                           @RequestBody DeactivateRequest requestDTO) {
        userService.deactivateUser(me.getUser().getId(), Duration.ofDays(30), requestDTO.password()); // 보관기간 정책
        return ResponseEntity.ok().build();
    }

    // 소셜 탈퇴
    @PostMapping("/deactivate/social")
    public ResponseEntity<Void> deactivate(@AuthenticationPrincipal CustomUserDetails me) {
        userService.deactivateUser(me.getUser().getId(), Duration.ofDays(30)); // 보관기간 정책
        return ResponseEntity.ok().build();
    }

    // 로컬 계정 복구
    @PostMapping("/account/recover")
    public ResponseEntity<?> recover(@RequestBody Map<String,String> body) {
        String email = body.get("email");
        userService.recoverAccount(email); // 소프트 삭제 → 활성 전환 + 상태 체크
        return ResponseEntity.ok(Map.of("status","OK"));
    }

    // 소셜계정 복구
    @PostMapping("/recover/social")
    public ResponseEntity<Void> socialRecover(@RequestBody SocialRecoverRequest request) {
        userService.recoverSocial(request.email(), request.provider());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/editpass")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                               @RequestBody PatchPasswordRequest requestDTO) {
        userService.editPassword(customUserDetails, requestDTO.currentPassword(), requestDTO.newPassword());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/changeimage")
    public ResponseEntity<Void> channgeProfileImage(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                    @RequestPart("file") MultipartFile file){
        userService.editImage(customUserDetails, file);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/editnickname")
    public ResponseEntity<Void> changeNickname(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                               @RequestBody PatchNicknameRequest requestDTO){
        userService.editNickname(customUserDetails, requestDTO.newNickname());
        return ResponseEntity.ok().build();
    }



}
