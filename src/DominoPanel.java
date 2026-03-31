import java.awt.*;
import javax.swing.*;

/**
 * Custom JPanel that draws a single domino tile using paintComponent.
 * No image files needed -- pips are drawn programmatically.
 */
public class DominoPanel extends JPanel {

    private Domino domino;
    private boolean selected;
    private boolean faceUp;

    // Pip positions for each value (0-6) as {x, y} pairs
    // Coordinates are relative to the center of one half, scaled 0.0 to 1.0
    private static final double[][][] PIP_POSITIONS = {
        {},                                                                     // 0: no pips
        {{0.5, 0.5}},                                                           // 1: center
        {{0.25, 0.25}, {0.75, 0.75}},                                           // 2: diagonal
        {{0.25, 0.25}, {0.5, 0.5}, {0.75, 0.75}},                              // 3: diagonal + center
        {{0.25, 0.25}, {0.75, 0.25}, {0.25, 0.75}, {0.75, 0.75}},              // 4: corners
        {{0.25, 0.25}, {0.75, 0.25}, {0.5, 0.5}, {0.25, 0.75}, {0.75, 0.75}},  // 5: corners + center
        {{0.25, 0.2}, {0.75, 0.2}, {0.25, 0.5}, {0.75, 0.5}, {0.25, 0.8}, {0.75, 0.8}} // 6: two columns
    };

    // Colors
    private static final Color TILE_COLOR = new Color(26, 26, 46);       // dark body
    private static final Color PIP_COLOR = Color.WHITE;
    private static final Color DIVIDER_COLOR = Color.WHITE;
    private static final Color SELECTED_BORDER = new Color(255, 215, 0); // gold highlight
    private static final Color FACE_DOWN_COLOR = new Color(60, 60, 80);

    public DominoPanel(Domino domino) {
        this.domino = domino;
        this.selected = false;
        this.faceUp = true;
        setPreferredSize(new Dimension(80, 40));
        setOpaque(false);
    }

    public void setDomino(Domino domino) {
        this.domino = domino;
        repaint();
    }

    public Domino getDomino() {
        return domino;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (domino == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int padding = 2;

        // Selection highlight border
        if (selected) {
            g2.setColor(SELECTED_BORDER);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
            padding = 4;
        }

        // Tile body
        if (faceUp) {
            g2.setColor(TILE_COLOR);
        } else {
            g2.setColor(FACE_DOWN_COLOR);
        }
        g2.fillRoundRect(padding, padding, w - padding * 2, h - padding * 2, 6, 6);

        if (!faceUp) {
            // Draw a simple pattern for face-down
            g2.setColor(new Color(80, 80, 100));
            g2.drawRoundRect(padding + 4, padding + 4,
                             w - padding * 2 - 8, h - padding * 2 - 8, 4, 4);
            g2.dispose();
            return;
        }

        // Divider line down the middle
        int midX = w / 2;
        g2.setColor(DIVIDER_COLOR);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(midX, padding + 3, midX, h - padding - 3);

        // Draw pips on left half
        drawPips(g2, domino.getLeft(), padding, padding, midX - padding, h - padding * 2);

        // Draw pips on right half
        drawPips(g2, domino.getRight(), midX, padding, w - padding - midX, h - padding * 2);

        g2.dispose();
    }

    /**
     * Draws the pip pattern for a given value within the specified rectangle.
     */
    private void drawPips(Graphics2D g2, int value, int x, int y, int width, int height) {
        if (value < 0 || value > 6) return;

        g2.setColor(PIP_COLOR);
        int pipRadius = Math.min(width, height) / 8;

        double[][] positions = PIP_POSITIONS[value];
        for (double[] pos : positions) {
            int px = x + (int) (pos[0] * width);
            int py = y + (int) (pos[1] * height);
            g2.fillOval(px - pipRadius, py - pipRadius, pipRadius * 2, pipRadius * 2);
        }
    }
}
