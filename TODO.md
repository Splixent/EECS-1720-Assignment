# Domino Match - TODO

## Group Members
- **Member A** — Backend: Board logic, Game logic, scoring, save/load
- **Member B** — Frontend: GUI polish, rendering, event handling, start screen

---

## Member A - Backend Tasks

### Board.java
- [ ] Finish vertical matching logic in `isValidPlacement()` — decide which domino value faces up vs down (line ~77)
- [ ] Add vertical match points in `calculatePoints()` (line ~134)
- [ ] Test edge cases: placing on board edges, double tiles, full board detection

### Game.java
- [ ] Add `resetScore()` method to Player and call it in `startGame()` (line ~84)
- [ ] Implement save/load: serialize game state to a file and load it back (see `DominoMatchGUI.java` line ~142 for the button hook)
- [ ] Test `checkGameOver()` — make sure it correctly detects when no moves remain
- [ ] Test `checkWinner()` — verify tiebreak logic works

### Player.java
- [ ] Verify `hasPlayableTile()` works correctly with flipped orientations
- [ ] Consider adding a `canPass()` method (only allow pass when no valid moves exist)

---

## Member B - Frontend Tasks

### DominoMatchGUI.java
- [ ] Implement the highlight color change from the JComboBox dropdown (line ~325) — store the selected color and use it in `mouseEntered` (line ~401)
- [ ] Implement save/load dialogs: file chooser for save location, load from file (line ~142)
- [ ] Improve the start screen: add a player count selector (2, 3, or 4 players) before starting
- [ ] Add a "Play Again" button on the game over dialog instead of just OK
- [ ] Style the game over screen better (show final board state, scores)

### DominoPanel.java
- [ ] Tweak pip sizes and positions if they look off at different panel sizes
- [ ] Consider adding a hover effect when mousing over tiles in the hand
- [ ] Add a visual indicator for double tiles (optional — e.g. slightly different background)

### General GUI
- [ ] Test that key events still work after clicking buttons (focus issues)
- [ ] Make sure the board cells resize properly if window size changes
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
- [x] Domino class — complete
- [x] Player class — complete
- [x] Board class — core logic done
- [x] Game class — core logic done
- [x] DominoPanel — rendering done
- [x] DominoMatchGUI — all 4 events wired up
- [x] README written
- [x] Design document written
