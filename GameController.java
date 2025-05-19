import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Controller class that manages game logic and interactions between UI and game engine
 * This separates game logic from UI display (following Single Responsibility Principle)
 */
public class GameController {
    private GameEngine gameEngine;
    private JFrame mainFrame;
    private List<GameStateListener> listeners = new ArrayList<>();
    
    /**
     * Interface for components to listen to game state changes
     */
    public interface GameStateListener {
        void onPlayerChanged(Player currentPlayer);
        void onPlayerMoved(Player player, int from, int to, boolean passedGo);
        void onPropertyPurchased(Player player, Buyable property);
        void onRentPaid(Player fromPlayer, Player toPlayer, int amount);
        void onPlayerBankrupt(Player player);
        void onGameOver(Player winner);
        void onPlayerSurrendered(Player player);
    }
    
    /**
     * Create a new game controller
     *
     * @param gameEngine The game engine
     * @param mainFrame The main UI frame
     */
    public GameController(GameEngine gameEngine, JFrame mainFrame) {
        this.gameEngine = gameEngine;
        this.mainFrame = mainFrame;
    }
    
    /**
     * Add a listener for game state changes
     */
    public void addGameStateListener(GameStateListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Get the current game engine
     */
    public GameEngine getGameEngine() {
        return gameEngine;
    }
    
    /**
     * Advance to the next player's turn
     */
    public void advanceToNextPlayer() {
        gameEngine.advanceToNextPlayer();
        
        // Skip bankrupt players
        while (gameEngine.getCurrentPlayer().isBankrupt() && !gameEngine.isGameOver()) {
            gameEngine.advanceToNextPlayer();
        }
        
        // Notify listeners
        for (GameStateListener listener : listeners) {
            listener.onPlayerChanged(gameEngine.getCurrentPlayer());
        }
    }
    
    /**
     * Move a player by a number of steps
     */
    public void movePlayer(Player player, int steps) {
        int oldPosition = player.getPosition();
        
        // Check the correct signature for the move method and use it
        // It appears the Player.move method requires a board size parameter
        int boardSize = gameEngine.getBoard().size();
        player.move(steps, boardSize);
        
        int newPosition = player.getPosition();
        boolean passedGo = (oldPosition > newPosition) && (steps > 0);
        
        // Handle passed GO
        if (passedGo) {
            player.addMoney(200); // GO Salary
        }
        
        // Notify listeners
        for (GameStateListener listener : listeners) {
            listener.onPlayerMoved(player, oldPosition, newPosition, passedGo);
        }
        
        // Handle the space the player landed on
        handleLandedOnSpace(player, gameEngine.getBoard().get(player.getPosition()));
    }
    
    /**
     * Buy the current property for the current player
     */
    public boolean buyCurrentProperty() {
        Player currentPlayer = gameEngine.getCurrentPlayer();
        Space currentSpace = gameEngine.getBoard().get(currentPlayer.getPosition());
        
        // Only handle if current space is a buyable property
        if (currentSpace instanceof Buyable) {
            Buyable property = (Buyable) currentSpace;
            
            if (property.getOwner() == null) {
                // Check if player can afford it
                if (currentPlayer.getMoney() >= property.getPrice()) {
                    // Buy the property
                    currentPlayer.pay(property.getPrice());
                    property.setOwner(currentPlayer);
                    currentPlayer.addProperty(property);
                    
                    // Notify listeners
                    for (GameStateListener listener : listeners) {
                        listener.onPropertyPurchased(currentPlayer, property);
                    }
                    
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Handle a player landing on a space
     */
    private void handleLandedOnSpace(Player player, Space space) {
        // Handle property spaces
        if (space instanceof Buyable) {
            Buyable property = (Buyable) space;
            
            if (property.getOwner() != null && property.getOwner() != player) {
                // Property is owned by another player - pay rent
                int rentAmount = property.calculateRent();
                
                // Handle utility spaces which need dice roll
                if (property instanceof UtilitySpace) {
                    // In a real implementation, we would use the dice roll
                    // For now we'll just use the default rent
                }
                
                // Pay rent
                if (player.pay(rentAmount)) {
                    property.getOwner().addMoney(rentAmount);
                    
                    // Notify listeners
                    for (GameStateListener listener : listeners) {
                        listener.onRentPaid(player, property.getOwner(), rentAmount);
                    }
                } else {
                    // Not enough money - handle bankruptcy
                    player.setBankrupt(true);
                    handleBankruptcy(player);
                }
            }
        }
        
        // Handle other space types
        else if (space.getType() == SpaceType.GO_TO_JAIL) {
            player.goToJail(10); // Assuming jail is at position 10
            
            // Notify listeners of movement
            for (GameStateListener listener : listeners) {
                listener.onPlayerMoved(player, player.getPosition(), 10, false);
            }
        }
        
        // Check for bankruptcy
        if (player.isBankrupt()) {
            handleBankruptcy(player);
        }
    }
    
    /**
     * Handle player bankruptcy
     */
    private void handleBankruptcy(Player player) {
        gameEngine.handleBankruptcy(player);
        
        // Notify listeners
        for (GameStateListener listener : listeners) {
            listener.onPlayerBankrupt(player);
        }
        
        // Check if game is over
        if (gameEngine.isGameOver()) {
            // Find the winner (non-bankrupt player)
            Player winner = null;
            for (Player p : gameEngine.getPlayers()) {
                if (!p.isBankrupt()) {
                    winner = p;
                    break;
                }
            }
            
            // Notify listeners
            for (GameStateListener listener : listeners) {
                listener.onGameOver(winner);
            }
            
            // Show rankings
            showGameRankings();
        }
    }
    
    /**
     * Handle player surrender
     */
    public void handleSurrender() {
        Player currentPlayer = gameEngine.getCurrentPlayer();
        currentPlayer.surrender();
        
        // Notify listeners
        for (GameStateListener listener : listeners) {
            listener.onPlayerSurrendered(currentPlayer);
        }
        
        // Explicitly set the game as over
        gameEngine.setGameOver();
        
        // Show rankings
        showGameRankings();
    }
    
    /**
     * Show game rankings
     */
    public void showGameRankings() {
        // Calculate player rankings based on net worth
        List<Player> rankedPlayers = new ArrayList<>(gameEngine.getPlayers());
        
        // Sort players by net worth in descending order
        rankedPlayers.sort((p1, p2) -> Integer.compare(p2.getNetWorth(), p1.getNetWorth()));
        
        // Show the ranking dialog
        SwingUtilities.invokeLater(() -> {
            RankingDialog.showDialog(mainFrame, rankedPlayers);
        });
    }
} 