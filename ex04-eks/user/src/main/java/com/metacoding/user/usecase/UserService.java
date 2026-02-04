package com.metacoding.user.usecase;

import com.metacoding.user.core.handler.ex.Exception404;
import com.metacoding.user.core.util.JwtUtil;
import com.metacoding.user.domain.user.User;
import com.metacoding.user.repository.UserRepository;
import com.metacoding.user.web.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.metacoding.user.core.util.JwtUtil.TOKEN_PREFIX;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserService implements GetUserUseCase, GetUsersUseCase, LoginUseCase {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public UserResponse findById(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception404("회원 정보를 찾을 수 없습니다."));
        return UserResponse.from(user);
    }

    @Override
    public List<UserResponse> findAll() {
        List<User> users = userRepository.findAll();
        return users.stream()
            .map(UserResponse::from)
            .toList();
    }

    @Override
    @Transactional
    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new Exception404("유저네임을 찾을 수 없습니다."));
        user.passwordCheck(password);
        String token = jwtUtil.create(user.getId(), user.getUsername());
        return TOKEN_PREFIX + token;
    }
}




