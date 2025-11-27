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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;

public class BinairoGUI extends JFrame {
    
    // Couleurs modernes - Palette premium
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color PRIMARY_LIGHT = new Color(52, 152, 219);
    private static final Color PRIMARY_DARK = new Color(30, 104, 155);
    private static final Color ACCENT_COLOR = new Color(46, 204, 113);
    private static final Color ACCENT_LIGHT = new Color(76, 224, 138);
    private static final Color DARK_BG = new Color(33, 47, 61);
    private static final Color DARKER_BG = new Color(22, 32, 44);
    private static final Color LIGHT_BG = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(52, 73, 94);
    private static final Color TEXT_LIGHT = new Color(230, 230, 230);
    
    private GridPanel gridPanel;
    private BinairoGenerator generator;
    private BinairoValidator validator;
    private AbstractCSPSolver currentSolver;
    
    private JLabel statusLabel;
    private JComboBox<String> sizeComboBox;
    private JComboBox<String> difficultyComboBox;
    private JComboBox<String> solverComboBox;
    
    public BinairoGUI() {
        super("Binairo - Jeu de Logique CSP");
        
        applyModernTheme();
        
        this.generator = new BinairoGenerator();
        this.validator = new BinairoValidator();
        this.currentSolver = new HeuristicSolver();
        
        initializeUI();
        
        generateNewGame(8, 0.5);
    }
    
    private void applyModernTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Font defaultFont = new Font("Segoe UI", Font.PLAIN, 12);
        Font boldFont = new Font("Segoe UI", Font.BOLD, 13);
        
        UIManager.put("Button.font", boldFont);
        UIManager.put("Label.font", defaultFont);
        UIManager.put("ComboBox.font", defaultFont);
        UIManager.put("TitledBorder.font", boldFont);
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(DARKER_BG);
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        gridPanel = new GridPanel(8);
        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setBackground(DARKER_BG);
        gridWrapper.add(gridPanel, BorderLayout.CENTER);
        add(gridWrapper, BorderLayout.CENTER);
        
        add(createControlPanel(), BorderLayout.NORTH);
        add(createStatusPanel(), BorderLayout.SOUTH);
        add(createSolverPanel(), BorderLayout.EAST);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(0, 0, DARK_BG, getWidth(), 0, DARKER_BG);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(41, 128, 185, 200)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel sizeLabel = createStyledLabel("Taille :", Font.BOLD);
        panel.add(sizeLabel);
        
        sizeComboBox = createStyledComboBox(new String[]{"4x4", "6x6", "8x8", "10x10", "12x12"});
        sizeComboBox.setSelectedItem("8x8");
        panel.add(sizeComboBox);
        
        panel.add(createSeparator());
        
        JLabel diffLabel = createStyledLabel("Difficulte :", Font.BOLD);
        panel.add(diffLabel);
        
        difficultyComboBox = createStyledComboBox(new String[]{"Facile", "Moyen", "Difficile"});
        difficultyComboBox.setSelectedItem("Moyen");
        panel.add(difficultyComboBox);
        
        panel.add(createSeparator());
        
        panel.add(createModernButton("Nouveau", e -> newGame(), PRIMARY_LIGHT));
        panel.add(createModernButton("Reinit", e -> gridPanel.reset(), new Color(155, 89, 182)));
        panel.add(createModernButton("Effacer", e -> gridPanel.clear(), new Color(230, 126, 34)));
        panel.add(createModernButton("Aide", e -> gridPanel.showHint(), ACCENT_LIGHT));
        panel.add(createModernButton("Save", e -> saveGame(), new Color(52, 73, 152)));
        panel.add(createModernButton("Load", e -> loadGame(), new Color(44, 62, 80)));
        
