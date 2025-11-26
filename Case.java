public class Case {
    private int ligne;
    private int colonne;
    private Integer valeur; // 0, 1 ou null pour vide

    public Case(int ligne, int colonne) {
        this.ligne = ligne;
        this.colonne = colonne;
        this.valeur = null;
    }

    public Integer getValeur() { return valeur; }
    public void setValeur(Integer valeur) { this.valeur = valeur; }

    public int getLigne() { return ligne; }
    public int getColonne() { return colonne; }
}
