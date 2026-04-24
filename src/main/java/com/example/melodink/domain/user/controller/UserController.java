package com.example.melodink.domain.user.controller;

import com.example.melodink.domain.user.dto.request.SignUpRequest;
import com.example.melodink.domain.user.dto.response.ShowInfoResponse;
import com.example.melodink.domain.user.entity.User;
import com.example.melodink.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public ResponseEntity<ShowInfoResponse> getUserInfo(@AuthenticationPrincipal User user) {
        ShowInfoResponse response = userService.showUserInfo(user);
        ResponseEntity<ShowInfoResponse> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
        return responseEntity;
    }

//    @PostMapping("signup")
//    public ResponseEntity<User> signup(@RequestBody SignUpRequest request) {
//
//
//
//    }


}
