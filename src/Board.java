/**
 * Represents the game board as a 2D grid where dominoes are placed.
 * Each cell in the grid either holds a Domino or is null (empty).
 *
 * A domino occupies one cell. The left value faces the cell to its left,
 * and the right value faces the cell to its right. When checking matches,
 * we compare the touching edges of adjacent dominoes.
 */
public class Board {

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
        // Cell must be in bounds and empty
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return false;
        }
        if (grid[row][col] != null) {
            return false;
        }

        // First tile on the board: always valid
        if (tilesPlaced == 0) {
            return true;
        }

        // Must be adjacent to at least one existing tile with a matching edge
        boolean hasAdjacentMatch = false;

        // Check left neighbor: our left value must match neighbor's right value
        if (col > 0 && grid[row][col - 1] != null) {
            if (d.getLeft() == grid[row][col - 1].getRight()) {
                hasAdjacentMatch = true;
            } else {
                return false; // adjacent but doesn't match = invalid
            }
        }

        // Check right neighbor: our right value must match neighbor's left value
        if (col < cols - 1 && grid[row][col + 1] != null) {
            if (d.getRight() == grid[row][col + 1].getLeft()) {
                hasAdjacentMatch = true;
            } else {
                return false;
            }
        }

        // TODO: If you want vertical matching too, add checks for
        //       top neighbor (row-1) and bottom neighbor (row+1).
        //       You'll need to decide which value faces up vs down.
        //       For simplicity, you can start with horizontal-only matching.

        // Check top neighbor
        if (row > 0 && grid[row - 1][col] != null) {
            // For vertical adjacency, decide on a convention:
            // e.g. top neighbor's "bottom-facing" value matches our "top-facing" value
            // For now, just require adjacency without directional match:
            hasAdjacentMatch = true;
        }

        // Check bottom neighbor
        if (row < rows - 1 && grid[row + 1][col] != null) {
            hasAdjacentMatch = true;
        }

        return hasAdjacentMatch;
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

        // TODO: add vertical match points if supporting vertical adjacency

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
