import java.util.ArrayList;
import java.util.List;

/**
 * The Player class represents a player in the Monopoly game.
 */
public class Player {
    private String name;
    private int money;
    private int position;
    private boolean inJail;
    private int jailTurns;
    private List<Buyable> properties;
    private boolean bankrupt;
    private int getOutOfJailCards;
    private Character character;
    private boolean surrendered; // Track if player surrendered voluntarily

    /**
     * Initializes a new player with default values.
     *
     * @param name The player's name
     */
    public Player(String name) {
        this.name = name;
        this.money = 3000; // Increased starting money (doubled from standard 1500)
        this.position = 0; // Starting position ("GO" space)
        this.inJail = false;
        this.jailTurns = 0;
        this.properties = new ArrayList<>(); // Use interface type for flexibility
        this.bankrupt = false;
        this.getOutOfJailCards = 0;
        this.character = null;
        this.surrendered = false;
    }

    /**
     * Initializes a new player with default values and a character.
     *
     * @param name The player's name
     * @param character The player's character/token
     */
    public Player(String name, Character character) {
        this(name);
        this.character = character;
    }

    /**
     * Moves the player on the board.
     *
     * @param steps Number of steps to move
     * @param boardSize Size of the board
     * @return boolean Returns true if the player passed GO
     */
    public boolean move(int steps, int boardSize) {
        int oldPosition = position;
        position = (position + steps) % boardSize;
        return position < oldPosition; // Returns true if passed GO (position wrapped around)
    }

    /**
     * Adds money to the player.
     *
     * @param amount Amount of money to add
     */
    public void addMoney(int amount) {
        this.money += amount;
    }

    /**
     * Subtracts money from the player without bankruptcy check.
     * Use this only when you've already verified the player has enough money.
     *
     * @param amount Amount of money to subtract
     */
    public void subtractMoney(int amount) {
        this.money -= amount;
    }

    /**
     * Deducts money from the player and checks for bankruptcy.
     *
     * @param amount Amount of money to deduct
     * @return boolean Returns false if the player cannot afford it (and might go bankrupt)
     */
    public boolean pay(int amount) {
        if (money >= amount) {
            money -= amount;
            return true;
        } else {
            // Check if net worth is insufficient to cover the amount
            // A more complex game might allow selling assets here before declaring bankruptcy.
            if (getNetWorth() < amount) {
                bankrupt = true; // Declare bankruptcy if assets + cash aren't enough
                return false;
            }
            // Player might be able to sell assets to pay
            // For now, return false indicating immediate payment failed.
            return false;
        }
    }

    /**
     * Calculates the player's total net worth (cash + value of properties).
     * Note: Property value here uses getPrice(), which might represent mortgage value
     * or purchase price. A more detailed model could include house/hotel values.
     *
     * @return int Total net worth
     */
    public int getNetWorth() {
        int propertyValue = 0;
        for (Buyable property : properties) {
            // Using getPrice() as a placeholder for property value.
            // This could be purchase price, mortgage value, or include improvements.
            propertyValue += property.getPrice();
        }
        return money + propertyValue;
    }

    /**
     * Sends the player to jail.
     */
    public void goToJail(int jailPosition) {
        this.position = jailPosition;
        this.inJail = true;
        this.jailTurns = 0;
    }

    /**
     * Attempts to use a Get Out of Jail Free card.
     *
     * @return boolean Returns true if a card was successfully used
     */
    public boolean useGetOutOfJailCard() {
        if (getOutOfJailCards > 0) {
            getOutOfJailCards--;
            setFreeFromJail(); // Use the common method to reset jail status
            return true;
        }
        return false;
    }

    /**
     * Increments the number of turns spent in jail and checks if the maximum (3) is reached.
     *
     * @return boolean Returns true if the player has spent 3 turns in jail
     */
    public boolean increaseJailTurn() {
        jailTurns++;
        if (jailTurns >= 3) {
            // Don't automatically free player here, GameEngine handles payment/release
            return true; // Signal that 3 turns are up
        }
        return false;
    }

    /**
     * Adds a property to the player's list of owned properties.
     *
     * @param property The Buyable property to add
     */
    public void addProperty(Buyable property) {
        properties.add(property);
    }

    /**
     * Removes a property from the player's list of owned properties.
     *
     * @param property The Buyable property to remove
     * @return boolean Returns true if the property was successfully removed
     */
    public boolean removeProperty(Buyable property) {
        return properties.remove(property);
    }

    /**
     * Checks if the player owns a property with a specific name.
     *
     * @param propertyName The name of the property to check for
     * @return Buyable The property if owned, null otherwise
     */
    public Buyable getProperty(String propertyName) {
        for (Buyable property : properties) {
            // Assumes Buyable interface has getName()
            if (property.getName().equals(propertyName)) {
                return property;
            }
        }
        return null;
    }

    /**
     * Gives the player a Get Out of Jail Free card.
     */
    public void addGetOutOfJailCard() {
        getOutOfJailCards++;
    }

    /**
     * Frees the player from jail (used after paying, rolling doubles, or using a card).
     */
    public void setFreeFromJail() {
        inJail = false;
        jailTurns = 0;
    }

    /**
     * Clears all properties from the player's list (used during bankruptcy).
     */
    public void clearProperties() {
        this.properties.clear();
    }

    /**
     * Checks if the player has a Get Out of Jail Free card.
     *
     * @return true if the player has at least one Get Out of Jail Free card
     */
    public boolean hasGetOutOfJailCard() {
        return getOutOfJailCards > 0;
    }

    /**
     * Sets whether the player is in jail.
     *
     * @param inJail true if the player is in jail, false otherwise
     */
    public void setInJail(boolean inJail) {
        this.inJail = inJail;
    }

    /**
     * Resets the jail turns counter to zero.
     */
    public void resetJailTurns() {
        this.jailTurns = 0;
    }

    /**
     * Increments the jail turns counter.
     */
    public void incrementJailTurns() {
        this.jailTurns++;
    }

    /**
     * Marks the player as having surrendered voluntarily.
     */
    public void surrender() {
        this.surrendered = true;
        this.bankrupt = true; // Player is also considered bankrupt for game mechanics
    }
    
    /**
     * Checks if the player surrendered voluntarily.
     * 
     * @return true if the player surrendered, false otherwise
     */
    public boolean hasSurrendered() {
        return surrendered;
    }
    
    public void setSurrendered(boolean surrendered) {
        this.surrendered = surrendered;
    }

    // Getters và Setters
    public String getName() {
        return name;
    }

    public int getMoney() {
        return money;
    }

    public int getPosition() {
        return position;
    }
    
    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isInJail() {
        return inJail;
    }

    public boolean isBankrupt() {
        return bankrupt;
    }
    
    public void setBankrupt(boolean bankrupt) {
        this.bankrupt = bankrupt;
    }

    public List<Buyable> getProperties() {
        return properties;
    }
    
    public int getJailTurns() {
        return jailTurns;
    }
    
    public int getGetOutOfJailCards() {
        return getOutOfJailCards;
    }
    
    public Character getCharacter() {
        return character;
    }
    
    public void setCharacter(Character character) {
        this.character = character;
    }

    @Override
    public String toString() {
        return name + " ($" + money + ", Position: " + position + ")";
    }
}