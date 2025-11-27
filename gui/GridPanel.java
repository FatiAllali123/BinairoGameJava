package gui;
import javax.swing.*;

import generator.BinairoValidator;
import model.BinairoGrid;
import model.BinairoProblem;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class GridPanel extends JPanel {
    
    private BinairoGrid grid;
    private BinairoGrid initialGrid;
    private BinairoValidator validator;
    
    private int selectedRow = -1;
    private int selectedCol = -1;
    
    // Couleurs premium
    private final Color COLOR_BACKGROUND = new Color(33, 47, 61);
    private final Color COLOR_CELL_EMPTY = new Color(240, 242, 245);
    private final Color COLOR_SELECTED = new Color(52, 152, 219);
    private final Color COLOR_SELECTED_LIGHT = new Color(174, 214, 241);
    private final Color COLOR_FIXED = new Color(44, 62, 80);
    private final Color COLOR_USER = new Color(41, 128, 185);
    private final Color COLOR_VIOLATION = new Color(231, 76, 60);
    private final Color COLOR_GRID = new Color(150, 160, 170);
    private final Color COLOR_GRID_THICK = new Color(52, 73, 94);
    
    /**
     * Constructeur.
     */
    public GridPanel(int size) {
        this.grid = new BinairoGrid(size);
        this.initialGrid = new BinairoGrid(size);
        this.validator = new BinairoValidator();
        
        setBackground(COLOR_BACKGROUND);
        
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
        this.initialGrid = new BinairoGrid(newGrid);
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
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int size = grid.getSize();
        if (size == 0) return;

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int padding = 15;
        int minDim = Math.min(panelWidth, panelHeight) - 2 * padding;
        int cellSize = minDim > 0 ? minDim / size : 0;
        if (cellSize == 0) return;

        int gridSize = cellSize * size;
        int offsetX = (panelWidth - gridSize) / 2;
        int offsetY = (panelHeight - gridSize) / 2;
        
        drawCells(g2d, size, cellSize, offsetX, offsetY);
        drawValues(g2d, size, cellSize, offsetX, offsetY);
        drawViolations(g2d, size, cellSize, offsetX, offsetY);
        drawGrid(g2d, size, cellSize, offsetX, offsetY);
    }
    
    /**
     * Dessine les cases avec coins arrondis et ombres.
     */
    private void drawCells(Graphics2D g2d, int size, int cellSize, int offsetX, int offsetY) {
        int padding = 7;
        int cellSizeWithPadding = cellSize - padding;
        
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int x = col * cellSize + offsetX;
                int y = row * cellSize + offsetY;
                
                Color bgColor;
                if (row == selectedRow && col == selectedCol) {
                    bgColor = COLOR_SELECTED_LIGHT;
                } else if (initialGrid.get(row, col) != -1) {
                    bgColor = new Color(220, 225, 235);
                } else {
                    bgColor = COLOR_CELL_EMPTY;
                }
                
                g2d.setColor(new Color(0, 0, 0, 25));
                g2d.fillRoundRect(x + 3, y + 3, cellSizeWithPadding, cellSizeWithPadding, 10, 10);
                
                g2d.setColor(bgColor);
                g2d.fillRoundRect(x, y, cellSizeWithPadding, cellSizeWithPadding, 10, 10);
                
                if (row == selectedRow && col == selectedCol) {
                    g2d.setColor(COLOR_SELECTED);
                    g2d.setStroke(new BasicStroke(3.5f));
                } else {
                    g2d.setColor(COLOR_GRID);
                    g2d.setStroke(new BasicStroke(1.8f));
                }
                g2d.drawRoundRect(x, y, cellSizeWithPadding, cellSizeWithPadding, 10, 10);
            }
        }
    }
    
    /**
     * Dessine les valeurs avec polices modernes.
     */
    private void drawValues(Graphics2D g2d, int size, int cellSize, int offsetX, int offsetY) {
        Font font = new Font("Segoe UI", Font.BOLD, cellSize / 2 + 2);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        
        int padding = 7;
        int cellSizeWithPadding = cellSize - padding;
        
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int value = grid.get(row, col);
                
                if (value != -1) {
                    String text = String.valueOf(value);
                    
                    g2d.setColor(initialGrid.get(row, col) != -1 ? COLOR_FIXED : COLOR_USER);
                    
                    int x = col * cellSize + offsetX;
                    int y = row * cellSize + offsetY;
                    
                    int textX = x + (cellSizeWithPadding - fm.stringWidth(text)) / 2;
                    int textY = y + (cellSizeWithPadding - fm.getHeight()) / 2 + fm.getAscent();
                    
                    g2d.drawString(text, textX, textY);
                }
            }
        }
    }
    
    /**
     * Dessine les violations avec indicateurs.
     */
    private void drawViolations(Graphics2D g2d, int size, int cellSize, int offsetX, int offsetY) {
        List<BinairoValidator.Violation> violations = validator.findViolations(grid);
        
        int padding = 7;
        int cellSizeWithPadding = cellSize - padding;
        
        for (BinairoValidator.Violation v : violations) {
            int x = v.col * cellSize + offsetX;
            int y = v.row * cellSize + offsetY;
            
            g2d.setColor(new Color(231, 76, 60, 110));
            g2d.fillRoundRect(x, y, cellSizeWithPadding, cellSizeWithPadding, 10, 10);
            
            g2d.setColor(COLOR_VIOLATION);
            g2d.setStroke(new BasicStroke(3.5f));
            g2d.drawRoundRect(x, y, cellSizeWithPadding, cellSizeWithPadding, 10, 10);
        }
    }
    
    /**
     * Dessine la grille.
     */
    private void drawGrid(Graphics2D g2d, int size, int cellSize, int offsetX, int offsetY) {
        int gridSize = size * cellSize;
        
        g2d.setColor(COLOR_GRID_THICK);
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawRect(offsetX, offsetY, gridSize, gridSize);
    }
    
    // ==================== INTERACTIONS ====================
    
    /**
     * Gère les clics de souris.
     */
    private void handleClick(MouseEvent e) {
        requestFocusInWindow();
        
        int size = grid.getSize();
        if (size == 0) return;

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int padding = 15;
        int minDim = Math.min(panelWidth, panelHeight) - 2 * padding;
        int cellSize = minDim > 0 ? minDim / size : 0;
        if (cellSize == 0) return;

        int gridSize = cellSize * size;
        int offsetX = (panelWidth - gridSize) / 2;
        int offsetY = (panelHeight - gridSize) / 2;

        int col = (e.getX() - offsetX) / cellSize;
        int row = (e.getY() - offsetY) / cellSize;
        
        if (row >= 0 && row < size && col >= 0 && col < size) {
            if (initialGrid.get(row, col) != -1) {
                return;
            }
            
            selectedRow = row;
            selectedCol = col;
            
            if (SwingUtilities.isLeftMouseButton(e)) {
                int currentValue = grid.get(row, col);
                int newValue = (currentValue + 2) % 3 - 1;
                grid.set(row, col, newValue);
            } else if (SwingUtilities.isRightMouseButton(e)) {
                grid.set(row, col, -1);
            }
            
            repaint();
            
            if (isSolved()) {
                JOptionPane.showMessageDialog(this, 
                    "Congratulations! Grid solved correctly!",
                    "Victory", JOptionPane.INFORMATION_MESSAGE);
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
        
        if (isSolved()) {
            JOptionPane.showMessageDialog(this, 
                "Congratulations! Grid solved correctly!",
                "Victory", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Affiche un indice (suggestion) pour une case.
     */
    public void showHint() {
        int[] hint = validator.findObviousMove(grid);
        
        if (hint != null) {
            selectedRow = hint[0];
            selectedCol = hint[1];
            int value = hint[2];
            
            String message = String.format(
                "Suggestion: Place %d at (%d, %d)",
                value, hint[0] + 1, hint[1] + 1
            );
            
            JOptionPane.showMessageDialog(this, 
                message,
                "Hint", JOptionPane.INFORMATION_MESSAGE);
            
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, 
                "No obvious suggestion available.",
                "Hint", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
