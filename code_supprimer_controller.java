# CODE SUPPRIMÉ DU FICHIER ORIGINAL
# PecVirementCentralCtr.java

Ce document contient tout le code qui a été supprimé ou modifié lors de la réorganisation.

================================================================================
## 1. IMPORTS EN DOUBLE SUPPRIMÉS (lignes 66-69)
================================================================================

```java
// Doublons supprimés:
import java.util.Date;              // Déjà importé ligne 10
import javax.faces.event.ActionEvent; // Déjà importé ligne 67
import javax.faces.context.FacesContext; // Déjà importé ligne 21
import javax.faces.component.UIComponent; // Déjà importé ligne 20
```

================================================================================
## 2. VARIABLES DÉCLARÉES EN DOUBLE SUPPRIMÉES
================================================================================

```java
// Variables déclarées 2 fois dans le code original:
private String ribBenificiaire;      // Ligne 106 ET ligne 129
private String ribBeneficiaire;      // Variable similaire avec orthographe différente
private boolean etatBenficiaire;     // Ligne 107 ET ligne 124
private boolean etatBeneficiaire;    // Variable similaire avec orthographe différente
private String messageEnregistBeneficiaire = ""; // Ligne 134 ET ligne 160
```

================================================================================
## 3. MÉTHODES MÉTIER COMPLÈTES CONSERVÉES MAIS RÉORGANISÉES
================================================================================

### 3.1 Méthode ajouterBeneficiaireVirement() COMPLÈTE
```java
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
```

### 3.2 Méthode editerBeneficiaireVirement1()
```java
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
```

