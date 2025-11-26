package gui;
import javax.swing.*;

import generator.BinairoGenerator;
import generator.BinairoValidator;
import model.BinairoGrid;
import model.BinairoState;
import solver.AC3Solver;
import solver.AC4Solver;
import solver.AbstractCSPSolver;
import solver.BacktrackingSolver;
import solver.ForwardCheckingSolver;
import solver.HeuristicSolver;
import solver.MACSolver;
import util.GridSaver;

import java.awt.*;
import java.io.*;

/**
 * Interface graphique principale pour le jeu Binairo.
 * 
 * FONCTIONNALITÉS :
 *   - Nouveau jeu (choix taille + difficulté)
 *   - Résolution automatique (choix du solveur)
 *   - Aide (suggestion)
 *   - Réinitialiser / Effacer
 *   - Sauvegarder / Charger
 *   - Comparaison des solveurs
 */
public class BinairoGUI extends JFrame {
    
    private GridPanel gridPanel;
    private BinairoGenerator generator;
    private BinairoValidator validator;
    
    // Solveurs
    private AbstractCSPSolver currentSolver;
    
    // Composants UI
    private JLabel statusLabel;
    private JComboBox<String> sizeComboBox;
    private JComboBox<String> difficultyComboBox;
    private JComboBox<String> solverComboBox;
    
    /**
     * Constructeur.
     */
    public BinairoGUI() {
        super("Binairo - Jeu de Logique CSP");
        
        this.generator = new BinairoGenerator();
        this.validator = new BinairoValidator();
        this.currentSolver = new HeuristicSolver();
        
        initializeUI();
        
        // Générer une grille par défaut
        generateNewGame(8, 0.5);
    }
    
    /**
     * Initialise l'interface utilisateur.
     */
    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Panel central : grille
        gridPanel = new GridPanel(8);
        add(gridPanel, BorderLayout.CENTER);
        
        // Panel supérieur : contrôles
        add(createControlPanel(), BorderLayout.NORTH);
        
        // Panel inférieur : statut
        add(createStatusPanel(), BorderLayout.SOUTH);
        
        // Panel droit : solveurs
        add(createSolverPanel(), BorderLayout.EAST);
        
