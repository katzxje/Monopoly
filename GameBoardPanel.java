import javax.swing.*;
import javax.swing.plaf.basic.BasicLabelUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Visual representation of the Monopoly game board.
 */
public class GameBoardPanel extends JPanel {
    private static final int BOARD_SIZE = 40;
    private int tileSize; // Tile size will be dynamically calculated based on scaling
    private int boardDimension; // Board dimension will be calculated from tile size
    
    private List<Space> spaces;
    private Map<Player, Point> playerPositions = new HashMap<>();
    private Map<Player, JLabel> playerTokens = new HashMap<>();
    private Map<Integer, JLabel> ownershipMarkers = new HashMap<>(); // Markers for property ownership
    private JPanel boardPanel;
    private JPanel[] tiles = new JPanel[BOARD_SIZE];
    private GameEngine gameEngine; // Reference to game engine for accessing player info
    private double currentScale = 1.0;
    
    // Map để lưu animation đang chạy cho từng player (chống giật)
    private final Map<Player, Timer> runningAnimations = new HashMap<>();
    
    /**
     * Creates a new game board panel.
     * 
     * @param spaces The list of spaces on the board
     */
    public GameBoardPanel(List<Space> spaces) {
        this.spaces = spaces;
        
        // Initialize scaling
        GameUtils.initializeScaling();
        currentScale = GameUtils.getScaleFactor();
        tileSize = GameUtils.scale(80); // Reduced from 100 to 80
        boardDimension = 11 * tileSize; // 11 spaces per side
        
        setLayout(new BorderLayout(0, 0));
        setPreferredSize(new Dimension(boardDimension, boardDimension));
        
        initializeBoard();
    }
    
    /**
     * Creates a new game board panel with game engine.
     * 
     * @param spaces The list of spaces on the board
     * @param gameEngine The game engine
     */
    public GameBoardPanel(List<Space> spaces, GameEngine gameEngine) {
        this.spaces = spaces;
        this.gameEngine = gameEngine;
        
        // Initialize scaling
        GameUtils.initializeScaling();
        currentScale = GameUtils.getScaleFactor();
        tileSize = GameUtils.scale(80); // Reduced from 100 to 80
        boardDimension = 11 * tileSize; // 11 spaces per side
        
        setLayout(new BorderLayout(0, 0));
        setPreferredSize(new Dimension(boardDimension, boardDimension));
        setMinimumSize(new Dimension(boardDimension, boardDimension));
        
        initializeBoard();
    }
    
