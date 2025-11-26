package gui;
import javax.swing.*;

import generator.BinairoValidator;
import model.BinairoGrid;
import model.BinairoProblem;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Panneau graphique pour afficher et interagir avec la grille Binairo.
 * 
 * FONCTIONNALITÉS :
 *   - Affichage de la grille avec lignes et cases
 *   - Clic pour placer 0 ou 1
 *   - Affichage des violations en rouge
 *   - Cases fixes (grille initiale) en gris
 *   - Cases remplies par le joueur en noir
 */
public class GridPanel extends JPanel {
    
    private BinairoGrid grid;
    private BinairoGrid initialGrid;  // Grille initiale (cases fixes)
    private BinairoValidator validator;
    
    private int cellSize = 60;  // Taille d'une case en pixels
    private int selectedRow = -1;
    private int selectedCol = -1;
    
    // Couleurs
    private final Color COLOR_BACKGROUND = Color.WHITE;
    private final Color COLOR_GRID_LINES = Color.GRAY;
    private final Color COLOR_SELECTED = new Color(200, 230, 255);
    private final Color COLOR_FIXED = Color.DARK_GRAY;
    private final Color COLOR_USER = Color.BLACK;
    private final Color COLOR_VIOLATION = Color.RED;
    private final Color COLOR_HINT = new Color(0, 200, 0);
    