        pack();
        setLocationRelativeTo(null);  // Centrer
        setVisible(true);
    }
    
    /**
     * Crée le panneau de contrôles.
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Contrôles"));
        
        // Taille
        panel.add(new JLabel("Taille :"));
        sizeComboBox = new JComboBox<>(new String[]{"4x4", "6x6", "8x8", "10x10", "12x12"});
        sizeComboBox.setSelectedItem("8x8");
        panel.add(sizeComboBox);
        
        // Difficulté
        panel.add(new JLabel("Difficulté :"));
        difficultyComboBox = new JComboBox<>(new String[]{"Facile", "Moyen", "Difficile"});
        difficultyComboBox.setSelectedItem("Moyen");
        panel.add(difficultyComboBox);
        
        // Bouton Nouveau jeu
        JButton newGameButton = new JButton("Nouveau Jeu");
        newGameButton.addActionListener(e -> newGame());
        panel.add(newGameButton);
        
        // Bouton Réinitialiser
        JButton resetButton = new JButton("Réinitialiser");
        resetButton.addActionListener(e -> gridPanel.reset());
        panel.add(resetButton);
        
        // Bouton Effacer
        JButton clearButton = new JButton("Effacer");
        clearButton.addActionListener(e -> gridPanel.clear());
        panel.add(clearButton);
        
        // Bouton Aide
        JButton hintButton = new JButton("Aide");
        hintButton.addActionListener(e -> gridPanel.showHint());
        panel.add(hintButton);
        
        // Bouton Sauvegarder
        JButton saveButton = new JButton("Sauvegarder");
        saveButton.addActionListener(e -> saveGame());
        panel.add(saveButton);
        
        // Bouton Charger
        JButton loadButton = new JButton("Charger");
        loadButton.addActionListener(e -> loadGame());
        panel.add(loadButton);
        
        return panel;
    }
    
    /**
     * Crée le panneau de statut.
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Statut"));
        
        statusLabel = new JLabel("Prêt");
        panel.add(statusLabel);
        
        return panel;
    }
    
    /**
     * Crée le panneau des solveurs.
     */
    private JPanel createSolverPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Résolution Automatique"));
        
        // Choix du solveur
        JPanel solverChoicePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        solverChoicePanel.add(new JLabel("Solveur :"));
        
        solverComboBox = new JComboBox<>(new String[]{
            "Backtracking",
            "Forward Checking",
            "AC-3",
            "AC-4",
            "Heuristiques (MRV+DEG+LCV)",
            "MAC (le plus puissant)"
        });
        solverComboBox.setSelectedIndex(4);  // Heuristiques par défaut
        solverComboBox.addActionListener(e -> updateSolver());
        solverChoicePanel.add(solverComboBox);
        
        panel.add(solverChoicePanel);
        
        // Bouton Résoudre
        JButton solveButton = new JButton("Résoudre Automatiquement");
        solveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        solveButton.addActionListener(e -> solveAutomatically());
        panel.add(Box.createVerticalStrut(10));
        panel.add(solveButton);
        
        // Bouton Comparer
        JButton compareButton = new JButton("Comparer Tous les Solveurs");
        compareButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        compareButton.addActionListener(e -> compareAllSolvers());
        panel.add(Box.createVerticalStrut(10));
        panel.add(compareButton);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    // ==================== ACTIONS ====================
    
    /**
     * Génère un nouveau jeu.
     */
    private void newGame() {
        String sizeStr = (String) sizeComboBox.getSelectedItem();
        int size = Integer.parseInt(sizeStr.split("x")[0]);
        
        String diffStr = (String) difficultyComboBox.getSelectedItem();
        double difficulty = 0.5;
        switch (diffStr) {
            case "Facile": difficulty = 0.3; break;
            case "Moyen": difficulty = 0.5; break;
            case "Difficile": difficulty = 0.7; break;
        }
        
        generateNewGame(size, difficulty);
    }
    
    /**
     * Génère une nouvelle grille.
     */
    private void generateNewGame(int size, double difficulty) {
        statusLabel.setText("Génération en cours...");
        
        // Générer dans un thread séparé pour ne pas bloquer l'UI
        SwingWorker<BinairoGrid, Void> worker = new SwingWorker<>() {
            @Override
            protected BinairoGrid doInBackground() {
                return generator.generate(size, difficulty);
            }
            
            @Override
            protected void done() {
                try {
                    BinairoGrid grid = get();
                    
                    // Recréer le GridPanel avec la nouvelle taille
                    remove(gridPanel);
                    gridPanel = new GridPanel(size);
                    gridPanel.setGrid(grid);
                    add(gridPanel, BorderLayout.CENTER);
                    
                    pack();
                    statusLabel.setText("Nouvelle grille " + size + "x" + size + " générée");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BinairoGUI.this,
                        "Erreur lors de la génération : " + e.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Erreur de génération");
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Met à jour le solveur sélectionné.
     */
    private void updateSolver() {
        int index = solverComboBox.getSelectedIndex();
        
        switch (index) {
            case 0: currentSolver = new BacktrackingSolver(); break;
            case 1: currentSolver = new ForwardCheckingSolver(); break;
            case 2: currentSolver = new AC3Solver(); break;
            case 3: currentSolver = new AC4Solver(); break;
            case 4: currentSolver = new HeuristicSolver(); break;
            case 5: currentSolver = new MACSolver(); break;
        }
    }
    
    /**
     * Résout automatiquement la grille.
     */
    private void solveAutomatically() {
        statusLabel.setText("Résolution en cours...");
        
        BinairoGrid currentGrid = gridPanel.getGrid();
        
        SwingWorker<BinairoState, Void> worker = new SwingWorker<>() {
            long startTime;
            
            @Override
            protected BinairoState doInBackground() {
                BinairoState initialState = new BinairoState(currentGrid);
                startTime = System.currentTimeMillis();
                return currentSolver.solveWithTiming(initialState);
            }
            
            @Override
            protected void done() {
                try {
                    BinairoState solution = get();
                    long endTime = System.currentTimeMillis();
                    
                    if (solution != null && solution.isSolved()) {
                        gridPanel.setGrid(solution.getGrid());
                        
                        String message = String.format(
                            "Résolu en %d ms\nNœuds explorés : %d\nBacktracks : %d",
                            currentSolver.getSolvingTime(),
                            currentSolver.getNodesExplored(),
                            currentSolver.getBacktrackCount()
                        );
                        
                        JOptionPane.showMessageDialog(BinairoGUI.this,
                            message,
                            "Solution trouvée",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        statusLabel.setText("Résolu avec " + currentSolver.getName());
                    } else {
                        JOptionPane.showMessageDialog(BinairoGUI.this,
                            "Aucune solution trouvée",
                            "Échec",
                            JOptionPane.WARNING_MESSAGE);
                        statusLabel.setText("Pas de solution");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BinairoGUI.this,
                        "Erreur : " + e.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Compare tous les solveurs.
     */
    private void compareAllSolvers() {
        BinairoGrid currentGrid = new BinairoGrid(gridPanel.getGrid());
        
        statusLabel.setText("Comparaison en cours...");
        
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                StringBuilder report = new StringBuilder();
                report.append("=== COMPARAISON DES SOLVEURS ===\n\n");
                
                AbstractCSPSolver[] solvers = {
                    new BacktrackingSolver(),
                    new ForwardCheckingSolver(),
                    new AC3Solver(),
                    new AC4Solver(),
                    new HeuristicSolver(),
                    new MACSolver()
                };
                
                for (AbstractCSPSolver solver : solvers) {
                    BinairoState initialState = new BinairoState(currentGrid);
                    BinairoState solution = solver.solveWithTiming(initialState);
                    
                    report.append(solver.getName()).append(" :\n");
                    report.append("  Temps : ").append(solver.getSolvingTime()).append(" ms\n");
                    report.append("  Nœuds : ").append(solver.getNodesExplored()).append("\n");
                    report.append("  Backtracks : ").append(solver.getBacktrackCount()).append("\n");
                    report.append("  Solution : ").append(solution != null ? "OUI" : "NON").append("\n\n");
                }
                
                return report.toString();
            }
            
            @Override
            protected void done() {
                try {
                    String report = get();
                    
                    JTextArea textArea = new JTextArea(report);
                    textArea.setEditable(false);
                    textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                    
                    JScrollPane scrollPane = new JScrollPane(textArea);
                    scrollPane.setPreferredSize(new Dimension(500, 400));
                    
                    JOptionPane.showMessageDialog(BinairoGUI.this,
                        scrollPane,
                        "Comparaison des Solveurs",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    statusLabel.setText("Comparaison terminée");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BinairoGUI.this,
                        "Erreur : " + e.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    // ==================== SAUVEGARDE / CHARGEMENT ====================
    
    /**
     * Sauvegarde la grille.
     */
    private void saveGame() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Sauvegarder la grille");
        
        int result = fileChooser.showSaveDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            try {
                GridSaver.save(gridPanel.getGrid(), file.getAbsolutePath());
                statusLabel.setText("Grille sauvegardée : " + file.getName());
                JOptionPane.showMessageDialog(this,
                    "Grille sauvegardée avec succès",
                    "Sauvegarde",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur de sauvegarde : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Charge une grille.
     */
    private void loadGame() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Charger une grille");
        
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            try {
                BinairoGrid loadedGrid = GridSaver.load(file.getAbsolutePath());
                
                // Recréer le GridPanel avec la nouvelle grille
                int size = loadedGrid.getSize();
                remove(gridPanel);
                gridPanel = new GridPanel(size);
                gridPanel.setGrid(loadedGrid);
                add(gridPanel, BorderLayout.CENTER);
                
                pack();
                statusLabel.setText("Grille chargée : " + file.getName());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur de chargement : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
