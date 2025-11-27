package solver;
import java.util.*;

import model.BinairoGrid;
import model.BinairoProblem;
import model.BinairoState;

/**
 * Solveur avec AC-3 (Arc Consistency 3) en pré-traitement.
 * 
 * PRINCIPE :
 *   - AVANT de commencer le backtracking, on réduit les domaines
 *   - AC-3 propage les contraintes pour éliminer les valeurs impossibles
 *   - Réduit drastiquement l'arbre de recherche
 * 
 * ALGORITHME AC-3 :
 *   1. Créer une file d'arcs (contraintes binaires)
 *   2. Pour chaque arc (Xi, Xj) :
 *      - Retirer de Di les valeurs incompatibles avec Dj
 *      - Si Di change, ajouter tous les arcs (Xk, Xi) à la file
 *   3. Recommencer jusqu'à ce que la file soit vide
 * 
 * Dans Binairo, les arcs sont :
 *   - Cases de la même ligne (contraintes de triplets et équilibre)
 *   - Cases de la même colonne (contraintes de triplets et équilibre)
 */
public class AC3Solver extends AbstractCSPSolver {
    
    // Structure pour représenter un arc (contrainte entre 2 cases)
    private static class Arc {
        int row1, col1;  // Première case
        int row2, col2;  // Deuxième case
        