    /**
     * Constructeur.
     */
    public GridPanel(int size) {
        this.grid = new BinairoGrid(size);
        this.initialGrid = new BinairoGrid(size);
        this.validator = new BinairoValidator();
        
        // Configurer le panneau
        int panelSize = size * cellSize + 1;
        setPreferredSize(new Dimension(panelSize, panelSize));
        setBackground(COLOR_BACKGROUND);
        
        // Ajouter les listeners
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e);
            }
        });
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
        
        setFocusable(true);
    }
    
    // ==================== GESTION DE LA GRILLE ====================
    
    /**
     * Définit une nouvelle grille.
     */
    public void setGrid(BinairoGrid newGrid) {
        this.grid = new BinairoGrid(newGrid);
        this.initialGrid = new BinairoGrid(newGrid);  // Sauvegarder comme référence
        this.selectedRow = -1;
        this.selectedCol = -1;
        repaint();
    }
    
    /**
     * Récupère la grille actuelle.
     */
    public BinairoGrid getGrid() {
        return grid;
    }
    
    /**
     * Réinitialise la grille à l'état initial.
     */
    public void reset() {
        this.grid = new BinairoGrid(initialGrid);
        this.selectedRow = -1;
        this.selectedCol = -1;
        repaint();
    }
    
    /**
     * Efface toutes les cases non-fixes.
     */
    public void clear() {
        int size = grid.getSize();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (initialGrid.isEmpty(row, col)) {
                    grid.set(row, col, -1);
                }
            }
        }
        repaint();
    }
    
    /**
     * Vérifie si la grille est résolue correctement.
     */
    public boolean isSolved() {
        return grid.isFull() && BinairoProblem.isValid(grid);
    }
    
    // ==================== AFFICHAGE ====================
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Antialiasing pour un meilleur rendu
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);
        
        int size = grid.getSize();
        
        // Dessiner les cases
        drawCells(g2d, size);
        
        // Dessiner les valeurs
        drawValues(g2d, size);
        
        // Dessiner les violations
        drawViolations(g2d);
        
        // Dessiner la grille
        drawGrid(g2d, size);
    }
    
    /**
     * Dessine les cases (fond + sélection).
     */
    private void drawCells(Graphics2D g2d, int size) {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int x = col * cellSize;
                int y = row * cellSize;
                
                // Fond de la case
                if (row == selectedRow && col == selectedCol) {
                    g2d.setColor(COLOR_SELECTED);
                    g2d.fillRect(x + 1, y + 1, cellSize - 1, cellSize - 1);
                }
            }
        }
    }
    
    /**
     * Dessine les valeurs (0 et 1).
     */
    private void drawValues(Graphics2D g2d, int size) {
        Font font = new Font("Arial", Font.BOLD, cellSize / 2);
        g2d.setFont(font);
        
        FontMetrics fm = g2d.getFontMetrics();
        
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int value = grid.get(row, col);
                
                if (value != -1) {
                    String text = String.valueOf(value);
                    
                    // Choisir la couleur
                    if (initialGrid.get(row, col) != -1) {
                        g2d.setColor(COLOR_FIXED);  // Case fixe
                    } else {
                        g2d.setColor(COLOR_USER);   // Case remplie par le joueur
                    }
                    
                    // Centrer le texte
                    int x = col * cellSize + (cellSize - fm.stringWidth(text)) / 2;
                    int y = row * cellSize + (cellSize - fm.getHeight()) / 2 + fm.getAscent();
                    
                    g2d.drawString(text, x, y);
                }
            }
        }
    }
    
    /**
     * Dessine les violations en rouge.
     */
    private void drawViolations(Graphics2D g2d) {
        List<BinairoValidator.Violation> violations = validator.findViolations(grid);
        
        g2d.setColor(COLOR_VIOLATION);
        g2d.setStroke(new BasicStroke(3));
        
        for (BinairoValidator.Violation v : violations) {
            int x = v.col * cellSize;
            int y = v.row * cellSize;
            
            // Dessiner un cercle rouge autour de la case
            g2d.drawOval(x + 5, y + 5, cellSize - 10, cellSize - 10);
        }
        
        g2d.setStroke(new BasicStroke(1));
    }
    
    /**
     * Dessine la grille (lignes).
     */
    private void drawGrid(Graphics2D g2d, int size) {
        g2d.setColor(COLOR_GRID_LINES);
        
        // Lignes verticales
        for (int col = 0; col <= size; col++) {
            int x = col * cellSize;
            g2d.drawLine(x, 0, x, size * cellSize);
        }
        
        // Lignes horizontales
        for (int row = 0; row <= size; row++) {
            int y = row * cellSize;
            g2d.drawLine(0, y, size * cellSize, y);
        }
    }
    
    // ==================== INTERACTIONS ====================
    
    /**
     * Gère les clics de souris.
     */
    private void handleClick(MouseEvent e) {
        requestFocusInWindow();  // Pour capturer les événements clavier
        
        int col = e.getX() / cellSize;
        int row = e.getY() / cellSize;
        
        int size = grid.getSize();
        
        if (row >= 0 && row < size && col >= 0 && col < size) {
            // Ne pas modifier les cases fixes
            if (initialGrid.get(row, col) != -1) {
                return;
            }
            
            selectedRow = row;
            selectedCol = col;
            
            // Clic gauche : placer 0 ou 1 (cycle)
            if (SwingUtilities.isLeftMouseButton(e)) {
                int currentValue = grid.get(row, col);
                int newValue = (currentValue + 2) % 3 - 1;  // Cycle : -1 → 0 → 1 → -1
                grid.set(row, col, newValue);
            }
            // Clic droit : effacer
            else if (SwingUtilities.isRightMouseButton(e)) {
                grid.set(row, col, -1);
            }
            
            repaint();
            
            // Vérifier si résolu
            if (isSolved()) {
                JOptionPane.showMessageDialog(this, 
                    "Félicitations ! Grille résolue correctement !",
                    "Victoire",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    /**
     * Gère les touches du clavier.
     */
    private void handleKeyPress(KeyEvent e) {
        if (selectedRow == -1 || selectedCol == -1) {
            return;
        }
        
        // Ne pas modifier les cases fixes
        if (initialGrid.get(selectedRow, selectedCol) != -1) {
            return;
        }
        
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_0 || key == KeyEvent.VK_NUMPAD0) {
            grid.set(selectedRow, selectedCol, 0);
            repaint();
        } else if (key == KeyEvent.VK_1 || key == KeyEvent.VK_NUMPAD1) {
            grid.set(selectedRow, selectedCol, 1);
            repaint();
        } else if (key == KeyEvent.VK_DELETE || key == KeyEvent.VK_BACK_SPACE) {
            grid.set(selectedRow, selectedCol, -1);
            repaint();
        } else if (key == KeyEvent.VK_UP && selectedRow > 0) {
            selectedRow--;
            repaint();
        } else if (key == KeyEvent.VK_DOWN && selectedRow < grid.getSize() - 1) {
            selectedRow++;
            repaint();
        } else if (key == KeyEvent.VK_LEFT && selectedCol > 0) {
            selectedCol--;
            repaint();
        } else if (key == KeyEvent.VK_RIGHT && selectedCol < grid.getSize() - 1) {
            selectedCol++;
            repaint();
        }
        
        // Vérifier si résolu
        if (isSolved()) {
            JOptionPane.showMessageDialog(this, 
                "Félicitations ! Grille résolue correctement !",
                "Victoire",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Affiche un indice (suggestion) pour une case.
     */
    public void showHint() {
        int[] hint = validator.findObviousMove(grid);
        
        if (hint != null) {
            int row = hint[0];
            int col = hint[1];
            int value = hint[2];
            
            // Mettre en évidence la case
            selectedRow = row;
            selectedCol = col;
            
            String message = String.format(
                "Suggestion : Placer %d en (%d, %d)",
                value, row + 1, col + 1
            );
            
            JOptionPane.showMessageDialog(this, 
                message,
                "Aide",
                JOptionPane.INFORMATION_MESSAGE);
            
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Aucune suggestion évidente disponible.",
                "Aide",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}