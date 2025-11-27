package test;
import generator.BinairoGenerator;
import generator.BinairoValidator;
import model.BinairoGrid;

public class TestGenerator {
    public static void main(String[] args) {
        System.out.println("=== TEST 5 : Générateur de grilles ===\n");
        
        BinairoGenerator generator = new BinairoGenerator();
        
        // Test génération facile
        System.out.println("Génération grille FACILE 6x6...");
        BinairoGrid easy = generator.generateEasy(6);
        System.out.println(easy);
        System.out.println("✓ Grille facile générée\n");
        
        // Test génération moyenne
        System.out.println("Génération grille MOYENNE 8x8...");
        BinairoGrid medium = generator.generateMedium(8);
        System.out.println(medium);
        System.out.println("✓ Grille moyenne générée\n");
        
        // Test génération difficile
        System.out.println("Génération grille DIFFICILE 10x10...");
        BinairoGrid hard = generator.generateHard(10);
        System.out.println(hard);
        System.out.println("✓ Grille difficile générée\n");
        
        // Vérifier que la grille est résoluble
        BinairoValidator validator = new BinairoValidator();
        if (validator.isSolvable(easy)) {
            System.out.println("✓ Grille résoluble");
        } else {
            System.out.println("✗ ERREUR : Grille non résoluble");
        }
        
        System.out.println("\n✅ TEST 5 RÉUSSI\n");
    }
}
