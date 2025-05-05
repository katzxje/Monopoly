import java.util.ArrayList;
import java.util.List;

/**
 * The GameEngine class manages the game flow, turns, and rules of Monopoly.
 */
public class GameEngine {
    private static final int BOARD_SIZE = 40;
    private static final int GO_SALARY = 200;
    private static final int JAIL_POSITION = 10;
    @SuppressWarnings("unused") // Board initialization uses this index implicitly
    private static final int GO_TO_JAIL_POSITION = 30;
    private static final int JAIL_FEE = 50;
    
    private List<Player> players;
    private Dice dice;
    private int currentPlayerIndex;
    private List<Space> board;
    private boolean gameOver;
    
    /**
     * Initializes a new game with a list of players.
     *
     * @param playerNames List of player names
     */
    public GameEngine(List<String> playerNames) {
        // Initialize players
        players = new ArrayList<>();
        for (String name : playerNames) {
            players.add(new Player(name));
        }
        
        // Initialize dice
        dice = new Dice();
        
        // Initialize the first player
        currentPlayerIndex = 0;
        
        // Initialize the board
        initializeBoard();
        
        // Game starts now
        gameOver = false;
    }
    
    /**
     * Initializes all the spaces on the Monopoly board.
     */
    private void initializeBoard() {
        board = new ArrayList<>();
        
        // Initialize 40 spaces on the board
        // Simple example; in a real game, each space would have more properties
        board.add(new Space("GO", SpaceType.GO));
        
        // Brown properties
        board.add(new PropertySpace("Mediterranean Avenue", 60, 2, 50, "Brown"));
        board.add(new Space("Community Chest", SpaceType.COMMUNITY_CHEST));
        board.add(new PropertySpace("Baltic Avenue", 60, 4, 50, "Brown"));
        board.add(new Space("Income Tax", SpaceType.TAX, 200));
        board.add(new RailroadSpace("Reading Railroad", 200));
        
        // Light Blue properties
        board.add(new PropertySpace("Oriental Avenue", 100, 6, 50, "Light Blue"));
        board.add(new Space("Chance", SpaceType.CHANCE));
        board.add(new PropertySpace("Vermont Avenue", 100, 6, 50, "Light Blue"));
        board.add(new PropertySpace("Connecticut Avenue", 120, 8, 50, "Light Blue"));
        
        // Jail / Just Visiting
        board.add(new Space("Jail / Just Visiting", SpaceType.JAIL));
        
        // Pink properties
        board.add(new PropertySpace("St. Charles Place", 140, 10, 100, "Pink"));
        board.add(new UtilitySpace("Electric Company", 150));
        board.add(new PropertySpace("States Avenue", 140, 10, 100, "Pink"));
        board.add(new PropertySpace("Virginia Avenue", 160, 12, 100, "Pink"));
        board.add(new RailroadSpace("Pennsylvania Railroad", 200));
        
        // Orange properties
        board.add(new PropertySpace("St. James Place", 180, 14, 100, "Orange"));
        board.add(new Space("Community Chest", SpaceType.COMMUNITY_CHEST));
        board.add(new PropertySpace("Tennessee Avenue", 180, 14, 100, "Orange"));
        board.add(new PropertySpace("New York Avenue", 200, 16, 100, "Orange"));
        
        // Free Parking
        board.add(new Space("Free Parking", SpaceType.FREE_PARKING));
        
        // Red properties
        board.add(new PropertySpace("Kentucky Avenue", 220, 18, 150, "Red"));
        board.add(new Space("Chance", SpaceType.CHANCE));
        board.add(new PropertySpace("Indiana Avenue", 220, 18, 150, "Red"));
        board.add(new PropertySpace("Illinois Avenue", 240, 20, 150, "Red"));
        board.add(new RailroadSpace("B & O Railroad", 200));
        
        // Yellow properties
        board.add(new PropertySpace("Atlantic Avenue", 260, 22, 150, "Yellow"));
        board.add(new PropertySpace("Ventnor Avenue", 260, 22, 150, "Yellow"));
        board.add(new UtilitySpace("Water Works", 150));
        board.add(new PropertySpace("Marvin Gardens", 280, 24, 150, "Yellow"));
        
        // Go To Jail
        board.add(new Space("Go To Jail", SpaceType.GO_TO_JAIL));
        
        // Green properties
        board.add(new PropertySpace("Pacific Avenue", 300, 26, 200, "Green"));
        board.add(new PropertySpace("North Carolina Avenue", 300, 26, 200, "Green"));
        board.add(new Space("Community Chest", SpaceType.COMMUNITY_CHEST));
        board.add(new PropertySpace("Pennsylvania Avenue", 320, 28, 200, "Green"));
        board.add(new RailroadSpace("Short Line Railroad", 200));
        board.add(new Space("Chance", SpaceType.CHANCE));
        
        // Blue properties
        board.add(new PropertySpace("Park Place", 350, 35, 200, "Blue"));
        board.add(new Space("Luxury Tax", SpaceType.TAX, 100));
        board.add(new PropertySpace("Boardwalk", 400, 50, 200, "Blue"));
    }
    