        return panel;
    }
    
    private JLabel createStyledLabel(String text, int fontStyle) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_LIGHT);
        label.setFont(new Font("Segoe UI", fontStyle, 12));
        return label;
    }
    
    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 25));
        sep.setForeground(new Color(100, 100, 100));
        return sep;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(0, 0, DARKER_BG, getWidth(), 0, DARK_BG);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(46, 204, 113, 200)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        statusLabel = new JLabel("✓ Prêt");
        statusLabel.setForeground(ACCENT_LIGHT);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(statusLabel);
        
        return panel;
    }
    
    private JPanel createSolverPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 15, 15);
                
                GradientPaint gradient = new GradientPaint(0, 0, DARK_BG, 0, getHeight(), DARKER_BG);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 15, 15);
                
                super.paintComponent(g);
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));
        panel.setPreferredSize(new Dimension(220, 0));
        
        JLabel titleLabel = new JLabel("AUTO-SOLVE");
        titleLabel.setForeground(PRIMARY_LIGHT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));
        
        JLabel solverLabel = new JLabel("Select Solver:");
        solverLabel.setForeground(TEXT_LIGHT);
        solverLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        solverLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(solverLabel);
        panel.add(Box.createVerticalStrut(8));
        
        solverComboBox = createStyledComboBox(new String[]{
            "Backtracking",
            "Forward Checking",
            "AC-3",
            "AC-4",
            "Heuristiques",
            "MAC (Best)"
        });
        solverComboBox.setSelectedIndex(4);
        solverComboBox.addActionListener(e -> updateSolver());
        solverComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        solverComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(solverComboBox);
        
        panel.add(Box.createVerticalStrut(18));
        
        JButton solveButton = createModernButton("SOLVE", e -> solveAutomatically(), PRIMARY_LIGHT);
        solveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        solveButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        panel.add(solveButton);
        
        panel.add(Box.createVerticalStrut(12));
        
        JButton compareButton = createModernButton("COMPARE", e -> compareAllSolvers(), ACCENT_LIGHT);
        compareButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        compareButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        panel.add(compareButton);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JButton createModernButton(String text, java.awt.event.ActionListener listener, Color baseColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bgColor = getModel().isArmed() ? baseColor.brighter() : baseColor;
                
                g2d.setColor(new Color(0, 0, 0, 40));
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
                
                GradientPaint gradient = new GradientPaint(0, 0, bgColor, 0, getHeight(), baseColor.darker());
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 8, 8);
                
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 8, 8);
                
                super.paintComponent(g);
            }
        };
        
        button.addActionListener(listener);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(10, 15, 10, 15));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });
        
        return button;
    }
    
    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
        };
        
        comboBox.setBackground(new Color(50, 60, 75));
        comboBox.setForeground(TEXT_LIGHT);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Personnaliser le renderer pour les items
        comboBox.setRenderer(new javax.swing.plaf.basic.BasicComboBoxRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                this.setForeground(TEXT_LIGHT);
                this.setBackground(isSelected ? new Color(52, 152, 219) : new Color(50, 60, 75));
                this.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                this.setText(value != null ? value.toString() : "");
                
                return this;
            }
        });
        
        return comboBox;
    }
    
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
    
    private void generateNewGame(int size, double difficulty) {
        statusLabel.setText("Generating...");
        
        SwingWorker<BinairoGrid, Void> worker = new SwingWorker<>() {
            @Override
            protected BinairoGrid doInBackground() {
                return generator.generate(size, difficulty);
            }
            
            @Override
            protected void done() {
                try {
                    BinairoGrid grid = get();
                    
                    Container parent = gridPanel.getParent();
                    if (parent != null) {
                        remove(parent);
                    }
                    
                    gridPanel = new GridPanel(size);
                    gridPanel.setGrid(grid);
                    JPanel gridWrapper = new JPanel(new BorderLayout());
                    gridWrapper.setBackground(DARKER_BG);
                    gridWrapper.add(gridPanel, BorderLayout.CENTER);
                    add(gridWrapper, BorderLayout.CENTER);
                    
                    revalidate();
                    repaint();
                    
                    statusLabel.setText("Grid " + size + "x" + size + " generated");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BinairoGUI.this,
                        "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Generation error");
                }
            }
        };
        
        worker.execute();
    }
    
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
    
    private void solveAutomatically() {
        statusLabel.setText("Solving...");
        
        BinairoGrid currentGrid = gridPanel.getGrid();
        
        SwingWorker<BinairoState, Void> worker = new SwingWorker<>() {
            @Override
            protected BinairoState doInBackground() {
                BinairoState initialState = new BinairoState(currentGrid);
                return currentSolver.solveWithTiming(initialState);
            }
            
            @Override
            protected void done() {
                try {
                    BinairoState solution = get();
                    
                    if (solution != null && solution.isSolved()) {
                        gridPanel.setGrid(solution.getGrid());
                        
                        String message = String.format(
                            "Solved in %d ms\nNodes: %d\nBacktracks: %d",
                            currentSolver.getSolvingTime(),
                            currentSolver.getNodesExplored(),
                            currentSolver.getBacktrackCount()
                        );
                        
                        JOptionPane.showMessageDialog(BinairoGUI.this, message,
                            "Solution Found", JOptionPane.INFORMATION_MESSAGE);
                        
                        statusLabel.setText("Solved with " + currentSolver.getName());
                    } else {
                        JOptionPane.showMessageDialog(BinairoGUI.this,
                            "No solution found", "Failed", JOptionPane.WARNING_MESSAGE);
                        statusLabel.setText("No solution");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BinairoGUI.this,
                        "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void compareAllSolvers() {
        BinairoGrid currentGrid = new BinairoGrid(gridPanel.getGrid());
        statusLabel.setText("Comparing...");
        
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                StringBuilder report = new StringBuilder();
                report.append("====================================\n");
                report.append("   SOLVER COMPARISON REPORT\n");
                report.append("====================================\n\n");
                
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
                    
                    report.append("[").append(solver.getName()).append("]\n");
                    report.append("  Time: ").append(solver.getSolvingTime()).append(" ms\n");
                    report.append("  Nodes: ").append(solver.getNodesExplored()).append("\n");
                    report.append("  Backtracks: ").append(solver.getBacktrackCount()).append("\n");
                    report.append("  Status: ").append(solution != null ? "SUCCESS" : "FAILED").append("\n\n");
                }
                
                return report.toString();
            }
            
            @Override
            protected void done() {
                try {
                    String report = get();
                    
                    JTextArea textArea = new JTextArea(report);
                    textArea.setEditable(false);
                    textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
                    textArea.setBackground(DARKER_BG);
                    textArea.setForeground(ACCENT_LIGHT);
                    textArea.setMargin(new Insets(10, 10, 10, 10));
                    
                    JScrollPane scrollPane = new JScrollPane(textArea);
                    scrollPane.setPreferredSize(new Dimension(550, 420));
                    
                    JOptionPane.showMessageDialog(BinairoGUI.this, scrollPane,
                        "Solver Comparison", JOptionPane.INFORMATION_MESSAGE);
                    
                    statusLabel.setText("Comparison done");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BinairoGUI.this,
                        "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void saveGame() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Grid");
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                GridSaver.save(gridPanel.getGrid(), fileChooser.getSelectedFile().getAbsolutePath());
                statusLabel.setText("Saved: " + fileChooser.getSelectedFile().getName());
                JOptionPane.showMessageDialog(this, "Grid saved successfully", "Save", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Save error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void loadGame() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Grid");
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                BinairoGrid loadedGrid = GridSaver.load(fileChooser.getSelectedFile().getAbsolutePath());
                
                int size = loadedGrid.getSize();
                Container parent = gridPanel.getParent();
                if (parent != null) {
                    remove(parent);
                }
                
                gridPanel = new GridPanel(size);
                gridPanel.setGrid(loadedGrid);
                JPanel gridWrapper = new JPanel(new BorderLayout());
                gridWrapper.setBackground(DARKER_BG);
                gridWrapper.add(gridPanel, BorderLayout.CENTER);
                add(gridWrapper, BorderLayout.CENTER);
                
                revalidate();
                repaint();
                
                statusLabel.setText("Loaded: " + fileChooser.getSelectedFile().getName());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Load error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}