/**
 * Defines the types of effects that Chance and Community Chest cards can have.
 */
public enum CardType {
    /**
     * Move to a specific position on the board
     */
    MOVEMENT,
    
    /**
     * Go directly to jail
     */
    GO_TO_JAIL,
    
    /**
     * Get out of jail free card
     */
    GET_OUT_OF_JAIL_FREE,
    
    /**
     * Collect money from the bank
     */
    COLLECT_MONEY,
    
    /**
     * Pay money to the bank
     */
    PAY_MONEY,
    
    /**
     * Pay a specified amount to each player
     */
    PAY_EACH_PLAYER,
    
    /**
     * Collect a specified amount from each player
     */
    COLLECT_FROM_EACH_PLAYER,
    
    /**
     * Pay for repairs based on houses and hotels owned
     */
    REPAIRS,
    
    /**
     * Move to the nearest railroad
     */
    NEAREST_RAILROAD,
    
    /**
     * Move to the nearest utility
     */
    NEAREST_UTILITY,
    
    /**
     * Move backward a specified number of spaces
     */
    MOVE_BACKWARD
} 