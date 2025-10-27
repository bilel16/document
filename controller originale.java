package com.bna.smile.web.virement.controller;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Connection;
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
import javax.faces.validator.ValidatorException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.ajax4jsf.model.KeepAlive;
import org.apache.log4j.Logger;

import com.bna.commun.model.BeneficiaireOrdreVirement;
import com.bna.commun.model.ContratCpt;
import com.bna.commun.model.ContratCptId;
import com.bna.commun.model.DetailVirement;
import com.bna.commun.model.Devise;
import com.bna.commun.model.GlobalVirement;
import com.bna.commun.model.NatureVirement;
import com.bna.commun.model.OrdreVirement;
import com.bna.commun.model.OrdreVirementId;
import com.bna.commun.model.Personnel;
import com.bna.commun.model.SeqAgence;
import com.bna.commun.model.Structure;
import com.bna.commun.model.Tache;
import com.bna.commun.model.TacheId;
import com.bna.commun.model.TypeVirement;
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

import java.util.Date;
import javax.faces.event.ActionEvent;
import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;

@KeepAlive
public class PecVirementCentralCtr implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Logger logger = Logger.getLogger(PecVirementCentralCtr.class);
	private String montantVirement;
	private String compteInterneMandat;
	private static final int LIMITE_MAX_BENEFICIAIRES = 10;
	private String messageMontant = "";
	private String messageEnregistBenefVirement;
	private String messageRIBexistant;
	private String messageValidationVirementBct = "";
	private boolean etatCmdValiderVirement = false;
	private boolean etatSaveBenefVir = false;
	private ParamAgence paramAgence = new ParamAgence();
	private String compteBenificiaire;
	private String benificiaire = "";
	private String motif;
	private String msgValidatorCompte;
	private String strEtatValidationTrt;
	private String strNumSeqGvir = "";
	private String strRib = "";
	private String strPath = "";
	private String strNumMatUser = "";
	private String strLibStructure = "";
	private String strCodeStructure = "";
	private String strTypeVirement = "";
	private String strNomInitCpt = "";
	private String strNombreVirement = "";
	private String strMontantVirement = "";
	private String strDateExecution = "";
	private boolean etatMotif = false;
	private String ribBenificiaire;
	private boolean etatBenficiaire = false;
	private BeneficiaireOrdreVirement beneficiaireOrdreVirement = new BeneficiaireOrdreVirement();
	private List<BeneficiaireOrdreVirement> listeBeneficiaireOrdreVirement = new ArrayList<BeneficiaireOrdreVirement>();
	private List<BeneficiaireOrdreVirement> listeBeneficiaireOrdreVirementSupprimes = new ArrayList<BeneficiaireOrdreVirement>();
	private BeneficiaireOrdreVirement selectedBeneficiaire;
	private Long nombreSaisie;
	private Long montantSaisie;
	private String numRemise;
	private RechercheClientByStructureCtr rechercheClientCtr;
	private String montantGlobal;
	private Long nbreVirement;
	private String critereRecherche = "R";
	private Double montantMinimal;
	private String messageInformation = "";
	private Date derniereMiseAJour;
	// private Date dateExecution;
	private boolean etatCmdAjouterBeneficiaire = true;
	private boolean etatBeneficiaire = false;
	// private boolean etatRIBCorrecte = false;
	// private boolean etatMotif = true;
	// private String msgValidatorRib;
	// Propriétés pour la gestion des bénéficiaires
	private String ribBeneficiaire;
	private String nomBeneficiaire;
	private long montantBeneficiaire;
	private String motifBeneficiaire;
	// private BeneficiaireOrdreVirement selectedBeneficiaire;
	private String messageErreur = "";
	private Date dateExecution;
	// private String messageEnregistBeneficiaire = "";
	private long nombreTotalVirements;
	private double montantTotalOrdres;

	// États pour l'interface
	// private boolean etatCmdAjouterBeneficiaire = true;
	// private boolean etatBeneficiaire = false;
	// private boolean etatRIBCorrecte = false;
	// private boolean etatMotif = true;
	private boolean etatmsgValidatorRib = true;
	private boolean etatMessageEnregistBeneficiaire = false;
	private String msgValidatorRib = "";

	/** reporting **/
	private String reportName;
	private String srcRepport;
	private boolean etatRIBCorrecte = true;
	// private Map<String, String> parametersJasper = new HashMap<String, String>();
	private HashMap<String, Object> parametersJasper;
	private List<OrdreVirement> listeOrdresVirements = new ArrayList<OrdreVirement>();
	private List<BeneficiaireOrdreVirement> listeBeneficiaires = new ArrayList<BeneficiaireOrdreVirement>();
	private List<BeneficiaireOrdreVirement> listeBeneficiairesAjoutes = new ArrayList<BeneficiaireOrdreVirement>();
	private OrdreVirement selectedOrdreVirement;

	private String messageEnregistBeneficiaire = "";

	private List<String> listNumRemise = new ArrayList<String>();
	private List<String> listNumCompteInterne = new ArrayList<String>();
	public static Long codPrdVirementPonctuel = new Long(1063);
	public static Long etatVirementPonctuelEncour = new Long(0);

	/********************************/
	public PecVirementCentralCtr() {

	}

	@PostConstruct
	public void init() {

		montantVirement = null;
		// listeTousLesOrdresVirement();
	}

	public void addMessage(String id, String summary, String detail, FacesMessage.Severity severity) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		FacesMessage facesMessage = new FacesMessage();
		facesMessage.setSeverity(severity);
		facesMessage.setSummary(summary);
		facesMessage.setDetail(detail);
		facesContext.addMessage(id, facesMessage);
	}

	public void validateMontant(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		String montant = value + "";

		messageMontant = "";
		if (!montant.trim().equals("") && montant.trim() != null) {

			String newmont = montant.replace(".", "");
			newmont = newmont.replace(" ", "");
			Pattern p = Pattern.compile("[0-9]{1,15}");
			Matcher m = p.matcher(newmont);

			if (newmont.length() <= 15) {

				if (!m.matches()) {

					throw new ValidatorException(
							new FacesMessage(FacesMessage.SEVERITY_ERROR, "Format Incorrect ", "Format Incorrect "));
				} else {
					if (Long.valueOf(newmont).longValue() == Long.valueOf(0)) {

						throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
								"Montant doit être  sup à 0", "Montant doit être sup à 0"));
					}
				}
			} else {

				throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Montant doit être  sur 15 chiffres ", "Montant doit être sur 15 chiffres "));
			}
		}

	}

	public void creerNouveauBenefVirement() {

		setBenificiaire("");
		msgValidatorCompte = "";
		setMontantVirement(null);
		setCompteBenificiaire(null);
		setRibBenificiaire("");
		setMessageEnregistBenefVirement("");
		setMessageRIBexistant("");

	}

	public void ajouterBeneficiaireVirement() {

		try {

			etatSaveBenefVir = false;
			boolean etatbenef = true;
			boolean etatBanqueBenef = true;
			messageEnregistBenefVirement = "";
			// Vérifier la limite avec la constante
			if (listeBeneficiaireOrdreVirement != null
					&& listeBeneficiaireOrdreVirement.size() >= LIMITE_MAX_BENEFICIAIRES) {
				messageEnregistBenefVirement = " Limite atteinte : Vous ne pouvez pas ajouter plus de "
						+ LIMITE_MAX_BENEFICIAIRES + " bénéficiaires !";
				// return null;
			}
			if (getRibBenificiaire().length() == 20 && montantVirement.length() > 0
					&& Long.valueOf(montantVirement.replace(".", "").replace(" ", "").trim()).longValue() > 0
					&& getRibBenificiaire().length() > 0 && msgValidatorCompte.length() < 1) {

				beneficiaireOrdreVirement = new BeneficiaireOrdreVirement();

				beneficiaireOrdreVirement.setNumRibbBenf(ribBenificiaire);
				Structure strcBenef = new Structure();
				strcBenef.setCodStrcStrc(Long.valueOf(getRibBenificiaire().substring(5, 8)));
				beneficiaireOrdreVirement.setStructureBenef(strcBenef);
				if (benificiaire.length() > 60) {

					benificiaire = benificiaire.substring(0, 60);
				}

				beneficiaireOrdreVirement.setNomPrnbBenf(benificiaire);
				if (motif.length() > 200) {

					motif = motif.substring(0, 200);
				}
				beneficiaireOrdreVirement.setLibMotifBenf(motif);
				beneficiaireOrdreVirement
						.setMontOperBenf(Long.valueOf(montantVirement.replace(".", "").replace(" ", "").trim()));
				beneficiaireOrdreVirement.setCodEtatDov("A");

				listeBeneficiaireOrdreVirement.add(beneficiaireOrdreVirement);
				creerNouveauBenefVirement();

				etatSaveBenefVir = true;

			} else {
				etatSaveBenefVir = false;
				messageEnregistBenefVirement = getMsgValidatorCompte();
			}
		} catch (Exception e) {
			etatSaveBenefVir = false;
			e.printStackTrace();
		}

	}

	public void editerBeneficiaireVirement1() {

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

				System.out.println(
						"id de beneficiare:" + selectedBeneficiaire.getBeneficiaireOrdreVirementId().getNumDetBenf());

			}

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public void supprimerBeneficiaireVirement() {

		try {
			if (selectedBeneficiaire != null) {
				listeBeneficiaireOrdreVirement.remove(selectedBeneficiaire);
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public Long getSeqAgence(VirementVo virementVo) {

		ISearchEngine searchEngine = (ISearchEngine) Context.getInstance().getSpringContext().getBean("searchEngine");
		ICriteria criteria = searchEngine.createCriteria();
		IExpression expression = searchEngine.createExpression();

		Context context = ContextHandler.getContext();
		CRUDservice crudService = (CRUDservice) context.getBean("crudservice");

		Long numValSeq = null;
		SeqAgence seqAgence = new SeqAgence();

		criteria.add(expression.eq("seqAgenceId.codStrcStrc", new Long(virementVo.getParamAgence().getCodStrcStrc())));
		criteria.add(expression.eq("seqAgenceId.libSeqSeqa", "SEQ_GLOB_VIR"));

		List l = searchEngine.find(SeqAgence.class, criteria);

		if (l != null && l.size() > 0) {
			seqAgence = (SeqAgence) l.get(0);

			numValSeq = seqAgence.getNumValSeqa();

			seqAgence.setNumValSeqa(numValSeq.longValue() + 1);
			crudService.update(seqAgence);

		}

		return numValSeq;

	}

	public void validerVirementMandatBct() {

		try {
			VirementVo virementVo = new VirementVo();
			VirementCmd virementCentraleCmd = new VirementCmd();
			OrdreVirement ordreVirement = new OrdreVirement();
			OrdreVirementId ordreVirementId = new OrdreVirementId();
			setStrEtatValidationTrt("0");

			parametersJasper = new HashMap<String, Object>();
			SimpleDateFormat dateFormatANNEE = new SimpleDateFormat("yyyy");
			virementVo.setParamAgence(getParamAgence());
			Long seqGlobVir = getSeqAgence(virementVo);
			String numeroRemise = StrHandler.lpad(getParamAgence().getCodStrcStrc() + "", '0', 3)
					+ dateFormatANNEE.format(new Date()) + StrHandler.lpad(seqGlobVir + "", '0', 5);

			if (listeBeneficiaireOrdreVirement != null && listeBeneficiaireOrdreVirement.size() != 0) {

				Structure structureInitiatrice = new Structure();
				structureInitiatrice.setCodStrcStrc(getParamAgence().getCodStrcStrc());

				ordreVirementId.setDatOvirOvir(DateHandler.strToDate(getParamAgence().getDateComptable()));
				ordreVirementId.setNumOvirOvir(numeroRemise);
				ordreVirement.setOrdreVirementId(ordreVirementId);
				ordreVirement.setStructure(structureInitiatrice);
				Personnel personnelInit = new Personnel();
				personnelInit.setNumMatrUser(getParamAgence().getNumMatrUser());
				ordreVirement.setPersonnel(personnelInit);
				ordreVirement.setPersonnelValidateur(personnelInit);
				ordreVirement.setDatExecOvir(DateHandler.strToDate(getParamAgence().getDateComptable()));
				ordreVirement.setCodEtatOvir("A");
				ordreVirement.setMontGlobOvir(montantSaisie);
				ordreVirement.setNbrVirOvir(nombreSaisie);
				ordreVirement.setCompteDebitOvir(compteInterneMandat);

				if (motif.length() > 200) {
					motif = motif.substring(0, 200);
				}
				ordreVirement.setLibMotfOvir(motif);
				ordreVirement.setListeBeneficiaireOrdreVirements(listeBeneficiaireOrdreVirement);
				virementVo.setOrdreVirement(ordreVirement);
				virementVo.setParamAgence(getParamAgence());

				virementVo.setStrEtatValidationTrt(getStrEtatValidationTrt());
				virementVo = (VirementVo) virementCentraleCmd.validerVirementCentralVirement(virementVo);

				messageValidationVirementBct = virementVo.getMessageValidation();
				System.out.println("num ordre virement imprint" + virementVo.getOrdreVirement().getCompteDebitOvir());

				if (virementVo.isStadeEnregistrement() == true) {
					setStrEtatValidationTrt("1");

					parametersJasper.put("P_NUM_VIREMENT",
							virementVo.getOrdreVirement().getOrdreVirementId().getNumOvirOvir() + "");
					parametersJasper.put("COMPTE", virementVo.getOrdreVirement().getCompteDebitOvir());
					parametersJasper.put("NOM_INTI_CCPT", "Compte Interne");
					parametersJasper.put("RIB_BEN_DETV", beneficiaireOrdreVirement.getNumRibbBenf());
					parametersJasper.put("NOM_BEN_DETV", beneficiaireOrdreVirement.getNumRibbBenf());
					parametersJasper.put("MOTIF_BENF", beneficiaireOrdreVirement.getLibMotifBenf());
					parametersJasper.put("MNT_DETV_DETV", montantVirement);

					setStrPath("");
					FacesContext context = FacesContext.getCurrentInstance();
					ServletContext servletContext = (ServletContext) context.getExternalContext().getContext();
					String webRoot = servletContext.getRealPath("/");
					setStrPath(webRoot + "reporting/Virement/");

					// Imprimer le PDF
					this.imprimerVirementMandatBct();

					// DÉSACTIVER LE BOUTON APRÈS VALIDATION ET IMPRESSION
					etatCmdValiderVirement = false;
					System.out.println(" Validation réussie - Bouton DÉSACTIVÉ: " + etatCmdValiderVirement);

				} else {
					setStrEtatValidationTrt("2");
					etatCmdValiderVirement = true; // Bouton reste activé en cas d'échec
					 System.out.println(" Validation échouée - Bouton reste ACTIVÉ");
				}

			} else {
				messageValidationVirementBct = "Liste des bénéficiaires est vide";
				etatCmdValiderVirement = true; // Bouton reste activé
				System.out.println(" ERREUR - Bouton reste ACTIVÉ");
			}

		} catch (Exception e) {
			setStrEtatValidationTrt("2");
			etatCmdValiderVirement = true; // Bouton reste activé en cas d'erreur
			messageValidationVirementBct = e.getMessage();
			logger.error(e.getMessage());
		}
	}

	public void imprimerVirementMandatBct() {
		try {
			srcRepport = "";
			Connection connection = null;
			// Paramètres de base
			parametersJasper.put("P_PATH", getStrPath());
			parametersJasper.put("P_NUM_MATR_USER", getParamAgence().getNumMatrUser());
			parametersJasper.put("COD_STRC_STRC", getParamAgence().getCodStrcStrc() + "");
			parametersJasper.put("LIB_STRC_STRC", getParamAgence().getLibStrcStrc());
			parametersJasper.put("P_RIB_DO", getStrRib());

			// Date comptable
			parametersJasper.put("P_DATE_COMPTABLE", getParamAgence().getDateComptable());

			// NUM_OVIR_OVIR
			String numOvirOvir = (String) parametersJasper.get("P_NUM_VIREMENT");
			parametersJasper.put("NUM_OVIR_OVIR", numOvirOvir);

			// DAT_OVIR_OVIR comme String (pas d'objet Date)
			parametersJasper.put("DAT_OVIR_OVIR", DateHandler.strToDate(getParamAgence().getDateComptable()));

			Context context = ContextHandler.getContext();
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpServletRequest httpServletRequest = (HttpServletRequest) facesContext.getExternalContext().getRequest();
			DataSource datasource = (DataSource) context.getBean("dataSource");
			connection = datasource.getConnection();

			if (reportName != null) {
				httpServletRequest.getSession().removeAttribute("REPORT_NAME_VIREMENT");
			}

			connection = datasource.getConnection();
			reportName = UtilCtr.printReport("dechargeVirementCentrale", "Virement", parametersJasper, connection);

			setSrcRepport(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
					+ "/reporting/Virement/" + reportName);

			httpServletRequest.getSession().setAttribute("REPORT_NAME_VIREMENT", reportName);

			/*
			 * FacesContext.getCurrentInstance().addMessage(null, new
			 * FacesMessage(FacesMessage.SEVERITY_INFO, "Succès",
			 * "Le PDF a été généré avec succès"));
			 */

			// NE RIEN MODIFIER ICI - la gestion de l'état du bouton se fait dans la méthode
			// appelante

		} catch (Exception e) {
			logger.error("Erreur lors de la génération du PDF: " + e.getMessage(), e);
			/*
			 * FacesContext.getCurrentInstance().addMessage(null, new
			 * FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur",
			 * "Impossible de générer le PDF: " + e.getMessage()));
			 */
			// En cas d'erreur d'impression, on réactive le bouton pour permettre de
			// réessayer
			etatCmdValiderVirement = true;
		}
	}

	public String annulerBoutton() {
	    try {
	        System.out.println("=== CLIC OK POPUP ===");
	        System.out.println("État validation: " + strEtatValidationTrt);
	        System.out.println("État bouton AVANT: " + etatCmdValiderVirement);
	        
	        //  SI SUCCÈS (état = 1), GARDER LE BOUTON DÉSACTIVÉ
	        if ("1".equals(strEtatValidationTrt)) {
	            etatCmdValiderVirement = false;
	            System.out.println(" Succès - Bouton reste DÉSACTIVÉ");
	           annulerSaisieBeneficiaire();
	        } else {
	            // En cas d'erreur, garder activé pour réessayer
	            etatCmdValiderVirement = true;
	            System.out.println("Échec - Bouton reste ACTIVÉ");
	        }
	        
	        System.out.println("État bouton APRÈS: " + etatCmdValiderVirement);
	        return null;
	        
	    } catch (Exception e) {
	        logger.error("Erreur annulerBoutton", e);
	        return null;
	    }
	}

	public void annulerVirementBoutton() {

		// / True
		if (getStrEtatValidationTrt() != null && getStrEtatValidationTrt().equals("1")) {
			setMontantVirement(null);
			messageMontant = "";
			compteBenificiaire = "";
			motif = "";
			benificiaire = "";
			messageValidationVirementBct = "";
			msgValidatorCompte = "";
			etatCmdValiderVirement = false;
			setRibBenificiaire("");
			setStrEtatValidationTrt("0");

		}
		// / False
		else {
			setMontantVirement(null);
			messageMontant = "";
			compteBenificiaire = "";
			msgValidatorCompte = "";
			motif = "";
			benificiaire = "";
			setRibBenificiaire("");
			messageValidationVirementBct = "";
			etatCmdValiderVirement = false;

			setStrEtatValidationTrt("0");

		}

	}

	public String quitterVirementMandatBct() {

		return "quitterVirementMandatBct";
	}

	public void validaterFormatCompte(FacesContext context, UIComponent component, Object value)
			throws ValidatorException {

		String testRibBenf = (String) value;
		etatRIBCorrecte = true;
		if (!testRibBenf.trim().equals("") && testRibBenf.trim() != null) {

			Pattern p = Pattern.compile("[0-9]{20}");
			Matcher m = p.matcher(testRibBenf);

			Pattern pBNA = Pattern.compile("[0-9]{13}");
			Matcher mBNA = pBNA.matcher(testRibBenf);
			if (testRibBenf.length() == 20 || testRibBenf.length() == 13) {

				if (!m.matches() && testRibBenf.length() == 20) {
					etatRIBCorrecte = false;
					throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Format RIB Incorrect ",
							"Format RIB Incorrect "));

				} else if (!mBNA.matches() && testRibBenf.length() == 13) {
					etatRIBCorrecte = false;
					throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Format RIB Incorrect ",
							"Format RIB Incorrect "));

				}

			} else {
				etatRIBCorrecte = false;
				throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Le RIB doit contenir 20 Chiffres ou 13 Chiffres si compte BNA",
						"Le RIB doit contenir 20 Chiffres ou 13 Chiffres si compte BNA "));
			}
		}

	}

	public void verifierCompte() {

		try {
			msgValidatorCompte = "";
			isEtatMotif();
			String strCodeBanque = "";
			String strCodeAgenceBanque = "";
			String strCompte = ""; // 13 c
			String strCle = "";
			benificiaire = "   ";

			String structure = "";
			String produit = "";
			String compteCCpt = "";

			boolean exist = false;
			boolean verifierLiaisonCompte = false;
			String rib = "";
			VirementVo virementVo = new VirementVo();
			if (getCompteBenificiaire().contains("-")) {

				rib = (getCompteBenificiaire().replace("-", ""));

			} else if (getCompteBenificiaire().contains(" ")) {

				rib = (getCompteBenificiaire().replace(" ", ""));

			} else {
				rib = getCompteBenificiaire();
			}

			// Cas Virement BNA BNA
			if (getCompteBenificiaire().length() == 13) {

				String CodAgence = getCompteBenificiaire().substring(0, 3);
				GetStructureCmd getStructureCmd = new GetStructureCmd();
				Structure structureBna = new Structure();
				structureBna.setCodStrcStrc(Long.valueOf(CodAgence));
				structureBna = (Structure) getStructureCmd.execute(structureBna);
				if (structureBna != null && structureBna.getCodBctStrc() != null) {
					String codBCT = StrHandler.lpad(structureBna.getCodBctStrc(), '0', 3);
					String petitRib = "03" + codBCT + getCompteBenificiaire();
					rib = petitRib + calculerRIB(petitRib);

				} else {
					etatRIBCorrecte = false;
					msgValidatorCompte = "Code agence erroné !!";
				}

			}

			if (rib.length() == 20) {

				setRibBenificiaire(rib);
				strCodeBanque = rib.substring(0, 2);
				strCodeAgenceBanque = rib.substring(2, 5);
				strCompte = rib.substring(5, 18);
				strCle = rib.substring(18, 20);

				virementVo.setStrCodbanque(strCodeBanque);
				virementVo.setStrCodAgenceBanque(strCodeAgenceBanque);
				virementVo.setStrCompte(strCompte);
				virementVo.setStrCle(strCle);
				virementVo.setStrPetitRib(strCodeBanque + strCodeAgenceBanque + strCompte);
				virementVo.setVerifier(exist);

				VirementCmd virementCmd = new VirementCmd();
				virementVo = (VirementVo) virementCmd.verifierRib(virementVo);

				exist = virementVo.isVerifier();

				// / -------------- Verifier que le RIB n'est pas déja saisie
				// -------------------///

				if (exist == false) {
					msgValidatorCompte = virementVo.getMessageVerificationRib();
					etatRIBCorrecte = false;

				} else if (strCodeBanque.equals("03")) {
					ContratCpt cpt = new ContratCpt();
					ContratCptId contratCptId = new ContratCptId();
					structure = getRibBenificiaire().substring(5, 8);
					produit = getRibBenificiaire().substring(8, 12);
					compteCCpt = getRibBenificiaire().substring(12, 18);

					contratCptId.setCodStrcStrc(new Long(structure));
					contratCptId.setCodPrdPrd(new Long(produit));
					contratCptId.setNumCcptCcpt(new Long(compteCCpt));

					// recherche contrat
					GetDetailContratCmd getDetailContratCmd = new GetDetailContratCmd();

					// -------------------------------------------------------------------
					// ------------- Recherche des donnée du Contrat et du Client ///
					cpt = (ContratCpt) getDetailContratCmd.execute(contratCptId);

					if (cpt != null && cpt.getContratCptId() != null) {

						// // Verifier si le RIB Beneficiaire n'est pas en Devise
						boolean boolRibBenifEnDevise = false;
						boolean boolRibBenfiEnDinarsConvertible = false;
						boolean boolRibBenfiSpeciauxEnDinars = false;

						int i = 0;
						while (boolRibBenifEnDevise == false && i < Constants.listCompteEnDevises.length) {
							if (new Long(produit).longValue() == Constants.listCompteEnDevises[i].longValue()) {
								boolRibBenifEnDevise = true;
							}
							i++;
						}

						if (boolRibBenifEnDevise == true) {
							msgValidatorCompte = "Impossible d'éffectuer un virement vers un RIB Bénéficiaire en Devises !";
							verifierLiaisonCompte = true;
						} else {

							// / Veriffier si Le RIB Beneficiaire est en Dinars Convertible
							i = 0;
							while (boolRibBenfiEnDinarsConvertible == false
									&& i < Constants.listCompteEnDinarsConvertibles.length) {
								if (new Long(produit).longValue() == Constants.listCompteEnDinarsConvertibles[i]
										.longValue()) {
									boolRibBenfiEnDinarsConvertible = true;
								}
								i++;
							}

							// / Veriffier si Le RIB Beneficiaire est un Compte Spéciaux en Dinars
							i = 0;
							while (boolRibBenfiSpeciauxEnDinars == false
									&& i < Constants.listCompteSpeciauxEnDinars.length) {
								if (new Long(produit).longValue() == Constants.listCompteSpeciauxEnDinars[i]
										.longValue()) {
									boolRibBenfiSpeciauxEnDinars = true;
								}
								i++;
							}

						}

						// /////////////////////////////////////////////////
						if (verifierLiaisonCompte == false) {

							String numCompteBenif = getRibBenificiaire().substring(5, 18);

							// /Verifier Que le compte Beneficiaire est cloturé
							if (cpt.getCodEtatCcpt().equals("R")) {
								msgValidatorCompte = "Le compte Bénéficiaire est Cloturé !";
							} else {

								benificiaire = cpt.getNomIntiCcpt();

							}
						} else {
							etatRIBCorrecte = false;
						}
					} else {
						benificiaire = "   ";
						msgValidatorCompte = "Compte Bénéficiaire inexistant!";
						etatRIBCorrecte = false;
					}

				} else {

					msgValidatorCompte = "Compte Bénéficiaire n'est pas un compte BNA!";
					etatRIBCorrecte = false;

				}

			} else if (msgValidatorCompte != null && msgValidatorCompte.length() == 0) {
				etatRIBCorrecte = false;
				msgValidatorCompte = "Le RIB doit contenir 20 caractéres ";
			}

		} catch (NumberFormatException e) {
			etatRIBCorrecte = false;
			msgValidatorCompte = " RIB incorrecte ";

		} catch (Exception e) {
			etatRIBCorrecte = false;
			msgValidatorCompte = e.getMessage();
		}

	}

	public boolean verifierRib(String rib) {

		try {

			String strCodeBanque = "";
			String strCodeAgenceBanque = "";
			String strCompte = ""; // 13 c
			String strCle = "";
			String structure = "";
			String produit = "";
			String compteCCpt = "";

			boolean exist = false;
			boolean verifierLiaisonCompte = false;
			VirementVo virementVo = new VirementVo();
			etatRIBCorrecte = true;

			if (rib.length() == 20) {

				setRibBenificiaire(rib);

				strCodeBanque = rib.substring(0, 2);
				strCodeAgenceBanque = rib.substring(2, 5);
				strCompte = rib.substring(5, 18);
				strCle = rib.substring(18, 20);

				virementVo.setStrCodbanque(strCodeBanque);
				virementVo.setStrCodAgenceBanque(strCodeAgenceBanque);
				virementVo.setStrCompte(strCompte);
				virementVo.setStrCle(strCle);
				virementVo.setStrPetitRib(strCodeBanque + strCodeAgenceBanque + strCompte);
				virementVo.setVerifier(exist);

				VirementCmd virementCmd = new VirementCmd();
				virementVo = (VirementVo) virementCmd.verifierRib(virementVo);

				exist = virementVo.isVerifier();

				// / -------------- Verifier que le RIB n'est pas déja saisie
				// -------------------///

				if (exist == false) {
					etatRIBCorrecte = false;

				} else if (strCodeBanque.equals("03")) {
					ContratCpt cpt = new ContratCpt();
					ContratCptId contratCptId = new ContratCptId();
					structure = getRibBenificiaire().substring(5, 8);
					produit = getRibBenificiaire().substring(8, 12);
					compteCCpt = getRibBenificiaire().substring(12, 18);

					contratCptId.setCodStrcStrc(new Long(structure));
					contratCptId.setCodPrdPrd(new Long(produit));
					contratCptId.setNumCcptCcpt(new Long(compteCCpt));

					// recherche contrat
					GetDetailContratCmd getDetailContratCmd = new GetDetailContratCmd();

					// -------------------------------------------------------------------
					// ------------- Recherche des donnée du Contrat et du Client ///
					cpt = (ContratCpt) getDetailContratCmd.execute(contratCptId);

					if (cpt != null && cpt.getContratCptId() != null) {

						// // Verifier si le RIB Beneficiaire n'est pas en Devise
						boolean boolRibBenifEnDevise = false;
						boolean boolRibBenfiEnDinarsConvertible = false;
						boolean boolRibBenfiSpeciauxEnDinars = false;

						int i = 0;
						while (boolRibBenifEnDevise == false && i < Constants.listCompteEnDevises.length) {
							if (new Long(produit).longValue() == Constants.listCompteEnDevises[i].longValue()) {
								boolRibBenifEnDevise = true;
							}
							i++;
						}

						if (boolRibBenifEnDevise == true) {

							verifierLiaisonCompte = true;
						} else {

							// / Veriffier si Le RIB Beneficiaire est en Dinars Convertible
							i = 0;
							while (boolRibBenfiEnDinarsConvertible == false
									&& i < Constants.listCompteEnDinarsConvertibles.length) {
								if (new Long(produit).longValue() == Constants.listCompteEnDinarsConvertibles[i]
										.longValue()) {
									boolRibBenfiEnDinarsConvertible = true;
								}
								i++;
							}

							// / Veriffier si Le RIB Beneficiaire est un Compte Spéciaux en Dinars
							i = 0;
							while (boolRibBenfiSpeciauxEnDinars == false
									&& i < Constants.listCompteSpeciauxEnDinars.length) {
								if (new Long(produit).longValue() == Constants.listCompteSpeciauxEnDinars[i]
										.longValue()) {
									boolRibBenfiSpeciauxEnDinars = true;
								}
								i++;
							}

						}

						// /////////////////////////////////////////////////
						if (verifierLiaisonCompte == false) {

							String numCompteBenif = getRibBenificiaire().substring(5, 18);

							// /Verifier Que le compte Beneficiaire est cloturé
							if (cpt.getCodEtatCcpt().equals("R")) {
								etatRIBCorrecte = false;
							}
						} else {
							etatRIBCorrecte = false;
						}
					} else {
						etatRIBCorrecte = false;
					}

				} else {

					etatRIBCorrecte = false;
				}

			} else if (msgValidatorCompte != null && msgValidatorCompte.length() == 0) {
				etatRIBCorrecte = false;
			}

		} catch (NumberFormatException e) {
			etatRIBCorrecte = false;
		} catch (Exception e) {
			etatRIBCorrecte = false;

		}
		return etatRIBCorrecte;
	}

	public String calculerRIB(String RIB) {

		String resultat = "";
		if (RIB.length() == 18) {

			String RI = RIB;
			BigInteger rr = new BigInteger(RI.concat("00"));
			int rest = rr.mod(new BigInteger("97")).intValue();
			int nb = 97 - rest;
			String nbr = "" + nb;
			if (nbr.length() == 1)
				resultat = "0" + nbr;
			else
				resultat = nbr;
		}
		return resultat;
	}

	static final Comparator<String> NUMREMISE_ORDER = new Comparator<String>() {
		public int compare(String a1, String a2) {
			try {
				// Vérifier les valeurs nulles
				if (a1 == null && a2 == null)
					return 0;
				if (a1 == null)
					return 1;
				if (a2 == null)
					return -1;

				// Nettoyer les chaînes
				a1 = a1.trim();
				a2 = a2.trim();

				// Comparer
				Long val1 = Long.valueOf(a1);
				Long val2 = Long.valueOf(a2);

				return val2.compareTo(val1); // Ordre décroissant

			} catch (NumberFormatException e) {
				// En cas d'erreur, faire une comparaison de chaînes
				return a2.compareTo(a1);
			}
		}
	};

	public List<String> getListNumRemise() {
		try {
			listNumRemise.clear();
			VirementVo virementVo = new VirementVo();
			virementVo.setParamAgence(getParamAgence());
			VirementCmd virementCmd = new VirementCmd();

			virementVo = (VirementVo) virementCmd.getListNumRemiseOrdreVirement(virementVo);

			if (virementVo.getListNumRemises() != null) {
				// Filtrer les valeurs nulles et invalides
				for (String numRemise : virementVo.getListNumRemises()) {
					if (numRemise != null && !numRemise.contains("null") && numRemise.trim().length() > 0) {
						try {
							// Vérifier que c'est un nombre valide
							Long.parseLong(numRemise);
							listNumRemise.add(numRemise);
						} catch (NumberFormatException e) {
							System.out.println("Numéro de remise invalide ignoré : " + numRemise);
						}
					}
				}
				Collections.sort(listNumRemise, NUMREMISE_ORDER);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return listNumRemise;
	}

	// Méthode pour réinitialiser les filtres et la liste
	public String reinitialiserListe() {
		try {
			listeOrdresVirements.clear();
			numRemise = "";
			montantMinimal = null;
			messageEnregistBeneficiaire = "";
			selectedOrdreVirement = null;
			listeBeneficiaireOrdreVirement.clear();
			listeOrdresVirements.clear();
			selectedBeneficiaire = null;
			messageInformation = "";
			return null;

		} catch (Exception e) {
			System.out.println("Erreur dans reinitialiserListe : ");
			e.printStackTrace();
			return null;
		}
	}

	// Calcul du montant total des ordres affichés
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

	// Calcul du nombre total de virements
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

	// Getter et setter pour montantMinimal
	public Double getMontantMinimal() {
		return montantMinimal;
	}

	public void setMontantMinimal(Double montantMinimal) {
		this.montantMinimal = montantMinimal;
	}

	public String rechercherGlobalVirementByRemise() {
		try {
			// Réinitialisation des listes et messages
			listeOrdresVirements.clear();
			listeBeneficiaires.clear();
			listeBeneficiairesAjoutes.clear();
			messageEnregistBeneficiaire = "";
			messageInformation = ""; //
			montantVirement = "";
			selectedOrdreVirement = null; //

			System.out.println("NumRemise saisi : " + getNumRemise());

			if (getNumRemise() == null || getNumRemise().trim().isEmpty()) {
				System.out.println("NumRemise est null ou vide !");
				messageInformation = "Veuillez saisir un numéro de remise";
				return null;
			}

			boolean trouve = getListNumRemise().contains(getNumRemise());
			if (!trouve) {
				System.out.println("NumRemise non trouvé dans la liste : " + getNumRemise());

				selectedOrdreVirement = null;
				if (getRechercheClientCtr() != null) {
					getRechercheClientCtr().resetAttributes();
				}
				messageInformation = "N° de Remise non trouvé dans la liste des remises disponibles";

			}

			VirementVo virementVo = new VirementVo();
			virementVo.setNumRemise(getNumRemise());
			virementVo.setParamAgence(getParamAgence());

			VirementCmd virementCmd = new VirementCmd();
			virementVo = (VirementVo) virementCmd.getOrdreVirementCentraleByNumRemise(virementVo);

			if (virementVo.getListeOrdresVirements() == null || virementVo.getListeOrdresVirements().isEmpty()) {
				System.out.println("Aucun ordre trouvé pour numRemise=" + getNumRemise());
				messageInformation = "Aucun ordre de virement trouvé pour le numéro de remise : " + getNumRemise();
				return null;
			}

			listeOrdresVirements.addAll(virementVo.getListeOrdresVirements());

			// Sélectionner le premier ordre trouvé
			setSelectedOrdreVirement(virementVo.getListeOrdresVirements().get(0));

			// Remplissage des données de l'UI pour les détails
			setMontantGlobal(getSelectedOrdreVirement().getMontGlobOvir() + "");
			setNbreVirement(getSelectedOrdreVirement().getNbrVirOvir());

			// Charger les bénéficiaires si nécessaire pour les détails
			listeBeneficiaires.clear();
			if (getSelectedOrdreVirement().getBeneficiaireOrdreVirements() != null) {
				listeBeneficiaires.addAll(getSelectedOrdreVirement().getBeneficiaireOrdreVirements());
			}

			messageInformation = "Ordre trouvé pour la remise " + getNumRemise();

			derniereMiseAJour = new Date();

			System.out.println("OrdreVirement récupéré et ajouté à la liste pour affichage.");
			System.out.println("Taille liste ordres : " + listeOrdresVirements.size());
			System.out.println("Ordre sélectionné : "
					+ (selectedOrdreVirement != null ? selectedOrdreVirement.getOrdreVirementId().getNumOvirOvir()
							: "null"));

			return null; // Reste sur la page de consultation

		} catch (Exception e) {
			System.out.println("Erreur dans rechercherGlobalVirementByRemise : ");
			e.printStackTrace();
			messageInformation = "Erreur lors de la recherche : " + e.getMessage();
			return null;
		}
	}

	public String listeTousLesOrdresVirement() {
		try {
			// Réinitialisation des listes
			if (listeOrdresVirements == null) {
				listeOrdresVirements = new ArrayList<OrdreVirement>();
			}
			listeOrdresVirements.clear();
			selectedOrdreVirement = null;

			System.out.println("Récupération de tous les ordres de virement...");

			// Création du VO pour récupérer tous les ordres
			VirementVo virementVo = new VirementVo();

			// Définir les paramètres si nécessaire (ex: agence courante)
			if (getParamAgence() != null) {
				virementVo.setParamAgence(getParamAgence());
			}

			// Appel de la commande pour récupérer tous les ordres
			VirementCmd virementCmd = new VirementCmd();

			// Option 1: Utiliser le traitement directement
			// GetOrdreVirementCentraleByNumRemiseTrt traitement = new
			// GetOrdreVirementCentraleByNumRemiseTrt();
			// virementVo = (VirementVo) traitement.perform(virementVo);

			// Option 2: OU utiliser une méthode dans VirementCmd (si vous l'ajoutez)
			virementVo = (VirementVo) virementCmd.getOrdreVirementCentral(virementVo);

			// Vérification du résultat
			if (virementVo.getListeOrdresVirements() != null && !virementVo.getListeOrdresVirements().isEmpty()) {

				// Remplissage de la liste des ordres pour l'affichage
				listeOrdresVirements.addAll(virementVo.getListeOrdresVirements());

				System.out.println("Nombre total d'ordres récupérés : " + listeOrdresVirements.size());
			} else {
				System.out.println("Aucun ordre de virement trouvé");
			}

			return null; // Reste sur la même page

		} catch (Exception e) {
			System.out.println("Erreur dans listeTousLesOrdresVirement : ");
			e.printStackTrace();
			return null;
		}
	}

	public String ListeTousLesOrdresVirement1() {
		try {
			// Réinitialisation des listes et messages
			listeOrdresVirements.clear();
			listeBeneficiaires.clear();
			listeBeneficiairesAjoutes.clear();
			messageEnregistBeneficiaire = "";
			montantVirement = "";
			selectedOrdreVirement = null;

			System.out.println("Récupération de tous les ordres de virement...");

			// Création du VO pour récupérer tous les ordres
			VirementVo virementVo = new VirementVo();

			// Définir les paramètres si nécessaire (ex: agence courante)
			if (getParamAgence() != null) {
				virementVo.setParamAgence(getParamAgence());
			}

			// Appel de la commande pour récupérer tous les ordres
			VirementCmd virementCmd = new VirementCmd();
			virementVo = (VirementVo) virementCmd.getOrdreVirementCentral(virementVo);

			// Vérification du résultat
			if (virementVo.getListeOrdresVirements() == null || virementVo.getListeOrdresVirements().isEmpty()) {
				System.out.println("Aucun ordre de virement trouvé");
				messageEnregistBeneficiaire = "Aucun ordre de virement trouvé";
				return "listeTousOrdres"; // Retourner vers la page de liste
			}

			// Remplissage de la liste des ordres pour l'affichage
			listeOrdresVirements.addAll(virementVo.getListeOrdresVirements());

			// Calcul des statistiques (optionnel)
			int totalOrdres = listeOrdresVirements.size();
			double montantTotal = 0;
			int totalVirements = 0;

			for (OrdreVirement ordre : listeOrdresVirements) {
				if (ordre.getMontGlobOvir() != null) {
					montantTotal += ordre.getMontGlobOvir();
				}
				if (ordre.getNbrVirOvir() != null) {
					totalVirements += ordre.getNbrVirOvir();
				}
			}

			System.out.println("Nombre total d'ordres récupérés : " + totalOrdres);
			System.out.println("Montant total : " + montantTotal);
			System.out.println("Nombre total de virements : " + totalVirements);

			// Message de succès
			messageEnregistBeneficiaire = "Liste des ordres de virement chargée avec succès (" + totalOrdres
					+ " ordres)";

			return "listeTousOrdres"; // Navigation vers la page de liste

		} catch (Exception e) {
			System.out.println("Erreur dans ListeTousLesOrdresVirement : ");
			e.printStackTrace();
			messageEnregistBeneficiaire = "Erreur lors de la récupération des ordres de virement";
			return null;
		}
	}

	/**
	 * Méthode appelée automatiquement au chargement de la page
	 */
	public void chargerTousLesOrdresAuChargement() {
		try {
			System.out.println("Chargement automatique des ordres au chargement de la page...");

			// Appeler la méthode de chargement
			listeTousLesOrdresVirement();

			// Mettre à jour le message et la date
			if (listeOrdresVirements != null && !listeOrdresVirements.isEmpty()) {
				messageInformation = "Liste chargée automatiquement : " + listeOrdresVirements.size()
						+ " ordres trouvés";
			} else {
				messageInformation = "Aucun ordre de virement trouvé dans le système";
			}

			derniereMiseAJour = new Date();

		} catch (Exception e) {
			System.out.println("Erreur lors du chargement automatique : ");
			e.printStackTrace();
			messageInformation = "Erreur lors du chargement des données";
		}
	}

	/**
	 * Afficher les détails complets d'un ordre
	 */
	public String afficherDetailsComplets(OrdreVirement ordre) {
		try {
			if (ordre == null) {
				messageInformation = "Ordre non valide";
				return null;
			}

			selectedOrdreVirement = ordre;

			// Charger les détails supplémentaires si nécessaire
			if (ordre.getOrdreVirementId() != null) {
				numRemise = ordre.getOrdreVirementId().getNumOvirOvir();
			}

			messageInformation = "Détails de l'ordre " + numRemise + " affichés";

			return null;

		} catch (Exception e) {
			System.out.println("Erreur dans afficherDetailsComplets : ");
			e.printStackTrace();
			messageInformation = "Erreur lors de l'affichage des détails";
			return null;
		}
	}

	/**
	 * Modifier un ordre sélectionné
	 */
	public String modifierOrdre() {
		try {
			if (selectedOrdreVirement == null) {
				messageInformation = "Aucun ordre sélectionné pour modification";
				return null;
			}

			// Appeler votre logique de modification existante
			// Par exemple : return afficherVirementPonctuel();

			System.out.println(
					"Modification de l'ordre : " + selectedOrdreVirement.getOrdreVirementId().getNumOvirOvir());

			System.out.println("size ordre:" + selectedOrdreVirement.getBeneficiaireOrdreVirements().size());

			return "modificationVirement"; // Navigation vers la page de modification

		} catch (Exception e) {
			System.out.println("Erreur dans modifierOrdre : ");
			e.printStackTrace();
			messageInformation = "Erreur lors de la modification";
			return null;
		}
	}

	/**
	 * Afficher les bénéficiaires d'un ordre
	 */
	public String afficherBeneficiaires() {
		try {
			if (selectedOrdreVirement == null) {
				messageInformation = "Aucun ordre sélectionné";
				return null;
			}

			// Charger les bénéficiaires
			if (selectedOrdreVirement.getBeneficiaireOrdreVirements() != null) {
				int nbBenef = selectedOrdreVirement.getBeneficiaireOrdreVirements().size();
				messageInformation = "Ordre contient " + nbBenef + " bénéficiaires";
			} else {
				messageInformation = "Aucun bénéficiaire trouvé pour cet ordre";
			}

			// Naviguer vers la page des bénéficiaires
			return "listeBeneficiaires";

		} catch (Exception e) {
			System.out.println("Erreur dans afficherBeneficiaires : ");
			e.printStackTrace();
			messageInformation = "Erreur lors de l'affichage des bénéficiaires";
			return null;
		}
	}

	/**
	 * Exporter la liste vers Excel ou CSV
	 */
	public String exporterListe() {
		try {
			if (listeOrdresVirements == null || listeOrdresVirements.isEmpty()) {
				messageInformation = "Aucune donnée à exporter";
				return null;
			}

			// Logique d'export ici
			// Par exemple, générer un fichier CSV ou Excel

			messageInformation = "Export de " + listeOrdresVirements.size() + " ordres en cours...";

			System.out.println("Export de la liste demandé");

			return null;

		} catch (Exception e) {
			System.out.println("Erreur dans exporterListe : ");
			e.printStackTrace();
			messageInformation = "Erreur lors de l'export";
			return null;
		}
	}

	// Getters et setters pour les nouvelles propriétés
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

	/**
	 * Sélectionner un ordre (compatible JSF ancien)
	 */
	public String selectionnerOrdre() {
		try {
			if (selectedOrdreVirement == null) {
				messageInformation = "Aucun ordre sélectionné";
				return null;
			}

			if (selectedOrdreVirement.getOrdreVirementId() != null) {
				numRemise = selectedOrdreVirement.getOrdreVirementId().getNumOvirOvir();
				messageInformation = "Ordre " + numRemise + " sélectionné";
			}

			return null;

		} catch (Exception e) {
			e.printStackTrace();
			messageInformation = "Erreur lors de la sélection";
			return null;
		}
	}

	/**
	 * Fermer les détails
	 */
	public String fermerDetails() {
		selectedOrdreVirement = null;
		messageInformation = "";
		return null;
	}

	/**
	 * Filtrer la liste des ordres
	 */
	public String filtrerOrdres() {
		try {
			if (listeOrdresVirements == null || listeOrdresVirements.isEmpty()) {
				messageInformation = "Aucune liste à filtrer. Chargez d'abord la liste complète.";
				return null;
			}

			List<OrdreVirement> listeOriginale = new ArrayList<OrdreVirement>(listeOrdresVirements);
			List<OrdreVirement> listeFiltrée = new ArrayList<OrdreVirement>();

			for (OrdreVirement ordre : listeOriginale) {
				boolean inclure = true;

				// Filtrer par numéro de remise si spécifié
				if (numRemise != null && !numRemise.trim().isEmpty()) {
					if (ordre.getOrdreVirementId() != null && ordre.getOrdreVirementId().getNumOvirOvir() != null) {
						if (!ordre.getOrdreVirementId().getNumOvirOvir().toLowerCase()
								.contains(numRemise.toLowerCase())) {
							inclure = false;
						}
					} else {
						inclure = false;
					}
				}

				// Filtrer par montant minimum si spécifié
				if (montantMinimal != null && montantMinimal > 0) {
					if (ordre.getMontGlobOvir() == null || ordre.getMontGlobOvir() < montantMinimal) {
						inclure = false;
					}
				}

				if (inclure) {
					listeFiltrée.add(ordre);
				}
			}

			// Remplacer la liste par la liste filtrée
			listeOrdresVirements.clear();
			listeOrdresVirements.addAll(listeFiltrée);

			messageInformation = "Filtrage effectué : " + listeFiltrée.size() + " ordres trouvés";

			return null;

		} catch (Exception e) {
			e.printStackTrace();
			messageInformation = "Erreur lors du filtrage";
			return null;
		}
	}

	public String afficherBeneficiairesSelectionnes() {
		// Navigation vers la page des bénéficiaires
		return "modificationBeneficiairepecVirementMandatBct";
	}

	public String modifierOrdreSelectionne() {
		try {
			if (selectedOrdreVirement == null) {
				messageInformation = "Aucun ordre sélectionné pour modification";
				return null;
			}

			// Charger les bénéficiaires de l'ordre sélectionné
			if (listeBeneficiaires == null) {
				listeBeneficiaires = new ArrayList<BeneficiaireOrdreVirement>();
			}
			listeBeneficiaires.clear();

			if (selectedOrdreVirement.getBeneficiaireOrdreVirements() != null) {
				listeBeneficiaires.addAll(selectedOrdreVirement.getBeneficiaireOrdreVirements());
			}

			// Autres initialisations...
			if (selectedOrdreVirement.getOrdreVirementId() != null) {
				numRemise = selectedOrdreVirement.getOrdreVirementId().getNumOvirOvir();
			}

			if (selectedOrdreVirement.getMontGlobOvir() != null) {
				setMontantGlobal(selectedOrdreVirement.getMontGlobOvir().toString());
			}

			if (selectedOrdreVirement.getNbrVirOvir() != null) {
				setNbreVirement(selectedOrdreVirement.getNbrVirOvir());
			}

			System.out.println("Ordre préparé pour modification avec " + listeBeneficiaires.size() + " bénéficiaires");

			return "modificationBeneficiairepecVirementMandatBct"; // Navigation vers la page de modification

		} catch (Exception e) {
			System.out.println("Erreur dans modifierOrdreSelectionne : ");
			e.printStackTrace();
			messageInformation = "Erreur lors de la préparation de la modification";
			return null;
		}
	}

	public void deselectionnerOrdre() {
		this.selectedOrdreVirement = null;
	}

	// *************** Getter and Setter ******************//

	public String getMontantVirement() {
		try {
			if (montantVirement != null && montantVirement.length() != 0) {
				long mnt = Long.valueOf(montantVirement.replace(".", "").replace(" ", "").trim());
				montantVirement = StrHandler.formatMontant(mnt, Long.valueOf(3));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return montantVirement;
	}

	public void setMontantVirement(String montantVirement) {
		this.montantVirement = montantVirement;
	}

	public String getMessageMontant() {
		return messageMontant;
	}

	public void setMessageMontant(String messageMontant) {
		this.messageMontant = messageMontant;
	}

	public void setEtatCmdValiderVirement(boolean etatCmdValiderVirement) {
		this.etatCmdValiderVirement = etatCmdValiderVirement;
	}

	public boolean isEtatCmdValiderVirement() {
		try {
			if (listeBeneficiaireOrdreVirement != null && listeBeneficiaireOrdreVirement.size() != 0) {
				etatCmdValiderVirement = true;
			} else {
				etatCmdValiderVirement = false;
			}
		} catch (Exception e) {
			etatCmdValiderVirement = false;
			e.printStackTrace();
		}
		return etatCmdValiderVirement;
	}

	public ParamAgence getParamAgence() {
		paramAgence = UtilCtr.getParamAgenceFromGeneralCtr();
		return paramAgence;
	}

	public void setParamAgence(ParamAgence paramAgence) {
		this.paramAgence = paramAgence;
	}

	public String getStrEtatValidationTrt() {
		return strEtatValidationTrt;
	}

	public void setStrEtatValidationTrt(String strEtatValidationTrt) {
		this.strEtatValidationTrt = strEtatValidationTrt;
	}

	public String getStrNumSeqGvir() {
		return strNumSeqGvir;
	}

	public void setStrNumSeqGvir(String strNumSeqGvir) {
		this.strNumSeqGvir = strNumSeqGvir;
	}

	public String getStrRib() {
		return strRib;
	}

	public void setStrRib(String strRib) {
		this.strRib = strRib;
	}

	public String getStrPath() {
		return strPath;
	}

	public void setStrPath(String strPath) {
		this.strPath = strPath;
	}

	public String getStrNumMatUser() {
		return strNumMatUser;
	}

	public void setStrNumMatUser(String strNumMatUser) {
		this.strNumMatUser = strNumMatUser;
	}

	public String getReportName() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletRequest httpServletRequest = (HttpServletRequest) facesContext.getExternalContext().getRequest();
		reportName = (String) httpServletRequest.getSession().getAttribute("REPORT_NAME_VIREMENT");

		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
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

	public String getStrTypeVirement() {
		return strTypeVirement;
	}

	public void setStrTypeVirement(String strTypeVirement) {
		this.strTypeVirement = strTypeVirement;
	}

	public String getStrNomInitCpt() {
		return strNomInitCpt;
	}

	public void setStrNomInitCpt(String strNomInitCpt) {
		this.strNomInitCpt = strNomInitCpt;
	}

	public String getStrNombreVirement() {
		return strNombreVirement;
	}

	public void setStrNombreVirement(String strNombreVirement) {
		this.strNombreVirement = strNombreVirement;
	}

	public String getStrMontantVirement() {
		return strMontantVirement;
	}

	public void setStrMontantVirement(String strMontantVirement) {
		this.strMontantVirement = strMontantVirement;
	}

	public String getStrDateExecution() {
		return strDateExecution;
	}

	public void setStrDateExecution(String strDateExecution) {
		this.strDateExecution = strDateExecution;
	}

	public void setCompteBenificiaire(String compteBenificiaire) {
		this.compteBenificiaire = compteBenificiaire;
	}

	public String getCompteBenificiaire() {
		return compteBenificiaire;
	}

	public void setBenificiaire(String benificiaire) {
		this.benificiaire = benificiaire;
	}

	public String getBenificiaire() {
		return benificiaire;
	}

	public void setMotif(String motif) {
		this.motif = motif;
	}

	public String getMotif() {
		return motif;
	}

	public void setMsgValidatorCompte(String msgValidatorCompte) {
		this.msgValidatorCompte = msgValidatorCompte;
	}

	public String getMsgValidatorCompte() {
		return msgValidatorCompte;
	}

	public void setEtatMotif(boolean etatMotif) {
		this.etatMotif = etatMotif;
	}

	public boolean isEtatMotif() {
		try {
			if (getRibBenificiaire() != null && getRibBenificiaire().length() == 20) {
				if (new Long(getRibBenificiaire().substring(0, 2)).longValue() == 3 && benificiaire.length() > 0
						&& getMontantVirement().length() > 0
						&& Long.valueOf(getMontantVirement().replace(".", "").replace(" ", "").trim()) > 0
						&& msgValidatorCompte.length() < 1) {
					etatMotif = true;
				} else {
					etatMotif = false;
				}

			} else {
				etatMotif = false;
			}

		} catch (Exception e) {
			etatMotif = false;
		}
		logger.info("etatMotif = " + etatMotif);
		return etatMotif;
	}

	public String getRibBenificiaire() {
		return ribBenificiaire;
	}

	public void setRibBenificiaire(String ribBenificiaire) {
		this.ribBenificiaire = ribBenificiaire;
	}

	public boolean isEtatBenficiaire() {

		try {
			if (getRibBenificiaire() != null && getRibBenificiaire().length() == 20) {
				if (new Long(getRibBenificiaire().substring(0, 2)).longValue() == 3
						&& (msgValidatorCompte.length() < 1 || msgValidatorCompte.length() > 1)) {
					etatBenficiaire = true;
				} else {
					etatBenficiaire = false;
				}

			} else {
				etatBenficiaire = false;
			}

		} catch (Exception e) {
			etatBenficiaire = false;
		}
		logger.info("etatBenficiaire = " + etatBenficiaire);
		return etatBenficiaire;
	}

	public void setEtatBenficiaire(boolean etatBenficiaire) {
		this.etatBenficiaire = etatBenficiaire;
	}

	public void setSrcRepport(String srcRepport) {
		this.srcRepport = srcRepport;
	}

	public String getSrcRepport() {
		return srcRepport;
	}

	public void setEtatRIBCorrecte(boolean etatRIBCorrecte) {
		this.etatRIBCorrecte = etatRIBCorrecte;
	}

	public boolean isEtatRIBCorrecte() {
		return etatRIBCorrecte;
	}

	public String getCompteInterneMandat() {
		return compteInterneMandat;
	}

	public void setCompteInterneMandat(String compteInterneMandat) {
		this.compteInterneMandat = compteInterneMandat;
	}

	public String getMessageValidationVirementBct() {
		return messageValidationVirementBct;
	}

	public void setMessageValidationVirementBct(String messageValidationVirementBct) {
		this.messageValidationVirementBct = messageValidationVirementBct;
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

	public boolean isEtatSaveBenefVir() {
		return etatSaveBenefVir;
	}

	public void setEtatSaveBenefVir(boolean etatSaveBenefVir) {
		this.etatSaveBenefVir = etatSaveBenefVir;
	}

	public BeneficiaireOrdreVirement getBeneficiaireOrdreVirement() {
		return beneficiaireOrdreVirement;
	}

	public void setBeneficiaireOrdreVirement(BeneficiaireOrdreVirement beneficiaireOrdreVirement) {
		this.beneficiaireOrdreVirement = beneficiaireOrdreVirement;
	}

	public Long getNombreSaisie() {
		nombreSaisie = Long.valueOf(listeBeneficiaireOrdreVirement.size());
		return nombreSaisie;
	}

	public void setNombreSaisie(Long nombreSaisie) {
		this.nombreSaisie = nombreSaisie;
	}

	public Long getMontantSaisie() {
		montantSaisie = 0L;
		if (listeBeneficiaireOrdreVirement != null && listeBeneficiaireOrdreVirement.size() > 0) {

			for (BeneficiaireOrdreVirement virement : listeBeneficiaireOrdreVirement) {
				montantSaisie += virement.getMontOperBenf();
			}
		}
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

	public OrdreVirement getSelectedOrdreVirement() {
		return selectedOrdreVirement;
	}

	public void setSelectedOrdreVirement(OrdreVirement selectedOrdreVirement) {
		this.selectedOrdreVirement = selectedOrdreVirement;
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

	public List<OrdreVirement> getListeOrdresVirements() {
		return listeOrdresVirements;
	}

	public void setListeOrdresVirements(List<OrdreVirement> listeOrdresVirements) {
		this.listeOrdresVirements = listeOrdresVirements;
	}

	public String getMessageEnregistBeneficiaire() {
		return messageEnregistBeneficiaire;
	}

	public void setMessageEnregistBeneficiaire(String messageEnregistBeneficiaire) {
		this.messageEnregistBeneficiaire = messageEnregistBeneficiaire;
	}

	// Getters et setters
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

	public String getMsgValidatorRib() {
		return msgValidatorRib;
	}

	public void setMsgValidatorRib(String msgValidatorRib) {
		this.msgValidatorRib = msgValidatorRib;
	}

	// ****temp**
	// Méthodes pour éviter les erreurs de méthodes non trouvées
	public String creerNouveauBeneficiaire() {
		// Réinitialiser les champs
		ribBeneficiaire = "03404002010100096947";
		nomBeneficiaire = "bilel jandoubi";
		montantBeneficiaire = 2L;
		motifBeneficiaire = "bilel test";
		messageEnregistBeneficiaire = "bilel test";
		return null;
	}

	public String ajouterBeneficiaire() {
		messageEnregistBeneficiaire = "Bénéficiaire ajouté avec succès";
		return null;
	}

	public String editerBeneficiaire() {
		if (selectedBeneficiaire != null) {
			ribBeneficiaire = selectedBeneficiaire.getNumRibbBenf();
			nomBeneficiaire = selectedBeneficiaire.getNomPrnbBenf();
			montantBeneficiaire = selectedBeneficiaire.getMontOperBenf();
			motifBeneficiaire = "test";
		}
		return null;
	}

	public String supprimerBeneficiaire() {
		if (selectedBeneficiaire != null && listeBeneficiaires != null) {
			listeBeneficiaires.remove(selectedBeneficiaire);
			messageEnregistBeneficiaire = "Bénéficiaire supprimé avec succès";
		}
		return null;
	}

	public String verifierRib() {
		etatRIBCorrecte = true;
		msgValidatorRib = "";
		return null;
	}

	public String verifierRibEditer() {
		etatRIBCorrecte = true;
		msgValidatorRib = "";
		return null;
	}

	public void validaterFormatRib(FacesContext context, UIComponent component, Object value) {
		// Validation du format RIB
	}

	// Méthode pour actionListener (avec ActionEvent)
	public void modifierOrdreVirement1(ActionEvent event) {
		try {
			// Logique de modification de l'ordre
			if (selectedOrdreVirement == null) {
				messageErreur = "Aucun ordre sélectionné";
				return;
			}

			// Mettre à jour l'ordre avec les nouvelles valeurs
			if (montantGlobal != null && !montantGlobal.trim().isEmpty()) {
				selectedOrdreVirement.setMontGlobOvir(Long.parseLong(montantGlobal));
			}

			if (nbreVirement != null) {
				selectedOrdreVirement.setNbrVirOvir(nbreVirement);
			}

			/*
			 * if (dateExecution != null) {
			 * selectedOrdreVirement.setDatExeOvir(dateExecution); }
			 */

			// Sauvegarder les modifications
			// Appeler votre service de sauvegarde ici

			messageErreur = "Ordre de virement modifié avec succès";

			System.out.println("Ordre modifié : " + selectedOrdreVirement.getOrdreVirementId().getNumOvirOvir());

		} catch (Exception e) {
			System.out.println("Erreur dans modifierOrdreVirement : ");
			e.printStackTrace();
			messageErreur = "Erreur lors de la modification de l'ordre";
		}
	}

	// Getter
	public List<BeneficiaireOrdreVirement> getListeBeneficiaires() {
		if (listeBeneficiaires == null) {
			listeBeneficiaires = new ArrayList<BeneficiaireOrdreVirement>();
		}
		return listeBeneficiaires;
	}

	// Setter
	public void setListeBeneficiaires(List<BeneficiaireOrdreVirement> listeBeneficiaires) {
		this.listeBeneficiaires = listeBeneficiaires;
	}

	public String afficherOrdreVirementDetail() {

		if (getSelectedOrdreVirement() != null
				&& getSelectedOrdreVirement().getOrdreVirementId().getNumOvirOvir() != null) {
			listeBeneficiaireOrdreVirement.clear();
			setMontantGlobal(getSelectedOrdreVirement().getMontGlobOvir() + "");
			setNbreVirement(getSelectedOrdreVirement().getNbrVirOvir());
			setDateExecution(getSelectedOrdreVirement().getDatExecOvir());
			setRibBeneficiaire(getSelectedOrdreVirement().getCompteDebitOvir());
			setMotifBeneficiaire(getSelectedOrdreVirement().getCompteDebitOvir());
			setNumRemise(getSelectedOrdreVirement().getOrdreVirementId().getNumOvirOvir());
			setEtatRecherche(getSelectedOrdreVirement().getCodEtatOvir());

			// setRefAutBct(getSelectedGlobalVirement().getReferenceAutBCT());
			listeBeneficiaireOrdreVirement.addAll(getSelectedOrdreVirement().getBeneficiaireOrdreVirements());
			// System.out.println("test la list beneifi size:" +
			// listeBeneficiaireOrdreVirement.get(0));
			return "modificationBeneficiairepecVirementMandatBct";
		} else {
			return null;

		}
	}

	public void supprimerBeneficiaireOrdreVirement() {
		try {
			if (selectedBeneficiaire != null) {
				if (selectedBeneficiaire.getBeneficiaireOrdreVirementId() != null) {

					listeBeneficiaireOrdreVirementSupprimes.add(selectedBeneficiaire);
				}

				listeBeneficiaireOrdreVirement.remove(selectedBeneficiaire);
				calculerTotauxGlobaux();

			}
		} catch (Exception e) {
			e.printStackTrace(); // log pour debug
		}
	}

	public void modifierOrdreVirement(ActionEvent event) {
		VirementVo virementVo = new VirementVo();
		VirementCmd virementCmd = new VirementCmd();
		messageErreur = "";
		logger.info("--------------modifierOrdreVirement----------------");

		try {
			if (selectedOrdreVirement != null && selectedOrdreVirement.getOrdreVirementId().getNumOvirOvir() != null) {

				// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

				// System.out.println("Date execution" + sdf.format(strDateExecution));

				// System.out.println("selected Date execution" +
				// selectedOrdreVirement.getDatExecOvir());

				if (dateExecution != null) {
					selectedOrdreVirement.setDatExecOvir(dateExecution);
					System.out.println("Date récupérée : " + new SimpleDateFormat("dd/MM/yyyy").format(dateExecution));
				}

				virementVo.setOrdreVirement(selectedOrdreVirement);
				virementVo.setParamAgence(getParamAgence());
				virementVo.setListeBeneficiaireOrdreVirement(getListeBeneficiaireOrdreVirement());

				// Laisser le traitement gérer la modification de date
				// virementVo.getOrdreVirement().setDatExecOvir(getDateExecution());

				virementVo = (VirementVo) virementCmd.modifierOrdreVirementCentral(virementVo);
				messageErreur = virementVo.getMessageValidation();
			} else {
				messageErreur = "Ordre de virement non sélectionné";
			}
		} catch (Exception e) {
			e.printStackTrace();
			messageErreur = "Erreur lors de la modification";
		}
	}

	// Nouvelle méthode pour gérer la sélection de date manuellement
	public void onDateChange() {
		try {
			System.out.println("========== onDateChangeAlternative DEBUT ==========");

			// Récupérer les paramètres de la requête directement
			FacesContext context = FacesContext.getCurrentInstance();
			Map<String, String> params = context.getExternalContext().getRequestParameterMap();

			// Chercher le paramètre de date
			for (String key : params.keySet()) {
				System.out.println("Paramètre: " + key + " = " + params.get(key));
			}

			if (dateExecution != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				String dateStr = sdf.format(dateExecution);
				System.out.println("Date finale récupérée : " + dateStr);

				// Forcer la mise à jour de l'ordre
				if (selectedOrdreVirement != null) {
					selectedOrdreVirement.setDatExecOvir(dateExecution);
				}

				messageErreur = "";
			}

			System.out.println("========== onDateChangeAlternative FIN ==========");

		} catch (Exception e) {
			e.printStackTrace();
			messageErreur = "Erreur: " + e.getMessage();
		}
	}

	private String dateExecutionString;

	public String getDateExecutionString() {
		if (dateExecution != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			dateExecutionString = sdf.format(dateExecution);
		}
		return dateExecutionString;
	}

	public void setDateExecutionString(String dateExecutionString) {
		this.dateExecutionString = dateExecutionString;
	}

	// Validation du format de date
	public void validateDateFormat(FacesContext context, UIComponent component, Object value)
			throws ValidatorException {
		String dateStr = (String) value;

		if (dateStr != null && !dateStr.trim().isEmpty()) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				sdf.setLenient(false); // Strict parsing
				Date parsedDate = sdf.parse(dateStr.trim());

			} catch (Exception e) {
				e.printStackTrace();
				messageErreur = "Erreur: " + e.getMessage();
			}
		}
	}

	public void onDateStringChange() {
		try {
			if (dateExecutionString != null && !dateExecutionString.trim().isEmpty()) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				dateExecution = sdf.parse(dateExecutionString.trim());

				System.out.println("Date saisie et convertie : " + sdf.format(dateExecution));

				if (selectedOrdreVirement != null) {
					selectedOrdreVirement.setDatExecOvir(dateExecution);
				}

				messageErreur = "";
			}
		} catch (Exception e) {
			e.printStackTrace();
			messageErreur = "Erreur: " + e.getMessage();
		}
	}

	public void modifierBeneficiaire() {
		try {
			messageEnregistBeneficiaire = "";
			etatMessageEnregistBeneficiaire = false;

			BeneficiaireOrdreVirement beneficiaireSaisie = new BeneficiaireOrdreVirement();

			if (getMsgValidatorRib().length() == 0) {
				if (selectedBeneficiaire != null) {

					// Remplir les données du nouveau bénéficiaire
					beneficiaireSaisie.setNumRibbBenf(getRibBeneficiaire());
					beneficiaireSaisie.setNomPrnbBenf(getNomBeneficiaire());
					beneficiaireSaisie.setMontOperBenf(getMontantBeneficiaire());
					beneficiaireSaisie.setCodEtatDov(selectedBeneficiaire.getCodEtatDov()); // Garder l'état existant

					// Définir la structure bénéficiaire
					Structure strcBenef = new Structure();
					strcBenef.setCodStrcStrc(Long.valueOf(getRibBeneficiaire().substring(5, 8)));
					beneficiaireSaisie.setStructureBenef(strcBenef);

					// Associer à l'ordre de virement
					beneficiaireSaisie.setOrdreVirement(selectedBeneficiaire.getOrdreVirement());

					// Garder l'ID si c'est une modification d'un bénéficiaire existant
					if (selectedBeneficiaire.getBeneficiaireOrdreVirementId() != null) {
						beneficiaireSaisie
								.setBeneficiaireOrdreVirementId(selectedBeneficiaire.getBeneficiaireOrdreVirementId());
					}

					// Vérifier qu'on ne fait pas un virement vers le même compte que le débiteur
					String numCompteBenif = getRibBeneficiaire().substring(5, 18);
					String compteDebiteur = selectedBeneficiaire.getOrdreVirement().getCompteDebitOvir();

					if (compteDebiteur != null && compteDebiteur.contains(numCompteBenif)) {
						messageEnregistBeneficiaire = "Impossible d'effectuer un virement vers le même compte débiteur";
						etatMessageEnregistBeneficiaire = true;
					} else {
						// Vérifier que le RIB n'existe pas déjà dans la liste (sauf pour le
						// bénéficiaire en cours de modification)
						boolean ribExiste = false;
						for (BeneficiaireOrdreVirement benef : listeBeneficiaireOrdreVirement) {
							if (benef != selectedBeneficiaire && benef.getNumRibbBenf().equals(getRibBeneficiaire())) {
								ribExiste = true;
								break;
							}
						}

						/*
						 * if (ribExiste) { messageEnregistBeneficiaire =
						 * "Ce RIB existe déjà dans la liste des bénéficiaires";
						 * etatMessageEnregistBeneficiaire = true; } else {
						 */
						// Ajouter l'ancien bénéficiaire à la liste des supprimés si il a un ID
						if (selectedBeneficiaire.getBeneficiaireOrdreVirementId() != null) {
							listeBeneficiaireOrdreVirementSupprimes.add(selectedBeneficiaire);
						}

						// Remplacer dans la liste principale
						int index = listeBeneficiaireOrdreVirement.indexOf(selectedBeneficiaire);
						if (index >= 0) {
							listeBeneficiaireOrdreVirement.set(index, beneficiaireSaisie);
						}
						calculerTotauxGlobaux();
						// messageEnregistBeneficiaire = "Bénéficiaire modifié avec succès";
						messageEnregistBeneficiaire = "";
						etatMessageEnregistBeneficiaire = false;

						// Log pour debug
						System.out.println("Bénéficiaire modifié - RIB: " + beneficiaireSaisie.getNumRibbBenf()
								+ ", Montant: " + beneficiaireSaisie.getMontOperBenf());
						// }
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
			e.printStackTrace();
			messageEnregistBeneficiaire = "Erreur lors de la modification du bénéficiaire: " + e.getMessage();
			etatMessageEnregistBeneficiaire = true;
		}
	}

	public void calculerTotauxGlobaux() {
		try {
			// Calcul du montant global
			Long montantTotal = 0L;
			Long nombreTotal = 0L;

			if (listeBeneficiaireOrdreVirement != null && !listeBeneficiaireOrdreVirement.isEmpty()) {
				for (BeneficiaireOrdreVirement beneficiaire : listeBeneficiaireOrdreVirement) {
					if (beneficiaire.getMontOperBenf() != null) {
						montantTotal += beneficiaire.getMontOperBenf();
						nombreTotal++;
					}
				}
			}

			// Mise à jour des propriétés
			setMontantGlobal(StrHandler.formatMontant(montantTotal, Long.valueOf(3)));
			setNbreVirement(nombreTotal);

			// Mise à jour de l'ordre sélectionné si il existe
			if (selectedOrdreVirement != null) {
				selectedOrdreVirement.setMontGlobOvir(montantTotal);
				selectedOrdreVirement.setNbrVirOvir(nombreTotal);
			}

			System.out.println("Totaux calculés - Montant: " + montantGlobal + ", Nombre: " + nombreTotal);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Erreur lors du calcul des totaux: " + e.getMessage());
		}
	}

	public String getMontantGlobalFormate() {
		Long montantTotal = 0L;

		if (listeBeneficiaireOrdreVirement != null) {
			for (BeneficiaireOrdreVirement beneficiaire : listeBeneficiaireOrdreVirement) {
				if (beneficiaire.getMontOperBenf() != null) {
					montantTotal += beneficiaire.getMontOperBenf();
				}
			}
		}

		return StrHandler.formatMontant(montantTotal, Long.valueOf(3)) + " TND";
	}

	/**
	 * Convertit le code d'état en libellé explicite
	 */
	public String getLibelleEtatOrdre() {
		if (selectedOrdreVirement != null && selectedOrdreVirement.getCodEtatOvir() != null) {
			return convertirCodeEtat(selectedOrdreVirement.getCodEtatOvir());
		}
		return "";
	}

	/**
	 * Méthode utilitaire pour convertir les codes d'état
	 */
	public String convertirCodeEtat(String codeEtat) {
		if (codeEtat == null || codeEtat.trim().isEmpty()) {
			return "Non défini";
		}

		switch (codeEtat.toUpperCase().trim()) {
		case "A":
			return "En attente";
		case "E":
			return "Exécuté";
		case "R":
			return "Rejeté";
		default:
			return "Inconnu (" + codeEtat + ")";
		}
	}

	/**
	 * Retourne la couleur CSS selon l'état
	 */
	public String getCouleurEtatOrdre() {
		if (selectedOrdreVirement != null && selectedOrdreVirement.getCodEtatOvir() != null) {
			String codeEtat = selectedOrdreVirement.getCodEtatOvir().toUpperCase().trim();
			switch (codeEtat) {
			case "A":
				return "color:orange;font-weight:bold;"; // En attente = Orange
			case "E":
				return "color:green;font-weight:bold;"; // Exécuté = Vert
			case "R":
				return "color:red;font-weight:bold;"; // Rejeté = Rouge
			default:
				return "color:gray;font-weight:bold;"; // Inconnu = Gris
			}
		}
		return "color:black;";
	}

	/**
	 * Pour les bénéficiaires individuels
	 */
	public String convertirEtatBeneficiaire(String codeEtat) {
		return convertirCodeEtat(codeEtat);
	}

	/**
	 * Couleur pour les bénéficiaires individuels
	 */
	public String getCouleurEtatBeneficiaire(String codeEtat) {
		if (codeEtat == null || codeEtat.trim().isEmpty()) {
			return "color:gray;";
		}

		switch (codeEtat.toUpperCase().trim()) {
		case "A":
			return "color:orange;font-weight:bold;";
		case "E":
			return "color:green;font-weight:bold;";
		case "R":
			return "color:red;font-weight:bold;";
		default:
			return "color:gray;font-weight:bold;";
		}
	}

	public String quitterModificationOrdreVirement() {
		selectedOrdreVirement = null;
		selectedBeneficiaire = null;
		listeBeneficiaireOrdreVirement.clear();
		// listeOrdresVirements.clear();
		beneficiaireOrdreVirement = null;
		listeBeneficiairesAjoutes.clear();
		etatMessageEnregistBeneficiaire = false;
		// System.out.println("test quitter");

		return "consultationpecVirementMandatRecuBct";

	}

	public String quitterConsultationOrdreVirement() {
		selectedOrdreVirement = null;
		selectedBeneficiaire = null;
		listeBeneficiaireOrdreVirement.clear();
		// listeOrdresVirements.clear();
		beneficiaireOrdreVirement = null;
		listeBeneficiairesAjoutes.clear();
		etatMessageEnregistBeneficiaire = false;
		setStrEtatValidationTrt("0");
		setSrcRepport("");
		// System.out.println("test quitter");

		return "quitterCompManuelle";
	}

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
			// messageErreur = virementVo.getMessageValidation();

			messageErreur = "Ordre de virement rejeté avec succès.";

			return "consultationpecVirementMandatRecuBct";

		} catch (Exception e) {
			messageErreur = "Erreur lors du rejet : " + e.getMessage();
			return null;
		}
	}

	// Méthode pour effacer les données lors du changement de critère
	public void effacerDonnees() {
		selectedOrdreVirement = null;
		messageInformation = null;
		messageEnregistBeneficiaire = null;

		if (!"R".equals(critereRecherche)) {
			numRemise = null;
		}

		if (!"T".equals(critereRecherche)) {
			montantMinimal = null;
			dateExecution = null;
		}
	}

	// Méthode pour actualiser (alias de listeTousLesOrdresVirement)
	public String actualiserListe() {
		messageInformation = "";
		return listeTousLesOrdresVirement();
	}

	/**
	 * Méthode appelée lors du changement de critère de recherche Charge
	 * automatiquement tous les ordres quand "T" est sélectionné
	 */
	public String changerCritereRecherche() {
		try {

			// Réinitialiser les données communes
			selectedOrdreVirement = null;
			messageInformation = null;
			messageEnregistBeneficiaire = null;
			messageInformation = "";
			messageInformationEtat = "";
			listeOrdresVirements = new ArrayList<>();
			selectedOrdreVirement = null;

			// Actions spécifiques selon le critère sélectionné
			switch (critereRecherche) {
			case "T": // Tous les Ordres
				listeTousLesOrdresVirement();
				break;

			case "R": // Par Numéro de Remise
				// Réinitialiser seulement les autres critères
				montantMinimal = null;
				dateExecution = null;
				etatRecherche = "";
				listeOrdresVirements.clear();
				messageInformation = "Veuillez sélectionner un numéro de remise";
				break;

			case "E": // Par État
				// Réinitialiser seulement les autres critères
				numRemise = null;
				montantMinimal = null;
				dateExecution = null;
				listeOrdresVirements.clear();
				messageInformationEtat = "Veuillez sélectionner un état";
				// Réinitialiser l'état de recherche
				etatRecherche = "";
				if (getRechercheClientCtr() != null) {
					getRechercheClientCtr().resetAttributes();
				}
				break;

			case "C": // Par Contrat Client
				// Réinitialiser seulement les autres critères
				numRemise = null;
				montantMinimal = null;
				dateExecution = null;
				etatRecherche = "";
				listeOrdresVirements.clear();
				messageInformation = "Veuillez rechercher un client";
				if (getRechercheClientCtr() != null) {
					getRechercheClientCtr().resetAttributes();
				}
				break;

			default:
				// Réinitialiser tous les critères
				numRemise = null;
				montantMinimal = null;
				dateExecution = null;
				etatRecherche = "";
				listeOrdresVirements.clear();
				messageInformation = "Veuillez sélectionner un critère de recherche";
				if (getRechercheClientCtr() != null) {
					getRechercheClientCtr().resetAttributes();
				}
				break;
			}

		} catch (Exception e) {
			System.out.println("Erreur lors du changement de critère de recherche : ");
			e.printStackTrace();
			messageInformation = "Erreur lors du changement de critère : " + e.getMessage();
			messageInformationEtat = "";
		}

		return null;
	}

	// par etat
	// Propriété pour l'état de recherche
	private String etatRecherche;
	private String messageInformationEtat;

	// Getter et Setter pour etatRecherche
	public String getEtatRecherche() {
		return etatRecherche;
	}

	public void setEtatRecherche(String etatRecherche) {
		this.etatRecherche = etatRecherche;
	}

	// Getter et Setter pour messageInformationEtat
	public String getMessageInformationEtat() {
		return messageInformationEtat;
	}

	public void setMessageInformationEtat(String messageInformationEtat) {
		this.messageInformationEtat = messageInformationEtat;
	}

	// Méthode de recherche par état
	public void rechercherGlobalVirementByEtat() {
		try {

			if (etatRecherche != null && !etatRecherche.isEmpty()) {
				// Appeler votre service pour rechercher les ordres par état
				// GetOrdreVirementCentralByEtatTrt
				VirementVo virementVo = new VirementVo();
				virementVo.setEtatRecherche(getEtatRecherche());
				virementVo.setParamAgence(getParamAgence());

				VirementCmd virementCmd = new VirementCmd();

				virementVo = (VirementVo) virementCmd.getOrdreVirementCentralByEtat(virementVo);

				listeOrdresVirements = virementVo.getListeOrdresVirements();

				if (listeOrdresVirements != null && !listeOrdresVirements.isEmpty()) {
					messageInformationEtat = "Trouvé " + listeOrdresVirements.size() + " ordre(s) avec l'état: "
							+ convertirCodeEtatRecherche(etatRecherche);

					// calculerNombreTotalVirements();
				} else {
					messageInformationEtat = "Aucun ordre trouvé pour l'état: "
							+ convertirCodeEtatRecherche(etatRecherche);
					listeOrdresVirements = new ArrayList<>();
				}
			} else {
				messageInformationEtat = "Veuillez sélectionner un état";
			}
		} catch (Exception e) {
			messageInformationEtat = "Erreur lors de la recherche: " + e.getMessage();
			listeOrdresVirements = new ArrayList<>();
		}
	}

	/******************/
	/**
	 * Calcule le nombre total de virements dans tous les ordres de la liste Cette
	 * méthode additionne le nombre de virements de chaque ordre (nbrVirOvir)
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

			} else {
				System.out.println("Liste des ordres vide - Nombre total de virements = 0");
			}

		} catch (Exception e) {

			e.printStackTrace();
			nombreTotalVirements = 0;
		}
	}

	/**
	 * Calcule et retourne le montant total de tous les ordres de virement Méthode
	 * utilitaire complémentaire pour les statistiques
	 */
	private Double calculerMontantTotal() {
		Double montantTotal = 0.0;

		try {
			if (listeOrdresVirements != null && !listeOrdresVirements.isEmpty()) {
				for (OrdreVirement ordre : listeOrdresVirements) {
					if (ordre != null && ordre.getMontGlobOvir() != null) {
						montantTotal += ordre.getMontGlobOvir().doubleValue();
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Erreur lors du calcul du montant total : " + e.getMessage());
			montantTotal = 0.0;
		}

		return montantTotal;
	}

	/**
	 * Calcule toutes les statistiques en une fois Méthode optimisée qui évite de
	 * parcourir plusieurs fois la liste
	 */
	private void calculerToutesLesStatistiques() {
		nombreTotalVirements = 0;
		montantTotalOrdres = 0.0;

		try {
			if (listeOrdresVirements != null && !listeOrdresVirements.isEmpty()) {
				for (OrdreVirement ordre : listeOrdresVirements) {
					if (ordre != null) {
						// Compter les virements
						if (ordre.getNbrVirOvir() != null) {
							nombreTotalVirements += ordre.getNbrVirOvir().intValue();
						}

						// Additionner les montants
						if (ordre.getMontGlobOvir() != null) {
							montantTotalOrdres += ordre.getMontGlobOvir().doubleValue();
						}
					}
				}

				System.out.println("Statistiques calculées :");
				System.out.println("- Nombre d'ordres : " + listeOrdresVirements.size());
				System.out.println("- Nombre total de virements : " + nombreTotalVirements);
				System.out.println("- Montant total : " + montantTotalOrdres);

			} else {
				System.out.println("Liste des ordres vide - Toutes les statistiques = 0");
			}

		} catch (Exception e) {
			System.out.println("Erreur lors du calcul des statistiques : " + e.getMessage());
			e.printStackTrace();
			nombreTotalVirements = 0;
			montantTotalOrdres = 0.0;
		}
	}

	/**
	 * Convertit le code état de la base de données en libellé lisible
	 * 
	 * @param codeEtat
	 *            Code état (A, R, E)
	 * @return Libellé correspondant
	 */
	public String convertirCodeEtatRecherche(String codeEtat) {
		if (codeEtat == null || codeEtat.trim().isEmpty()) {
			return "Non défini";
		}

		switch (codeEtat.trim().toUpperCase()) {
		case "A":
			return "En Attente / Annulé";
		case "R":
			return "Rejeté";
		case "E":
			return "Exécuté";
		default:
			return "État inconnu (" + codeEtat + ")";
		}
	}

	/**
	 * Retourne la couleur CSS selon l'état pour l'affichage
	 * 
	 * @param codeEtat
	 *            Code état (A, R, E)
	 * @return Style CSS pour la couleur
	 */
	public String getCouleurEtatBeneficiaire1(String codeEtat) {
		if (codeEtat == null || codeEtat.trim().isEmpty()) {
			return "color: #666;"; // Gris pour non défini
		}

		switch (codeEtat.trim().toUpperCase()) {
		case "A":
			return "color: #FFA500; font-weight: bold;"; // Orange pour En Attente/Annulé
		case "R":
			return "color: #FF0000; font-weight: bold;"; // Rouge pour Rejeté
		case "E":
			return "color: #008000; font-weight: bold;"; // Vert pour Exécuté
		default:
			return "color: #666;"; // Gris par défaut
		}
	}

	/**
	 * Méthode de recherche par état adaptée à votre BD
	 */
	public void rechercherGlobalVirementByEtat1() {
		try {
			if (etatRecherche != null && !etatRecherche.isEmpty()) {
				VirementVo virementVo = new VirementVo();
				VirementCmd virementCmd = new VirementCmd();

				/*
				 * virementVo.setOrdreVirement(selectedOrdreVirement);
				 * virementVo.setParamAgence(getParamAgence());
				 * virementVo.setListeBeneficiaireOrdreVirement(
				 * getListeBeneficiaireOrdreVirement());
				 */

				virementVo = (VirementVo) virementCmd.getOrdreVirementCentralByEtat(virementVo);

				// Récupérer les résultats
				listeOrdresVirements = virementVo.getListeOrdresVirements();

				// Définir le message selon les résultats
				if (listeOrdresVirements != null && !listeOrdresVirements.isEmpty()) {
					messageInformationEtat = "Trouvé " + listeOrdresVirements.size() + " ordre(s) avec l'état : "
							+ convertirCodeEtat(etatRecherche);
					calculerNombreTotalVirements();
				} else {
					messageInformationEtat = "Aucun ordre trouvé pour l'état : " + convertirCodeEtat(etatRecherche);
					nombreTotalVirements = 0;
				}

			} else {
				messageInformationEtat = "Veuillez sélectionner un état";
				listeOrdresVirements = new ArrayList<>();
				nombreTotalVirements = 0;
			}

		} catch (Exception e) {
			messageInformationEtat = "Erreur lors de la recherche: " + e.getMessage();
			listeOrdresVirements = new ArrayList<>();
			nombreTotalVirements = 0;
			e.printStackTrace();
		}
	}

	/**
	 * Retourne la liste des états disponibles pour les statistiques
	 * 
	 * @return Map<Code, Libellé>
	 */
	public Map<String, String> getListeEtatsDisponibles() {
		Map<String, String> etats = new LinkedHashMap<>();
		etats.put("A", "En Attente / Annulé");
		etats.put("R", "Rejeté");
		etats.put("E", "Exécuté");
		return etats;
	}

	/**
	 * Retourne le nombre d'ordres par état (pour statistiques avancées)
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

	// ****partie execuxon *****
	/**
	 * Récupère les ordres de virement à exécuter pour le jour en cours
	 */
	private List<OrdreVirement> recupererOrdresDuJour() {
		try {
			VirementVo virementVo = new VirementVo();
			virementVo.setParamAgence(getParamAgence());

			Calendar cal = Calendar.getInstance();

			// Début du jour (00:00:00)
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Date debutJour = cal.getTime();

			// Fin du jour (23:59:59)
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			cal.set(Calendar.MILLISECOND, 999);
			Date finJour = cal.getTime();

			virementVo.setDateDebutPeriode(debutJour);
			virementVo.setDateFinPeriode(finJour);

			// Log pour debug
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			logger.info("Recherche des ordres entre " + sdf.format(debutJour) + " et " + sdf.format(finJour));

			VirementCmd virementCmd = new VirementCmd();
			virementVo = (VirementVo) virementCmd.GetOrdresVirementPourPeriode(virementVo);

			// Filtrer uniquement les ordres en attente (état 'A')
			List<OrdreVirement> ordresAExecuter = new ArrayList<OrdreVirement>();
			if (virementVo.getListeOrdresVirements() != null) {
				for (OrdreVirement ordre : virementVo.getListeOrdresVirements()) {
					if ("A".equals(ordre.getCodEtatOvir())) {
						ordresAExecuter.add(ordre);
					}
				}
			}

			logger.info("Nombre d'ordres en attente trouvés pour aujourd'hui : " + ordresAExecuter.size());

			return ordresAExecuter;

		} catch (Exception e) {
			logger.error("Erreur lors de la récupération des ordres du jour", e);
			return new ArrayList<OrdreVirement>();
		}
	}

	/**
	 * Exécution de l'ordre de virement sélectionné
	 */
	public void executerVirementDuJour(ActionEvent event) {
		System.out.println("========== Début traitement  ==========");

		messageInformation = "";
		messageErreur = "";
		List<String> messagesResultat = new ArrayList<String>();
		VirementCmd virementCmd = new VirementCmd();

		System.out.println("========== Début Exécution Ordre Virement Sélectionné ==========");

		long nbreVirementExecutes = 0;
		long nbreVirementRejetes = 0;
		long nbreVirementdecales = 0;

		try {
			// Vérifier qu'un ordre est sélectionné
			if (selectedOrdreVirement == null || selectedOrdreVirement.getOrdreVirementId() == null) {
				messageErreur = "Aucun ordre de virement sélectionné pour l'exécution";
				System.out.println("Tentative d'exécution sans ordre sélectionné");
				return;
			}
			System.out.println("etat rechercher:" + selectedOrdreVirement.getCodEtatOvir());
			// Vérifier l'état de l'ordre
			if (!"A".equals(selectedOrdreVirement.getCodEtatOvir())) {
				messageErreur = "Cet ordre n'est pas en état 'En attente'. État actuel : "
						+ convertirCodeEtat(selectedOrdreVirement.getCodEtatOvir());
				System.out.println("Ordre n° " + selectedOrdreVirement.getOrdreVirementId().getNumOvirOvir()
						+ " n'est pas en état 'A' : " + selectedOrdreVirement.getCodEtatOvir());
				return;
			}

			// Vérifier qu'il y a des bénéficiaires
			if (selectedOrdreVirement.getBeneficiaireOrdreVirements() == null
					|| selectedOrdreVirement.getBeneficiaireOrdreVirements().isEmpty()) {
				messageErreur = "Cet ordre ne contient aucun bénéficiaire à traiter";
				System.out.println("Ordre n° " + selectedOrdreVirement.getOrdreVirementId().getNumOvirOvir()
						+ " sans bénéficiaires");
				return;
			}

			System.out.println(
					"Traitement de l'ordre n° : " + selectedOrdreVirement.getOrdreVirementId().getNumOvirOvir());
			System.out.println(
					"Nombre de bénéficiaires : " + selectedOrdreVirement.getBeneficiaireOrdreVirements().size());
			System.out.println("Montant global : " + selectedOrdreVirement.getMontGlobOvir());

			// Préparer le VO pour l'exécution
			VirementVo virementVo = new VirementVo();
			virementVo.setOrdreVirement(selectedOrdreVirement);
			virementVo.setParamAgence(getParamAgence());

			System.out.println(">>> JUSTE AVANT L'APPEL ExecutionOrdreVirementCentralDuJour");
			System.out.println(">>> VirementVo préparé - Ordre : "
					+ virementVo.getOrdreVirement().getOrdreVirementId().getNumOvirOvir());
			System.out.println(">>> ParamAgence : " + virementVo.getParamAgence().getNumMatrUser());

			// Appeler le traitement d'exécution
			virementVo = (VirementVo) virementCmd.ExecutionOrdreVirementCentralDuJour(virementVo);

			System.out.println(">>> JUSTE APRÈS L'APPEL ExecutionOrdreVirementCentralDuJour");

			// Récupérer les statistiques
			nbreVirementExecutes = virementVo.getNbreVirementExecutes();
			nbreVirementRejetes = virementVo.getNbreVirementRejetes();
			nbreVirementdecales = virementVo.getNbreVirementdecales();

			System.out.println("Résultats - Exécutés: " + nbreVirementExecutes + ", Rejetés: " + nbreVirementRejetes
					+ ", Décalés: " + nbreVirementdecales);

			// Récupérer les messages d'erreur éventuels
			if (virementVo.getListeMsgValidation() != null && virementVo.getListeMsgValidation().size() > 0) {
				messagesResultat.addAll(virementVo.getListeMsgValidation());
				logger.warn("Des erreurs ont été détectées lors de l'exécution");
			}

			// Recharger l'ordre pour avoir les états à jour
			ISearchEngine searchEngine = (ISearchEngine) Context.getInstance().getSpringContext()
					.getBean("searchEngine");
			selectedOrdreVirement = (OrdreVirement) searchEngine.get(OrdreVirement.class,
					selectedOrdreVirement.getOrdreVirementId());

			// Rafraîchir la liste des bénéficiaires avec leurs nouveaux états
			if (selectedOrdreVirement.getBeneficiaireOrdreVirements() != null) {
				listeBeneficiaireOrdreVirement.clear();
				listeBeneficiaireOrdreVirement.addAll(selectedOrdreVirement.getBeneficiaireOrdreVirements());
			}

			// Génération du message récapitulatif
			StringBuilder recap = new StringBuilder();
			recap.append("===== EXÉCUTION DE L'ORDRE TERMINÉE =====\n");
			recap.append("Numéro d'ordre : ").append(selectedOrdreVirement.getOrdreVirementId().getNumOvirOvir())
					.append("\n");
			recap.append("Date : ").append(new SimpleDateFormat("dd/MM/yyyy").format(new Date())).append("\n");
			recap.append("État de l'ordre : ").append(convertirCodeEtat(selectedOrdreVirement.getCodEtatOvir()))
					.append("\n\n");

			recap.append("===== STATISTIQUES =====\n");
			recap.append("Virements exécutés : ").append(nbreVirementExecutes).append("\n");

			if (nbreVirementRejetes > 0) {
				recap.append("Virements rejetés : ").append(nbreVirementRejetes).append("\n");
			}

			if (nbreVirementdecales > 0) {
				recap.append("Virements décalés : ").append(nbreVirementdecales).append("\n");
			}

			// Résumé du traitement
			long totalBeneficiaires = selectedOrdreVirement.getNbrVirOvir();
			recap.append("\nTotal bénéficiaires : ").append(totalBeneficiaires).append("\n");

			if (messagesResultat.size() > 0) {
				recap.append("\n===== DÉTAILS DES ERREURS =====\n");
				for (String msg : messagesResultat) {
					recap.append("- ").append(msg).append("\n");
				}
			} else {
				recap.append("\n Exécution terminée sans erreur");
			}

			messageInformation = recap.toString();

			// Message de succès dans les logs
			logger.info("========== Fin Exécution Ordre Virement - Succès ==========");

		} catch (Exception e) {
			System.out.println("========== ERREUR CRITIQUE ==========");
			System.out.println("Erreur critique lors de l'exécution de l'ordre");
			System.out.println("Type d'exception : " + e.getClass().getName());
			System.out.println("Message : " + e.getMessage());
			System.out.println("Stack trace complète :");
			e.printStackTrace(); // IMPORTANT : affiche toute la stack trace

			// Vérifier la cause racine
			Throwable cause = e.getCause();
			if (cause != null) {
				System.out.println("Cause racine : " + cause.getClass().getName());
				System.out.println("Message cause : " + cause.getMessage());
				cause.printStackTrace();
			}

			messageErreur = "Erreur critique lors de l'exécution : " + e.getMessage();
			messageInformation = "L'exécution de l'ordre a échoué. Consultez les logs pour plus de détails.";

			addMessage(null, "Erreur d'exécution", "Une erreur s'est produite : " + e.getMessage(),
					FacesMessage.SEVERITY_ERROR);
		}
	}

	/**
	 * Bouton d'exécution - Vérification avant exécution
	 */
	public String executerOrdreSelectionne() {
		try {
			if (selectedOrdreVirement == null) {
				messageErreur = "Veuillez sélectionner un ordre de virement à exécuter";
				/*
				 * addMessage(null, "Attention", "Aucun ordre sélectionné",
				 * FacesMessage.SEVERITY_WARN);
				 */
				return null;
			}

			// Appeler la méthode d'exécution
			executerVirementDuJour(null);

			// Rafraîchir la liste si on est sur la page de consultation
			if ("A".equals(critereRecherche)) {
				// Recharger tous les ordres
				listeTousLesOrdresVirement();
			}

			return null;

		} catch (Exception e) {
			System.out.println("Erreur dans executerOrdreSelectionne");
			messageErreur = "Erreur : " + e.getMessage();
			return null;
		}
	}

	/**
	 * Vérifier si l'ordre sélectionné peut être exécuté
	 */
	public boolean isPeutExecuterOrdre() {
		try {
			if (selectedOrdreVirement == null) {
				return false;
			}

			// Vérifier l'état
			if (!"A".equals(selectedOrdreVirement.getCodEtatOvir())) {
				return false;
			}

			// Vérifier qu'il y a des bénéficiaires
			if (selectedOrdreVirement.getBeneficiaireOrdreVirements() == null
					|| selectedOrdreVirement.getBeneficiaireOrdreVirements().isEmpty()) {
				return false;
			}

			return true;

		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Message informatif sur l'état de l'exécution
	 */
	public String getMessageExecutionPossible() {
		if (selectedOrdreVirement == null) {
			return "Aucun ordre sélectionné";
		}

		if (!"A".equals(selectedOrdreVirement.getCodEtatOvir())) {
			return "Cet ordre n'est pas en état 'En attente'";
		}

		if (selectedOrdreVirement.getBeneficiaireOrdreVirements() == null
				|| selectedOrdreVirement.getBeneficiaireOrdreVirements().isEmpty()) {
			return "Cet ordre ne contient aucun bénéficiaire";
		}

		return "Prêt pour l'exécution";
	}

	// Variable à ajouter dans votre contrôleur
	private boolean modeModification = false;

	// Getter et Setter
	public boolean isModeModification() {
		return modeModification;
	}

	public void setModeModification(boolean modeModification) {
		this.modeModification = modeModification;
	}

	// Votre fonction editerBeneficiaireVirement() modifiée
	public void editerBeneficiaireVirement() {
		try {
			if (selectedBeneficiaire != null) {
				System.out.println("benif" + selectedBeneficiaire.getNumRibbBenf());
				// Charger les données du bénéficiaire sélectionné dans le formulaire
				if (!selectedBeneficiaire.getNumRibbBenf().isEmpty()) {
					setCompteBenificiaire(selectedBeneficiaire.getNumRibbBenf());
					// Adapter selon vos getters/setters exacts
					setRibBeneficiaire(selectedBeneficiaire.getNumRibbBenf());
				}
				benificiaire = selectedBeneficiaire.getNomPrnbBenf();
				setNomBeneficiaire(selectedBeneficiaire.getNomPrnbBenf());

				montantVirement = selectedBeneficiaire.getMontOperBenf() + "";
				setMontantBeneficiaire(selectedBeneficiaire.getMontOperBenf());

				// Réinitialiser les messages
				messageEnregistBenefVirement = "";
				msgValidatorCompte = "";
				messageRIBexistant = "";
				setMessageEnregistBeneficiaire("");
				setMsgValidatorRib("");

				// Activer le mode modification
				setModeModification(true);

				// System.out.println("Mode modification activé pour bénéficiaire: " +
				// selectedBeneficiaire.getBeneficiaireOrdreVirementId().getNumDetBenf());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Votre fonction modifierBeneficiaire() adaptée (en gardant votre logique)
	// Version corrigée de votre méthode avec vérifications null
	public void modifierBeneficiaireBenf() {
		System.out.println("=== DEBUT MODIFICATION ===");

		try {
			// Réinitialiser les messages
			messageEnregistBeneficiaire = "";
			etatMessageEnregistBeneficiaire = false;

			// Vérifications de base
			if (selectedBeneficiaire == null) {
				System.out.println("ERREUR: Aucun bénéficiaire sélectionné");
				messageEnregistBeneficiaire = "Aucun bénéficiaire sélectionné pour modification";
				etatMessageEnregistBeneficiaire = true;
				return;
			}

			String msgValidator = getMsgValidatorRib();
			if (msgValidator != null && msgValidator.length() > 0) {
				System.out.println("ERREUR: Validation RIB échouée - " + msgValidator);
				messageEnregistBeneficiaire = msgValidator;
				etatMessageEnregistBeneficiaire = true;
				return;
			}

			// Récupérer les données saisies
			String ribBenef = getRibBeneficiaire();
			String nomBenef = getNomBeneficiaire();
			long montantBenef = getMontantBeneficiaire();

			// Validation simple des données
			if (ribBenef == null || ribBenef.trim().isEmpty()) {
				System.out.println("ERREUR: RIB vide");
				messageEnregistBeneficiaire = "Le RIB est obligatoire";
				etatMessageEnregistBeneficiaire = true;
				return;
			}

			if (nomBenef == null || nomBenef.trim().isEmpty()) {
				System.out.println("ERREUR: Nom vide");
				messageEnregistBeneficiaire = "Le nom est obligatoire";
				etatMessageEnregistBeneficiaire = true;
				return;
			}

			if (montantBenef <= 0) {
				System.out.println("ERREUR: Montant invalide");
				messageEnregistBeneficiaire = "Le montant doit être supérieur à zéro";
				etatMessageEnregistBeneficiaire = true;
				return;
			}

			System.out.println("Données OK - RIB: " + ribBenef + ", Nom: " + nomBenef + ", Montant: " + montantBenef);

			// Créer le nouveau bénéficiaire
			BeneficiaireOrdreVirement nouveauBenef = new BeneficiaireOrdreVirement();
			nouveauBenef.setNumRibbBenf(ribBenef);
			nouveauBenef.setNomPrnbBenf(nomBenef);
			nouveauBenef.setMontOperBenf(montantBenef);

			// Copier les propriétés de l'ancien bénéficiaire
			if (selectedBeneficiaire.getCodEtatDov() != null) {
				nouveauBenef.setCodEtatDov(selectedBeneficiaire.getCodEtatDov());
			}

			if (selectedBeneficiaire.getOrdreVirement() != null) {
				nouveauBenef.setOrdreVirement(selectedBeneficiaire.getOrdreVirement());
			}

			if (selectedBeneficiaire.getBeneficiaireOrdreVirementId() != null) {
				nouveauBenef.setBeneficiaireOrdreVirementId(selectedBeneficiaire.getBeneficiaireOrdreVirementId());
			}

			// Définir la structure (si RIB valide)
			if (ribBenef.length() >= 8) {
				try {
					Structure strcBenef = new Structure();
					String codeStrc = ribBenef.substring(5, 8);
					strcBenef.setCodStrcStrc(Long.valueOf(codeStrc));
					nouveauBenef.setStructureBenef(strcBenef);
					System.out.println("Structure définie: " + codeStrc);
				} catch (Exception e) {
					System.out.println("ERREUR Structure: " + e.getMessage());
					messageEnregistBeneficiaire = "Format de RIB invalide";
					etatMessageEnregistBeneficiaire = true;
					return;
				}
			}

			// Vérifier que ce n'est pas le même compte que le débiteur
			if (selectedBeneficiaire.getOrdreVirement() != null && ribBenef.length() >= 18) {
				String compteDebit = selectedBeneficiaire.getOrdreVirement().getCompteDebitOvir();
				String compteBenif = ribBenef.substring(5, 18);

				if (compteDebit != null && compteDebit.contains(compteBenif)) {
					System.out.println("ERREUR: Même compte débiteur");
					messageEnregistBeneficiaire = "Impossible d'effectuer un virement vers le même compte débiteur";
					etatMessageEnregistBeneficiaire = true;
					return;
				}
			}

			// Vérifier que le RIB n'existe pas déjà (sauf pour le bénéficiaire en cours)
			if (listeBeneficiaireOrdreVirement != null) {
				for (BeneficiaireOrdreVirement benef : listeBeneficiaireOrdreVirement) {
					if (benef != selectedBeneficiaire && benef.getNumRibbBenf() != null
							&& benef.getNumRibbBenf().equals(ribBenef)) {
						System.out.println("ERREUR: RIB déjà existant");
						messageEnregistBeneficiaire = "Ce RIB existe déjà dans la liste";
						etatMessageEnregistBeneficiaire = true;
						return;
					}
				}
			}

			// Remplacer le bénéficiaire dans la liste
			int index = listeBeneficiaireOrdreVirement.indexOf(selectedBeneficiaire);
			if (index >= 0) {
				// Ajouter l'ancien à la liste des supprimés si il a un ID
				if (selectedBeneficiaire.getBeneficiaireOrdreVirementId() != null) {
					if (listeBeneficiaireOrdreVirementSupprimes == null) {
						listeBeneficiaireOrdreVirementSupprimes = new ArrayList<BeneficiaireOrdreVirement>();
					}
					listeBeneficiaireOrdreVirementSupprimes.add(selectedBeneficiaire);
					System.out.println("Ancien bénéficiaire ajouté aux supprimés");
				}

				// Remplacer dans la liste
				listeBeneficiaireOrdreVirement.set(index, nouveauBenef);
				System.out.println("Bénéficiaire remplacé à l'index: " + index);

				// Recalculer les totaux
				try {
					calculerTotauxGlobaux();
					System.out.println("Totaux recalculés");
				} catch (Exception e) {
					System.out.println("ATTENTION: Erreur calcul totaux - " + e.getMessage());
				}

				// Sortir du mode modification
				annulerModeModification();
				System.out.println("MODIFICATION REUSSIE");

			} else {
				System.out.println("ERREUR: Bénéficiaire introuvable dans la liste");
				messageEnregistBeneficiaire = "Erreur: bénéficiaire introuvable";
				etatMessageEnregistBeneficiaire = true;
			}

		} catch (Exception e) {
			System.out.println("EXCEPTION: " + e.getMessage());
			e.printStackTrace();
			messageEnregistBeneficiaire = "Erreur lors de la modification: " + e.getMessage();
			etatMessageEnregistBeneficiaire = true;

			// Essayer d'annuler le mode modification
			try {
				annulerModeModification();
			} catch (Exception ex) {
				System.out.println("ERREUR annulation: " + ex.getMessage());
			}
		}

		System.out.println("=== FIN MODIFICATION ===");
	}

	// Méthode helper pour vérifier et initialiser les listes si nécessaire
	private void verifierEtInitialiserListes() {
		if (listeBeneficiaireOrdreVirement == null) {
			listeBeneficiaireOrdreVirement = new ArrayList<BeneficiaireOrdreVirement>();
			System.out.println("listeBeneficiaireOrdreVirement initialisée");
		}

		if (listeBeneficiaireOrdreVirementSupprimes == null) {
			listeBeneficiaireOrdreVirementSupprimes = new ArrayList<BeneficiaireOrdreVirement>();
			System.out.println("listeBeneficiaireOrdreVirementSupprimes initialisée");
		}
	}

	// Version sécurisée de annulerModeModification
	public void annulerModeModification() {
		try {
			System.out.println("=== DEBUT annulerModeModification ===");

			// Vider les champs du formulaire avec vérifications null
			try {
				setCompteBenificiaire("");
			} catch (Exception e) {
				System.out.println("Erreur setCompteBenificiaire: " + e.getMessage());
			}

			try {
				setRibBeneficiaire("");
			} catch (Exception e) {
				System.out.println("Erreur setRibBeneficiaire: " + e.getMessage());
			}

			benificiaire = "";

			try {
				setNomBeneficiaire("");
			} catch (Exception e) {
				System.out.println("Erreur setNomBeneficiaire: " + e.getMessage());
			}

			montantVirement = "";

			try {
				setMontantBeneficiaire(0);
			} catch (Exception e) {
				System.out.println("Erreur setMontantBeneficiaire: " + e.getMessage());
			}

			// Réinitialiser les messages
			if (messageEnregistBenefVirement == null)
				messageEnregistBenefVirement = "";
			messageEnregistBenefVirement = "";

			if (msgValidatorCompte == null)
				msgValidatorCompte = "";
			msgValidatorCompte = "";

			if (messageRIBexistant == null)
				messageRIBexistant = "";
			messageRIBexistant = "";

			try {
				setMessageEnregistBeneficiaire("");
			} catch (Exception e) {
				System.out.println("Erreur setMessageEnregistBeneficiaire: " + e.getMessage());
			}

			try {
				setMsgValidatorRib("");
			} catch (Exception e) {
				System.out.println("Erreur setMsgValidatorRib: " + e.getMessage());
			}
			// Désactiver le mode modification
			modeModification = false;

			// Réinitialiser la sélection
			selectedBeneficiaire = null;

			System.out.println("Mode modification annulé - retour au mode ajout");
			System.out.println("=== FIN annulerModeModification ===");
			viderFormulaire();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("EXCEPTION dans annulerModeModification: " + e.getMessage());
		}
	}

	// Fonction pour modification directe avec changement visible immédiatement
	// Version corrigée pour récupérer les vraies valeurs du formulaire
	// Version corrigée pour récupérer les vraies valeurs du formulaire
	public void modifierBeneficiaireEnMemoire() {
		System.out.println("=== MODIFICATION EN MEMOIRE ===");

		try {
			// Vérifications minimales
			if (selectedBeneficiaire == null) {
				System.out.println("ERREUR: Aucun bénéficiaire sélectionné");
				messageEnregistBeneficiaire = "Aucun bénéficiaire sélectionné";
				etatMessageEnregistBeneficiaire = true;
				return;
			}

			System.out.println("selectedBeneficiaire: " + selectedBeneficiaire.getNumRibbBenf());

			if (listeBeneficiaireOrdreVirement == null) {
				System.out.println("ERREUR: Liste des bénéficiaires null");
				messageEnregistBeneficiaire = "Liste non initialisée";
				etatMessageEnregistBeneficiaire = true;
				return;
			}

			// Trouver l'index du bénéficiaire à modifier
			int index = listeBeneficiaireOrdreVirement.indexOf(selectedBeneficiaire);
			if (index < 0) {
				System.out.println("ERREUR: Bénéficiaire introuvable dans la liste");
				messageEnregistBeneficiaire = "Bénéficiaire introuvable";
				etatMessageEnregistBeneficiaire = true;
				return;
			}

			System.out.println("Bénéficiaire trouvé à l'index: " + index);

			// === RECUPERER LES VALEURS DU FORMULAIRE ===
			// IMPORTANT: Utiliser les variables du formulaire directement, pas les getters
			String nouveauRib = compteBenificiaire; // Variable directe du formulaire
			String nouveauNom = benificiaire; // Variable directe du formulaire
			String nouveauMontantStr = montantVirement; // Variable directe du formulaire

			System.out.println("=== VALEURS DIRECTES DU FORMULAIRE ===");
			System.out.println("compteBenificiaire (RIB) = " + nouveauRib);
			System.out.println("benificiaire (Nom) = " + nouveauNom);
			System.out.println("montantVirement (Montant String) = " + nouveauMontantStr);

			// Convertir le montant String en long
			String nouveauMontant = "";
			try {
				if (nouveauMontantStr != null && !nouveauMontantStr.trim().isEmpty()) {
					// Nettoyer le montant (enlever espaces et remplacer virgule par point)
					String montantClean = nouveauMontantStr.replace(" ", "").replace(",", ".");
					// Enlever les points de milliers s'il y en a
					if (montantClean.contains(".")) {
						// Si c'est un format comme "1.000,50" -> "1000.50"
						String[] parts = montantClean.split("\\.");
						if (parts.length > 2) {
							// Format avec milliers : 1.000.500 -> 1000500
							montantClean = montantClean.replace(".", "");
						}
					}
					nouveauMontant = montantClean;
					System.out.println("Montant converti: " + nouveauMontant);
				} else {
					System.out.println("ERREUR: montantVirement est vide");
					messageEnregistBeneficiaire = "Le montant est obligatoire";
					etatMessageEnregistBeneficiaire = true;
					return;
				}
			} catch (Exception e) {
				System.out.println("ERREUR conversion montant: " + e.getMessage());
				messageEnregistBeneficiaire = "Format de montant invalide: " + nouveauMontantStr;
				etatMessageEnregistBeneficiaire = true;
				return;
			}

			// Validation des données
			if (nouveauRib == null || nouveauRib.trim().isEmpty()) {
				System.out.println("ERREUR: RIB vide");
				messageEnregistBeneficiaire = "Le RIB est obligatoire";
				etatMessageEnregistBeneficiaire = true;
				return;
			}

			if (nouveauNom == null || nouveauNom.trim().isEmpty()) {
				System.out.println("ERREUR: Nom vide");
				messageEnregistBeneficiaire = "Le nom est obligatoire";
				etatMessageEnregistBeneficiaire = true;
				return;
			}
			double montant = Double.parseDouble(nouveauMontant);
			if (montant <= 0) {
				System.out.println("ERREUR: Montant invalide: " + nouveauMontant);
				messageEnregistBeneficiaire = "Le montant doit être supérieur à zéro";
				etatMessageEnregistBeneficiaire = true;
				return;
			}

			System.out.println("=== VALEURS FINALES POUR MODIFICATION ===");
			System.out.println("RIB final: " + nouveauRib);
			System.out.println("Nom final: " + nouveauNom);
			System.out.println("Montant final: " + nouveauMontant);

			// Modifier directement les propriétés du bénéficiaire dans la liste
			BeneficiaireOrdreVirement benefAModifier = listeBeneficiaireOrdreVirement.get(index);

			System.out.println("=== AVANT MODIFICATION ===");
			System.out.println("RIB: " + benefAModifier.getNumRibbBenf());
			System.out.println("Nom: " + benefAModifier.getNomPrnbBenf());
			System.out.println("Montant: " + benefAModifier.getMontOperBenf());

			// Appliquer les modifications directement
			benefAModifier.setNumRibbBenf(nouveauRib);
			benefAModifier.setNomPrnbBenf(nouveauNom);
			benefAModifier.setMontOperBenf(Long.valueOf(nouveauMontant.replace(".", "").replace(" ", "").trim()));

			System.out.println("=== APRES MODIFICATION ===");
			System.out.println("RIB: " + benefAModifier.getNumRibbBenf());
			System.out.println("Nom: " + benefAModifier.getNomPrnbBenf());
			System.out.println("Montant: " + benefAModifier.getMontOperBenf());

			// Vérifier que la modification a bien eu lieu

			if (benefAModifier.getMontOperBenf() == montant) {
				System.out.println("SUCCES: Montant correctement modifié");
			} else {
				System.out.println("ERREUR: Montant pas modifié!");
			}

			// Recalculer les totaux
			try {
				calculerTotauxGlobaux();
				System.out.println("Totaux recalculés");
			} catch (Exception e) {
				System.out.println("Pas de recalcul des totaux: " + e.getMessage());
			}

			// Message de succès
			messageEnregistBeneficiaire = "Modification effectuée avec succès";
			etatMessageEnregistBeneficiaire = false;

			// Sortir du mode modification
			modeModification = false;
			selectedBeneficiaire = null;

			// Vider le formulaire
			viderFormulaire();

			System.out.println("MODIFICATION TERMINEE AVEC SUCCES");

		} catch (Exception e) {
			System.out.println("ERREUR: " + e.getMessage());
			e.printStackTrace();
			messageEnregistBeneficiaire = "Erreur lors de la modification: " + e.getMessage();
			etatMessageEnregistBeneficiaire = true;
		}

		System.out.println("=== FIN MODIFICATION ===");
	}

	// Fonction helper pour vider le formulaire
	public void viderFormulaire() {
		try {
			// Vider les champs du formulaire
			setRibBeneficiaire("");
			setNomBeneficiaire("");
			setMontantBeneficiaire(0);

			// Vider aussi les variables d'affichage
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
			creerNouveauBenefVirement();

			System.out.println("Formulaire complètement vidé");
		} catch (Exception e) {
			System.out.println("Erreur lors du vidage formulaire: " + e.getMessage());
		}
	}

	public String reinitialiserTout() {
		try {
			// Réinitialiser le formulaire
			viderFormulaire();

			// Réinitialiser la liste des bénéficiaires
			if (listeBeneficiaireOrdreVirement != null) {
				listeBeneficiaireOrdreVirement.clear();
			}

			// Réinitialiser les totaux
			nombreSaisie = 0L;
			// montantSaisie = new BigDecimal("0.000");

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
			viderFormulaire();
			System.out.println(" Réinitialisation complète effectuée avec succès");

			return null;

		} catch (Exception e) {
			System.out.println(" Erreur dans reinitialiserTout : " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public boolean isLimiteAtteinte() {
		return listeBeneficiaireOrdreVirement != null
				&& listeBeneficiaireOrdreVirement.size() >= LIMITE_MAX_BENEFICIAIRES;
	}

	public int getNombrePlacesRestantes() {
		if (listeBeneficiaireOrdreVirement == null) {
			return LIMITE_MAX_BENEFICIAIRES;
		}
		return LIMITE_MAX_BENEFICIAIRES - listeBeneficiaireOrdreVirement.size();
	}

	public String annulerSaisieBeneficiaire() {
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

			// OU utilisez valueOf() pour être explicite
			nombreSaisie = Long.valueOf(0);

			// Désactiver le bouton de validation
			etatCmdValiderVirement = false;

			System.out.println("Formulaire et liste réinitialisés avec succès");
			viderFormulaire();
			return null;

		} catch (Exception e) {
			System.out.println(" Erreur dans annulerSaisieBeneficiaire : " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public List<String> getListNumCompteInterne() {
		try {
			VirementVo virementVo = new VirementVo();
			virementVo.setParamAgence(getParamAgence());
			VirementCmd virementCmd = new VirementCmd();

			if (getParamAgence() != null) {
				virementVo.setCodStrcStrc(getParamAgence().getCodStrcStrc());
			}

			System.out.println("Début récupération des comptes internes");

			virementVo = (VirementVo) virementCmd.GetListNumCompteInterneCentraleTrt(virementVo);

			System.out.println("Liste size: " + virementVo.getListNumComptes().size());

			if (virementVo.getListNumComptes() != null && !virementVo.getListNumComptes().isEmpty()) {
				// Les comptes sont déjà formatés (codStrc + codPrd + numCcpt) dans le
				// traitement
				for (String compteComplet : virementVo.getListNumComptes()) {
					if (compteComplet != null && !compteComplet.trim().isEmpty()) {
						listNumCompteInterne.add(compteComplet);
					}
				}

				// Optionnel : Trier la liste par ordre croissant
				// Collections.sort(listNumCompteInterne);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Erreur lors de la récupération des numéros de comptes internes", e);
		}
		return listNumCompteInterne;
	}

	public void setParametersJasper(HashMap<String, Object> parametersJasper) {
		this.parametersJasper = parametersJasper;
	}
	
	

}