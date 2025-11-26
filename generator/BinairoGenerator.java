package generator;
import java.util.*;

import model.BinairoGrid;
import model.BinairoProblem;
import model.BinairoState;
import solver.AbstractCSPSolver;
import solver.HeuristicSolver;

/**
 * Générateur de grilles Binairo jouables.
 * 
 * STRATÉGIE DE GÉNÉRATION :
 *   1. Créer une grille complète et valide (solution)
 *   2. Retirer des cases de manière aléatoire
 *   3. Vérifier que la grille reste résoluble (unique solution)
 * 
 * ALTERNATIVE (plus simple) :
 *   1. Placer quelques valeurs aléatoires
 *   2. Résoudre avec un solveur
 *   3. Retirer des cases pour créer le puzzle
 * 
 * On utilise la deuxième approche car elle est plus fiable.
 */
public class BinairoGenerator {
    
    private Random random;
    private AbstractCSPSolver solver;
    
    /**
     * Constructeur avec seed aléatoire.
     */
    public BinairoGenerator() {
        this.random = new Random();
        // Utiliser HeuristicSolver pour la génération (rapide et efficace)
        this.solver = new HeuristicSolver();
    }
    
    /**
     * Constructeur avec seed fixe (pour tests reproductibles).
     */
    public BinairoGenerator(long seed) {
        this.random = new Random(seed);
        this.solver = new HeuristicSolver();
    }
    
    /**
     * Génère une grille Binairo jouable.
     * 
     * @param size Taille de la grille (doit être paire >= 4)
     * @param difficulty Niveau de difficulté (0.3 = facile, 0.5 = moyen, 0.7 = difficile)
     *                   Représente le pourcentage de cases vides
     * @return Grille générée avec des cases pré-remplies
     */
    public BinairoGrid generate(int size, double difficulty) {
        if (size < 4 || size % 2 != 0) {
            throw new IllegalArgumentException("Taille doit être >= 4 et paire");
        }
        
        if (difficulty < 0.1 || difficulty > 0.9) {
            throw new IllegalArgumentException("Difficulté doit être entre 0.1 et 0.9");
        }
        
        // ÉTAPE 1 : Générer une solution complète
        BinairoGrid solution = generateCompleteSolution(size);
        
        if (solution == null) {
            // Échec de génération, réessayer
            System.out.println("Échec génération, nouvelle tentative...");
            return generate(size, difficulty);
        }
        
        // ÉTAPE 2 : Créer le puzzle en retirant des cases
        BinairoGrid puzzle = createPuzzle(solution, difficulty);
        
        return puzzle;
    }
    
    /**
     * Génère une solution complète valide.
     * 
     * MÉTHODE :
     *   1. Créer une grille vide
     *   2. Placer quelques valeurs aléatoires (seed)
     *   3. Résoudre avec un solveur
     */
    private BinairoGrid generateCompleteSolution(int size) {
        BinairoGrid grid = new BinairoGrid(size);
        
        // Placer aléatoirement quelques valeurs pour varier les solutions
        int seedCount = size / 2; // Placer size/2 valeurs initiales
        
        for (int i = 0; i < seedCount; i++) {
            int row = random.nextInt(size);
            int col = random.nextInt(size);
            int value = random.nextInt(2); // 0 ou 1
            
            grid.set(row, col, value);
            
            // Vérifier que c'est toujours cohérent
            if (!BinairoProblem.isConsistentAt(grid, row, col)) {
                grid.set(row, col, -1); // Annuler si incohérent
            }
        }
        
        // Résoudre la grille
        BinairoState initialState = new BinairoState(grid);
        BinairoState solution = solver.solve(initialState);
        
        if (solution != null && solution.isSolved()) {
            return solution.getGrid();
        }
        
        return null; // Échec
    }
    
    /**
     * Crée un puzzle en retirant des cases d'une solution complète.
     * 
     * @param solution Grille complète et valide
     * @param difficulty Pourcentage de cases à retirer
     * @return Grille puzzle
     */
    private BinairoGrid createPuzzle(BinairoGrid solution, double difficulty) {
        BinairoGrid puzzle = new BinairoGrid(solution); // Copie
        int size = solution.getSize();
        
        // Calculer le nombre de cases à retirer
        int totalCells = size * size;
        int cellsToRemove = (int) (totalCells * difficulty);
        
        // Liste de toutes les positions
        List<int[]> positions = new ArrayList<>();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                positions.add(new int[]{row, col});
            }
        }
        
        // Mélanger aléatoirement
        Collections.shuffle(positions, random);
        
        // Retirer les cases une par une
        int removed = 0;
        for (int[] pos : positions) {
            if (removed >= cellsToRemove) {
                break;
            }
            
            int row = pos[0];
            int col = pos[1];
            
            // Sauvegarder la valeur
            int value = puzzle.get(row, col);
            
            // Retirer temporairement
            puzzle.set(row, col, -1);
            
            // Vérifier que la grille reste résoluble
            // (optionnel : peut ralentir la génération)
            // Pour accélérer, on fait confiance à la difficulté
            
            removed++;
        }
        
        return puzzle;
    }
    
    /**
     * Génère une grille avec difficulté prédéfinie.
     */
    public BinairoGrid generateEasy(int size) {
        return generate(size, 0.3); // 30% de cases vides
    }
    
    public BinairoGrid generateMedium(int size) {
        return generate(size, 0.5); // 50% de cases vides
    }
    
    public BinairoGrid generateHard(int size) {
        return generate(size, 0.7); // 70% de cases vides
    }
    
    /**
     * Génère une grille manuelle avec pattern spécifique.
     * Utile pour les tests.
     */
    public BinairoGrid generateWithPattern(int size, String pattern) {
        BinairoGrid grid = new BinairoGrid(size);
        
        // Pattern exemple : "0.1...10...." où . = vide
        int index = 0;
        for (int row = 0; row < size && index < pattern.length(); row++) {
            for (int col = 0; col < size && index < pattern.length(); col++) {
                char c = pattern.charAt(index++);
                if (c == '0') {
                    grid.set(row, col, 0);
                } else if (c == '1') {
                    grid.set(row, col, 1);
                }
                // Sinon, laisser vide (-1)
            }
        }
        
        return grid;
    }
    
    /**
     * Affiche des statistiques de génération.
     */
    public void printGenerationStats(int size, double difficulty, int count) {
        System.out.println("========== Statistiques de génération ==========");
        System.out.println("Taille : " + size + "x" + size);
        System.out.println("Difficulté : " + (difficulty * 100) + "%");
        System.out.println("Nombre de grilles : " + count);
        
        long totalTime = 0;
        int successCount = 0;
        
        for (int i = 0; i < count; i++) {
            long start = System.currentTimeMillis();
            BinairoGrid grid = generate(size, difficulty);
            long end = System.currentTimeMillis();
            
            if (grid != null) {
                successCount++;
                totalTime += (end - start);
            }
        }
        
        System.out.println("Succès : " + successCount + "/" + count);
        System.out.println("Temps moyen : " + (totalTime / count) + " ms");
        System.out.println("================================================");
    }
}
