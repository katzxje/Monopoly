/**
 * Represents a Chance or Community Chest card in Monopoly.
 */
public class Card {
    private String description;
    private CardType type;
    private int value;
    private int extraValue;
    
    /**
     * Creates a new card with a specified description and type.
     * 
     * @param description Text description of the card
     * @param type The type of card effect
     */
    public Card(String description, CardType type) {
        this.description = description;
        this.type = type;
        this.value = 0;
        this.extraValue = 0;
    }
    
    /**
     * Creates a new card with a description, type, and value.
     * 
     * @param description Text description of the card
     * @param type The type of card effect
     * @param value The primary value for the card effect (e.g., money amount, position)
     */
    public Card(String description, CardType type, int value) {
        this.description = description;
        this.type = type;
        this.value = value;
        this.extraValue = 0;
    }
    
    /**
     * Creates a new card with a description, type, primary value, and secondary value.
     * 
     * @param description Text description of the card
     * @param type The type of card effect
     * @param value The primary value for the card effect
     * @param extraValue A secondary value (e.g., hotel repair cost)
     */
    public Card(String description, CardType type, int value, int extraValue) {
        this.description = description;
        this.type = type;
        this.value = value;
        this.extraValue = extraValue;
    }
    
    /**
     * Gets the card description.
     * 
     * @return The text description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the card type.
     * 
     * @return The card effect type
     */
    public CardType getType() {
        return type;
    }
    
    /**
     * Gets the primary value associated with this card.
     * 
     * @return The card's primary value
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Gets the secondary value associated with this card.
     * 
     * @return The card's secondary value
     */
    public int getExtraValue() {
        return extraValue;
    }
} 