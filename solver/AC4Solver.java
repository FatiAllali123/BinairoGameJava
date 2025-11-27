package solver;
import java.util.*;

import model.BinairoGrid;
import model.BinairoProblem;
import model.BinairoState;

/**
 * Solveur avec AC-4 (Arc Consistency 4).
 * 
 * DIFFÉRENCE AVEC AC-3 :
 *   - AC-3 : révise tous les arcs (Xk, Xi) quand Di change
 *   - AC-4 : maintient des compteurs de "support" pour éviter re-vérifications
 *   - Plus complexe mais théoriquement plus efficace
 * 
 * COMPLEXITÉ :
 *   - AC-3 : O(ed³) où e = arcs, d = taille domaine
 *   - AC-4 : O(ed²)
 * 
 * Note : En pratique, AC-3 est souvent plus rapide sur petites grilles
 * car AC-4 a plus d'overhead mémoire.
 */
public class AC4Solver extends AbstractCSPSolver {
    
    // Structure pour (case, valeur)
    private static class Label {
        int row, col, value;
        
        Label(int row, int col, int value) {
            this.row = row;
            this.col = col;
            this.value = value;
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Label)) return false;
            Label other = (Label) o;
            return row == other.row && col == other.col && value == other.value;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(row, col, value);
        }
        
        String getKey() {
            return row + "," + col;
        }
    }
    
    // Domaines
    private Map<String, Set<Integer>> domains;
    
    // Compteurs de support : pour chaque (Xi, a), combien de supports dans Xj
    private Map<Label, Integer> counter;
    
    // Liste de supports : quels (Xj, b) supportent (Xi, a)
    private Map<Label, List<Label>> supportList;
    
    @Override
    public BinairoState solve(BinairoState initialState) {
        BinairoState state = new BinairoState(initialState);
        BinairoGrid grid = state.getGrid();
        
        // Initialiser les domaines
        initializeDomains(grid);
        
        // Appliquer AC-4
        if (!ac4(grid)) {
            return null; // Incohérence détectée
        }
        
        // Appliquer les domaines réduits
        applyReducedDomains(grid);
        
        // Backtracking
        if (backtrack(state)) {
            return state;
        }
        
        return null;
    }
    
    private void initializeDomains(BinairoGrid grid) {
        domains = new HashMap<>();
        int size = grid.getSize();
        
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                String key = row + "," + col;
                Set<Integer> domain = new HashSet<>();
                
                int value = grid.get(row, col);
                if (value == -1) {
                    domain.add(0);
                    domain.add(1);
                } else {
                    domain.add(value);
                }
                
                domains.put(key, domain);
            }
        }
    }
    
    /**
     * Algorithme AC-4.
     * Plus complexe que AC-3 mais théoriquement plus efficace.
     */
    private boolean ac4(BinairoGrid grid) {
        int size = grid.getSize();
        
        // Initialiser les structures
        counter = new HashMap<>();
        supportList = new HashMap<>();
        Queue<Label> queue = new LinkedList<>();
        
        // PHASE 1 : Initialisation des compteurs
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                String key = row + "," + col;
                Set<Integer> domain = domains.get(key);
                
                for (int a : domain) {
                    Label label = new Label(row, col, a);
                    counter.put(label, 0);
                    supportList.put(label, new ArrayList<>());
                    
                    // Compter les supports dans les voisins
                    // Voisins de la même ligne
                    for (int c = 0; c < size; c++) {
                        if (c != col) {
                            countSupports(grid, label, row, c);
                        }
                    }
                    
                    // Voisins de la même colonne
                    for (int r = 0; r < size; r++) {
                        if (r != row) {
                            countSupports(grid, label, r, col);
                        }
                    }
                    
                    // Si aucun support → ajouter à la file
                    if (counter.get(label) == 0) {
                        queue.add(label);
                    }
                }
            }
        }
        
        // PHASE 2 : Propagation
        while (!queue.isEmpty()) {
            Label label = queue.poll();
            
            // Retirer la valeur du domaine
            String key = label.getKey();
            Set<Integer> domain = domains.get(key);
            domain.remove(label.value);
            
            // Si domaine vide → incohérence
            if (domain.isEmpty()) {
                return false;
            }
            
            // Décrémenter les compteurs des labels qui dépendaient de celui-ci
            for (Label dependent : supportList.get(label)) {
                int count = counter.get(dependent);
                counter.put(dependent, count - 1);
                
                if (count - 1 == 0) {
                    queue.add(dependent);
                }
            }
        }
        
        return true;
    }
    
    /**
     * Compte les supports pour un label dans une case voisine.
     */
    private void countSupports(BinairoGrid grid, Label label, int neighborRow, int neighborCol) {
        String neighborKey = neighborRow + "," + neighborCol;
        Set<Integer> neighborDomain = domains.get(neighborKey);
        
        for (int b : neighborDomain) {
            if (isConsistent(grid, label.row, label.col, label.value, 
                           neighborRow, neighborCol, b)) {
                // b supporte a
                counter.put(label, counter.get(label) + 1);
                
                // Ajouter label à la liste de support de (voisin, b)
                Label neighborLabel = new Label(neighborRow, neighborCol, b);
                supportList.computeIfAbsent(neighborLabel, k -> new ArrayList<>()).add(label);
            }
        }
    }
    
    private boolean isConsistent(BinairoGrid grid, int r1, int c1, int v1, 
                                  int r2, int c2, int v2) {
        int old1 = grid.get(r1, c1);
        int old2 = grid.get(r2, c2);
        
        grid.set(r1, c1, v1);
        grid.set(r2, c2, v2);
        
        boolean consistent = BinairoProblem.isConsistentAt(grid, r1, c1)
                          && BinairoProblem.isConsistentAt(grid, r2, c2);
        
        grid.set(r1, c1, old1);
        grid.set(r2, c2, old2);
        
        return consistent;
    }
    
    private void applyReducedDomains(BinairoGrid grid) {
        int size = grid.getSize();
        
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (grid.isEmpty(row, col)) {
                    String key = row + "," + col;
                    Set<Integer> domain = domains.get(key);
                    
                    if (domain.size() == 1) {
                        int value = domain.iterator().next();
                        grid.set(row, col, value);
                    }
                }
            }
        }
    }
    
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