        Arc(int row1, int col1, int row2, int col2) {
            this.row1 = row1;
            this.col1 = col1;
            this.row2 = row2;
            this.col2 = col2;
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Arc)) return false;
            Arc other = (Arc) o;
            return row1 == other.row1 && col1 == other.col1 
                && row2 == other.row2 && col2 == other.col2;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(row1, col1, row2, col2);
        }
    }
    
    // Domaines des cases : Map<"row,col", Set<Integer>>
    private Map<String, Set<Integer>> domains;
    
    @Override
    public BinairoState solve(BinairoState initialState) {
        BinairoState state = new BinairoState(initialState);
        BinairoGrid grid = state.getGrid();
        
        // ÉTAPE 1 : Initialiser les domaines
        initializeDomains(grid);
        
        // ÉTAPE 2 : Appliquer AC-3 pour réduire les domaines
        if (!ac3(grid)) {
            // AC-3 a détecté une incohérence → pas de solution
            return null;
        }
        
        // ÉTAPE 3 : Appliquer les domaines réduits à la grille
        applyReducedDomains(grid);
        
        // ÉTAPE 4 : Backtracking avec les domaines réduits
        if (backtrack(state)) {
            return state;
        }
        
        return null;
    }
    
    /**
     * Initialise les domaines de toutes les cases.
     * Cases vides : {0, 1}
     * Cases remplies : {valeur}
     */
    private void initializeDomains(BinairoGrid grid) {
        domains = new HashMap<>();
        int size = grid.getSize();
        
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                String key = row + "," + col;
                Set<Integer> domain = new HashSet<>();
                
                int value = grid.get(row, col);
                if (value == -1) {
                    // Case vide : domaine {0, 1}
                    domain.add(0);
                    domain.add(1);
                } else {
                    // Case remplie : domaine singleton
                    domain.add(value);
                }
                
                domains.put(key, domain);
            }
        }
    }
    
    /**
     * Algorithme AC-3 (Arc Consistency 3).
     * 
     * @return true si les domaines sont cohérents, false si incohérence détectée
     */
    private boolean ac3(BinairoGrid grid) {
        int size = grid.getSize();
        
        // ÉTAPE 1 : Créer la file d'arcs initiale
        Queue<Arc> queue = new LinkedList<>();
        
        // Ajouter tous les arcs (contraintes binaires)
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                // Arcs avec les cases de la même ligne
                for (int c = 0; c < size; c++) {
                    if (c != col) {
                        queue.add(new Arc(row, col, row, c));
                    }
                }
                
                // Arcs avec les cases de la même colonne
                for (int r = 0; r < size; r++) {
                    if (r != row) {
                        queue.add(new Arc(row, col, r, col));
                    }
                }
            }
        }
        
        // ÉTAPE 2 : Propager les contraintes
        while (!queue.isEmpty()) {
            Arc arc = queue.poll();
            
            // Réviser l'arc (Xi, Xj)
            if (revise(grid, arc)) {
                // Le domaine de Xi a changé
                String key = arc.row1 + "," + arc.col1;
                Set<Integer> domain = domains.get(key);
                
                // Si domaine vide → incohérence détectée
                if (domain.isEmpty()) {
                    return false;
                }
                
                // Ajouter tous les arcs (Xk, Xi) à la file
                for (int c = 0; c < size; c++) {
                    if (c != arc.col1) {
                        queue.add(new Arc(arc.row1, c, arc.row1, arc.col1));
                    }
                }
                for (int r = 0; r < size; r++) {
                    if (r != arc.row1) {
                        queue.add(new Arc(r, arc.col1, arc.row1, arc.col1));
                    }
                }
            }
        }
        
        return true; // Domaines cohérents
    }
    
    /**
     * Révise l'arc (Xi, Xj).
     * Retire de Di les valeurs incompatibles avec Dj.
     * 
     * @return true si Di a changé, false sinon
     */
    private boolean revise(BinairoGrid grid, Arc arc) {
        boolean revised = false;
        
        String key1 = arc.row1 + "," + arc.col1;
        String key2 = arc.row2 + "," + arc.col2;
        
        Set<Integer> domain1 = domains.get(key1);
        Set<Integer> domain2 = domains.get(key2);
        
        // Pour chaque valeur x dans Di
        Set<Integer> toRemove = new HashSet<>();
        for (int x : domain1) {
            // Vérifier s'il existe une valeur y dans Dj compatible avec x
            boolean hasSupport = false;
            
            for (int y : domain2) {
                if (isConsistent(grid, arc.row1, arc.col1, x, arc.row2, arc.col2, y)) {
                    hasSupport = true;
                    break;
                }
            }
            
            // Si aucune valeur compatible → retirer x de Di
            if (!hasSupport) {
                toRemove.add(x);
                revised = true;
            }
        }
        
        domain1.removeAll(toRemove);
        
        return revised;
    }
    
    /**
     * Vérifie si deux affectations sont compatibles.
     * 
     * @param grid Grille actuelle
     * @param r1, c1 Première case avec valeur v1
     * @param v1 Valeur de la première case
     * @param r2, c2 Deuxième case avec valeur v2
     * @param v2 Valeur de la deuxième case
     * @return true si compatible
     */
    private boolean isConsistent(BinairoGrid grid, int r1, int c1, int v1, 
                                  int r2, int c2, int v2) {
        // Sauvegarder les valeurs actuelles
        int old1 = grid.get(r1, c1);
        int old2 = grid.get(r2, c2);
        
        // Placer temporairement les valeurs
        grid.set(r1, c1, v1);
        grid.set(r2, c2, v2);
        
        // Vérifier les contraintes
        boolean consistent = BinairoProblem.isConsistentAt(grid, r1, c1)
                          && BinairoProblem.isConsistentAt(grid, r2, c2);
        
        // Restaurer les valeurs
        grid.set(r1, c1, old1);
        grid.set(r2, c2, old2);
        
        return consistent;
    }
    
    /**
     * Applique les domaines réduits à la grille.
     * Si une case a un domaine singleton, on la remplit.
     */
    private void applyReducedDomains(BinairoGrid grid) {
        int size = grid.getSize();
        
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (grid.isEmpty(row, col)) {
                    String key = row + "," + col;
                    Set<Integer> domain = domains.get(key);
                    
                    // Si domaine singleton → remplir la case
                    if (domain.size() == 1) {
                        int value = domain.iterator().next();
                        grid.set(row, col, value);
                    }
                }
            }
        }
    }
    
    /**
     * Backtracking simple après AC-3.
     */
    private boolean backtrack(BinairoState state) {
        nodesExplored++;
        
        BinairoGrid grid = state.getGrid();
        
        if (grid.isFull()) {
            return BinairoProblem.isValid(grid);
        }
        
        int[] cell = findFirstEmptyCell(grid);
        if (cell == null) {
            return grid.isFull() && BinairoProblem.isValid(grid);
        }
        
        int row = cell[0];
        int col = cell[1];
        
        // Utiliser le domaine réduit par AC-3
        String key = row + "," + col;
        Set<Integer> domain = domains.getOrDefault(key, new HashSet<>(Arrays.asList(0, 1)));
        
        for (int value : domain) {
            grid.set(row, col, value);
            
            if (BinairoProblem.isConsistentAt(grid, row, col)) {
                if (backtrack(state)) {
                    return true;
                }
            }
            
            grid.set(row, col, -1);
            backtrackCount++;
        }
        
        return false;
    }
}