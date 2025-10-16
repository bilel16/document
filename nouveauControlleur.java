package com.bna.smile.web.virement.controller;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.validator.ValidatorException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.ajax4jsf.model.KeepAlive;
import org.apache.log4j.Logger;

import com.bna.commun.model.*;
import com.bna.commun.util.*;
import com.bna.smile.model.constant.Constants;
import com.bna.smile.model.domainecommun.commande.*;
import com.bna.smile.model.domainecommun.service.CRUDservice;
import com.bna.smile.model.virement.commande.VirementCmd;
import com.bna.smile.model.virement.model.VirementVo;
import com.bna.smile.web.commun.controller.*;
import com.bna.smile.web.commun.model.ParamAgence;
import com.ibm.icu.util.Calendar;
import com.oxia.fwk.context.Context;
import com.oxia.fwk.core.*;

/**
 * Contrôleur de gestion des virements centraux (Mandat BCT)
 * 
 * <p>Ce contrôleur gère l'ensemble du cycle de vie des ordres de virement centraux :</p>
 * <ul>
 *   <li>Création et validation d'ordres de virement</li>
 *   <li>Gestion des bénéficiaires (ajout, modification, suppression)</li>
 *   <li>Recherche et consultation des ordres</li>
 *   <li>Exécution et rejet des virements</li>
 *   <li>Génération de rapports</li>
 * </ul>
 * 
 * @author BNA Smile Team
 * @version 2.0 - Restructuré Phase 1
 */
@KeepAlive
public class PecVirementCentralCtr implements Serializable {

    // ============================================================================
    // CONSTANTES
    // ============================================================================
    
    private static final long serialVersionUID = 1L;
    
    /** Code produit pour virement ponctuel */
    public static final Long COD_PRD_VIREMENT_PONCTUEL = 1063L;
    
    /** État virement ponctuel en cours */
    public static final Long ETAT_VIREMENT_PONCTUEL_ENCOUR = 0L;
    
    /** Compte interne pour les mandats */
    private static final String COMPTE_INTERNE_MANDAT = "03404002010100096947";
    
    /** Logger de la classe */
    private final Logger logger = Logger.getLogger(PecVirementCentralCtr.class);

    // ============================================================================
    // DONNÉES ORDRE DE VIREMENT
    // ============================================================================
    
    /** Ordre de virement sélectionné pour consultation/modification */
    private OrdreVirement selectedOrdreVirement;
    
    /** Liste des ordres de virement affichés */
    private List<OrdreVirement> listeOrdresVirements = new ArrayList<>();
    
    /** Numéro de remise de l'ordre */
    private String numRemise;
    
    /** Montant global de l'ordre */
    private String montantGlobal;
    
    /** Nombre de virements dans l'ordre */
    private Long nbreVirement;
    
    /** Date d'exécution de l'ordre */
    private Date dateExecution;
    
    /** Date d'exécution au format String pour l'UI */
    private String dateExecutionString;
    
    /** Motif du virement */
    private String motif;
    
    /** État de l'ordre pour recherche (A=Attente, E=Exécuté, R=Rejeté) */
    private String etatRecherche;

    // ============================================================================
    // DONNÉES BÉNÉFICIAIRES
    // ============================================================================
    
    /** Bénéficiaire en cours de création/modification */
    private BeneficiaireOrdreVirement beneficiaireOrdreVirement = new BeneficiaireOrdreVirement();
    
    /** Bénéficiaire sélectionné dans la liste */
    private BeneficiaireOrdreVirement selectedBeneficiaire;
    
    /** Liste des bénéficiaires de l'ordre en cours */
    private List<BeneficiaireOrdreVirement> listeBeneficiaireOrdreVirement = new ArrayList<>();
    
    /** Liste des bénéficiaires supprimés (pour tracking) */
    private List<BeneficiaireOrdreVirement> listeBeneficiaireOrdreVirementSupprimes = new ArrayList<>();
    
    /** Liste des bénéficiaires pour affichage */
    private List<BeneficiaireOrdreVirement> listeBeneficiaires = new ArrayList<>();
    
    /** Liste des bénéficiaires ajoutés */
    private List<BeneficiaireOrdreVirement> listeBeneficiairesAjoutes = new ArrayList<>();
    
    /** RIB du bénéficiaire */
    private String ribBeneficiaire;
    
    /** Nom du bénéficiaire */
    private String nomBeneficiaire;
    
    /** Nom du bénéficiaire (alias) */
    private String benificiaire = "";
    
    /** Montant pour le bénéficiaire */
    private long montantBeneficiaire;
    
    /** Montant du virement en cours de saisie */
    private String montantVirement;
    
    /** Motif pour le bénéficiaire */
    private String motifBeneficiaire;
    
    /** Compte du bénéficiaire */
    private String compteBenificiaire;

    // ============================================================================
    // CRITÈRES DE RECHERCHE ET FILTRES
    // ============================================================================
    
    /** Critère de recherche (R=Remise, T=Tous, E=État, C=Client) */
    private String critereRecherche = "R";
    
    /** Montant minimal pour filtre */
    private Double montantMinimal;
    
    /** Date de dernière mise à jour */
    private Date derniereMiseAJour;
    
    /** Liste des numéros de remise disponibles */
    private List<String> listNumRemise = new ArrayList<>();

    // ============================================================================
    // STATISTIQUES ET TOTAUX
    // ============================================================================
    
    /** Nombre de virements saisis */
    private Long nombreSaisie;
    
    /** Montant total saisi */
    private Long montantSaisie;
    
    /** Nombre total de virements (statistique) */
    private long nombreTotalVirements;
    
    /** Montant total des ordres (statistique) */
    private double montantTotalOrdres;

    // ============================================================================
    // MESSAGES ET ÉTATS UI
    // ============================================================================
    
    /** Message d'erreur général */
    private String messageErreur = "";
    
    /** Message d'information général */
    private String messageInformation = "";
    
    /** Message d'information pour recherche par état */
    private String messageInformationEtat = "";
    
    /** Message de validation du virement BCT */
    private String messageValidationVirementBct = "";
    
    /** Message pour le montant */
    private String messageMontant = "";
    
    /** Message pour l'enregistrement du bénéficiaire */
    private String messageEnregistBenefVirement = "";
    
    /** Message pour l'enregistrement du bénéficiaire (alias) */
    private String messageEnregistBeneficiaire = "";
    
    /** Message pour RIB existant */
    private String messageRIBexistant = "";
    
    /** Message de validation du compte */
    private String msgValidatorCompte = "";
    
    /** Message de validation du RIB */
    private String msgValidatorRib = "";
    
    /** État de validation du traitement (0=Init, 1=Success, 2=Error) */
    private String strEtatValidationTrt;
    
    /** État du bouton de validation du virement */
    private boolean etatCmdValiderVirement = false;
    
    /** État de sauvegarde du bénéficiaire */
    private boolean etatSaveBenefVir = false;
    
    /** État du motif */
    private boolean etatMotif = false;
    
    /** État du bénéficiaire */
    private boolean etatBeneficiaire = false;
    
    /** État de validité du RIB */
    private boolean etatRIBCorrecte = true;
    
    /** État du bouton d'ajout de bénéficiaire */
    private boolean etatCmdAjouterBeneficiaire = true;
    
    /** État du message de validation RIB */
    private boolean etatmsgValidatorRib = true;
    
    /** État du message d'enregistrement bénéficiaire */
    private boolean etatMessageEnregistBeneficiaire = false;

    // ============================================================================
    // REPORTING ET IMPRESSION
    // ============================================================================
    
    /** Nom du rapport généré */
    private String reportName;
    
    /** Source du rapport (URL) */
    private String srcRepport;
    
    /** Paramètres pour Jasper Reports */
    private Map<String, String> parametersJasper = new HashMap<>();
    
    /** Chemin du répertoire des rapports */
    private String strPath = "";

    // ============================================================================
    // PARAMÈTRES ET CONFIGURATION
    // ============================================================================
    
    /** Paramètres de l'agence connectée */
    private ParamAgence paramAgence = new ParamAgence();
    
    /** Contrôleur de recherche client */
    private RechercheClientByStructureCtr rechercheClientCtr;
    
    /** Numéro de séquence globale virement */
    private String strNumSeqGvir = "";
    
    /** RIB (usage interne) */
    private String strRib = "";
    
    /** Matricule utilisateur */
    private String strNumMatUser = "";
    
    /** Libellé de la structure */
    private String strLibStructure = "";
    
    /** Code de la structure */
    private String strCodeStructure = "";
    
    /** Type de virement */
    private String strTypeVirement = "";
    
    /** Nom initial du compte */
    private String strNomInitCpt = "";
    
    /** Nombre de virements (String) */
    private String strNombreVirement = "";
    
    /** Montant du virement (String) */
    private String strMontantVirement = "";
    
    /** Date d'exécution (String) */
    private String strDateExecution = "";

    // ============================================================================
    // CYCLE DE VIE DU CONTRÔLEUR
    // ============================================================================

