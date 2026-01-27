package com.metacoding.user.users;

public class UserRequest {
    public record LoginDTO(
        String username,
        String password
    ) {
    }
}
