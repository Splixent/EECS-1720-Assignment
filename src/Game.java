import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * Controls game flow: initialization, turns, scoring, and win detection.
 * Coordinates Domino, Player, and Board objects.
 */
public class Game implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private ArrayList<Player> players;
    private int currentPlayerIndex;
    private Board board;
    private ArrayList<Domino> drawPile;
    private boolean gameOver;

    // Constants
    public static final int BOARD_ROWS = 9;
    public static final int BOARD_COLS = 9;
    public static final int TILES_PER_PLAYER_2P = 7;
    public static final int TILES_PER_PLAYER_3P = 5;
    public static final int MAX_PIP_VALUE = 6; // double-six set

    /**
     * Creates a new game with the given number of players.
     * Generates all 28 tiles, shuffles them, and deals hands.
     */
    public Game(int numPlayers) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.players = new ArrayList<>();
        this.board = new Board(BOARD_ROWS, BOARD_COLS);
        this.drawPile = new ArrayList<>();
        this.gameOver = false;
        this.currentPlayerIndex = 0;

        // Create players
        for (int i = 1; i <= numPlayers; i++) {
            players.add(new Player("Player " + i, i));
        }

        // Generate and shuffle all tiles
        generateAllTiles();
        Collections.shuffle(drawPile);

        // Deal hands
        int tilesPerPlayer = (numPlayers == 2) ? TILES_PER_PLAYER_2P : TILES_PER_PLAYER_3P;
        dealHands(tilesPerPlayer);
    }

    /**
     * Creates all 28 domino tiles for a double-six set.
     * Combinations: (0|0), (0|1), ..., (0|6), (1|1), (1|2), ..., (6|6).
     */
    private void generateAllTiles() {
        drawPile.clear();
        for (int i = 0; i <= MAX_PIP_VALUE; i++) {
            for (int j = i; j <= MAX_PIP_VALUE; j++) {
                drawPile.add(new Domino(i, j));
            }
        }
    }

    /**
     * Deals the specified number of tiles to each player from the draw pile.
     */
    private void dealHands(int tilesPerPlayer) {
        for (Player p : players) {
            for (int i = 0; i < tilesPerPlayer; i++) {
                if (!drawPile.isEmpty()) {
                    p.addToHand(drawPile.remove(0));
                }
            }
        }
    }

    /**
     * Resets and starts a new game. Shuffles, deals, sets first player.
     */
    public void startGame() {
        // Clear player hands and scores
        for (Player p : players) {
            p.getHand().clear();
            p.resetScore();
        }

        // Re-generate and shuffle tiles
        generateAllTiles();
        Collections.shuffle(drawPile);

        // Deal hands
        int tilesPerPlayer = (players.size() == 2) ? TILES_PER_PLAYER_2P : TILES_PER_PLAYER_3P;
        dealHands(tilesPerPlayer);

        // Reset board
        this.board = new Board(BOARD_ROWS, BOARD_COLS);
        this.currentPlayerIndex = 0;
        this.gameOver = false;
    }

    /**
     * Ends the game and marks it as over.
     */
    public void endGame() {
        this.gameOver = true;
    }

    /**
     * Advances to the next player's turn.
     */
    public void switchTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    /**
     * Returns the player whose turn it is.
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /**
     * Draws one tile from the draw pile and adds it to the player's hand.
     * @return the drawn Domino, or null if the draw pile is empty
     */
    public Domino drawTile(Player p) {
        if (!drawPile.isEmpty()) {
            Domino d = drawPile.remove(0);
            p.addToHand(d);
            return d;
        }
        return null;
    }

    /**
     * Attempts to place a domino from the current player's hand onto the board.
     *
     * @param handIndex index of the domino in the player's hand
     * @param row       target row on the board
     * @param col       target column on the board
     * @return true if the domino was successfully placed
     */
    public boolean playDomino(int handIndex, int row, int col) {
        Player p = getCurrentPlayer();
        Domino d = p.getDomino(handIndex);
        if (d == null) return false;

        if (board.placeDomino(d, row, col)) {
            // Calculate and award points
            int points = board.calculatePoints(d, row, col);
            p.addScore(points);

            // Remove from player's hand
            p.removeFromHand(handIndex);

            // Check if this player won (empty hand)
            if (p.getHandSize() == 0) {
                endGame();
                return true;
            }

            // Switch to next player
            switchTurn();

            // Check if game should end (no one can play)
            if (checkGameOver()) {
                endGame();
            }

            return true;
        }

        return false;
    }

    /**
     * Checks if the game should end:
     * - No player can make a valid move AND the draw pile is empty.
     */
    public boolean checkGameOver() {
        if (gameOver) return true;

        // If there are tiles to draw, game continues
        if (!drawPile.isEmpty()) return false;

        // Check if any player can make a move
        for (Player p : players) {
            if (p.hasPlayableTile(board)) {
                return false;
            }
        }

        // No one can play and pile is empty
        return true;
    }

    /**
     * Determines the winner.
     * - If a player has an empty hand, they win.
     * - Otherwise, highest score wins.
     * - Tie broken by lowest remaining tile total.
     */
    public Player checkWinner() {
        // Check if someone emptied their hand
        for (Player p : players) {
            if (p.getHandSize() == 0) {
                return p;
            }
        }

        // Otherwise: highest score, tiebreak by lowest remaining total
        Player winner = players.get(0);
        for (int i = 1; i < players.size(); i++) {
            Player p = players.get(i);
            if (p.getScore() > winner.getScore()) {
                winner = p;
            } else if (p.getScore() == winner.getScore()) {
                if (p.getRemainingTotal() < winner.getRemainingTotal()) {
                    winner = p;
                }
            }
        }
        return winner;
    }

    // --- Getters ---

    public String getId() {
        return id;
    }

    public Board getBoard() {
        return board;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public int getDrawPileSize() {
        return drawPile.size();
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Game ").append(id).append(" ===\n");
        sb.append("Draw pile: ").append(drawPile.size()).append(" tiles remaining\n");
        sb.append("Current turn: ").append(getCurrentPlayer().getName()).append("\n\n");
        for (Player p : players) {
            sb.append(p.toString()).append("\n");
        }
        sb.append("\n").append(board.toString());
        return sb.toString();
    }
}
