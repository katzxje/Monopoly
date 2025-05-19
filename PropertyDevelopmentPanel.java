import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A panel for developing properties by building houses and hotels.
 */
public class PropertyDevelopmentPanel extends JPanel {
    private GameEngine gameEngine;
    private JPanel propertiesPanel;
    private JLabel titleLabel;
    private JLabel descriptionLabel;
    private Map<String, List<PropertySpace>> colorGroups;
    
    /**
     * Creates a new property development panel.
     * 
     * @param gameEngine The game engine
     */
    public PropertyDevelopmentPanel(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        this.colorGroups = new HashMap<>();
        initComponents();
    }
    
    /**
     * Initializes panel components.
     */
    private void initComponents() {
        setLayout(new BorderLayout(GameUtils.scale(10), GameUtils.scale(10)));
        setBorder(BorderFactory.createEmptyBorder(
                GameUtils.scale(10), GameUtils.scale(10), 
                GameUtils.scale(10), GameUtils.scale(10)));
        
        // Title label
        titleLabel = new JLabel("Property Development");
        titleLabel.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Description label
        descriptionLabel = new JLabel("Build houses and hotels on your property sets");
        descriptionLabel.setFont(GameUtils.getScaledFont("Arial", Font.ITALIC, 14));
        descriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(descriptionLabel, BorderLayout.CENTER);
        
        // Properties panel with scrolling
        propertiesPanel = new JPanel();
        propertiesPanel.setLayout(new BoxLayout(propertiesPanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(propertiesPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Add components
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Close button at the bottom
        JButton closeButton = GameUtils.createStyledButton("Close", new Color(230, 230, 230), Color.BLACK);
        closeButton.addActionListener(e -> {
            // Find parent dialog and close it
            Container parent = getParent();
            while (parent != null && !(parent instanceof JDialog)) {
                parent = parent.getParent();
            }
            if (parent != null) {
                ((JDialog) parent).dispose();
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Updates the display for the current player.
     */
    public void updateForCurrentPlayer() {
        Player currentPlayer = gameEngine.getCurrentPlayer();
        propertiesPanel.removeAll();
        
        // Group properties by color
        organizePropertiesByColor(currentPlayer);
        
        // No monopolies message
        if (colorGroups.isEmpty()) {
            JLabel noMonopoliesLabel = new JLabel("You don't have any complete property sets yet");
            noMonopoliesLabel.setFont(GameUtils.getScaledFont("Arial", Font.ITALIC, 14));
            noMonopoliesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            propertiesPanel.add(Box.createVerticalStrut(GameUtils.scale(20)));
            propertiesPanel.add(noMonopoliesLabel);
        } else {
            // Add each color group
            for (String colorGroup : colorGroups.keySet()) {
                addColorGroupPanel(colorGroup, colorGroups.get(colorGroup), currentPlayer);
            }
        }
        
        propertiesPanel.revalidate();
        propertiesPanel.repaint();
    }
    
    /**
     * Organizes the player's properties by color group, keeping only monopolies.
     * 
     * @param player The player
     */
    private void organizePropertiesByColor(Player player) {
        colorGroups.clear();
        
        // Group player's properties by color
        Map<String, List<PropertySpace>> playerProperties = new HashMap<>();
        
        for (Buyable property : player.getProperties()) {
            if (property instanceof PropertySpace) {
                PropertySpace propertySpace = (PropertySpace) property;
                String colorGroup = propertySpace.getColorGroup();
                
                if (!playerProperties.containsKey(colorGroup)) {
                    playerProperties.put(colorGroup, new ArrayList<>());
                }
                
                playerProperties.get(colorGroup).add(propertySpace);
            }
        }
        
        // Check which color groups are monopolies
        for (String colorGroup : playerProperties.keySet()) {
            if (gameEngine.hasMonopoly(player, colorGroup)) {
                colorGroups.put(colorGroup, playerProperties.get(colorGroup));
            }
        }
    }
    
    /**
     * Adds a panel for a color group of properties.
     * 
     * @param colorGroup The color group name
     * @param properties The properties in this color group
     * @param player The current player
     */
    private void addColorGroupPanel(String colorGroup, List<PropertySpace> properties, Player player) {
        // Create panel for this color group
        JPanel groupPanel = new JPanel();
        groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
        groupPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(getColorForGroup(colorGroup), GameUtils.scale(2)),
                        colorGroup + " Properties",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        GameUtils.getScaledFont("Arial", Font.BOLD, 16),
                        getColorForGroup(colorGroup)
                ),
                BorderFactory.createEmptyBorder(
                        GameUtils.scale(5), GameUtils.scale(5), 
                        GameUtils.scale(5), GameUtils.scale(5))
        ));
        
        // Check if player has a monopoly
        boolean hasMonopoly = properties.size() > 0 && properties.get(0).hasMonopoly();
        
        // Add warning if no monopoly
        if (!hasMonopoly) {
            JLabel warningLabel = new JLabel("You must own all properties in this color group to develop them.");
            warningLabel.setFont(GameUtils.getScaledFont("Arial", Font.ITALIC, 14));
            warningLabel.setForeground(Color.RED);
            groupPanel.add(warningLabel);
            groupPanel.add(Box.createVerticalStrut(GameUtils.scale(5)));
        }
        
        // Add each property in the color group
        for (PropertySpace property : properties) {
            JPanel propertyPanel = new JPanel(new BorderLayout(GameUtils.scale(10), 0));
            propertyPanel.setBorder(BorderFactory.createEmptyBorder(
                    GameUtils.scale(5), GameUtils.scale(5), 
                    GameUtils.scale(5), GameUtils.scale(5)));
            
            // Property name and development info
            JPanel infoPanel = new JPanel(new GridLayout(3, 1));
            JLabel nameLabel = new JLabel(property.getName());
            nameLabel.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 14));
            
            String developmentInfo = property.hasHotel() ? "Hotel" : 
                    property.getHouses() + " Houses";
            JLabel developmentLabel = new JLabel("Development: " + developmentInfo);
            developmentLabel.setFont(GameUtils.getScaledFont("Arial", Font.PLAIN, 14));
            
            // Add rent information
            JLabel rentLabel = new JLabel("Current Rent: $" + property.calculateRent());
            rentLabel.setFont(GameUtils.getScaledFont("Arial", Font.PLAIN, 14));
            
            infoPanel.add(nameLabel);
            infoPanel.add(developmentLabel);
            infoPanel.add(rentLabel);
            
            // Build buttons panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            
            // House button
            JButton buildHouseButton = new JButton("Build House");
            buildHouseButton.setFont(GameUtils.getScaledFont("Arial", Font.PLAIN, 12));
            buildHouseButton.setEnabled(hasMonopoly && property.canBuildHouse() && player.getMoney() >= property.getHouseCost());
            buildHouseButton.setToolTipText("Cost: $" + property.getHouseCost());
            
            // If house cannot be built, show appropriate tooltip
            if (!hasMonopoly) {
                buildHouseButton.setToolTipText("You need to own all properties in this color group");
            } else if (property.isMortgaged()) {
                buildHouseButton.setToolTipText("Property is mortgaged");
            } else if (property.getHouses() >= 4) {
                buildHouseButton.setToolTipText("Property already has maximum houses");
            } else if (property.hasHotel()) {
                buildHouseButton.setToolTipText("Property already has a hotel");
            } else if (player.getMoney() < property.getHouseCost()) {
                buildHouseButton.setToolTipText("Not enough money (need $" + property.getHouseCost() + ")");
            } else {
                // Check if other properties in the group have uneven houses
                boolean unevenDevelopment = false;
                for (PropertySpace prop : properties) {
                    if (prop != property && prop.getHouses() < property.getHouses()) {
                        unevenDevelopment = true;
                        break;
                    }
                }
                
                if (unevenDevelopment) {
                    buildHouseButton.setToolTipText("You must build houses evenly across all properties in this group");
                    buildHouseButton.setEnabled(false);
                }
            }
            
            buildHouseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean success = gameEngine.buildHouse(player, property);
                    if (success) {
                        JOptionPane.showMessageDialog(PropertyDevelopmentPanel.this, 
                                "House built on " + property.getName() + "! New rent: $" + property.calculateRent(), 
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        updateForCurrentPlayer();
                    } else {
                        JOptionPane.showMessageDialog(PropertyDevelopmentPanel.this, 
                                "Cannot build house on " + property.getName(), 
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            
            // Hotel button
            JButton buildHotelButton = new JButton("Build Hotel");
            buildHotelButton.setFont(GameUtils.getScaledFont("Arial", Font.PLAIN, 12));
            buildHotelButton.setEnabled(hasMonopoly && property.canBuildHotel() && player.getMoney() >= property.getHouseCost());
            buildHotelButton.setToolTipText("Cost: $" + property.getHouseCost());
            
            // If hotel cannot be built, show appropriate tooltip
            if (!hasMonopoly) {
                buildHotelButton.setToolTipText("You need to own all properties in this color group");
            } else if (property.isMortgaged()) {
                buildHotelButton.setToolTipText("Property is mortgaged");
            } else if (property.getHouses() < 4) {
                buildHotelButton.setToolTipText("Property needs 4 houses before building a hotel");
            } else if (property.hasHotel()) {
                buildHotelButton.setToolTipText("Property already has a hotel");
            } else if (player.getMoney() < property.getHouseCost()) {
                buildHotelButton.setToolTipText("Not enough money (need $" + property.getHouseCost() + ")");
            }
            
            buildHotelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean success = gameEngine.buildHotel(player, property);
                    if (success) {
                        JOptionPane.showMessageDialog(PropertyDevelopmentPanel.this, 
                                "Hotel built on " + property.getName() + "! New rent: $" + property.calculateRent(), 
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        updateForCurrentPlayer();
                    } else {
                        JOptionPane.showMessageDialog(PropertyDevelopmentPanel.this, 
                                "Cannot build hotel on " + property.getName(), 
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            
            buttonPanel.add(buildHouseButton);
            buttonPanel.add(buildHotelButton);
            
            // Add to property panel
            propertyPanel.add(infoPanel, BorderLayout.CENTER);
            propertyPanel.add(buttonPanel, BorderLayout.EAST);
            
            // Add to group panel
            groupPanel.add(propertyPanel);
            groupPanel.add(Box.createVerticalStrut(GameUtils.scale(5)));
        }
        
        // Add to main panel
        propertiesPanel.add(groupPanel);
        propertiesPanel.add(Box.createVerticalStrut(GameUtils.scale(15)));
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
    
    /**
     * Shows the property development dialog.
     * 
     * @param parent The parent frame
     * @param gameEngine The game engine
     */
    public static void showDialog(JFrame parent, GameEngine gameEngine) {
        JDialog dialog = new JDialog(parent, "Property Development", true);
        PropertyDevelopmentPanel panel = new PropertyDevelopmentPanel(gameEngine);
        panel.updateForCurrentPlayer();
        
        dialog.setContentPane(panel);
        dialog.setSize(GameUtils.scale(600), GameUtils.scale(500));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
} 