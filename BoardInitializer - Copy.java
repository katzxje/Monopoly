import java.util.ArrayList;
import java.util.List;

/**
 * Initializes the game board with all its spaces.
 */
public class BoardInitializer {

    /**
     * Creates and returns a new Monopoly game board.
     * 
     * @return A list of Space objects representing the game board.
     */
    public static List<Space> createBoard() {
        List<Space> board = new ArrayList<>();
        
        // Initialize 40 spaces on the board
        board.add(new Space("GO", SpaceType.GO));
        
        // Brown properties
        board.add(new PropertySpace("Mediterranean Avenue", 120, 2, 50, "Brown"));
        board.add(new Space("Community Chest", SpaceType.COMMUNITY_CHEST));
        board.add(new PropertySpace("Baltic Avenue", 120, 4, 50, "Brown"));
        board.add(new Space("Income Tax", SpaceType.TAX, 200));
        board.add(new RailroadSpace("Reading Railroad", 400));
        
        // Light Blue properties
        board.add(new PropertySpace("Oriental Avenue", 200, 6, 50, "Light Blue"));
        board.add(new Space("Chance", SpaceType.CHANCE));
        board.add(new PropertySpace("Vermont Avenue", 200, 6, 50, "Light Blue"));
        board.add(new PropertySpace("Connecticut Avenue", 240, 8, 50, "Light Blue"));
        
        // Jail / Just Visiting
        board.add(new Space("Jail / Just Visiting", SpaceType.JAIL));
        
        // Pink properties
        board.add(new PropertySpace("St. Charles Place", 280, 10, 100, "Pink"));
        board.add(new UtilitySpace("Electric Company", 300));
        board.add(new PropertySpace("States Avenue", 280, 10, 100, "Pink"));
        board.add(new PropertySpace("Virginia Avenue", 320, 12, 100, "Pink"));
        board.add(new RailroadSpace("Pennsylvania Railroad", 400));
        
        // Orange properties
        board.add(new PropertySpace("St. James Place", 360, 14, 100, "Orange"));
        board.add(new Space("Community Chest", SpaceType.COMMUNITY_CHEST));
        board.add(new PropertySpace("Tennessee Avenue", 360, 14, 100, "Orange"));
        board.add(new PropertySpace("New York Avenue", 400, 16, 100, "Orange"));
        
        // Free Parking
        board.add(new Space("Free Parking", SpaceType.FREE_PARKING));
        
        // Red properties
        board.add(new PropertySpace("Kentucky Avenue", 440, 18, 150, "Red"));
        board.add(new Space("Chance", SpaceType.CHANCE));
        board.add(new PropertySpace("Indiana Avenue", 440, 18, 150, "Red"));
        board.add(new PropertySpace("Illinois Avenue", 480, 20, 150, "Red"));
        board.add(new RailroadSpace("B & O Railroad", 400));
        
        // Yellow properties
        board.add(new PropertySpace("Atlantic Avenue", 520, 22, 150, "Yellow"));
        board.add(new PropertySpace("Ventnor Avenue", 520, 22, 150, "Yellow"));
        board.add(new UtilitySpace("Water Works", 300));
        board.add(new PropertySpace("Marvin Gardens", 560, 24, 150, "Yellow"));
        
        // Go To Jail
        board.add(new Space("Go To Jail", SpaceType.GO_TO_JAIL));
        
        // Green properties
        board.add(new PropertySpace("Pacific Avenue", 600, 26, 200, "Green"));
        board.add(new PropertySpace("North Carolina Avenue", 600, 26, 200, "Green"));
        board.add(new Space("Community Chest", SpaceType.COMMUNITY_CHEST));
        board.add(new PropertySpace("Pennsylvania Avenue", 640, 28, 200, "Green"));
        board.add(new RailroadSpace("Short Line Railroad", 400));
        board.add(new Space("Chance", SpaceType.CHANCE));
        
        // Blue properties
        board.add(new PropertySpace("Park Place", 700, 35, 200, "Blue"));
        board.add(new Space("Luxury Tax", SpaceType.TAX, 100));
        board.add(new PropertySpace("Boardwalk", 800, 50, 200, "Blue"));
        
        return board;
    }
} 