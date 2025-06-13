package com.salaire.service;

import com.salaire.entity.CalculSalaire;
import com.salaire.entity.User;
import com.salaire.repository.CalculSalaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des calculs de salaire
 */
@Service
@Transactional
public class CalculSalaireService {
    
    @Autowired
    private CalculSalaireRepository calculSalaireRepository;
    
    @Autowired
    private SalaireCalculatorService calculatorService;
    
    /**
     * Sauvegarde un nouveau calcul de salaire
     */
    public CalculSalaire sauvegarderCalcul(CalculSalaire calcul) {
        // Effectuer le calcul avant la sauvegarde
        calcul = calculatorService.calculerSalaireNet(calcul);
        
        // Sauvegarder en base
        return calculSalaireRepository.save(calcul);
    }
    
    /**
     * Trouve un calcul par son ID
     */
    @Transactional(readOnly = true)
    public Optional<CalculSalaire> trouverParId(Long id) {
        return calculSalaireRepository.findById(id);
    }
    
    /**
     * Trouve tous les calculs d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<CalculSalaire> trouverCalculsUtilisateur(User user) {
        return calculSalaireRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * Trouve les calculs d'un utilisateur avec pagination
     */
    @Transactional(readOnly = true)
    public Page<CalculSalaire> trouverCalculsUtilisateur(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return calculSalaireRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
    
    /**
     * Trouve les derniers calculs d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<CalculSalaire> trouverDerniersCalculs(User user, int limite) {
        Pageable pageable = PageRequest.of(0, limite);
        return calculSalaireRepository.findTopByUser(user, pageable);
    }
    
    /**
     * Compte le nombre de calculs d'un utilisateur
     */
    @Transactional(readOnly = true)
    public long compterCalculsUtilisateur(User user) {
        return calculSalaireRepository.countByUser(user);
    }
    
    /**
     * Supprime un calcul
     */
    public void supprimerCalcul(Long id) {
        calculSalaireRepository.deleteById(id);
    }
    
    /**
     * Supprime tous les calculs d'un utilisateur
     */
    public void supprimerTousCalculsUtilisateur(User user) {
        List<CalculSalaire> calculs = calculSalaireRepository.findByUserOrderByCreatedAtDesc(user);
        calculSalaireRepository.deleteAll(calculs);
    }
    
    /**
     * Trouve les calculs récents d'un utilisateur (dernières 24h)
     */
    @Transactional(readOnly = true)
    public List<CalculSalaire> trouverCalculsRecents(User user) {
        LocalDateTime hier = LocalDateTime.now().minusDays(1);
        return calculSalaireRepository.findByUserAndCreatedAtAfterOrderByCreatedAtDesc(user, hier);
    }
    
    /**
     * Trouve tous les calculs entre deux dates
     */
    @Transactional(readOnly = true)
    public List<CalculSalaire> trouverCalculsParPeriode(LocalDateTime debut, LocalDateTime fin) {
        return calculSalaireRepository.findByCreatedAtBetween(debut, fin);
    }
    
    /**
     * Effectue un nouveau calcul sans sauvegarde
     */
    public CalculSalaire effectuerCalcul(CalculSalaire calcul) {
        return calculatorService.calculerSalaireNet(calcul);
    }
    
    /**
     * Duplique un calcul existant avec de nouvelles données
     */
    public CalculSalaire dupliquerCalcul(Long calculId, CalculSalaire nouvellesDonnees) {
        Optional<CalculSalaire> calculExistant = trouverParId(calculId);
        if (calculExistant.isPresent()) {
            CalculSalaire original = calculExistant.get();
            
            // Copier les données de base depuis l'original
            nouvellesDonnees.setUser(original.getUser());
            nouvellesDonnees.setStatutMarital(original.getStatutMarital());
            nouvellesDonnees.setNombreParts(original.getNombreParts());
            
            // Effectuer le nouveau calcul
            return effectuerCalcul(nouvellesDonnees);
        }
        
        return nouvellesDonnees;
    }
}