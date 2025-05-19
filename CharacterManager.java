import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 * Manages the available characters for the Monopoly game.
 */
public class CharacterManager {
    private List<Character> availableCharacters;
    private Map<String, Character> selectedCharacters; // Map player name to selected character
    
    /**
     * Creates a new character manager with default characters.
     */
    public CharacterManager() {
        availableCharacters = new ArrayList<>();
        selectedCharacters = new HashMap<>();
        
        // Initialize with default characters
        for (String characterName : Character.DEFAULT_CHARACTERS) {
            availableCharacters.add(new Character(characterName));
        }
    }
    
    /**
     * Gets the list of available characters.
     * 
     * @return List of available characters
     */
    public List<Character> getAvailableCharacters() {
        return availableCharacters;
    }
    
    /**
     * Gets a character by name.
     * 
     * @param name The character name
     * @return The character, or null if not found
     */
    public Character getCharacterByName(String name) {
        for (Character character : availableCharacters) {
            if (character.getName().equalsIgnoreCase(name)) {
                return character;
            }
        }
        return null;
    }
    
    /**
     * Selects a character for a player.
     * 
     * @param playerName The player's name
     * @param character The selected character
     */
    public void selectCharacter(String playerName, Character character) {
        selectedCharacters.put(playerName, character);
    }
    
    /**
     * Gets the character selected by a player.
     * 
     * @param playerName The player's name
     * @return The selected character, or null if none selected
     */
    public Character getSelectedCharacter(String playerName) {
        return selectedCharacters.get(playerName);
    }
    
    /**
     * Checks if a character is already selected by another player.
     * 
     * @param character The character to check
     * @return true if the character is already selected
     */
    public boolean isCharacterSelected(Character character) {
        return selectedCharacters.containsValue(character);
    }
} 