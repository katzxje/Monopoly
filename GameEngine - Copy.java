import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The GameEngine class manages the game flow, turns, and rules of Monopoly.
 */
public class GameEngine implements MortgageService {
    private static final int BOARD_SIZE = 40;
    private static final int GO_SALARY = 200;
    private static final int JAIL_POSITION = 10;
    @SuppressWarnings("unused") // Board initialization uses this index implicitly
    private static final int GO_TO_JAIL_POSITION = 30;
    private static final int JAIL_FEE = 50;
    private static final int MAX_JAIL_TURNS = 3;
    
    private final List<Player> players;
    private final Dice dice;
    private int currentPlayerIndex;
    private final List<Space> board;
    private boolean gameOver;
    private final List<Card> chanceCards;
    private final List<Card> communityChestCards;
    private final Random random;
    private final MortgageService mortgageService;
    
    /**
     * Initializes a new game with a list of players and pre-initialized game components.
     *
     * @param playerNames List of player names.
     * @param dice The Dice instance to use for the game.
     * @param mortgageService The MortgageService instance to use for the game.
     * @param board The initialized game board.
     * @param chanceCards The initialized deck of Chance cards.
     * @param communityChestCards The initialized deck of Community Chest cards.
     */
    public GameEngine(List<String> playerNames, 
                        Dice dice, 
                        MortgageService mortgageService,
                        List<Space> board,
                        List<Card> chanceCards,
                        List<Card> communityChestCards) {
        // Initialize players
        this.players = new ArrayList<>();
        for (String name : playerNames) {
            this.players.add(new Player(name));
        }
        
        this.dice = dice;
        this.mortgageService = mortgageService;
        this.board = board;
        this.chanceCards = chanceCards;
        this.communityChestCards = communityChestCards;
        
        this.currentPlayerIndex = 0;
        this.random = new Random();
        this.gameOver = false;
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
        
        // Check if player has a Get Out of Jail Free card
        if (player.hasGetOutOfJailCard()) {
            result.append(player.getName() + " uses a Get Out of Jail Free card.\n");
            player.useGetOutOfJailCard();
            player.setInJail(false);
            player.resetJailTurns();
            return result.toString();
        }
        
        // Option 1: Pay the fine
        if (player.getMoney() >= JAIL_FEE) {
            // For simplicity, we'll have the AI automatically pay after 2 turns
            // In a real game, this would be a player choice
            if (player.getJailTurns() >= 2) {
                result.append(player.getName() + " pays the $" + JAIL_FEE + " fine to get out of jail.\n");
                player.pay(JAIL_FEE);
                player.setInJail(false);
                player.resetJailTurns();
                return result.toString();
            }
        }
        
        // Option 2: Try to roll doubles
        dice.roll();
        result.append(player.getName() + " rolls " + dice.toString() + " to try to get out of jail.\n");
        
        if (dice.isDouble()) {
            result.append(player.getName() + " rolled doubles and gets out of jail!\n");
            player.setInJail(false);
            player.resetJailTurns();
            
            // Move the player according to the roll
            int steps = dice.getTotal();
            boolean passedGo = player.move(steps, BOARD_SIZE);
            if (passedGo) {
                player.addMoney(GO_SALARY);
                result.append(player.getName() + " passed GO and collected $" + GO_SALARY + ".\n");
            }
            
            // Handle the space the player landed on
            Space currentSpace = board.get(player.getPosition());
            result.append(player.getName() + " landed on " + currentSpace.getName() + ".\n");
            handleLandedOnSpace(player, currentSpace, result);
        } else {
            // Failed to roll doubles
            player.incrementJailTurns();
            
            // If this is the third turn in jail, player must pay and get out
            if (player.getJailTurns() >= MAX_JAIL_TURNS) {
                result.append("This is " + player.getName() + "'s third turn in jail. ");
                
                if (player.getMoney() >= JAIL_FEE) {
                    result.append("Must pay the $" + JAIL_FEE + " fine to get out of jail.\n");
                    player.pay(JAIL_FEE);
                    player.setInJail(false);
                    player.resetJailTurns();
                } else {
                    result.append("Cannot afford the $" + JAIL_FEE + " fine! Must sell properties or declare bankruptcy.\n");
                    // In a real game, player would have options to mortgage properties
                    // For simplicity, we're assuming bankruptcy if they can't pay
                    player.setBankrupt(true);
                }
            }
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
                handleCardDraw(player, true, result);
                break;
            case COMMUNITY_CHEST:
                handleCardDraw(player, false, result);
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
     * Handles drawing a card from Chance or Community Chest.
     * 
     * @param player The player drawing the card
     * @param isChance True if drawing from Chance, false for Community Chest
     * @param result StringBuilder to update with results
     */
    private void handleCardDraw(Player player, boolean isChance, StringBuilder result) {
        List<Card> deck = isChance ? chanceCards : communityChestCards;
        String deckName = isChance ? "Chance" : "Community Chest";
        
        // Draw the top card
        Card card = deck.remove(0);
        result.append(player.getName() + " draws " + deckName + " card: " + card.getDescription() + "\n");
        
        // Execute card effect
        switch (card.getType()) {
            case MOVEMENT:
                int destination = card.getValue();
                int currentPosition = player.getPosition();
                
                // If moving to a position before current position, player passes GO
                if (destination < currentPosition) {
                    player.addMoney(GO_SALARY);
                    result.append(player.getName() + " passes GO and collects $" + GO_SALARY + ".\n");
                }
                
                // Move player to destination
                player.setPosition(destination);
                Space destinationSpace = board.get(destination);
                result.append(player.getName() + " moves to " + destinationSpace.getName() + ".\n");
                
                // Handle effects of the destination space
                handleLandedOnSpace(player, destinationSpace, result);
                break;
                
            case GO_TO_JAIL:
                result.append(player.getName() + " goes to jail!\n");
                player.goToJail(JAIL_POSITION);
                break;
                
            case COLLECT_MONEY:
                int amount = card.getValue();
                player.addMoney(amount);
                result.append(player.getName() + " collects $" + amount + ".\n");
                break;
                
            case PAY_MONEY:
                int fee = card.getValue();
                boolean canPay = player.pay(fee);
                if (canPay) {
                    result.append(player.getName() + " pays $" + fee + ".\n");
                } else {
                    result.append(player.getName() + " cannot afford to pay $" + fee + "!\n");
                    player.setBankrupt(true);
                }
                break;
                
            case GET_OUT_OF_JAIL_FREE:
                player.addGetOutOfJailCard();
                result.append(player.getName() + " keeps this card for future use.\n");
                break;
                
            case REPAIRS:
                int houseRepairCost = card.getValue();
                int hotelRepairCost = card.getExtraValue();
                int totalRepairCost = 0;
                
                // Calculate repair costs based on properties owned
                for (Buyable property : player.getProperties()) {
                    if (property instanceof PropertySpace) {
                        PropertySpace propertySpace = (PropertySpace) property;
                        
                        if (propertySpace.hasHotel()) {
                            totalRepairCost += hotelRepairCost;
                        } else {
                            totalRepairCost += houseRepairCost * propertySpace.getHouses();
                        }
                    }
                }
                
                if (totalRepairCost > 0) {
                    result.append(player.getName() + " must pay $" + totalRepairCost + " for repairs.\n");
                    boolean canPayRepairs = player.pay(totalRepairCost);
                    if (!canPayRepairs) {
                        result.append(player.getName() + " cannot afford repairs!\n");
                        player.setBankrupt(true);
                    }
                } else {
                    result.append(player.getName() + " has no properties requiring repair.\n");
                }
                break;
                
            case PAY_EACH_PLAYER:
                int payAmount = card.getValue();
                int totalPaid = 0;
                
                for (Player otherPlayer : players) {
                    if (otherPlayer != player && !otherPlayer.isBankrupt()) {
                        otherPlayer.addMoney(payAmount);
                        totalPaid += payAmount;
                        result.append(player.getName() + " pays $" + payAmount + " to " + otherPlayer.getName() + ".\n");
                    }
                }
                
                boolean canPayAll = player.pay(totalPaid);
                if (!canPayAll) {
                    result.append(player.getName() + " cannot afford to pay all players!\n");
                    player.setBankrupt(true);
                }
                break;
                
            case COLLECT_FROM_EACH_PLAYER:
                int collectAmount = card.getValue();
                int totalCollected = 0;
                
                for (Player otherPlayer : players) {
                    if (otherPlayer != player && !otherPlayer.isBankrupt()) {
                        boolean paid = otherPlayer.pay(collectAmount);
                        if (paid) {
                            totalCollected += collectAmount;
                            result.append(otherPlayer.getName() + " pays $" + collectAmount + " to " + player.getName() + ".\n");
                        } else {
                            result.append(otherPlayer.getName() + " cannot afford to pay $" + collectAmount + "!\n");
                            otherPlayer.setBankrupt(true);
                        }
                    }
                }
                
                player.addMoney(totalCollected);
                result.append(player.getName() + " collects a total of $" + totalCollected + ".\n");
                break;
                
            case NEAREST_RAILROAD:
                moveToNearestProperty(player, SpaceType.RAILROAD, result);
                break;
                
            case NEAREST_UTILITY:
                moveToNearestProperty(player, SpaceType.UTILITY, result);
                break;
                
            case MOVE_BACKWARD:
                int spacesToMove = card.getValue();
                int newPosition = (player.getPosition() - spacesToMove + BOARD_SIZE) % BOARD_SIZE;
                player.setPosition(newPosition);
                
                Space newSpace = board.get(newPosition);
                result.append(player.getName() + " moves back " + spacesToMove + " spaces to " + newSpace.getName() + ".\n");
                
                // Handle the effects of the new space
                handleLandedOnSpace(player, newSpace, result);
                break;
        }
        
        // Put the card at the bottom of the deck (unless it's Get Out of Jail Free)
        if (card.getType() != CardType.GET_OUT_OF_JAIL_FREE) {
            deck.add(card);
        }
    }
    
    /**
     * Moves a player to the nearest property of a specified type.
     * 
     * @param player The player to move
     * @param type The type of property to find (RAILROAD or UTILITY)
     * @param result StringBuilder to update with results
     */
    private void moveToNearestProperty(Player player, SpaceType type, StringBuilder result) {
        int currentPosition = player.getPosition();
        int nearestPosition = -1;
        int distance = BOARD_SIZE; // Maximum possible distance
        
        // Find the nearest property of the specified type
        for (int i = 0; i < board.size(); i++) {
            if (board.get(i).getType() == type) {
                int dist = (i - currentPosition + BOARD_SIZE) % BOARD_SIZE;
                if (dist > 0 && dist < distance) {
                    distance = dist;
                    nearestPosition = i;
                }
            }
        }
        
        if (nearestPosition != -1) {
            // Check if passing GO
            if (nearestPosition < currentPosition) {
                player.addMoney(GO_SALARY);
                result.append(player.getName() + " passes GO and collects $" + GO_SALARY + ".\n");
            }
            
            // Move to the nearest property
            player.setPosition(nearestPosition);
            Space destination = board.get(nearestPosition);
            result.append(player.getName() + " moves to " + destination.getName() + ".\n");
            
            // Handle landing on the property with double rent if it's owned
            if (destination instanceof Buyable) {
                Buyable property = (Buyable) destination;
                if (property.getOwner() != null && property.getOwner() != player) {
                    // Apply double rent for landing via card
                    int regularRent = property.calculateRent();
                    int doubleRent = regularRent * 2;
                    
                    result.append(property.getName() + " is owned by " + property.getOwner().getName() + ".\n");
                    result.append("Card effect: Rent is doubled to $" + doubleRent + ".\n");
                    
                    boolean canPay = player.pay(doubleRent);
                    if (canPay) {
                        property.getOwner().addMoney(doubleRent);
                        result.append(player.getName() + " pays $" + doubleRent + " to " + property.getOwner().getName() + ".\n");
                    } else {
                        result.append(player.getName() + " cannot afford the double rent!\n");
                        player.setBankrupt(true);
                    }
                } else {
                    // If unowned or owned by the player, handle normally
                    handlePropertySpace(player, property, result);
                }
            }
        } else {
            result.append("Error: No property of specified type found on the board.\n");
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
     * Allows a player to build a house on a property.
     * 
     * @param player The player building
     * @param property The property to build on
     * @return true if the house was successfully built
     */
    public boolean buildHouse(Player player, PropertySpace property) {
        // Check if the player is the owner and can build
        if (property.getOwner() != player || !property.canBuildHouse()) {
            return false;
        }
        
        // Check if player can afford it
        int houseCost = property.getHouseCost();
        if (player.getMoney() < houseCost) {
            return false;
        }
        
        // Build the house
        boolean built = property.addHouse();
        if (built) {
            player.pay(houseCost);
            return true;
        }
        
        return false;
    }
    
    /**
     * Allows a player to build a hotel on a property.
     * 
     * @param player The player building
     * @param property The property to build on
     * @return true if the hotel was successfully built
     */
    public boolean buildHotel(Player player, PropertySpace property) {
        // Check if the player is the owner and can build a hotel
        if (property.getOwner() != player || !property.canBuildHotel()) {
            return false;
        }
        
        // Check if player can afford it
        int houseCost = property.getHouseCost();
        if (player.getMoney() < houseCost) {
            return false;
        }
        
        // Build the hotel
        boolean built = property.upgradeToHotel();
        if (built) {
            player.pay(houseCost);
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if a player has a monopoly on a color group.
     * 
     * @param player The player to check
     * @param colorGroup The color group to check
     * @return true if the player has a monopoly
     */
    public boolean hasMonopoly(Player player, String colorGroup) {
        int ownedInGroup = 0;
        int totalInGroup = 0;
        
        for (Space space : board) {
            if (space instanceof PropertySpace) {
                PropertySpace property = (PropertySpace) space;
                if (property.getColorGroup().equals(colorGroup)) {
                    totalInGroup++;
                    if (property.getOwner() == player) {
                        ownedInGroup++;
                    }
                }
            }
        }
        
        return totalInGroup > 0 && ownedInGroup == totalInGroup;
    }
    
    /**
     * Handles a player going bankrupt.
     *
     * @param player The player who went bankrupt
     */
    public void handleBankruptcy(Player player) {
        // Make sure the player is marked bankrupt
        player.setBankrupt(true);
        
        // Give all properties back to the bank (or could transfer to creditor)
        for (Buyable property : player.getProperties()) {
            property.setOwner(null);
        }
        
        // Clear the player's properties
        player.clearProperties();
        
        // Check if the game is over
        checkGameOver();
    }
    
    /**
     * Advances to the next player in sequence.
     */
    public void advanceToNextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
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
     * Sets the game as over, for example when a player surrenders.
     * This is different from the automatic game over detection by bankruptcy.
     */
    public void setGameOver() {
        gameOver = true;
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
    
    /**
     * Get the board spaces.
     * 
     * @return The list of spaces on the board
     */
    public List<Space> getBoard() {
        return board;
    }
    
    /**
     * Checks if a property can be mortgaged.
     *
     * @param property The property to check
     * @return true if the property can be mortgaged
     */
    @Override
    public boolean canMortgage(Mortgageable property) {
        return mortgageService.canMortgage(property);
    }
    
    /**
     * Mortgages a property.
     *
     * @param property The property to mortgage
     * @return The mortgage value received
     */
    @Override
    public int mortgage(Mortgageable property) {
        return mortgageService.mortgage(property);
    }
    
    /**
     * Checks if a property can be unmortgaged.
     *
     * @param property The property to check
     * @return true if the property can be unmortgaged
     */
    @Override
    public boolean canUnmortgage(Mortgageable property) {
        return mortgageService.canUnmortgage(property);
    }
    
    /**
     * Unmortgages a property.
     *
     * @param property The property to unmortgage
     * @return The amount paid to unmortgage
     */
    @Override
    public int unmortgage(Mortgageable property) {
        return mortgageService.unmortgage(property);
    }
    
    /**
     * Gets the unmortgage cost (mortgage value + interest).
     *
     * @param property The property to calculate cost for
     * @return The unmortgage cost
     */
    @Override
    public int getUnmortgageCost(Mortgageable property) {
        return mortgageService.getUnmortgageCost(property);
    }
}