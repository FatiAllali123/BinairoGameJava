package test;

import model.BinairoGrid;
import model.BinairoState;
import generator.BinairoGenerator;
import solver.AbstractCSPSolver;
import solver.BacktrackingSolver;
import solver.HeuristicSolver;
import solver.MACSolver;

/**
 * Test de performance sur grilles difficiles.
 * Compare les performances des principaux solveurs.
 */
public class TestPerformance {
    
    public static void main(String[] args) {
        System.out.println("=== TEST 7 : Performance sur grilles difficiles ===\n");
        
        BinairoGenerator generator = new BinairoGenerator();
        
        // Test sur grille 10x10
        System.out.println("Test sur grille 10x10 difficile...");
        BinairoGrid grid10 = generator.generateHard(10);
        testPerformanceOnGrid(grid10);
        
        // Test sur grille 12x12
        System.out.println("\nTest sur grille 12x12 difficile...");
        BinairoGrid grid12 = generator.generateHard(12);
        testPerformanceOnGrid(grid12);
        
        System.out.println("\n✅ TEST 7 RÉUSSI\n");
    }
    
    private static void testPerformanceOnGrid(BinairoGrid grid) {
        System.out.println("Grille " + grid.getSize() + "x" + grid.getSize());
        System.out.println("Cases vides : " + grid.countEmptyCells());
        
        AbstractCSPSolver[] solvers = {
            new BacktrackingSolver(),
            new HeuristicSolver(),
            new MACSolver()
        };
        
        for (AbstractCSPSolver solver : solvers) {
            BinairoState initialState = new BinairoState(grid);
            BinairoState solution = solver.solveWithTiming(initialState);
            
            if (solution != null) {
                System.out.printf("  %s: %d ms (%d nœuds)%n",
                    solver.getName(),
                    solver.getSolvingTime(),
                    solver.getNodesExplored()
                );
            } else {
                System.out.println("  " + solver.getName() + ": ÉCHEC");
            }
        }
    }
}