package solver;

import model.BinairoGrid;
import model.BinairoProblem;
import model.BinairoState;

/**
 * Classe abstraite pour tous les solveurs CSP du Binairo.
 * 
 * PATTERN UTILISÉ :
 *   - Méthode abstraite solve() que chaque solveur implémente
 *   - Méthodes communes partagées par tous les solveurs
 *   - Statistiques de performance pour la comparaison
 */
public abstract class AbstractCSPSolver {
    
    // ==================== STATISTIQUES ====================
    // Pour comparer les performances des différents solveurs
    
    protected int nodesExplored;      // Nombre de nœuds visités
    protected int backtrackCount;     // Nombre de retours arrière
    protected long solvingTime;       // Temps de résolution en ms
    protected boolean solutionFound;  // Solution trouvée ou non
    
    /**
     * Constructeur : initialise les statistiques
     */
    public AbstractCSPSolver() {
        resetStatistics();
    }
    
    /**
     * Réinitialise les statistiques avant une nouvelle résolution
     */
    protected void resetStatistics() {
        this.nodesExplored = 0;
        this.backtrackCount = 0;
        this.solvingTime = 0;
        this.solutionFound = false;
    }
    
    // ==================== MÉTHODE ABSTRAITE ====================
    
    /**
     * MÉTHODE ABSTRAITE : chaque solveur doit l'implémenter.
     * 
     * @param initialState État initial du problème
     * @return État résolu, ou null si pas de solution
     */
    public abstract BinairoState solve(BinairoState initialState);
    
    // ==================== MÉTHODE PUBLIQUE AVEC TIMING ====================
    
    /**
     * Résout le problème en mesurant le temps d'exécution.
     * Cette méthode appelle solve() et enregistre les statistiques.
     * 
     * @param initialState État initial
     * @return État résolu ou null
     */
    public BinairoState solveWithTiming(BinairoState initialState) {
        resetStatistics();
        
        long startTime = System.currentTimeMillis();
        BinairoState solution = solve(initialState);
        long endTime = System.currentTimeMillis();
        
        this.solvingTime = endTime - startTime;
        this.solutionFound = (solution != null && solution.isSolved());
        
        return solution;
    }
    
    // ==================== MÉTHODES UTILITAIRES COMMUNES ====================
    
