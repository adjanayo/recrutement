package com.example.recru;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.recru.auth.entities.Role;
import com.example.recru.auth.entities.Utilisateurs;
import com.example.recru.auth.repositories.RoleRepository;
import com.example.recru.auth.repositories.UtilisateurRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UtilisateurRepository utilisateurRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // Création des rôles
        Role roleUser = createRoleIfNotExists("ROLE_USER");
        Role roleAdmin = createRoleIfNotExists("ROLE_ADMIN");

        // Création de l'utilisateur admin
        createAdminUserIfNotExists(roleAdmin);
    }

    private Role createRoleIfNotExists(String roleName) {
        return roleRepository.findByNomRole(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setNomRole(roleName);
                    roleRepository.save(role);
                    System.out.println("✔ Rôle créé : " + roleName);
                    return role;
                });
    }

    private void createAdminUserIfNotExists(Role roleAdmin) {
        String adminUsername = "admin";
        if (utilisateurRepository.findByUsername(adminUsername).isEmpty()) {
            Utilisateurs admin = new Utilisateurs();
            admin.setNom("System");
            admin.setPrenom("Admin");
            admin.setUsername(adminUsername);
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("Admin@123")); // mot de passe sécurisé
            admin.setRole(roleAdmin);
            admin.setIsActive(true);

            utilisateurRepository.save(admin);
            System.out.println("✔ Utilisateur admin créé : " + adminUsername);
        }
    }
}
