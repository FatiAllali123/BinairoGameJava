package test;
import model.BinairoGrid;
import model.BinairoProblem;
import model.BinairoState;
import solver.BacktrackingSolver;

public class TestBacktracking {
    public static void main(String[] args) {
        System.out.println("=== TEST 3 : Backtracking simple ===\n");
        
        // Grille 4x4 facile
        BinairoGrid grid = new BinairoGrid(4);
        grid.set(0, 0, 0);
        grid.set(0, 2, 1);
        grid.set(1, 1, 1);
        grid.set(2, 3, 0);
        
        System.out.println("Grille initiale :");
        System.out.println(grid);
        
        BinairoState initialState = new BinairoState(grid);
        BacktrackingSolver solver = new BacktrackingSolver();
        
        System.out.println("Résolution en cours...\n");
        BinairoState solution = solver.solveWithTiming(initialState);
        
        if (solution != null && solution.isSolved()) {
            System.out.println("✓ Solution trouvée !");
            System.out.println(solution);
            System.out.println("Nœuds explorés : " + solver.getNodesExplored());
            System.out.println("Backtracks : " + solver.getBacktrackCount());
            System.out.println("Temps : " + solver.getSolvingTime() + " ms");
            
            // Vérifier la validité
            if (BinairoProblem.isValid(solution.getGrid())) {
                System.out.println("✓ Solution valide !");
            } else {
                System.out.println("✗ ERREUR : Solution invalide");
            }
        } else {
            System.out.println("✗ ERREUR : Pas de solution trouvée");
        }
        
        System.out.println("\n✅ TEST 3 RÉUSSI\n");
    }
}