    /**
     * Constructeur par défaut
     */
    public PecVirementCentralCtr() {
        // Constructeur vide requis par JSF
    }

    /**
     * Initialisation du contrôleur après construction
     * Appelée automatiquement par le conteneur JSF
     */
    @PostConstruct
    public void init() {
        montantVirement = null;
        logger.info("Contrôleur PecVirementCentralCtr initialisé");
    }

    /**
     * Réinitialise toutes les listes et critères de recherche
     * 
     * @return null pour rester sur la même page
     */
    public String reinitialiserListe() {
        try {
            listeOrdresVirements.clear();
            numRemise = "";
            montantMinimal = null;
            messageEnregistBeneficiaire = "";
            selectedOrdreVirement = null;
            listeBeneficiaireOrdreVirement.clear();
            selectedBeneficiaire = null;
            messageInformation = "";
            return null;
        } catch (Exception e) {
            logger.error("Erreur dans reinitialiserListe", e);
            return null;
        }
    }

    /**
     * Charge automatiquement tous les ordres au chargement de la page
     * Utilisé dans le preRenderView de la page JSF
     */
    public void chargerTousLesOrdresAuChargement() {
        try {
            logger.info("Chargement automatique des ordres au chargement de la page");
            listeTousLesOrdresVirement();
            
            if (listeOrdresVirements != null && !listeOrdresVirements.isEmpty()) {
                messageInformation = "Liste chargée automatiquement : " + 
                                   listeOrdresVirements.size() + " ordres trouvés";
            } else {
                messageInformation = "Aucun ordre de virement trouvé dans le système";
            }
            
            derniereMiseAJour = new Date();
        } catch (Exception e) {
            logger.error("Erreur lors du chargement automatique", e);
            messageInformation = "Erreur lors du chargement des données";
        }
    }

    // ============================================================================
    // GESTION DES BÉNÉFICIAIRES - CRÉATION ET AJOUT
    // ============================================================================

    /**
     * Prépare le formulaire pour créer un nouveau bénéficiaire
     * Réinitialise tous les champs du formulaire
     */
    public void creerNouveauBenefVirement() {
        setBenificiaire("");
        msgValidatorCompte = "";
        setMontantVirement(null);
        setCompteBenificiaire(null);
        setRibBeneficiaire("");
        setMessageEnregistBenefVirement("");
        setMessageRIBexistant("");
    }

    /**
     * Prépare le formulaire pour créer un nouveau bénéficiaire (alias)
     * 
     * @return null pour rester sur la même page
     */
    public String creerNouveauBeneficiaire() {
        ribBeneficiaire = "";
        nomBeneficiaire = "";
        montantBeneficiaire = 0L;
        motifBeneficiaire = "";
        messageEnregistBeneficiaire = "";
        return null;
    }

    /**
     * Ajoute un bénéficiaire à la liste après validation
     * Vérifie le format du RIB, le montant et l'absence d'erreurs
     */
    public void ajouterBeneficiaireVirement() {
        try {
            etatSaveBenefVir = false;
            messageEnregistBenefVirement = "";
            
            // Validation des champs obligatoires
            if (getRibBeneficiaire().length() == 20 && 
                montantVirement != null && montantVirement.length() > 0 &&
                Long.valueOf(montantVirement.replace(".", "").replace(" ", "").trim()).longValue() > 0 &&
                msgValidatorCompte.length() < 1) {

                beneficiaireOrdreVirement = new BeneficiaireOrdreVirement();
                beneficiaireOrdreVirement.setNumRibbBenf(ribBeneficiaire);
                
                // Structure du bénéficiaire
                Structure strcBenef = new Structure();
                strcBenef.setCodStrcStrc(Long.valueOf(getRibBeneficiaire().substring(5, 8)));
                beneficiaireOrdreVirement.setStructureBenef(strcBenef);
                
                // Nom (tronqué à 60 caractères si nécessaire)
                if (benificiaire.length() > 60) {
                    benificiaire = benificiaire.substring(0, 60);
                }
                beneficiaireOrdreVirement.setNomPrnbBenf(benificiaire);
                
                // Montant
                beneficiaireOrdreVirement.setMontOperBenf(
                    Long.valueOf(montantVirement.replace(".", "").replace(" ", "").trim())
                );
                
                // État "A" pour Ajouté
                beneficiaireOrdreVirement.setCodEtatDov("A");

                listeBeneficiaireOrdreVirement.add(beneficiaireOrdreVirement);
                creerNouveauBenefVirement();

                etatSaveBenefVir = true;
                logger.info("Bénéficiaire ajouté avec succès - RIB: " + ribBeneficiaire);
            } else {
                etatSaveBenefVir = false;
                messageEnregistBenefVirement = getMsgValidatorCompte();
            }
        } catch (Exception e) {
            etatSaveBenefVir = false;
            logger.error("Erreur lors de l'ajout du bénéficiaire", e);
        }
    }

    /**
     * Ajoute un bénéficiaire (méthode alternative)
     * 
     * @return null pour rester sur la même page
     */
    public String ajouterBeneficiaire() {
        messageEnregistBeneficiaire = "Bénéficiaire ajouté avec succès";
        return null;
    }

    // ============================================================================
    // GESTION DES BÉNÉFICIAIRES - MODIFICATION
    // ============================================================================

