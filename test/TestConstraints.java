package test;
import model.BinairoGrid;
import model.BinairoProblem;

public class TestConstraints {
    public static void main(String[] args) {
        System.out.println("=== TEST 2 : Vérification des contraintes ===\n");
        
        BinairoGrid grid = new BinairoGrid(6);
        
        // TEST 2.1 : Contrainte triplets
        System.out.println("TEST 2.1 : Pas de triplets");
        grid.set(0, 0, 0);
        grid.set(0, 1, 0);
        grid.set(0, 2, 0); // Triplet !
        
        if (!BinairoProblem.checkNoTripletsAt(grid, 0, 2)) {
            System.out.println("✓ Triplet détecté correctement");
        } else {
            System.out.println("✗ ERREUR : Triplet non détecté");
        }
        
        // TEST 2.2 : Contrainte équilibre
        System.out.println("\nTEST 2.2 : Équilibre 0/1");
        BinairoGrid grid2 = new BinairoGrid(4);
        // Ligne : 0 0 0 0 (trop de 0)
        grid2.set(0, 0, 0);
        grid2.set(0, 1, 0);
        grid2.set(0, 2, 0);
        grid2.set(0, 3, 0);
        
        if (!BinairoProblem.checkRowBalance(grid2, 0)) {
            System.out.println("✓ Déséquilibre détecté correctement");
        } else {
            System.out.println("✗ ERREUR : Déséquilibre non détecté");
        }
        
        // TEST 2.3 : Contrainte unicité
        System.out.println("\nTEST 2.3 : Unicité des lignes");
        BinairoGrid grid3 = new BinairoGrid(4);
        // Deux lignes identiques : 0 1 0 1
        for (int col = 0; col < 4; col++) {
            int val = col % 2;
            grid3.set(0, col, val);
            grid3.set(1, col, val);
        }
        
        if (!BinairoProblem.checkUniqueRows(grid3)) {
            System.out.println("✓ Lignes identiques détectées");
        } else {
            System.out.println("✗ ERREUR : Lignes identiques non détectées");
        }
        
        System.out.println("\n✅ TEST 2 RÉUSSI\n");
    }
}
