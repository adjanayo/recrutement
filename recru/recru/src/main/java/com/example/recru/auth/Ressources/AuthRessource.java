/* 
package com.example.recru.auth.ressources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.recru.auth.dtos.AuthResponse;
import com.example.recru.auth.dtos.LoginRequest;
import com.example.recru.auth.dtos.RegisterRequest;
import com.example.recru.auth.entities.Role;
import com.example.recru.auth.entities.Utilisateurs;
import com.example.recru.auth.repositories.RoleRepository;
import com.example.recru.auth.repositories.UtilisateurRepository;
import com.example.recru.auth.services.JwtService;

@RestController
@RequestMapping("/auth")
public class AuthRessource {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    // Register
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (utilisateurRepository.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        if (utilisateurRepository.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        Role userRole = roleRepository.findByNomRole("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Utilisateurs user = new Utilisateurs();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(userRole);
        user.setIsActive(true);

        utilisateurRepository.save(user);

        return ResponseEntity.ok("User created");
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Utilisateurs user = utilisateurRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid password");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        AuthResponse response = new AuthResponse(accessToken, refreshToken, user.getUsername(),
                user.getRole().getNomRole());

        return ResponseEntity.ok(response);
    }

    // Refresh token
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody String refreshToken) {
        String token = refreshToken.replace("Bearer ", "");
        String username = jwtService.extractUsername(token);
        Utilisateurs user = utilisateurRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtService.validateToken(token, user)) {
            return ResponseEntity.badRequest().body("Invalid refresh token");
        }

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        AuthResponse response = new AuthResponse(newAccessToken, newRefreshToken, user.getUsername(),
                user.getRole().getNomRole());

        return ResponseEntity.ok(response);
    }
}

*/

/**/
package com.example.recru.auth.ressources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.recru.auth.dtos.AuthResponse;
import com.example.recru.auth.dtos.LoginRequest;
import com.example.recru.auth.dtos.RegisterRequest;
import com.example.recru.auth.entities.Role;
import com.example.recru.auth.entities.Utilisateurs;
import com.example.recru.auth.repositories.RoleRepository;
import com.example.recru.auth.repositories.UtilisateurRepository;
import com.example.recru.auth.services.JwtService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthRessource {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    // ---------------- Register ----------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (utilisateurRepository.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        if (utilisateurRepository.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        Role userRole = roleRepository.findByNomRole("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Utilisateurs user = new Utilisateurs();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(userRole);
        user.setIsActive(true);

        utilisateurRepository.save(user);

        return ResponseEntity.ok("User created");
    }

    // ---------------- Login ----------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        Utilisateurs user = utilisateurRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid password");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // ✅ Création des cookies HttpOnly
        ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                .secure(true)          // HTTPS seulement
                .path("/")
                .maxAge(jwtService.getJwtExpirationMs() / 1000)
                .sameSite("Strict")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtService.getJwtExpirationMs() * 10 / 1000)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        AuthResponse authResponse = new AuthResponse(accessToken, refreshToken, user.getUsername(),
                user.getRole().getNomRole());

        return ResponseEntity.ok(authResponse);
    }

    // ---------------- Logout ----------------
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie deleteAccess = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        ResponseCookie deleteRefresh = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefresh.toString());

        return ResponseEntity.ok("Logged out successfully");
    }

    // ---------------- Profile ----------------
    @GetMapping("/profile")
    public ResponseEntity<?> profile(@CookieValue(name = "access_token", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        String username;
        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }

        Utilisateurs user = utilisateurRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(user);
    }

    // ---------------- Refresh Token ----------------
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refresh_token", required = false) String refreshToken,
                                          HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(401).body("Refresh token missing");
        }

        String username;
        try {
            username = jwtService.extractUsername(refreshToken);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid refresh token");
        }

        Utilisateurs user = utilisateurRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtService.validateToken(refreshToken, user)) {
            return ResponseEntity.badRequest().body("Invalid refresh token");
        }

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        ResponseCookie accessCookie = ResponseCookie.from("access_token", newAccessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtService.getJwtExpirationMs() / 1000)
                .sameSite("Strict")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtService.getJwtExpirationMs() * 10 / 1000)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        AuthResponse authResponse = new AuthResponse(newAccessToken, newRefreshToken, user.getUsername(),
                user.getRole().getNomRole());

        return ResponseEntity.ok(authResponse);
    }
}
