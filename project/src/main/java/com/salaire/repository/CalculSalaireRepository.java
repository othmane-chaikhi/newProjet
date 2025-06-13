package com.salaire.repository;

import com.salaire.entity.CalculSalaire;
import com.salaire.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour la gestion des calculs de salaire
 */
@Repository
public interface CalculSalaireRepository extends JpaRepository<CalculSalaire, Long> {
    
    /**
     * Trouve tous les calculs d'un utilisateur, triés par date de création décroissante
     */
    List<CalculSalaire> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Trouve les calculs d'un utilisateur avec pagination
     */
    Page<CalculSalaire> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    /**
     * Trouve les calculs d'un utilisateur créés après une certaine date
     */
    List<CalculSalaire> findByUserAndCreatedAtAfterOrderByCreatedAtDesc(User user, LocalDateTime date);
    
    /**
     * Compte le nombre de calculs d'un utilisateur
     */
    long countByUser(User user);
    
    /**
     * Trouve les derniers calculs d'un utilisateur (limite)
     */
    @Query("SELECT c FROM CalculSalaire c WHERE c.user = :user ORDER BY c.createdAt DESC")
    List<CalculSalaire> findTopByUser(@Param("user") User user, Pageable pageable);
    
    /**
     * Trouve tous les calculs créés entre deux dates
     */
    @Query("SELECT c FROM CalculSalaire c WHERE c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    List<CalculSalaire> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
}