package com.salaire.service;

import com.salaire.entity.CalculSalaire;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service pour le calcul du salaire net
 * Implémente les règles de calcul françaises pour 2025
 */
@Service
public class SalaireCalculatorService {
    
    // Taux de cotisations sociales 2025 (salarié)
    private static final BigDecimal TAUX_SECU_MALADIE = BigDecimal.valueOf(0.0075); // 0.75%
    private static final BigDecimal TAUX_CHOMAGE = BigDecimal.valueOf(0.024); // 2.4%
    private static final BigDecimal TAUX_RETRAITE_BASE = BigDecimal.valueOf(0.0690); // 6.90%
    private static final BigDecimal TAUX_RETRAITE_COMPLEMENTAIRE = BigDecimal.valueOf(0.0387); // 3.87%
    private static final BigDecimal TAUX_CSG_DEDUCTIBLE = BigDecimal.valueOf(0.068); // 6.8%
    private static final BigDecimal TAUX_CSG_NON_DEDUCTIBLE = BigDecimal.valueOf(0.024); // 2.4%
    private static final BigDecimal TAUX_CRDS = BigDecimal.valueOf(0.005); // 0.5%
    
    // Plafonds et seuils 2025
    private static final BigDecimal PLAFOND_SECU_MENSUEL = BigDecimal.valueOf(3864.0); // Plafond sécurité sociale mensuel
    private static final BigDecimal SEUIL_CSG_CRDS = BigDecimal.valueOf(1.05); // 105% du SMIC
    
    // Barème impôt sur le revenu 2025 (mensuel)
    private static final BigDecimal SEUIL_TRANCHE_1 = BigDecimal.valueOf(916.67); // 11 000 / 12
    private static final BigDecimal SEUIL_TRANCHE_2 = BigDecimal.valueOf(2291.67); // 27 500 / 12
    private static final BigDecimal SEUIL_TRANCHE_3 = BigDecimal.valueOf(6458.33); // 77 500 / 12
    private static final BigDecimal SEUIL_TRANCHE_4 = BigDecimal.valueOf(14166.67); // 170 000 / 12
    
    private static final BigDecimal TAUX_TRANCHE_1 = BigDecimal.valueOf(0.11); // 11%
    private static final BigDecimal TAUX_TRANCHE_2 = BigDecimal.valueOf(0.30); // 30%
    private static final BigDecimal TAUX_TRANCHE_3 = BigDecimal.valueOf(0.41); // 41%
    private static final BigDecimal TAUX_TRANCHE_4 = BigDecimal.valueOf(0.45); // 45%
    