    /**
     * Trouve la première case vide dans la grille.
     * Parcours ligne par ligne, colonne par colonne.
     * 
     * @return Tableau [row, col] ou null si grille complète
     */
    protected int[] findFirstEmptyCell(BinairoGrid grid) {
        int size = grid.getSize();
        
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (grid.isEmpty(row, col)) {
                    return new int[]{row, col};
                }
            }
        }
        
        return null; // Grille complète
    }
    
    /**
     * Trouve la case vide avec le domaine le plus petit (MRV).
     * 
     * MRV = Minimum Remaining Values
     * Heuristique : choisir la variable la plus contrainte en premier
     * → réduit l'arbre de recherche
     * 
     * @return Tableau [row, col] ou null si grille complète
     */
    protected int[] findMRVCell(BinairoGrid grid) {
        int size = grid.getSize();
        int minDomainSize = Integer.MAX_VALUE;
        int[] bestCell = null;
        
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (grid.isEmpty(row, col)) {
                    // Calculer la taille du domaine pour cette case
                    java.util.List<Integer> possibleValues = 
                        BinairoProblem.getPossibleValues(grid, row, col);
                    
                    int domainSize = possibleValues.size();
                    
                    // Si domaine vide → échec immédiat
                    if (domainSize == 0) {
                        return new int[]{row, col}; // Retourner cette case pour détecter l'échec
                    }
                    
                    // Garder la case avec le plus petit domaine
                    if (domainSize < minDomainSize) {
                        minDomainSize = domainSize;
                        bestCell = new int[]{row, col};
                    }
                }
            }
        }
        
        return bestCell;
    }
    
    /**
     * Trouve la case avec MRV + tie-breaking avec Degree heuristic.
     * 
     * Si plusieurs cases ont le même domaine minimal :
     *   → choisir celle qui contraint le plus d'autres variables (degré maximal)
     * 
     * @return Tableau [row, col] ou null
     */
    protected int[] findMRVWithDegreeCell(BinairoGrid grid) {
        int size = grid.getSize();
        int minDomainSize = Integer.MAX_VALUE;
        int maxDegree = -1;
        int[] bestCell = null;
        
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (grid.isEmpty(row, col)) {
                    java.util.List<Integer> possibleValues = 
                        BinairoProblem.getPossibleValues(grid, row, col);
                    
                    int domainSize = possibleValues.size();
                    
                    if (domainSize == 0) {
                        return new int[]{row, col};
                    }
                    
                    // MRV : domaine plus petit
                    if (domainSize < minDomainSize) {
                        minDomainSize = domainSize;
                        maxDegree = BinairoProblem.getDegree(grid, row, col);
                        bestCell = new int[]{row, col};
                    }
                    // Tie-breaking avec Degree
                    else if (domainSize == minDomainSize) {
                        int degree = BinairoProblem.getDegree(grid, row, col);
                        if (degree > maxDegree) {
                            maxDegree = degree;
                            bestCell = new int[]{row, col};
                        }
                    }
                }
            }
        }
        
        return bestCell;
    }
    
    /**
     * Ordonne les valeurs du domaine selon LCV (Least Constraining Value).
     * 
     * LCV : essayer d'abord les valeurs qui laissent le plus de liberté
     * aux autres variables.
     * 
     * @param grid Grille actuelle
     * @param row Ligne de la case
     * @param col Colonne de la case
     * @param domain Domaine initial (typiquement {0, 1})
     * @return Liste ordonnée selon LCV
     */
    protected java.util.List<Integer> orderValuesByLCV(
            BinairoGrid grid, int row, int col, java.util.List<Integer> domain) {
        
        // Map pour stocker : valeur → nombre de choix éliminés
        java.util.Map<Integer, Integer> constrainingCount = new java.util.HashMap<>();
        
        for (int value : domain) {
            // Placer temporairement la valeur
            grid.set(row, col, value);
            
            // Compter combien de valeurs sont éliminées dans les cases voisines
            int eliminated = countEliminatedValues(grid, row, col);
            
            constrainingCount.put(value, eliminated);
            
            // Retirer la valeur
            grid.set(row, col, -1);
        }
        
        // Trier le domaine : valeur qui élimine le MOINS en premier
        java.util.List<Integer> ordered = new java.util.ArrayList<>(domain);
        ordered.sort((v1, v2) -> 
            Integer.compare(constrainingCount.get(v1), constrainingCount.get(v2))
        );
        
        return ordered;
    }
    
    /**
     * Compte combien de valeurs sont éliminées dans les cases voisines
     * après placement d'une valeur en (row, col).
     * 
     * Utilisé par LCV.
     */
    private int countEliminatedValues(BinairoGrid grid, int row, int col) {
        int size = grid.getSize();
        int eliminated = 0;
        
        // Vérifier les cases vides de la même ligne
        for (int c = 0; c < size; c++) {
            if (c != col && grid.isEmpty(row, c)) {
                java.util.List<Integer> possible = 
                    BinairoProblem.getPossibleValues(grid, row, c);
                eliminated += (2 - possible.size()); // 2 car domaine initial {0,1}
            }
        }
        
        // Vérifier les cases vides de la même colonne
        for (int r = 0; r < size; r++) {
            if (r != row && grid.isEmpty(r, col)) {
                java.util.List<Integer> possible = 
                    BinairoProblem.getPossibleValues(grid, r, col);
                eliminated += (2 - possible.size());
            }
        }
        
        return eliminated;
    }
    
    // ==================== GETTERS POUR STATISTIQUES ====================
    
    public int getNodesExplored() {
        return nodesExplored;
    }
    
    public int getBacktrackCount() {
        return backtrackCount;
    }
    
    public long getSolvingTime() {
        return solvingTime;
    }
    
    public boolean isSolutionFound() {
        return solutionFound;
    }
    
    /**
     * Affiche un résumé des statistiques de résolution.
     */
    public void printStatistics() {
        System.out.println("========== Statistiques du solveur ==========");
        System.out.println("Solveur : " + this.getClass().getSimpleName());
        System.out.println("Solution trouvée : " + (solutionFound ? "OUI" : "NON"));
        System.out.println("Nœuds explorés : " + nodesExplored);
        System.out.println("Retours arrière : " + backtrackCount);
        System.out.println("Temps de résolution : " + solvingTime + " ms");
        System.out.println("=============================================");
    }
    
    /**
     * Retourne un nom court pour le solveur (utilisé dans l'interface)
     */
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
