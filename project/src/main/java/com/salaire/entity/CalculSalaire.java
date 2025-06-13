package com.salaire.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité représentant un calcul de salaire net
 */
@Entity
@Table(name = "calculs_salaire")
public class CalculSalaire {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    // Données d'entrée
    @NotNull(message = "Le salaire brut est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le salaire brut doit être positif")
    @Column(name = "salaire_brut", precision = 10, scale = 2)
    private BigDecimal salaireBrut;
    
    @Column(name = "primes", precision = 10, scale = 2)
    private BigDecimal primes = BigDecimal.ZERO;
    
    @Column(name = "indemnites", precision = 10, scale = 2)
    private BigDecimal indemnites = BigDecimal.ZERO;
    
    @Column(name = "avantages_nature", precision = 10, scale = 2)
    private BigDecimal avantagesNature = BigDecimal.ZERO;
    
    @Column(name = "heures_supplementaires", precision = 10, scale = 2)
    private BigDecimal heuresSupplementaires = BigDecimal.ZERO;
    
    // Cotisations sociales
    @Column(name = "cotisations_secu", precision = 10, scale = 2)
    private BigDecimal cotisationsSecu;
    
    @Column(name = "cotisations_chomage", precision = 10, scale = 2)
    private BigDecimal cotisationsChomage;
    
    @Column(name = "cotisations_retraite", precision = 10, scale = 2)
    private BigDecimal cotisationsRetraite;
    
    @Column(name = "cotisations_csg_crds", precision = 10, scale = 2)
    private BigDecimal cotisationsCsgCrds;
    
    @Column(name = "total_cotisations", precision = 10, scale = 2)
    private BigDecimal totalCotisations;
    
    // Impôt sur le revenu
    @Column(name = "impot_revenu", precision = 10, scale = 2)
    private BigDecimal impotRevenu;
    
    // Résultat
    @Column(name = "salaire_net_imposable", precision = 10, scale = 2)
    private BigDecimal salaireNetImposable;
    
    @Column(name = "salaire_net_payer", precision = 10, scale = 2)
    private BigDecimal salaireNetPayer;
    
    @Column(name = "taux_prelevement", precision = 5, scale = 2)
    private BigDecimal tauxPrelevement = BigDecimal.ZERO;
    
    // Métadonnées
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "description")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_marital")
    private StatutMarital statutMarital = StatutMarital.CELIBATAIRE;
    
    @Column(name = "nombre_parts", precision = 3, scale = 1)
    private BigDecimal nombreParts = BigDecimal.valueOf(1.0);
    
    // Constructeurs
    public CalculSalaire() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public BigDecimal getSalaireBrut() { return salaireBrut; }
    public void setSalaireBrut(BigDecimal salaireBrut) { this.salaireBrut = salaireBrut; }
    
    public BigDecimal getPrimes() { return primes; }
    public void setPrimes(BigDecimal primes) { this.primes = primes; }
    
    public BigDecimal getIndemnites() { return indemnites; }
    public void setIndemnites(BigDecimal indemnites) { this.indemnites = indemnites; }
    
    public BigDecimal getAvantagesNature() { return avantagesNature; }
    public void setAvantagesNature(BigDecimal avantagesNature) { this.avantagesNature = avantagesNature; }
    
    public BigDecimal getHeuresSupplementaires() { return heuresSupplementaires; }
    public void setHeuresSupplementaires(BigDecimal heuresSupplementaires) { this.heuresSupplementaires = heuresSupplementaires; }
    
    public BigDecimal getCotisationsSecu() { return cotisationsSecu; }
    public void setCotisationsSecu(BigDecimal cotisationsSecu) { this.cotisationsSecu = cotisationsSecu; }
    
    public BigDecimal getCotisationsChomage() { return cotisationsChomage; }
    public void setCotisationsChomage(BigDecimal cotisationsChomage) { this.cotisationsChomage = cotisationsChomage; }
    
    public BigDecimal getCotisationsRetraite() { return cotisationsRetraite; }
    public void setCotisationsRetraite(BigDecimal cotisationsRetraite) { this.cotisationsRetraite = cotisationsRetraite; }
    
    public BigDecimal getCotisationsCsgCrds() { return cotisationsCsgCrds; }
    public void setCotisationsCsgCrds(BigDecimal cotisationsCsgCrds) { this.cotisationsCsgCrds = cotisationsCsgCrds; }
    
    public BigDecimal getTotalCotisations() { return totalCotisations; }
    public void setTotalCotisations(BigDecimal totalCotisations) { this.totalCotisations = totalCotisations; }
    
    public BigDecimal getImpotRevenu() { return impotRevenu; }
    public void setImpotRevenu(BigDecimal impotRevenu) { this.impotRevenu = impotRevenu; }
    
    public BigDecimal getSalaireNetImposable() { return salaireNetImposable; }
    public void setSalaireNetImposable(BigDecimal salaireNetImposable) { this.salaireNetImposable = salaireNetImposable; }
    
    public BigDecimal getSalaireNetPayer() { return salaireNetPayer; }
    public void setSalaireNetPayer(BigDecimal salaireNetPayer) { this.salaireNetPayer = salaireNetPayer; }
    
    public BigDecimal getTauxPrelevement() { return tauxPrelevement; }
    public void setTauxPrelevement(BigDecimal tauxPrelevement) { this.tauxPrelevement = tauxPrelevement; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public StatutMarital getStatutMarital() { return statutMarital; }
    public void setStatutMarital(StatutMarital statutMarital) { this.statutMarital = statutMarital; }
    
    public BigDecimal getNombreParts() { return nombreParts; }
    public void setNombreParts(BigDecimal nombreParts) { this.nombreParts = nombreParts; }
    
    // Méthodes utilitaires
    public BigDecimal getSalaireBrutTotal() {
        BigDecimal total = salaireBrut;
        if (primes != null) total = total.add(primes);
        if (indemnites != null) total = total.add(indemnites);
        if (avantagesNature != null) total = total.add(avantagesNature);
        if (heuresSupplementaires != null) total = total.add(heuresSupplementaires);
        return total;
    }
    
    public enum StatutMarital {
        CELIBATAIRE("Célibataire"),
        MARIE("Marié(e)"),
        PACS("Pacsé(e)"),
        DIVORCE("Divorcé(e)"),
        VEUF("Veuf/Veuve");
        
        private final String libelle;
        
        StatutMarital(String libelle) {
            this.libelle = libelle;
        }
        
        public String getLibelle() {
            return libelle;
        }
    }
}