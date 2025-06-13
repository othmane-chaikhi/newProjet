package com.salaire.controller.api;

import com.salaire.entity.CalculSalaire;
import com.salaire.entity.User;
import com.salaire.service.CalculSalaireService;
import com.salaire.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur REST pour les calculs de salaire
 */
@RestController
@RequestMapping("/api/salaire")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SalaireApiController {
    
    @Autowired
    private CalculSalaireService calculSalaireService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Calcule le salaire net (sans sauvegarde)
     */
    @PostMapping("/calculer")
    public ResponseEntity<?> calculerSalaire(@RequestBody CalculSalaire calcul) {
        try {
            // Initialiser les valeurs nulles
            if (calcul.getPrimes() == null) calcul.setPrimes(BigDecimal.ZERO);
            if (calcul.getIndemnites() == null) calcul.setIndemnites(BigDecimal.ZERO);
            if (calcul.getAvantagesNature() == null) calcul.setAvantagesNature(BigDecimal.ZERO);
            if (calcul.getHeuresSupplementaires() == null) calcul.setHeuresSupplementaires(BigDecimal.ZERO);
            if (calcul.getNombreParts() == null) calcul.setNombreParts(BigDecimal.valueOf(1.0));
            
            // Effectuer le calcul
            CalculSalaire resultat = calculSalaireService.effectuerCalcul(calcul);
            
            return ResponseEntity.ok(resultat);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors du calcul : " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Sauvegarde un calcul pour l'utilisateur connecté
     */
    @PostMapping("/sauvegarder")
    public ResponseEntity<?> sauvegarderCalcul(@RequestBody CalculSalaire calcul,
                                              Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Authentification requise"));
        }
        
        try {
            String username = authentication.getName();
            Optional<User> userOpt = userService.trouverParNomUtilisateur(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Utilisateur non trouvé"));
            }
            
            calcul.setUser(userOpt.get());
            
            // Initialiser les valeurs nulles
            if (calcul.getPrimes() == null) calcul.setPrimes(BigDecimal.ZERO);
            if (calcul.getIndemnites() == null) calcul.setIndemnites(BigDecimal.ZERO);
            if (calcul.getAvantagesNature() == null) calcul.setAvantagesNature(BigDecimal.ZERO);
            if (calcul.getHeuresSupplementaires() == null) calcul.setHeuresSupplementaires(BigDecimal.ZERO);
            if (calcul.getNombreParts() == null) calcul.setNombreParts(BigDecimal.valueOf(1.0));
            
            CalculSalaire resultat = calculSalaireService.sauvegarderCalcul(calcul);
            
            return ResponseEntity.ok(resultat);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la sauvegarde : " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Récupère l'historique des calculs de l'utilisateur connecté
     */
    @GetMapping("/historique")
    public ResponseEntity<?> obtenirHistorique(Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Authentification requise"));
        }
        
        try {
            String username = authentication.getName();
            Optional<User> userOpt = userService.trouverParNomUtilisateur(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Utilisateur non trouvé"));
            }
            
            List<CalculSalaire> calculs = calculSalaireService.trouverCalculsUtilisateur(userOpt.get());
            
            return ResponseEntity.ok(calculs);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la récupération : " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Récupère un calcul spécifique
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenirCalcul(@PathVariable Long id,
                                          Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Authentification requise"));
        }
        
        try {
            Optional<CalculSalaire> calculOpt = calculSalaireService.trouverParId(id);
            
            if (calculOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CalculSalaire calcul = calculOpt.get();
            String username = authentication.getName();
            
            // Vérifier que le calcul appartient à l'utilisateur
            if (!calcul.getUser().getUsername().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé"));
            }
            
            return ResponseEntity.ok(calcul);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la récupération : " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Supprime un calcul
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerCalcul(@PathVariable Long id,
                                            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Authentification requise"));
        }
        
        try {
            Optional<CalculSalaire> calculOpt = calculSalaireService.trouverParId(id);
            
            if (calculOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            CalculSalaire calcul = calculOpt.get();
            String username = authentication.getName();
            
            // Vérifier que le calcul appartient à l'utilisateur
            if (!calcul.getUser().getUsername().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès non autorisé"));
            }
            
            calculSalaireService.supprimerCalcul(id);
            
            return ResponseEntity.ok(Map.of("message", "Calcul supprimé avec succès"));
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la suppression : " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}