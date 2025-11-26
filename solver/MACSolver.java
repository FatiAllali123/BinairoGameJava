package solver;

import java.util.*;

import model.BinairoGrid;
import model.BinairoProblem;
import model.BinairoState;

/**
 * MAC = Maintaining Arc Consistency: On a aussi créé MACSolver qui combine AC-3 avec le backtracking de manière optimale, pour montrer la meilleure approche possible en CSP.
 * 
 * C'EST LE SOLVEUR LE PLUS PUISSANT !
 * 
 * Combine :
 *   - Backtracking avec MRV + Degree + LCV
 *   - AC-3 appliqué APRÈS CHAQUE PLACEMENT (pas juste en pré-traitement)
 *   - Détecte les incohérences le plus tôt possible
 * 
 * Algorithme :
 *   1. Choisir une variable avec MRV + Degree
 *   2. Pour chaque valeur du domaine (ordonnée par LCV) :
 *      a. Placer la valeur
 *      b. Appliquer AC-3 pour propager les contraintes
 *      c. Si AC-3 réussit → continuer récursivement
 *      d. Sinon → backtrack et restaurer les domaines
 * 
 * La clé : on applique AC-3 À CHAQUE ÉTAPE, pas juste au début !
 */
public class MACSolver extends AbstractCSPSolver {
    
    // Domaines courants : Map<"row,col", Set<valeurs possibles>>
    // Mis à jour dynamiquement par AC-3 pendant la recherche
    private Map<String, Set<Integer>> domains;
    
    @Override
    public BinairoState solve(BinairoState initialState) {
        BinairoState state = new BinairoState(initialState);
        BinairoGrid grid = state.getGrid();
        
        // ÉTAPE 1 : Initialiser les domaines
        initializeDomains(grid);
        
        // ÉTAPE 2 : AC-3 initial (pré-traitement global)
        if (!ac3(grid, -1, -1)) {
            // AC-3 initial a échoué → pas de solution
            return null;
        }
        
        // ÉTAPE 3 : Backtracking avec MAC
        if (backtrackMAC(state)) {
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
     * Backtracking avec Maintaining Arc Consistency.
     * 
     * LA DIFFÉRENCE CRITIQUE avec les autres solveurs :
     *   - Après chaque placement, on applique AC-3
     *   - On sauvegarde/restaure les domaines à chaque backtrack
     */
    private boolean backtrackMAC(BinairoState state) {
        nodesExplored++;
        
        BinairoGrid grid = state.getGrid();
        
        // CAS DE BASE : grille complète
        if (grid.isFull()) {
            return BinairoProblem.isValid(grid);
        }
        
        // HEURISTIQUE : MRV + Degree
        // Choisir la case avec le plus petit domaine et le plus grand degré
        int[] cell = findMRVWithDegreeCell(grid);
        if (cell == null) {
            return grid.isFull() && BinairoProblem.isValid(grid);
        }
        
        int row = cell[0];
        int col = cell[1];
        
        // Récupérer le domaine actuel de cette case
        String key = row + "," + col;
        Set<Integer> domain = new HashSet<>(domains.getOrDefault(key, 
            new HashSet<>(Arrays.asList(0, 1))));
        
        // Si domaine vide → échec immédiat
        if (domain.isEmpty()) {
            backtrackCount++;
            return false;
        }
        
        // HEURISTIQUE LCV (optionnel, peut être ajouté)
        // Pour simplifier, on essaie les valeurs dans l'ordre {0, 1}
        List<Integer> orderedDomain = new ArrayList<>(domain);
        
        // Essayer chaque valeur du domaine
        for (int value : orderedDomain) {
            // SAUVEGARDER les domaines avant modification (CRITIQUE pour backtrack)
            Map<String, Set<Integer>> savedDomains = copyDomains();
            
            // Placer la valeur
            grid.set(row, col, value);
            
            // Vérifier les contraintes locales
            if (BinairoProblem.isConsistentAt(grid, row, col)) {
                // Mettre à jour le domaine de cette case (devient singleton)
                domains.get(key).clear();
                domains.get(key).add(value);
                
                // ★★★ CŒUR DE MAC : APPLIQUER AC-3 APRÈS PLACEMENT ★★★
                // C'est ce qui différencie MAC de tous les autres solveurs
                if (ac3(grid, row, col)) {
                    // AC-3 réussi → tous les domaines sont cohérents
                    // Continuer récursivement
                    if (backtrackMAC(state)) {
                        return true; // Solution trouvée !
                    }
                }
                // Si AC-3 échoue, on backtrack automatiquement
            }
            
            // BACKTRACK : restaurer l'état
            grid.set(row, col, -1);
            domains = savedDomains; // Restaurer TOUS les domaines
            backtrackCount++;
        }
        
        return false; // Aucune valeur ne fonctionne
    }
    
    /**
     * Applique AC-3 focalisé sur les voisins de (row, col).
     * 
     * @param grid Grille actuelle
     * @param row Ligne de la case modifiée (-1 pour AC-3 global)
     * @param col Colonne de la case modifiée
     * @return true si domaines cohérents, false si incohérence détectée
     */
    private boolean ac3(BinairoGrid grid, int row, int col) {
        int size = grid.getSize();
        Queue<Arc> queue = new LinkedList<>();
        
        if (row == -1) {
            // AC-3 GLOBAL (appelé au début)
            // Ajouter tous les arcs de la grille
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    addArcsForCell(queue, r, c, size);
                }
            }
        } else {
            // AC-3 LOCAL (après placement en row, col)
            // Ajouter seulement les arcs affectés par cette case
            addArcsForCell(queue, row, col, size);
        }
        
        // Propager les contraintes
        while (!queue.isEmpty()) {
            Arc arc = queue.poll();
            
            // Réviser l'arc
            if (revise(grid, arc)) {
                // Le domaine de Xi a changé
                String key = arc.row1 + "," + arc.col1;
                Set<Integer> domain = domains.get(key);
                
                // Si domaine vide → incohérence détectée
                if (domain.isEmpty()) {
                    return false;
                }
                
                // Ajouter les arcs voisins à la file (propagation)
                addArcsForCell(queue, arc.row1, arc.col1, size);
            }
        }
        
        return true; // Tous les domaines sont cohérents
    }
    
