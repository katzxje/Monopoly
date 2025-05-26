# Monopoly Game - Visual Board Implementation

This Monopoly game now includes a visual game board, animated dice, and player piece movement.

## Features Added

1. **Visual Game Board**
   - 40 colored tiles representing all Monopoly spaces
   - Proper orientation of tile names on all sides of the board
   - Player tokens displayed on the board

2. **Animated Dice**
   - Visual representation of two dice
   - Animated rolling effect
   - Shows proper dot patterns for values 1-6

3. **Player Movement**
   - Player tokens move around the board
   - Animated movement showing each space the player passes
   - Proper positioning when multiple players are on the same space

4. **Property Ownership**
   - Players can buy properties they land on
   - Property ownership is displayed in the game log
   - Rent payments are calculated and displayed

## How to Play

1. Start the game and select the number of players (2-4)
2. Enter player names and select character tokens
3. Players take turns by clicking the "Roll Dice" button
4. After rolling, the player's token moves around the board
5. If landing on an unowned property, the "Buy Property" button becomes active
6. When finished with a turn, click "End Turn" to pass to the next player
7. Game continues until only one player remains solvent

## Required Image Files

To display character tokens, you need to add the following image files to the `images/` directory:
- `images/car.png`
- `images/dog.png`
- `images/hat.png`
- `images/ship.png`
- `images/iron.png`
- `images/boot.png`
- `images/thimble.png`
- `images/wheelbarrow.png`

Each image should be at least 50x50 pixels in size for best display.

## Future Enhancements

- Display of property ownership on the board
- More detailed property cards
- Trading between players
- Mortgage functionality
- House and hotel building 