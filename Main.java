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
                // Utiliser le Look and Feel Nimbus pour un style moderne et unifié
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
                System.out.println("Look and Feel : " + UIManager.getLookAndFeel().getName());

            } catch (Exception e) {
                // En cas d'erreur, on se rabat sur le L&F du système
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex) {
                    System.err.println("Impossible de charger le Look and Feel Nimbus ou Système.");
                    e.printStackTrace();
                }
            }
            
            // Créer et afficher la fenêtre principale
            System.out.println("Démarrage de l'application Binairo...");
            new BinairoGUI();
            System.out.println("Application lancée avec succès !");
        });
    }
}
