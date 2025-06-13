package com.salaire.service;

import com.salaire.entity.User;
import com.salaire.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des utilisateurs
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Enregistre un nouvel utilisateur
     */
    public User enregistrerUtilisateur(User user) {
        // Encoder le mot de passe
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Trouve un utilisateur par son nom d'utilisateur
     */
    @Transactional(readOnly = true)
    public Optional<User> trouverParNomUtilisateur(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Trouve un utilisateur par son email
     */
    @Transactional(readOnly = true)
    public Optional<User> trouverParEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Trouve un utilisateur par nom d'utilisateur ou email
     */
    @Transactional(readOnly = true)
    public Optional<User> trouverParIdentifiant(String identifier) {
        return userRepository.findByUsernameOrEmail(identifier);
    }
    
    /**
     * Trouve un utilisateur par son ID
     */
    @Transactional(readOnly = true)
    public Optional<User> trouverParId(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * Trouve tous les utilisateurs
     */
    @Transactional(readOnly = true)
    public List<User> trouverTousLesUtilisateurs() {
        return userRepository.findAll();
    }
    
    /**
     * Vérifie si un nom d'utilisateur existe
     */
    @Transactional(readOnly = true)
    public boolean existeParNomUtilisateur(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * Vérifie si un email existe
     */
    @Transactional(readOnly = true)
    public boolean existeParEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Met à jour un utilisateur
     */
    public User mettreAJourUtilisateur(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    /**
     * Change le mot de passe d'un utilisateur
     */
    public void changerMotDePasse(User user, String nouveauMotDePasse) {
        user.setPassword(passwordEncoder.encode(nouveauMotDePasse));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    /**
     * Vérifie si le mot de passe actuel est correct
     */
    public boolean verifierMotDePasse(User user, String motDePasse) {
        return passwordEncoder.matches(motDePasse, user.getPassword());
    }
    
    /**
     * Supprime un utilisateur
     */
    public void supprimerUtilisateur(Long userId) {
        userRepository.deleteById(userId);
    }
    
    /**
     * Compte le nombre total d'utilisateurs
     */
    @Transactional(readOnly = true)
    public long compterUtilisateurs() {
        return userRepository.count();
    }
    
    /**
     * Valide les données d'un utilisateur avant enregistrement
     */
    public void validerUtilisateur(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom d'utilisateur est obligatoire");
        }
        
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("L'email est obligatoire");
        }
        
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caractères");
        }
        
        if (existeParNomUtilisateur(user.getUsername())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur existe déjà");
        }
        
        if (existeParEmail(user.getEmail())) {
            throw new IllegalArgumentException("Cet email existe déjà");
        }
    }
}