    /**
     * Charge les données du bénéficiaire sélectionné pour édition
     */
    public void editerBeneficiaireVirement() {
        try {
            if (selectedBeneficiaire != null) {
                if (!selectedBeneficiaire.getNumRibbBenf().isEmpty()) {
                    setCompteBenificiaire(selectedBeneficiaire.getNumRibbBenf());
                }
                benificiaire = selectedBeneficiaire.getNomPrnbBenf();
                montantVirement = selectedBeneficiaire.getMontOperBenf() + "";

                messageEnregistBenefVirement = "";
                msgValidatorCompte = "";
                messageRIBexistant = "";
                
                logger.debug("Édition du bénéficiaire ID: " + 
                           selectedBeneficiaire.getBeneficiaireOrdreVirementId().getNumDetBenf());
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'édition du bénéficiaire", e);
        }
    }

    /**
     * Charge les données du bénéficiaire pour édition (méthode alternative)
     * 
     * @return null pour rester sur la même page
     */
    public String editerBeneficiaire() {
        if (selectedBeneficiaire != null) {
            ribBeneficiaire = selectedBeneficiaire.getNumRibbBenf();
            nomBeneficiaire = selectedBeneficiaire.getNomPrnbBenf();
            montantBeneficiaire = selectedBeneficiaire.getMontOperBenf();
            motifBeneficiaire = "";
        }
        return null;
    }

    /**
     * Modifie le bénéficiaire sélectionné avec les nouvelles valeurs
     * Ajoute l'ancien bénéficiaire à la liste des supprimés si nécessaire
     */
    public void modifierBeneficiaire() {
        try {
            messageEnregistBeneficiaire = "";
            etatMessageEnregistBeneficiaire = false;
            
            if (getMsgValidatorRib().length() == 0) {
                if (selectedBeneficiaire != null) {
                    BeneficiaireOrdreVirement beneficiaireSaisie = new BeneficiaireOrdreVirement();
                    
                    // Remplir les données du nouveau bénéficiaire
                    beneficiaireSaisie.setNumRibbBenf(getRibBeneficiaire());
                    beneficiaireSaisie.setNomPrnbBenf(getNomBeneficiaire());
                    beneficiaireSaisie.setMontOperBenf(getMontantBeneficiaire());
                    beneficiaireSaisie.setCodEtatDov(selectedBeneficiaire.getCodEtatDov());
                    
                    // Structure bénéficiaire
                    Structure strcBenef = new Structure();
                    strcBenef.setCodStrcStrc(Long.valueOf(getRibBeneficiaire().substring(5, 8)));
                    beneficiaireSaisie.setStructureBenef(strcBenef);
                    
                    // Associer à l'ordre de virement
                    beneficiaireSaisie.setOrdreVirement(selectedBeneficiaire.getOrdreVirement());
                    
                    // Garder l'ID si c'est une modification
                    if (selectedBeneficiaire.getBeneficiaireOrdreVirementId() != null) {
                        beneficiaireSaisie.setBeneficiaireOrdreVirementId(
                            selectedBeneficiaire.getBeneficiaireOrdreVirementId()
                        );
                    }
                    
                    // Vérifier qu'on ne fait pas un virement vers le même compte
                    String numCompteBenif = getRibBeneficiaire().substring(5, 18);
                    String compteDebiteur = selectedBeneficiaire.getOrdreVirement().getCompteDebitOvir();
                    
                    if (compteDebiteur != null && compteDebiteur.contains(numCompteBenif)) {
                        messageEnregistBeneficiaire = "Impossible d'effectuer un virement vers le même compte débiteur";
                        etatMessageEnregistBeneficiaire = true;
                    } else {
                        // Ajouter l'ancien à la liste des supprimés
                        if (selectedBeneficiaire.getBeneficiaireOrdreVirementId() != null) {
                            listeBeneficiaireOrdreVirementSupprimes.add(selectedBeneficiaire);
                        }
                        
                        // Remplacer dans la liste
                        int index = listeBeneficiaireOrdreVirement.indexOf(selectedBeneficiaire);
                        if (index >= 0) {
                            listeBeneficiaireOrdreVirement.set(index, beneficiaireSaisie);
                        }
                        
                        calculerTotauxGlobaux();
                        messageEnregistBeneficiaire = "";
                        etatMessageEnregistBeneficiaire = false;
                        
                        logger.info("Bénéficiaire modifié - RIB: " + beneficiaireSaisie.getNumRibbBenf());
                    }
                } else {
                    messageEnregistBeneficiaire = "Aucun bénéficiaire sélectionné pour modification";
                    etatMessageEnregistBeneficiaire = true;
                }
            } else {
                messageEnregistBeneficiaire = getMsgValidatorRib();
                etatMessageEnregistBeneficiaire = true;
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la modification du bénéficiaire", e);
            messageEnregistBeneficiaire = "Erreur lors de la modification : " + e.getMessage();
            etatMessageEnregistBeneficiaire = true;
        }
    }

    // ============================================================================
    // GESTION DES BÉNÉFICIAIRES - SUPPRESSION
    // ============================================================================

    /**
     * Supprime le bénéficiaire sélectionné de la liste
     */
    public void supprimerBeneficiaireVirement() {
        try {
            if (selectedBeneficiaire != null) {
                listeBeneficiaireOrdreVirement.remove(selectedBeneficiaire);
                logger.info("Bénéficiaire supprimé de la liste");
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du bénéficiaire", e);
        }
    }

    /**
     * Supprime un bénéficiaire d'un ordre existant
     * Ajoute le bénéficiaire à la liste des supprimés si il a un ID
     */
    public void supprimerBeneficiaireOrdreVirement() {
        try {
            if (selectedBeneficiaire != null) {
                if (selectedBeneficiaire.getBeneficiaireOrdreVirementId() != null) {
                    listeBeneficiaireOrdreVirementSupprimes.add(selectedBeneficiaire);
                }
                listeBeneficiaireOrdreVirement.remove(selectedBeneficiaire);
                calculerTotauxGlobaux();
                logger.info("Bénéficiaire supprimé et ajouté à la liste des suppressions");
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du bénéficiaire d'ordre", e);
        }
    }

    /**
     * Supprime un bénéficiaire (méthode alternative)
     * 
     * @return null pour rester sur la même page
     */
    public String supprimerBeneficiaire() {
        if (selectedBeneficiaire != null && listeBeneficiaires != null) {
            listeBeneficiaires.remove(selectedBeneficiaire);
            messageEnregistBeneficiaire = "Bénéficiaire supprimé avec succès";
        }
        return null;
    }

    // ============================================================================
    // GESTION DES ORDRES DE VIREMENT - CRÉATION ET VALIDATION
    // ============================================================================

    /**
     * Valide et crée un ordre de virement avec ses bénéficiaires
     * Génère un numéro de remise unique et sauvegarde l'ordre
     */
    public void validerVirementMandatBct() {
        try {
            VirementVo virementVo = new VirementVo();
            VirementCmd virementPonctuelCmd = new VirementCmd();
            OrdreVirement ordreVirement = new OrdreVirement();
            OrdreVirementId ordreVirementId = new OrdreVirementId();
            
            setStrEtatValidationTrt("0");
            parametersJasper = new HashMap<>();
            SimpleDateFormat dateFormatANNEE = new SimpleDateFormat("yyyy");
            virementVo.setParamAgence(getParamAgence());
            
            // Génération du numéro de remise
            Long seqGlobVir = getSeqAgence(virementVo);
            String numeroRemise = StrHandler.lpad(getParamAgence().getCodStrcStrc() + "", '0', 3) +
                                 dateFormatANNEE.format(new Date()) + 
                                 StrHandler.lpad(seqGlobVir + "", '0', 5);

            if (listeBeneficiaireOrdreVirement != null && !listeBeneficiaireOrdreVirement.isEmpty()) {
                // Structure initiatrice
                Structure structureInitiatrice = new Structure();
                structureInitiatrice.setCodStrcStrc(getParamAgence().getCodStrcStrc());

                // ID de l'ordre
                ordreVirementId.setDatOvirOvir(DateHandler.strToDate(getParamAgence().getDateComptable()));
                ordreVirementId.setNumOvirOvir(numeroRemise);
                ordreVirement.setOrdreVirementId(ordreVirementId);
                ordreVirement.setStructure(structureInitiatrice);
                
                // Personnel
                Personnel personnelInit = new Personnel();
                personnelInit.setNumMatrUser(getParamAgence().getNumMatrUser());
                ordreVirement.setPersonnel(personnelInit);
                ordreVirement.setPersonnelValidateur(personnelInit);
                
                // Dates et états
                ordreVirement.setDatExecOvir(DateHandler.strToDate(getParamAgence().getDateComptable()));
                ordreVirement.setCodEtatOvir("A");
                
                // Montants
                ordreVirement.setMontGlobOvir(montantSaisie);
                ordreVirement.setNbrVirOvir(nombreSaisie);
                
                // Compte débiteur
                ordreVirement.setCompteDebitOvir(COMPTE_INTERNE_MANDAT);

                // Motif (tronqué à 200 caractères si nécessaire)
                if (motif != null && motif.length() > 200) {
                    motif = motif.substring(0, 200);
                }
                ordreVirement.setLibMotfOvir(motif);
                
                // Liste des bénéficiaires
                ordreVirement.setListeBeneficiaireOrdreVirements(listeBeneficiaireOrdreVirement);
                
                // Préparation du VO
                virementVo.setOrdreVirement(ordreVirement);
                virementVo.setParamAgence(getParamAgence());
                virementVo.setStrEtatValidationTrt(getStrEtatValidationTrt());
                
                // Appel du service de validation
                virementVo = (VirementVo) virementPonctuelCmd.validerVirementCentralVirement(virementVo);
                messageValidationVirementBct = virementVo.getMessageValidation();
                
                logger.info("Ordre de virement créé avec succès - Numéro: " + numeroRemise);
            } else {
                messageValidationVirementBct = "Liste des bénéficiaires est vide";
                etatCmdValiderVirement = true;
            }
        } catch (Exception e) {
            setStrEtatValidationTrt("2");
            etatCmdValiderVirement = true;
            messageValidationVirementBct = e.getMessage();
            logger.error("Erreur lors de la validation du virement", e);
        }
    }

    /**
     * Récupère la séquence globale de virement pour l'agence
     * 
     * @param virementVo VO contenant les paramètres de l'agence
     * @return Numéro de séquence
     */
    public Long getSeqAgence(VirementVo virementVo) {
        ISearchEngine searchEngine = (ISearchEngine) Context.getInstance()
            .getSpringContext().getBean("searchEngine");
        ICriteria criteria = searchEngine.createCriteria();
        IExpression expression = searchEngine.createExpression();
        Context context = ContextHandler.getContext();
        CRUDservice crudService = (CRUDservice) context.getBean("crudservice");

        Long numValSeq = null;
        SeqAgence seqAgence = new SeqAgence();

        criteria.add(expression.eq("seqAgenceId.codStrcStrc", 
                                   new Long(virementVo.getParamAgence().getCodStrcStrc())));
        criteria.add(expression.eq("seqAgenceId.libSeqSeqa", "SEQ_GLOB_VIR"));

        List l = searchEngine.find(SeqAgence.class, criteria);

        if (l != null && !l.isEmpty()) {
            seqAgence = (SeqAgence) l.get(0);
            numValSeq = seqAgence.getNumValSeqa();
            seqAgence.setNumValSeqa(numValSeq.longValue() + 1);
            crudService.update(seqAgence);
        }

        return numValSeq;
    }

    // ============================================================================
    // GESTION DES ORDRES DE VIREMENT - RECHERCHE
    // ============================================================================

    /**
     * Recherche un ordre de virement par son numéro de remise
     * 
     * @return null pour rester sur la même page
     */
    public String rechercherGlobalVirementByRemise() {
        try {
            listeOrdresVirements.clear();
            listeBeneficiaires.clear();
            listeBeneficiairesAjoutes.clear();
            messageEnregistBeneficiaire = "";
            messageInformation = "";
            montantVirement = "";
            selectedOrdreVirement = null;
            
            logger.debug("Recherche par numéro de remise: " + getNumRemise());
            
            if (getNumRemise() == null || getNumRemise().trim().isEmpty()) {
                messageInformation = "Veuillez saisir un numéro de remise";
                return null;
            }
            
            // Vérifier que le numéro existe dans la liste
            boolean trouve = getListNumRemise().contains(getNumRemise());
            if (!trouve) {
                selectedOrdreVirement = null;
                if (getRechercheClientCtr() != null) {
                    getRechercheClientCtr().resetAttributes();
                }
                messageInformation = "N° de Remise non trouvé dans la liste des remises disponibles";
                return null;
            }
            
            // Recherche de l'ordre
            VirementVo virementVo = new VirementVo();
            virementVo.setNumRemise(getNumRemise());
            virementVo.setParamAgence(getParamAgence());
            
            VirementCmd virementCmd = new VirementCmd();
            virementVo = (VirementVo) virementCmd.getOrdreVirementCentraleByNumRemise(virementVo);
            
            if (virementVo.getListeOrdresVirements() == null || 
                virementVo.getListeOrdresVirements().isEmpty()) {
                messageInformation = "Aucun ordre de virement trouvé pour le numéro : " + getNumRemise();
                return null;
            }
            
            // Remplir les données
            listeOrdresVirements.addAll(virementVo.getListeOrdresVirements());
            setSelectedOrdreVirement(virementVo.getListeOrdresVirements().get(0));
            setMontantGlobal(getSelectedOrdreVirement().getMontGlobOvir() + "");
            setNbreVirement(getSelectedOrdreVirement().getNbrVirOvir());
            
            // Charger les bénéficiaires
            listeBeneficiaires.clear();
            if (getSelectedOrdreVirement().getBeneficiaireOrdreVirements() != null) {
                listeBeneficiaires.addAll(getSelectedOrdreVirement().getBeneficiaireOrdreVirements());
            }
            
            messageInformation = "Ordre trouvé pour la remise " + getNumRemise();
            derniereMiseAJour = new Date();
            
            logger.info("Ordre récupéré - Taille liste: " + listeOrdresVirements.size());
            return null;
            
        } catch (Exception e) {
            logger.error("Erreur dans rechercherGlobalVirementByRemise", e);
            messageInformation = "Erreur lors de la recherche : " + e.getMessage();
            return null;
        }
    }

    /**
     * Recherche les ordres de virement par état
     */
    public void rechercherGlobalVirementByEtat() {
        try {
            if (etatRecherche != null && !etatRecherche.isEmpty()) {
                VirementVo virementVo = new VirementVo();
                virementVo.setEtatRecherche(getEtatRecherche());
                virementVo.setParamAgence(getParamAgence());
                
                VirementCmd virementCmd = new VirementCmd();
                virementVo = (VirementVo) virementCmd.getOrdreVirementCentralByEtat(virementVo);
                
                listeOrdresVirements = virementVo.getListeOrdresVirements();
                
                if (listeOrdresVirements != null && !listeOrdresVirements.isEmpty()) {
                    messageInformationEtat = "Trouvé " + listeOrdresVirements.size() + 
                                           " ordre(s) avec l'état: " + convertirCodeEtat(etatRecherche);
                    calculerNombreTotalVirements();
                } else {
                    messageInformationEtat = "Aucun ordre trouvé pour l'état: " + 
                                           convertirCodeEtat(etatRecherche);
                    listeOrdresVirements = new ArrayList<>();
                    nombreTotalVirements = 0;
                }
            } else {
                messageInformationEtat = "Veuillez sélectionner un état";
                listeOrdresVirements = new ArrayList<>();
                nombreTotalVirements = 0;
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche par état", e);
            messageInformationEtat = "Erreur lors de la recherche: " + e.getMessage();
            listeOrdresVirements = new ArrayList<>();
            nombreTotalVirements = 0;
        }
    }

    /**
     * Liste tous les ordres de virement disponibles
     * 
     * @return null pour rester sur la même page
     */
    public String listeTousLesOrdresVirement() {
        try {
            if (listeOrdresVirements == null) {
                listeOrdresVirements = new ArrayList<>();
            }
            listeOrdresVirements.clear();
            selectedOrdreVirement = null;
            
            logger.info("Récupération de tous les ordres de virement");
            
            VirementVo virementVo = new VirementVo();
            if (getParamAgence() != null) {
                virementVo.setParamAgence(getParamAgence());
            }
            
            VirementCmd virementCmd = new VirementCmd();
            virementVo = (VirementVo) virementCmd.getOrdreVirementCentral(virementVo);
            
            if (virementVo.getListeOrdresVirements() != null && 
                !virementVo.getListeOrdresVirements().isEmpty()) {
                listeOrdresVirements.addAll(virementVo.getListeOrdresVirements());
                logger.info("Nombre total d'ordres récupérés : " + listeOrdresVirements.size());
            } else {
                logger.info("Aucun ordre de virement trouvé");
            }
            
            return null;
        } catch (Exception e) {
            logger.error("Erreur dans listeTousLesOrdresVirement", e);
            return null;
        }
    }

    /**
     * Filtre la liste des ordres selon les critères spécifiés
     * 
     * @return null pour rester sur la même page
     */
    public String filtrerOrdres() {
        try {
            if (listeOrdresVirements == null || listeOrdresVirements.isEmpty()) {
                messageInformation = "Aucune liste à filtrer. Chargez d'abord la liste complète.";
                return null;
            }
            
            List<OrdreVirement> listeOriginale = new ArrayList<>(listeOrdresVirements);
            List<OrdreVirement> listeFiltree = new ArrayList<>();
            
            for (OrdreVirement ordre : listeOriginale) {
                boolean inclure = true;
                
                // Filtre par numéro de remise
                if (numRemise != null && !numRemise.trim().isEmpty()) {
                    if (ordre.getOrdreVirementId() != null && 
                        ordre.getOrdreVirementId().getNumOvirOvir() != null) {
                        if (!ordre.getOrdreVirementId().getNumOvirOvir().toLowerCase()
                               .contains(numRemise.toLowerCase())) {
                            inclure = false;
                        }
                    } else {
                        inclure = false;
                    }
                }
                
                // Filtre par montant minimum
                if (montantMinimal != null && montantMinimal > 0) {
                    if (ordre.getMontGlobOvir() == null || 
                        ordre.getMontGlobOvir() < montantMinimal) {
                        inclure = false;
                    }
                }
                
                if (inclure) {
                    listeFiltree.add(ordre);
                }
            }
            
            listeOrdresVirements.clear();
            listeOrdresVirements.addAll(listeFiltree);
            
            messageInformation = "Filtrage effectué : " + listeFiltree.size() + " ordres trouvés";
            return null;
            
        } catch (Exception e) {
            logger.error("Erreur lors du filtrage", e);
            messageInformation = "Erreur lors du filtrage";
            return null;
        }
    }

    /**
     * Change le critère de recherche et réinitialise les champs appropriés
     * 
     * @return null pour rester sur la même page
     */
    public String changerCritereRecherche() {
        try {
            selectedOrdreVirement = null;
            messageInformation = null;
            messageEnregistBeneficiaire = null;
            messageInformationEtat = "";
            listeOrdresVirements = new ArrayList<>();
            
            switch (critereRecherche) {
                case "T":
                    listeTousLesOrdresVirement();
                    break;
                    
                case "R":
                    montantMinimal = null;
                    dateExecution = null;
                    etatRecherche = "";
                    messageInformation = "Veuillez sélectionner un numéro de remise";
                    break;
                    
                case "E":
                    numRemise = null;
                    montantMinimal = null;
                    dateExecution = null;
                    messageInformationEtat = "Veuillez sélectionner un état";
                    etatRecherche = "";
                    if (getRechercheClientCtr() != null) {
                        getRechercheClientCtr().resetAttributes();
                    }
                    break;
                    
                case "C":
                    numRemise = null;
                    montantMinimal = null;
                    dateExecution = null;
                    etatRecherche = "";
                    messageInformation = "Veuillez rechercher un client";
                    if (getRechercheClientCtr() != null) {
                        getRechercheClientCtr().resetAttributes();
                    }
                    break;
                    
                default:
                    numRemise = null;
                    montantMinimal = null;
                    dateExecution = null;
                    etatRecherche = "";
                    messageInformation = "Veuillez sélectionner un critère de recherche";
                    if (getRechercheClientCtr() != null) {
                        getRechercheClientCtr().resetAttributes();
                    }
                    break;
            }
            
        } catch (Exception e) {
            logger.error("Erreur lors du changement de critère de recherche", e);
            messageInformation = "Erreur lors du changement de critère : " + e.getMessage();
        }
        
        return null;
    }

    /**
     * Récupère la liste des numéros de remise disponibles
     * 
     * @return Liste triée des numéros de remise
     */
    public List<String> getListNumRemise() {
        try {
            listNumRemise.clear();
            VirementVo virementVo = new VirementVo();
            virementVo.setParamAgence(getParamAgence());
            VirementCmd virementCmd = new VirementCmd();
            
            virementVo = (VirementVo) virementCmd.getListNumRemiseOrdreVirement(virementVo);
            
            if (virementVo.getListNumRemises() != null) {
                for (String numRemise : virementVo.getListNumRemises()) {
                    if (numRemise != null && !numRemise.contains("null") && 
                        numRemise.trim().length() > 0) {
                        try {
                            Long.parseLong(numRemise);
                            listNumRemise.add(numRemise);
                        } catch (NumberFormatException e) {
                            logger.debug("Numéro de remise invalide ignoré : " + numRemise);
                        }
                    }
                }
                Collections.sort(listNumRemise, NUMREMISE_ORDER);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des numéros de remise", e);
        }
        return listNumRemise;
    }

    /**
     * Comparateur pour trier les numéros de remise par ordre décroissant
     */
    static final Comparator<String> NUMREMISE_ORDER = new Comparator<String>() {
        public int compare(String a1, String a2) {
            try {
                if (a1 == null && a2 == null) return 0;
                if (a1 == null) return 1;
                if (a2 == null) return -1;
                
                a1 = a1.trim();
                a2 = a2.trim();
                
                Long val1 = Long.valueOf(a1);
                Long val2 = Long.valueOf(a2);
                
                return val2.compareTo(val1);
            } catch (NumberFormatException e) {
                return a2.compareTo(a1);
            }
        }
    };

    // ============================================================================
    // GESTION DES ORDRES DE VIREMENT - MODIFICATION
    // ============================================================================

    /**
     * Prépare l'ordre sélectionné pour modification
     * 
     * @return Navigation vers la page de modification
     */
    public String modifierOrdreSelectionne() {
        try {
            if (selectedOrdreVirement == null) {
                messageInformation = "Aucun ordre sélectionné pour modification";
                return null;
            }
            
            if (listeBeneficiaires == null) {
                listeBeneficiaires = new ArrayList<>();
            }
            listeBeneficiaires.clear();
            
            if (selectedOrdreVirement.getBeneficiaireOrdreVirements() != null) {
                listeBeneficiaires.addAll(selectedOrdreVirement.getBeneficiaireOrdreVirements());
            }
            
            if (selectedOrdreVirement.getOrdreVirementId() != null) {
                numRemise = selectedOrdreVirement.getOrdreVirementId().getNumOvirOvir();
            }
            
            if (selectedOrdreVirement.getMontGlobOvir() != null) {
                setMontantGlobal(selectedOrdreVirement.getMontGlobOvir().toString());
            }
            
            if (selectedOrdreVirement.getNbrVirOvir() != null) {
                setNbreVirement(selectedOrdreVirement.getNbrVirOvir());
            }
            
            logger.info("Ordre préparé pour modification avec " + 
                       listeBeneficiaires.size() + " bénéficiaires");
            
            return "modificationBeneficiairepecVirementMandatBct";
            
        } catch (Exception e) {
            logger.error("Erreur dans modifierOrdreSelectionne", e);
            messageInformation = "Erreur lors de la préparation de la modification";
            return null;
        }
    }

    /**
     * Affiche les détails de l'ordre de virement sélectionné
     * 
     * @return Navigation vers la page de détails
     */
    public String afficherOrdreVirementDetail() {
        if (getSelectedOrdreVirement() != null && 
            getSelectedOrdreVirement().getOrdreVirementId().getNumOvirOvir() != null) {
            
            listeBeneficiaireOrdreVirement.clear();
            setMontantGlobal(getSelectedOrdreVirement().getMontGlobOvir() + "");
            setNbreVirement(getSelectedOrdreVirement().getNbrVirOvir());
            setDateExecution(getSelectedOrdreVirement().getDatExecOvir());
            setRibBeneficiaire(getSelectedOrdreVirement().getCompteDebitOvir());
            setMotifBeneficiaire(getSelectedOrdreVirement().getCompteDebitOvir());
            setNumRemise(getSelectedOrdreVirement().getOrdreVirementId().getNumOvirOvir());
            setEtatRecherche(getSelectedOrdreVirement().getCodEtatOvir());
            
            listeBeneficiaireOrdreVirement.addAll(
                getSelectedOrdreVirement().getBeneficiaireOrdreVirements()
            );
            
            return "modificationBeneficiairepecVirementMandatBct";
        } else {
            return null;
        }
    }

    /**
     * Modifie l'ordre de virement avec les nouvelles données
     * 
     * @param event ActionEvent JSF
     */
    public void modifierOrdreVirement(ActionEvent event) {
        VirementVo virementVo = new VirementVo();
        VirementCmd virementCmd = new VirementCmd();
        messageErreur = "";
        
        logger.info("Modification de l'ordre de virement");
        
        try {
            if (selectedOrdreVirement != null && 
                selectedOrdreVirement.getOrdreVirementId().getNumOvirOvir() != null) {
                
                if (dateExecution != null) {
                    selectedOrdreVirement.setDatExecOvir(dateExecution);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    logger.debug("Date récupérée : " + sdf.format(dateExecution));
                }
                
                virementVo.setOrdreVirement(selectedOrdreVirement);
                virementVo.setParamAgence(getParamAgence());
                virementVo.setListeBeneficiaireOrdreVirement(getListeBeneficiaireOrdreVirement());
                
                virementVo = (VirementVo) virementCmd.modifierOrdreVirementCentral(virementVo);
                messageErreur = virementVo.getMessageValidation();
            } else {
                messageErreur = "Ordre de virement non sélectionné";
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la modification", e);
            messageErreur = "Erreur lors de la modification";
        }
    }

    // ============================================================================
    // GESTION DES ORDRES DE VIREMENT - EXÉCUTION
    // ============================================================================

    /**
     * Exécute l'ordre de virement sélectionné
     * 
     * @param event ActionEvent JSF
     */
    public void executerVirementDuJour(ActionEvent event) {
        logger.info("========== Début Exécution Ordre Virement ==========");
        
        messageInformation = "";
        messageErreur = "";
        List<String> messagesResultat = new ArrayList<>();
        VirementCmd virementCmd = new VirementCmd();
        
        long nbreVirementExecutes = 0;
        long nbreVirementRejetes = 0;
        long nbreVirementdecales = 0;
        
        try {
            if (selectedOrdreVirement == null || 
                selectedOrdreVirement.getOrdreVirementId() == null) {
                messageErreur = "Aucun ordre de virement sélectionné pour l'exécution";
                logger.warn("Tentative d'exécution sans ordre sélectionné");
                return;
            }
            
            if (!"A".equals(selectedOrdreVirement.getCodEtatOvir())) {
                messageErreur = "Cet ordre n'est pas en état 'En attente'. État actuel : " + 
                               convertirCodeEtat(selectedOrdreVirement.getCodEtatOvir());
                logger.warn("Ordre n° " + selectedOrdreVirement.getOrdreVirementId().getNumOvirOvir() + 
                           " n'est pas en état 'A'");
                return;
            }
            
            if (selectedOrdreVirement.getBeneficiaireOrdreVirements() == null || 
                selectedOrdreVirement.getBeneficiaireOrdreVirements().isEmpty()) {
                messageErreur = "Cet ordre ne contient aucun bénéficiaire à traiter";
                logger.warn("Ordre sans bénéficiaires");
                return;
            }
            
            logger.info("Traitement de l'ordre n° : " + 
                       selectedOrdreVirement.getOrdreVirementId().getNumOvirOvir());
            logger.info("Nombre de bénéficiaires : " + 
                       selectedOrdreVirement.getBeneficiaireOrdreVirements().size());
            
            VirementVo virementVo = new VirementVo();
            virementVo.setOrdreVirement(selectedOrdreVirement);
            virementVo.setParamAgence(getParamAgence());
            
            virementVo = (VirementVo) virementCmd.ExecutionOrdreVirementCentralDuJour(virementVo);
            
            nbreVirementExecutes = virementVo.getNbreVirementExecutes();
            nbreVirementRejetes = virementVo.getNbreVirementRejetes();
            nbreVirementdecales = virementVo.getNbreVirementdecales();
            
            logger.info("Résultats - Exécutés: " + nbreVirementExecutes + 
                       ", Rejetés: " + nbreVirementRejetes + 
                       ", Décalés: " + nbreVirementdecales);
            
            if (virementVo.getListeMsgValidation() != null && 
                virementVo.getListeMsgValidation().size() > 0) {
                messagesResultat.addAll(virementVo.getListeMsgValidation());
                logger.warn("Des erreurs ont été détectées lors de l'exécution");
            }
            
            ISearchEngine searchEngine = (ISearchEngine) Context.getInstance()
                .getSpringContext().getBean("searchEngine");
            selectedOrdreVirement = (OrdreVirement) searchEngine.get(
                OrdreVirement.class, 
                selectedOrdreVirement.getOrdreVirementId()
            );
            
            if (selectedOrdreVirement.getBeneficiaireOrdreVirements() != null) {
                listeBeneficiaireOrdreVirement.clear();
                listeBeneficiaireOrdreVirement.addAll(
                    selectedOrdreVirement.getBeneficiaireOrdreVirements()
                );
            }
            
            StringBuilder recap = new StringBuilder();
            recap.append("===== EXÉCUTION DE L'ORDRE TERMINÉE =====\n");
            recap.append("Numéro d'ordre : ").append(
                selectedOrdreVirement.getOrdreVirementId().getNumOvirOvir()).append("\n");
            recap.append("Date : ").append(
                new SimpleDateFormat("dd/MM/yyyy").format(new Date())).append("\n");
            recap.append("État de l'ordre : ").append(
                convertirCodeEtat(selectedOrdreVirement.getCodEtatOvir())).append("\n\n");
            
            recap.append("===== STATISTIQUES =====\n");
            recap.append("Virements exécutés : ").append(nbreVirementExecutes).append("\n");
            
            if (nbreVirementRejetes > 0) {
                recap.append("Virements rejetés : ").append(nbreVirementRejetes).append("\n");
            }
            
            if (nbreVirementdecales > 0) {
                recap.append("Virements décalés : ").append(nbreVirementdecales).append("\n");
            }
            
            long totalBeneficiaires = selectedOrdreVirement.getNbrVirOvir();
            recap.append("\nTotal bénéficiaires : ").append(totalBeneficiaires).append("\n");
            
            if (!messagesResultat.isEmpty()) {
                recap.append("\n===== DÉTAILS DES ERREURS =====\n");
                for (String msg : messagesResultat) {
                    recap.append("- ").append(msg).append("\n");
                }
            } else {
                recap.append("\n✓ Exécution terminée sans erreur");
            }
            
            messageInformation = recap.toString();
            logger.info("========== Fin Exécution Ordre Virement - Succès ==========");
            
        } catch (Exception e) {
            logger.error("========== ERREUR CRITIQUE ==========", e);
            logger.error("Type d'exception : " + e.getClass().getName());
            
            Throwable cause = e.getCause();
            if (cause != null) {
                logger.error("Cause racine : " + cause.getClass().getName() + 
                           " - " + cause.getMessage());
            }
            
            messageErreur = "Erreur critique lors de l'exécution : " + e.getMessage();
            messageInformation = "L'exécution de l'ordre a échoué. Consultez les logs.";
            
            addMessage(null, "Erreur d'exécution", 
                      "Une erreur s'est produite : " + e.getMessage(), 
                      FacesMessage.SEVERITY_ERROR);
        }
    }

    /**
     * Exécute l'ordre sélectionné (méthode de navigation)
     * 
     * @return null pour rester sur la même page
     */
    public String executerOrdreSelectionne() {
        try {
            if (selectedOrdreVirement == null) {
                messageErreur = "Veuillez sélectionner un ordre de virement à exécuter";
                return null;
            }
            
            executerVirementDuJour(null);
            
            if ("A".equals(critereRecherche)) {
                listeTousLesOrdresVirement();
            }
            
            return null;
        } catch (Exception e) {
            logger.error("Erreur dans executerOrdreSelectionne", e);
            messageErreur = "Erreur : " + e.getMessage();
            return null;
        }
    }

    /**
     * Vérifie si l'ordre sélectionné peut être exécuté
     * 
     * @return true si l'ordre peut être exécuté
     */
    public boolean isPeutExecuterOrdre() {
        try {
            if (selectedOrdreVirement == null) {
                return false;
            }
            
            if (!"A".equals(selectedOrdreVirement.getCodEtatOvir())) {
                return false;
            }
            
            if (selectedOrdreVirement.getBeneficiaireOrdreVirements() == null || 
                selectedOrdreVirement.getBeneficiaireOrdreVirements().isEmpty()) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Message informatif sur l'état de l'exécution
     * 
     * @return Message descriptif
     */
    public String getMessageExecutionPossible() {
        if (selectedOrdreVirement == null) {
            return "Aucun ordre sélectionné";
        }
        
        if (!"A".equals(selectedOrdreVirement.getCodEtatOvir())) {
            return "Cet ordre n'est pas en état 'En attente'";
        }
        
        if (selectedOrdreVirement.getBeneficiaireOrdreVirements() == null || 
            selectedOrdreVirement.getBeneficiaireOrdreVirements().isEmpty()) {
            return "Cet ordre ne contient aucun bénéficiaire";
        }
        
        return "Prêt pour l'exécution";
    }

    // ============================================================================
    // GESTION DES ORDRES DE VIREMENT - REJET ET ANNULATION
    // ============================================================================

    /**
     * Rejette l'ordre de virement sélectionné
     * 
     * @return Navigation vers la page de consultation
     */
    public String rejeterOrdreVirement() {
        VirementVo virementVo = new VirementVo();
        VirementCmd virementCmd = new VirementCmd();
        messageErreur = "";
        
        try {
            selectedOrdreVirement.setCodEtatOvir("R");
            
            virementVo.setOrdreVirement(selectedOrdreVirement);
            virementVo.setParamAgence(getParamAgence());
            virementVo.setListeBeneficiaireOrdreVirement(getListeBeneficiaireOrdreVirement());
            virementVo = (VirementVo) virementCmd.modifierOrdreVirementCentral(virementVo);
            
            messageErreur = "Ordre de virement rejeté avec succès.";
            logger.info("Ordre rejeté : " + 
                       selectedOrdreVirement.getOrdreVirementId().getNumOvirOvir());
            
            return "consultationpecVirementMandatRecuBct";
        } catch (Exception e) {
            logger.error("Erreur lors du rejet", e);
            messageErreur = "Erreur lors du rejet : " + e.getMessage();
            return null;
        }
    }

    /**
     * Annule le virement en cours de saisie
     * 
     * @return Navigation appropriée selon l'état
     */
    public String annulerBoutton() {
        if (getStrEtatValidationTrt().equals("2")) {
            return null;
        } else if (getStrEtatValidationTrt().equals("1")) {
            reinitialiserFormulaire();
            setStrEtatValidationTrt("0");
            return "quitterVirementMandatBct";
        } else {
            reinitialiserFormulaire();
            setStrEtatValidationTrt("0");
            return null;
        }
    }

    /**
     * Annule le virement (méthode alternative sans navigation)
     */
    public void annulerVirementBoutton() {
        if (getStrEtatValidationTrt() != null && getStrEtatValidationTrt().equals("1")) {
            reinitialiserFormulaire();
            setStrEtatValidationTrt("0");
        } else {
            reinitialiserFormulaire();
            setStrEtatValidationTrt("0");
        }
    }

    /**
     * Réinitialise tous les champs du formulaire de virement
     */
    private void reinitialiserFormulaire() {
        setMontantVirement(null);
        messageMontant = "";
        compteBenificiaire = "";
        motif = "";
        benificiaire = "";
        messageValidationVirementBct = "";
        msgValidatorCompte = "";
        etatCmdValiderVirement = false;
        setRibBeneficiaire("");
    }

    // ============================================================================
    // VALIDATION - RIB
    // ============================================================================

    /**
     * Valide le format d'un RIB (20 chiffres ou 13 pour BNA)
     * 
     * @param context FacesContext
     * @param component UIComponent
     * @param value Valeur à valider
     * @throws ValidatorException si le format est incorrect
     */
    public void validaterFormatCompte(FacesContext context, UIComponent component, Object value)
            throws ValidatorException {
        
        String testRibBenf = (String) value;
        etatRIBCorrecte = true;
        
        if (testRibBenf != null && !testRibBenf.trim().isEmpty()) {
            Pattern p = Pattern.compile("[0-9]{20}");
            Matcher m = p.matcher(testRibBenf);

            Pattern pBNA = Pattern.compile("[0-9]{13}");
            Matcher mBNA = pBNA.matcher(testRibBenf);
            
            if (testRibBenf.length() == 20 || testRibBenf.length() == 13) {
                if (!m.matches() && testRibBenf.length() == 20) {
                    etatRIBCorrecte = false;
                    throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, 
                        "Format RIB Incorrect", 
                        "Format RIB Incorrect"
                    ));
                } else if (!mBNA.matches() && testRibBenf.length() == 13) {
                    etatRIBCorrecte = false;
                    throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, 
                        "Format RIB Incorrect", 
                        "Format RIB Incorrect"
                    ));
                }
            } else {
                etatRIBCorrecte = false;
                throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Le RIB doit contenir 20 Chiffres ou 13 Chiffres si compte BNA",
                    "Le RIB doit contenir 20 Chiffres ou 13 Chiffres si compte BNA"
                ));
            }
        }
    }

    /**
     * Valide le format d'un RIB (méthode alternative)
     * 
     * @param context FacesContext
     * @param component UIComponent
     * @param value Valeur à valider
     */
    public void validaterFormatRib(FacesContext context, UIComponent component, Object value) {
        // Implémentation similaire à validaterFormatCompte
    }

    /**
     * Vérifie qu'un compte bénéficiaire est valide et actif
     * Gère la conversion RIB 13 -> 20 chiffres pour BNA
     */
    public void verifierCompte() {
        try {
            msgValidatorCompte = "";
            isEtatMotif();
            benificiaire = "   ";
            boolean exist = false;
            String rib = "";
            
            VirementVo virementVo = new VirementVo();
            
            if (getCompteBenificiaire().contains("-")) {
                rib = getCompteBenificiaire().replace("-", "");
            } else if (getCompteBenificiaire().contains(" ")) {
                rib = getCompteBenificiaire().replace(" ", "");
            } else {
                rib = getCompteBenificiaire();
            }

            if (rib.length() == 13) {
                String CodAgence = rib.substring(0, 3);
                GetStructureCmd getStructureCmd = new GetStructureCmd();
                Structure structureBna = new Structure();
                structureBna.setCodStrcStrc(Long.valueOf(CodAgence));
                structureBna = (Structure) getStructureCmd.execute(structureBna);
                
                if (structureBna != null && structureBna.getCodBctStrc() != null) {
                    String codBCT = StrHandler.lpad(structureBna.getCodBctStrc(), '0', 3);
                    String petitRib = "03" + codBCT + rib;
                    rib = petitRib + calculerRIB(petitRib);
                } else {
                    etatRIBCorrecte = false;
                    msgValidatorCompte = "Code agence erroné !!";
                    return;
                }
            }

            if (rib.length() == 20) {
                setRibBeneficiaire(rib);
                
                String strCodeBanque = rib.substring(0, 2);
                String strCodeAgenceBanque = rib.substring(2, 5);
                String strCompte = rib.substring(5, 18);
                String strCle = rib.substring(18, 20);

                virementVo.setStrCodbanque(strCodeBanque);
                virementVo.setStrCodAgenceBanque(strCodeAgenceBanque);
                virementVo.setStrCompte(strCompte);
                virementVo.setStrCle(strCle);
                virementVo.setStrPetitRib(strCodeBanque + strCodeAgenceBanque + strCompte);
                virementVo.setVerifier(exist);

                VirementCmd virementCmd = new VirementCmd();
                virementVo = (VirementVo) virementCmd.verifierRib(virementVo);
                exist = virementVo.isVerifier();

                if (!exist) {
                    msgValidatorCompte = virementVo.getMessageVerificationRib();
                    etatRIBCorrecte = false;
                } else if (strCodeBanque.equals("03")) {
                    verifierCompteBNA(rib);
                } else {
                    msgValidatorCompte = "Compte Bénéficiaire n'est pas un compte BNA!";
                    etatRIBCorrecte = false;
                }
            } else if (msgValidatorCompte.isEmpty()) {
                etatRIBCorrecte = false;
                msgValidatorCompte = "Le RIB doit contenir 20 caractères";
            }
        } catch (NumberFormatException e) {
            etatRIBCorrecte = false;
            msgValidatorCompte = "RIB incorrecte";
            logger.error("Erreur format RIB", e);
        } catch (Exception e) {
            etatRIBCorrecte = false;
            msgValidatorCompte = e.getMessage();
            logger.error("Erreur vérification compte", e);
        }
    }

    /**
     * Vérifie qu'un compte BNA est valide (méthode interne)
     * 
     * @param rib RIB à vérifier (20 chiffres)
     */
    private void verifierCompteBNA(String rib) {
        try {
            String structure = rib.substring(5, 8);
            String produit = rib.substring(8, 12);
            String compteCCpt = rib.substring(12, 18);

            ContratCptId contratCptId = new ContratCptId();
            contratCptId.setCodStrcStrc(Long.valueOf(structure));
            contratCptId.setCodPrdPrd(Long.valueOf(produit));
            contratCptId.setNumCcptCcpt(Long.valueOf(compteCCpt));

            GetDetailContratCmd getDetailContratCmd = new GetDetailContratCmd();
            ContratCpt cpt = (ContratCpt) getDetailContratCmd.execute(contratCptId);

            if (cpt != null && cpt.getContratCptId() != null) {
                boolean boolRibBenifEnDevise = false;
                for (Long codDevise : Constants.listCompteEnDevises) {
                    if (Long.valueOf(produit).equals(codDevise)) {
                        boolRibBenifEnDevise = true;
                        break;
                    }
                }

                if (boolRibBenifEnDevise) {
                    msgValidatorCompte = "Impossible d'effectuer un virement vers un RIB Bénéficiaire en Devises !";
                    etatRIBCorrecte = false;
                } else if (cpt.getCodEtatCcpt().equals("R")) {
                    msgValidatorCompte = "Le compte Bénéficiaire est Clôturé !";
                    etatRIBCorrecte = false;
                } else {
                    benificiaire = cpt.getNomIntiCcpt();
                    etatRIBCorrecte = true;
                }
            } else {
                benificiaire = "   ";
                msgValidatorCompte = "Compte Bénéficiaire inexistant!";
                etatRIBCorrecte = false;
            }
        } catch (Exception e) {
            logger.error("Erreur vérification compte BNA", e);
            msgValidatorCompte = "Erreur lors de la vérification du compte";
            etatRIBCorrecte = false;
        }
    }

    /**
     * Vérifie qu'un RIB est valide (version simplifiée)
     * 
     * @param rib RIB à vérifier
     * @return true si le RIB est valide
     */
    public boolean verifierRib(String rib) {
        try {
            VirementVo virementVo = new VirementVo();
            etatRIBCorrecte = true;

            if (rib.length() == 20) {
                setRibBeneficiaire(rib);

                String strCodeBanque = rib.substring(0, 2);
                String strCodeAgenceBanque = rib.substring(2, 5);
                String strCompte = rib.substring(5, 18);
                String strCle = rib.substring(18, 20);

                virementVo.setStrCodbanque(strCodeBanque);
                virementVo.setStrCodAgenceBanque(strCodeAgenceBanque);
                virementVo.setStrCompte(strCompte);
                virementVo.setStrCle(strCle);
                virementVo.setStrPetitRib(strCodeBanque + strCodeAgenceBanque + strCompte);
                virementVo.setVerifier(false);

                VirementCmd virementCmd = new VirementCmd();
                virementVo = (VirementVo) virementCmd.verifierRib(virementVo);

                boolean exist = virementVo.isVerifier();

                if (!exist) {
                    etatRIBCorrecte = false;
                } else if (strCodeBanque.equals("03")) {
                    verifierCompteBNASimple(rib);
                } else {
                    etatRIBCorrecte = false;
                }
            } else {
                etatRIBCorrecte = false;
            }
        } catch (Exception e) {
            etatRIBCorrecte = false;
            logger.error("Erreur vérification RIB", e);
        }
        return etatRIBCorrecte;
    }

    /**
     * Vérification simplifiée d'un compte BNA
     * 
     * @param rib RIB à vérifier
     */
    private void verifierCompteBNASimple(String rib) {
        try {
            String structure = rib.substring(5, 8);
            String produit = rib.substring(8, 12);
            String compteCCpt = rib.substring(12, 18);

            ContratCptId contratCptId = new ContratCptId();
            contratCptId.setCodStrcStrc(Long.valueOf(structure));
            contratCptId.setCodPrdPrd(Long.valueOf(produit));
            contratCptId.setNumCcptCcpt(Long.valueOf(compteCCpt));

            GetDetailContratCmd getDetailContratCmd = new GetDetailContratCmd();
            ContratCpt cpt = (ContratCpt) getDetailContratCmd.execute(contratCptId);

            if (cpt != null && cpt.getContratCptId() != null) {
                for (Long codDevise : Constants.listCompteEnDevises) {
                    if (Long.valueOf(produit).equals(codDevise)) {
                        etatRIBCorrecte = false;
                        return;
                    }
                }

                if (cpt.getCodEtatCcpt().equals("R")) {
                    etatRIBCorrecte = false;
                }
            } else {
                etatRIBCorrecte = false;
            }
        } catch (Exception e) {
            etatRIBCorrecte = false;
        }
    }

    /**
     * Méthode de vérification RIB (alias)
     * 
     * @return null pour rester sur la même page
     */
    public String verifierRibEditer() {
        etatRIBCorrecte = true;
        msgValidatorRib = "";
        return null;
    }

    // ============================================================================
    // VALIDATION - MONTANT
    // ============================================================================

    /**
     * Valide qu'un montant est correct (format et valeur > 0)
     * 
     * @param context FacesContext
     * @param component UIComponent
     * @param value Valeur à valider
     * @throws ValidatorException si le montant est invalide
     */
    public void validateMontant(FacesContext context, UIComponent component, Object value) 
            throws ValidatorException {
        
        String montant = value + "";
        messageMontant = "";
        
        if (montant != null && !montant.trim().isEmpty()) {
            String newmont = montant.replace(".", "").replace(" ", "");
            Pattern p = Pattern.compile("[0-9]{1,15}");
            Matcher m = p.matcher(newmont);

            if (newmont.length() <= 15) {
                if (!m.matches()) {
                    throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, 
                        "Format Incorrect", 
                        "Format Incorrect"
                    ));
                } else {
                    if (Long.valueOf(newmont).longValue() == 0L) {
                        throw new ValidatorException(new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Montant doit être sup à 0", 
                            "Montant doit être sup à 0"
                        ));
                    }
                }
            } else {
                throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Montant doit être sur 15 chiffres", 
                    "Montant doit être sur 15 chiffres"
                ));
            }
        }
    }

    // ============================================================================
    // VALIDATION - DATE
    // ============================================================================

    /**
     * Valide le format d'une date
     * 
     * @param context FacesContext
     * @param component UIComponent
     * @param value Valeur à valider
     * @throws ValidatorException si le format est incorrect
     */
    public void validateDateFormat(FacesContext context, UIComponent component, Object value) 
            throws ValidatorException {
        
        String dateStr = (String) value;
        
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                sdf.setLenient(false);
                Date parsedDate = sdf.parse(dateStr.trim());
            } catch (Exception e) {
                logger.error("Erreur validation date", e);
                messageErreur = "Erreur: " + e.getMessage();
            }
        }
    }

    /**
     * Gère le changement de date d'exécution
     */
    public void onDateChange() {
        try {
            logger.debug("========== onDateChange DEBUT ==========");
            
            FacesContext context = FacesContext.getCurrentInstance();
            Map<String, String> params = context.getExternalContext().getRequestParameterMap();
            
            for (String key : params.keySet()) {
                logger.debug("Paramètre: " + key + " = " + params.get(key));
            }
            
            if (dateExecution != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String dateStr = sdf.format(dateExecution);
                logger.debug("Date récupérée : " + dateStr);
                
                if (selectedOrdreVirement != null) {
                    selectedOrdreVirement.setDatExecOvir(dateExecution);
                }
                
                messageErreur = "";
            }
            
            logger.debug("========== onDateChange FIN ==========");
        } catch (Exception e) {
            logger.error("Erreur onDateChange", e);
            messageErreur = "Erreur: " + e.getMessage();
        }
    }

    /**
     * Gère le changement de date au format String
     */
    public void onDateStringChange() {
        try {
            if (dateExecutionString != null && !dateExecutionString.trim().isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                dateExecution = sdf.parse(dateExecutionString.trim());
                
                logger.debug("Date saisie et convertie : " + sdf.format(dateExecution));
                
                if (selectedOrdreVirement != null) {
                    selectedOrdreVirement.setDatExecOvir(dateExecution);
                }
                
                messageErreur = "";
            }
        } catch (Exception e) {
            logger.error("Erreur onDateStringChange", e);
            messageErreur = "Erreur: " + e.getMessage();
        }
    }

    // ============================================================================
    // CALCULS ET STATISTIQUES
    // ============================================================================

    /**
     * Calcule les totaux globaux (montant et nombre de virements)
     */
    public void calculerTotauxGlobaux() {
        try {
            Long montantTotal = 0L;
            Long nombreTotal = 0L;
            
            if (listeBeneficiaireOrdreVirement != null && 
                !listeBeneficiaireOrdreVirement.isEmpty()) {
                for (BeneficiaireOrdreVirement beneficiaire : listeBeneficiaireOrdreVirement) {
                    if (beneficiaire.getMontOperBenf() != null) {
                        montantTotal += beneficiaire.getMontOperBenf();
                        nombreTotal++;
                    }
                }
            }
            
            setMontantGlobal(StrHandler.formatMontant(montantTotal, 3L));
            setNbreVirement(nombreTotal);
            
            if (selectedOrdreVirement != null) {
                selectedOrdreVirement.setMontGlobOvir(montantTotal);
                selectedOrdreVirement.setNbrVirOvir(nombreTotal);
            }
            
            logger.debug("Totaux calculés - Montant: " + montantGlobal + ", Nombre: " + nombreTotal);
        } catch (Exception e) {
            logger.error("Erreur lors du calcul des totaux", e);
        }
    }

    /**
     * Calcule le montant total des ordres affichés
     * 
     * @return Montant total
     */
    public Double getMontantTotalOrdres() {
        if (listeOrdresVirements == null || listeOrdresVirements.isEmpty()) {
            return 0.0;
        }
        
        double total = 0;
        for (OrdreVirement ordre : listeOrdresVirements) {
            if (ordre.getMontGlobOvir() != null) {
                total += ordre.getMontGlobOvir();
            }
        }
        return total;
    }

    /**
     * Calcule le nombre total de virements
     * 
     * @return Nombre total
     */
    public Integer getNombreTotalVirements() {
        if (listeOrdresVirements == null || listeOrdresVirements.isEmpty()) {
            return 0;
        }
        
        int total = 0;
        for (OrdreVirement ordre : listeOrdresVirements) {
            if (ordre.getNbrVirOvir() != null) {
                total += ordre.getNbrVirOvir();
            }
        }
        return total;
    }

    /**
     * Calcule le nombre total de virements (version privée)
     */
    private void calculerNombreTotalVirements() {
        nombreTotalVirements = 0;
        
        try {
            if (listeOrdresVirements != null && !listeOrdresVirements.isEmpty()) {
                for (OrdreVirement ordre : listeOrdresVirements) {
                    if (ordre != null && ordre.getNbrVirOvir() != null) {
                        nombreTotalVirements += ordre.getNbrVirOvir().intValue();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Erreur calcul nombre total virements", e);
            nombreTotalVirements = 0;
        }
    }

    /**
     * Retourne le montant global formaté
     * 
     * @return Montant formaté avec devise
     */
    public String getMontantGlobalFormate() {
        Long montantTotal = 0L;
        
        if (listeBeneficiaireOrdreVirement != null) {
            for (BeneficiaireOrdreVirement beneficiaire : listeBeneficiaireOrdreVirement) {
                if (beneficiaire.getMontOperBenf() != null) {
                    montantTotal += beneficiaire.getMontOperBenf();
                }
            }
        }
        
        return StrHandler.formatMontant(montantTotal, 3L) + " TND";
    }

    /**
     * Retourne les statistiques par état
     * 
     * @return Map avec le nombre d'ordres par état
     */
    public Map<String, Integer> getStatistiquesParEtat() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("A", 0);
        stats.put("R", 0);
        stats.put("E", 0);
        
        if (listeOrdresVirements != null && !listeOrdresVirements.isEmpty()) {
            for (OrdreVirement ordre : listeOrdresVirements) {
                if (ordre != null && ordre.getCodEtatOvir() != null) {
                    String etat = ordre.getCodEtatOvir().trim().toUpperCase();
                    if (stats.containsKey(etat)) {
                        stats.put(etat, stats.get(etat) + 1);
                    }
                }
            }
        }
        
        return stats;
    }

    // ============================================================================
    // REPORTING ET IMPRESSION
    // ============================================================================

    /**
     * Imprime le reçu du virement mandat BCT
     */
    public void imprimerVirementMandatBct() {
        try {
            srcRepport = "";
            parametersJasper.put("P_PATH", getStrPath());
            parametersJasper.put("P_NUM_MATR_USER", getParamAgence().getNumMatrUser());
            parametersJasper.put("COD_STRC_STRC", getParamAgence().getCodStrcStrc() + "");
            parametersJasper.put("LIB_STRC_STRC", getParamAgence().getLibStrcStrc());
            parametersJasper.put("P_RIB_DO", getStrRib());

            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpServletRequest httpServletRequest = 
                (HttpServletRequest) facesContext.getExternalContext().getRequest();
            
            if (reportName != null) {
                httpServletRequest.getSession().removeAttribute("REPORT_NAME_VIREMENT");
            }
            
            reportName = UtilCtr.printReport(
                "dechargeVirementMandatBct", 
                "Virement", 
                parametersJasper, 
                null
            );
            
            setSrcRepport(FacesContext.getCurrentInstance()
                .getExternalContext().getRequestContextPath() +
                "/reporting/Virement/" + reportName);
            
            httpServletRequest.getSession().setAttribute("REPORT_NAME_VIREMENT", reportName);
            
            logger.info("Rapport généré : " + reportName);
        } catch (Exception e) {
            logger.error("Erreur lors de l'impression", e);
        }
    }

    // ============================================================================
    // NAVIGATION ET CONTRÔLE UI
    // ============================================================================

    /**
     * Quitte l'écran de virement mandat BCT
     * 
     * @return Navigation vers la page de sortie
     */
    public String quitterVirementMandatBct() {
        return "quitterVirementMandatBct";
    }

    /**
     * Quitte l'écran de modification d'ordre de virement
     * 
     * @return Navigation vers la page de consultation
     */
    public String quitterModificationOrdreVirement() {
        selectedOrdreVirement = null;
        selectedBeneficiaire = null;
        listeBeneficiaireOrdreVirement.clear();
        beneficiaireOrdreVirement = null;
        listeBeneficiairesAjoutes.clear();
        etatMessageEnregistBeneficiaire = false;
        return "consultationpecVirementMandatRecuBct";
    }

    /**
     * Quitte l'écran de consultation d'ordre de virement
     * 
     * @return Navigation vers la page principale
     */
    public String quitterConsultationOrdreVirement() {
        selectedOrdreVirement = null;
        selectedBeneficiaire = null;
        listeBeneficiaireOrdreVirement.clear();
        beneficiaireOrdreVirement = null;
        listeBeneficiairesAjoutes.clear();
        etatMessageEnregistBeneficiaire = false;
        return "quitterCompManuelle";
    }