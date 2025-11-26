package util;
import java.io.*;

import model.BinairoGrid;

/**
 * Utilitaire pour sauvegarder et charger des grilles Binairo.
 * 
 * FORMAT DE FICHIER (texte simple) :
 *   Ligne 1 : taille
 *   Lignes suivantes : valeurs séparées par espaces (. pour vide)
 * 
 * Exemple 4x4 :
 *   4
 *   0 . 1 .
 *   . 1 . 0
 *   1 . 0 .
 *   . 0 . 1
 * 
 * AVANTAGES DE CE FORMAT :
 *   - Lisible par un humain
 *   - Facile à éditer manuellement
 *   - Compatible avec n'importe quel éditeur de texte
 *   - Pas besoin de bibliothèques externes
 */
public class GridSaver {
    
    /**
     * Sauvegarde une grille dans un fichier.
     * 
     * @param grid Grille à sauvegarder
     * @param filename Nom du fichier (extension .txt recommandée)
     * @throws IOException Si erreur d'écriture
     */
    public static void save(BinairoGrid grid, String filename) throws IOException {
        // Utiliser try-with-resources pour fermer automatiquement le fichier
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            int size = grid.getSize();
            
            // LIGNE 1 : Écrire la taille de la grille
            writer.write(String.valueOf(size));
            writer.newLine();
            
            // LIGNES SUIVANTES : Écrire la grille ligne par ligne
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    int value = grid.get(row, col);
                    
                    // Conversion : -1 → "." | 0 → "0" | 1 → "1"
                    if (value == -1) {
                        writer.write(".");
                    } else {
                        writer.write(String.valueOf(value));
                    }
                    
                    // Ajouter un espace entre les valeurs (sauf dernière colonne)
                    if (col < size - 1) {
                        writer.write(" ");
                    }
                }
                writer.newLine();
            }
        }
        // Le fichier est automatiquement fermé grâce à try-with-resources
    }
    
    /**
     * Charge une grille depuis un fichier.
     * 
     * @param filename Nom du fichier
     * @return Grille chargée
     * @throws IOException Si erreur de lecture ou format invalide
     */
    public static BinairoGrid load(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            
            // LIGNE 1 : Lire la taille
            String sizeLine = reader.readLine();
            if (sizeLine == null) {
                throw new IOException("Fichier vide");
            }
            
            int size = Integer.parseInt(sizeLine.trim());
            
            // Vérifier que la taille est valide
            if (size < 4 || size % 2 != 0) {
                throw new IOException("Taille invalide : " + size + " (doit être >= 4 et paire)");
            }
            
            BinairoGrid grid = new BinairoGrid(size);
            
            // LIGNES SUIVANTES : Lire la grille
            for (int row = 0; row < size; row++) {
                String line = reader.readLine();
                
                if (line == null) {
                    throw new IOException("Fichier incomplet : attendu " + size + " lignes, trouvé " + row);
                }
                
                // Diviser la ligne par espaces
                String[] tokens = line.trim().split("\\s+");
                
                if (tokens.length != size) {
                    throw new IOException(
                        "Ligne " + (row + 1) + " invalide : attendu " + size + " valeurs, trouvé " + tokens.length
                    );
                }
                
                // Convertir chaque token en valeur
                for (int col = 0; col < size; col++) {
                    String token = tokens[col];
                    
                    // Conversion : "." → -1 | "0" → 0 | "1" → 1
                    if (token.equals(".")) {
                        grid.set(row, col, -1);
                    } else if (token.equals("0")) {
                        grid.set(row, col, 0);
                    } else if (token.equals("1")) {
                        grid.set(row, col, 1);
                    } else {
                        throw new IOException(
                            "Valeur invalide '" + token + "' à la position (" + row + ", " + col + ")"
                        );
                    }
                }
            }
            
            return grid;
            
        } catch (NumberFormatException e) {
            throw new IOException("Format de fichier invalide : la taille doit être un nombre entier");
        }
    }
    
    /**
     * Vérifie si un fichier existe et est lisible.
     * 
     * @param filename Nom du fichier
     * @return true si le fichier existe et est lisible
     */
    public static boolean fileExists(String filename) {
        File file = new File(filename);
        return file.exists() && file.isFile() && file.canRead();
    }
    
    /**
     * Sauvegarde une grille avec métadonnées (commentaires).
     * Format étendu avec informations supplémentaires.
     * 
     * @param grid Grille à sauvegarder
     * @param filename Nom du fichier
     * @param comment Commentaire optionnel (peut être null)
     * @throws IOException Si erreur d'écriture
     */
    public static void saveWithMetadata(BinairoGrid grid, String filename, String comment) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // En-tête avec métadonnées
            writer.write("# Grille Binairo");
            writer.newLine();
            writer.write("# Générée le : " + java.time.LocalDateTime.now());
            writer.newLine();
            
            if (comment != null && !comment.isEmpty()) {
                writer.write("# " + comment);
                writer.newLine();
            }
            
            writer.newLine();
            
            // Taille
            int size = grid.getSize();
            writer.write(String.valueOf(size));
            writer.newLine();
            
            // Grille
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    int value = grid.get(row, col);
                    
                    if (value == -1) {
                        writer.write(".");
                    } else {
                        writer.write(String.valueOf(value));
                    }
                    
                    if (col < size - 1) {
                        writer.write(" ");
                    }
                }
                writer.newLine();
            }
        }
    }
    
    /**
     * Charge une grille en ignorant les lignes de commentaires (commençant par #).
     * Compatible avec saveWithMetadata().
     * 
     * @param filename Nom du fichier
     * @return Grille chargée
     * @throws IOException Si erreur de lecture
     */
    public static BinairoGrid loadIgnoringComments(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            
            // Ignorer les lignes de commentaires
            do {
                line = reader.readLine();
                if (line == null) {
                    throw new IOException("Fichier vide ou contenant uniquement des commentaires");
                }
                line = line.trim();
            } while (line.startsWith("#") || line.isEmpty());
            
            // La première ligne non-commentaire doit être la taille
            int size = Integer.parseInt(line);
            
            if (size < 4 || size % 2 != 0) {
                throw new IOException("Taille invalide : " + size);
            }
            
            BinairoGrid grid = new BinairoGrid(size);
            
            // Lire la grille
            for (int row = 0; row < size; row++) {
                line = reader.readLine();
                
                // Ignorer les lignes vides ou de commentaires
                while (line != null && (line.trim().isEmpty() || line.trim().startsWith("#"))) {
                    line = reader.readLine();
                }
                
                if (line == null) {
                    throw new IOException("Fichier incomplet");
                }
                
                String[] tokens = line.trim().split("\\s+");
                
                if (tokens.length != size) {
                    throw new IOException("Ligne invalide");
                }
                
                for (int col = 0; col < size; col++) {
                    String token = tokens[col];
                    
                    if (token.equals(".")) {
                        grid.set(row, col, -1);
                    } else if (token.equals("0")) {
                        grid.set(row, col, 0);
                    } else if (token.equals("1")) {
                        grid.set(row, col, 1);
                    } else {
                        throw new IOException("Valeur invalide : " + token);
                    }
                }
            }
            
            return grid;
            
        } catch (NumberFormatException e) {
            throw new IOException("Format invalide : " + e.getMessage());
        }
    }
    
    /**
     * Exemple d'utilisation.
     */
    public static void main(String[] args) {
        try {
            // Créer une grille de test
            BinairoGrid grid = new BinairoGrid(4);
            grid.set(0, 0, 0);
            grid.set(0, 2, 1);
            grid.set(1, 1, 1);
            grid.set(1, 3, 0);
            
            // Sauvegarder
            save(grid, "test_grid.txt");
            System.out.println("Grille sauvegardée dans test_grid.txt");
            
            // Charger
            BinairoGrid loadedGrid = load("test_grid.txt");
            System.out.println("Grille chargée :");
            System.out.println(loadedGrid);
            
            // Sauvegarder avec métadonnées
            saveWithMetadata(grid, "test_grid_metadata.txt", "Grille de test 4x4");
            System.out.println("Grille avec métadonnées sauvegardée");
            
            // Charger avec métadonnées
            BinairoGrid loadedWithMeta = loadIgnoringComments("test_grid_metadata.txt");
            System.out.println("Grille chargée (avec métadonnées) :");
            System.out.println(loadedWithMeta);
            
        } catch (IOException e) {
            System.err.println("Erreur : " + e.getMessage());
        }
    }
}