    /**
     * Initialize the visual game board.
     */
    private void initializeBoard() {
        boardPanel = new JPanel(null); // Using null layout for absolute positioning
        boardPanel.setPreferredSize(new Dimension(boardDimension, boardDimension));
        boardPanel.setBackground(new Color(233, 247, 239)); // Light green background
        boardPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 0), GameUtils.scale(3))); // Add border to define board edge
        
        // Create the border of tiles
        createTiles();
        
        // Center area of the board
        JPanel centerPanel = new JPanel(new BorderLayout(0, 0));
        centerPanel.setBounds(tileSize, tileSize, 9 * tileSize, 9 * tileSize);
        centerPanel.setBackground(new Color(233, 247, 239));
        
        // Add Monopoly logo to center with improved look
        try {
            ImageIcon logoIcon = new ImageIcon("images/monopoly-logo.png");
            Image scaledImage = logoIcon.getImage().getScaledInstance(
                    GameUtils.scale(400), GameUtils.scale(160), Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            centerPanel.add(logoLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            // Fallback to text if image is not found
            JLabel logoLabel = new JLabel("MONOPOLY");
            logoLabel.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 64));
            logoLabel.setForeground(new Color(0, 102, 0)); // Dark green color
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            centerPanel.add(logoLabel, BorderLayout.CENTER);
        }
        
        // Add Chance and Community Chest decorations
        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, GameUtils.scale(80), GameUtils.scale(30)));
        cardsPanel.setOpaque(false);
        
        JLabel chanceLabel = new JLabel("CHANCE");
        chanceLabel.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 22));
        chanceLabel.setForeground(new Color(255, 165, 0)); // Orange
        chanceLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, GameUtils.scale(2)),
                BorderFactory.createEmptyBorder(
                        GameUtils.scale(8), GameUtils.scale(16), 
                        GameUtils.scale(8), GameUtils.scale(16))));
        
        JLabel communityChestLabel = new JLabel("COMMUNITY CHEST");
        communityChestLabel.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 20));
        communityChestLabel.setForeground(new Color(0, 100, 255)); // Blue
        communityChestLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, GameUtils.scale(2)),
                BorderFactory.createEmptyBorder(
                        GameUtils.scale(8), GameUtils.scale(16), 
                        GameUtils.scale(8), GameUtils.scale(16))));
        
        cardsPanel.add(chanceLabel);
        cardsPanel.add(communityChestLabel);
        
        centerPanel.add(cardsPanel, BorderLayout.SOUTH);
        
        boardPanel.add(centerPanel);
        
        // Use a wrapper panel to center the board
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBackground(new Color(230, 230, 230)); // Light gray background
        
        // Add the board panel to the wrapper with proper centering
        wrapperPanel.add(boardPanel);
        
        add(wrapperPanel, BorderLayout.CENTER);
    }
    
    /**
     * Create all the tiles around the board's border.
     */
    private void createTiles() {
        // Create all tiles
        for (int i = 0; i < BOARD_SIZE; i++) {
            tiles[i] = createTile(i);
            boardPanel.add(tiles[i]);
        }
    }
    
    /**
     * Create an individual tile with proper position and rotation.
     * 
     * @param index The index of the tile (0-39)
     * @return The created tile panel
     */
    private JPanel createTile(int index) {
        JPanel tile = new JPanel();
        String spaceName = (index < spaces.size()) ? spaces.get(index).getName() : "Space " + index;
        Color tileColor = getTileColor(index);
        
        // Special case for Jail (index 10)
        if (index == 10) {
            // Create custom jail tile with image
            tile.setLayout(new BorderLayout());
            
            // Try to load prison image
            ImageIcon prisonIcon = GameUtils.loadImageWithFallback("images/prision.png");
            if (prisonIcon.getIconWidth() > 0) {
                // Scale image to fit tile
                Image originalImage = prisonIcon.getImage();
                Image scaledImage = originalImage.getScaledInstance(tileSize - 4, tileSize - 4, Image.SCALE_SMOOTH);
                
                // Add prison image
                JLabel prisonLabel = new JLabel(new ImageIcon(scaledImage));
                prisonLabel.setHorizontalAlignment(SwingConstants.CENTER);
                prisonLabel.setVerticalAlignment(SwingConstants.CENTER);
                tile.add(prisonLabel, BorderLayout.CENTER);
            } else {
                // Fallback to default text if image fails to load
                JLabel nameLabel = new JLabel("<html><div style='text-align:center'>Jail /<br>Just Visiting</div></html>");
                nameLabel.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 11));
                nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
                tile.add(nameLabel, BorderLayout.CENTER);
            }
        } else {
            // Regular tile creation for other spaces
            // Set tile appearance
            tile.setLayout(new BorderLayout());
            
            // Format the space name (shorten if needed)
            String displayName = formatTileName(spaceName);
            
            // Add the tile name as a label with larger font - adjust font size based on text length
            JLabel nameLabel = new JLabel("<html><div style='text-align:center'>" + displayName + "</div></html>");
            
            // Use consistent font size for all tiles
            int fontSize = 11;
            if (displayName.length() > 15 || displayName.contains("<br>")) {
                fontSize = 10;
            }
            if (displayName.length() > 25) {
                fontSize = 9;
            }
            
            nameLabel.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, fontSize));
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            // Rotate text depending on which side of the board
            if (index >= 11 && index <= 19) { // Right side
                nameLabel.setUI(new VerticalLabelUI(false));
            } else if (index >= 21 && index <= 29) { // Top side
                nameLabel.setUI(new VerticalLabelUI(false));
            }
            
            tile.add(nameLabel, BorderLayout.CENTER);
        }
        
        // Set tile location based on its position on the board
        Point location = getTileLocation(index);
        tile.setBounds(location.x, location.y, tileSize, tileSize);
        
        // Set border and background color for all tiles
        tile.setBorder(BorderFactory.createLineBorder(Color.BLACK, GameUtils.scale(2)));
        tile.setBackground(tileColor);
        
        // Add property interactions for better UX
        if (index < spaces.size() && spaces.get(index) instanceof Buyable) {
            final int tileIndex = index;
            
            // Add hover effect
            tile.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    tile.setBorder(BorderFactory.createLineBorder(Color.YELLOW, GameUtils.scale(3)));
                    
                    // Could show tooltip here if desired
                    Buyable property = (Buyable) spaces.get(tileIndex);
                    String info = property.getName() + " - $" + property.getPrice();
                    if (property.getOwner() != null) {
                        info += " (owned by " + property.getOwner().getName() + ")";
                    }
                    tile.setToolTipText(info);
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    tile.setBorder(BorderFactory.createLineBorder(Color.BLACK, GameUtils.scale(2)));
                }
            });
        }
        
        return tile;
    }
    
    /**
     * Format tile name to fit better on the board.
     * 
     * @param name Original name
     * @return Formatted name
     */
    private String formatTileName(String name) {
        // Split long names with line breaks for better fit
        if (name.length() > 10) {
            // Handle special cases
            if (name.contains("Railroad")) {
                return name.replace("Railroad", "RR");
            } else if (name.contains("Avenue")) {
                return name.replace("Avenue", "Ave");
            } else if (name.contains("Place")) {
                return name.replace("Place", "Pl");
            } else if (name.contains("Community Chest")) {
                return "Community<br>Chest";
            } else if (name.contains("New York")) {
                return "New York<br>Ave";
            } else if (name.contains("Tennessee")) {
                return "Tennessee<br>Ave";
            } else if (name.contains("Kentucky")) {
                return "Kentucky<br>Ave";
            } else if (name.contains("Indiana")) {
                return "Indiana<br>Ave";
            } else if (name.contains("Illinois")) {
                return "Illinois<br>Ave";
            } else if (name.contains("Atlantic")) {
                return "Atlantic<br>Ave";
            } else if (name.contains("Ventnor")) {
                return "Ventnor<br>Ave";
            } else if (name.contains("Marvin")) {
                return "Marvin<br>Gardens";
            } else if (name.contains("Pacific")) {
                return "Pacific<br>Ave";
            } else if (name.contains("Carolina")) {
                return "N Carolina<br>Ave";
            } else if (name.contains("Pennsylvania")) {
                return "Penn.<br>Ave";
            } else if (name.contains("Park")) {
                return "Park<br>Place";
            } else if (name.contains("Boardwalk")) {
                return "Board-<br>walk";
            } else if (name.contains("Reading RR")) {
                return "Reading<br>RR";
            } else if (name.contains("B & O RR")) {
                return "B & O<br>RR";
            } else if (name.contains("Short Line")) {
                return "Short Line<br>RR";
            } else if (name.contains("Electric")) {
                return "Electric<br>Company";
            } else if (name.contains("Water")) {
                return "Water<br>Works";
            } else if (name.contains("Luxury")) {
                return "Luxury<br>Tax";
            } else if (name.contains("Income")) {
                return "Income<br>Tax";
            } else if (name.contains("Free")) {
                return "Free<br>Parking";
            } else if (name.contains("Go To")) {
                return "Go To<br>Jail";
            } else if (name.contains("Visiting")) {
                return "Jail /<br>Just Visiting";
            } else if (name.contains("Connecticut")) {
                return "Connecticut<br>Ave";
            } else if (name.contains("Vermont")) {
                return "Vermont<br>Ave";
            } else if (name.contains("Oriental")) {
                return "Oriental<br>Ave";
            } else if (name.contains("Baltic")) {
                return "Baltic<br>Ave";
            } else if (name.contains("Mediterranean")) {
                return "Mediterranean<br>Ave";
            } else if (name.contains("St. Charles")) {
                return "St. Charles<br>Place";
            } else if (name.contains("St. James")) {
                return "St. James<br>Place";
            }
            
            // General case: Add a line break at a space if possible
            if (name.contains(" ")) {
                int middle = name.length() / 2;
                int breakPoint = name.lastIndexOf(" ", middle);
                if (breakPoint > 0) {
                    return name.substring(0, breakPoint) + "<br>" + name.substring(breakPoint + 1);
                }
            }
        }
        return name;
    }
    
    /**
     * Get the location for a tile based on its index.
     * 
     * @param index The tile index (0-39)
     * @return The Point where the tile should be positioned
     */
    private Point getTileLocation(int index) {
        // Bottom row (0-10) - aligned from right to left
        if (index <= 10) {
            return new Point((10 - index) * tileSize, 10 * tileSize);
        }
        // Left column (11-20) - aligned from bottom to top
        else if (index <= 20) {
            return new Point(0, (20 - index) * tileSize);
        }
        // Top row (21-30) - aligned from left to right
        else if (index <= 30) {
            return new Point((index - 20) * tileSize, 0);
        }
        // Right column (31-39) - aligned from top to bottom
        else {
            return new Point(10 * tileSize, (index - 30) * tileSize);
        }
    }
    
    /**
     * Get a color for a tile based on its position/type.
     * 
     * @param index The tile index
     * @return The color for the tile
     */
    private Color getTileColor(int index) {
        // Corner spaces
        if (index == 0) return new Color(213, 232, 212); // GO - light green
        if (index == 10) return new Color(255, 240, 199); // Jail - light yellow
        if (index == 20) return new Color(255, 228, 225); // Free Parking - misty rose
        if (index == 30) return new Color(255, 218, 185); // Go To Jail - peach
        
        // Property colors
        if (index == 1 || index == 3) return new Color(205, 133, 63); // Brown
        if (index == 6 || index == 8 || index == 9) return new Color(135, 206, 250); // Light blue
        if (index == 11 || index == 13 || index == 14) return new Color(255, 182, 193); // Pink
        if (index == 16 || index == 18 || index == 19) return new Color(255, 165, 0); // Orange
        if (index == 21 || index == 23 || index == 24) return new Color(255, 0, 0); // Red
        if (index == 26 || index == 27 || index == 29) return new Color(255, 255, 0); // Yellow
        if (index == 31 || index == 32 || index == 34) return new Color(0, 128, 0); // Green
        if (index == 37 || index == 39) return new Color(0, 0, 255); // Blue
        
        // Railroad and Utilities
        if (index == 5 || index == 15 || index == 25 || index == 35) return new Color(211, 211, 211); // Light gray - railroads
        if (index == 12 || index == 28) return new Color(173, 216, 230); // Light blue - utilities
        
        // Tax spaces
        if (index == 4 || index == 38) return new Color(255, 228, 225); // Misty rose - taxes
        
        // Cards
        if (index == 2 || index == 17 || index == 33) return new Color(255, 218, 185); // Peach - Community Chest
        if (index == 7 || index == 22 || index == 36) return new Color(240, 230, 140); // Khaki - Chance
        
        return Color.WHITE;
    }
    
    /**
     * Adds a player's token to the board.
     * 
     * @param player The player to add
     */
    public void addPlayer(Player player) {
        // Create a token for the player
        JLabel tokenLabel = new JLabel();
        
        // If the player has a character, use its icon
        if (player.getCharacter() != null) {
            ImageIcon icon = player.getCharacter().getIcon();
            if (icon != null) {
                // Scale the icon to a larger size for better visibility
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(
                        GameUtils.scale(48), GameUtils.scale(48), Image.SCALE_SMOOTH);
                tokenLabel.setIcon(new ImageIcon(scaledImg));
            } else {
                // Fallback if no icon
                createPlayerToken(tokenLabel, player);
            }
        } else {
            // Fallback if no character
            createPlayerToken(tokenLabel, player);
        }
        
        // Add the token to the board
        playerTokens.put(player, tokenLabel);
        boardPanel.add(tokenLabel);
        
        // Position the token on the GO space
        updatePlayerPosition(player);
    }
    
    /**
     * Creates a visually appealing player token.
     */
    private void createPlayerToken(JLabel tokenLabel, Player player) {
        // Get a color for the player
        Color playerColor = getPlayerColor(player);
        
        // Create a custom token using the first letter of the player's name
        String letter = player.getName().substring(0, 1).toUpperCase();
        
        // Create a token image with scaled size
        int size = GameUtils.scale(48);
        BufferedImage tokenImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = tokenImage.createGraphics();
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw a colored circle
        g2d.setColor(playerColor);
        g2d.fillOval(0, 0, size, size);
        
        // Draw a border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(GameUtils.scale(3)));
        g2d.drawOval(0, 0, size, size);
        
        // Draw the letter - with scaled font
        g2d.setColor(Color.WHITE);
        g2d.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 30));
        FontMetrics fm = g2d.getFontMetrics();
        int letterWidth = fm.stringWidth(letter);
        int letterHeight = fm.getHeight();
        g2d.drawString(letter, (size - letterWidth) / 2, (size + letterHeight / 2) / 2);
        
        g2d.dispose();
        
        tokenLabel.setIcon(new ImageIcon(tokenImage));
    }
    
    /**
     * Updates a player's position on the board.
     * 
     * @param player The player to update
     */
    public void updatePlayerPosition(Player player) {
        int position = player.getPosition();
        JLabel tokenLabel = playerTokens.get(player);
        
        if (tokenLabel != null) {
            // Get the base position for this space
            Point tileLocation = getTileLocation(position);
            
            // Use the offsetPositionForPlayer method to properly position the token
            Point adjustedLocation = offsetPositionForPlayer(tileLocation, player);
            playerPositions.put(player, adjustedLocation);
            
            // Set the token's position
            tokenLabel.setBounds(
                adjustedLocation.x,
                adjustedLocation.y,
                tokenLabel.getIcon().getIconWidth(),
                tokenLabel.getIcon().getIconHeight()
            );
            
            // Ensure player tokens are at the top layer
            boardPanel.setComponentZOrder(tokenLabel, 0);
            
            // Repaint the board
            revalidate();
            repaint();
        }
    }
    
    /**
     * Static class containing easing functions for animations
     */
    private static class AnimationSystem {
        // Different easing functions for animation
        public static double easeInOutQuad(double t) {
            return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
        }
        
        public static double easeOutQuad(double t) {
            return 1 - (1 - t) * (1 - t);
        }
        
        public static double easeInOutCubic(double t) {
            return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
        }
        
        public static double easeOutBounce(double t) {
            double n1 = 7.5625;
            double d1 = 2.75;
            
            if (t < 1 / d1) {
                return n1 * t * t;
            } else if (t < 2 / d1) {
                return n1 * (t -= 1.5 / d1) * t + 0.75;
            } else if (t < 2.5 / d1) {
                return n1 * (t -= 2.25 / d1) * t + 0.9375;
            } else {
                return n1 * (t -= 2.625 / d1) * t + 0.984375;
            }
        }
        
        public static double easeOutElastic(double t) {
            double c4 = (2 * Math.PI) / 3;
            
            return t == 0 ? 0 : t == 1 ? 1 :
                Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * c4) + 1;
        }
        
        public static double easeInOutBack(double t) {
            double c1 = 1.70158;
            double c2 = c1 * 1.525;
            
            return t < 0.5
                ? (Math.pow(2 * t, 2) * ((c2 + 1) * 2 * t - c2)) / 2
                : (Math.pow(2 * t - 2, 2) * ((c2 + 1) * (t * 2 - 2) + c2) + 2) / 2;
        }
    }
    
    /**
     * Calculates an offset position for a player token within a tile.
     * 
     * @param baseLocation The base tile location
     * @param player The player to position
     * @return The offset position for the player's token
     */
    private Point offsetPositionForPlayer(Point baseLocation, Player player) {
        JLabel tokenLabel = playerTokens.get(player);
        if (tokenLabel == null) {
            return baseLocation;
        }
        
        int position = player.getPosition();
        int tokenWidth = tokenLabel.getWidth();
        int tokenHeight = tokenLabel.getHeight();
        
        // Count how many players are on this space
        int playerCount = 0;
        int playerIndex = 0;
        
        for (Player p : playerPositions.keySet()) {
            if (p.getPosition() == position) {
                playerCount++;
                if (p == player) {
                    playerIndex = playerCount - 1;
                }
            }
        }
        
        // Calculate offset based on player count
        int offsetX, offsetY;
        
        if (playerCount <= 1) {
            // Center of tile
            offsetX = (tileSize - tokenWidth) / 2;
            offsetY = (tileSize - tokenHeight) / 2;
        } else if (playerCount <= 2) {
            // Two players side by side
            offsetX = playerIndex * (tileSize / 2) + (tileSize / 4) - (tokenWidth / 2);
            offsetY = (tileSize - tokenHeight) / 2;
        } else if (playerCount <= 4) {
            // Four players in corners
            offsetX = (playerIndex % 2) * (tileSize - tokenWidth - GameUtils.scale(10)) + GameUtils.scale(5);
            offsetY = (playerIndex / 2) * (tileSize - tokenHeight - GameUtils.scale(10)) + GameUtils.scale(5);
        } else {
            // More than 4 players, compact arrangement
            offsetX = (playerIndex % 3) * (tileSize / 3) + (tileSize / 6) - (tokenWidth / 2);
            offsetY = (playerIndex / 3) * (tileSize / 3) + (tileSize / 6) - (tokenHeight / 2);
        }
        
        return new Point(baseLocation.x + offsetX, baseLocation.y + offsetY);
    }
    
    /**
     * Plays a sound effect if available.
     * 
     * @param soundFile The sound file name
     */
    private void playSound(String soundFile) {
        try {
            // Check for sound file in resources
            java.net.URL soundURL = getClass().getResource("/sounds/" + soundFile);
            if (soundURL != null) {
                javax.sound.sampled.AudioInputStream audioIn = 
                    javax.sound.sampled.AudioSystem.getAudioInputStream(soundURL);
                javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            }
        } catch (Exception e) {
            // Silently fail if sound can't be played
        }
    }
    
    /**
     * Updates player position without animation (used internally).
     */
    private void updatePlayerPositionWithoutAnimation(Player player) {
        int position = player.getPosition();
        Point location = getTileLocation(position);
        
        // Get a position within the tile appropriate for this player
        Point adjustedLocation = offsetPositionForPlayer(location, player);
        
        // Update stored position
        playerPositions.put(player, adjustedLocation);
        
        // Get or create player token label
        JLabel tokenLabel = playerTokens.get(player);
        
        if (tokenLabel != null) {
            // Update token position
            tokenLabel.setLocation(adjustedLocation.x, adjustedLocation.y);
            boardPanel.setComponentZOrder(tokenLabel, 0); // Ensure token is on top
        }
    }
    
    /**
     * Updates property ownership visuals on the board.
     * 
     * @param property The property being updated
     */
    public void updatePropertyOwnership(Buyable property) {
        int position = -1;
        
        // Find the position of this property on the board
        for (int i = 0; i < spaces.size(); i++) {
            if (spaces.get(i) == property) {
                position = i;
                break;
            }
        }
        
        if (position >= 0) {
            // Get the owner
            Player owner = property.getOwner();
            
            if (owner != null) {
                Point location = getTileLocation(position);
                
                // Create or update ownership marker
                JLabel marker = ownershipMarkers.get(position);
                if (marker == null) {
                    marker = new JLabel();
                    marker.setOpaque(true);
                    marker.setBorder(BorderFactory.createLineBorder(Color.BLACK, GameUtils.scale(2)));
                    boardPanel.add(marker);
                    ownershipMarkers.put(position, marker);
                }
                
                // Set color based on player index
                Color ownerColor = getPlayerColor(owner);
                marker.setBackground(ownerColor);
                
                // Position the marker at the top of the tile (make it larger and ensure it's visible)
                marker.setBounds(
                        location.x + tileSize - GameUtils.scale(20), 
                        location.y + GameUtils.scale(4), 
                        GameUtils.scale(16), 
                        GameUtils.scale(16));
                
                // Ensure the marker is visible (bring to front)
                boardPanel.setComponentZOrder(marker, 0);
                
            } else if (ownershipMarkers.containsKey(position)) {
                // Remove marker if property is no longer owned
                boardPanel.remove(ownershipMarkers.get(position));
                ownershipMarkers.remove(position);
            }
            
            // Repaint the board
            revalidate();
            repaint();
        }
    }
    
    /**
     * Gets a color for a player based on their index.
     * 
     * @param player The player
     * @return A color for this player
     */
    private Color getPlayerColor(Player player) {
        if (gameEngine != null) {
            int index = gameEngine.getPlayers().indexOf(player);
            switch (index) {
                case 0: return new Color(255, 0, 0);    // Red
                case 1: return new Color(0, 0, 255);    // Blue
                case 2: return new Color(0, 180, 0);    // Green
                case 3: return new Color(255, 165, 0);  // Orange
                default: return Color.GRAY;
            }
        } else {
            // If no game engine, use hash code to generate a consistent color
            int hash = player.hashCode();
            return new Color(
                    Math.abs(hash) % 200 + 55,
                    Math.abs(hash / 256) % 200 + 55,
                    Math.abs(hash / 65536) % 200 + 55
            );
        }
    }
    
    /**
     * Get the position index for a space.
     * 
     * @param space The space to look for
     * @return The position index or -1 if not found
     */
    public int getSpacePosition(Space space) {
        for (int i = 0; i < spaces.size(); i++) {
            if (spaces.get(i) == space) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Sets the game engine for this board.
     * 
     * @param gameEngine The game engine reference
     */
    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }
    
    /**
     * Removes a player's token from the board.
     * 
     * @param player The player to remove
     */
    public void removePlayer(Player player) {
        JLabel token = playerTokens.remove(player);
        if (token != null) {
            boardPanel.remove(token);
            playerPositions.remove(player);
            revalidate();
            repaint();
        }
    }
    
    /**
     * Animates a player moving smoothly across multiple spaces (liền mạch, tối ưu hiệu ứng)
     */
    public void animatePlayerMovement(Player player, int steps, Runnable onDone) {
        if (runningAnimations.containsKey(player)) {
            Timer old = runningAnimations.remove(player);
            if (old != null) old.stop();
        }
        final int boardSize = BOARD_SIZE;
        final int start = player.getPosition();
        final int end = (start + steps) % boardSize;
        final int totalSteps = steps;
        final int totalFrames = Math.max(60, steps * 15); // More frames for smoother and slower animation
        final int frameDelay = 16; // ~60fps for smoother animation but slower movement
        final int originalWidth = playerTokens.get(player).getWidth();
        final int originalHeight = playerTokens.get(player).getHeight();
        final JLabel playerToken = playerTokens.get(player);
        final ImageIcon originalIcon = (ImageIcon) playerToken.getIcon();
        
        // Build the path of positions
        java.util.List<Integer> path = new java.util.ArrayList<>();
        for (int i = 0; i <= steps; i++) {
            path.add((start + i) % boardSize);
        }
        
        // Calculate pixel coordinates for each point in the path
        java.util.List<Point> pixelPath = new java.util.ArrayList<>();
        for (int idx : path) {
            Point tile = getTileLocation(idx);
            Point offset = offsetPositionForPlayer(tile, player);
            pixelPath.add(offset);
        }
        
        // Generate intermediate points for smoother curves between tiles
        java.util.List<Point> smoothPath = generateSmoothPath(pixelPath);
        
        // Animation state
        final int[] frame = {0};
        final int pathLen = smoothPath.size();
        final boolean[] passedGo = {false};
        final double[] cumulativeRotation = {0}; // Track total rotation for spinning effect
        
        // Create trail effects list
        final java.util.List<TrailEffect> trailEffects = new java.util.ArrayList<>();
        
        Timer timer = new Timer(frameDelay, null);
        runningAnimations.put(player, timer);
        timer.addActionListener(e -> {
            double progress = (double) frame[0] / totalFrames;
            
            // Use more advanced easing functions
            double eased;
            if (progress < 0.3) {
                // Accelerate at the start (slower acceleration)
                eased = AnimationSystem.easeOutQuad(progress / 0.3) * 0.3;
            } else if (progress > 0.85) {
                // Decelerate at the end (longer deceleration)
                eased = 0.85 + AnimationSystem.easeInOutBack((progress - 0.85) / 0.15) * 0.15;
            } else {
                // Move at consistent speed in the middle
                eased = 0.3 + (progress - 0.3) * (0.55 / 0.55);
            }
            
            // Calculate smooth position along the path
            double pos = eased * (pathLen - 1);
            int idx0 = (int) Math.floor(pos);
            int idx1 = Math.min(idx0 + 1, pathLen - 1);
            double localT = pos - idx0;
            
            // Get the current and next points
            Point p0 = smoothPath.get(idx0);
            Point p1 = smoothPath.get(idx1);
            
            // Linear interpolation between points
            int x = (int) (p0.x + (p1.x - p0.x) * localT);
            int y = (int) (p0.y + (p1.y - p0.y) * localT);
            
            // Calculate movement speed for dynamic effects
            double speed = idx0 < smoothPath.size() - 2 ? 
                    distanceBetweenPoints(p0, p1) : 0;
            
            // Multi-sine wave for natural bounce effect
            double bounce = Math.sin(progress * Math.PI * 2) * GameUtils.scale(4);
            bounce += Math.sin(progress * Math.PI * 5) * GameUtils.scale(1);
            
            // Speed-based scaling - stretch in direction of movement
            double baseScale = 1.0 + Math.sin(progress * Math.PI * 2) * 0.05; // Reduced scaling
            double scaleX = baseScale;
            double scaleY = baseScale;
            
            // Add speed-based stretching in movement direction
            double stretchAmount = Math.min(speed / 30, 0.2); // Reduced stretching
            if (Math.abs(p1.x - p0.x) > Math.abs(p1.y - p0.y)) {
                // Horizontal movement
                scaleX += stretchAmount;
                scaleY -= stretchAmount * 0.5;
            } else {
                // Vertical movement
                scaleY += stretchAmount;
                scaleX -= stretchAmount * 0.5;
            }
            
            int newWidth = (int)(originalWidth * scaleX);
            int newHeight = (int)(originalHeight * scaleY);
            int offsetX = (newWidth - originalWidth) / 2;
            int offsetY = (newHeight - originalHeight) / 2;
            
            // Enhanced rotation animation - reduced rotation
            int direction = getMovementDirection(path.get(Math.min(idx0 / (smoothPath.size() / path.size()), path.size() - 1)));
            double baseRotation = Math.sin(progress * Math.PI * 2) * 0.05; // Reduced wobble
            
            // Add speed-based rotation
            double speedRotation = stretchAmount * direction * 0.5; // Reduced rotation
            cumulativeRotation[0] += speedRotation;
            
            // Combined rotation with limits
            double rotation = baseRotation + Math.sin(cumulativeRotation[0]) * 0.1;
            
            // Apply calculated position - do not apply rotation directly to avoid blurring
            playerToken.setBounds(x - offsetX, y - offsetY - (int)bounce, newWidth, newHeight);
            
            // Only rotate icon on significant rotation changes to prevent blurriness
            if (frame[0] % 5 == 0 && Math.abs(rotation) > 0.03) {
                applyRotationToToken(playerToken, rotation, originalIcon);
            }
            
            playerPositions.put(player, new Point(x, y));
            
            // Add trail effect at certain intervals - reduced frequency
            if (frame[0] % 6 == 0 && progress > 0.1 && progress < 0.9 && speed > 5) {
                trailEffects.add(new TrailEffect(x, y, 
                        speed * 0.08, // Size based on speed
                        new Color(
                                getPlayerColor(player).getRed(), 
                                getPlayerColor(player).getGreen(), 
                                getPlayerColor(player).getBlue(), 
                                100))); // Reduced opacity
            }
            
            // Update trail effects
            for (int i = trailEffects.size() - 1; i >= 0; i--) {
                TrailEffect trail = trailEffects.get(i);
                trail.update();
                if (trail.isExpired()) {
                    trailEffects.remove(i);
                } else {
                    trail.draw(boardPanel.getGraphics());
                }
            }
            
            // Update logical position based on progress
            int logicalIdx = (int)(eased * path.size());
            if (logicalIdx < path.size()) {
                int logicPos = path.get(logicalIdx);
                player.setPosition(logicPos);
                
                // Check if passed GO
                if (!passedGo[0]) {
                    for (int i = 1; i <= logicalIdx; i++) {
                        if (path.get(i - 1) != 0 && path.get(i) == 0) {
                            animatePassGo(player);
                            playSound("go.wav");
                            passedGo[0] = true;
                        }
                    }
                }
            }
            
            repaint();
            frame[0]++;
            
            if (frame[0] > totalFrames) {
                timer.stop();
                runningAnimations.remove(player);
                player.setPosition(end);
                
                // Restore original icon to ensure clarity
                playerToken.setIcon(originalIcon); 
                
                // Update final position properly
                updatePlayerPosition(player);
                playerToken.setSize(originalWidth, originalHeight);
                
                // Clear any remaining trail effects
                trailEffects.clear();
                
                // Play landing sound with slight delay
                Timer landSoundTimer = new Timer(100, event -> playSound("land.wav"));
                landSoundTimer.setRepeats(false);
                landSoundTimer.start();
                
                if (onDone != null) onDone.run();
            }
        });
        timer.start();
    }
    
    /**
     * Generates a smooth path with additional points between tiles for a more natural curve
     */
    private List<Point> generateSmoothPath(List<Point> originalPath) {
        if (originalPath.size() <= 2) {
            return new ArrayList<>(originalPath);
        }
        
        List<Point> smoothPath = new ArrayList<>();
        
        // Add the first point
        smoothPath.add(originalPath.get(0));
        
        // Generate additional points between each pair of original points
        for (int i = 0; i < originalPath.size() - 1; i++) {
            Point current = originalPath.get(i);
            Point next = originalPath.get(i + 1);
            
            // Add intermediate points (more on corners, fewer on straights)
            int pointsToAdd = isCornerTransition(i, originalPath) ? 4 : 2;
            
            for (int j = 1; j <= pointsToAdd; j++) {
                double t = (double) j / (pointsToAdd + 1);
                
                // For corners, add a slight curve
                if (isCornerTransition(i, originalPath)) {
                    Point midPoint = new Point(
                        (int)(current.x + (next.x - current.x) * t),
                        (int)(current.y + (next.y - current.y) * t)
                    );
                    
                    // Add a slight offset to create a curve
                    if (j > 1 && j < pointsToAdd) {
                        // Determine curve direction based on the board quadrant
                        int curveDir = (i % 10 == 9) ? 1 : -1;
                        int curveAmount = GameUtils.scale(5);
                        
                        if (Math.abs(next.x - current.x) > Math.abs(next.y - current.y)) {
                            // Horizontal movement - curve vertically
                            midPoint.y += curveDir * curveAmount * Math.sin(t * Math.PI);
                        } else {
                            // Vertical movement - curve horizontally
                            midPoint.x += curveDir * curveAmount * Math.sin(t * Math.PI);
                        }
                    }
                    smoothPath.add(midPoint);
                } else {
                    // Simple linear interpolation for straight lines
                    smoothPath.add(new Point(
                        (int)(current.x + (next.x - current.x) * t),
                        (int)(current.y + (next.y - current.y) * t)
                    ));
                }
            }
            
            // Add the next original point
            smoothPath.add(next);
        }
        
        return smoothPath;
    }
    
    /**
     * Determines if a transition is a corner (90 degree turn)
     */
    private boolean isCornerTransition(int index, List<Point> path) {
        if (index == 0 || index >= path.size() - 2) {
            return false;
        }
        
        Point prev = path.get(index);
        Point current = path.get(index + 1);
        Point next = path.get(index + 2);
        
        boolean horizontalFirst = Math.abs(current.x - prev.x) > Math.abs(current.y - prev.y);
        boolean horizontalSecond = Math.abs(next.x - current.x) > Math.abs(next.y - current.y);
        
        return horizontalFirst != horizontalSecond;
    }
    
    /**
     * Calculates the distance between two points
     */
    private double distanceBetweenPoints(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
    }
    
    /**
     * Trail effect for player movement
     */
    private class TrailEffect {
        private int x, y;
        private double size;
        private Color color;
        private int lifetime = 12;
        
        public TrailEffect(int x, int y, double size, Color color) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
        }
        
        public void update() {
            lifetime--;
            // Fade out
            int alpha = color.getAlpha();
            if (alpha > 10) {
                alpha -= 10;
                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            }
            
            // Expand slightly
            size *= 1.05;
        }
        
        public boolean isExpired() {
            return lifetime <= 0;
        }
        
        public void draw(Graphics g) {
            if (g == null) return;
            
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            
            int s = (int)size;
            g2d.fillOval(x - s/2, y - s/2, s, s);
            g2d.dispose();
        }
    }
    
    /**
     * Applies rotation to a token icon with improved quality
     */
    private void applyRotationToToken(JLabel tokenLabel, double angle, ImageIcon originalIcon) {
        if (originalIcon == null) return;
        
        // Use the original icon as source to prevent quality loss
        int width = originalIcon.getIconWidth();
        int height = originalIcon.getIconHeight();
        
        // Create buffered image for rotation
        BufferedImage rotatedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotatedImage.createGraphics();
        
        // Set rendering hints for smoother rotation
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        
        // Rotate around center
        AffineTransform at = new AffineTransform();
        at.translate(width / 2, height / 2);
        at.rotate(angle);
        at.translate(-width / 2, -height / 2);
        g2d.setTransform(at);
        
        // Draw the original image
        g2d.drawImage(originalIcon.getImage(), 0, 0, null);
        g2d.dispose();
        
        // Update the token with rotated image
        tokenLabel.setIcon(new ImageIcon(rotatedImage));
    }
    
    /**
     * Determines the direction of movement on the board (clockwise/counterclockwise)
     * for rotation effects.
     * 
     * @param position Current position on board
     * @return Direction factor (-1 or 1)
     */
    private int getMovementDirection(int position) {
        // This determines if we should rotate clockwise or counterclockwise
        // based on which side of the board we're on
        if (position < 10 || (position >= 20 && position < 30)) {
            return -1; // Moving left or right
        } else {
            return 1;  // Moving up or down
        }
    }
    
    /**
     * Animates a monetary effect when passing GO.
     * 
     * @param player The player passing GO
     */
    private void animatePassGo(Player player) {
        JLabel moneyEffect = new JLabel("+$200");
        moneyEffect.setForeground(new Color(0, 180, 0)); // Bright green
        moneyEffect.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 28));
        
        Point position = playerPositions.get(player);
        moneyEffect.setBounds(
                position.x - GameUtils.scale(30), 
                position.y - GameUtils.scale(40), 
                GameUtils.scale(100), 
                GameUtils.scale(40));
        boardPanel.add(moneyEffect, 0); // Add to top level
        
        // Create enhanced animation for money effect
        Timer timer = new Timer(16, null); // ~60fps for smoother animation
        final int[] frame = {0};
        final int totalFrames = 40; // More frames for smoother animation
        
        // Keep track of particles
        List<MoneyParticle> particles = new ArrayList<>();
        final int particleCount = 12;
        
        // Create particles for enhanced effect
        for (int i = 0; i < particleCount; i++) {
            particles.add(new MoneyParticle(
                position.x, 
                position.y,
                Math.random() * 2 - 1, // Random x velocity
                -Math.random() * 2 - 1  // Random upward y velocity
            ));
        }
        
        timer.addActionListener(e -> {
            if (frame[0] < totalFrames) {
                // Calculate progress
                double progress = (double)frame[0] / totalFrames;
                
                // Move upward with enhanced parabolic path 
                int x = position.x - GameUtils.scale(30);
                int y = position.y - GameUtils.scale(40) - 
                       (int)(AnimationSystem.easeOutBounce(progress) * GameUtils.scale(60));
                
                // Scale up initially then down
                double scale = 1.0 + AnimationSystem.easeInOutBack(progress) * 0.7;
                
                // Apply scaling to font with bouncy effect
                Font currentFont = GameUtils.getScaledFont("Arial", Font.BOLD, (int)(28 * scale));
                moneyEffect.setFont(currentFont);
                
                // Apply position and opacity
                moneyEffect.setLocation(x, y);
                int alpha = 255;
                if (progress > 0.7) {
                    // Fade out towards the end
                    alpha = (int)(255 * (1.0 - (progress - 0.7) / 0.3));
                }
                moneyEffect.setForeground(new Color(0, 180, 0, alpha));
                
                // Update and draw particles
                for (MoneyParticle particle : particles) {
                    particle.update();
                    particle.draw(boardPanel.getGraphics());
                }
                
                frame[0]++;
            } else {
                timer.stop();
                boardPanel.remove(moneyEffect);
                boardPanel.repaint();
            }
        });
        
        timer.start();
    }
    
    /**
     * Particle class for money animation effects
     */
    private class MoneyParticle {
        private double x, y;
        private double vx, vy;
        private double gravity = 0.1;
        private int size;
        private Color color;
        
        public MoneyParticle(int x, int y, double vx, double vy) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.size = (int)(Math.random() * 5) + 2;
            this.color = new Color(0, 150, 0, 200);
        }
        
        public void update() {
            x += vx;
            y += vy;
            vy += gravity;
            
            // Fade out
            int alpha = color.getAlpha();
            if (alpha > 5) {
                alpha -= 5;
                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            }
        }
        
        public void draw(Graphics g) {
            if (g == null) return;
            
            g.setColor(color);
            g.fillRect((int)x, (int)y, size, size);
        }
    }
}

