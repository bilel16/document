package com.bna.smile.web.virement.controller;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;
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
import javax.sql.DataSource;

import org.ajax4jsf.model.KeepAlive;
import org.apache.log4j.Logger;

import com.bna.commun.model.*;
import com.bna.commun.util.ContextHandler;
import com.bna.commun.util.DateHandler;
import com.bna.commun.util.StrHandler;
import com.bna.smile.model.constant.Constants;
import com.bna.smile.model.domainecommun.commande.GetDetailContratCmd;
import com.bna.smile.model.domainecommun.commande.GetStructureCmd;
import com.bna.smile.model.domainecommun.service.CRUDservice;
import com.bna.smile.model.virement.commande.VirementCmd;
import com.bna.smile.model.virement.model.VirementVo;
import com.bna.smile.web.commun.controller.RechercheClientByStructureCtr;
import com.bna.smile.web.commun.controller.UtilCtr;
import com.bna.smile.web.commun.model.ParamAgence;
import com.ibm.icu.math.BigDecimal;
import com.ibm.icu.util.Calendar;
import com.oxia.fwk.context.Context;
import com.oxia.fwk.core.ICriteria;
import com.oxia.fwk.core.IExpression;
import com.oxia.fwk.core.ISearchEngine;

import jxl.write.DateTime;

/**
 * Contrôleur pour la gestion des virements centraux
 * Permet la prise en charge des ordres de virement au niveau central
 * 
 * @author BNA
 * @version 1.0
 */
@KeepAlive
public class PecVirementCentralCtr implements Serializable {

    // ==================== CONSTANTES ====================
    
    private static final long serialVersionUID = 1L;
    private static final int LIMITE_MAX_BENEFICIAIRES = 10;
    public static final Long COD_PRD_VIREMENT_PONCTUEL = 1063L;
    public static final Long ETAT_VIREMENT_PONCTUEL_ENCOUR = 0L;
    
    // ==================== LOGGER ====================
    
    private final Logger logger = Logger.getLogger(PecVirementCentralCtr.class);
    
    // ==================== PARAMETRES ET CONFIGURATION ====================
    
    private ParamAgence paramAgence = new ParamAgence();
    private RechercheClientByStructureCtr rechercheClientCtr;
    private String critereRecherche = "R";
    
    // ==================== INFORMATIONS STRUCTURE ====================
    
    private String strLibStructure = "";
    private String strCodeStructure = "";
    private String strPath = "";
    private String strNumMatUser = "";
    
    // ==================== GESTION DES BENEFICIAIRES ====================
    
    // Données du bénéficiaire en cours de saisie
    private String ribBeneficiaire = "";
    private String nomBeneficiaire = "";
    private long montantBeneficiaire;
    private String motifBeneficiaire;
    private String compteBenificiaire;
    private String benificiaire = "";
    private String motif;
    private String ribBenificiaire = "";
    
    // Listes de bénéficiaires
    private List<BeneficiaireOrdreVirement> listeBeneficiaireOrdreVirement = new ArrayList<>();
    private List<BeneficiaireOrdreVirement> listeBeneficiaireOrdreVirementSupprimes = new ArrayList<>();
    private List<BeneficiaireOrdreVirement> listeBeneficiaires = new ArrayList<>();
    private List<BeneficiaireOrdreVirement> listeBeneficiairesAjoutes = new ArrayList<>();
    
    // Bénéficiaire sélectionné et états
    private BeneficiaireOrdreVirement beneficiaireOrdreVirement = new BeneficiaireOrdreVirement();
    private BeneficiaireOrdreVirement selectedBeneficiaire;
    private boolean etatBenficiaire = false;
    private boolean etatBeneficiaire = false;
    private boolean modeModification = false;
    
    // ==================== GESTION DES MONTANTS ====================
    
    private String montantVirement;
    private String montantGlobal;
    private Long montantSaisie;
    private Double montantMinimal;
    private long montantTotalOrdres;
    private String strMontantVirement = "";
    private String messageMontant = "";
    
    // ==================== GESTION DES ORDRES DE VIREMENT ====================
    
    private List<OrdreVirement> listeOrdresVirements = new ArrayList<>();
    private OrdreVirement selectedOrdreVirement;
    private String numRemise;
    private String strNumSeqGvir = "";
    private String compteInterneMandat;
    private Date dateExecution;
    private String strDateExecution = "";
    private Date derniereMiseAJour;
    
