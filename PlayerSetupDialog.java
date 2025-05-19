import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for setting up players with names and characters.
 */
public class PlayerSetupDialog extends JDialog {
    private List<JTextField> nameFields;
    private List<CharacterSelectionPanel> characterPanels;
    private CharacterManager characterManager;
    private int numPlayers;
    private boolean setupComplete;
    private List<String> playerNames;
    private List<Character> selectedCharacters;
    private Font largeFont = new Font("Arial", Font.BOLD, 20);
    private Font titleFont = new Font("Arial", Font.BOLD, 24);

    /**
     * Creates a new player setup dialog.
     * 
     * @param parent The parent frame
     * @param numPlayers The number of players to set up
     */
    public PlayerSetupDialog(JFrame parent, int numPlayers) {
        super(parent, "Player Setup", true);
        this.numPlayers = numPlayers;
        this.characterManager = new CharacterManager();
        this.setupComplete = false;
        
        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Initializes the dialog components.
     */
    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        
        // Title at the top
        JLabel titleLabel = new JLabel("Set Up Players", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        titleLabel.setForeground(new Color(0, 102, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Main panel for player setups
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(new Color(240, 255, 240)); // Light mint

        // Fields for player setup
        nameFields = new ArrayList<>();
        characterPanels = new ArrayList<>();

        // Create player setup panels
        for (int i = 0; i < numPlayers; i++) {
            JPanel playerPanel = new JPanel(new BorderLayout(15, 15));
            playerPanel.setBackground(new Color(240, 255, 240));
            
            // Create a titled border with larger font
            TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 102, 0), 2),
                "Player " + (i + 1)
            );
            titledBorder.setTitleFont(titleFont);
            titledBorder.setTitleColor(new Color(0, 102, 0));
            playerPanel.setBorder(BorderFactory.createCompoundBorder(
                    titledBorder,
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)));

            // Name field
            JPanel namePanel = new JPanel(new BorderLayout(15, 0));
            namePanel.setBackground(new Color(240, 255, 240));
            
            JLabel nameLabel = new JLabel("Name: ");
            nameLabel.setFont(largeFont);
            namePanel.add(nameLabel, BorderLayout.WEST);
            
            JTextField nameField = new JTextField(15);
            nameField.setFont(largeFont);
            nameField.setPreferredSize(new Dimension(300, 45));
            namePanel.add(nameField, BorderLayout.CENTER);
            nameFields.add(nameField);

            // Character selection
            CharacterSelectionPanel characterPanel = new CharacterSelectionPanel(
                    characterManager.getAvailableCharacters());
            characterPanels.add(characterPanel);

            // Add to player panel
            playerPanel.add(namePanel, BorderLayout.NORTH);
            playerPanel.add(characterPanel, BorderLayout.CENTER);

            // Add to main panel
            mainPanel.add(playerPanel);
            
            // Add some spacing
            if (i < numPlayers - 1) {
                mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
            }
        }

        // Add main panel to a scroll pane in case there are many players
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBackground(new Color(240, 255, 240));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        buttonPanel.setBackground(new Color(240, 255, 240));
        
        JButton okButton = new JButton("Start Game");
        okButton.setFont(new Font("Arial", Font.BOLD, 22));
        okButton.setPreferredSize(new Dimension(250, 60));
        okButton.setBackground(new Color(60, 179, 113)); // Medium sea green
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        
        okButton.addActionListener(e -> validateAndComplete());
        buttonPanel.add(okButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Set dialog size - much larger
        setPreferredSize(new Dimension(800, 800));
    }

    /**
     * Validates the player information and completes setup if valid.
     */
    private void validateAndComplete() {
        playerNames = new ArrayList<>();
        selectedCharacters = new ArrayList<>();
        
        // Validate names
        boolean valid = true;
        for (int i = 0; i < numPlayers; i++) {
            String name = nameFields.get(i).getText().trim();
            
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Player " + (i + 1) + " name cannot be empty.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                valid = false;
                break;
            }
            
            if (playerNames.contains(name)) {
                JOptionPane.showMessageDialog(this,
                        "Player name '" + name + "' is already taken.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                valid = false;
                break;
            }
            
            Character character = characterPanels.get(i).getSelectedCharacter();
            if (character == null) {
                JOptionPane.showMessageDialog(this,
                        "Player " + (i + 1) + " must select a character.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                valid = false;
                break;
            }
            
            // Check for duplicate characters
            for (int j = 0; j < i; j++) {
                if (character.equals(selectedCharacters.get(j))) {
                    JOptionPane.showMessageDialog(this,
                            "Character '" + character.getName() + "' is already selected by another player.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                    valid = false;
                    break;
                }
            }
            
            if (!valid) break;
            
            playerNames.add(name);
            selectedCharacters.add(character);
        }
        
        if (valid) {
            setupComplete = true;
            dispose();
        }
    }
    
    /**
     * @return True if setup was completed, false if canceled
     */
    public boolean isSetupComplete() {
        return setupComplete;
    }
    
    /**
     * @return The list of player names entered
     */
    public List<String> getPlayerNames() {
        return playerNames;
    }
    
    /**
     * @return The list of characters selected
     */
    public List<Character> getSelectedCharacters() {
        return selectedCharacters;
    }
    
    /**
     * Shows the dialog and returns whether setup was completed.
     * 
     * @return True if setup was completed, false if canceled
     */
    public boolean showDialog() {
        setVisible(true);
        return setupComplete;
    }
} 