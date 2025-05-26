/**
 * Interface for properties that can be mortgaged.
 */
public interface Mortgageable extends Buyable {
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