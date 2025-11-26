package model;
/**
 * Classe centrale définissant le problème CSP du Binairo.
 * 
 * Un CSP se compose de :
 *   - VARIABLES : chaque case vide de la grille
 *   - DOMAINES : {0, 1} pour chaque variable
 *   - CONTRAINTES : les 3 règles du Binairo
 * 
 * Cette classe fournit toutes les méthodes de vérification des contraintes
 * utilisées par les différents solveurs.
 */
public class BinairoProblem {
    
    // ==================== RÈGLE 1 : Pas de triplets ====================
    
    /**
     * Vérifie qu'il n'y a pas trois valeurs identiques consécutives.
     * 
     * CONTRAINTE LOCALE : vérifie seulement autour de la case (row, col)
     * pour optimiser les performances.
     * 
     * @param grid La grille à vérifier
     * @param row Ligne de la case modifiée
     * @param col Colonne de la case modifiée
     * @return true si pas de triplet, false sinon
     */
    public static boolean checkNoTripletsAt(BinairoGrid grid, int row, int col) {
        int value = grid.get(row, col);
        if (value == -1) return true; // Case vide, pas de contrainte
        
        int size = grid.getSize();
        
        // Vérification HORIZONTALE (dans la ligne)
        // Pattern 1 : XXX (la case actuelle est à droite)
        if (col >= 2) {
            if (grid.get(row, col - 1) == value && grid.get(row, col - 2) == value) {
                return false; // Triplet trouvé : [val][val][X]
            }
        }
        
        // Pattern 2 : XXX (la case actuelle est au milieu)
        if (col >= 1 && col < size - 1) {
            if (grid.get(row, col - 1) == value && grid.get(row, col + 1) == value) {
                return false; // Triplet trouvé : [val][X][val]
            }
        }
        
        // Pattern 3 : XXX (la case actuelle est à gauche)
        if (col <= size - 3) {
            if (grid.get(row, col + 1) == value && grid.get(row, col + 2) == value) {
                return false; // Triplet trouvé : [X][val][val]
            }
        }
        
        // Vérification VERTICALE (dans la colonne)
        // Pattern 1 : X sur X sur X (case actuelle en bas)
        if (row >= 2) {
            if (grid.get(row - 1, col) == value && grid.get(row - 2, col) == value) {
                return false;
            }
        }
        
        // Pattern 2 : X sur X sur X (case actuelle au milieu)
        if (row >= 1 && row < size - 1) {
            if (grid.get(row - 1, col) == value && grid.get(row + 1, col) == value) {
                return false;
            }
        }
        
        // Pattern 3 : X sur X sur X (case actuelle en haut)
        if (row <= size - 3) {
            if (grid.get(row + 1, col) == value && grid.get(row + 2, col) == value) {
                return false;
            }
        }
        
        return true; // Aucun triplet détecté
    }
    