/**
 * UI delegate for vertical labels (used for the sides of the board).
 */
class VerticalLabelUI extends BasicLabelUI {
    private boolean clockwise;
    
    public VerticalLabelUI(boolean clockwise) {
        this.clockwise = clockwise;
    }
    
    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension dim = super.getPreferredSize(c);
        return new Dimension(dim.height, dim.width);
    }
    
    @Override
    public void paint(Graphics g, JComponent c) {
        JLabel label = (JLabel) c;
        String text = label.getText();
        
        // Check if we have HTML content
        boolean isHtml = text.contains("<html>");
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
        if (isHtml) {
            // For HTML content, we'll draw the label normally and then rotate the entire graphics context
            Dimension d = c.getSize();
            
            // Save the original transform
            AffineTransform originalTransform = g2.getTransform();
            
            // Rotate and translate as needed
            if (clockwise) {
                g2.rotate(Math.PI / 2);
                g2.translate(0, -d.width);
            } else {
                g2.rotate(-Math.PI / 2);
                g2.translate(-d.height, 0);
            }
            
            // Paint the unrotated label using the superclass implementation
            // but with swapped width/height
            super.paint(g, c);
            
            // Restore the original transform
            g2.setTransform(originalTransform);
        } else {
            // Legacy implementation for non-HTML content
            FontMetrics fm = g2.getFontMetrics();
            
            if (clockwise) {
                g2.rotate(Math.PI / 2, c.getWidth() / 2.0, c.getHeight() / 2.0);
            } else {
                g2.rotate(-Math.PI / 2, c.getWidth() / 2.0, c.getHeight() / 2.0);
            }
            
            int stringWidth = SwingUtilities.computeStringWidth(fm, text);
            int x = (c.getWidth() - stringWidth) / 2;
            int y = (c.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            
            g2.drawString(text, x, y);
        }
    }
} 