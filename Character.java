import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Represents a character token in the Monopoly game.
 */
public class Character {
    private String name;
    private ImageIcon icon;
    private Color color;
    
    // List of default characters
    public static final String[] DEFAULT_CHARACTERS = {
        "Car", "Dog", "Hat", "Ship", "Boot", "Iron", "Thimble", "Wheelbarrow"
    };
    
    // Colors for the characters
    private static final Color[] DEFAULT_COLORS = {
        Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, 
        Color.MAGENTA, Color.CYAN, new Color(139, 69, 19), // Brown
        new Color(255, 215, 0) // Gold
    };
    
    /**
     * Creates a new character with the specified name.
     * 
     * @param name The character name
     */
    public Character(String name) {
        this.name = name;
        this.color = getDefaultColorForName(name);
        this.icon = createIconForCharacter(name);
    }
    
    /**
     * Creates a new character with the specified name and icon.
     * 
     * @param name The character name
     * @param icon The character's icon
     */
    public Character(String name, ImageIcon icon) {
        this.name = name;
        this.icon = icon;
        this.color = getDefaultColorForName(name);
    }
    
    /**
     * Creates a new character with the specified name, icon, and color.
     * 
     * @param name The character name
     * @param icon The character's icon
     * @param color The character's color
     */
    public Character(String name, ImageIcon icon, Color color) {
        this.name = name;
        this.icon = icon;
        this.color = color;
    }
    
    /**
     * Gets the character's name.
     * 
     * @return The character name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the character's icon.
     * 
     * @return The character icon
     */
    public ImageIcon getIcon() {
        return icon;
    }
    
    /**
     * Gets the character's color.
     * 
     * @return The color associated with this character
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * Gets the default color for a character name.
     * 
     * @param characterName The character name
     * @return A color for the character
     */
    private Color getDefaultColorForName(String characterName) {
        for (int i = 0; i < DEFAULT_CHARACTERS.length; i++) {
            if (DEFAULT_CHARACTERS[i].equalsIgnoreCase(characterName)) {
                return DEFAULT_COLORS[i];
            }
        }
        // Fallback to a random color
        return DEFAULT_COLORS[Math.abs(characterName.hashCode()) % DEFAULT_COLORS.length];
    }
    
    /**
     * Creates a basic icon for a character if no image file is available.
     * 
     * @param characterName The name of the character
     * @return An ImageIcon for the character
     */
    private ImageIcon createIconForCharacter(String characterName) {
        // Special case for Boot character - use monopoly-shoe-512.png directly
        if (characterName.equalsIgnoreCase("Boot")) {
            // Use the PNG image directly
            ImageIcon shoeIcon = GameUtils.loadImageWithFallback("images/monopoly-shoe-512.png");
            
            // If the image loaded successfully
            if (shoeIcon.getIconWidth() > 0) {
                // Scale the image to appropriate size for token
                Image originalImage = shoeIcon.getImage();
                int size = 40; // Standard size for character tokens
                Image scaledImage = originalImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        }
        
        // Try to load from PNG file for other characters or if Boot special case failed
        // For Boot, this will no longer be used as the special case above should succeed
        String filename = "images/" + characterName.toLowerCase() + ".png";
        ImageIcon fileIcon = GameUtils.loadImageWithFallback(filename);
        
        // If the image loaded successfully and has valid dimensions
        if (fileIcon.getIconWidth() > 0) {
            return fileIcon;
        }
        
        // Create a generated icon
        int size = 30;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Get the character's color
        Color color = getDefaultColorForName(characterName);
        
        // Draw a colored circle with the first letter
        g2d.setColor(color);
        g2d.fillOval(0, 0, size - 1, size - 1);
        g2d.setColor(Color.WHITE);
        g2d.drawOval(0, 0, size - 1, size - 1);
        
        // Draw the first letter
        g2d.setFont(g2d.getFont().deriveFont(18f));
        String letter = characterName.substring(0, 1).toUpperCase();
        int letterWidth = g2d.getFontMetrics().stringWidth(letter);
        g2d.drawString(letter, (size - letterWidth) / 2, (size + 12) / 2);
        
        g2d.dispose();
        
        return new ImageIcon(image);
    }
    
    @Override
    public String toString() {
        return name;
    }
} 