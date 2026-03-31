# Domino Match

**EECS 1720 - W2026 Assignment**
GUI / Event-Enabled Interactive Application

---

## Project Description

Domino Match is a tile-matching strategy game built with Java Swing. It uses a standard double-six domino set (28 tiles). Two players take turns placing dominoes onto a shared grid board. A domino can only be placed next to an existing tile if the touching sides have matching numbers. Matching tiles earn the player points. The game ends when a player empties their hand or no valid moves remain.

The project demonstrates object-oriented class design with HAS-A relationships, a graphical user interface with multiple screen views, and event-driven interaction using four distinct event types.

---

## How to Run

**Requirements:** Java 8 or higher (JDK, not just JRE).

### From Terminal

```bash
# 1. Clone the repo
git clone https://github.com/Splixent/EECS-1720-Assignment.git

# 2. Navigate into the source folder
cd EECS-1720-Assignment/src

# 3. Compile all Java files
javac *.java

# 4. Run the game
java DominoMatchGUI
```

### From Eclipse

1. Open Eclipse and go to **File > Import > General > Existing Projects into Workspace**.
2. Select the root folder of this repository.
3. Make sure the project is checked, then click **Finish**.
4. In the Package Explorer, open `src/DominoMatchGUI.java`.
5. Right-click the file and select **Run As > Java Application**.

### For Group Members Working on the Code

```bash
# Pull the latest changes before you start working
git pull origin main

# After making your changes, stage, commit, and push
git add -A
git commit -m "Description of what you changed"
git push origin main
```

If you get merge conflicts, coordinate with the group before force-pushing.

---

## How to Play

### Starting a Game

- Launch the application. The start screen has three buttons.
- Click **"PLAY GAME"** to start a new 2-player game.
- Click **"HOW TO PLAY"** for a quick rules summary.

### On Your Turn

1. **Select a domino** from your hand:
   - Press a number key (`1`-`7`) corresponding to the tile's position, OR
   - Click directly on a tile in the hand panel at the bottom.
   - The selected tile is highlighted with a gold border.
2. **Flip the domino** if needed:
   - Press `F` on the keyboard, or click the **"Flip Domino"** button.
   - This swaps the left and right sides so you can match either direction.
3. **Place the domino** on the board:
   - Click an empty cell on the board grid.
   - If the placement is valid (touching sides match), the tile is placed, you earn points, and the turn passes to the next player.
   - If invalid, the cell flashes red and the move is rejected.
4. **If you cannot place any tile:**
   - Click **"Draw Tile"** to take a tile from the draw pile.
   - Click **"Pass Turn"** to skip your turn.
5. Press `Escape` to deselect the current tile.

### Scoring

- Points are awarded based on the sum of matched numbers on touching edges.
- The first tile placed earns points equal to its total pip count.

### Winning

- The first player to place all their tiles wins.
- If no one can move and the draw pile is empty, the player with the highest score wins. Ties are broken by lowest remaining tile total.

---

## Class Structure

| Class | Role | Description |
|---|---|---|
| `Domino.java` | Entity | Represents a single tile with two numbered sides (0-6). Supports flipping, matching, and total calculation. Drawn programmatically using pip positions. |
| `Player.java` | Entity | Represents a player with a hand of dominoes and a score. Manages adding/removing tiles, checking for playable moves, and tracking points. |
| `Board.java` | Entity / Logic | Represents the game board as a 2D grid. Handles placement validation, point calculation, and board state queries. |
| `Game.java` | Logic / Control | Controls game flow. Generates all 28 tiles, shuffles, deals hands, manages turns, and determines the winner. |
| `DominoPanel.java` | View | Custom JPanel that draws a domino tile using `paintComponent`. Renders tile body, dividing line, and pips programmatically. |
| `DominoMatchGUI.java` | GUI / Events | Main GUI class. Manages start screen and game screen using `CardLayout`. Contains all event handlers. |

### Relationships (all HAS-A)

- **Game** HAS-A **Board** (one board per game)
- **Game** HAS-A **Player** list (2 or more players)
- **Game** HAS-A **Domino** list (the draw pile)
- **Player** HAS-A **Domino** list (the player's hand)
- **Board** HAS-A **Domino** 2D array (placed tiles on the grid)

---

## Event Handling

| # | Type | Control | Action |
|---|---|---|---|
| 1 | Action Event | `JButton` | **Draw Tile** - moves a domino from the draw pile to the current player's hand. Updates the hand panel and draw pile counter. |
| 2 | Action Event | `JComboBox` | **Highlight dropdown** - changes the highlight color used when hovering over valid board cells. Different control type from buttons. |
| 3 | Mouse Event | Board `JPanel` | **Click on board cell** - places the selected domino at that position. Validates placement, awards points, switches turns. Invalid moves flash red. |
| 4 | Key Event | `JFrame` | **Number keys (1-7)** select a domino, **F** flips it, **Escape** deselects. |

---

## References

- [Java Swing Documentation](https://docs.oracle.com/javase/tutorial/uiswing/components/index.html)
- [Java AWT Documentation](https://www.javatpoint.com/java-awt)
- [Java 8 API](https://docs.oracle.com/javase/8/docs/api/)
