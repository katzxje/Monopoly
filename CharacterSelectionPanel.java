import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Panel for selecting a character in the game.
 */
public class CharacterSelectionPanel extends JPanel {
    private JComboBox<Character> characterComboBox;
    private JLabel previewLabel;
    private Character selectedCharacter;
    private Font largeFont = new Font("Arial", Font.BOLD, 20);
    private Font titleFont = new Font("Arial", Font.BOLD, 22);

    /**
     * Creates a new character selection panel.
     * 
     * @param characters List of available characters
     */
    public CharacterSelectionPanel(List<Character> characters) {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(240, 255, 240));
        
        // Create titled border with larger font
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 100, 200), 2),
            "Select Character"
        );
        titledBorder.setTitleFont(titleFont);
        titledBorder.setTitleColor(new Color(0, 100, 200));
        setBorder(BorderFactory.createCompoundBorder(
                titledBorder,
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Create the combo box with characters
        characterComboBox = new JComboBox<>();
        characterComboBox.setFont(largeFont);
        characterComboBox.setPreferredSize(new Dimension(300, 45));
        
        for (Character character : characters) {
            characterComboBox.addItem(character);
        }

        // Preview label for the character image
        previewLabel = new JLabel();
        previewLabel.setPreferredSize(new Dimension(200, 200));
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        previewLabel.setBackground(Color.WHITE);
        previewLabel.setOpaque(true);
        
        // Update preview when selection changes
        characterComboBox.addActionListener(e -> updatePreview());
        
        // Set initial preview
        if (characterComboBox.getItemCount() > 0) {
            selectedCharacter = (Character) characterComboBox.getSelectedItem();
            updatePreview();
        }
        
        // Character Label
        JLabel characterLabel = new JLabel("Character: ");
        characterLabel.setFont(largeFont);
        
        // Layout components
        JPanel selectionPanel = new JPanel(new BorderLayout(15, 0));
        selectionPanel.setBackground(new Color(240, 255, 240));
        selectionPanel.add(characterLabel, BorderLayout.WEST);
        selectionPanel.add(characterComboBox, BorderLayout.CENTER);
        
        // Create a panel for preview with a label
        JPanel previewPanel = new JPanel(new BorderLayout(10, 10));
        previewPanel.setBackground(new Color(240, 255, 240));
        JLabel previewTitleLabel = new JLabel("Preview:", JLabel.CENTER);
        previewTitleLabel.setFont(largeFont);
        previewPanel.add(previewTitleLabel, BorderLayout.NORTH);
        previewPanel.add(previewLabel, BorderLayout.CENTER);
        
        add(selectionPanel, BorderLayout.NORTH);
        add(previewPanel, BorderLayout.CENTER);
    }
    
    /**
     * Updates the preview image based on selected character.
     */
    private void updatePreview() {
        selectedCharacter = (Character) characterComboBox.getSelectedItem();
        if (selectedCharacter != null) {
            // Get the icon and scale it larger
            ImageIcon originalIcon = selectedCharacter.getIcon();
            if (originalIcon != null) {
                Image originalImage = originalIcon.getImage();
                Image scaledImage = originalImage.getScaledInstance(160, 160, Image.SCALE_SMOOTH);
                previewLabel.setIcon(new ImageIcon(scaledImage));
            } else {
                previewLabel.setIcon(null);
            }
        } else {
            previewLabel.setIcon(null);
        }
    }
    
    /**
     * @return The currently selected character
     */
    public Character getSelectedCharacter() {
        return selectedCharacter;
    }
} 