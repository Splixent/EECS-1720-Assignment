# Domino Match - TODO

## Group Members

- **Member A** - Backend: Board logic, Game logic, scoring, save/load
- **Member B** - Frontend: GUI polish, rendering, event handling, start screen

---

## Victoria - Backend Tasks

### Board.java

- [X] Finish vertical matching logic in `isValidPlacement()` (left = top face, right = bottom face)
- [X] Add vertical match points in `calculatePoints()`
- [X] Test edge cases: placing on board edges, double tiles, full board detection

### Game.java

- [X] Add `resetScore()` method to Player and call it in `startGame()`
- [X] Implement save/load: multi-slot Java serialization in `SaveManager.java`
- [X] Test `checkGameOver()` - make sure it correctly detects when no moves remain
- [X] Test `checkWinner()` - verify tiebreak logic works

### Player.java

- [X] Verify `hasPlayableTile()` works correctly with flipped orientations
- [X] Consider adding a `canPass()` method (only allow pass when no valid moves exist)


---

## Avinash - Frontend Tasks

### DominoMatchGUI.java

- [X] Implement the highlight color change from the JComboBox dropdown
- [X] Wire up save/load: auto-save on Main Menu / window close, Load Game button on start screen, auto-delete on game end
- [X] Improve the start screen: add a player count selector (2, 3, or 4 players) before starting
- [X] Add a "Play Again" button on the game over dialog instead of just OK
- [ ] Style the game over screen better (show final board state, scores)

### Quick tweaks (each one is a 2-3 line edit at the top of `DominoMatchGUI.java`)

All of these constants live in the **THEME CONSTANTS** block at the top of the file - just change the number/color literal:

- [ ] Resize the window: change `WINDOW_WIDTH` / `WINDOW_HEIGHT`
- [ ] Recolor the screen background: change `BG_COLOR`
- [ ] Recolor the top bar / title / board border: change `ACCENT_COLOR`
- [ ] Recolor empty board cells: change `EMPTY_CELL_COLOR`
- [ ] Recolor the invalid-move red flash: change `INVALID_FLASH_COLOR`
- [ ] Recolor the default cell hover highlight: change `DEFAULT_HIGHLIGHT`
- [ ] Tweak the four named highlight colors (Green/Blue/Yellow/None) - they're in the `switch` block inside the JComboBox listener

### DominoPanel.java

- [X] Vertical rendering for stacked dominoes (`setVertical()` flag, divider rotates)
- [ ] Tweak pip sizes and positions if they look off at different panel sizes
- [ ] Consider adding a hover effect when mousing over tiles in the hand
- [ ] Add a visual indicator for double tiles (optional, e.g. slightly different background)

### General GUI

- [ ] Test that key events still work after clicking buttons (focus issues)
- [ ] Make the board cells resize properly if window size changes (currently `setResizable(false)` - flip to true and test)
- [ ] Add sound effects on placement / invalid move (optional stretch goal)

---

## Shared Tasks (Either Member)

- [ ] Write the UML diagram (use draw.io, Lucidchart, or similar)
- [ ] Update the README with final instructions and group member names
- [ ] Sign the Academic Integrity Statement
- [ ] Test full game flow: start -> play -> draw -> pass -> game over -> play again
- [ ] Package as Eclipse project zip: `Assignment_Group_XX.zip`
- [ ] Submit to https://webapp.eecs.yorku.ca/submit/ by April 6, 11:59 PM

---

## Done

- [X] Domino class - complete
- [X] Player class - complete
- [X] Board class - core logic done
- [X] Game class - core logic done
- [X] DominoPanel - rendering done
- [X] DominoMatchGUI - all 4 events wired up
- [X] README written
- [X] Design document written
