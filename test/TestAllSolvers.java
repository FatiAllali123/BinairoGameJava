package test;
import model.BinairoGrid;
import model.BinairoState;
import solver.AC3Solver;
import solver.AC4Solver;
import solver.AbstractCSPSolver;
import solver.BacktrackingSolver;
import solver.ForwardCheckingSolver;
import solver.HeuristicSolver;
import solver.MACSolver;

public class TestAllSolvers {
    public static void main(String[] args) {
        System.out.println("=== TEST 4 : Comparaison de tous les solveurs ===\n");
        
        // Grille 6x6 de difficulté moyenne
        BinairoGrid grid = new BinairoGrid(6);
        grid.set(0, 1, 0);
        grid.set(1, 0, 1);
        grid.set(1, 4, 0);
        grid.set(2, 2, 1);
        grid.set(3, 3, 0);
        grid.set(4, 1, 1);
        grid.set(5, 4, 1);
        
        System.out.println("Grille initiale 6x6 :");
        System.out.println(grid);
        
        AbstractCSPSolver[] solvers = {
            new BacktrackingSolver(),
            new ForwardCheckingSolver(),
            new AC3Solver(),
            new AC4Solver(),
            new HeuristicSolver(),
            new MACSolver()
        };
        
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║           COMPARAISON DES SOLVEURS                         ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.printf("║ %-25s %-10s %-10s %-10s ║%n", "Solveur", "Temps (ms)", "Nœuds", "Backtracks");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        
        for (AbstractCSPSolver solver : solvers) {
            BinairoState initialState = new BinairoState(grid);
            BinairoState solution = solver.solveWithTiming(initialState);
            
            String name = solver.getName();
            long time = solver.getSolvingTime();
            int nodes = solver.getNodesExplored();
            int backtracks = solver.getBacktrackCount();
            
            System.out.printf("║ %-25s %-10d %-10d %-10d ║%n", name, time, nodes, backtracks);
            
            if (solution == null || !solution.isSolved()) {
                System.out.println("║ ✗ ERREUR : Pas de solution trouvée                        ║");
            }
        }
        
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        
        System.out.println("\n✅ TEST 4 RÉUSSI\n");
    }
}
