package abstraction.eq5Transformateur2;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;


import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.general.Variable;

public class Transformateur2AcheteurCCadre extends Transformateur2MasseSalariale implements IAcheteurContratCadre {
	protected SuperviseurVentesContratCadre supCC;
	private List<ExemplaireContratCadre> contratsEnCours;
	private List<ExemplaireContratCadre> contratsTermines;
	protected Journal journalCC;
	private HashMap<IVendeurContratCadre, Integer> BlackListVendeur;
	private int Etapenego; //ajout d'un compteur de tours de négociation 
	
	////////////////////
	//Fait par Vincent//
	////////////////////
	
	/////////////////
	// Constructor //
	/////////////////
	public Transformateur2AcheteurCCadre() {
		super(); //récupère les infos de Transformateur2Acteur
		this.contratsEnCours = new LinkedList<ExemplaireContratCadre>();
		this.contratsTermines = new LinkedList<ExemplaireContratCadre>();
		this.journalCC = new Journal(this.getNom()+" journal CC", this);
	}
	
	///////////////////////////
	// Initialise le contrat //
	///////////////////////////
	public void initialiser() {
		super.initialiser();
		this.supCC = (SuperviseurVentesContratCadre)(Filiere.LA_FILIERE.getActeur("Sup.CCadre"));
		this.BlackListVendeur = new HashMap<IVendeurContratCadre, Integer>();
		this.Etapenego = 0;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// Permet d'enregistrer et de garder une trace des contrats en cours et des anciens contrats //
	///////////////////////////////////////////////////////////////////////////////////////////////
	public void next() {
		super.next();
		this.Etapenego ++;  
		this.journalCC.ajouter("=== STEP "+Filiere.LA_FILIERE.getEtape()+" ====================");
				for (Feve f : stockFeves.keySet()) { // pas forcement equitable : on avise si on lance un contrat cadre pour tout type de feve
					if ((this.stockFeves.get(f)<1200) & (f.getGamme()!=Gamme.HQ)) { // Modifier quantité minimale avant achat
						this.journalCC.ajouter("   "+f+" suffisamment peu en stock pour passer un CC");
						double parStep = 35000; // Changer quantité par Step
						Echeancier e = new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 78, parStep);
						List<IVendeurContratCadre> vendeurs = supCC.getVendeurs(f);
						if (vendeurs.size()>0) {
							IVendeurContratCadre vendeur = vendeurs.get(Filiere.random.nextInt(vendeurs.size()));
							for (IVendeurContratCadre v : this.BlackListVendeur.keySet()) {
								if (this.BlackListVendeur.containsKey(vendeur)) {
									if (this.BlackListVendeur.get(vendeur)>this.BlackListVendeur.get(v)) { // Choisit le vendeur avec qui le moins de négociations a échoué
										vendeur = v;
									}
								}
							}
							journalCC.ajouter("   "+vendeur.getNom()+" retenu comme vendeur parmi "+vendeurs.size()+" vendeurs potentiels");
							ExemplaireContratCadre contrat = supCC.demandeAcheteur(this, vendeur, f, e, cryptogramme, false);
							if (contrat==null) {
								if (this.BlackListVendeur.containsKey(vendeur)) {
									this.BlackListVendeur.put(vendeur,this.BlackListVendeur.get(vendeur)+1);
								} else {
									this.BlackListVendeur.put(vendeur, 1);
								}
								
								journalCC.ajouter(Color.RED, Color.white,"   echec des negociations -- échec de "+this.BlackListVendeur.get(vendeur)+" contrats avec : "+vendeur);
								this.Etapenego=0;
							} else {
								this.contratsEnCours.add(contrat);
								this.Etapenego=0;
								journalCC.ajouter(Color.GREEN, vendeur.getColor(), "   contrat signe : #"+contrat.getNumero()+" | Acheteur : "+contrat.getAcheteur()+" | Vendeur : "+contrat.getVendeur()+" | Produit : "+contrat.getProduit()+" | Quantité totale : "+contrat.getQuantiteTotale()+" | Prix : "+contrat.getPrix());
							}
						} else {
							journalCC.ajouter("   pas de vendeur");
							this.Etapenego=0;
					}
					} else {
						if (f.getGamme()!=Gamme.HQ) {
						journalCC.ajouter(f+" suffisament de stock pour ne pas passer de contrat cadre");
							}
						this.Etapenego=0;
					}
				}	
		// On archive les contrats termines
		for (ExemplaireContratCadre c : this.contratsEnCours) {
			if (c.getQuantiteRestantALivrer()==0.0 && c.getMontantRestantARegler()<=0.0) {
				journalCC.ajouter(Color.YELLOW, Color.BLACK,"Archivage du contrat : "+"#"+c.getNumero()+" | Acheteur : "+c.getAcheteur()+" | Vendeur : "+c.getVendeur()+" | Produit : "+c.getProduit()+" | Quantité totale : "+c.getQuantiteTotale()+" | Prix : "+c.getPrix());
				this.contratsTermines.add(c);
			}
		}
		for (ExemplaireContratCadre c : this.contratsTermines) {
			this.contratsEnCours.remove(c);
		}
		this.journalCC.ajouter("Nombre de contrats en cours : "+this.contratsEnCours.size());
		this.journalCC.ajouter("Nombre de contrats termines : "+this.contratsTermines.size());
		this.journalCC.ajouter("=================================");
	}
	
	/////////////////////////////////////
	// Ajoute notre journal aux autres //
	/////////////////////////////////////
	public List<Journal> getJournaux() {
		List<Journal> jx=super.getJournaux();
		jx.add(journalCC);
		return jx;
	}
	
	
	/////////////////////////////////////////////
	//     Fonctions du protocole de Vente     //
	/////////////////////////////////////////////
	
