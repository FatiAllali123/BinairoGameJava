package model;

/**
 * Représente la grille du jeu Binairo.
 * Utilise une matrice d'entiers où :
 *   -1 = case vide
 *    0 = zéro placé
 *    1 = un placé
 * 
 * Cette classe est immuable en taille mais mutable en contenu.
 * Le constructeur par copie est ESSENTIEL pour le backtracking.
 */
public class BinairoGrid {
    private final int size;           // Taille de la grille (ex: 8 pour 8x8)
    private final int[][] cells;      // Matrice représentant la grille
    
    /**
     * Constructeur : crée une grille vide de taille donnée
     * @param size Taille de la grille (doit être >= 4 et paire de préférence)
     */
    public BinairoGrid(int size) {
        if (size < 4 || size % 2 != 0) {
            throw new IllegalArgumentException("Taille doit être >= 4 et paire");
        }
        this.size = size;
        this.cells = new int[size][size];
        
        // Initialiser toutes les cases à -1 (vide)
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j] = -1;
            }
        }
    }
    
    /**
     * Constructeur par copie (CRITIQUE pour backtracking)
     * Crée une copie profonde de la grille pour éviter les références partagées
     * @param other Grille à copier
     */
    public BinairoGrid(BinairoGrid other) {
        this.size = other.size;
        this.cells = new int[size][size];
        
        // Copie profonde ligne par ligne
        for (int i = 0; i < size; i++) {
            System.arraycopy(other.cells[i], 0, this.cells[i], 0, size);
        }
    }
    
    // ==================== GETTERS ====================
    
    public int getSize() { 
        return size; 
    }
    
    /**
     * Récupère la valeur d'une case
     * @return -1 si vide, 0 ou 1 sinon
     */
    public int get(int row, int col) {
        checkBounds(row, col);
        return cells[row][col];
    }
    
    /**
     * Place une valeur dans une case
     * @param value -1 (vide), 0 ou 1
     */
    public void set(int row, int col, int value) {
        checkBounds(row, col);
        if (value != -1 && value != 0 && value != 1) {
            throw new IllegalArgumentException("Valeur doit être -1, 0 ou 1");
        }
        cells[row][col] = value;
    }
    
    /**
     * Vérifie si une case est vide
     */
    public boolean isEmpty(int row, int col) {
        return get(row, col) == -1;
    }
    
    /**
     * Vérifie si la grille est complètement remplie
     */
    public boolean isFull() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (cells[i][j] == -1) return false;
            }
        }
        return true;
    }
    
    /**
     * Compte le nombre de cases vides
     * Utile pour MRV (Minimum Remaining Values)
     */
    public int countEmptyCells() {
        int count = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (cells[i][j] == -1) count++;
            }
        }
        return count;
    }
    
    /**
     * Récupère une ligne complète
     * @return Copie du tableau de la ligne
     */
    public int[] getRow(int row) {
        checkBounds(row, 0);
        int[] rowCopy = new int[size];
        System.arraycopy(cells[row], 0, rowCopy, 0, size);
        return rowCopy;
    }
    
    /**
     * Récupère une colonne complète
     * @return Nouveau tableau contenant la colonne
     */
    public int[] getColumn(int col) {
        checkBounds(0, col);
        int[] column = new int[size];
        for (int i = 0; i < size; i++) {
            column[i] = cells[i][col];
        }
        return column;
    }
    
    // ==================== VALIDATION ====================
    
    /**
     * Vérifie que les indices sont dans les limites
     */
    private void checkBounds(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            throw new IndexOutOfBoundsException(
                String.format("Indices (%d, %d) hors limites pour grille %dx%d", 
                              row, col, size, size)
            );
        }
    }
    
    // ==================== AFFICHAGE ====================
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Grille ").append(size).append("x").append(size).append(":\n");
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int val = cells[i][j];
                sb.append(val == -1 ? "." : val).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Affichage avec indices pour déboggage
     */
    public String toStringWithIndices() {
        StringBuilder sb = new StringBuilder();
        
        // En-tête colonnes
        sb.append("   ");
        for (int j = 0; j < size; j++) {
            sb.append(j).append(" ");
        }
        sb.append("\n");
        
        // Lignes avec indices
        for (int i = 0; i < size; i++) {
            sb.append(i).append("| ");
            for (int j = 0; j < size; j++) {
                int val = cells[i][j];
                sb.append(val == -1 ? "." : val).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}