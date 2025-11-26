package test;

import java.io.File;

import model.BinairoGrid;
import util.GridSaver;

public class TestSaveLoad {
    public static void main(String[] args) {
        System.out.println("=== TEST 6 : Sauvegarde et chargement ===\n");
        
        try {
            // Créer une grille
            BinairoGrid original = new BinairoGrid(4);
            original.set(0, 0, 0);
            original.set(0, 2, 1);
            original.set(1, 1, 1);
            original.set(2, 3, 0);
            
            System.out.println("Grille originale :");
            System.out.println(original);
            
            // Sauvegarder
            String filename = "test_save.txt";
            GridSaver.save(original, filename);
            System.out.println("✓ Grille sauvegardée dans " + filename);
            
            // Charger
            BinairoGrid loaded = GridSaver.load(filename);
            System.out.println("\nGrille chargée :");
            System.out.println(loaded);
            
            // Vérifier que les deux sont identiques
            boolean identical = true;
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 4; col++) {
                    if (original.get(row, col) != loaded.get(row, col)) {
                        identical = false;
                        break;
                    }
                }
            }
            
            if (identical) {
                System.out.println("✓ Les grilles sont identiques");
            } else {
                System.out.println("✗ ERREUR : Les grilles diffèrent");
            }
            
            // Nettoyer
            new File(filename).delete();
            System.out.println("✓ Fichier de test supprimé");
            
            System.out.println("\n✅ TEST 6 RÉUSSI\n");
            
        } catch (Exception e) {
            System.out.println("✗ ERREUR : " + e.getMessage());
            e.printStackTrace();
        }
    }
}