package com.example.recru.auth.repositories;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.recru.auth.entities.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByNomRole(String nomRole);
    
}