    /**
     * Calcule le salaire net à partir des données d'entrée
     */
    public CalculSalaire calculerSalaireNet(CalculSalaire calcul) {
        // 1. Calcul du salaire brut total
        BigDecimal salaireBrutTotal = calcul.getSalaireBrutTotal();
        
        // 2. Calcul des cotisations sociales
        calculerCotisationsSociales(calcul, salaireBrutTotal);
        
        // 3. Calcul du salaire net imposable
        BigDecimal salaireNetImposable = salaireBrutTotal.subtract(calcul.getTotalCotisations());
        calcul.setSalaireNetImposable(salaireNetImposable);
        
        // 4. Calcul de l'impôt sur le revenu
        BigDecimal impotRevenu = calculerImpotRevenu(salaireNetImposable, calcul.getNombreParts());
        calcul.setImpotRevenu(impotRevenu);
        
        // 5. Calcul du salaire net à payer
        BigDecimal salaireNetPayer = salaireNetImposable.subtract(impotRevenu);
        calcul.setSalaireNetPayer(salaireNetPayer);
        
        // 6. Calcul du taux de prélèvement global
        if (salaireBrutTotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalPrelevements = calcul.getTotalCotisations().add(impotRevenu);
            BigDecimal tauxPrelevement = totalPrelevements
                .divide(salaireBrutTotal, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            calcul.setTauxPrelevement(tauxPrelevement);
        }
        
        return calcul;
    }
    
    /**
     * Calcule les cotisations sociales
     */
    private void calculerCotisationsSociales(CalculSalaire calcul, BigDecimal salaireBrutTotal) {
        // Cotisations sécurité sociale (maladie)
        BigDecimal cotisationsSecu = salaireBrutTotal.multiply(TAUX_SECU_MALADIE);
        calcul.setCotisationsSecu(arrondir(cotisationsSecu));
        
        // Cotisations chômage (limitées au plafond)
        BigDecimal assiettePlafonnee = salaireBrutTotal.min(PLAFOND_SECU_MENSUEL.multiply(BigDecimal.valueOf(4)));
        BigDecimal cotisationsChomage = assiettePlafonnee.multiply(TAUX_CHOMAGE);
        calcul.setCotisationsChomage(arrondir(cotisationsChomage));
        
        // Cotisations retraite
        BigDecimal cotisationsRetraiteBase = assiettePlafonnee.multiply(TAUX_RETRAITE_BASE);
        BigDecimal cotisationsRetraiteCompl = salaireBrutTotal.multiply(TAUX_RETRAITE_COMPLEMENTAIRE);
        BigDecimal cotisationsRetraite = cotisationsRetraiteBase.add(cotisationsRetraiteCompl);
        calcul.setCotisationsRetraite(arrondir(cotisationsRetraite));
        
        // CSG et CRDS (sur 98.25% du salaire brut)
        BigDecimal assietteCsgCrds = salaireBrutTotal.multiply(BigDecimal.valueOf(0.9825));
        BigDecimal csgDeductible = assietteCsgCrds.multiply(TAUX_CSG_DEDUCTIBLE);
        BigDecimal csgNonDeductible = assietteCsgCrds.multiply(TAUX_CSG_NON_DEDUCTIBLE);
        BigDecimal crds = assietteCsgCrds.multiply(TAUX_CRDS);
        BigDecimal cotisationsCsgCrds = csgDeductible.add(csgNonDeductible).add(crds);
        calcul.setCotisationsCsgCrds(arrondir(cotisationsCsgCrds));
        
        // Total des cotisations
        BigDecimal totalCotisations = cotisationsSecu
            .add(cotisationsChomage)
            .add(cotisationsRetraite)
            .add(csgDeductible) // Seule la CSG déductible est retirée du salaire imposable
            .add(crds);
        calcul.setTotalCotisations(arrondir(totalCotisations));
    }
    
    /**
     * Calcule l'impôt sur le revenu mensuel selon le barème progressif
     */
    private BigDecimal calculerImpotRevenu(BigDecimal revenuImposable, BigDecimal nombreParts) {
        if (revenuImposable.compareTo(BigDecimal.ZERO) <= 0 || nombreParts.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Calcul du quotient familial
        BigDecimal quotientFamilial = revenuImposable.divide(nombreParts, 2, RoundingMode.HALF_UP);
        
        BigDecimal impot = BigDecimal.ZERO;
        
        // Tranche 1 : 0 à 916,67€ - 0%
        if (quotientFamilial.compareTo(SEUIL_TRANCHE_1) <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Tranche 2 : 916,67€ à 2 291,67€ - 11%
        if (quotientFamilial.compareTo(SEUIL_TRANCHE_2) > 0) {
            BigDecimal tranche2 = SEUIL_TRANCHE_2.subtract(SEUIL_TRANCHE_1);
            impot = impot.add(tranche2.multiply(TAUX_TRANCHE_1));
        } else {
            BigDecimal tranche2 = quotientFamilial.subtract(SEUIL_TRANCHE_1);
            impot = impot.add(tranche2.multiply(TAUX_TRANCHE_1));
            return arrondir(impot.multiply(nombreParts));
        }
        
        // Tranche 3 : 2 291,67€ à 6 458,33€ - 30%
        if (quotientFamilial.compareTo(SEUIL_TRANCHE_3) > 0) {
            BigDecimal tranche3 = SEUIL_TRANCHE_3.subtract(SEUIL_TRANCHE_2);
            impot = impot.add(tranche3.multiply(TAUX_TRANCHE_2));
        } else {
            BigDecimal tranche3 = quotientFamilial.subtract(SEUIL_TRANCHE_2);
            impot = impot.add(tranche3.multiply(TAUX_TRANCHE_2));
            return arrondir(impot.multiply(nombreParts));
        }
        
        // Tranche 4 : 6 458,33€ à 14 166,67€ - 41%
        if (quotientFamilial.compareTo(SEUIL_TRANCHE_4) > 0) {
            BigDecimal tranche4 = SEUIL_TRANCHE_4.subtract(SEUIL_TRANCHE_3);
            impot = impot.add(tranche4.multiply(TAUX_TRANCHE_3));
        } else {
            BigDecimal tranche4 = quotientFamilial.subtract(SEUIL_TRANCHE_3);
            impot = impot.add(tranche4.multiply(TAUX_TRANCHE_3));
            return arrondir(impot.multiply(nombreParts));
        }
        
        // Tranche 5 : au-delà de 14 166,67€ - 45%
        BigDecimal tranche5 = quotientFamilial.subtract(SEUIL_TRANCHE_4);
        impot = impot.add(tranche5.multiply(TAUX_TRANCHE_4));
        
        return arrondir(impot.multiply(nombreParts));
    }
    
    /**
     * Arrondit un montant à 2 décimales
     */
    private BigDecimal arrondir(BigDecimal montant) {
        return montant.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calcule le pourcentage de prélèvement par rapport au brut
     */
    public BigDecimal calculerPourcentagePrelevement(BigDecimal montantBrut, BigDecimal montantPreleve) {
        if (montantBrut.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return montantPreleve
            .divide(montantBrut, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);
    }
}