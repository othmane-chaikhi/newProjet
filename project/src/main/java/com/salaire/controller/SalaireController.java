package com.salaire.controller;

import com.salaire.entity.CalculSalaire;
import com.salaire.entity.User;
import com.salaire.service.CalculSalaireService;
import com.salaire.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Contrôleur pour la gestion des calculs de salaire
 */
@Controller
@RequestMapping("/salaire")
public class SalaireController {
    
    @Autowired
    private CalculSalaireService calculSalaireService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Affiche le formulaire de calcul
     */
    @GetMapping("/calculer")
    public String afficherFormulaireCalcul(Model model) {
        model.addAttribute("calcul", new CalculSalaire());
        model.addAttribute("titre", "Calculer votre salaire net");
        return "salaire/calculer";
    }
    
    /**
     * Traite le calcul de salaire
     */
    @PostMapping("/calculer")
    public String calculerSalaire(@Valid @ModelAttribute("calcul") CalculSalaire calcul,
                                 BindingResult result,
                                 Model model,
                                 Authentication authentication,
                                 @RequestParam(value = "sauvegarder", required = false) boolean sauvegarder) {
        
        if (result.hasErrors()) {
            model.addAttribute("titre", "Calculer votre salaire net");
            return "salaire/calculer";
        }
        
        // Initialiser les valeurs nulles à zéro
        if (calcul.getPrimes() == null) calcul.setPrimes(BigDecimal.ZERO);
        if (calcul.getIndemnites() == null) calcul.setIndemnites(BigDecimal.ZERO);
        if (calcul.getAvantagesNature() == null) calcul.setAvantagesNature(BigDecimal.ZERO);
        if (calcul.getHeuresSupplementaires() == null) calcul.setHeuresSupplementaires(BigDecimal.ZERO);
        if (calcul.getNombreParts() == null) calcul.setNombreParts(BigDecimal.valueOf(1.0));
        
        // Effectuer le calcul
        calcul = calculSalaireService.effectuerCalcul(calcul);
        
        // Sauvegarder si demandé et utilisateur connecté
        if (sauvegarder && authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<User> userOpt = userService.trouverParNomUtilisateur(username);
            if (userOpt.isPresent()) {
                calcul.setUser(userOpt.get());
                calcul = calculSalaireService.sauvegarderCalcul(calcul);
                model.addAttribute("message", "Calcul sauvegardé avec succès !");
            }
        }
        
        model.addAttribute("calcul", calcul);
        model.addAttribute("titre", "Résultat du calcul");
        return "salaire/resultat";
    }
    
    /**
     * Affiche l'historique des calculs
     */
    @GetMapping("/historique")
    public String afficherHistorique(Model model,
                                   Authentication authentication,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        Optional<User> userOpt = userService.trouverParNomUtilisateur(username);
        
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        Page<CalculSalaire> calculs = calculSalaireService.trouverCalculsUtilisateur(user, page, size);
        
        model.addAttribute("calculs", calculs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", calculs.getTotalPages());
        model.addAttribute("totalElements", calculs.getTotalElements());
        model.addAttribute("titre", "Historique des calculs");
        
        return "salaire/historique";
    }
    
    /**
     * Affiche le détail d'un calcul
     */
    @GetMapping("/detail/{id}")
    public String afficherDetailCalcul(@PathVariable Long id,
                                     Model model,
                                     Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Optional<CalculSalaire> calculOpt = calculSalaireService.trouverParId(id);
        if (calculOpt.isEmpty()) {
            return "redirect:/salaire/historique";
        }
        
        CalculSalaire calcul = calculOpt.get();
        
        // Vérifier que le calcul appartient à l'utilisateur connecté
        String username = authentication.getName();
        if (!calcul.getUser().getUsername().equals(username)) {
            return "redirect:/salaire/historique";
        }
        
        model.addAttribute("calcul", calcul);
        model.addAttribute("titre", "Détail du calcul");
        
        return "salaire/detail";
    }
    
    /**
     * Supprime un calcul
     */
    @PostMapping("/supprimer/{id}")
    public String supprimerCalcul(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Optional<CalculSalaire> calculOpt = calculSalaireService.trouverParId(id);
        if (calculOpt.isPresent()) {
            CalculSalaire calcul = calculOpt.get();
            
            // Vérifier que le calcul appartient à l'utilisateur connecté
            String username = authentication.getName();
            if (calcul.getUser().getUsername().equals(username)) {
                calculSalaireService.supprimerCalcul(id);
                redirectAttributes.addFlashAttribute("message", "Calcul supprimé avec succès !");
            }
        }
        
        return "redirect:/salaire/historique";
    }
    
    /**
     * Duplique un calcul existant
     */
    @GetMapping("/dupliquer/{id}")
    public String dupliquerCalcul(@PathVariable Long id,
                                 Model model,
                                 Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Optional<CalculSalaire> calculOpt = calculSalaireService.trouverParId(id);
        if (calculOpt.isEmpty()) {
            return "redirect:/salaire/historique";
        }
        
        CalculSalaire calculOriginal = calculOpt.get();
        
        // Vérifier que le calcul appartient à l'utilisateur connecté
        String username = authentication.getName();
        if (!calculOriginal.getUser().getUsername().equals(username)) {
            return "redirect:/salaire/historique";
        }
        
        // Créer une copie du calcul
        CalculSalaire nouveauCalcul = new CalculSalaire();
        nouveauCalcul.setSalaireBrut(calculOriginal.getSalaireBrut());
        nouveauCalcul.setPrimes(calculOriginal.getPrimes());
        nouveauCalcul.setIndemnites(calculOriginal.getIndemnites());
        nouveauCalcul.setAvantagesNature(calculOriginal.getAvantagesNature());
        nouveauCalcul.setHeuresSupplementaires(calculOriginal.getHeuresSupplementaires());
        nouveauCalcul.setStatutMarital(calculOriginal.getStatutMarital());
        nouveauCalcul.setNombreParts(calculOriginal.getNombreParts());
        
        model.addAttribute("calcul", nouveauCalcul);
        model.addAttribute("titre", "Nouveau calcul (copié)");
        model.addAttribute("message", "Calcul copié, vous pouvez modifier les valeurs");
        
        return "salaire/calculer";
    }
}