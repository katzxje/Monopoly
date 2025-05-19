/**
 * Represents a Railroad space on the Monopoly board.
 */
public class RailroadSpace extends Space implements Mortgageable {
    private int price;
    private Player owner;
    // Base rent for owning 1 railroad
    private static final int BASE_RENT = 25; 
    private boolean mortgaged;

    /**
     * Initializes a new Railroad space.
     *
     * @param name Name of the railroad
     * @param price Purchase price
     */
    public RailroadSpace(String name, int price) {
        super(name, SpaceType.RAILROAD);
        this.price = price;
        this.owner = null;
        this.mortgaged = false;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public int getPrice() {
        return price;
    }

    /**
     * Calculates the rent for landing on this railroad.
     * NOTE: This implementation is basic. The actual rent depends on the *total number*
     * of railroads owned by the owner. This calculation should ideally happen 
     * within the GameEngine where owner information is readily available.
     *
     * @return The calculated rent amount (based on standard Monopoly rules).
     */
    @Override
    public int calculateRent() {
        if (owner == null || mortgaged) {
            return 0; // No rent if unowned or mortgaged
        }
        
        // TODO: Refactor rent calculation to GameEngine or pass owner's railroad count.
        // For now, just return base rent. The actual logic is more complex:
        // Rent is $25, $50, $100, $200 for 1, 2, 3, or 4 railroads owned respectively.
        // We need to count how many RailroadSpace objects the owner has in their properties list.
        
        // Count railroads owned by the owner
        int railroadCount = 0;
        if (owner != null) {
             for (Buyable p : owner.getProperties()) {
                 if (p instanceof RailroadSpace) {
                     railroadCount++;
                 }
             }
        }
        
        switch (railroadCount) {
            case 1: return BASE_RENT;      // 1 * 25
            case 2: return BASE_RENT * 2;  // 2 * 25 = 50
            case 3: return BASE_RENT * 4;  // 4 * 25 = 100
            case 4: return BASE_RENT * 8;  // 8 * 25 = 200
            default: return 0; // Should not happen if owner exists and owns this
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
        this.mortgaged = false;
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
}