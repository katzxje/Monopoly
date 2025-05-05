/**
 * Represents a standard property space on the Monopoly board.
 */
public class PropertySpace extends Space implements Buyable {
    private int price;
    private int baseRent;
    private int houseCost;
    private String colorGroup;
    private Player owner;
    private int houses;
    private boolean hasHotel;
    
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
    }
    
    @Override
    public int getPrice() {
        return price;
    }
    
    @Override
    public int calculateRent() {
        if (owner == null) {
            return 0; // No rent if unowned
        }
        
        // Rent increases with houses and hotels
        // TODO: Implement monopoly rent doubling (check if owner owns all properties in color group)
        // TODO: Implement rent based on number of houses/hotel using a more sophisticated structure (e.g., rent array)
        if (hasHotel) {
            // Placeholder rent calculation for hotel
            return baseRent * 25; // Example: Rent with hotel is 25x base rent
        } else if (houses > 0) {
            // Placeholder rent calculation for houses
            return baseRent * (int) Math.pow(5, houses); // Example: Rent increases exponentially
        } else {
            // Base rent (potentially doubled if owner has monopoly)
            // For simplicity, not checking for monopoly here yet.
            return baseRent;
        }
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
        // Mortgaged status should also be reset if applicable
    }
    
    /**
     * Adds a house to the property.
     *
     * @return true if a house was successfully added
     */
    public boolean addHouse() {
        if (houses < 4) {
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
        if (houses == 4 && !hasHotel) {
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
}