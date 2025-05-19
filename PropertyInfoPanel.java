import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

/**
 * A panel that displays information about properties, including details
 * for the currently selected property and properties owned by the current player.
 */
public class PropertyInfoPanel extends JPanel {
    private JLabel nameLabel;
    private JLabel priceLabel;
    private JLabel rentLabel;
    private JLabel ownerLabel;
    private JLabel colorLabel;
    private JLabel mortgageStatusLabel;
    private JButton mortgageButton;
    private JButton unmortgageButton;
    private JPanel infoPanel;
    private JPanel propertyListPanel;
    private JPanel actionPanel;
    private Font infoLabelFont;
    private Buyable selectedProperty;
    private GameEngine gameEngine;
    private MortgageService mortgageService;
    
    /**
     * Creates a new property information panel.
     */
    public PropertyInfoPanel() {
        // Initialize scaling and font
        infoLabelFont = GameUtils.getScaledFont("Arial", Font.PLAIN, 14); // Larger text
        
        setLayout(new BorderLayout(GameUtils.scale(5), GameUtils.scale(5)));
        
        // Property info display (top section)
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.GRAY, GameUtils.scale(1)),
                        "Property Information",
                        TitledBorder.CENTER,
                        TitledBorder.TOP,
                        GameUtils.getScaledFont("Arial", Font.BOLD, 14),
                        Color.DARK_GRAY
                ),
                BorderFactory.createEmptyBorder(
                    GameUtils.scale(5), GameUtils.scale(10), 
                    GameUtils.scale(5), GameUtils.scale(10))
        ));
        
        // Initialize property info labels
        nameLabel = createInfoLabel("Property: ");
        priceLabel = createInfoLabel("Price: ");
        rentLabel = createInfoLabel("Rent: ");
        ownerLabel = createInfoLabel("Owner: ");
        colorLabel = createInfoLabel("Color: ");
        mortgageStatusLabel = createInfoLabel("Mortgage Status: Not Mortgaged");
        
        // Add labels to info panel
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, GameUtils.scale(5)))); // Increase spacing
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, GameUtils.scale(5))));
        infoPanel.add(rentLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, GameUtils.scale(5))));
        infoPanel.add(ownerLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, GameUtils.scale(5))));
        infoPanel.add(colorLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, GameUtils.scale(5))));
        infoPanel.add(mortgageStatusLabel);
        
        // Create action panel for mortgage/unmortgage buttons
        actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        // Initialize mortgage buttons
        mortgageButton = new JButton("Mortgage Property");
        mortgageButton.setEnabled(false); // Initially disabled
        mortgageButton.addActionListener(e -> mortgageSelectedProperty());
        
        unmortgageButton = new JButton("Unmortgage Property");
        unmortgageButton.setEnabled(false); // Initially disabled
        unmortgageButton.addActionListener(e -> unmortgageSelectedProperty());
        
        actionPanel.add(mortgageButton);
        actionPanel.add(unmortgageButton);
        infoPanel.add(Box.createRigidArea(new Dimension(0, GameUtils.scale(5))));
        infoPanel.add(actionPanel);
        
        // Panel for player's property list (bottom section)
        propertyListPanel = new JPanel();
        propertyListPanel.setLayout(new BoxLayout(propertyListPanel, BoxLayout.Y_AXIS));
        propertyListPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.GRAY, GameUtils.scale(1)),
                        "Your Properties.",
                        TitledBorder.CENTER,
                        TitledBorder.TOP,
                        GameUtils.getScaledFont("Arial", Font.BOLD, 14),
                        Color.DARK_GRAY
                ),
                BorderFactory.createEmptyBorder(
                    GameUtils.scale(5), GameUtils.scale(10), 
                    GameUtils.scale(5), GameUtils.scale(10))
        ));
        
        // Default "no properties" message
        JLabel noPropertiesLabel = new JLabel("No properties owned");
        noPropertiesLabel.setFont(infoLabelFont);
        noPropertiesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        propertyListPanel.add(noPropertiesLabel);
        
        // Add panels to main panel
        add(infoPanel, BorderLayout.NORTH);
        add(propertyListPanel, BorderLayout.CENTER);
        
        // Set panel background
        setBackground(new Color(245, 245, 240));
    }
    
    /**
     * Sets the game engine for this panel.
     * 
     * @param gameEngine The game engine
     */
    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }
    
    /**
     * Sets the mortgage service for this panel.
     * 
     * @param mortgageService The mortgage service
     */
    public void setMortgageService(MortgageService mortgageService) {
        this.mortgageService = mortgageService;
    }
    
    /**
     * Mortgages the currently selected property.
     */
    private void mortgageSelectedProperty() {
        if (selectedProperty instanceof Mortgageable && mortgageService != null) {
            Mortgageable property = (Mortgageable) selectedProperty;
            
            if (mortgageService.canMortgage(property)) {
                int mortgageValue = mortgageService.mortgage(property);
                
                if (mortgageValue > 0) {
                    mortgageStatusLabel.setText("Mortgage Status: Mortgaged");
                    mortgageStatusLabel.setForeground(Color.RED);
                    
                    mortgageButton.setEnabled(false);
                    unmortgageButton.setEnabled(true);
                    
                    // Update property list to reflect the new mortgage status
                    if (property.getOwner() != null) {
                        updateForPlayer(property.getOwner());
                    }
                    
                    // Show confirmation message
                    JOptionPane.showMessageDialog(this, 
                        "You have mortgaged " + property.getName() + " for $" + mortgageValue + ".",
                        "Property Mortgaged", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                // Check if it's because of buildings
                if (property instanceof PropertySpace) {
                    PropertySpace prop = (PropertySpace) property;
                    if (prop.getHouses() > 0 || prop.hasHotel()) {
                        JOptionPane.showMessageDialog(this, 
                            "You must sell all buildings on this property before mortgaging it.",
                            "Cannot Mortgage", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }
    }
    
    /**
     * Unmortgages the currently selected property.
     */
    private void unmortgageSelectedProperty() {
        if (selectedProperty instanceof Mortgageable && mortgageService != null) {
            Mortgageable property = (Mortgageable) selectedProperty;
            
            if (mortgageService.canUnmortgage(property)) {
                int unmortgageCost = mortgageService.getUnmortgageCost(property);
                
                int result = JOptionPane.showConfirmDialog(this, 
                    "Unmortgaging " + property.getName() + " will cost $" + unmortgageCost + ". Proceed?",
                    "Confirm Unmortgage", JOptionPane.YES_NO_OPTION);
                
                if (result == JOptionPane.YES_OPTION) {
                    int paidAmount = mortgageService.unmortgage(property);
                    
                    if (paidAmount > 0) {
                        mortgageStatusLabel.setText("Mortgage Status: Not Mortgaged");
                        mortgageStatusLabel.setForeground(Color.BLACK);
                        
                        mortgageButton.setEnabled(true);
                        unmortgageButton.setEnabled(false);
                        
                        // Update property list to reflect the new mortgage status
                        if (property.getOwner() != null) {
                            updateForPlayer(property.getOwner());
                        }
                        
                        // Show confirmation message
                        JOptionPane.showMessageDialog(this, 
                            "You have unmortgaged " + property.getName() + " for $" + paidAmount + ".",
                            "Property Unmortgaged", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } else {
                // Check if it's because of insufficient funds
                if (property.getOwner() != null) {
                    int unmortgageCost = mortgageService.getUnmortgageCost(property);
                    if (property.getOwner().getMoney() < unmortgageCost) {
                        JOptionPane.showMessageDialog(this, 
                            "You don't have enough money to unmortgage this property. You need $" + unmortgageCost + ".",
                            "Insufficient Funds", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }
    }
    
    /**
     * Creates a label for displaying property information.
     * 
     * @param text The label's text
     * @return The created label
     */
    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(infoLabelFont);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    /**
     * Displays information about a property.
     * 
     * @param property The property to display
     */
    public void displayProperty(Buyable property) {
        selectedProperty = property;
        
        if (property != null) {
            nameLabel.setText("Property: " + property.getName());
            priceLabel.setText("Price: $" + property.getPrice());
            rentLabel.setText("Rent: $" + property.calculateRent());
            
            if (property.getOwner() != null) {
                ownerLabel.setText("Owner: " + property.getOwner().getName());
                
                // Update mortgage status
                updateMortgageStatusAndButtons(property);
            } else {
                ownerLabel.setText("Owner: None (Available)");
                mortgageStatusLabel.setText("Mortgage Status: N/A");
                mortgageStatusLabel.setForeground(Color.BLACK);
                mortgageButton.setEnabled(false);
                unmortgageButton.setEnabled(false);
            }
            
            // Set color info if it's a PropertySpace
            if (property instanceof PropertySpace) {
                PropertySpace propertySpace = (PropertySpace) property;
                colorLabel.setText("Color: " + propertySpace.getColorGroup());
                
                // Highlight the color group with an actual color
                Color color = getColorForGroup(propertySpace.getColorGroup());
                colorLabel.setForeground(color);
            } else if (property instanceof RailroadSpace) {
                colorLabel.setText("Type: Railroad");
                colorLabel.setForeground(Color.BLACK);
            } else if (property instanceof UtilitySpace) {
                colorLabel.setText("Type: Utility");
                colorLabel.setForeground(Color.BLACK);
            }
        } else {
            // Clear the display if no property
            nameLabel.setText("Property: -");
            priceLabel.setText("Price: -");
            rentLabel.setText("Rent: -");
            ownerLabel.setText("Owner: -");
            colorLabel.setText("Color: -");
            colorLabel.setForeground(Color.BLACK);
            mortgageStatusLabel.setText("Mortgage Status: -");
            mortgageStatusLabel.setForeground(Color.BLACK);
            mortgageButton.setEnabled(false);
            unmortgageButton.setEnabled(false);
        }
    }
    
    /**
     * Updates the mortgage status display and mortgage/unmortgage buttons.
     * 
     * @param property The property to check
     */
    private void updateMortgageStatusAndButtons(Buyable property) {
        if (property instanceof Mortgageable && mortgageService != null) {
            Mortgageable mortgageableProperty = (Mortgageable) property;
            
            // Update mortgage status
            if (mortgageableProperty.isMortgaged()) {
                mortgageStatusLabel.setText("Mortgage Status: Mortgaged");
                mortgageStatusLabel.setForeground(Color.RED);
            } else {
                mortgageStatusLabel.setText("Mortgage Status: Not Mortgaged");
                mortgageStatusLabel.setForeground(Color.BLACK);
            }
            
            // Update mortgage buttons if current player owns this property
            Player currentPlayer = getGameCurrentPlayer();
            if (currentPlayer != null && property.getOwner() == currentPlayer) {
                mortgageButton.setEnabled(mortgageService.canMortgage(mortgageableProperty));
                unmortgageButton.setEnabled(mortgageService.canUnmortgage(mortgageableProperty));
            } else {
                mortgageButton.setEnabled(false);
                unmortgageButton.setEnabled(false);
            }
        } else {
            mortgageStatusLabel.setText("Mortgage Status: N/A");
            mortgageStatusLabel.setForeground(Color.BLACK);
            mortgageButton.setEnabled(false);
            unmortgageButton.setEnabled(false);
        }
    }
    
    /**
     * Gets the game engine for this panel.
     * 
     * @return The game engine
     */
    public GameEngine getGameEngine() {
        return gameEngine;
    }
    
    /**
     * Gets the current player from the game engine.
     * 
     * @return The current player
     */
    private Player getGameCurrentPlayer() {
        // Use reflection to work around dependencies
        if (getGameEngine() != null) {
            try {
                return getGameEngine().getCurrentPlayer();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Updates the property list to show properties owned by the current player.
     * 
     * @param player The current player
     */
    public void updateForPlayer(Player player) {
        // Clear the property list panel
        propertyListPanel.removeAll();
        
        // Get player's properties
        List<Buyable> properties = player.getProperties();
        
        if (properties.isEmpty()) {
            // Show "no properties" message
            JLabel noPropertiesLabel = new JLabel("No properties owned");
            noPropertiesLabel.setFont(infoLabelFont);
            noPropertiesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            propertyListPanel.add(noPropertiesLabel);
        } else {
            // Add each property to the list
            for (Buyable property : properties) {
                JPanel propertyPanel = createPropertyListItem(property);
                propertyListPanel.add(propertyPanel);
                propertyListPanel.add(Box.createRigidArea(new Dimension(0, GameUtils.scale(5))));
            }
        }
        
        // Update the panel
        propertyListPanel.revalidate();
        propertyListPanel.repaint();
    }
    
    /**
     * Creates a panel for displaying a property in the property list.
     * 
     * @param property The property to display
     * @return A panel representing the property
     */
    private JPanel createPropertyListItem(Buyable property) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, GameUtils.scale(1)));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, GameUtils.scale(36))); // Increased height
        
        // Property name label
        JLabel nameLabel = new JLabel(property.getName());
        nameLabel.setFont(infoLabelFont);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(0, GameUtils.scale(5), 0, 0));
        
        // If property is mortgaged, show it in the name
        if (property instanceof Mortgageable && ((Mortgageable)property).isMortgaged()) {
            nameLabel.setText(property.getName() + " (Mortgaged)");
            nameLabel.setForeground(Color.RED);
        }
        
        // Price/rent label
        JLabel infoLabel = new JLabel("$" + property.getPrice() + " / $" + property.calculateRent());
        infoLabel.setFont(infoLabelFont);
        infoLabel.setPreferredSize(new Dimension(GameUtils.scale(100), GameUtils.scale(30)));
        infoLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, GameUtils.scale(10)));
        
        // Color indicator for property group
        JPanel colorIndicator = new JPanel();
        colorIndicator.setPreferredSize(new Dimension(GameUtils.scale(15), GameUtils.scale(36)));
        
        // Set color based on property type
        if (property instanceof PropertySpace) {
            PropertySpace propertySpace = (PropertySpace) property;
            colorIndicator.setBackground(getColorForGroup(propertySpace.getColorGroup()));
        } else if (property instanceof RailroadSpace) {
            colorIndicator.setBackground(Color.GRAY);
        } else if (property instanceof UtilitySpace) {
            colorIndicator.setBackground(Color.LIGHT_GRAY);
        }
        
        // If property is mortgaged, add visual indication
        if (property instanceof Mortgageable && ((Mortgageable)property).isMortgaged()) {
            colorIndicator.setBackground(Color.LIGHT_GRAY);
        }
        
        // Add components to panel
        panel.add(colorIndicator, BorderLayout.WEST);
        panel.add(nameLabel, BorderLayout.CENTER);
        panel.add(infoLabel, BorderLayout.EAST);
        
        // Add interactive hover effect
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                panel.setBorder(BorderFactory.createLineBorder(Color.BLUE, GameUtils.scale(1)));
                displayProperty(property);
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, GameUtils.scale(1)));
            }
        });
        
        return panel;
    }
    
    /**
     * Gets a color for a property color group.
     * 
     * @param colorGroup The color group name
     * @return A color for the group
     */
    private Color getColorForGroup(String colorGroup) {
        switch (colorGroup.toLowerCase()) {
            case "brown": return new Color(139, 69, 19);
            case "light blue": return new Color(173, 216, 230);
            case "pink": return new Color(255, 105, 180);
            case "orange": return new Color(255, 165, 0);
            case "red": return new Color(255, 0, 0);
            case "yellow": return new Color(255, 255, 0);
            case "green": return new Color(0, 128, 0);
            case "blue": return new Color(0, 0, 255);
            default: return Color.GRAY;
        }
    }
} 