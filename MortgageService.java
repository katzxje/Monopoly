/**
 * Service interface for mortgage-related operations.
 * Used to separate mortgage business logic from UI.
 */
public interface MortgageService {
    /**
     * Checks if a property can be mortgaged.
     *
     * @param property The property to check
     * @return true if the property can be mortgaged
     */
    boolean canMortgage(Mortgageable property);
    
    /**
     * Mortgages a property.
     *
     * @param property The property to mortgage
     * @return The mortgage value received
     */
    int mortgage(Mortgageable property);
    
    /**
     * Checks if a property can be unmortgaged.
     *
     * @param property The property to check
     * @return true if the property can be unmortgaged
     */
    boolean canUnmortgage(Mortgageable property);
    
    /**
     * Unmortgages a property.
     *
     * @param property The property to unmortgage
     * @return The amount paid to unmortgage
     */
    int unmortgage(Mortgageable property);
    
    /**
     * Gets the unmortgage cost (mortgage value + interest).
     *
     * @param property The property to calculate cost for
     * @return The unmortgage cost
     */
    int getUnmortgageCost(Mortgageable property);
} 