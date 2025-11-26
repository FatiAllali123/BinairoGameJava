public  abstract class AbstractSolver {

    protected Grille grille;

    public AbstractSolver(Grille grille) {
        this.grille = grille;
    }

    // Lancer la résolution
    public abstract boolean resoudre();

    // Retourner la grille résolue
    public Grille getSolution() {
        return grille;
    }
}

