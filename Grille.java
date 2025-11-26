public class Grille {
    private int taille; // n x n
    private Case[][] cases;

    public Grille(int taille) {
        this.taille = taille;
        cases = new Case[taille][taille];
        for(int i=0; i<taille; i++)
            for(int j=0; j<taille; j++)
                cases[i][j] = new Case(i, j);
    }

    public Case getCase(int ligne, int col) {
        return cases[ligne][col];
    }

    public void afficherGrille() {
        for(int i=0; i<taille; i++){
            for(int j=0; j<taille; j++){
                Integer val = cases[i][j].getValeur();
                System.out.print((val == null ? "." : val) + " ");
            }
            System.out.println();
        }
    }




// Retourne true si la grille respecte la règle 
// "pas plus de deux identiques d'affilée"
public boolean verifierDeuxIdentiques() {

    // Vérification sur les lignes
    for(int i=0; i<taille; i++){
        for(int j=0; j<taille-2; j++) {
            Integer v1 = cases[i][j].getValeur();
            Integer v2 = cases[i][j+1].getValeur();
            Integer v3 = cases[i][j+2].getValeur();

            if(v1 != null && v1.equals(v2) && v2.equals(v3))
                return false; // 3 identiques → interdit
        }
    }

    // Vérification sur les colonnes
    for(int j=0; j<taille; j++){
        for(int i=0; i<taille-2; i++){
            Integer v1 = cases[i][j].getValeur();
            Integer v2 = cases[i+1][j].getValeur();
            Integer v3 = cases[i+2][j].getValeur();

            if(v1 != null && v1.equals(v2) && v2.equals(v3))
                return false;
        }
    }

    return true; 
}



// Vérifier "autant de 0 que de 1"
public boolean verifierEquilibre() {
    int half = taille / 2;

    // Vérification des lignes
    for(int i=0; i<taille; i++){
        int count0 = 0, count1 = 0;

        for(int j=0; j<taille; j++){
            Integer v = cases[i][j].getValeur();
            if(v != null) {
                if(v == 0) count0++;
                if(v == 1) count1++;
            }
        }

        if(count0 > half || count1 > half)
            return false;
    }

    // Vérification des colonnes
    for(int j=0; j<taille; j++){
        int count0 = 0, count1 = 0;

        for(int i=0; i<taille; i++){
            Integer v = cases[i][j].getValeur();
            if(v != null) {
                if(v == 0) count0++;
                if(v == 1) count1++;
            }
        }

        if(count0 > half || count1 > half)
            return false;
    }

    return true;
}

// Vérifier est ce que tous les lignes sont différentes
public boolean verifierLignesDifferentes() {

    for(int i=0; i<taille; i++){
        for(int k=i+1; k<taille; k++){ // compare ligne i avec ligne k
            boolean identiques = true;

            for(int j=0; j<taille; j++){
                Integer v1 = cases[i][j].getValeur();
                Integer v2 = cases[k][j].getValeur();

                if(v1 == null || !v1.equals(v2)){
                    identiques = false;
                    break;
                }
            }

            if(identiques)
                return false; // lignes identiques → interdit
        }
    }

    return true;
}


public boolean grilleValide() {
    return verifierDeuxIdentiques()
        && verifierEquilibre()
        && verifierLignesDifferentes();
}

}
