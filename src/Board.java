import java.io.Serializable;

/**
 * Represents the game board as a 2D grid where dominoes are placed.
 * Each cell in the grid either holds a Domino or is null (empty).
 *
 * A domino occupies one cell. The left value faces the cell to its left,
 * and the right value faces the cell to its right. When checking matches,
 * we compare the touching edges of adjacent dominoes.
 */
public class Board implements Serializable {

    private static final long serialVersionUID = 1L;

    private Domino[][] grid;
    private int rows;
    private int cols;
    private int tilesPlaced;

    /**
     * Creates an empty board grid.
     * @param rows number of rows
     * @param cols number of columns
     */
    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Domino[rows][cols];
        this.tilesPlaced = 0;
    }

    /**
     * Checks if a domino can be legally placed at position (row, col).
     *
     * Rules:
     * - The cell must be empty.
     * - If the board is empty (first tile), any placement is valid.
     * - Otherwise, at least one adjacent cell must contain a domino,
     *   and the touching sides must have matching numbers.
     *
     * @param d   the domino to place
     * @param row target row
     * @param col target column
     * @return true if placement is valid
     */
    public boolean isValidPlacement(Domino d, int row, int col) {
        return getInvalidReason(d, row, col) == null;
    }

    /**
     * Returns null if the placement is legal, or a short human-readable
     * reason if it isn't. Used by the GUI to explain why a click failed.
     *
     * Vertical matching convention: left = top face, right = bottom face.
     */
    public String getInvalidReason(Domino d, int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return "Out of bounds";
        }
        if (grid[row][col] != null) {
            return "That cell is already taken";
        }

        // First tile on the board is always valid
        if (tilesPlaced == 0) {
            return null;
        }

        boolean hasAdjacentMatch = false;

        // Left neighbor: our left value must match neighbor's right value
        if (col > 0 && grid[row][col - 1] != null) {
            if (d.getLeft() == grid[row][col - 1].getRight()) {
                hasAdjacentMatch = true;
            } else {
                return "Left side (" + d.getLeft() + ") doesn't match neighbor ("
                    + grid[row][col - 1].getRight() + ")";
            }
        }

        // Right neighbor: our right value must match neighbor's left value
        if (col < cols - 1 && grid[row][col + 1] != null) {
            if (d.getRight() == grid[row][col + 1].getLeft()) {
                hasAdjacentMatch = true;
            } else {
                return "Right side (" + d.getRight() + ") doesn't match neighbor ("
                    + grid[row][col + 1].getLeft() + ")";
            }
        }

        // Top neighbor: our top (left) must match neighbor's bottom (right)
        if (row > 0 && grid[row - 1][col] != null) {
            if (d.getLeft() == grid[row - 1][col].getRight()) {
                hasAdjacentMatch = true;
            } else {
                return "Top side (" + d.getLeft() + ") doesn't match neighbor ("
                    + grid[row - 1][col].getRight() + ")";
            }
        }

        // Bottom neighbor: our bottom (right) must match neighbor's top (left)
        if (row < rows - 1 && grid[row + 1][col] != null) {
            if (d.getRight() == grid[row + 1][col].getLeft()) {
                hasAdjacentMatch = true;
            } else {
                return "Bottom side (" + d.getRight() + ") doesn't match neighbor ("
                    + grid[row + 1][col].getLeft() + ")";
            }
        }

        if (!hasAdjacentMatch) {
            return "Tile must touch an existing tile";
        }
        return null;
    }

    /**
     * Places a domino at the given position if the move is valid.
     * @return true if the domino was placed, false if placement was invalid
     */
    public boolean placeDomino(Domino d, int row, int col) {
        if (!isValidPlacement(d, row, col)) {
            return false;
        }
        grid[row][col] = d;
        tilesPlaced++;
        return true;
    }

    /**
     * Calculates points earned for placing a domino at (row, col).
     * Points = sum of the matching numbers on touching edges.
     *
     * Call this AFTER placeDomino so the tile is already on the grid.
     */
    public int calculatePoints(Domino d, int row, int col) {
        int points = 0;

        // Left neighbor match
        if (col > 0 && grid[row][col - 1] != null) {
            if (d.getLeft() == grid[row][col - 1].getRight()) {
                points += d.getLeft(); // or += d.getLeft() * 2 for both sides
            }
        }

        // Right neighbor match
        if (col < cols - 1 && grid[row][col + 1] != null) {
            if (d.getRight() == grid[row][col + 1].getLeft()) {
                points += d.getRight();
            }
        }

        // Top neighbor match (left = top face)
        if (row > 0 && grid[row - 1][col] != null) {
            if (d.getLeft() == grid[row - 1][col].getRight()) {
                points += d.getLeft();
            }
        }

        // Bottom neighbor match (right = bottom face)
        if (row < rows - 1 && grid[row + 1][col] != null) {
            if (d.getRight() == grid[row + 1][col].getLeft()) {
                points += d.getRight();
            }
        }

        // Minimum 1 point for any valid placement (optional design choice)
        if (points == 0 && tilesPlaced == 1) {
            points = d.getTotal(); // first tile gets its own total as points
        }

        return points;
    }

    /**
     * Returns the domino at the given position, or null if empty.
     */
    public Domino getDominoAt(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return grid[row][col];
        }
        return null;
    }

    /**
     * Checks if any empty cell adjacent to an existing tile remains.
     */
    public boolean isFull() {
        if (tilesPlaced == 0) return false;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == null && hasAdjacentTile(r, c)) {
                    return false; // at least one playable empty cell exists
                }
            }
        }
        return true;
    }

    /**
     * Returns true if the cell at (row, col) has at least one non-null neighbor.
     */
    private boolean hasAdjacentTile(int row, int col) {
        if (row > 0 && grid[row - 1][col] != null) return true;
        if (row < rows - 1 && grid[row + 1][col] != null) return true;
        if (col > 0 && grid[row][col - 1] != null) return true;
        if (col < cols - 1 && grid[row][col + 1] != null) return true;
        return false;
    }

    /**
     * Checks if a given domino has any valid placement on the board.
     * Used by Player.hasPlayableTile().
     */
    public boolean hasValidMoveForDomino(Domino d) {
        if (tilesPlaced == 0) return true; // first move is always valid

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == null) {
                    // Try original orientation
                    if (isValidPlacement(d, r, c)) {
                        return true;
                    }
                    // Try flipped orientation
                    d.flip();
                    if (isValidPlacement(d, r, c)) {
                        d.flip(); // flip back
                        return true;
                    }
                    d.flip(); // flip back
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any player can make any move.
     */
    public boolean hasValidMove(Player p) {
        return p.hasPlayableTile(this);
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getTilesPlaced() {
        return tilesPlaced;
    }

    public boolean isEmpty() {
        return tilesPlaced == 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Board (").append(tilesPlaced).append(" tiles placed):\n");
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] != null) {
                    sb.append(grid[r][c].toString());
                } else {
                    sb.append("[ | ]");
                }
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
