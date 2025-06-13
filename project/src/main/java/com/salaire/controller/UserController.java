package com.salaire.controller;

import com.salaire.entity.User;
import com.salaire.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Contrôleur pour la gestion des utilisateurs
 */
@Controller
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Traite l'inscription d'un nouvel utilisateur
     */
    @PostMapping("/register")
    public String enregistrerUtilisateur(@Valid @ModelAttribute("user") User user,
                                        BindingResult result,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "auth/register";
        }
        
        try {
            // Valider les données
            userService.validerUtilisateur(user);
            
            // Enregistrer l'utilisateur
            userService.enregistrerUtilisateur(user);
            
            redirectAttributes.addFlashAttribute("message", 
                "Inscription réussie ! Vous pouvez maintenant vous connecter.");
            return "redirect:/login";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        } catch (Exception e) {
            model.addAttribute("error", "Une erreur s'est produite lors de l'inscription.");
            return "auth/register";
        }
    }
}