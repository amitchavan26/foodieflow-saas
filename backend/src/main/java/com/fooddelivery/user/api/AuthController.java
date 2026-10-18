package com.fooddelivery.user.api;

import com.fooddelivery.user.api.dto.AuthResponse;
import com.fooddelivery.user.api.dto.LoginRequest;
import com.fooddelivery.user.api.dto.RegisterRequest;
import com.fooddelivery.user.domain.User;
import com.fooddelivery.user.domain.UserStatus;
import com.fooddelivery.user.infrastructure.UserRepository;
import com.fooddelivery.common.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Collections;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole(User.UserRole.valueOf(request.role()));
        user.setStatus(UserStatus.ACTIVE);
        
        userRepository.save(user);

        String token = generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // 1. Authenticate using Spring Security (checks email & BCrypt password)
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. If successful, fetch user and generate JWT
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        String token = generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getRole().name()));
    }

    // Helper to generate token with correct authorities
    private String generateToken(User user) {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPasswordHash(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        return jwtService.generateToken(userDetails);
    }
}