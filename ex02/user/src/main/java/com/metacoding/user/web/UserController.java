package com.metacoding.user.web;

import com.metacoding.user.usecase.*;
import com.metacoding.user.web.dto.*;
import com.metacoding.user.core.util.Resp;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequiredArgsConstructor
@RestController
public class UserController {
    private final GetUserUseCase getUserUseCase;
    private final GetAllUsersUseCase getAllUsersUseCase;
    private final LoginUseCase loginUseCase;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest requestDTO) {
        return Resp.ok(loginUseCase.login(requestDTO.username(), requestDTO.password()));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUser(@PathVariable("userId") int userId) {
        UserResponse response = getUserUseCase.findById(userId);
        return Resp.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<UserResponse> responses = getAllUsersUseCase.findAll();
        return Resp.ok(responses);
    }
}


















