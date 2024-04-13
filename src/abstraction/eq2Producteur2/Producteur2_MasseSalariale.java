package abstraction.eq2Producteur2;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;


public abstract class Producteur2_MasseSalariale extends Producteur2_Stocks {
	private int nb_employes ;
	private int nb_employes_equitable;
	private int nb_employes_enfants;
	
	private double salaire_enfant;
	private double salaire_adulte;
	private double salaire_adulte_equitable;
	protected Journal journal_RH;
	
			
	public void initialiser_masse_salariale() {
		this.journal = new Journal(this.getNom()+" journal_RH", this);
		this.nb_employes =  3679200;
		this.nb_employes_equitable = 100800;//2% init
		this.nb_employes_enfants = 1260000; //25% au départ
		
		this.salaire_enfant =  1;
		this.salaire_adulte = 2;
		this.salaire_adulte_equitable = 3;
	}
	
	public int getNb_employes() {
		return nb_employes;
	}
	public void setNb_employes(int nb_employes) {
		this.nb_employes = nb_employes;
	}
	public int getNb_employes_equitable() {
		return nb_employes_equitable;
	}
	public void setNb_employes_equitable(int nb_employes_equitable) {
		this.nb_employes_equitable = nb_employes_equitable;
	}
	public int getNb_employes_enfants() {
		return nb_employes_enfants;
	}
	public void setNb_employes_enfants(int nb_employes_enfants) {
		this.nb_employes_enfants = nb_employes_enfants;
	}
	
	public double getSalaire(String categorie) {
		if (categorie == "enfant") {
			return salaire_enfant;
		}
		else if (categorie == "adulte équitable") {
			return salaire_adulte_equitable;
		}
		else {
			return salaire_adulte;
		}
	}

	public int getNombreEmployes(String categorie) {
		if (categorie == "enfant") {
			return this.getNb_employes_enfants();
		}
		else if (categorie == "adulte équitable") {
			return this.getNb_employes_equitable();
		}
		else {
			return this.getNb_employes();
		}
	}
	
	public void setNombreEmployes(String categorie, int d) {
		if (categorie == "enfant") {
			this.setNb_employes_enfants(d);
		}
		else if (categorie == "adulte équitable") {
			this.setNb_employes_equitable(d);
		}
		else {
			this.setNb_employes(d);
		}
	}
	
	public void licenciement(int n, String categorie) {
		int nb_emp = getNombreEmployes(categorie);
		if ((nb_emp - n) < 0) {
			this.setNombreEmployes(categorie, 0);
		}
		else { 
			this.setNombreEmployes(categorie, nb_emp-n);
		}
	}
	
	public void embauche(int n, String categorie) {
		int nb_emp = getNombreEmployes(categorie);
		this.setNombreEmployes(categorie, nb_emp+n);
	}
	
	public double cout_humain_par_step() { // Renvoie le coût total lié à la main d'oeuvre par step 
		double enfants = getNb_employes_enfants()* getSalaire("enfant");
		double adultes_eq = getNb_employes_equitable()*getSalaire("adulte équitable");
		double adultes = getNb_employes()*getSalaire("adulte"); 
		return enfants + adultes_eq + adultes;
	}

	// Fonction qui permet d'implémenter notre stratégie. 
	// Si notre solde est supérieur au cout humain pour 10 tours
	public void next_RH() {
		double solde = this.getSolde();
		if (solde > 10*cout_humain_par_step()){
			int nb_enf = getNb_employes_enfants();
			int modif = (int) Math.round(0.04*nb_enf);
			int modif_equitable = (int) Math.round(modif*0.2);
			System.out.println("\n ETAPE " + Filiere.LA_FILIERE.getEtape());
			System.out.println("nombre d'enfants : " + this.getNb_employes_enfants());
			System.out.println("nombre d'employes equitable : " + this.getNb_employes_equitable());
			
			this.setNb_employes_enfants(nb_enf - modif);
			this.setNb_employes_equitable(this.getNb_employes_equitable() + modif_equitable);
			this.setNb_employes(this.getNb_employes() + modif - modif_equitable);
			
			this.journal.ajouter("-------------- ETAPE " + Filiere.LA_FILIERE.getEtape() + " --------------------");
			this.journal.ajouter("nombre d'employes équitable :" + this.getNb_employes_equitable());
			this.journal.ajouter("nombre d'employes adultes : "+ this.getNb_employes());
			this.journal.ajouter("nombre d'employes enfants " + this.getNb_employes_enfants());
		}
	}
	

}
