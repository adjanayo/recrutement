package com.example.recru.auth.repositories;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.recru.auth.entities.Utilisateurs;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateurs, Long> {
Optional<Utilisateurs> findByEmail(String email);
Optional<Utilisateurs> findByUsername(String username);
}