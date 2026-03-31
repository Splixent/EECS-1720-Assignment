import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Main GUI class for Domino Match.
 * Manages the window, screens, and event handling.
 *
 * Events implemented:
 *   Action 1: JButton     - "Draw Tile" button
 *   Action 2: JComboBox   - game settings dropdown
 *   Mouse:    MouseListener on the board grid - click to place domino
 *   Key:      KeyListener on the frame - number keys to select domino, F to flip
 */
public class DominoMatchGUI extends JFrame {

    private Game game;
    private CardLayout cardLayout;   // switches between start screen and game screen
    private JPanel mainPanel;

    // Game screen components
    private JPanel boardPanel;       // the grid of board cells
    private JPanel handPanel;        // current player's hand
    private JLabel turnLabel;        // shows whose turn it is
    private JLabel drawPileLabel;    // shows how many tiles remain in draw pile
    private JLabel[] scoreLabels;    // player score displays

    // Currently selected domino index in the player's hand (-1 = none)
    private int selectedDominoIndex = -1;

    // Window dimensions
    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 700;

    public DominoMatchGUI() {
        setTitle("Domino Match");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);

        // CardLayout to switch between start screen and game screen
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createStartScreen(), "start");
        // Game screen will be added when "Play" is clicked

        add(mainPanel);
        cardLayout.show(mainPanel, "start");

        // --- KEY EVENT (Requirement #4) ---
        // Number keys select domino from hand, F flips, Escape deselects
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (game == null || game.isGameOver()) return;

                int key = e.getKeyCode();
                Player current = game.getCurrentPlayer();

                // Number keys 1-9: select domino at that index
                if (key >= KeyEvent.VK_1 && key <= KeyEvent.VK_9) {
                    int index = key - KeyEvent.VK_1; // 0-based
                    if (index < current.getHandSize()) {
                        selectedDominoIndex = index;
                        refreshHandPanel();
                    }
                }

                // F key: flip the selected domino
                if (key == KeyEvent.VK_F && selectedDominoIndex >= 0) {
                    Domino d = current.getDomino(selectedDominoIndex);
                    if (d != null) {
                        d.flip();
                        refreshHandPanel();
                    }
                }

                // Escape: deselect
                if (key == KeyEvent.VK_ESCAPE) {
                    selectedDominoIndex = -1;
                    refreshHandPanel();
                }
            }
        });

        setFocusable(true);
        setVisible(true);
    }

    /**
     * Creates the start/title screen with Play, How to Play, and Save/Load buttons.
     */
    private JPanel createStartScreen() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(240, 244, 248));

        panel.add(Box.createVerticalGlue());

        // Title
        JLabel title = new JLabel("DOMINO MATCH");
        title.setFont(new Font("SansSerif", Font.BOLD, 42));
        title.setForeground(new Color(27, 58, 75));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);

        JLabel subtitle = new JLabel("A Tile-Matching Strategy Game");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(subtitle);

        panel.add(Box.createRigidArea(new Dimension(0, 40)));

        // Play button
        JButton playBtn = createMenuButton("PLAY GAME", new Color(27, 58, 75));
        playBtn.addActionListener(e -> startNewGame(2)); // default 2 players
        panel.add(playBtn);

        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // How to Play button
        JButton helpBtn = createMenuButton("HOW TO PLAY", new Color(6, 90, 96));
        helpBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Domino Match Rules:\n\n"
                + "1. Each player starts with 7 tiles.\n"
                + "2. Take turns placing tiles on the board.\n"
                + "3. Matching numbers on touching sides = points!\n"
                + "4. Press 1-7 to select a tile, F to flip, click board to place.\n"
                + "5. First to empty their hand wins!",
                "How to Play", JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(helpBtn);

        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Save/Load button
        JButton saveBtn = createMenuButton("SAVE / LOAD", new Color(100, 100, 110));
        saveBtn.addActionListener(e -> {
            // TODO: implement save/load functionality
            JOptionPane.showMessageDialog(this,
                "Save/Load not yet implemented.",
                "Save / Load", JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(saveBtn);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * Creates a styled button for the start menu.
     */
    private JButton createMenuButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(250, 45));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Initializes a new game and switches to the game screen.
     */
    private void startNewGame(int numPlayers) {
        game = new Game(numPlayers);
        selectedDominoIndex = -1;

        JPanel gameScreen = createGameScreen();
        mainPanel.add(gameScreen, "game");
        cardLayout.show(mainPanel, "game");

        refreshAll();
        requestFocusInWindow(); // so key events work
    }

    /**
     * Creates the main game screen with board, hand, controls, and score bar.
     */
    private JPanel createGameScreen() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(240, 244, 248));

        // --- TOP BAR: scores and turn indicator ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(27, 58, 75));
        topBar.setPreferredSize(new Dimension(WINDOW_WIDTH, 40));

        scoreLabels = new JLabel[game.getPlayers().size()];
        JPanel scoresPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        scoresPanel.setOpaque(false);
        for (int i = 0; i < game.getPlayers().size(); i++) {
            scoreLabels[i] = new JLabel();
            scoreLabels[i].setForeground(Color.WHITE);
            scoreLabels[i].setFont(new Font("SansSerif", Font.BOLD, 14));
            scoresPanel.add(scoreLabels[i]);
        }
        topBar.add(scoresPanel, BorderLayout.WEST);

        turnLabel = new JLabel("", SwingConstants.CENTER);
        turnLabel.setForeground(new Color(255, 200, 50));
        turnLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        topBar.add(turnLabel, BorderLayout.CENTER);

        panel.add(topBar, BorderLayout.NORTH);

        // --- BOARD AREA (center) ---
        boardPanel = new JPanel(new GridLayout(Game.BOARD_ROWS, Game.BOARD_COLS, 1, 1));
        boardPanel.setBackground(Color.DARK_GRAY);
        boardPanel.setBorder(BorderFactory.createLineBorder(new Color(27, 58, 75), 2));

        // Create clickable cells
        for (int r = 0; r < Game.BOARD_ROWS; r++) {
            for (int c = 0; c < Game.BOARD_COLS; c++) {
                JPanel cell = createBoardCell(r, c);
                boardPanel.add(cell);
            }
        }

        panel.add(boardPanel, BorderLayout.CENTER);

        // --- CONTROL PANEL (right side) ---
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setPreferredSize(new Dimension(160, 0));
        controlPanel.setBackground(new Color(230, 234, 238));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel controlTitle = new JLabel("Controls");
        controlTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        controlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(controlTitle);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Draw pile count
        drawPileLabel = new JLabel("Draw Pile: " + game.getDrawPileSize());
        drawPileLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        drawPileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(drawPileLabel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- ACTION EVENT 1: JButton - Draw Tile ---
        JButton drawBtn = new JButton("Draw Tile");
        drawBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        drawBtn.setMaximumSize(new Dimension(140, 35));
        drawBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (game.isGameOver()) return;

                Domino drawn = game.drawTile(game.getCurrentPlayer());
                if (drawn != null) {
                    selectedDominoIndex = -1;
                    refreshAll();
                    requestFocusInWindow();
                } else {
                    JOptionPane.showMessageDialog(DominoMatchGUI.this,
                        "Draw pile is empty!", "Cannot Draw", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        controlPanel.add(drawBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Pass Turn button
        JButton passBtn = new JButton("Pass Turn");
        passBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        passBtn.setMaximumSize(new Dimension(140, 35));
        passBtn.addActionListener(e -> {
            if (game.isGameOver()) return;
            selectedDominoIndex = -1;
            game.switchTurn();
            if (game.checkGameOver()) {
                game.endGame();
                showGameOver();
            }
            refreshAll();
            requestFocusInWindow();
        });
        controlPanel.add(passBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Flip button
        JButton flipBtn = new JButton("Flip Domino");
        flipBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        flipBtn.setMaximumSize(new Dimension(140, 35));
        flipBtn.addActionListener(e -> {
            if (selectedDominoIndex >= 0) {
                Player current = game.getCurrentPlayer();
                Domino d = current.getDomino(selectedDominoIndex);
                if (d != null) {
                    d.flip();
                    refreshHandPanel();
                }
            }
            requestFocusInWindow();
        });
        controlPanel.add(flipBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // --- ACTION EVENT 2: JComboBox - Game Setting ---
        JLabel settingLabel = new JLabel("Highlight:");
        settingLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        settingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(settingLabel);

        String[] options = {"Green", "Blue", "Yellow", "None"};
        JComboBox<String> settingsBox = new JComboBox<>(options);
        settingsBox.setMaximumSize(new Dimension(140, 30));
        settingsBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        settingsBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String choice = (String) settingsBox.getSelectedItem();
                // TODO: change the highlight color used for valid placement cells
                //       based on the selected option. Then refresh the board.
                refreshBoardPanel();
                requestFocusInWindow();
            }
        });
        controlPanel.add(settingsBox);

        controlPanel.add(Box.createVerticalGlue());

        // Back to menu button
        JButton menuBtn = new JButton("Main Menu");
        menuBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuBtn.setMaximumSize(new Dimension(140, 30));
        menuBtn.addActionListener(e -> {
            cardLayout.show(mainPanel, "start");
        });
        controlPanel.add(menuBtn);

        panel.add(controlPanel, BorderLayout.EAST);

        // --- PLAYER HAND (bottom) ---
        handPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        handPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, 70));
        handPanel.setBackground(new Color(220, 225, 230));
        handPanel.setBorder(BorderFactory.createTitledBorder("Your Hand (press 1-7 to select, F to flip)"));

        panel.add(handPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates a single clickable cell for the board grid.
     */
    private JPanel createBoardCell(int row, int col) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(new Color(245, 245, 240));
        cell.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        // --- MOUSE EVENT (Requirement #3) ---
        // Click on a board cell to place the selected domino
        cell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (game == null || game.isGameOver()) return;
                if (selectedDominoIndex < 0) {
                    // No domino selected -- flash a message
                    turnLabel.setText("Select a domino first! (press 1-" +
                        game.getCurrentPlayer().getHandSize() + ")");
                    return;
                }

                boolean success = game.playDomino(selectedDominoIndex, row, col);
                if (success) {
                    selectedDominoIndex = -1;
                    if (game.isGameOver()) {
                        showGameOver();
                    }
                    refreshAll();
                } else {
                    // Flash cell red briefly to indicate invalid move
                    cell.setBackground(new Color(255, 200, 200));
                    Timer timer = new Timer(300, evt -> {
                        cell.setBackground(new Color(245, 245, 240));
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
                requestFocusInWindow();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (game != null && !game.isGameOver() && selectedDominoIndex >= 0) {
                    if (game.getBoard().getDominoAt(row, col) == null) {
                        cell.setBackground(new Color(200, 230, 200)); // hint green
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (game != null && game.getBoard().getDominoAt(row, col) == null) {
                    cell.setBackground(new Color(245, 245, 240));
                }
            }
        });

        return cell;
    }

    /**
     * Refreshes the entire game screen: board, hand, scores, turn label.
     */
    private void refreshAll() {
        if (game == null) return;
        refreshBoardPanel();
        refreshHandPanel();
        refreshScores();
    }

    /**
     * Redraws the board grid to reflect current game state.
     */
    private void refreshBoardPanel() {
        Component[] cells = boardPanel.getComponents();
        Board board = game.getBoard();

        for (int i = 0; i < cells.length; i++) {
            int row = i / Game.BOARD_COLS;
            int col = i % Game.BOARD_COLS;
            JPanel cell = (JPanel) cells[i];
            cell.removeAll();

            Domino d = board.getDominoAt(row, col);
            if (d != null) {
                DominoPanel dp = new DominoPanel(d);
                cell.setBackground(new Color(180, 180, 170));
                cell.add(dp, BorderLayout.CENTER);
            } else {
                cell.setBackground(new Color(245, 245, 240));
            }
            cell.revalidate();
            cell.repaint();
        }
    }

    /**
     * Redraws the player's hand panel showing their current tiles.
     */
    private void refreshHandPanel() {
        handPanel.removeAll();

        Player current = game.getCurrentPlayer();
        for (int i = 0; i < current.getHandSize(); i++) {
            Domino d = current.getDomino(i);
            DominoPanel dp = new DominoPanel(d);
            dp.setPreferredSize(new Dimension(80, 45));
            dp.setSelected(i == selectedDominoIndex);

            // Also allow clicking on a tile in the hand to select it
            final int index = i;
            dp.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedDominoIndex = index;
                    refreshHandPanel();
                    requestFocusInWindow();
                }
            });

            handPanel.add(dp);
        }

        handPanel.setBorder(BorderFactory.createTitledBorder(
            current.getName() + "'s Hand (press 1-" + current.getHandSize() + " to select, F to flip)"));

        handPanel.revalidate();
        handPanel.repaint();
    }

    /**
     * Updates the score labels and turn indicator.
     */
    private void refreshScores() {
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player p = game.getPlayers().get(i);
            scoreLabels[i].setText(p.getName() + ": " + p.getScore() + " pts");
        }

        Player current = game.getCurrentPlayer();
        turnLabel.setText("Current Turn: " + current.getName());

        drawPileLabel.setText("Draw Pile: " + game.getDrawPileSize());
    }

    /**
     * Shows the game over dialog with the winner.
     */
    private void showGameOver() {
        Player winner = game.checkWinner();
        StringBuilder msg = new StringBuilder();
        msg.append("Game Over!\n\n");
        for (Player p : game.getPlayers()) {
            msg.append(p.getName()).append(": ").append(p.getScore()).append(" pts\n");
        }
        msg.append("\nWinner: ").append(winner.getName()).append("!");

        JOptionPane.showMessageDialog(this, msg.toString(), "Game Over",
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Main entry point.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DominoMatchGUI();
        });
    }
}
