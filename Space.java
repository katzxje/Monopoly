/**
 * Base class representing a space on the Monopoly board.
 */
public class Space {
    private String name;
    private SpaceType type;
    private int value;
    
    /**
     * Initializes a space on the board.
     * 
     * @param name The name of the space
     * @param type The type of the space
     */
    public Space(String name, SpaceType type) {
        this.name = name;
        this.type = type;
        this.value = 0;
    }
    
    /**
     * Initializes a space on the board with a value (e.g., for tax spaces).
     * 
     * @param name The name of the space
     * @param type The type of the space
     * @param value The value associated with the space (e.g., tax amount)
     */
    public Space(String name, SpaceType type, int value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }
    
    /**
     * Gets the name of the space.
     * 
     * @return The name of the space
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the type of the space.
     * 
     * @return The type of the space
     */
    public SpaceType getType() {
        return type;
    }
    
    /**
     * Gets the value associated with the space (e.g., tax amount).
     * 
     * @return The value of the space
     */
    public int getValue() {
        return value;
    }
}