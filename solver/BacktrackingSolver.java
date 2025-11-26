package solver;

import model.BinairoGrid;
import model.BinairoProblem;
import model.BinairoState;

/**
 * Solveur par BACKTRACKING simple (sans heuristiques).
 * 
 * ALGORITHME :
 *   1. Trouver une case vide
 *   2. Essayer 0, puis 1
 *   3. Si valide → continuer récursivement
 *   4. Si échec → backtrack (retour arrière)
 * Sert de référence pour comparer avec les versions optimisées.
 */
public class BacktrackingSolver extends AbstractCSPSolver {
    
    /**
     * Résout le problème par backtracking simple.
     * 
     * @param initialState État initial
     * @return État résolu ou null si impossible
     */
    @Override
    public BinairoState solve(BinairoState initialState) {
        // Copier l'état pour ne pas modifier l'original
        BinairoState state = new BinairoState(initialState);
        
        // Lancer la recherche récursive
        if (backtrack(state)) {
            return state;
        }
        
        return null; // Pas de solution trouvée
    }
    
    /**
     * Fonction récursive de backtracking.
     * 
     * @param state État courant
     * @return true si solution trouvée, false sinon
     */
    private boolean backtrack(BinairoState state) {
        nodesExplored++; // Incrémenter le compteur de nœuds
        
        BinairoGrid grid = state.getGrid();
        
        // CAS DE BASE : grille complète
        if (grid.isFull()) {
            // Vérifier si c'est une solution valide
            return BinairoProblem.isValid(grid);
        }
        
        // ÉTAPE 1 : Trouver la première case vide
        int[] cell = findFirstEmptyCell(grid);
        if (cell == null) {
            return grid.isFull() && BinairoProblem.isValid(grid);
        }
        
        int row = cell[0];
        int col = cell[1];
        
        // ÉTAPE 2 : Essayer les valeurs 0 et 1
        for (int value : new int[]{0, 1}) {
            // Placer la valeur
            grid.set(row, col, value);
            
            // ÉTAPE 3 : Vérifier les contraintes
            if (BinairoProblem.isConsistentAt(grid, row, col)) {
                // Contraintes OK → continuer récursivement
                if (backtrack(state)) {
                    return true; // Solution trouvée !
                }
            }
            
            // ÉTAPE 4 : BACKTRACK - retirer la valeur
            grid.set(row, col, -1);
            backtrackCount++;
        }
        
        // Aucune valeur ne fonctionne → échec
        return false;
    }
}