### 3.3 Méthode supprimerBeneficiaireVirement() - Version originale
```java
public void supprimerBeneficiaireVirement() {
    try {
        if (selectedBeneficiaire != null) {
            listeBeneficiaireOrdreVirement.remove(selectedBeneficiaire);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

================================================================================
## 4. MÉTHODES DE GESTION DE SÉQUENCE ET VALIDATION
================================================================================

### 4.1 getSeqAgence()
```java
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
```

### 4.2 validerVirementMandatBct()
```java
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
```

### 4.3 imprimerVirementMandatBct()
```java
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
        
        // ... (le reste du code d'impression - lignes 477-762 du fichier original)
    } catch (Exception e) {
        logger.error("Erreur impression", e);
    }
}
```

================================================================================
## 5. MÉTHODES DE VÉRIFICATION RIB
================================================================================

### 5.1 verifierCompte() - MÉTHODE COMPLÈTE
```java
public void verifierCompte() {
    try {
        String strCodeBanque = "";
        String strCodeAgenceBanque = "";
        String strCompte = "";
        String strCle = "";
        String structure = "";
        String produit = "";
        String compteCCpt = "";

        boolean exist = false;
        boolean verifierLiaisonCompte = false;
        VirementVo virementVo = new VirementVo();

        if (compteBenificiaire != null && compteBenificiaire.length() == 20) {

            setRibBenificiaire(compteBenificiaire);

            strCodeBanque = compteBenificiaire.substring(0, 2);
            strCodeAgenceBanque = compteBenificiaire.substring(2, 5);
            strCompte = compteBenificiaire.substring(5, 18);
            strCle = compteBenificiaire.substring(18, 20);

            virementVo.setStrCodbanque(strCodeBanque);
            virementVo.setStrCodAgenceBanque(strCodeAgenceBanque);
            virementVo.setStrCompte(strCompte);
            virementVo.setStrCle(strCle);
            virementVo.setStrPetitRib(strCodeBanque + strCodeAgenceBanque + strCompte);
            virementVo.setVerifier(exist);

            VirementCmd virementCmd = new VirementCmd();
            virementVo = (VirementVo) virementCmd.verifierRib(virementVo);

            exist = virementVo.isVerifier();

            if (exist == false) {
                etatRIBCorrecte = false;
                msgValidatorCompte = " RIB incorrecte ";

            } else if (strCodeBanque.equals("03")) {
                ContratCpt cpt = new ContratCpt();
                ContratCptId contratCptId = new ContratCptId();
                structure = getRibBenificiaire().substring(5, 8);
                produit = getRibBenificiaire().substring(8, 12);
                compteCCpt = getRibBenificiaire().substring(12, 18);

                contratCptId.setCodStrcStrc(new Long(structure));
                contratCptId.setCodPrdPrd(new Long(produit));
                contratCptId.setNumCcptCcpt(new Long(compteCCpt));

                GetDetailContratCmd getDetailContratCmd = new GetDetailContratCmd();
                cpt = (ContratCpt) getDetailContratCmd.execute(contratCptId);

                if (cpt != null && cpt.getContratCptId() != null) {

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
                        i = 0;
                        while (boolRibBenfiEnDinarsConvertible == false
                                && i < Constants.listCompteEnDinarsConvertibles.length) {
                            if (new Long(produit).longValue() == Constants.listCompteEnDinarsConvertibles[i]
                                    .longValue()) {
                                boolRibBenfiEnDinarsConvertible = true;
                            }
                            i++;
                        }

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

                    if (verifierLiaisonCompte == false) {
                        String numCompteBenif = getRibBenificiaire().substring(5, 18);

                        if (cpt.getCodEtatCcpt().equals("R")) {
                            etatRIBCorrecte = false;
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
```

### 5.2 verifierRib()
```java
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

                GetDetailContratCmd getDetailContratCmd = new GetDetailContratCmd();
                cpt = (ContratCpt) getDetailContratCmd.execute(contratCptId);

                if (cpt != null && cpt.getContratCptId() != null) {

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
                        i = 0;
                        while (boolRibBenfiEnDinarsConvertible == false
                                && i < Constants.listCompteEnDinarsConvertibles.length) {
                            if (new Long(produit).longValue() == Constants.listCompteEnDinarsConvertibles[i]
                                    .longValue()) {
                                boolRibBenfiEnDinarsConvertible = true;
                            }
                            i++;
                        }

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

                    if (verifierLiaisonCompte == false) {
                        String numCompteBenif = getRibBenificiaire().substring(5, 18);

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
```

### 5.3 calculerRIB()
```java
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
```

================================================================================
## 6. MÉTHODES DE RECHERCHE ET LISTES
================================================================================

### 6.1 Comparator pour le tri des numéros de remise
```java
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
```

### 6.2 getListNumRemise()
```java
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
```

### 6.3 rechercherGlobalVirementByRemise()
```java
public String rechercherGlobalVirementByRemise() {
    try {
        // Réinitialisation des listes et messages
        listeOrdresVirements.clear();
        listeBeneficiaires.clear();
        listeBeneficiairesAjoutes.clear();
        messageEnregistBeneficiaire = "";
        messageInformation = "";
        montantVirement = "";
        selectedOrdreVirement = null;

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
```

================================================================================
## 7. MÉTHODES TEMPORAIRES/TEST (À SUPPRIMER EN PRODUCTION)
================================================================================

```java
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
```

================================================================================
## 8. MÉTHODES D'EXÉCUTION DES VIREMENTS
================================================================================

### 8.1 executerVirementDuJour()
```java
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
        e.printStackTrace();

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
```

================================================================================
## 9. GETTERS AVEC LOGIQUE MÉTIER
================================================================================

```java
// Getters calculés automatiquement

public Long getNombreSaisie() {
    nombreSaisie = Long.valueOf(listeBeneficiaireOrdreVirement.size());
    return nombreSaisie;
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
```

================================================================================
## 10. RÉSUMÉ DES SUPPRESSIONS
================================================================================

### Éléments supprimés:
1. Imports en double (4 lignes)
2. Déclarations de variables en double (5 variables)
3. System.out.println() remplacés par logger (environ 100+ occurrences)
4. Commentaires de code mort (plusieurs sections)
5. Variables temporaires de test (données de "bilel")
6. Constantes renommées pour suivre les conventions (COD_PRD_VIREMENT_PONCTUEL, etc.)

### Éléments conservés mais réorganisés:
1. Toutes les méthodes métier importantes
2. Toutes les validations RIB
3. Toutes les méthodes de recherche et d'exécution
4. Tous les getters/setters

### Total de lignes:
- Fichier original: 3917 lignes
- Fichier réorganisé: ~1700 lignes
- Réduction: ~56% (grâce à l'organisation, suppression des doublons et code mort)
```