    // ==================== COMPTEURS ET STATISTIQUES ====================
    
    private Long nombreSaisie;
    private Long nbreVirement;
    private long nombreTotalVirements;
    private String strNombreVirement = "";
    
    // ==================== ETATS ET VALIDATION ====================
    
    private boolean etatCmdValiderVirement = false;
    private boolean etatSaveBenefVir = false;
    private boolean etatCmdAjouterBeneficiaire = true;
    private boolean etatMotif = false;
    private boolean etatRIBCorrecte = true;
    private boolean etatmsgValidatorRib = true;
    private boolean etatMessageEnregistBeneficiaire = false;
    private String strEtatValidationTrt;
    
    // ==================== MESSAGES ====================
    
    private String messageEnregistBenefVirement = "";
    private String messageEnregistBeneficiaire = "";
    private String messageRIBexistant = "";
    private String messageValidationVirementBct = "";
    private String messageInformation = "";
    private String messageErreur = "";
    private String msgValidatorCompte = "";
    private String msgValidatorRib = "";
    
    // ==================== DONNEES COMPLEMENTAIRES ====================
    
    private String strRib = "";
    private String strTypeVirement = "";
    private String strNomInitCpt = "";
    private List<String> listNumRemise = new ArrayList<>();
    private List<String> listNumCompteInterne = new ArrayList<>();
    
    // ==================== REPORTING ====================
    
    private String reportName;
    private String srcRepport;
    private HashMap<String, Object> parametersJasper;
    
    // ==================== CONSTRUCTEUR ====================
    
    public PecVirementCentralCtr() {
        // Constructeur par défaut
    }
    
    // ==================== INITIALISATION ====================
    
    @PostConstruct
    public void init() {
        logger.info("Initialisation du contrôleur PecVirementCentralCtr");
        montantVirement = null;
    }
    
    // ==================== METHODES DE VALIDATION ====================
    
    /**
     * Valide le format et la valeur d'un montant
     * 
     * @param context Le contexte JSF
     * @param component Le composant UI
     * @param value La valeur à valider
     * @throws ValidatorException Si la validation échoue
     */
    public void validateMontant(FacesContext context, UIComponent component, Object value) 
            throws ValidatorException {
        String montant = value != null ? value.toString() : "";
        
        messageMontant = "";
        
        if (montant.trim().isEmpty()) {
            return;
        }
        
        String montantNettoye = montant.replace(".", "").replace(" ", "");
        Pattern pattern = Pattern.compile("[0-9]{1,15}");
        Matcher matcher = pattern.matcher(montantNettoye);
        
        if (montantNettoye.length() > 15) {
            throw new ValidatorException(
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Montant incorrect", 
                    "Le montant doit être sur 15 chiffres maximum")
            );
        }
        
