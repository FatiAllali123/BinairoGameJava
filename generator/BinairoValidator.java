package generator;

import model.BinairoGrid;
import model.BinairoProblem;
import model.BinairoState;
import solver.AbstractCSPSolver;
import solver.HeuristicSolver;

/**
 * Validateur de grilles Binairo.
 * 
 * RESPONSABILITÉS :
 *   1. Vérifier la validité d'une grille (respect des contraintes)
 *   2. Vérifier la résolubilité (existe-t-il au moins une solution ?)
 *   3. Vérifier l'unicité de la solution (puzzle bien formé)
 *   4. Détecter les règles violées (pour aide au joueur)
 */
public class BinairoValidator {
    
    private AbstractCSPSolver solver;
    
    /**
     * Constructeur avec solveur par défaut.
     */
    public BinairoValidator() {
        // Utiliser HeuristicSolver pour validation rapide
        this.solver = new HeuristicSolver();
    }
    
    /**
     * Constructeur avec solveur personnalisé.
     */
    public BinairoValidator(AbstractCSPSolver solver) {
        this.solver = solver;
    }
    
    // ==================== VALIDATION COMPLÈTE ====================
    
    /**
     * Vérifie si une grille est valide ET résoluble.
     * 
     * @param grid Grille à valider
     * @return Résultat de validation avec détails
     */
    public ValidationResult validate(BinairoGrid grid) {
        ValidationResult result = new ValidationResult();
        
        // ÉTAPE 1 : Vérifier les contraintes actuelles
        result.constraintsValid = checkConstraints(grid, result);
        
        if (!result.constraintsValid) {
            result.valid = false;
            result.message = "Contraintes violées";
            return result;
        }
        
        // ÉTAPE 2 : Vérifier la résolubilité
        BinairoState initialState = new BinairoState(grid);
        BinairoState solution = solver.solve(initialState);
        
        result.solvable = (solution != null && solution.isSolved());
        
        if (!result.solvable) {
            result.valid = false;
            result.message = "Grille non résoluble";
            return result;
        }
        
        // ÉTAPE 3 : Tout est OK
        result.valid = true;
        result.solution = solution.getGrid();
        result.message = "Grille valide et résoluble";
        
        return result;
    }
    
    /**
     * Vérifie uniquement les contraintes (sans résoudre).
     * Plus rapide que validate().
     */
    public boolean isValid(BinairoGrid grid) {
        ValidationResult dummy = new ValidationResult();
        return checkConstraints(grid, dummy);
    }
    
    /**
     * Vérifie si la grille est résoluble (au moins une solution existe).
     */
    public boolean isSolvable(BinairoGrid grid) {
        BinairoState initialState = new BinairoState(grid);
        BinairoState solution = solver.solve(initialState);
        return solution != null && solution.isSolved();
    }
    
    // ==================== VÉRIFICATION DES CONTRAINTES ====================
    
    /**
     * Vérifie toutes les contraintes et remplit les détails.
     */
    private boolean checkConstraints(BinairoGrid grid, ValidationResult result) {
        boolean valid = true;
        
        // Contrainte 1 : Pas de triplets
        if (!BinairoProblem.checkNoTripletsGlobal(grid)) {
            result.addViolation("Triplets détectés (trois valeurs identiques consécutives)");
            valid = false;
        }
        
        // Contrainte 2 : Équilibre
        if (!BinairoProblem.checkBalanceGlobal(grid)) {
            result.addViolation("Équilibre 0/1 non respecté dans une ligne ou colonne");
            valid = false;
        }
        
        // Contrainte 3 : Unicité (seulement si lignes/colonnes complètes)
        if (!BinairoProblem.checkUniqueRows(grid)) {
            result.addViolation("Deux lignes identiques détectées");
            valid = false;
        }
        
        if (!BinairoProblem.checkUniqueColumns(grid)) {
            result.addViolation("Deux colonnes identiques détectées");
            valid = false;
        }
        
        return valid;
    }
    
    // ==================== DÉTECTION DE VIOLATIONS ====================
    
