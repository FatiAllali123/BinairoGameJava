package solver;

import java.util.*;

import model.BinairoGrid;
import model.BinairoProblem;
import model.BinairoState;

/**
 * Solveur avec FORWARD CHECKING (FC).
 * 
 * AMÉLIORATION par rapport au backtracking simple :
 *   - Après chaque placement, on vérifie les domaines des cases voisines
 *   - Si une case voisine n'a plus de valeur possible → échec immédiat
 *   - Évite d'explorer des branches vouées à l'échec
 * 
 * FC détecte les échecs AVANT de descendre dans l'arbre de recherche.
 */
public class ForwardCheckingSolver extends AbstractCSPSolver {
    
    @Override
    public BinairoState solve(BinairoState initialState) {
        BinairoState state = new BinairoState(initialState);
        
        if (backtrackWithFC(state)) {
            return state;
        }
        
        return null;
    }
    
    /**
     * Backtracking avec Forward Checking.
     */
    private boolean backtrackWithFC(BinairoState state) {
        nodesExplored++;
        
        BinairoGrid grid = state.getGrid();
        
        // Cas de base
        if (grid.isFull()) {
            return BinairoProblem.isValid(grid);
        }
        
        // Trouver une case vide (on peut utiliser MRV ici aussi)
        int[] cell = findFirstEmptyCell(grid);
        if (cell == null) {
            return grid.isFull() && BinairoProblem.isValid(grid);
        }
        
        int row = cell[0];
        int col = cell[1];
        
        // Essayer chaque valeur du domaine
        for (int value : new int[]{0, 1}) {
            grid.set(row, col, value);
            
            // Vérifier les contraintes locales
            if (BinairoProblem.isConsistentAt(grid, row, col)) {
                
                // FORWARD CHECKING : vérifier les domaines des voisins
                if (forwardCheck(grid, row, col)) {
                    // Tous les voisins ont encore des valeurs possibles
                    if (backtrackWithFC(state)) {
                        return true;
                    }
                }
            }
            
            grid.set(row, col, -1);
            backtrackCount++;
        }
        
        return false;
    }
    
    /**
     * FORWARD CHECKING : vérifie que toutes les cases voisines
     * ont encore au moins une valeur possible dans leur domaine.
     * 
     * @param grid Grille actuelle
     * @param row Ligne de la case qu'on vient de remplir
     * @param col Colonne de la case qu'on vient de remplir
     * @return true si tous les voisins ont encore des valeurs possibles
     */
    private boolean forwardCheck(BinairoGrid grid, int row, int col) {
        int size = grid.getSize();
        
        // Vérifier les cases vides de la même ligne
        for (int c = 0; c < size; c++) {
            if (grid.isEmpty(row, c)) {
                List<Integer> possible = BinairoProblem.getPossibleValues(grid, row, c);
                if (possible.isEmpty()) {
                    return false; // Domaine vide détecté !
                }
            }
        }
        
        // Vérifier les cases vides de la même colonne
        for (int r = 0; r < size; r++) {
            if (grid.isEmpty(r, col)) {
                List<Integer> possible = BinairoProblem.getPossibleValues(grid, r, col);
                if (possible.isEmpty()) {
                    return false;
                }
            }
        }
        
        return true; // Tous les voisins ont encore des valeurs possibles
    }
}