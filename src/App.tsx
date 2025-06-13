import React, { useState } from 'react';
import { Calculator, DollarSign, TrendingUp, Shield, Clock, Info } from 'lucide-react';

interface SalaryCalculation {
  salaireBrut: number;
  primes: number;
  indemnites: number;
  avantagesNature: number;
  heuresSupplementaires: number;
  statutMarital: 'CELIBATAIRE' | 'MARIE' | 'PACS' | 'DIVORCE' | 'VEUF';
  nombreParts: number;
  cotisationsSecu: number;
  cotisationsChomage: number;
  cotisationsRetraite: number;
  cotisationsCsgCrds: number;
  totalCotisations: number;
  impotRevenu: number;
  salaireNetImposable: number;
  salaireNetPayer: number;
  tauxPrelevement: number;
}

const TAUX_SECU_MALADIE = 0.0075;
const TAUX_CHOMAGE = 0.024;
const TAUX_RETRAITE_BASE = 0.0690;
const TAUX_RETRAITE_COMPLEMENTAIRE = 0.0387;
const TAUX_CSG_DEDUCTIBLE = 0.068;
const TAUX_CSG_NON_DEDUCTIBLE = 0.024;
const TAUX_CRDS = 0.005;
const PLAFOND_SECU_MENSUEL = 3864.0;

const SEUIL_TRANCHE_1 = 916.67;
const SEUIL_TRANCHE_2 = 2291.67;
const SEUIL_TRANCHE_3 = 6458.33;
const SEUIL_TRANCHE_4 = 14166.67;

const TAUX_TRANCHE_1 = 0.11;
const TAUX_TRANCHE_2 = 0.30;
const TAUX_TRANCHE_3 = 0.41;
const TAUX_TRANCHE_4 = 0.45;

