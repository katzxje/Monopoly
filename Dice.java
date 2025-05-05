import java.util.Random;

/**
 * The Dice class represents a pair of dice for the Monopoly game.
 */
public class Dice {
    private Random random;
    private int value1;
    private int value2;
    private int consecutiveDoubles;
    
    /**
     * Initializes a new pair of dice.
     */
    public Dice() {
        random = new Random();
        value1 = 1;
        value2 = 1;
        consecutiveDoubles = 0;
    }
    
    /**
     * Rolls the dice.
     *
     * @return int The total value of the two dice
     */
    public int roll() {
        value1 = random.nextInt(6) + 1;
        value2 = random.nextInt(6) + 1;
        
        if (isDouble()) {
            consecutiveDoubles++;
        } else {
            consecutiveDoubles = 0;
        }
        
        return value1 + value2;
    }
    
    /**
     * Checks if both dice have the same value (doubles).
     *
     * @return boolean True if doubles were rolled, false otherwise
     */
    public boolean isDouble() {
        return value1 == value2;
    }
    
    /**
     * Checks if the player has rolled three consecutive doubles.
     *
     * @return boolean True if three consecutive doubles were rolled
     */
    public boolean isThreeConsecutiveDoubles() {
        return consecutiveDoubles >= 3;
    }
    
    /**
     * Resets the consecutive doubles counter.
     */
    public void resetConsecutiveDoubles() {
        consecutiveDoubles = 0;
    }
    
    /**
     * Gets the value of the first die.
     *
     * @return int Value of the first die
     */
    public int getValue1() {
        return value1;
    }
    
    /**
     * Gets the value of the second die.
     *
     * @return int Value of the second die
     */
    public int getValue2() {
        return value2;
    }
    
    /**
     * Gets the total value of both dice.
     *
     * @return int The sum of the two dice values
     */
    public int getTotal() {
        return value1 + value2;
    }
    
    /**
     * Gets the current count of consecutive doubles rolled.
     *
     * @return int The number of consecutive doubles
     */
    public int getConsecutiveDoubles() {
        return consecutiveDoubles;
    }
    
    @Override
    public String toString() {
        return "Dice roll: " + value1 + " and " + value2 + " (Total: " + getTotal() + ")";
    }
}