    /**
     * Executes a complete turn for the current player.
     *
     * @return String Result of the turn
     */
    public String playTurn() {
        if (gameOver) {
            return "Game is over!";
        }
        
        Player currentPlayer = players.get(currentPlayerIndex);
        StringBuilder result = new StringBuilder();
        
        // Check if the player is bankrupt
        if (currentPlayer.isBankrupt()) {
            advanceToNextPlayer();
            return currentPlayer.getName() + " is bankrupt and cannot play.";
        }
        
        result.append(currentPlayer.getName() + "'s turn.\n");
        
        // Handle player in jail
        if (currentPlayer.isInJail()) {
            result.append(handleJailTurn(currentPlayer));
            // If still in jail after handling, end the turn
            if (currentPlayer.isInJail()) {
                advanceToNextPlayer();
                return result.toString();
            }
        }
        
        // Roll the dice
        int steps = dice.roll();
        result.append(dice.toString() + "\n");
        
        // Check for three consecutive doubles
        if (dice.isThreeConsecutiveDoubles()) {
            result.append(currentPlayer.getName() + " rolled 3 consecutive doubles and must go to jail!\n");
            currentPlayer.goToJail(JAIL_POSITION);
            dice.resetConsecutiveDoubles();
            advanceToNextPlayer();
            return result.toString();
        }
        
        // Move the player
        boolean passedGo = currentPlayer.move(steps, BOARD_SIZE);
        if (passedGo) {
            currentPlayer.addMoney(GO_SALARY);
            result.append(currentPlayer.getName() + " passed GO and collected $" + GO_SALARY + ".\n");
        }
        
        // Handle the current space
        Space currentSpace = board.get(currentPlayer.getPosition());
        result.append(currentPlayer.getName() + " landed on " + currentSpace.getName() + ".\n");
        
        handleLandedOnSpace(currentPlayer, currentSpace, result);
        
        // Check if the player went bankrupt after the turn
        if (currentPlayer.isBankrupt()) {
            result.append(currentPlayer.getName() + " went bankrupt!\n");
            handleBankruptcy(currentPlayer);
            checkGameOver();
        }
        
        // If not doubles or went to jail, advance to the next player
        if (!dice.isDouble() || currentPlayer.isInJail()) {
            advanceToNextPlayer();
        } else {
            result.append(currentPlayer.getName() + " rolled doubles and gets another turn!\n");
        }
        
        return result.toString();
    }
    
    /**
     * Handles the turn when a player is in jail.
     *
     * @param player The player in jail
     * @return String Result of handling the jail turn
     */
    private String handleJailTurn(Player player) {
        StringBuilder result = new StringBuilder();
        result.append(player.getName() + " is in jail (Turn " + (player.getJailTurns() + 1) + ").\n");
        
        // Check if the player has a Get Out of Jail Free card
        if (player.getGetOutOfJailCards() > 0) {
            player.useGetOutOfJailCard();
            result.append(player.getName() + " used a Get Out of Jail Free card.\n");
            return result.toString();
        }
        
        // Roll dice to try to get out
        dice.roll();
        result.append(dice.toString() + "\n");
        
        // Get out if doubles are rolled
        if (dice.isDouble()) {
            player.setFreeFromJail();
            result.append(player.getName() + " rolled doubles and got out of jail!\n");
            // Move according to the dice roll
            player.move(dice.getTotal(), BOARD_SIZE);
            return result.toString();
        }
        
        // Increment jail turns and check if it's the 3rd turn
        if (player.increaseJailTurn()) {
            // After 3 turns, must pay to get out
            boolean canPay = player.pay(JAIL_FEE);
            if (canPay) {
                result.append(player.getName() + " has been in jail for 3 turns and paid $" + JAIL_FEE + " to get out.\n");
                player.setFreeFromJail();
            } else {
                // If unable to pay, might need to sell assets or go bankrupt
                result.append(player.getName() + " cannot afford the jail fee and needs to sell assets.\n");
                // In this example, assume the player cannot sell and goes bankrupt
                player.setBankrupt(true);
            }
        } else {
            result.append(player.getName() + " did not get out of jail this turn.\n");
        }
        
        return result.toString();
    }
    
    /**
     * Handles actions when a player lands on a specific board space.
     *
     * @param player The player who moved
     * @param space The space the player landed on
     * @param result StringBuilder to update with results
     */
    private void handleLandedOnSpace(Player player, Space space, StringBuilder result) {
        SpaceType spaceType = space.getType();
        
        switch (spaceType) {
            case PROPERTY:
            case RAILROAD:
            case UTILITY:
                handlePropertySpace(player, (Buyable) space, result);
                break;
            case TAX:
                handleTaxSpace(player, space, result);
                break;
            case GO_TO_JAIL:
                result.append(player.getName() + " must go to jail!\n");
                player.goToJail(JAIL_POSITION);
                break;
            case CHANCE:
                result.append("Landed on Chance (not fully implemented).\n");
                // In a real game, draw a Chance card and perform action
                break;
            case COMMUNITY_CHEST:
                result.append("Landed on Community Chest (not fully implemented).\n");
                // In a real game, draw a Community Chest card and perform action
                break;
            case FREE_PARKING:
                result.append(player.getName() + " landed on Free Parking. Nothing happens.\n");
                break;
            case GO:
                result.append(player.getName() + " landed on GO.\n");
                break;
            case JAIL:
                result.append(player.getName() + " is just visiting jail.\n");
                break;
            default:
                break;
        }
    }
    