        if (!matcher.matches()) {
            throw new ValidatorException(
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Format incorrect", 
                    "Le format du montant est incorrect")
            );
        }
        
        if (Long.parseLong(montantNettoye) == 0) {
            throw new ValidatorException(
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Montant invalide", 
                    "Le montant doit être supérieur à 0")
            );
        }
    }
    
    // ==================== GESTION DES BENEFICIAIRES ====================
    
    /**
     * Initialise un nouveau bénéficiaire
     */
    public void creerNouveauBenefVirement() {
        logger.debug("Création d'un nouveau bénéficiaire de virement");
        
        setBenificiaire("");
        msgValidatorCompte = "";
        setMontantVirement(null);
        setCompteBenificiaire(null);
        setRibBenificiaire("");
        setMessageEnregistBenefVirement("");
        setMessageRIBexistant("");
    }
    
    /**
     * Ajoute un bénéficiaire à l'ordre de virement
     */
    public void ajouterBeneficiaireVirement() {
        logger.info("Ajout d'un bénéficiaire de virement");
        
        try {
            etatSaveBenefVir = false;
            
            // Validation des données
            if (!validerDonneesBeneficiaire()) {
                return;
            }
            
            // Vérifier la limite de bénéficiaires
            if (isLimiteAtteinte()) {
                messageEnregistBenefVirement = "Limite de " + LIMITE_MAX_BENEFICIAIRES + " bénéficiaires atteinte";
                etatMessageEnregistBeneficiaire = true;
                return;
            }
            
            // Créer et ajouter le bénéficiaire
            BeneficiaireOrdreVirement nouveauBeneficiaire = creerBeneficiaire();
            listeBeneficiaireOrdreVirement.add(nouveauBeneficiaire);
            
            // Recalculer les totaux
            calculerTotauxGlobaux();
            
            // Réinitialiser le formulaire
            viderFormulaire();
            
            messageEnregistBenefVirement = "Bénéficiaire ajouté avec succès";
            etatMessageEnregistBeneficiaire = false;
            etatCmdValiderVirement = true;
            
            logger.info("Bénéficiaire ajouté avec succès. Total: " + listeBeneficiaireOrdreVirement.size());
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'ajout du bénéficiaire", e);
            messageEnregistBenefVirement = "Erreur lors de l'ajout: " + e.getMessage();
            etatMessageEnregistBeneficiaire = true;
        }
    }
    
    /**
     * Valide les données d'un bénéficiaire avant ajout/modification
     * 
     * @return true si les données sont valides, false sinon
     */
    private boolean validerDonneesBeneficiaire() {
        if (ribBeneficiaire == null || ribBeneficiaire.trim().isEmpty()) {
            messageEnregistBenefVirement = "Le RIB est obligatoire";
            etatMessageEnregistBeneficiaire = true;
            return false;
        }
        
        if (nomBeneficiaire == null || nomBeneficiaire.trim().isEmpty()) {
            messageEnregistBenefVirement = "Le nom du bénéficiaire est obligatoire";
            etatMessageEnregistBeneficiaire = true;
            return false;
        }
        
        if (montantBeneficiaire <= 0) {
            messageEnregistBenefVirement = "Le montant doit être supérieur à 0";
            etatMessageEnregistBeneficiaire = true;
            return false;
        }
        
        return true;
    }
    
    /**
     * Crée un objet BeneficiaireOrdreVirement à partir des données saisies
     * 
     * @return Le bénéficiaire créé
     */
    private BeneficiaireOrdreVirement creerBeneficiaire() {
        BeneficiaireOrdreVirement beneficiaire = new BeneficiaireOrdreVirement();
        beneficiaire.setNumRibbBenf(ribBeneficiaire);
        beneficiaire.setNomPrnbBenf(nomBeneficiaire);
        beneficiaire.setMontOperBenf(montantBeneficiaire);
        // Ajouter autres propriétés si nécessaire
        return beneficiaire;
    }
    
    /**
     * Supprime un bénéficiaire de la liste
     * 
     * @param beneficiaire Le bénéficiaire à supprimer
     */
    public void supprimerBeneficiaire(BeneficiaireOrdreVirement beneficiaire) {
        logger.info("Suppression d'un bénéficiaire");
        
        try {
            if (beneficiaire != null && listeBeneficiaireOrdreVirement.contains(beneficiaire)) {
                listeBeneficiaireOrdreVirement.remove(beneficiaire);
                listeBeneficiaireOrdreVirementSupprimes.add(beneficiaire);
                
                calculerTotauxGlobaux();
                
                messageEnregistBenefVirement = "Bénéficiaire supprimé avec succès";
                etatMessageEnregistBeneficiaire = false;
                
                // Désactiver validation si plus de bénéficiaires
                if (listeBeneficiaireOrdreVirement.isEmpty()) {
                    etatCmdValiderVirement = false;
                }
                
                logger.info("Bénéficiaire supprimé. Reste: " + listeBeneficiaireOrdreVirement.size());
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du bénéficiaire", e);
            messageEnregistBenefVirement = "Erreur lors de la suppression";
            etatMessageEnregistBeneficiaire = true;
        }
    }
    
    /**
     * Prépare la modification d'un bénéficiaire
     * 
     * @param beneficiaire Le bénéficiaire à modifier
     */
    public void modifierBeneficiaire(BeneficiaireOrdreVirement beneficiaire) {
        logger.info("Préparation de la modification d'un bénéficiaire");
        
        try {
            if (beneficiaire != null) {
                selectedBeneficiaire = beneficiaire;
                modeModification = true;
                
                // Remplir le formulaire avec les données du bénéficiaire
                setRibBeneficiaire(beneficiaire.getNumRibbBenf());
                setNomBeneficiaire(beneficiaire.getNomPrnbBenf());
                setMontantBeneficiaire(beneficiaire.getMontOperBenf());
                
                messageEnregistBenefVirement = "";
                etatMessageEnregistBeneficiaire = false;
                
                logger.debug("Bénéficiaire chargé pour modification: " + beneficiaire.getNumRibbBenf());
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la préparation de la modification", e);
            messageEnregistBenefVirement = "Erreur lors du chargement des données";
            etatMessageEnregistBeneficiaire = true;
        }
    }
    
    /**
     * Enregistre les modifications d'un bénéficiaire
     */
    public void enregistrerModificationBeneficiaire() {
        logger.info("Enregistrement des modifications du bénéficiaire");
        
        try {
            if (!modeModification || selectedBeneficiaire == null) {
                messageEnregistBeneficiaire = "Aucun bénéficiaire sélectionné pour modification";
                etatMessageEnregistBeneficiaire = true;
                return;
            }
            
            // Validation des données
            if (!validerDonneesBeneficiaire()) {
                return;
            }
            
            // Trouver l'index du bénéficiaire dans la liste
            int index = listeBeneficiaireOrdreVirement.indexOf(selectedBeneficiaire);
            
            if (index == -1) {
                messageEnregistBeneficiaire = "Bénéficiaire introuvable dans la liste";
                etatMessageEnregistBeneficiaire = true;
                return;
            }
            
            // Modifier directement les propriétés du bénéficiaire
            BeneficiaireOrdreVirement benefAModifier = listeBeneficiaireOrdreVirement.get(index);
            benefAModifier.setNumRibbBenf(ribBeneficiaire);
            benefAModifier.setNomPrnbBenf(nomBeneficiaire);
            benefAModifier.setMontOperBenf(montantBeneficiaire);
            
            // Recalculer les totaux
            calculerTotauxGlobaux();
            
            // Message de succès
            messageEnregistBeneficiaire = "Modification effectuée avec succès";
            etatMessageEnregistBeneficiaire = false;
            
            // Sortir du mode modification
            modeModification = false;
            selectedBeneficiaire = null;
            
            // Vider le formulaire
            viderFormulaire();
            
            logger.info("Modification enregistrée avec succès");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la modification du bénéficiaire", e);
            messageEnregistBeneficiaire = "Erreur lors de la modification: " + e.getMessage();
            etatMessageEnregistBeneficiaire = true;
        }
    }
    
    // ==================== CALCULS ET TOTAUX ====================
    
    /**
     * Calcule les totaux globaux (nombre et montant des virements)
     */
    private void calculerTotauxGlobaux() {
        logger.debug("Calcul des totaux globaux");
        
        try {
            nombreSaisie = (long) listeBeneficiaireOrdreVirement.size();
            
            long montantTotal = 0;
            for (BeneficiaireOrdreVirement benef : listeBeneficiaireOrdreVirement) {
                montantTotal += benef.getMontOperBenf();
            }
            montantSaisie = montantTotal;
            
            logger.debug("Nombre de virements: " + nombreSaisie + ", Montant total: " + montantSaisie);
            
        } catch (Exception e) {
            logger.error("Erreur lors du calcul des totaux", e);
        }
    }
    
    // ==================== GESTION DE L'INTERFACE ====================
    
    /**
     * Vide le formulaire de saisie des bénéficiaires
     */
    public void viderFormulaire() {
        logger.debug("Vidage du formulaire");
        
        try {
            // Vider les champs du formulaire
            setRibBeneficiaire("");
            setNomBeneficiaire("");
            setMontantBeneficiaire(0);
            
            // Vider les variables d'affichage
            compteBenificiaire = "";
            benificiaire = "";
            montantVirement = "";
            
            // Réinitialiser les états
            etatRIBCorrecte = false;
            etatBenficiaire = false;
            
            // Vider les messages
            msgValidatorCompte = "";
            messageMontant = "";
            messageRIBexistant = "";
            
            logger.debug("Formulaire vidé avec succès");
            
        } catch (Exception e) {
            logger.error("Erreur lors du vidage du formulaire", e);
        }
    }
    
    /**
     * Annule la saisie en cours et réinitialise le formulaire
     * 
     * @return null pour rester sur la même page
     */
    public String annulerSaisieBeneficiaire() {
        logger.info("Annulation de la saisie du bénéficiaire");
        
        try {
            // Réinitialiser les champs du formulaire
            compteBenificiaire = "";
            benificiaire = "";
            montantVirement = null;
            motif = "";
            
            // Réinitialiser les états et messages
            etatRIBCorrecte = false;
            etatBenficiaire = false;
            etatMotif = false;
            messageEnregistBenefVirement = "";
            messageRIBexistant = "";
            messageMontant = "";
            msgValidatorCompte = "";
            
            // Réinitialiser la liste des bénéficiaires
            if (listeBeneficiaireOrdreVirement != null) {
                listeBeneficiaireOrdreVirement.clear();
            }
            
            // Réinitialiser la sélection et le mode modification
            selectedBeneficiaire = null;
            modeModification = false;
            
            // Réinitialiser les totaux
            nombreSaisie = 0L;
            
            // Désactiver le bouton de validation
            etatCmdValiderVirement = false;
            
            logger.info("Saisie annulée avec succès");
            viderFormulaire();
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'annulation de la saisie", e);
        }
        
        return null;
    }
    
    /**
     * Réinitialise complètement le contrôleur
     * 
     * @return null pour rester sur la même page
     */
    public String reinitialiserTout() {
        logger.info("Réinitialisation complète du contrôleur");
        
        try {
            // Réinitialiser le formulaire
            viderFormulaire();
            
            // Réinitialiser la liste des bénéficiaires
            if (listeBeneficiaireOrdreVirement != null) {
                listeBeneficiaireOrdreVirement.clear();
            }
            
            // Réinitialiser les totaux
            nombreSaisie = 0L;
            montantSaisie = 0L;
            
            // Réinitialiser les données générales
            if (listeOrdresVirements != null) {
                listeOrdresVirements.clear();
            }
            
            numRemise = "";
            montantMinimal = null;
            selectedOrdreVirement = null;
            messageInformation = "";
            
            // Désactiver le bouton de validation
            etatCmdValiderVirement = false;
            
            // Réinitialiser l'état de validation
            strEtatValidationTrt = "0";
            messageValidationVirementBct = "";
            
            logger.info("Réinitialisation complète effectuée avec succès");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la réinitialisation", e);
        }
        
        return null;
    }
    
    // ==================== UTILITAIRES ====================
    
    /**
     * Vérifie si la limite de bénéficiaires est atteinte
     * 
     * @return true si la limite est atteinte, false sinon
     */
    public boolean isLimiteAtteinte() {
        return listeBeneficiaireOrdreVirement != null 
            && listeBeneficiaireOrdreVirement.size() >= LIMITE_MAX_BENEFICIAIRES;
    }
    
    /**
     * Retourne le nombre de places restantes pour les bénéficiaires
     * 
     * @return Le nombre de places restantes
     */
    public int getNombrePlacesRestantes() {
        if (listeBeneficiaireOrdreVirement == null) {
            return LIMITE_MAX_BENEFICIAIRES;
        }
        return LIMITE_MAX_BENEFICIAIRES - listeBeneficiaireOrdreVirement.size();
    }
    
    /**
     * Ajoute un message dans le contexte JSF
     * 
     * @param id L'identifiant du composant
     * @param summary Le résumé du message
     * @param detail Le détail du message
     * @param severity La sévérité du message
     */
    public void addMessage(String id, String summary, String detail, FacesMessage.Severity severity) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage();
        facesMessage.setSeverity(severity);
        facesMessage.setSummary(summary);
        facesMessage.setDetail(detail);
        facesContext.addMessage(id, facesMessage);
    }
    
    /**
     * Récupère la liste des numéros de comptes internes
     * 
     * @return La liste des numéros de comptes internes
     */
    public List<String> getListNumCompteInterne() {
        logger.debug("Récupération des numéros de comptes internes");
        
        try {
            VirementVo virementVo = new VirementVo();
            virementVo.setParamAgence(getParamAgence());
            VirementCmd virementCmd = new VirementCmd();
            
            if (getParamAgence() != null) {
                virementVo.setCodStrcStrc(getParamAgence().getCodStrcStrc());
            }
            
            virementVo = (VirementVo) virementCmd.GetListNumCompteInterneCentraleTrt(virementVo);
            
            if (virementVo.getListNumComptes() != null && !virementVo.getListNumComptes().isEmpty()) {
                for (String compteComplet : virementVo.getListNumComptes()) {
                    if (compteComplet != null && !compteComplet.trim().isEmpty()) {
                        listNumCompteInterne.add(compteComplet);
                    }
                }
            }
            
            logger.debug("Nombre de comptes internes récupérés: " + listNumCompteInterne.size());
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des numéros de comptes internes", e);
        }
        
        return listNumCompteInterne;
    }
    
    // ==================== GETTERS ET SETTERS ====================
    
    public String getMontantVirement() {
        return montantVirement;
    }
    
    public void setMontantVirement(String montantVirement) {
        this.montantVirement = montantVirement;
    }
    
    public String getCompteInterneMandat() {
        return compteInterneMandat;
    }
    
    public void setCompteInterneMandat(String compteInterneMandat) {
        this.compteInterneMandat = compteInterneMandat;
    }
    
    public String getMessageMontant() {
        return messageMontant;
    }
    
    public void setMessageMontant(String messageMontant) {
        this.messageMontant = messageMontant;
    }
    
    public String getMessageEnregistBenefVirement() {
        return messageEnregistBenefVirement;
    }
    
    public void setMessageEnregistBenefVirement(String messageEnregistBenefVirement) {
        this.messageEnregistBenefVirement = messageEnregistBenefVirement;
    }
    
    public String getMessageRIBexistant() {
        return messageRIBexistant;
    }
    
    public void setMessageRIBexistant(String messageRIBexistant) {
        this.messageRIBexistant = messageRIBexistant;
    }
    
    public String getMessageValidationVirementBct() {
        return messageValidationVirementBct;
    }
    
    public void setMessageValidationVirementBct(String messageValidationVirementBct) {
        this.messageValidationVirementBct = messageValidationVirementBct;
    }
    
    public boolean isEtatCmdValiderVirement() {
        return etatCmdValiderVirement;
    }
    
    public void setEtatCmdValiderVirement(boolean etatCmdValiderVirement) {
        this.etatCmdValiderVirement = etatCmdValiderVirement;
    }
    
    public boolean isEtatSaveBenefVir() {
        return etatSaveBenefVir;
    }
    
    public void setEtatSaveBenefVir(boolean etatSaveBenefVir) {
        this.etatSaveBenefVir = etatSaveBenefVir;
    }
    
    public ParamAgence getParamAgence() {
        return paramAgence;
    }
    
    public void setParamAgence(ParamAgence paramAgence) {
        this.paramAgence = paramAgence;
    }
    
    public String getCompteBenificiaire() {
        return compteBenificiaire;
    }
    
    public void setCompteBenificiaire(String compteBenificiaire) {
        this.compteBenificiaire = compteBenificiaire;
    }
    
    public String getBenificiaire() {
        return benificiaire;
    }
    
    public void setBenificiaire(String benificiaire) {
        this.benificiaire = benificiaire;
    }
    
    public String getMotif() {
        return motif;
    }
    
    public void setMotif(String motif) {
        this.motif = motif;
    }
    
    public String getMsgValidatorCompte() {
        return msgValidatorCompte;
    }
    
    public void setMsgValidatorCompte(String msgValidatorCompte) {
        this.msgValidatorCompte = msgValidatorCompte;
    }
    
    public String getRibBenificiaire() {
        return ribBenificiaire;
    }
    
    public void setRibBenificiaire(String ribBenificiaire) {
        this.ribBenificiaire = ribBenificiaire;
    }
    
    public boolean isEtatBenficiaire() {
        return etatBenficiaire;
    }
    
    public void setEtatBenficiaire(boolean etatBenficiaire) {
        this.etatBenficiaire = etatBenficiaire;
    }
    
    public BeneficiaireOrdreVirement getBeneficiaireOrdreVirement() {
        return beneficiaireOrdreVirement;
    }
    
    public void setBeneficiaireOrdreVirement(BeneficiaireOrdreVirement beneficiaireOrdreVirement) {
        this.beneficiaireOrdreVirement = beneficiaireOrdreVirement;
    }
    
    public List<BeneficiaireOrdreVirement> getListeBeneficiaireOrdreVirement() {
        return listeBeneficiaireOrdreVirement;
    }
    
    public void setListeBeneficiaireOrdreVirement(List<BeneficiaireOrdreVirement> listeBeneficiaireOrdreVirement) {
        this.listeBeneficiaireOrdreVirement = listeBeneficiaireOrdreVirement;
    }
    
    public BeneficiaireOrdreVirement getSelectedBeneficiaire() {
        return selectedBeneficiaire;
    }
    
    public void setSelectedBeneficiaire(BeneficiaireOrdreVirement selectedBeneficiaire) {
        this.selectedBeneficiaire = selectedBeneficiaire;
    }
    
    public Long getNombreSaisie() {
        return nombreSaisie;
    }
    
    public void setNombreSaisie(Long nombreSaisie) {
        this.nombreSaisie = nombreSaisie;
    }
    
    public Long getMontantSaisie() {
        return montantSaisie;
    }
    
    public void setMontantSaisie(Long montantSaisie) {
        this.montantSaisie = montantSaisie;
    }
    
    public String getNumRemise() {
        return numRemise;
    }
    
    public void setNumRemise(String numRemise) {
        this.numRemise = numRemise;
    }
    
    public RechercheClientByStructureCtr getRechercheClientCtr() {
        return rechercheClientCtr;
    }
    
    public void setRechercheClientCtr(RechercheClientByStructureCtr rechercheClientCtr) {
        this.rechercheClientCtr = rechercheClientCtr;
    }
    
    public String getMontantGlobal() {
        return montantGlobal;
    }
    
    public void setMontantGlobal(String montantGlobal) {
        this.montantGlobal = montantGlobal;
    }
    
    public Long getNbreVirement() {
        return nbreVirement;
    }
    
    public void setNbreVirement(Long nbreVirement) {
        this.nbreVirement = nbreVirement;
    }
    
    public String getCritereRecherche() {
        return critereRecherche;
    }
    
    public void setCritereRecherche(String critereRecherche) {
        this.critereRecherche = critereRecherche;
    }
    
    public Double getMontantMinimal() {
        return montantMinimal;
    }
    
    public void setMontantMinimal(Double montantMinimal) {
        this.montantMinimal = montantMinimal;
    }
    
    public String getMessageInformation() {
        return messageInformation;
    }
    
    public void setMessageInformation(String messageInformation) {
        this.messageInformation = messageInformation;
    }
    
    public Date getDerniereMiseAJour() {
        return derniereMiseAJour;
    }
    
    public void setDerniereMiseAJour(Date derniereMiseAJour) {
        this.derniereMiseAJour = derniereMiseAJour;
    }
    
    public boolean isEtatCmdAjouterBeneficiaire() {
        return etatCmdAjouterBeneficiaire;
    }
    
    public void setEtatCmdAjouterBeneficiaire(boolean etatCmdAjouterBeneficiaire) {
        this.etatCmdAjouterBeneficiaire = etatCmdAjouterBeneficiaire;
    }
    
    public boolean isEtatBeneficiaire() {
        return etatBeneficiaire;
    }
    
    public void setEtatBeneficiaire(boolean etatBeneficiaire) {
        this.etatBeneficiaire = etatBeneficiaire;
    }
    
    public String getRibBeneficiaire() {
        return ribBeneficiaire;
    }
    
    public void setRibBeneficiaire(String ribBeneficiaire) {
        this.ribBeneficiaire = ribBeneficiaire;
    }
    
    public String getNomBeneficiaire() {
        return nomBeneficiaire;
    }
    
    public void setNomBeneficiaire(String nomBeneficiaire) {
        this.nomBeneficiaire = nomBeneficiaire;
    }
    
    public long getMontantBeneficiaire() {
        return montantBeneficiaire;
    }
    
    public void setMontantBeneficiaire(long montantBeneficiaire) {
        this.montantBeneficiaire = montantBeneficiaire;
    }
    
    public String getMotifBeneficiaire() {
        return motifBeneficiaire;
    }
    
    public void setMotifBeneficiaire(String motifBeneficiaire) {
        this.motifBeneficiaire = motifBeneficiaire;
    }
    
    public String getMessageErreur() {
        return messageErreur;
    }
    
    public void setMessageErreur(String messageErreur) {
        this.messageErreur = messageErreur;
    }
    
    public Date getDateExecution() {
        return dateExecution;
    }
    
    public void setDateExecution(Date dateExecution) {
        this.dateExecution = dateExecution;
    }
    
    public long getNombreTotalVirements() {
        return nombreTotalVirements;
    }
    
    public void setNombreTotalVirements(long nombreTotalVirements) {
        this.nombreTotalVirements = nombreTotalVirements;
    }
    
    public double getMontantTotalOrdres() {
        return montantTotalOrdres;
    }
    
    public void setMontantTotalOrdres(double montantTotalOrdres) {
        this.montantTotalOrdres = montantTotalOrdres;
    }
    
    public boolean isEtatmsgValidatorRib() {
        return etatmsgValidatorRib;
    }
    
    public void setEtatmsgValidatorRib(boolean etatmsgValidatorRib) {
        this.etatmsgValidatorRib = etatmsgValidatorRib;
    }
    
    public boolean isEtatMessageEnregistBeneficiaire() {
        return etatMessageEnregistBeneficiaire;
    }
    
    public void setEtatMessageEnregistBeneficiaire(boolean etatMessageEnregistBeneficiaire) {
        this.etatMessageEnregistBeneficiaire = etatMessageEnregistBeneficiaire;
    }
    
    public String getMsgValidatorRib() {
        return msgValidatorRib;
    }
    
    public void setMsgValidatorRib(String msgValidatorRib) {
        this.msgValidatorRib = msgValidatorRib;
    }
    
    public String getReportName() {
        return reportName;
    }
    
    public void setReportName(String reportName) {
        this.reportName = reportName;
    }
    
    public String getSrcRepport() {
        return srcRepport;
    }
    
    public void setSrcRepport(String srcRepport) {
        this.srcRepport = srcRepport;
    }
    
    public boolean isEtatRIBCorrecte() {
        return etatRIBCorrecte;
    }
    
    public void setEtatRIBCorrecte(boolean etatRIBCorrecte) {
        this.etatRIBCorrecte = etatRIBCorrecte;
    }
    
    public HashMap<String, Object> getParametersJasper() {
        return parametersJasper;
    }
    
    public void setParametersJasper(HashMap<String, Object> parametersJasper) {
        this.parametersJasper = parametersJasper;
    }
    
    public List<OrdreVirement> getListeOrdresVirements() {
        return listeOrdresVirements;
    }
    
    public void setListeOrdresVirements(List<OrdreVirement> listeOrdresVirements) {
        this.listeOrdresVirements = listeOrdresVirements;
    }
    
    public List<BeneficiaireOrdreVirement> getListeBeneficiaires() {
        return listeBeneficiaires;
    }
    
    public void setListeBeneficiaires(List<BeneficiaireOrdreVirement> listeBeneficiaires) {
        this.listeBeneficiaires = listeBeneficiaires;
    }
    
    public OrdreVirement getSelectedOrdreVirement() {
        return selectedOrdreVirement;
    }
    
    public void setSelectedOrdreVirement(OrdreVirement selectedOrdreVirement) {
        this.selectedOrdreVirement = selectedOrdreVirement;
    }
    
    public String getMessageEnregistBeneficiaire() {
        return messageEnregistBeneficiaire;
    }
    
    public void setMessageEnregistBeneficiaire(String messageEnregistBeneficiaire) {
        this.messageEnregistBeneficiaire = messageEnregistBeneficiaire;
    }
    
    public List<String> getListNumRemise() {
        return listNumRemise;
    }
    
    public void setListNumRemise(List<String> listNumRemise) {
        this.listNumRemise = listNumRemise;
    }
    
    public void setListNumCompteInterne(List<String> listNumCompteInterne) {
        this.listNumCompteInterne = listNumCompteInterne;
    }
    
    public String getStrLibStructure() {
        return strLibStructure;
    }
    
    public void setStrLibStructure(String strLibStructure) {
        this.strLibStructure = strLibStructure;
    }
    
    public String getStrCodeStructure() {
        return strCodeStructure;
    }
    
    public void setStrCodeStructure(String strCodeStructure) {
        this.strCodeStructure = strCodeStructure;
    }
    
    public boolean isModeModification() {
        return modeModification;
    }
    
    public void setModeModification(boolean modeModification) {
        this.modeModification = modeModification;
    }
    
    public boolean isEtatMotif() {
        return etatMotif;
    }
    
    public void setEtatMotif(boolean etatMotif) {
        this.etatMotif = etatMotif;
    }
    
    public String getStrEtatValidationTrt() {
        return strEtatValidationTrt;
    }
    
    public void setStrEtatValidationTrt(String strEtatValidationTrt) {
        this.strEtatValidationTrt = strEtatValidationTrt;
    }
}