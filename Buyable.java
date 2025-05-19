/**
 * Interface representing buyable spaces on the Monopoly board.
 */
public interface Buyable {
    /**
     * Gets the name of the property.
     *
     * @return The name of the property
     */
    String getName();
    
    /**
     * Gets the purchase price of the property.
     *
     * @return The purchase price
     */
    int getPrice();
    
    /**
     * Calculates the rent based on the current state of the property.
     *
     * @return The rent amount
     */
    int calculateRent();
    
    /**
     * Gets the current owner of the property.
     *
     * @return The owner (Player), or null if unowned
     */
    Player getOwner();
    
    /**
     * Sets the new owner for the property.
     *
     * @param owner The new owner (Player)
     */
    void setOwner(Player owner);
    
    /**
     * Resets the property to an unowned state.
     * (e.g., when returned to the bank due to bankruptcy).
     */
    void resetOwner();
    
    /**
     * Checks if the property is currently mortgaged.
     *
     * @return true if the property is mortgaged
     */
    boolean isMortgaged();
    
    /**
     * Mortgages the property, providing the mortgage value to the owner.
     *
     * @return The mortgage value
     */
    int mortgage();
    
    /**
     * Unmortgages the property, requiring the owner to pay the mortgage plus 10% interest.
     *
     * @return The amount paid to unmortgage (mortgage value + 10%)
     */
    int unmortgage();
    
    /**
     * Gets the mortgage value of the property (typically half the purchase price).
     *
     * @return The mortgage value
     */
    int getMortgageValue();
}