    /**
     * Handles landing on a buyable space (Property, Railroad, Utility).
     *
     * @param player The player
     * @param property The buyable space
     * @param result StringBuilder for results
     */
    private void handlePropertySpace(Player player, Buyable property, StringBuilder result) {
        // If unowned, allow purchase
        if (property.getOwner() == null) {
            if (player.getMoney() >= property.getPrice()) {
                // Assume player always buys if they can
                player.pay(property.getPrice());
                property.setOwner(player);
                player.addProperty(property);
                result.append(player.getName() + " bought " + property.getName() + " for $" + property.getPrice() + ".\n");
            } else {
                result.append(player.getName() + " cannot afford to buy " + property.getName() + ".\n");
                // In a real game, an auction could happen here
            }
        } else if (!property.getOwner().equals(player)) {
            // If owned by another player, pay rent
            int rentMultiplier = property.calculateRent(); // Gets base rent, railroad rent, or utility multiplier
            int finalRent;

            if (property instanceof UtilitySpace) {
                // Utility rent = multiplier * dice roll
                int diceRoll = dice.getTotal(); // Get the last dice roll total
                finalRent = rentMultiplier * diceRoll;
                result.append(player.getName() + " must pay rent based on dice roll (" + diceRoll + "). Multiplier: " + rentMultiplier + "x.\n");
            } else {
                // Property or Railroad rent is directly calculated
                finalRent = rentMultiplier;
            }

            result.append(player.getName() + " must pay $" + finalRent + " rent to " + property.getOwner().getName() + ".\n");
            boolean canPay = player.pay(finalRent);
            if (canPay) {
                property.getOwner().addMoney(finalRent);
                result.append(property.getOwner().getName() + " received $" + finalRent + ".\n");
            } else {
                result.append(player.getName() + " cannot afford the rent!\n");
                // In reality, player could sell assets to pay debt
                player.setBankrupt(true);
            }
        } else {
            result.append(player.getName() + " already owns " + property.getName() + ".\n");
        }
    }
    
    /**
     * Handles landing on a tax space.
     *
     * @param player The player
     * @param taxSpace The tax space
     * @param result StringBuilder for results
     */
    private void handleTaxSpace(Player player, Space taxSpace, StringBuilder result) {
        int taxAmount = taxSpace.getValue(); // Assumes Space has getValue() for tax amount
        result.append(player.getName() + " must pay $" + taxAmount + " in taxes.\n");
        boolean canPay = player.pay(taxAmount);
        if (!canPay) {
            result.append(player.getName() + " cannot afford the tax!\n");
            // In reality, player could sell assets to pay tax
            player.setBankrupt(true);
        }
    }
    
    /**
     * Handles a player going bankrupt.
     *
     * @param player The player who went bankrupt
     */
    private void handleBankruptcy(Player player) {
        // Return all assets to the bank (or handle appropriately, e.g., transfer to creditor)
        for (Buyable property : player.getProperties()) {
            property.resetOwner(); // Reset ownership
        }
        player.clearProperties(); // Clear player's list
    }
    
    /**
     * Advances to the next player in sequence.
     */
    private void advanceToNextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        // Skip bankrupt players
        while (players.get(currentPlayerIndex).isBankrupt() && !gameOver) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
    }
    
    /**
     * Checks if the game is over (only one non-bankrupt player remaining).
     */
    private void checkGameOver() {
        int activePlayers = 0;
        int lastActivePlayerIndex = -1;
        
        for (int i = 0; i < players.size(); i++) {
            if (!players.get(i).isBankrupt()) {
                activePlayers++;
                lastActivePlayerIndex = i;
            }
        }
        
        if (activePlayers <= 1) {
            gameOver = true;
            // Print winner message to console (GUI should handle this display)
            if (lastActivePlayerIndex != -1) {
                System.out.println("Game Over! " + players.get(lastActivePlayerIndex).getName() + " is the winner!");
            } else {
                System.out.println("Game Over! No winner (all players bankrupt simultaneously?).");
            }
        }
    }
    
    /**
     * Gets the list of players.
     *
     * @return List of players
     */
    public List<Player> getPlayers() {
        return players;
    }
    
    /**
     * Gets the current player.
     *
     * @return The current player
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
    
    /**
     * Checks if the game is over.
     *
     * @return true if the game is over, false otherwise
     */
    public boolean isGameOver() {
        return gameOver;
    }
}