package com.metacoding.user.users;

public class UserResponse {
    public record DTO(
        int id,
        String username,
        String email
    ) {
        public DTO(User user) {
            this(user.getId(), user.getUsername(), user.getEmail());
        }
    }
}
