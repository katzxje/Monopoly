/**
 * Represents a standard property space on the Monopoly board.
 */
public class PropertySpace extends Space implements Mortgageable {
    private int price;
    private int baseRent;
    private int houseCost;
    private String colorGroup;
    private Player owner;
    private int houses;
    private boolean hasHotel;
    private int[] rentLevels; // Array of rent amounts for different development levels
    private boolean mortgaged;
    
    /**
     * Initializes a property space.
     *
     * @param name Name of the property
     * @param price Purchase price
     * @param baseRent Base rent amount (rent with no houses/hotels)
     * @param houseCost Cost to build one house
     * @param colorGroup The color group this property belongs to
     */
    public PropertySpace(String name, int price, int baseRent, int houseCost, String colorGroup) {
        super(name, SpaceType.PROPERTY);
        this.price = price;
        this.baseRent = baseRent;
        this.houseCost = houseCost;
        this.colorGroup = colorGroup;
        this.owner = null;
        this.houses = 0;
        this.hasHotel = false;
        this.mortgaged = false;
        
        // Initialize rent levels based on property value
        initializeRentLevels();
    }
    
    /**
     * Initialize rent levels for different development states
     * (no houses, 1-4 houses, hotel)
     */
    private void initializeRentLevels() {
        rentLevels = new int[6]; // 0 houses, 1-4 houses, hotel
        rentLevels[0] = price; // Base rent is 100% of property price
        
        // Calculate progressive rent levels based on property price
        rentLevels[1] = price * 2;      // 1 house - 200% of property price
        rentLevels[2] = price * 3;      // 2 houses - 300% of property price
        rentLevels[3] = price * 4;      // 3 houses - 400% of property price
        rentLevels[4] = price * 5;      // 4 houses - 500% of property price
        rentLevels[5] = price * 6;      // hotel - 600% of property price
    }
    
    @Override
    public int getPrice() {
        return price;
    }
    
    @Override
    public int calculateRent() {
        if (owner == null || mortgaged) {
            return 0; // No rent if unowned or mortgaged
        }
        
        // Rent increases with houses and hotels
        if (hasHotel) {
            return rentLevels[5]; // Hotel rent
        } else if (houses > 0) {
            return rentLevels[houses]; // House-based rent
        } else {
            // Base rent is now 100% of property price (doubled if owner has monopoly)
            int baseRentAmount = price; // 100% of property price instead of baseRent
            return hasMonopoly() ? baseRentAmount * 2 : baseRentAmount;
        }
    }
    
    /**
     * Checks if the owner has a monopoly (owns all properties in this color group)
     *
     * @return true if owner has a monopoly on this color group
     */
    public boolean hasMonopoly() {
        if (owner == null) return false;
        
        // Count properties in this color group owned by the player
        int ownedInGroup = 0;
        int totalInGroup = 0;
        
        for (Buyable property : owner.getProperties()) {
            if (property instanceof PropertySpace) {
                PropertySpace prop = (PropertySpace) property;
                if (prop.getColorGroup().equals(colorGroup)) {
                    totalInGroup++;
                    if (prop.getOwner() == owner) {
                        ownedInGroup++;
                    }
                }
            }
        }
        
        // Check if player owns all properties in the group
        // This requires checking the total properties in the group on the board
        return ownedInGroup > 0 && ownedInGroup == totalInGroup;
    }
    
    @Override
    public Player getOwner() {
        return owner;
    }
    
    @Override
    public void setOwner(Player owner) {
        this.owner = owner;
    }
    
    @Override
    public void resetOwner() {
        this.owner = null;
        this.houses = 0;
        this.hasHotel = false;
        this.mortgaged = false;
    }
    
    /**
     * Adds a house to the property.
     *
     * @return true if a house was successfully added
     */
    public boolean addHouse() {
        if (houses < 4 && owner != null && hasMonopoly()) {
            houses++;
            return true;
        }
        return false;
    }
    
    /**
     * Upgrades from 4 houses to a hotel.
     *
     * @return true if the upgrade to a hotel was successful
     */
    public boolean upgradeToHotel() {
        if (houses == 4 && !hasHotel && owner != null) {
            houses = 0;
            hasHotel = true;
            return true;
        }
        return false;
    }
    
    /**
     * Gets the number of houses on the property.
     *
     * @return The number of houses
     */
    public int getHouses() {
        return houses;
    }
    
    /**
     * Checks if the property has a hotel.
     *
     * @return true if a hotel is present
     */
    public boolean hasHotel() {
        return hasHotel;
    }
    
    /**
     * Gets the cost to build a house on this property.
     *
     * @return The cost per house
     */
    public int getHouseCost() {
        return houseCost;
    }
    
    /**
     * Gets the color group of the property.
     *
     * @return The color group name
     */
    public String getColorGroup() {
        return colorGroup;
    }
    
    /**
     * Gets the current rent amount for this property.
     *
     * @return The current rent amount
     */
    public int getCurrentRent() {
        return calculateRent();
    }
    
    /**
     * Gets the rent amount at a specific development level.
     *
     * @param level Development level (0=base, 1-4=houses, 5=hotel)
     * @return The rent amount at that level
     */
    public int getRentForLevel(int level) {
        if (level >= 0 && level < rentLevels.length) {
            return rentLevels[level];
        }
        return baseRent;
    }
    
    /**
     * Checks if houses can be built evenly across a property group.
     * According to Monopoly rules, houses must be built evenly.
     *
     * @return true if a house can be built on this property
     */
    public boolean canBuildHouse() {
        if (owner == null || !hasMonopoly() || houses >= 4 || hasHotel || mortgaged) {
            return false;
        }
        
        // Check if building would violate the "build evenly" rule
        for (Buyable property : owner.getProperties()) {
            if (property instanceof PropertySpace) {
                PropertySpace prop = (PropertySpace) property;
                if (prop.getColorGroup().equals(colorGroup) && prop != this) {
                    // If any property in the group has fewer houses, build there first
                    if (prop.getHouses() < houses) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Checks if a hotel can be built on this property.
     *
     * @return true if a hotel can be built
     */
    public boolean canBuildHotel() {
        if (owner == null || !hasMonopoly() || houses < 4 || hasHotel) {
            return false;
        }
        
        // Check if all properties in the group have 4 houses
        for (Buyable property : owner.getProperties()) {
            if (property instanceof PropertySpace) {
                PropertySpace prop = (PropertySpace) property;
                if (prop.getColorGroup().equals(colorGroup)) {
                    if (prop.getHouses() < 4 || prop.hasHotel()) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    @Override
    public boolean isMortgaged() {
        return mortgaged;
    }
    
    @Override
    public int mortgage() {
        if (!mortgaged) {
            mortgaged = true;
            return getMortgageValue();
        }
        return 0;
    }
    
    @Override
    public int unmortgage() {
        if (mortgaged) {
            mortgaged = false;
            return 1; // Success
        }
        return 0;
    }
    
    @Override
    public int getMortgageValue() {
        return price / 2; // Mortgage value is half the purchase price
    }
    
    /**
     * Checks if this property can be mortgaged.
     * 
     * @return true if the property can be mortgaged
     */
    public boolean canMortgage() {
        // Can only mortgage if owned, not already mortgaged, and has no buildings
        return owner != null && !mortgaged && houses == 0 && !hasHotel;
    }
}