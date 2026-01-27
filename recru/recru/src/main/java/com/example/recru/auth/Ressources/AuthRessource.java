package com.example.recru.auth.Ressources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.recru.auth.dtos.RegisterRequest;
import com.example.recru.auth.entities.Role;
import com.example.recru.auth.entities.Utilisateurs;
import com.example.recru.auth.repositories.RoleRepository;
import com.example.recru.auth.repositories.UtilisateurRepository;

@RestController
@RequestMapping("/auth")
public class AuthRessource {
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {

        if (this.utilisateurRepository.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Email already exists");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Utilisateurs user = new Utilisateurs();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(userRole);

        utilisateurRepository.save(user);

        return ResponseEntity.ok("User created");
    }
}