    /**
     * Trouve toutes les violations dans la grille.
     * Utilisé pour afficher les erreurs au joueur.
     * 
     * @return Liste des positions avec violations
     */
    public java.util.List<Violation> findViolations(BinairoGrid grid) {
        java.util.List<Violation> violations = new java.util.ArrayList<>();
        int size = grid.getSize();
        
        // Vérifier les triplets
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size - 2; col++) {
                int v1 = grid.get(row, col);
                int v2 = grid.get(row, col + 1);
                int v3 = grid.get(row, col + 2);
                
                if (v1 != -1 && v1 == v2 && v2 == v3) {
                    violations.add(new Violation(row, col, "Triplet horizontal"));
                    violations.add(new Violation(row, col + 1, "Triplet horizontal"));
                    violations.add(new Violation(row, col + 2, "Triplet horizontal"));
                }
            }
        }
        
        for (int col = 0; col < size; col++) {
            for (int row = 0; row < size - 2; row++) {
                int v1 = grid.get(row, col);
                int v2 = grid.get(row + 1, col);
                int v3 = grid.get(row + 2, col);
                
                if (v1 != -1 && v1 == v2 && v2 == v3) {
                    violations.add(new Violation(row, col, "Triplet vertical"));
                    violations.add(new Violation(row + 1, col, "Triplet vertical"));
                    violations.add(new Violation(row + 2, col, "Triplet vertical"));
                }
            }
        }
        
        // Vérifier l'équilibre
        for (int row = 0; row < size; row++) {
            if (!BinairoProblem.checkRowBalance(grid, row)) {
                for (int col = 0; col < size; col++) {
                    if (grid.get(row, col) != -1) {
                        violations.add(new Violation(row, col, "Déséquilibre ligne " + row));
                    }
                }
            }
        }
        
        for (int col = 0; col < size; col++) {
            if (!BinairoProblem.checkColumnBalance(grid, col)) {
                for (int row = 0; row < size; row++) {
                    if (grid.get(row, col) != -1) {
                        violations.add(new Violation(row, col, "Déséquilibre colonne " + col));
                    }
                }
            }
        }
        
        return violations;
    }
    
    /**
     * Suggère une valeur valide pour une case.
     * Utilisé pour le système d'aide.
     * 
     * @return Valeur suggérée (0, 1) ou -1 si plusieurs possibilités
     */
    public int suggestValue(BinairoGrid grid, int row, int col) {
        if (grid.get(row, col) != -1) {
            return -1; // Case déjà remplie
        }
        
        java.util.List<Integer> possible = BinairoProblem.getPossibleValues(grid, row, col);
        
        if (possible.size() == 1) {
            return possible.get(0); // Une seule valeur possible → suggérer
        }
        
        return -1; // Plusieurs possibilités ou aucune
    }
    
    /**
     * Trouve la prochaine case évidente à remplir.
     * Utilisé pour le bouton "Aide".
     * 
     * @return Tableau [row, col, value] ou null
     */
    public int[] findObviousMove(BinairoGrid grid) {
        int size = grid.getSize();
        
        // Chercher une case avec domaine singleton
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (grid.isEmpty(row, col)) {
                    java.util.List<Integer> possible = 
                        BinairoProblem.getPossibleValues(grid, row, col);
                    
                    if (possible.size() == 1) {
                        return new int[]{row, col, possible.get(0)};
                    }
                }
            }
        }
        
        return null; // Aucun coup évident
    }
    
    // ==================== CLASSES INTERNES ====================
    
    /**
     * Résultat de validation avec détails.
     */
    public static class ValidationResult {
        public boolean valid = false;
        public boolean constraintsValid = false;
        public boolean solvable = false;
        public BinairoGrid solution = null;
        public String message = "";
        public java.util.List<String> violations = new java.util.ArrayList<>();
        
        public void addViolation(String violation) {
            violations.add(violation);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Validation : ").append(valid ? "OK" : "ÉCHEC").append("\n");
            sb.append("Message : ").append(message).append("\n");
            
            if (!violations.isEmpty()) {
                sb.append("Violations :\n");
                for (String v : violations) {
                    sb.append("  - ").append(v).append("\n");
                }
            }
            
            return sb.toString();
        }
    }
    
    /**
     * Représente une violation à une position donnée.
     */
    public static class Violation {
        public final int row;
        public final int col;
        public final String message;
        
        public Violation(int row, int col, String message) {
            this.row = row;
            this.col = col;
            this.message = message;
        }
        
        @Override
        public String toString() {
            return String.format("(%d, %d): %s", row, col, message);
        }
    }
}