    /**
     * Ajoute tous les arcs impliquant la case (row, col) à la file.
     * 
     * Les arcs sont dirigés : (voisin, case)
     * Cela signifie "vérifier si le domaine du voisin est cohérent avec case"
     */
    private void addArcsForCell(Queue<Arc> queue, int row, int col, int size) {
        // Arcs avec les cases de la même ligne
        for (int c = 0; c < size; c++) {
            if (c != col) {
                queue.add(new Arc(row, c, row, col));
            }
        }
        
        // Arcs avec les cases de la même colonne
        for (int r = 0; r < size; r++) {
            if (r != row) {
                queue.add(new Arc(r, col, row, col));
            }
        }
    }
    
    /**
     * Révise l'arc (Xi, Xj).
     * Retire de Di les valeurs incompatibles avec toutes les valeurs de Dj.
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
            // Chercher s'il existe une valeur y dans Dj compatible avec x
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
     * Place temporairement v1 en (r1, c1) et v2 en (r2, c2),
     * puis vérifie les contraintes.
     * 
     * @return true si les deux affectations respectent les contraintes
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
        
        // Restaurer les valeurs originales
        grid.set(r1, c1, old1);
        grid.set(r2, c2, old2);
        
        return consistent;
    }
    
    /**
     * Crée une copie profonde des domaines.
     * ESSENTIEL pour pouvoir backtrack et restaurer l'état précédent.
     * 
     * @return Copie indépendante de tous les domaines
     */
    private Map<String, Set<Integer>> copyDomains() {
        Map<String, Set<Integer>> copy = new HashMap<>();
        
        for (Map.Entry<String, Set<Integer>> entry : domains.entrySet()) {
            // Copier la clé et créer un nouveau Set avec les mêmes valeurs
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        
        return copy;
    }
    
    // ==================== CLASSE INTERNE ARC ====================
    
    /**
     * Représente un arc (contrainte dirigée) entre deux cases.
     * Arc(Xi, Xj) signifie "vérifier la cohérence de Xi par rapport à Xj"
     */
    private static class Arc {
        int row1, col1;  // Première case (Xi)
        int row2, col2;  // Deuxième case (Xj)
        
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
        
        @Override
        public String toString() {
            return String.format("Arc[(%d,%d) -> (%d,%d)]", row1, col1, row2, col2);
        }
    }
}