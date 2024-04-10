package abstraction.eq5Transformateur2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.filiere.IMarqueChocolat;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.general.VariablePrivee;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.produits.IProduit;

public class Transformateur2Acteur implements IActeur,IMarqueChocolat {
	
	protected Journal journal;
	protected int cryptogramme;
	private double coutStockage;
	
	private static final double STOCKINITIAL=1000.0;
	
	protected List<Feve> lesFeves;
	protected List<Chocolat> lesChocolats;
	protected List<ChocolatDeMarque>chocosProduits;
	protected HashMap<Feve, Double> stockFeves;
	protected HashMap<Chocolat, Double> stockChoco;
	protected HashMap<ChocolatDeMarque, Double> stockChocoMarque;
	protected HashMap<Feve, HashMap<Chocolat, Double>> pourcentageTransfo; // pour les differentes feves, le chocolat qu'elle peuvent contribuer a produire avec le ratio
	protected List<ChocolatDeMarque> chocolatsFusion;
	protected Variable totalStocksFeves;  // La qualite totale de stock de feves 
	protected Variable totalStocksChoco;  // La qualite totale de stock de chocolat 
	protected Variable totalStocksChocoMarque;  // La qualite totale de stock de chocolat de marque 
	
	
	////////////////////////////////////////////
	// Constructor & Initialization of stocks //
	////////////////////////////////////////////
	public Transformateur2Acteur() {
		this.journal = new Journal(this.getNom()+" journal", this);
		this.totalStocksFeves = new VariablePrivee("Eq5TStockFeves", "<html>Quantite totale de feves en stock</html>",this, 0.0, 1000000.0, 0.0);
		this.totalStocksChoco = new VariablePrivee("Eq5TStockChoco", "<html>Quantite totale de chocolat en stock</html>",this, 0.0, 1000000.0, 0.0);
		this.totalStocksChocoMarque = new VariablePrivee("Eq5TStockChocoMarque", "<html>Quantite totale de chocolat de marque en stock</html>",this, 0.0, 1000000.0, 0.0);
	}
	
	public void initialiser() {
		this.lesFeves = new LinkedList<Feve>();
		this.journal.ajouter("Les Feves sont :");
		for (Feve f : Feve.values()) {
			if (f.getGamme()!=Gamme.HQ) {
			this.lesFeves.add(f);
			this.journal.ajouter("   - "+f);
			}
		}
		this.coutStockage = Filiere.LA_FILIERE.getParametre("cout moyen stockage producteur").getValeur()*4;
		
		this.stockFeves=new HashMap<Feve,Double>();
		for (Feve f : this.lesFeves) {
			this.stockFeves.put(f, STOCKINITIAL);
			this.totalStocksFeves.ajouter(this, STOCKINITIAL, this.cryptogramme);
			this.journal.ajouter("ajout de "+STOCKINITIAL+" tonnes de : "+f+" au stock total de fèves // stock total : "+this.totalStocksFeves.getValeur(this.cryptogramme));
		}
		this.lesChocolats = new LinkedList<Chocolat>();
		this.journal.ajouter("Les Chocolats sont :");
		for (Chocolat c : Chocolat.values()) {
			this.lesChocolats.add(c);
			this.journal.ajouter("   - "+c);
		}
		this.stockChoco=new HashMap<Chocolat,Double>();
		for (Chocolat c : this.lesChocolats) {
			this.stockChoco.put(c, STOCKINITIAL);
			this.totalStocksChoco.ajouter(this, STOCKINITIAL, this.cryptogramme);
			this.journal.ajouter("ajout de "+STOCKINITIAL+" tonnes de : "+c+" au stock total de Chocolat // stock total : "+this.totalStocksChoco.getValeur(this.cryptogramme));
		}
		this.chocosProduits = new LinkedList<ChocolatDeMarque>();
		this.journal.ajouter("Les Chocolats de marque sont :");
		for (ChocolatDeMarque cm : Filiere.LA_FILIERE.getChocolatsProduits()) {
			if (Filiere.LA_FILIERE.getMarquesDistributeur().contains(cm.getMarque()) || cm.getMarque().equals("CacaoFusion")){
				this.chocosProduits.add(cm);
				this.journal.ajouter("   - "+cm);
			}
		}
		this.stockChocoMarque=new HashMap<ChocolatDeMarque,Double>();
		for (ChocolatDeMarque cm : this.chocosProduits) {
			this.stockChocoMarque.put(cm, STOCKINITIAL);
			this.totalStocksChocoMarque.ajouter(this, STOCKINITIAL, this.cryptogramme);
			this.journal.ajouter("ajout de "+STOCKINITIAL+" tonnes de : "+cm+" au stock total de Chocolat de marque // stock total : "+this.totalStocksChocoMarque.getValeur(this.cryptogramme));
		}
	}

