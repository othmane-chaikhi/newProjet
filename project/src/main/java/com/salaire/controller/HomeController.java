package com.salaire.controller;

import com.salaire.entity.User;
import com.salaire.service.CalculSalaireService;
import com.salaire.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur pour les pages d'accueil et principales
 */
@Controller
public class HomeController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CalculSalaireService calculSalaireService;
    
    /**
     * Page d'accueil
     */
    @GetMapping("/")
    public String accueil(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            userService.trouverParNomUtilisateur(username).ifPresent(user -> {
                model.addAttribute("user", user);
                model.addAttribute("nombreCalculs", calculSalaireService.compterCalculsUtilisateur(user));
                model.addAttribute("derniersCalculs", calculSalaireService.trouverDerniersCalculs(user, 5));
            });
        }
        
        model.addAttribute("titre", "Calculateur de Salaire Net 2025");
        return "index";
    }
    
    /**
     * Page de connexion
     */
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
    
    /**
     * Page d'inscription
     */
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }
    
    /**
     * Page à propos
     */
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("titre", "À propos - Calculateur de Salaire Net");
        return "about";
    }
}