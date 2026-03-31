import java.util.ArrayList;

/**
 * Represents a player with a hand of dominoes and a score.
 */
public class Player {

    private String name;
    private int playerNumber;
    private ArrayList<Domino> hand;
    private int score;

    /**
     * Creates a player with an empty hand and 0 score.
     * @param name player display name
     * @param num  player number (1, 2, etc.)
     */
    public Player(String name, int num) {
        this.name = name;
        this.playerNumber = num;
        this.hand = new ArrayList<>();
        this.score = 0;
    }

    /**
     * Adds a domino to the player's hand.
     */
    public void addToHand(Domino d) {
        hand.add(d);
    }

    /**
     * Removes and returns the domino at the given index.
     * @param index position in the hand (0-based)
     * @return the removed Domino
     */
    public Domino removeFromHand(int index) {
        if (index >= 0 && index < hand.size()) {
            return hand.remove(index);
        }
        return null;
    }

    /**
     * Returns the domino at the given index without removing it.
     */
    public Domino getDomino(int index) {
        if (index >= 0 && index < hand.size()) {
            return hand.get(index);
        }
        return null;
    }

    public int getHandSize() {
        return hand.size();
    }

    public ArrayList<Domino> getHand() {
        return hand;
    }

    /**
     * Checks if any domino in the player's hand can be legally placed on the board.
     * @param board the game board to check against
     * @return true if at least one valid move exists
     */
    public boolean hasPlayableTile(Board board) {
        // TODO (@Member A): Verify this works correctly once vertical matching
        //   is added to Board.isValidPlacement(). Test with edge cases:
        //   empty board, full board, hand with no matching tiles.
        for (Domino d : hand) {
            if (board.hasValidMoveForDomino(d)) {
                return true;
            }
        }
        return false;
    }

    public void addScore(int pts) {
        this.score += pts;
    }

    public int getScore() {
        return score;
    }

    public String getName() {
        return name;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    /**
     * Sum of all tile values left in hand. Used for end-game tiebreak:
     * lower remaining total wins.
     */
    public int getRemainingTotal() {
        int total = 0;
        for (Domino d : hand) {
            total += d.getTotal();
        }
        return total;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" (Player ").append(playerNumber).append(")");
        sb.append(" | Score: ").append(score);
        sb.append(" | Hand: ");
        for (Domino d : hand) {
            sb.append(d.toString()).append(" ");
        }
        return sb.toString();
    }
}
