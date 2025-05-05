/**
 * Represents a Utility space (Water Works, Electric Company) on the Monopoly board.
 */
public class UtilitySpace extends Space implements Buyable {
    private int price;
    private Player owner;

    /**
     * Initializes a new Utility space.
     *
     * @param name Name of the utility
     * @param price Purchase price
     */
    public UtilitySpace(String name, int price) {
        super(name, SpaceType.UTILITY);
        this.price = price;
        this.owner = null;
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
     * Calculates the rent multiplier for this utility.
     * The actual rent is this multiplier times the dice roll.
     * The multiplication by the dice roll should happen in the GameEngine
     * when processing the rent payment.
     *
     * @return The rent multiplier (4 if owner owns 1 utility, 10 if owner owns 2).
     */
    @Override
    public int calculateRent() {
        if (owner == null) {
            return 0; // No rent if unowned
        }

        // Count utilities owned by the owner
        int utilityCount = 0;
        for (Buyable p : owner.getProperties()) {
            if (p instanceof UtilitySpace) {
                utilityCount++;
            }
        }

        if (utilityCount == 1) {
            return 4; // Multiplier for owning 1 utility
        } else if (utilityCount >= 2) { // Should be 2 in standard Monopoly
            return 10; // Multiplier for owning 2 utilities
        } else {
            return 0; // Should not happen if owner exists and owns this
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
        // Reset mortgage status if applicable
    }
}