	public String getNom() {// NE PAS MODIFIER
		return "EQ5";
	}
	
	public String toString() {// NE PAS MODIFIER
		return this.getNom();
	}

	////////////////////////////////////////////////////////
	//         En lien avec l'interface graphique         //
	////////////////////////////////////////////////////////

	public void next() {
		this.journal.ajouter(" ===ETAPE = " + Filiere.LA_FILIERE.getEtape()+ " A L'ANNEE " + Filiere.LA_FILIERE.getAnnee()+" ===");
		this.journal.ajouter("=====STOCKS=====");
		this.journal.ajouter("prix stockage chez producteur : "+ Filiere.LA_FILIERE.getParametre("cout moyen stockage producteur").getValeur());
		this.journal.ajouter("Quantité en stock de feves : "+stockFeves);
		this.journal.ajouter("Quantité en stock de Chocolat : "+stockChoco);
		this.journal.ajouter("Quantité en stock de chocolat de marque : " +stockChocoMarque);
		this.journal.ajouter("stocks feves : "+this.totalStocksFeves.getValeur(this.cryptogramme));
		this.journal.ajouter("stocks chocolat : "+this.totalStocksChoco.getValeur(this.cryptogramme));
		Filiere.LA_FILIERE.getBanque().payerCout(this, cryptogramme, "Stockage", (this.totalStocksFeves.getValeur(cryptogramme)+this.totalStocksChoco.getValeur(cryptogramme)+this.totalStocksChocoMarque.getValeur(cryptogramme))*this.coutStockage);

	}

	public Color getColor() {// NE PAS MODIFIER
		return new Color(165, 235, 195); 
	}

	public String getDescription() {
		return "Fuuuuuuusion";
	}

	// Renvoie les indicateurs
	public List<Variable> getIndicateurs() {
		List<Variable> res = new ArrayList<Variable>();
		return res;
	}

	// Renvoie les parametres
	public List<Variable> getParametres() {
		List<Variable> res=new ArrayList<Variable>();
		return res;
	}

	// Renvoie les journaux
	public List<Journal> getJournaux() {
		List<Journal> res=new ArrayList<Journal>();
		res.add(this.journal);
		return res;
	}

	////////////////////////////////////////////////////////
	//               En lien avec la Banque               //
	////////////////////////////////////////////////////////

	// Appelee en debut de simulation pour vous communiquer 
	// votre cryptogramme personnel, indispensable pour les
	// transactions.
	public void setCryptogramme(Integer crypto) {
		this.cryptogramme = crypto;
	}

	// Appelee lorsqu'un acteur fait faillite (potentiellement vous)
	// afin de vous en informer.
	public void notificationFaillite(IActeur acteur) {
	}

	// Apres chaque operation sur votre compte bancaire, cette
	// operation est appelee pour vous en informer
	public void notificationOperationBancaire(double montant) {
	}
	
	// Renvoie le solde actuel de l'acteur
	protected double getSolde() {
		return Filiere.LA_FILIERE.getBanque().getSolde(Filiere.LA_FILIERE.getActeur(getNom()), this.cryptogramme);
	}

	////////////////////////////////////////////////////////
	//        Pour la creation de filieres de test        //
	////////////////////////////////////////////////////////

	// Renvoie la liste des filieres proposees par l'acteur
	public List<String> getNomsFilieresProposees() {
		ArrayList<String> filieres = new ArrayList<String>();
		return(filieres);
	}

	// Renvoie une instance d'une filiere d'apres son nom
	public Filiere getFiliere(String nom) {
		return Filiere.LA_FILIERE;
	}

	public double getQuantiteEnStock(IProduit p, int cryptogramme) {
		if (this.cryptogramme==cryptogramme) { // c'est donc bien un acteur assermente qui demande a consulter la quantite en stock
			if (p.getType().equals("Feve")) {
				if (this.stockFeves.keySet().contains(p)) {
					return this.stockFeves.get(p);
				} else {
					return 0.0;
				}
			} else if (p.getType().equals("Chocolat")) {
				if (this.stockChoco.keySet().contains(p)) {
					return this.stockChoco.get(p);
				} else {
					return 0.0;
				}
			} else {
				if (this.stockChocoMarque.keySet().contains(p)) {
					return this.stockChocoMarque.get(p);
				} else {
					return 0.0;
				}
			}
		} else {
			return 0; // Les acteurs non assermentes n'ont pas a connaitre notre stock
		}
	}

	
	
	////////////////////////////////////////////////////////
	//        Déclaration de la marque CacaoFusion        //
	////////////////////////////////////////////////////////
	public List<String> getMarquesChocolat() {
		LinkedList<String> marques = new LinkedList<String>();
		//marques.add("CacaoFusion");
		return marques;
	}
}
