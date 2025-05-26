/**
 * Implementation of the MortgageService interface.
 * Handles the business logic for mortgaging and unmortgaging properties.
 */
public class MortgageServiceImpl implements MortgageService {
    
    /**
     * The interest rate applied when unmortgaging a property (typically 10%).
     */
    private static final double INTEREST_RATE = 0.1;
    
    /**
     * Checks if a property can be mortgaged.
     *
     * @param property The property to check
     * @return true if the property can be mortgaged
     */
    @Override
    public boolean canMortgage(Mortgageable property) {
        // Property must have an owner, not be mortgaged, and have no buildings
        if (property == null || property.getOwner() == null || property.isMortgaged()) {
            return false;
        }
        
        // Check for buildings if it's a PropertySpace
        if (property instanceof PropertySpace) {
            PropertySpace propertySpace = (PropertySpace) property;
            if (propertySpace.getHouses() > 0 || propertySpace.hasHotel()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Mortgages a property.
     *
     * @param property The property to mortgage
     * @return The mortgage value received
     */
    @Override
    public int mortgage(Mortgageable property) {
        if (!canMortgage(property)) {
            return 0;
        }
        
        int mortgageValue = property.getMortgageValue();
        property.getOwner().addMoney(mortgageValue);
        property.mortgage();
        return mortgageValue;
    }
    
    /**
     * Checks if a property can be unmortgaged.
     *
     * @param property The property to check
     * @return true if the property can be unmortgaged
     */
    @Override
    public boolean canUnmortgage(Mortgageable property) {
        if (property == null || property.getOwner() == null || !property.isMortgaged()) {
            return false;
        }
        
        // Check if owner has enough money to unmortgage
        int unmortgageCost = getUnmortgageCost(property);
        return property.getOwner().getMoney() >= unmortgageCost;
    }
    
    /**
     * Unmortgages a property.
     *
     * @param property The property to unmortgage
     * @return The amount paid to unmortgage
     */
    @Override
    public int unmortgage(Mortgageable property) {
        if (!canUnmortgage(property)) {
            return 0;
        }
        
        int unmortgageCost = getUnmortgageCost(property);
        property.getOwner().subtractMoney(unmortgageCost);
        property.unmortgage();
        return unmortgageCost;
    }
    
    /**
     * Gets the unmortgage cost (mortgage value + interest).
     *
     * @param property The property to calculate cost for
     * @return The unmortgage cost
     */
    @Override
    public int getUnmortgageCost(Mortgageable property) {
        if (property == null) {
            return 0;
        }
        
        int mortgageValue = property.getMortgageValue();
        return (int)(mortgageValue * (1 + INTEREST_RATE));
    }
} 