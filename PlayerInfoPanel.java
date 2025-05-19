import javax.swing.*;
import java.awt.*;
import javax.swing.border.LineBorder;

/**
 * Panel for displaying player information including character.
 */
public class PlayerInfoPanel extends JPanel {
    private Player player;
    private JLabel nameLabel;
    private JLabel moneyLabel;
    private JLabel positionLabel;
    private JLabel statusLabel;
    private JLabel characterImage;
    private Font infoFont; // Font will be scaled based on resolution
    private boolean isCurrentPlayer;
    
    /**
     * Creates a new player info panel.
     * 
     * @param player The player to display information for
     */
    public PlayerInfoPanel(Player player) {
        this.player = player;
        
        // Initialize scaling and font
        infoFont = GameUtils.getScaledFont("Arial", Font.BOLD, 18); // Increased base font size
        
        initComponents();
        updateDisplay();
    }
    
    /**
     * Initializes the panel components.
     */
    private void initComponents() {
        setLayout(new BorderLayout(GameUtils.scale(8), GameUtils.scale(8)));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, GameUtils.scale(2)),
                BorderFactory.createEmptyBorder(
                    GameUtils.scale(8), GameUtils.scale(8), 
                    GameUtils.scale(8), GameUtils.scale(8))
        ));
        
        // Character image on the left
        characterImage = new JLabel();
        characterImage.setPreferredSize(new Dimension(
                GameUtils.scale(70), GameUtils.scale(70)));
        add(characterImage, BorderLayout.WEST);
        
        // Player info on the right
        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 0, GameUtils.scale(5)));
        nameLabel = new JLabel();
        nameLabel.setFont(infoFont);
        
        moneyLabel = new JLabel();
        moneyLabel.setFont(infoFont);
        
        positionLabel = new JLabel();
        positionLabel.setFont(infoFont);
        
        statusLabel = new JLabel();
        statusLabel.setFont(infoFont);
        
        infoPanel.add(nameLabel);
        infoPanel.add(moneyLabel);
        infoPanel.add(positionLabel);
        infoPanel.add(statusLabel);
        
        add(infoPanel, BorderLayout.CENTER);
    }
    
    /**
     * Updates the display with current player information.
     */
    public void updateDisplay() {
        if (player == null) return;
        
        nameLabel.setText("Name: " + player.getName());
        moneyLabel.setText("Money: $" + player.getMoney());
        positionLabel.setText("Position: " + player.getPosition());
        
        String status = player.isInJail() ? "In Jail" : 
                        player.isBankrupt() ? "Bankrupt" : "Active";
        statusLabel.setText("Status: " + status);
        
        // Display character icon if available
        if (player.getCharacter() != null) {
            ImageIcon icon = player.getCharacter().getIcon();
            if (icon != null) {
                // Scale the icon to fit
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(
                        GameUtils.scale(60), GameUtils.scale(60), 
                        Image.SCALE_SMOOTH);
                characterImage.setIcon(new ImageIcon(scaledImg));
            }
        }
    }
    
    /**
     * Updates the player displayed by this panel.
     * 
     * @param player The new player to display
     */
    public void setPlayer(Player player) {
        this.player = player;
        updateDisplay();
    }
    
    /**
     * Highlights this panel to indicate it's the current player's turn.
     * 
     * @param isCurrentPlayer True if this player has the current turn
     */
    public void setCurrentPlayer(boolean isCurrentPlayer) {
        this.isCurrentPlayer = isCurrentPlayer;
        
        if (isCurrentPlayer) {
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.RED, GameUtils.scale(3)),
                    BorderFactory.createEmptyBorder(
                        GameUtils.scale(7), GameUtils.scale(7), 
                        GameUtils.scale(7), GameUtils.scale(7))
            ));
        } else {
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK, GameUtils.scale(2)),
                    BorderFactory.createEmptyBorder(
                        GameUtils.scale(8), GameUtils.scale(8), 
                        GameUtils.scale(8), GameUtils.scale(8))
            ));
        }
    }
    
    /**
     * Gets the player associated with this panel.
     * 
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Updates the UI scale according to current settings
     */
    public void updateUIScale() {
        // Update the font for all components
        nameLabel.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 14));
        moneyLabel.setFont(GameUtils.getScaledFont("Arial", Font.PLAIN, 13));
        
        // Cập nhật font cho tất cả component con
        for (Component comp : getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label != nameLabel && label != moneyLabel) {
                    label.setFont(GameUtils.getScaledFont("Arial", Font.PLAIN, 12));
                }
            }
        }
        
        // Refresh the panel
        revalidate();
        repaint();
    }
} 