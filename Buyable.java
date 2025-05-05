/**
 * Interface representing buyable spaces on the Monopoly board (Properties, Railroads, Utilities).
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
     * Calculates the rent based on the current state of the property
     * (e.g., ownership, houses/hotels, number of railroads/utilities owned).
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
}