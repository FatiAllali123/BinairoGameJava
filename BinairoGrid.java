public class BinairoGrid {
    private final int size;         // taille de la grille (peut être 4, 6, 8, 10, 12, etc.)
    private final int[][] cells;    // -1 = case vide, 0 ou 1 = rempli

    // Constructeur : grille vide de taille donnée
    public BinairoGrid(int size) {
        if (size < 4 || size % 2 != 0 && size % 2 == 1 && size > 10) {
            // On accepte toutes les tailles paires + quelques impaires classiques
            // Mais on peut tout gérer, donc on laisse ouvert
        }
        this.size = size;
        this.cells = new int[size][size];
        clear(); // toutes les cases à -1
    }

    // Constructeur par copie profonde (INDISPENSABLE pour le backtracking)
    public BinairoGrid(BinairoGrid other) {
        this.size = other.size;
        this.cells = new int[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(other.cells[i], 0, this.cells[i], 0, size);
        }
    }

    // Remet toute la grille à vide
    public void clear() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j] = -1; // -1 cad vide 
            }
        }
    }

    // Getters
    public int getSize() { return size; }

    public int get(int row, int col) {
        checkBounds(row, col);
        return cells[row][col];
    }

    // Setters
    public void set(int row, int col, int value) {
        checkBounds(row, col);
        if (value != -1 && value != 0 && value != 1) {
            throw new IllegalArgumentException("Valeur doit être -1 (vide), 0 ou 1");
        }
        cells[row][col] = value;
    }
     
    // est ce que une case est vide 
    public boolean isEmpty(int row, int col) {
        return get(row, col) == -1;
    }

    public boolean isFilled(int row, int col) {
        return get(row, col) != -1;
    }

    // Vérifie si la grille est complètement remplie
    public boolean isFull() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (cells[i][j] == -1) return false;
            }
        }
        return true;
    }

    // Pour l’affichage dans la console (très utile pour débugger)
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int val = cells[i][j];
                sb.append(val == -1 ? "." : val);
                if (j < size - 1) sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // Vérification des indices
    private void checkBounds(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            throw new IndexOutOfBoundsException("Position (" + row + "," + col + ") hors grille " + size + "x" + size);
        }
    }

    // Utile plus tard pour la sauvegarde
    public BinairoGrid copy() {
        return new BinairoGrid(this);
    }
}