    /**
     * Vérifie l'absence de triplets dans TOUTE la grille.
     * Utilisé pour validation finale ou vérification globale.
     * 
     * @param grid La grille à vérifier
     * @return true si aucun triplet dans toute la grille
     */
    public static boolean checkNoTripletsGlobal(BinairoGrid grid) {
        int size = grid.getSize();
        
        // Vérifier chaque ligne
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size - 2; col++) {
                int v1 = grid.get(row, col);
                int v2 = grid.get(row, col + 1);
                int v3 = grid.get(row, col + 2);
                
                // Si les 3 cases sont remplies et identiques → violation
                if (v1 != -1 && v1 == v2 && v2 == v3) {
                    return false;
                }
            }
        }
        
        // Vérifier chaque colonne
        for (int col = 0; col < size; col++) {
            for (int row = 0; row < size - 2; row++) {
                int v1 = grid.get(row, col);
                int v2 = grid.get(row + 1, col);
                int v3 = grid.get(row + 2, col);
                
                if (v1 != -1 && v1 == v2 && v2 == v3) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    // ==================== RÈGLE 2 : Équilibre 0/1 ====================
    
    /**
     * Vérifie l'équilibre des 0 et des 1 dans une ligne ou colonne.
     * 
     * Pour une grille PAIRE (ex: 8x8) :
     *   - Exactement size/2 zéros et size/2 uns
     * 
     * Pour une grille IMPAIRE (ex: 7x7) :
     *   - Soit ⌊size/2⌋ et ⌈size/2⌉
     * 
     * @param values Tableau représentant la ligne ou colonne
     * @return true si équilibre respecté ou encore possible
     */
    public static boolean checkBalance(int[] values) {
        int size = values.length;
        int countZeros = 0;
        int countOnes = 0;
        int countEmpty = 0;
        
        // Compter les valeurs
        for (int val : values) {
            if (val == 0) countZeros++;
            else if (val == 1) countOnes++;
            else countEmpty++;
        }
        
        int maxAllowed = (size + 1) / 2; // Pour gérer pair et impair
        
        // Si on a déjà trop de 0 ou trop de 1 → violation
        if (countZeros > maxAllowed || countOnes > maxAllowed) {
            return false;
        }
        
        // Si la ligne/colonne est complète, vérifier l'équilibre parfait
        if (countEmpty == 0) {
            if (size % 2 == 0) {
                // Grille paire : doit être exactement size/2 et size/2
                return countZeros == size / 2 && countOnes == size / 2;
            } else {
                // Grille impaire : différence de 1 acceptable
                return Math.abs(countZeros - countOnes) <= 1;
            }
        }
        
        return true; // Encore des cases vides, équilibre encore possible
    }
    
    /**
     * Vérifie l'équilibre pour UNE ligne spécifique.
     */
    public static boolean checkRowBalance(BinairoGrid grid, int row) {
        return checkBalance(grid.getRow(row));
    }
    
    /**
     * Vérifie l'équilibre pour UNE colonne spécifique.
     */
    public static boolean checkColumnBalance(BinairoGrid grid, int col) {
        return checkBalance(grid.getColumn(col));
    }
    
    /**
     * Vérifie l'équilibre de TOUTES les lignes et colonnes.
     */
    public static boolean checkBalanceGlobal(BinairoGrid grid) {
        int size = grid.getSize();
        
        // Vérifier toutes les lignes
        for (int row = 0; row < size; row++) {
            if (!checkRowBalance(grid, row)) {
                return false;
            }
        }
        
        // Vérifier toutes les colonnes
        for (int col = 0; col < size; col++) {
            if (!checkColumnBalance(grid, col)) {
                return false;
            }
        }
        
        return true;
    }
    
    // ==================== RÈGLE 3 : Unicité ====================
    
    /**
     * Vérifie que deux tableaux sont identiques.
     * Ignore les cases vides (-1) dans la comparaison.
     * 
     * @return true si identiques (pour les cases remplies)
     */
    private static boolean arraysEqual(int[] arr1, int[] arr2) {
        if (arr1.length != arr2.length) return false;
        
        for (int i = 0; i < arr1.length; i++) {
            // Ignorer les cases vides dans la comparaison
            if (arr1[i] == -1 || arr2[i] == -1) continue;
            
            if (arr1[i] != arr2[i]) return false;
        }
        
        return true;
    }
    
    /**
     * Vérifie qu'aucune ligne n'est identique à une autre.
     * Seulement pour les lignes COMPLÈTES (pas de -1).
     */
    public static boolean checkUniqueRows(BinairoGrid grid) {
        int size = grid.getSize();
        
        for (int i = 0; i < size; i++) {
            int[] row1 = grid.getRow(i);
            
            // Vérifier si la ligne est complète
            boolean row1Complete = true;
            for (int val : row1) {
                if (val == -1) {
                    row1Complete = false;
                    break;
                }
            }
            
            if (!row1Complete) continue; // Ligne incomplète, on skip
            
            // Comparer avec toutes les autres lignes complètes
            for (int j = i + 1; j < size; j++) {
                int[] row2 = grid.getRow(j);
                
                // Vérifier si la ligne 2 est complète
                boolean row2Complete = true;
                for (int val : row2) {
                    if (val == -1) {
                        row2Complete = false;
                        break;
                    }
                }
                
                if (!row2Complete) continue;
                
                // Les deux lignes sont complètes : vérifier unicité
                if (arraysEqual(row1, row2)) {
                    return false; // Deux lignes identiques trouvées !
                }
            }
        }
        
        return true;
    }
    
    /**
     * Vérifie qu'aucune colonne n'est identique à une autre.
     */
    public static boolean checkUniqueColumns(BinairoGrid grid) {
        int size = grid.getSize();
        
        for (int i = 0; i < size; i++) {
            int[] col1 = grid.getColumn(i);
            
            // Vérifier si la colonne est complète
            boolean col1Complete = true;
            for (int val : col1) {
                if (val == -1) {
                    col1Complete = false;
                    break;
                }
            }
            
            if (!col1Complete) continue;
            
            // Comparer avec toutes les autres colonnes complètes
            for (int j = i + 1; j < size; j++) {
                int[] col2 = grid.getColumn(j);
                
                boolean col2Complete = true;
                for (int val : col2) {
                    if (val == -1) {
                        col2Complete = false;
                        break;
                    }
                }
                
                if (!col2Complete) continue;
                
                if (arraysEqual(col1, col2)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    // ==================== VÉRIFICATION COMPLÈTE ====================
    
    /**
     * Vérifie TOUTES les contraintes pour une case spécifique.
     * Utilisé après placement d'une valeur dans (row, col).
     * 
     * C'est la méthode la plus utilisée par les solveurs !
     * 
     * @return true si toutes les contraintes sont respectées
     */
    public static boolean isConsistentAt(BinairoGrid grid, int row, int col) {
        // Contrainte 1 : Pas de triplets autour de cette case
        if (!checkNoTripletsAt(grid, row, col)) {
            return false;
        }
        
        // Contrainte 2 : Équilibre dans la ligne et la colonne
        if (!checkRowBalance(grid, row)) {
            return false;
        }
        if (!checkColumnBalance(grid, col)) {
            return false;
        }
        
        // Contrainte 3 : Unicité (vérifiée seulement si ligne/colonne complète)
        // On pourrait optimiser en ne vérifiant que si la ligne/colonne vient d'être complétée
        
        return true;
    }
    
    /**
     * Vérifie si la grille complète est valide (toutes contraintes).
     * Utilisé pour la validation finale de la solution.
     */
    public static boolean isValid(BinairoGrid grid) {
        return checkNoTripletsGlobal(grid) 
            && checkBalanceGlobal(grid) 
            && checkUniqueRows(grid) 
            && checkUniqueColumns(grid);
    }
    
    /**
     * Vérifie si une grille complète est une solution correcte.
     */
    public static boolean isSolution(BinairoGrid grid) {
        return grid.isFull() && isValid(grid);
    }
    
    // ==================== UTILITAIRES POUR HEURISTIQUES ====================
    
    /**
     * Calcule le domaine possible pour une case (row, col).
     * Retourne les valeurs {0, 1} qui ne violent pas les contraintes.
     * 
     * ESSENTIEL pour Forward Checking et LCV !
     * 
     * @return Liste des valeurs possibles (peut être vide si aucune valeur valide)
     */
    public static java.util.List<Integer> getPossibleValues(BinairoGrid grid, int row, int col) {
        java.util.List<Integer> possible = new java.util.ArrayList<>();
        
        if (grid.get(row, col) != -1) {
            // Case déjà remplie, pas de domaine
            return possible;
        }
        
        // Tester 0
        grid.set(row, col, 0);
        if (isConsistentAt(grid, row, col)) {
            possible.add(0);
        }
        
        // Tester 1
        grid.set(row, col, 1);
        if (isConsistentAt(grid, row, col)) {
            possible.add(1);
        }
        
        // Remettre la case vide
        grid.set(row, col, -1);
        
        return possible;
    }
    
    /**
     * Compte le nombre de contraintes affectées par une case.
     * Utilisé pour la DEGREE HEURISTIC.
     * 
     * Une case affecte :
     *   - Sa ligne (size - 1 autres cases)
     *   - Sa colonne (size - 1 autres cases)
     *   - Les contraintes de triplets (jusqu'à 6 voisins)
     */
    public static int getDegree(BinairoGrid grid, int row, int col) {
        int size = grid.getSize();
        int degree = 0;
        
        // Compter les cases vides dans la même ligne
        for (int c = 0; c < size; c++) {
            if (c != col && grid.isEmpty(row, c)) {
                degree++;
            }
        }
        
        // Compter les cases vides dans la même colonne
        for (int r = 0; r < size; r++) {
            if (r != row && grid.isEmpty(r, col)) {
                degree++;
            }
        }
        
        return degree;
    }
}