function App() {
  const [activeTab, setActiveTab] = useState<'calculator' | 'about'>('calculator');
  const [formData, setFormData] = useState({
    salaireBrut: '',
    primes: '',
    indemnites: '',
    avantagesNature: '',
    heuresSupplementaires: '',
    statutMarital: 'CELIBATAIRE' as const,
    nombreParts: '1.0'
  });
  const [result, setResult] = useState<SalaryCalculation | null>(null);

  const calculateSalary = () => {
    const salaireBrut = parseFloat(formData.salaireBrut) || 0;
    const primes = parseFloat(formData.primes) || 0;
    const indemnites = parseFloat(formData.indemnites) || 0;
    const avantagesNature = parseFloat(formData.avantagesNature) || 0;
    const heuresSupplementaires = parseFloat(formData.heuresSupplementaires) || 0;
    const nombreParts = parseFloat(formData.nombreParts) || 1.0;

    const salaireBrutTotal = salaireBrut + primes + indemnites + avantagesNature + heuresSupplementaires;

    // Cotisations sociales
    const cotisationsSecu = salaireBrutTotal * TAUX_SECU_MALADIE;
    const assiettePlafonnee = Math.min(salaireBrutTotal, PLAFOND_SECU_MENSUEL * 4);
    const cotisationsChomage = assiettePlafonnee * TAUX_CHOMAGE;
    const cotisationsRetraiteBase = assiettePlafonnee * TAUX_RETRAITE_BASE;
    const cotisationsRetraiteCompl = salaireBrutTotal * TAUX_RETRAITE_COMPLEMENTAIRE;
    const cotisationsRetraite = cotisationsRetraiteBase + cotisationsRetraiteCompl;

    const assietteCsgCrds = salaireBrutTotal * 0.9825;
    const csgDeductible = assietteCsgCrds * TAUX_CSG_DEDUCTIBLE;
    const csgNonDeductible = assietteCsgCrds * TAUX_CSG_NON_DEDUCTIBLE;
    const crds = assietteCsgCrds * TAUX_CRDS;
    const cotisationsCsgCrds = csgDeductible + csgNonDeductible + crds;

    const totalCotisations = cotisationsSecu + cotisationsChomage + cotisationsRetraite + csgDeductible + crds;

    // Salaire net imposable
    const salaireNetImposable = salaireBrutTotal - totalCotisations;

    // Impôt sur le revenu
    const impotRevenu = calculateImpotRevenu(salaireNetImposable, nombreParts);

    // Salaire net à payer
    const salaireNetPayer = salaireNetImposable - impotRevenu;

    // Taux de prélèvement
    const totalPrelevements = totalCotisations + impotRevenu;
    const tauxPrelevement = salaireBrutTotal > 0 ? (totalPrelevements / salaireBrutTotal) * 100 : 0;

    const calculation: SalaryCalculation = {
      salaireBrut,
      primes,
      indemnites,
      avantagesNature,
      heuresSupplementaires,
      statutMarital: formData.statutMarital,
      nombreParts,
      cotisationsSecu: Math.round(cotisationsSecu * 100) / 100,
      cotisationsChomage: Math.round(cotisationsChomage * 100) / 100,
      cotisationsRetraite: Math.round(cotisationsRetraite * 100) / 100,
      cotisationsCsgCrds: Math.round(cotisationsCsgCrds * 100) / 100,
      totalCotisations: Math.round(totalCotisations * 100) / 100,
      impotRevenu: Math.round(impotRevenu * 100) / 100,
      salaireNetImposable: Math.round(salaireNetImposable * 100) / 100,
      salaireNetPayer: Math.round(salaireNetPayer * 100) / 100,
      tauxPrelevement: Math.round(tauxPrelevement * 100) / 100
    };

    setResult(calculation);
  };

  const calculateImpotRevenu = (revenuImposable: number, nombreParts: number): number => {
    if (revenuImposable <= 0 || nombreParts <= 0) return 0;

    const quotientFamilial = revenuImposable / nombreParts;
    let impot = 0;

    if (quotientFamilial <= SEUIL_TRANCHE_1) {
      return 0;
    }

    if (quotientFamilial > SEUIL_TRANCHE_2) {
      const tranche2 = SEUIL_TRANCHE_2 - SEUIL_TRANCHE_1;
      impot += tranche2 * TAUX_TRANCHE_1;
    } else {
      const tranche2 = quotientFamilial - SEUIL_TRANCHE_1;
      impot += tranche2 * TAUX_TRANCHE_1;
      return impot * nombreParts;
    }

    if (quotientFamilial > SEUIL_TRANCHE_3) {
      const tranche3 = SEUIL_TRANCHE_3 - SEUIL_TRANCHE_2;
      impot += tranche3 * TAUX_TRANCHE_2;
    } else {
      const tranche3 = quotientFamilial - SEUIL_TRANCHE_2;
      impot += tranche3 * TAUX_TRANCHE_2;
      return impot * nombreParts;
    }

    if (quotientFamilial > SEUIL_TRANCHE_4) {
      const tranche4 = SEUIL_TRANCHE_4 - SEUIL_TRANCHE_3;
      impot += tranche4 * TAUX_TRANCHE_3;
    } else {
      const tranche4 = quotientFamilial - SEUIL_TRANCHE_3;
      impot += tranche4 * TAUX_TRANCHE_3;
      return impot * nombreParts;
    }

    const tranche5 = quotientFamilial - SEUIL_TRANCHE_4;
    impot += tranche5 * TAUX_TRANCHE_4;

    return impot * nombreParts;
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR'
    }).format(amount);
  };

  const resetForm = () => {
    setFormData({
      salaireBrut: '',
      primes: '',
      indemnites: '',
      avantagesNature: '',
      heuresSupplementaires: '',
      statutMarital: 'CELIBATAIRE',
      nombreParts: '1.0'
    });
    setResult(null);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      {/* Header */}
      <header className="bg-white shadow-lg">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div className="flex items-center">
              <Calculator className="h-8 w-8 text-blue-600 mr-3" />
              <h1 className="text-2xl font-bold text-gray-900">
                Calculateur de Salaire Net 2025
              </h1>
            </div>
            <nav className="flex space-x-8">
              <button
                onClick={() => setActiveTab('calculator')}
                className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                  activeTab === 'calculator'
                    ? 'bg-blue-100 text-blue-700'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                <Calculator className="h-4 w-4 inline mr-2" />
                Calculateur
              </button>
              <button
                onClick={() => setActiveTab('about')}
                className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                  activeTab === 'about'
                    ? 'bg-blue-100 text-blue-700'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                <Info className="h-4 w-4 inline mr-2" />
                À propos
              </button>
            </nav>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {activeTab === 'calculator' ? (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Calculator Form */}
            <div className="bg-white rounded-xl shadow-lg p-6">
              <div className="flex items-center mb-6">
                <DollarSign className="h-6 w-6 text-blue-600 mr-2" />
                <h2 className="text-xl font-semibold text-gray-900">
                  Informations de salaire
                </h2>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Salaire brut mensuel (€) *
                  </label>
                  <input
                    type="number"
                    value={formData.salaireBrut}
                    onChange={(e) => handleInputChange('salaireBrut', e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="3000"
                    min="0"
                    step="0.01"
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Primes (€)
                    </label>
                    <input
                      type="number"
                      value={formData.primes}
                      onChange={(e) => handleInputChange('primes', e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="0"
                      min="0"
                      step="0.01"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Indemnités (€)
                    </label>
                    <input
                      type="number"
                      value={formData.indemnites}
                      onChange={(e) => handleInputChange('indemnites', e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="0"
                      min="0"
                      step="0.01"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Avantages en nature (€)
                    </label>
                    <input
                      type="number"
                      value={formData.avantagesNature}
                      onChange={(e) => handleInputChange('avantagesNature', e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="0"
                      min="0"
                      step="0.01"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Heures supplémentaires (€)
                    </label>
                    <input
                      type="number"
                      value={formData.heuresSupplementaires}
                      onChange={(e) => handleInputChange('heuresSupplementaires', e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="0"
                      min="0"
                      step="0.01"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Statut marital
                    </label>
                    <select
                      value={formData.statutMarital}
                      onChange={(e) => handleInputChange('statutMarital', e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    >
                      <option value="CELIBATAIRE">Célibataire</option>
                      <option value="MARIE">Marié(e)</option>
                      <option value="PACS">Pacsé(e)</option>
                      <option value="DIVORCE">Divorcé(e)</option>
                      <option value="VEUF">Veuf/Veuve</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Nombre de parts fiscales
                    </label>
                    <input
                      type="number"
                      value={formData.nombreParts}
                      onChange={(e) => handleInputChange('nombreParts', e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="1.0"
                      min="0.5"
                      max="10"
                      step="0.5"
                    />
                  </div>
                </div>

                <div className="flex space-x-4 pt-4">
                  <button
                    onClick={calculateSalary}
                    className="flex-1 bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors font-medium"
                  >
                    <Calculator className="h-4 w-4 inline mr-2" />
                    Calculer
                  </button>
                  <button
                    onClick={resetForm}
                    className="px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors"
                  >
                    Réinitialiser
                  </button>
                </div>
              </div>
            </div>

            {/* Results */}
            <div className="bg-white rounded-xl shadow-lg p-6">
              <div className="flex items-center mb-6">
                <TrendingUp className="h-6 w-6 text-green-600 mr-2" />
                <h2 className="text-xl font-semibold text-gray-900">
                  Résultats du calcul
                </h2>
              </div>

              {result ? (
                <div className="space-y-4">
                  {/* Summary */}
                  <div className="bg-gradient-to-r from-blue-50 to-indigo-50 p-4 rounded-lg border-l-4 border-blue-500">
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <p className="text-sm text-gray-600">Salaire brut total</p>
                        <p className="text-lg font-semibold text-gray-900">
                          {formatCurrency(result.salaireBrut + result.primes + result.indemnites + result.avantagesNature + result.heuresSupplementaires)}
                        </p>
                      </div>
                      <div>
                        <p className="text-sm text-gray-600">Salaire net à payer</p>
                        <p className="text-lg font-semibold text-green-600">
                          {formatCurrency(result.salaireNetPayer)}
                        </p>
                      </div>
                    </div>
                    <div className="mt-3 pt-3 border-t border-blue-200">
                      <p className="text-sm text-gray-600">Taux de prélèvement global</p>
                      <p className="text-lg font-semibold text-red-600">
                        {result.tauxPrelevement.toFixed(2)}%
                      </p>
                    </div>
                  </div>

                  {/* Detailed breakdown */}
                  <div className="space-y-3">
                    <h3 className="font-medium text-gray-900">Détail des cotisations</h3>
                    
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span className="text-gray-600">Cotisations sécurité sociale</span>
                        <span className="font-medium">{formatCurrency(result.cotisationsSecu)}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">Cotisations chômage</span>
                        <span className="font-medium">{formatCurrency(result.cotisationsChomage)}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">Cotisations retraite</span>
                        <span className="font-medium">{formatCurrency(result.cotisationsRetraite)}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">CSG/CRDS</span>
                        <span className="font-medium">{formatCurrency(result.cotisationsCsgCrds)}</span>
                      </div>
                      <div className="flex justify-between border-t pt-2">
                        <span className="text-gray-900 font-medium">Total cotisations</span>
                        <span className="font-semibold">{formatCurrency(result.totalCotisations)}</span>
                      </div>
                    </div>

                    <div className="space-y-2 text-sm pt-3 border-t">
                      <div className="flex justify-between">
                        <span className="text-gray-600">Salaire net imposable</span>
                        <span className="font-medium">{formatCurrency(result.salaireNetImposable)}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-600">Impôt sur le revenu</span>
                        <span className="font-medium">{formatCurrency(result.impotRevenu)}</span>
                      </div>
                      <div className="flex justify-between border-t pt-2">
                        <span className="text-gray-900 font-medium">Salaire net à payer</span>
                        <span className="font-semibold text-green-600">{formatCurrency(result.salaireNetPayer)}</span>
                      </div>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="text-center py-12">
                  <Calculator className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                  <p className="text-gray-500">
                    Remplissez le formulaire et cliquez sur "Calculer" pour voir les résultats
                  </p>
                </div>
              )}
            </div>
          </div>
        ) : (
          /* About Page */
          <div className="max-w-4xl mx-auto">
            <div className="bg-white rounded-xl shadow-lg p-8">
              <h2 className="text-3xl font-bold text-gray-900 mb-6">
                À propos du Calculateur de Salaire Net 2025
              </h2>
              
              <div className="prose max-w-none">
                <p className="text-lg text-gray-600 mb-6">
                  Notre calculateur de salaire net utilise les taux officiels français 2025 pour vous donner 
                  une estimation précise de votre salaire net à partir de votre salaire brut.
                </p>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                  <div className="text-center p-6 bg-blue-50 rounded-lg">
                    <Shield className="h-12 w-12 text-blue-600 mx-auto mb-4" />
                    <h3 className="font-semibold text-gray-900 mb-2">Calculs Précis</h3>
                    <p className="text-sm text-gray-600">
                      Basés sur les taux officiels de cotisations sociales et fiscales 2025
                    </p>
                  </div>
                  <div className="text-center p-6 bg-green-50 rounded-lg">
                    <Clock className="h-12 w-12 text-green-600 mx-auto mb-4" />
                    <h3 className="font-semibold text-gray-900 mb-2">Instantané</h3>
                    <p className="text-sm text-gray-600">
                      Résultats immédiats sans inscription ni sauvegarde de données
                    </p>
                  </div>
                  <div className="text-center p-6 bg-purple-50 rounded-lg">
                    <TrendingUp className="h-12 w-12 text-purple-600 mx-auto mb-4" />
                    <h3 className="font-semibold text-gray-900 mb-2">Détaillé</h3>
                    <p className="text-sm text-gray-600">
                      Breakdown complet de toutes les cotisations et déductions
                    </p>
                  </div>
                </div>

                <h3 className="text-xl font-semibold text-gray-900 mb-4">
                  Taux utilisés (2025)
                </h3>
                
                <div className="bg-gray-50 p-6 rounded-lg mb-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                    <div>
                      <h4 className="font-medium text-gray-900 mb-2">Cotisations sociales</h4>
                      <ul className="space-y-1 text-gray-600">
                        <li>• Sécurité sociale (maladie) : 0,75%</li>
                        <li>• Chômage : 2,4%</li>
                        <li>• Retraite de base : 6,90%</li>
                        <li>• Retraite complémentaire : 3,87%</li>
                      </ul>
                    </div>
                    <div>
                      <h4 className="font-medium text-gray-900 mb-2">CSG/CRDS</h4>
                      <ul className="space-y-1 text-gray-600">
                        <li>• CSG déductible : 6,8%</li>
                        <li>• CSG non déductible : 2,4%</li>
                        <li>• CRDS : 0,5%</li>
                      </ul>
                    </div>
                  </div>
                </div>

                <h3 className="text-xl font-semibold text-gray-900 mb-4">
                  Barème de l'impôt sur le revenu 2025
                </h3>
                
                <div className="bg-gray-50 p-6 rounded-lg">
                  <div className="text-sm space-y-2">
                    <div className="flex justify-between">
                      <span>Jusqu'à 916,67 € / mois</span>
                      <span className="font-medium">0%</span>
                    </div>
                    <div className="flex justify-between">
                      <span>De 916,67 € à 2 291,67 € / mois</span>
                      <span className="font-medium">11%</span>
                    </div>
                    <div className="flex justify-between">
                      <span>De 2 291,67 € à 6 458,33 € / mois</span>
                      <span className="font-medium">30%</span>
                    </div>
                    <div className="flex justify-between">
                      <span>De 6 458,33 € à 14 166,67 € / mois</span>
                      <span className="font-medium">41%</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Au-delà de 14 166,67 € / mois</span>
                      <span className="font-medium">45%</span>
                    </div>
                  </div>
                </div>

                <div className="mt-8 p-4 bg-yellow-50 border-l-4 border-yellow-400 rounded">
                  <p className="text-sm text-yellow-800">
                    <strong>Avertissement :</strong> Ce calculateur fournit une estimation basée sur les taux standards. 
                    Les résultats peuvent varier selon votre situation spécifique (convention collective, 
                    statut particulier, etc.). Consultez un expert-comptable pour des calculs précis.
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="bg-gray-800 text-white py-8 mt-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            <div>
              <h3 className="text-lg font-semibold mb-4">Calculateur Salaire Net 2025</h3>
              <p className="text-gray-300">
                Calculez facilement votre salaire net à partir de votre salaire brut 
                avec les taux officiels français 2025.
              </p>
            </div>
            <div>
              <h3 className="text-lg font-semibold mb-4">Informations</h3>
              <ul className="space-y-2 text-gray-300">
                <li>• Taux officiels 2025</li>
                <li>• Calculs instantanés</li>
                <li>• Aucune donnée sauvegardée</li>
                <li>• Gratuit et sans inscription</li>
              </ul>
            </div>
          </div>
          <div className="border-t border-gray-700 mt-8 pt-8 text-center text-gray-400">
            <p>© 2025 Calculateur Salaire Net. Données fiscales officielles françaises.</p>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default App;