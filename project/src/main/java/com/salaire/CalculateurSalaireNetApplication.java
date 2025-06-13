package com.salaire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principale de l'application Calculateur Salaire Net 2025
 * 
 * Cette application permet de calculer le salaire net à partir du salaire brut
 * en appliquant les taux de cotisations sociales et fiscales françaises.
 */
@SpringBootApplication
public class CalculateurSalaireNetApplication {

    public static void main(String[] args) {
        SpringApplication.run(CalculateurSalaireNetApplication.class, args);
    }
}