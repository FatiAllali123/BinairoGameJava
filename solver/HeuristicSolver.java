package solver;
import java.util.*;

import model.BinairoGrid;
import model.BinairoProblem;
import model.BinairoState;

/**
 * Solveur avec TOUTES les heuristiques :
 *   - MRV (Minimum Remaining Values) : choisir la case avec le domaine le plus petit
 *   - Degree : en cas d'égalité MRV, choisir la case qui contraint le plus de voisins
 *   - LCV (Least Constraining Value) : ordonner les valeurs par nombre de contraintes
 *   - Forward Checking : vérifier les domaines après chaque placement
 * Combine toutes les optimisations vues en cours.
 */
public class HeuristicSolver extends AbstractCSPSolver {
    
    @Override
    public BinairoState solve(BinairoState initialState) {
        BinairoState state = new BinairoState(initialState);
        
        if (backtrackWithHeuristics(state)) {
            return state;
        }
        
        return null;
    }
    
    /**
     * Backtracking avec TOUTES les heuristiques.
     */
    private boolean backtrackWithHeuristics(BinairoState state) {
        nodesExplored++;
        
        BinairoGrid grid = state.getGrid();
        
        // Cas de base
        if (grid.isFull()) {
            return BinairoProblem.isValid(grid);
        }
        
        // HEURISTIQUE 1 : MRV + Degree
        // Choisir la case avec le domaine le plus petit et le degré maximal
        int[] cell = findMRVWithDegreeCell(grid);
        
        if (cell == null) {
            return grid.isFull() && BinairoProblem.isValid(grid);
        }
        
        int row = cell[0];
        int col = cell[1];
        
        // Calculer le domaine de cette case
        List<Integer> domain = BinairoProblem.getPossibleValues(grid, row, col);
        
        // Si domaine vide → échec immédiat (détecté par FC)
        if (domain.isEmpty()) {
            backtrackCount++;
            return false;
        }
        
        // HEURISTIQUE 2 : LCV
        // Ordonner les valeurs : essayer d'abord celle qui contraint le moins
        List<Integer> orderedDomain = orderValuesByLCV(grid, row, col, domain);
        
        // Essayer chaque valeur dans l'ordre LCV
        for (int value : orderedDomain) {
            grid.set(row, col, value);
            
            // Vérifier les contraintes locales
            if (BinairoProblem.isConsistentAt(grid, row, col)) {
                
                // HEURISTIQUE 3 : Forward Checking
                // Vérifier que tous les voisins ont encore des valeurs possibles
                if (forwardCheck(grid, row, col)) {
                    // Récursion
                    if (backtrackWithHeuristics(state)) {
                        return true;
                    }
                }
            }
            
            // Backtrack
            grid.set(row, col, -1);
            backtrackCount++;
        }
        
        return false;
    }
    
    /**
     * Forward Checking : vérifie les domaines des voisins.
     */
    private boolean forwardCheck(BinairoGrid grid, int row, int col) {
        int size = grid.getSize();
        
        // Vérifier ligne
        for (int c = 0; c < size; c++) {
            if (grid.isEmpty(row, c)) {
                List<Integer> possible = BinairoProblem.getPossibleValues(grid, row, c);
                if (possible.isEmpty()) {
                    return false;
                }
            }
        }
        
        // Vérifier colonne
        for (int r = 0; r < size; r++) {
            if (grid.isEmpty(r, col)) {
                List<Integer> possible = BinairoProblem.getPossibleValues(grid, r, col);
                if (possible.isEmpty()) {
                    return false;
                }
            }
        }
        
        return true;
    }
}
