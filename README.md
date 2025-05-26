# Monopoly Game - Character System

This is an enhanced version of the Monopoly game with a character system. Players can choose their game tokens (characters) when starting a new game.

## Character Images

The game looks for character images in the `images/` directory. You need to add the following image files:

- `images/car.png`
- `images/dog.png`
- `images/hat.png`
- `images/ship.png`
- `images/iron.png`
- `images/boot.png`
- `images/thimble.png`
- `images/wheelbarrow.png`

Each image should be a square PNG file representing the Monopoly token. If you don't have these images, you can:

1. Create your own character token images (50x50 pixels or larger is recommended)
2. Find free token images online (make sure they're properly licensed)
3. Use placeholder images for testing

## Adding Custom Characters

If you want to add more characters beyond the default ones:

1. Add your character image to the `images/` directory
2. Edit the `CharacterManager.java` file to include your new character:

```java
// In CharacterManager.java constructor
availableCharacters.add(new Character("YourNewToken", "images/yourtoken.png"));
```

## How It Works

When starting a new game:
1. You'll be prompted to select the number of players
2. For each player, you'll enter a name and select a character
3. Characters are displayed throughout gameplay to represent each player

Enjoy your enhanced Monopoly game! 