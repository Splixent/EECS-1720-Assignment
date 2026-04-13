import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;
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

    // ============================================================
    // THEME CONSTANTS — quick tweaks live here
    // ============================================================
    // TODO: change the window size for a bigger or smaller game.
    //   Bump these two numbers; the layout adjusts automatically.
    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 700;

    // TODO: tweak the background color of the screens here.
    private static final Color BG_COLOR = new Color(240, 244, 248);

    // TODO: tweak the dark accent color (top bar, title, board border).
    private static final Color ACCENT_COLOR = new Color(27, 58, 75);

    // TODO: tweak the empty board cell color here.
    private static final Color EMPTY_CELL_COLOR = new Color(245, 245, 240);

    // TODO: tweak the invalid-move flash color here.
    private static final Color INVALID_FLASH_COLOR = new Color(255, 200, 200);

    // TODO: change the default highlight color (also overridden by combo box).
    private static final Color DEFAULT_HIGHLIGHT = new Color(200, 230, 200);

    // TODO: change the turn label normal color and the error flash color here.
    private static final Color TURN_LABEL_COLOR = new Color(255, 200, 50);
    private static final Color ERROR_LABEL_COLOR = new Color(255, 110, 110);
    // ============================================================

    private Game game;
    private CardLayout cardLayout;   // switches between start screen and game screen
    private JPanel mainPanel;
    private JPanel gameScreenPanel;  // current game screen instance (rebuilt per game)

    // Game screen components
    private JPanel boardPanel;       // the grid of board cells
    private JPanel handPanel;        // current player's hand
    private JLabel turnLabel;        // shows whose turn it is
    private JLabel drawPileLabel;    // shows how many tiles remain in draw pile
    private JLabel[] scoreLabels;    // player score displays

    // Currently selected domino index in the player's hand (-1 = none)
    private int selectedDominoIndex = -1;

    // Most recent game settings (used by "Play Again")
    private int currentNumPlayers = 2;

    // Live highlight color picked from the combo box
    private Color highlightColor = DEFAULT_HIGHLIGHT;

    // Last placed tile coordinates, used for a subtle "just played" outline
    private int lastPlacedRow = -1;
    private int lastPlacedCol = -1;

    // Shared timer for the turn-label flash messages (success / error)
    private Timer flashTimer;

    public DominoMatchGUI() {
        setTitle("Domino Match");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);

        // Auto-save on window close if a game is in progress
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                autoSaveIfInProgress();
                System.exit(0);
            }
        });

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
        panel.setBackground(BG_COLOR);

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

        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Player count selector
        JLabel countLabel = new JLabel("Number of players:");
        countLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        countLabel.setForeground(new Color(80, 80, 90));
        countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(countLabel);

        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        Integer[] playerCounts = {2, 3, 4};
        JComboBox<Integer> playerCountBox = new JComboBox<>(playerCounts);
        playerCountBox.setSelectedItem(currentNumPlayers);
        playerCountBox.setMaximumSize(new Dimension(120, 28));
        playerCountBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(playerCountBox);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Play button
        JButton playBtn = createMenuButton("PLAY GAME", ACCENT_COLOR);
        playBtn.addActionListener(e -> {
            currentNumPlayers = (Integer) playerCountBox.getSelectedItem();
            startNewGame(currentNumPlayers);
        });
        panel.add(playBtn);

        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // How to Play button
        JButton helpBtn = createMenuButton("HOW TO PLAY", new Color(6, 90, 96));
        helpBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "DOMINO MATCH\n"
                + "------------------------------------------------------------\n\n"
                + "GOAL\n"
                + "  Empty your hand, or score the most points before everyone\n"
                + "  is stuck.\n\n"
                + "ON YOUR TURN\n"
                + "  1. Click a tile in your hand (or press 1-7) to select it.\n"
                + "  2. Press F (or click \"Flip Domino\") to swap its two halves.\n"
                + "  3. Click an empty board cell to place it.\n\n"
                + "PLACEMENT RULES\n"
                + "  - The first tile can go anywhere.\n"
                + "  - After that, every tile must touch an existing tile.\n"
                + "  - Touching edges must show the SAME number.\n"
                + "  - Each match scores points equal to the matched value.\n\n"
                + "DRAW PILE\n"
                + "  - Click \"Draw Tile\" only when none of your tiles can\n"
                + "    legally be played. It pulls one extra tile into your hand.\n\n"
                + "PASSING\n"
                + "  - Click \"Pass Turn\" only if the draw pile is empty AND\n"
                + "    you still have no legal move. Otherwise, draw or play.\n\n"
                + "GAME END\n"
                + "  - A player empties their hand: they win.\n"
                + "  - Or no one can move and the pile is empty: highest score\n"
                + "    wins (ties broken by lowest tile total left in hand).\n\n"
                + "SAVING\n"
                + "  - New games are auto-saved as soon as you start them.\n"
                + "  - Closing the window or clicking \"Main Menu\" auto-saves.\n"
                + "  - Click \"LOAD GAME\" on the start screen to resume any save.",
                "How to Play", JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(helpBtn);

        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Load Game button
        JButton loadBtn = createMenuButton("LOAD GAME", new Color(100, 100, 110));
        loadBtn.addActionListener(e -> loadSavedGame());
        panel.add(loadBtn);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * Small italic gray hint label for under control buttons.
     */
    private JLabel makeHintLabel(String text) {
        JLabel hint = new JLabel(text);
        hint.setFont(new Font("SansSerif", Font.ITALIC, 10));
        hint.setForeground(new Color(110, 110, 120));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        return hint;
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
        currentNumPlayers = numPlayers;
        selectedDominoIndex = -1;
        lastPlacedRow = -1;
        lastPlacedCol = -1;

        // Save the brand-new game immediately so it shows up in Load Game right away
        try {
            SaveManager.save(game);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Could not create save file: " + ex.getMessage(),
                "Save Failed", JOptionPane.WARNING_MESSAGE);
        }

        // Remove any previous game screen so CardLayout shows the fresh one
        if (gameScreenPanel != null) {
            mainPanel.remove(gameScreenPanel);
        }
        gameScreenPanel = createGameScreen();
        mainPanel.add(gameScreenPanel, "game");
        cardLayout.show(mainPanel, "game");

        refreshAll();
        requestFocusInWindow(); // so key events work
    }

    /**
     * Creates the main game screen with board, hand, controls, and score bar.
     */
    private JPanel createGameScreen() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(BG_COLOR);

        // --- TOP BAR: scores and turn indicator ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ACCENT_COLOR);
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
        turnLabel.setForeground(TURN_LABEL_COLOR);
        turnLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        topBar.add(turnLabel, BorderLayout.CENTER);

        panel.add(topBar, BorderLayout.NORTH);

        // --- BOARD AREA (center) ---
        boardPanel = new JPanel(new GridLayout(Game.BOARD_ROWS, Game.BOARD_COLS, 1, 1));
        boardPanel.setBackground(Color.DARK_GRAY);
        boardPanel.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 2));

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
        drawBtn.setToolTipText("Pull a tile from the draw pile when none of yours can be played.");
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
        controlPanel.add(makeHintLabel("if you can't play any tile"));
        controlPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Pass Turn button
        JButton passBtn = new JButton("Pass Turn");
        passBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        passBtn.setMaximumSize(new Dimension(140, 35));
        passBtn.setToolTipText("Skip your turn -- only useful when the draw pile is empty and you're stuck.");
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
        controlPanel.add(makeHintLabel("only if pile is empty & stuck"));
        controlPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Flip button
        JButton flipBtn = new JButton("Flip Domino");
        flipBtn.setToolTipText("Swap the two halves of the selected tile (or press F).");
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
                // TODO: tweak any of these four highlight colors to taste.
                switch (choice) {
                    case "Green":  highlightColor = new Color(200, 230, 200); break;
                    case "Blue":   highlightColor = new Color(200, 210, 240); break;
                    case "Yellow": highlightColor = new Color(245, 240, 200); break;
                    case "None":   highlightColor = EMPTY_CELL_COLOR;          break;
                }
                refreshBoardPanel();
                requestFocusInWindow();
            }
        });
        controlPanel.add(settingsBox);

        controlPanel.add(Box.createVerticalGlue());

        // Back to menu button - auto-saves if game is still in progress
        JButton menuBtn = new JButton("Main Menu");
        menuBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuBtn.setMaximumSize(new Dimension(140, 30));
        menuBtn.addActionListener(e -> {
            autoSaveIfInProgress();
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
                    flashTurnLabel(
                        "Select a tile first (press 1-" + game.getCurrentPlayer().getHandSize() + ")",
                        ERROR_LABEL_COLOR, 2000);
                    return;
                }

                Player playerBefore = game.getCurrentPlayer();
                Domino selected = playerBefore.getDomino(selectedDominoIndex);
                String reason = game.getBoard().getInvalidReason(selected, row, col);

                if (reason != null) {
                    flashTurnLabel(reason, ERROR_LABEL_COLOR, 2200);
                    cell.setBackground(INVALID_FLASH_COLOR);
                    Timer timer = new Timer(300, evt -> {
                        cell.setBackground(EMPTY_CELL_COLOR);
                    });
                    timer.setRepeats(false);
                    timer.start();
                    requestFocusInWindow();
                    return;
                }

                int scoreBefore = playerBefore.getScore();
                boolean success = game.playDomino(selectedDominoIndex, row, col);
                if (success) {
                    int gained = playerBefore.getScore() - scoreBefore;
                    selectedDominoIndex = -1;
                    lastPlacedRow = row;
                    lastPlacedCol = col;
                    if (game.isGameOver()) {
                        refreshAll();
                        showGameOver();
                    } else {
                        refreshAll();
                        if (gained > 0) {
                            flashTurnLabel(
                                playerBefore.getName() + " scored +" + gained + "!",
                                TURN_LABEL_COLOR, 1500);
                        }
                    }
                }
                requestFocusInWindow();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (game != null && !game.isGameOver() && selectedDominoIndex >= 0) {
                    if (game.getBoard().getDominoAt(row, col) == null) {
                        cell.setBackground(highlightColor);
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (game != null && game.getBoard().getDominoAt(row, col) == null) {
                    cell.setBackground(EMPTY_CELL_COLOR);
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
                dp.setVertical(shouldDrawVertical(board, row, col));
                cell.setBackground(EMPTY_CELL_COLOR);
                cell.add(dp, BorderLayout.CENTER);
                if (row == lastPlacedRow && col == lastPlacedCol) {
                    cell.setBorder(BorderFactory.createLineBorder(TURN_LABEL_COLOR, 2));
                } else {
                    cell.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
                }
            } else {
                cell.setBackground(EMPTY_CELL_COLOR);
                cell.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            }
            cell.revalidate();
            cell.repaint();
        }
    }

    /**
     * Picks orientation for a placed domino: vertical if it has a vertical
     * neighbor and no horizontal neighbor, otherwise horizontal.
     */
    private boolean shouldDrawVertical(Board board, int row, int col) {
        boolean hasVerticalNeighbor =
            (row > 0 && board.getDominoAt(row - 1, col) != null) ||
            (row < Game.BOARD_ROWS - 1 && board.getDominoAt(row + 1, col) != null);
        boolean hasHorizontalNeighbor =
            (col > 0 && board.getDominoAt(row, col - 1) != null) ||
            (col < Game.BOARD_COLS - 1 && board.getDominoAt(row, col + 1) != null);
        return hasVerticalNeighbor && !hasHorizontalNeighbor;
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
     * Saves the current game to disk if one is in progress (not yet over).
     * Best-effort: failures pop a warning but don't block leaving the screen.
     */
    private void autoSaveIfInProgress() {
        if (game == null || game.isGameOver()) return;
        try {
            SaveManager.save(game);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Could not save game: " + ex.getMessage(),
                "Save Failed", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Shows a list of all saved games and lets the user pick one to load.
     */
    private void loadSavedGame() {
        List<Game> saves = SaveManager.listSaves();
        if (saves.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No saved games found.",
                "Load Game", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] labels = new String[saves.size()];
        for (int i = 0; i < saves.size(); i++) {
            Game g = saves.get(i);
            labels[i] = "Game " + g.getId()
                + " (" + g.getPlayers().size() + "p, "
                + g.getCurrentPlayer().getName() + "'s turn)";
        }

        String chosen = (String) JOptionPane.showInputDialog(this,
            "Choose a saved game:",
            "Load Game",
            JOptionPane.PLAIN_MESSAGE,
            null,
            labels,
            labels[0]);

        if (chosen == null) return; // user cancelled

        for (int i = 0; i < labels.length; i++) {
            if (labels[i].equals(chosen)) {
                enterLoadedGame(saves.get(i));
                return;
            }
        }
    }

    /**
     * Same screen-rebuild path as startNewGame, but reuses an existing Game object.
     */
    private void enterLoadedGame(Game loaded) {
        this.game = loaded;
        this.currentNumPlayers = loaded.getPlayers().size();
        this.selectedDominoIndex = -1;
        this.lastPlacedRow = -1;
        this.lastPlacedCol = -1;

        if (gameScreenPanel != null) {
            mainPanel.remove(gameScreenPanel);
        }
        gameScreenPanel = createGameScreen();
        mainPanel.add(gameScreenPanel, "game");
        cardLayout.show(mainPanel, "game");

        refreshAll();
        requestFocusInWindow();
    }

    /**
     * Shows the game over dialog with the winner and Play Again / Main Menu options.
     */
    private void showGameOver() {
        // Game is finished -- delete this game's save slot so it isn't offered again
        SaveManager.delete(game.getId());
        Player winner = game.checkWinner();
        StringBuilder msg = new StringBuilder();
        msg.append("Game Over!\n\n");
        for (Player p : game.getPlayers()) {
            msg.append(p.getName()).append(": ").append(p.getScore()).append(" pts\n");
        }
        msg.append("\nWinner: ").append(winner.getName()).append("!");

        Object[] options = {"Play Again", "Main Menu"};
        int choice = JOptionPane.showOptionDialog(this, msg.toString(), "Game Over",
            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
            null, options, options[0]);

        if (choice == JOptionPane.YES_OPTION) {
            startNewGame(currentNumPlayers);
        } else {
            cardLayout.show(mainPanel, "start");
        }
    }

    /**
     * Briefly shows a message in the turn label using the given color, then
     * restores the normal turn text. Cancels any prior flash so messages don't
     * stack.
     */
    private void flashTurnLabel(String message, Color color, int durationMs) {
        if (flashTimer != null && flashTimer.isRunning()) {
            flashTimer.stop();
        }
        turnLabel.setForeground(color);
        turnLabel.setText(message);
        flashTimer = new Timer(durationMs, evt -> {
            turnLabel.setForeground(TURN_LABEL_COLOR);
            if (game != null && !game.isGameOver()) {
                turnLabel.setText("Current Turn: " + game.getCurrentPlayer().getName());
            }
        });
        flashTimer.setRepeats(false);
        flashTimer.start();
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
