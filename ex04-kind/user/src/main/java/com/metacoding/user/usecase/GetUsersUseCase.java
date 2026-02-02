package com.metacoding.user.usecase;

import com.metacoding.user.web.dto.UserResponse;

import java.util.List;

public interface GetUsersUseCase {
    List<UserResponse> findAll();
}



