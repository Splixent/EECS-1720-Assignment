/**
 * Represents a single domino tile with two numbered sides (0-6).
 */
public class Domino {

    private int leftValue;
    private int rightValue;
    private boolean isFlipped;

    /**
     * Creates a domino with the given pip values.
     * @param left  pip count on the left side (0-6)
     * @param right pip count on the right side (0-6)
     */
    public Domino(int left, int right) {
        this.leftValue = left;
        this.rightValue = right;
        this.isFlipped = false;
    }

    /**
     * Swaps left and right values so the tile can be placed in either direction.
     */
    public void flip() {
        int temp = this.leftValue;
        this.leftValue = this.rightValue;
        this.rightValue = temp;
        this.isFlipped = !this.isFlipped;
    }

    public int getLeft() {
        return leftValue;
    }

    public int getRight() {
        return rightValue;
    }

    /**
     * Returns the sum of both sides. Used for scoring and end-game tiebreaks.
     */
    public int getTotal() {
        return leftValue + rightValue;
    }

    /**
     * Returns true if both sides have the same number (e.g. [3|3]).
     */
    public boolean isDouble() {
        return leftValue == rightValue;
    }

    /**
     * Returns true if either side equals the given number.
     * Used to check if this domino could match an adjacent tile.
     */
    public boolean matches(int value) {
        return leftValue == value || rightValue == value;
    }

    public boolean isFlipped() {
        return isFlipped;
    }

    @Override
    public String toString() {
        return "[" + leftValue + "|" + rightValue + "]";
    }
}
