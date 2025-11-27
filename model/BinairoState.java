package model;
/**
 * Représente un état du jeu Binairo.
 * Contient une grille et potentiellement d'autres informations d'état.
 * 
 * Cette classe encapsule BinairoGrid et peut être étendue pour inclure
 * des métadonnées comme le nombre de coups, l'historique, etc.
 * 
 */
public class BinairoState {
    private final BinairoGrid grid;
    private final boolean isInitialState;  // Pour marquer les cases fixes (non modifiables)
    
    /**
     * Constructeur : crée un état avec une grille vide
     * @param size Taille de la grille
     */
    public BinairoState(int size) {
        this.grid = new BinairoGrid(size);
        this.isInitialState = true;
    }
    
    /**
     * Constructeur par copie d'état
     * ESSENTIEL pour le backtracking : chaque nœud de l'arbre a son propre état
     * @param other État à copier
     */
    public BinairoState(BinairoState other) {
        this.grid = new BinairoGrid(other.grid);  // Copie profonde de la grille
        this.isInitialState = other.isInitialState;
    }
    
    /**
     * Constructeur à partir d'une grille existante
     * @param grid Grille à encapsuler
     */
    public BinairoState(BinairoGrid grid) {
        this.grid = new BinairoGrid(grid);  // Toujours copier !
        this.isInitialState = false;
    }
    
    // ==================== ACCESSEURS ====================
    
    public BinairoGrid getGrid() { 
        return grid; 
    }
    
    public int getSize() { 
        return grid.getSize(); 
    }
    
    /**
     * Vérifie si la grille est complètement résolue
     */
    public boolean isSolved() {
        return grid.isFull();
    }
    
    /**
     * Crée une copie de cet état
     * Utilisé intensivement dans le backtracking
     */
    public BinairoState copy() {
        return new BinairoState(this);
    }
    
    // ==================== AFFICHAGE ====================
    
    @Override
    public String toString() {
        return grid.toString();
    }
    
    public String toStringWithIndices() {
        return grid.toStringWithIndices();
    }
}
