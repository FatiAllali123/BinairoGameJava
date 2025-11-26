import javax.swing.*;

import gui.BinairoGUI;

/**
 * Classe principale pour lancer l'application Binairo.
 * 
 * RESPONSABILITÉ :
 *   - Point d'entrée du programme
 *   - Initialisation de l'interface graphique Swing
 *   - Configuration du Look and Feel
 */
public class Main {
    
    /**
     * Point d'entrée principal de l'application.
     * 
     * @param args Arguments de ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        // Lancer l'interface graphique dans le thread Swing (EDT - Event Dispatch Thread)
        // C'est OBLIGATOIRE pour Swing pour éviter les problèmes de concurrence
        SwingUtilities.invokeLater(() -> {
            try {
                // Essayer d'utiliser le Look and Feel du système d'exploitation
                // Cela rend l'application plus native (Windows, macOS, Linux)
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                System.out.println("Look and Feel : " + UIManager.getLookAndFeel().getName());
                
            } catch (Exception e) {
                // Si erreur, utiliser le Look and Feel par défaut (Metal pour Java)
                System.err.println("Impossible de charger le Look and Feel système");
                System.err.println("Utilisation du Look and Feel par défaut");
                e.printStackTrace();
            }
            
            // Créer et afficher la fenêtre principale
            System.out.println("Démarrage de l'application Binairo...");
            new BinairoGUI();
            System.out.println("Application lancée avec succès !");
        });
    }
}
