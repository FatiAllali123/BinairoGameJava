package test;
import model.BinairoGrid;

public class TestGrid {
    public static void main(String[] args) {
        System.out.println("=== TEST 1 : Création de grille ===");
        
        try {
            BinairoGrid grid = new BinairoGrid(8);
            System.out.println("✓ Grille 8x8 créée");
            System.out.println(grid);
            
            // Test des setters/getters
            grid.set(0, 0, 0);
            grid.set(0, 1, 1);
            grid.set(1, 0, 1);
            
            System.out.println("✓ Valeurs placées");
            System.out.println(grid);
            
            // Test de copie
            BinairoGrid copy = new BinairoGrid(grid);
            copy.set(0, 0, 1);
            
            if (grid.get(0, 0) == 0 && copy.get(0, 0) == 1) {
                System.out.println("✓ Copie profonde fonctionne");
            } else {
                System.out.println("✗ ERREUR : Copie profonde échoue");
            }
            
            System.out.println("\n✅ TEST 1 RÉUSSI\n");
            
        } catch (Exception e) {
            System.out.println("✗ ERREUR : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