	public boolean achete(IProduit produit) {
		return produit.getType().equals("F_MQ") || produit.getType().equals("F_MQ_E") || produit.getType().equals("F_BQ");
	}

	public Echeancier contrePropositionDeLAcheteur(ExemplaireContratCadre contrat) {
		if (contrat.getProduit().getType().equals("F_HQ") || contrat.getProduit().getType().equals("F_HQ_BE") || contrat.getProduit().getType().equals("F_HQ_E")) {
			return null; // retourne null si ce n'est pas la bonne fève
		}
				
		if (contrat.getEcheancier().getNbEcheances()<78) { //durée trop courte 
				if (contrat.getEcheancier().getQuantiteTotale()>35000) { //quantité trop grande 
					return new Echeancier(Filiere.LA_FILIERE.getEtape()+1,78,35000) ; //on ramène la durée et la quantité aux bornes fixées
				}
				else if (contrat.getEcheancier().getQuantiteTotale()<20000) { //quantité trop faible
					return new Echeancier(Filiere.LA_FILIERE.getEtape()+1,78,20000) ; //on ramène la durée et la quantité aux bornes fixées
				}
				else { //quantité convenable
					return new Echeancier(Filiere.LA_FILIERE.getEtape()+1,78,contrat.getEcheancier().getQuantiteTotale()) ; //on ne change que la durée 
				}
			}
		if (contrat.getEcheancier().getNbEcheances()>260) { //durée trop longue 
				if (contrat.getEcheancier().getQuantiteTotale()>35000) { //quantité trop grande 
					return new Echeancier(Filiere.LA_FILIERE.getEtape()+1,260,35000) ; //on ramène la durée et la quantité aux bornes fixées
				}
				else if (contrat.getEcheancier().getQuantiteTotale()<20000) { //quantité trop faible
					return new Echeancier(Filiere.LA_FILIERE.getEtape()+1,260,20000) ; //on ramène la durée et la quantité aux bornes fixées
				}
				else { //quantité convenable
					return new Echeancier(Filiere.LA_FILIERE.getEtape()+1,260,contrat.getEcheancier().getQuantiteTotale()) ; //on ne change que la durée 
				}
			}
		
		//Durée convenable
			if (contrat.getEcheancier().getQuantiteTotale()>35000) { //quantité trop grande 
				return new Echeancier(Filiere.LA_FILIERE.getEtape()+1,contrat.getEcheancier().getNbEcheances(),35000) ; //on ramène la quantité à la borne fixée et on garde la durée 
			}
			else if (contrat.getEcheancier().getQuantiteTotale()<20000) { //quantité trop faible
				return new Echeancier(Filiere.LA_FILIERE.getEtape()+1,contrat.getEcheancier().getNbEcheances(),20000) ; //on ramène la quantité à la borne fixée et on garde la durée
			}
		return new Echeancier(Filiere.LA_FILIERE.getEtape()+1,contrat.getEcheancier().getNbEcheances(),contrat.getEcheancier().getQuantiteTotale()) ; //on garde tout tel quel
		
	}

	public double contrePropositionPrixAcheteur(ExemplaireContratCadre contrat) {
		BourseCacao bourse = (BourseCacao)(Filiere.LA_FILIERE.getActeur("BourseCacao"));
		if (Filiere.random.nextDouble()<0.20) { //20% de chance 
			return contrat.getPrix(); //on ne négocie pas 
		}
		else { //dans 80% des cas on négocie 
			if (contrat.getProduit().getType().equals("F_MQ") || contrat.getProduit().getType().equals("F_MQ")) { // pour les fèves pour lesquelles on connaît le prix en bourse 
				if (contrat.getEcheancier().getQuantiteTotale()*bourse.getCours((Feve)contrat.getProduit()).getValeur()*(1-(0.1/this.Etapenego))<contrat.getPrix()) { // si prix proposé par vendeur supérieur à prix voulu (varie à chaque tour de négo)
					return contrat.getEcheancier().getQuantiteTotale()*bourse.getCours((Feve)contrat.getProduit()).getValeur()*(1-(0.1/this.Etapenego)); //re-négociation à la valeur voulue (varie à chaque tour de négo)
				}
				else {
					return contrat.getPrix();
				}
			}
			else { //pas d'info sur la bourse pour équitable donc on renégocie par rapport au prix proposé par le vendeur sur le même modèle mathématique
				return contrat.getPrix()*(1-(0.1/this.Etapenego));
				}
		}
	}
	
	public void notificationNouveauContratCadre(ExemplaireContratCadre contrat) {
		journalCC.ajouter("Nouveau contrat accepté : "+"#"+contrat.getNumero()+" | Acheteur : "+contrat.getAcheteur()+" | Vendeur : "+contrat.getVendeur()+" | Produit : "+contrat.getProduit()+" | Quantité totale : "+contrat.getQuantiteTotale()+" | Prix : "+contrat.getPrix());	
		this.contratsEnCours.add(contrat);
	}


	public void receptionner(IProduit p, double quantiteEnTonnes, ExemplaireContratCadre contrat) {
		journalCC.ajouter("Réception de : "+quantiteEnTonnes+", tonnes de : "+p+" provenant du contrat : "+contrat.getNumero());
		stockFeves.put((Feve)p, stockFeves.get((Feve)p)+quantiteEnTonnes);
		totalStocksFeves.ajouter(this, quantiteEnTonnes, cryptogramme);
	}

}