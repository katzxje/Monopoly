import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.awt.geom.RoundRectangle2D;
import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class Main extends JFrame implements DicePanel.DiceRollListener {

    private GameEngine gameEngine;
    private JTextArea logArea;
    private JLabel statusLabel; // To show current player info
    private JPanel playersPanel; // Panel to show all players
    private List<PlayerInfoPanel> playerInfoPanels; // List of player info panels
    private GameBoardPanel gameBoardPanel; // Visual game board
    private DicePanel dicePanel; // Animated dice
    private JButton buyPropertyButton; // Button to buy property
    private JLayeredPane layeredPane;
    private NotificationPanel notificationPanel;
    private PropertyInfoPanel propertyInfoPanel;

    public Main() {
        // Initialize scaling
        GameUtils.initializeScaling();
        
        // Set up the main frame
        setTitle("Monopoly Game");
        // Get screen dimensions
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle screenRect = ge.getMaximumWindowBounds();
        // Set up full screen size
        setSize(screenRect.width, screenRect.height);
        setLocation(0, 0);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Set application icon using the monopoly shoe image
        try {
            // Use the helper method to load the PNG image with fallback support
            ImageIcon icon = GameUtils.loadImageWithFallback("images/monopoly-shoe-512.png");
            
            if (icon.getIconWidth() > 0) {
                setIconImage(icon.getImage());
            } else {
                // Fallback to boot.png if PNG image can't be loaded
                setIconImage(GameUtils.loadImageWithFallback("images/boot.png").getImage());
            }
        } catch (Exception e) {
            // Silently fail if icon can't be loaded
            try {
                // Fallback to boot.png
                setIconImage(GameUtils.loadImageWithFallback("images/boot.png").getImage());
            } catch (Exception ex) {
                // Ignore if even the fallback fails
            }
        }
        
        // Thêm menu bar cho game
        createMenuBar();
        
        // Use layered pane for popup notifications
        layeredPane = new JLayeredPane();
        setContentPane(layeredPane);
        layeredPane.setLayout(null); // Use null layout instead of BorderLayout
        
        // Add a base panel to contain the main UI with BorderLayout
        JPanel basePanel = new JPanel(new BorderLayout(0, 0)); // Remove spacing between components
        basePanel.setBounds(0, 0, screenRect.width, screenRect.height);
        layeredPane.add(basePanel, JLayeredPane.DEFAULT_LAYER);
        
        // Show player setup dialog
        int numPlayers = showNumPlayersDialog();
        if (numPlayers < 2) {
            // User canceled, exit application
            System.exit(0);
        }
        
        PlayerSetupDialog setupDialog = new PlayerSetupDialog(this, numPlayers);
        boolean setupComplete = setupDialog.showDialog();
        
        if (!setupComplete) {
            // User canceled, exit application
            System.exit(0);
        }
        
        // Get player names and characters
        List<String> playerNames = setupDialog.getPlayerNames();
        List<Character> selectedCharacters = setupDialog.getSelectedCharacters();
        
        // --- Initialize Dependencies for GameEngine ---
        Dice dice = new Dice();
        MortgageService mortgageService = new MortgageServiceImpl();
        List<Space> board = BoardInitializer.createBoard();
        List<Card> chanceCards = CardDeckManager.createChanceDeck();
        List<Card> communityChestCards = CardDeckManager.createCommunityChestDeck();
        
        // --- Initialize GameEngine ---
        gameEngine = new GameEngine(playerNames, dice, mortgageService, board, chanceCards, communityChestCards);
        
        // Assign characters to players
        List<Player> players = gameEngine.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setCharacter(selectedCharacters.get(i));
        }

        // --- Setup GUI ---
        // Status Label with improved styling
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 0)); // Dark green
        headerPanel.setBorder(BorderFactory.createEmptyBorder(
                GameUtils.scale(3), GameUtils.scale(10), 
                GameUtils.scale(3), GameUtils.scale(10)));
        
        statusLabel = new JLabel("Game Started. Initializing...");
        statusLabel.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 16));
        statusLabel.setForeground(Color.WHITE);
        headerPanel.add(statusLabel, BorderLayout.CENTER);
        
        basePanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create main game panel (center)
        JPanel mainGamePanel = new JPanel(new BorderLayout(GameUtils.scale(10), 0)); // Increased spacing between components
        
        // Calculate optimal size for the game board
        int boardSize = Math.min(screenRect.height - GameUtils.scale(120), screenRect.width - GameUtils.scale(500));
        
        // Make sure the board size is reasonable
        boardSize = Math.max(boardSize, GameUtils.scale(600));

        // Game Board (center of main panel)
        gameBoardPanel = new GameBoardPanel(gameEngine.getBoard(), gameEngine);
        gameBoardPanel.setPreferredSize(new Dimension(boardSize, boardSize));
        
        // Add players to the game board
        for (Player player : players) {
            gameBoardPanel.addPlayer(player);
        }
        
        // Create a board container panel to center the board with padding
        JPanel boardContainer = new JPanel(new GridBagLayout());
        boardContainer.setBackground(new Color(230, 230, 230)); // Light gray background
        boardContainer.setBorder(BorderFactory.createEmptyBorder(
                GameUtils.scale(10), GameUtils.scale(10), 
                GameUtils.scale(10), GameUtils.scale(10)));
        
        boardContainer.add(gameBoardPanel);
        
        // Add the board container to main panel
        mainGamePanel.add(boardContainer, BorderLayout.CENTER);
        
        // Panels for player info, dice and controls - calculate side panel width as proportion of screen
        int sideWidth = Math.max(GameUtils.scale(250), (screenRect.width - boardSize) / 2 - GameUtils.scale(20));
        
        // Create left side panel for player info - keep it compact
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(
                GameUtils.scale(10), GameUtils.scale(10), 
                GameUtils.scale(10), GameUtils.scale(10)));
        leftPanel.setPreferredSize(new Dimension(sideWidth, boardSize));
        
        // Players Panel (left side) with compact layout
        playersPanel = new JPanel();
        playersPanel.setLayout(new GridLayout(players.size(), 1, 0, GameUtils.scale(5)));
        playersPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.GRAY, GameUtils.scale(1)),
                        "Players",
                        javax.swing.border.TitledBorder.CENTER,
                        javax.swing.border.TitledBorder.TOP,
                        GameUtils.getScaledFont("Arial", Font.BOLD, 14),
                        Color.DARK_GRAY
                ),
                BorderFactory.createEmptyBorder(
                    GameUtils.scale(2), GameUtils.scale(2), 
                    GameUtils.scale(2), GameUtils.scale(2))
        ));
        
        // Create player info panels with improved styling
        playerInfoPanels = new ArrayList<>();
        for (Player player : players) {
            PlayerInfoPanel infoPanel = new PlayerInfoPanel(player);
            playerInfoPanels.add(infoPanel);
            playersPanel.add(infoPanel);
        }
        
        leftPanel.add(playersPanel, BorderLayout.NORTH);
        
        // Create right side panel for dice, property info and controls
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(
                GameUtils.scale(5), GameUtils.scale(5), 
                GameUtils.scale(5), GameUtils.scale(5)));
        rightPanel.setPreferredSize(new Dimension(sideWidth, boardSize));
        
        // Dice Panel with fixed position at the top
        dicePanel = new DicePanel();
        dicePanel.setDiceRollListener(this);
        dicePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dicePanel.setRollEnabled(true);
        
        // Make dice panel prettier with a titled border
        dicePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(100, 150, 255), GameUtils.scale(2)),
                        "Dice",
                        javax.swing.border.TitledBorder.CENTER,
                        javax.swing.border.TitledBorder.TOP,
                        GameUtils.getScaledFont("Arial", Font.BOLD, 14),
                        new Color(0, 0, 150)
                ),
                BorderFactory.createEmptyBorder(
                    GameUtils.scale(2), GameUtils.scale(2), 
                    GameUtils.scale(2), GameUtils.scale(2))
        ));
        
        // Create a panel for dice with fixed height
        JPanel diceContainerPanel = new JPanel(new BorderLayout());
        diceContainerPanel.add(dicePanel, BorderLayout.CENTER);
        diceContainerPanel.setPreferredSize(new Dimension(sideWidth - GameUtils.scale(10), GameUtils.scale(250)));
        diceContainerPanel.setMinimumSize(new Dimension(sideWidth - GameUtils.scale(10), GameUtils.scale(250)));
        diceContainerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, GameUtils.scale(250)));
        
        rightPanel.add(diceContainerPanel);
        
        // Add separator for visual distinction
        JSeparator diceSeparator = new JSeparator(JSeparator.HORIZONTAL);
        diceSeparator.setPreferredSize(new Dimension(sideWidth - GameUtils.scale(20), GameUtils.scale(2)));
        diceSeparator.setMaximumSize(new Dimension(Integer.MAX_VALUE, GameUtils.scale(2)));
        diceSeparator.setForeground(new Color(180, 180, 180));
        diceSeparator.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        rightPanel.add(Box.createRigidArea(new Dimension(0, GameUtils.scale(10))));
        rightPanel.add(diceSeparator);
        rightPanel.add(Box.createRigidArea(new Dimension(0, GameUtils.scale(10))));
        
        // Property info panel (shows details of properties)
        propertyInfoPanel = new PropertyInfoPanel();
        propertyInfoPanel.setGameEngine(gameEngine); // Connect to game engine
        propertyInfoPanel.setMortgageService(gameEngine); // Connect to mortgage service
        propertyInfoPanel.setMinimumSize(new Dimension(sideWidth - GameUtils.scale(20), GameUtils.scale(180)));
        propertyInfoPanel.setPreferredSize(new Dimension(sideWidth - GameUtils.scale(20), GameUtils.scale(180)));
        propertyInfoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, GameUtils.scale(180)));
        rightPanel.add(propertyInfoPanel);
        
        // Add "Develop Properties" button
        JButton developPropertiesButton = new JButton("Develop Properties");
        developPropertiesButton.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 14));
        developPropertiesButton.setBackground(new Color(200, 230, 200));
        developPropertiesButton.setForeground(new Color(0, 80, 0));
        developPropertiesButton.setFocusPainted(false);
        developPropertiesButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 100, 0), GameUtils.scale(2)),
                BorderFactory.createEmptyBorder(
                    GameUtils.scale(5), GameUtils.scale(10), 
                    GameUtils.scale(5), GameUtils.scale(10))
        ));
        
        // Add hover effect
        developPropertiesButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (developPropertiesButton.isEnabled()) {
                    developPropertiesButton.setBackground(new Color(180, 220, 180));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (developPropertiesButton.isEnabled()) {
                    developPropertiesButton.setBackground(new Color(200, 230, 200));
                }
            }
        });
        
        developPropertiesButton.addActionListener(e -> {
            PropertyDevelopmentPanel.showDialog(this, gameEngine);
            // Update UI after dialog closes in case properties were developed
            updatePlayerInfoPanels();
            gameBoardPanel.repaint();
        });
        
        JPanel developPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        developPanel.add(developPropertiesButton);
        developPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, GameUtils.scale(50)));
        rightPanel.add(developPanel);
        
        // Add separator for visual distinction
        JSeparator propertySeparator = new JSeparator(JSeparator.HORIZONTAL);
        propertySeparator.setPreferredSize(new Dimension(sideWidth - GameUtils.scale(20), GameUtils.scale(2)));
        propertySeparator.setMaximumSize(new Dimension(Integer.MAX_VALUE, GameUtils.scale(2)));
        propertySeparator.setForeground(new Color(180, 180, 180));
        propertySeparator.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        rightPanel.add(Box.createRigidArea(new Dimension(0, GameUtils.scale(10))));
        rightPanel.add(propertySeparator);
        rightPanel.add(Box.createRigidArea(new Dimension(0, GameUtils.scale(10))));
        
        // Game action buttons panel
        JPanel actionButtonsPanel = new JPanel();
        actionButtonsPanel.setLayout(new BoxLayout(actionButtonsPanel, BoxLayout.X_AXIS));
        actionButtonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionButtonsPanel.setMaximumSize(new Dimension(sideWidth - GameUtils.scale(20), GameUtils.scale(60)));
        
        // Buy property button with improved look
        buyPropertyButton = new JButton("Buy Property");
        buyPropertyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buyPropertyButton.addActionListener(e -> buyCurrentProperty());
        buyPropertyButton.setEnabled(false); // Initially disabled
        
        // Style the buy button
        buyPropertyButton.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 14));
        buyPropertyButton.setBackground(new Color(152, 251, 152)); // Pale green
        buyPropertyButton.setForeground(new Color(0, 100, 0));
        buyPropertyButton.setFocusPainted(false);
        buyPropertyButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 120, 0), GameUtils.scale(2)),
                BorderFactory.createEmptyBorder(
                    GameUtils.scale(5), GameUtils.scale(10), 
                    GameUtils.scale(5), GameUtils.scale(10))
        ));
        
        // Add hover effect
        buyPropertyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (buyPropertyButton.isEnabled()) {
                    buyPropertyButton.setBackground(new Color(127, 255, 127));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (buyPropertyButton.isEnabled()) {
                    buyPropertyButton.setBackground(new Color(152, 251, 152));
                }
            }
        });
        
        // Add Surrender button
        JButton surrenderButton = new JButton("Surrender");
        surrenderButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        surrenderButton.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 14));
        surrenderButton.setBackground(new Color(255, 99, 71)); // Tomato red
        surrenderButton.setForeground(Color.WHITE);
        surrenderButton.setFocusPainted(false);
        surrenderButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(178, 34, 34), GameUtils.scale(2)), // Firebrick
                BorderFactory.createEmptyBorder(
                    GameUtils.scale(5), GameUtils.scale(10), 
                    GameUtils.scale(5), GameUtils.scale(10))
        ));
        
        // Add hover effect for surrender button
        surrenderButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                surrenderButton.setBackground(new Color(220, 20, 60)); // Crimson
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                surrenderButton.setBackground(new Color(255, 99, 71)); // Back to Tomato
            }
        });
        
        // Add surrender action
        surrenderButton.addActionListener(e -> handleSurrender());
        
        // Add buttons to the panel with some spacing
        actionButtonsPanel.add(Box.createHorizontalGlue());
        actionButtonsPanel.add(buyPropertyButton);
        actionButtonsPanel.add(Box.createRigidArea(new Dimension(GameUtils.scale(10), 0)));
        actionButtonsPanel.add(surrenderButton);
        actionButtonsPanel.add(Box.createHorizontalGlue());
        
        rightPanel.add(actionButtonsPanel);
        
        // Add separator for visual distinction
        JSeparator buttonsSeparator = new JSeparator(JSeparator.HORIZONTAL);
        buttonsSeparator.setPreferredSize(new Dimension(sideWidth - GameUtils.scale(20), GameUtils.scale(2)));
        buttonsSeparator.setMaximumSize(new Dimension(Integer.MAX_VALUE, GameUtils.scale(2)));
        buttonsSeparator.setForeground(new Color(180, 180, 180));
        buttonsSeparator.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        rightPanel.add(Box.createRigidArea(new Dimension(0, GameUtils.scale(10))));
        rightPanel.add(buttonsSeparator);
        rightPanel.add(Box.createRigidArea(new Dimension(0, GameUtils.scale(10))));
        
        // Log Area with improved styling - make it fill remaining space
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(GameUtils.getScaledFont("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(250, 250, 240));
        logArea.setBorder(BorderFactory.createEmptyBorder(
                GameUtils.scale(2), GameUtils.scale(2), 
                GameUtils.scale(2), GameUtils.scale(2)));
        
        // Create scroll pane with fixed height for the log area
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(sideWidth - GameUtils.scale(20), GameUtils.scale(200)));
        scrollPane.setMinimumSize(new Dimension(sideWidth - GameUtils.scale(20), GameUtils.scale(200)));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, GameUtils.scale(200)));
        
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.GRAY, GameUtils.scale(1)),
                        "Game Log",
                        javax.swing.border.TitledBorder.CENTER,
                        javax.swing.border.TitledBorder.TOP,
                        GameUtils.getScaledFont("Arial", Font.BOLD, 14),
                        Color.DARK_GRAY
                ),
                BorderFactory.createEmptyBorder(
                    GameUtils.scale(2), GameUtils.scale(2), 
                    GameUtils.scale(2), GameUtils.scale(2))
        ));
        
        // Create a fixed-height panel to contain the log
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.add(scrollPane, BorderLayout.CENTER);
        logPanel.setPreferredSize(new Dimension(sideWidth - GameUtils.scale(10), GameUtils.scale(220)));
        logPanel.setMinimumSize(new Dimension(sideWidth - GameUtils.scale(10), GameUtils.scale(220)));
        logPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, GameUtils.scale(220)));
        
        // Add log panel to the right side with appropriate constraints
        rightPanel.add(logPanel);
        
        // Add side panels to main panel
        mainGamePanel.add(leftPanel, BorderLayout.WEST);
        mainGamePanel.add(rightPanel, BorderLayout.EAST);
        
        // Add main panel to base panel
        basePanel.add(mainGamePanel, BorderLayout.CENTER);
        
        // Create notification panel (initially invisible)
        notificationPanel = new NotificationPanel();
        notificationPanel.setSize(400, 100);
        notificationPanel.setLocation(
                (getWidth() - notificationPanel.getWidth()) / 2,
                (getHeight() - notificationPanel.getHeight()) / 2);
        notificationPanel.setVisible(false);
        layeredPane.add(notificationPanel, JLayeredPane.POPUP_LAYER);

        // Display welcome message
        logArea.append("--- Monopoly Game Started! ---\n");
        logArea.append("Players: " + playerNames.size() + "\n");
        for (int i = 0; i < playerNames.size(); i++) {
            logArea.append("  - " + playerNames.get(i) + " (" + 
                    selectedCharacters.get(i).getName() + ")\n");
        }
        logArea.append("\n");
        logArea.append("Roll the dice to begin!\n");
        
        updateStatusLabel();
        updatePlayerHighlights();

        setVisible(true);
        
        // Show welcome notification after a short delay
        Timer welcomeTimer = new Timer(500, e -> {
            showNotification("Welcome to Monopoly!", "Roll the dice to start your turn.", 
                    new Color(0, 150, 0));
        });
        welcomeTimer.setRepeats(false);
        welcomeTimer.start();
    }
    
    /**
     * Tạo thanh menu cho game.
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // Menu Game
        JMenu gameMenu = new JMenu("Game");
        
        JMenuItem newGameItem = new JMenuItem("Trò chơi mới");
        newGameItem.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn bắt đầu trò chơi mới không? Tiến trình hiện tại sẽ bị mất.",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);
            
            if (option == JOptionPane.YES_OPTION) {
                dispose();
                SwingUtilities.invokeLater(() -> {
                    new Main();
                });
            }
        });
        
        JMenuItem reloadPrisonItem = new JMenuItem("Tải lại hình ảnh nhà tù");
        reloadPrisonItem.addActionListener(e -> {
            // Delete existing prison image
            File prisonFile = new File("images/prision.png");
            if (prisonFile.exists()) {
                prisonFile.delete();
            }
            
            // Create new prison image
            GameUtils.createPrisonImage();
            
            // Inform user to restart game to see changes
            JOptionPane.showMessageDialog(this,
                "Hình ảnh nhà tù đã được tạo lại.\nBạn cần khởi động lại trò chơi để thấy thay đổi.",
                "Tạo hình ảnh hoàn tất",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        JMenuItem reloadCarItem = new JMenuItem("Khôi phục hình ảnh xe hơi mặc định");
        reloadCarItem.addActionListener(e -> {
            // Ask user if they want to replace their custom car image with the default one
            int option = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn thay thế hình ảnh xe hơi hiện tại bằng hình mặc định không?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);
            
            if (option == JOptionPane.YES_OPTION) {
                // Delete existing car image
                File carFile = new File("images/car.png");
                if (carFile.exists()) {
                    carFile.delete();
                }
                
                // Create new car image
                GameUtils.createRedCarImage();
                
                // Inform user to restart game to see changes
                JOptionPane.showMessageDialog(this,
                    "Hình ảnh xe hơi đã được khôi phục về mặc định.\nBạn cần khởi động lại trò chơi để thấy thay đổi.",
                    "Tạo hình ảnh hoàn tất",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        JMenuItem reloadDogItem = new JMenuItem("Khôi phục hình ảnh chó mặc định");
        reloadDogItem.addActionListener(e -> {
            // Ask user if they want to replace their custom dog image with the default one
            int option = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn thay thế hình ảnh chó hiện tại bằng hình mặc định không?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);
            
            if (option == JOptionPane.YES_OPTION) {
                // Delete existing dog image
                File dogFile = new File("images/dog.png");
                if (dogFile.exists()) {
                    dogFile.delete();
                }
                
                // Create new default dog image
                GameUtils.createBlueDogImage();
                
                // Inform user to restart game to see changes
                JOptionPane.showMessageDialog(this,
                    "Hình ảnh chó đã được khôi phục về mặc định.\nBạn cần khởi động lại trò chơi để thấy thay đổi.",
                    "Tạo hình ảnh hoàn tất",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        JMenuItem reloadHatItem = new JMenuItem("Khôi phục hình ảnh mũ mặc định");
        reloadHatItem.addActionListener(e -> {
            // Ask user if they want to replace their custom hat image with the default one
            int option = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn thay thế hình ảnh mũ hiện tại bằng hình mặc định không?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);
            
            if (option == JOptionPane.YES_OPTION) {
                // Delete existing hat image
                File hatFile = new File("images/hat.png");
                if (hatFile.exists()) {
                    hatFile.delete();
                }
                
                // Create new default hat image
                GameUtils.createHatImage();
                
                // Inform user to restart game to see changes
                JOptionPane.showMessageDialog(this,
                    "Hình ảnh mũ đã được khôi phục về mặc định.\nBạn cần khởi động lại trò chơi để thấy thay đổi.",
                    "Tạo hình ảnh hoàn tất",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        JMenuItem removeWhiteBackgroundItem = new JMenuItem("Loại bỏ nền trắng của hình ảnh chó");
        removeWhiteBackgroundItem.addActionListener(e -> {
            try {
                // Load the dog image
                File dogFile = new File("images/dog.png");
                if (!dogFile.exists()) {
                    JOptionPane.showMessageDialog(this,
                        "Không tìm thấy hình ảnh chó.\nVui lòng đảm bảo tệp dog.png đã tồn tại trong thư mục images.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Load the image
                BufferedImage originalImage = ImageIO.read(dogFile);
                if (originalImage == null) {
                    JOptionPane.showMessageDialog(this,
                        "Không thể đọc tệp hình ảnh chó.\nTệp có thể bị hỏng hoặc không phải định dạng hình ảnh hợp lệ.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Remove white background
                BufferedImage transparentImage = GameUtils.removeWhiteBackground(originalImage);
                
                // Create backup of original file
                File backupFile = new File("images/dog_backup.png");
                if (backupFile.exists()) {
                    backupFile.delete();
                }
                Files.copy(dogFile.toPath(), backupFile.toPath());
                
                // Save the new image
                ImageIO.write(transparentImage, "png", dogFile);
                
                // Inform user
                JOptionPane.showMessageDialog(this,
                    "Đã xử lý hình ảnh chó để loại bỏ nền trắng.\nBạn cần khởi động lại trò chơi để thấy thay đổi.\n" +
                    "Bản sao lưu của hình ảnh gốc đã được lưu tại images/dog_backup.png",
                    "Xử lý hình ảnh hoàn tất",
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi xử lý hình ảnh: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JMenuItem removeCarWhiteBackgroundItem = new JMenuItem("Loại bỏ nền trắng của hình ảnh xe hơi");
        removeCarWhiteBackgroundItem.addActionListener(e -> {
            try {
                // Load the car image
                File carFile = new File("images/car.png");
                if (!carFile.exists()) {
                    JOptionPane.showMessageDialog(this,
                        "Không tìm thấy hình ảnh xe hơi.\nVui lòng đảm bảo tệp car.png đã tồn tại trong thư mục images.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Load the image
                BufferedImage originalImage = ImageIO.read(carFile);
                if (originalImage == null) {
                    JOptionPane.showMessageDialog(this,
                        "Không thể đọc tệp hình ảnh xe hơi.\nTệp có thể bị hỏng hoặc không phải định dạng hình ảnh hợp lệ.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Remove white background
                BufferedImage transparentImage = GameUtils.removeWhiteBackground(originalImage);
                
                // Create backup of original file
                File backupFile = new File("images/car_backup.png");
                if (backupFile.exists()) {
                    backupFile.delete();
                }
                Files.copy(carFile.toPath(), backupFile.toPath());
                
                // Save the new image
                ImageIO.write(transparentImage, "png", carFile);
                
                // Inform user
                JOptionPane.showMessageDialog(this,
                    "Đã xử lý hình ảnh xe hơi để loại bỏ nền trắng.\nBạn cần khởi động lại trò chơi để thấy thay đổi.\n" +
                    "Bản sao lưu của hình ảnh gốc đã được lưu tại images/car_backup.png",
                    "Xử lý hình ảnh hoàn tất",
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi xử lý hình ảnh: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JMenuItem removeHatWhiteBackgroundItem = new JMenuItem("Loại bỏ nền trắng của hình ảnh mũ");
        removeHatWhiteBackgroundItem.addActionListener(e -> {
            try {
                // Load the hat image
                File hatFile = new File("images/hat.png");
                if (!hatFile.exists()) {
                    JOptionPane.showMessageDialog(this,
                        "Không tìm thấy hình ảnh mũ.\nVui lòng đảm bảo tệp hat.png đã tồn tại trong thư mục images.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Load the image
                BufferedImage originalImage = ImageIO.read(hatFile);
                if (originalImage == null) {
                    JOptionPane.showMessageDialog(this,
                        "Không thể đọc tệp hình ảnh mũ.\nTệp có thể bị hỏng hoặc không phải định dạng hình ảnh hợp lệ.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Remove white background
                BufferedImage transparentImage = GameUtils.removeWhiteBackground(originalImage);
                
                // Create backup of original file
                File backupFile = new File("images/hat_backup.png");
                if (backupFile.exists()) {
                    backupFile.delete();
                }
                Files.copy(hatFile.toPath(), backupFile.toPath());
                
                // Save the new image
                ImageIO.write(transparentImage, "png", hatFile);
                
                // Inform user
                JOptionPane.showMessageDialog(this,
                    "Đã xử lý hình ảnh mũ để loại bỏ nền trắng.\nBạn cần khởi động lại trò chơi để thấy thay đổi.\n" +
                    "Bản sao lưu của hình ảnh gốc đã được lưu tại images/hat_backup.png",
                    "Xử lý hình ảnh hoàn tất",
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi khi xử lý hình ảnh: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JMenuItem exitItem = new JMenuItem("Thoát");
        exitItem.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn thoát game không?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);
            
            if (option == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        
        gameMenu.add(newGameItem);
        gameMenu.add(reloadPrisonItem);
        gameMenu.add(reloadCarItem);
        gameMenu.add(reloadDogItem);
        gameMenu.add(reloadHatItem);
        gameMenu.add(removeWhiteBackgroundItem);
        gameMenu.add(removeCarWhiteBackgroundItem);
        gameMenu.add(removeHatWhiteBackgroundItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);
        
        // Menu Cài đặt
        JMenu settingsMenu = new JMenu("Cài đặt");
        
        JMenuItem displaySettingsItem = new JMenuItem("Cài đặt hiển thị");
        displaySettingsItem.addActionListener(e -> {
            // Hiển thị hộp thoại cài đặt
            boolean settingsChanged = SettingsDialog.showDialog(this);
            
            // Nếu cài đặt thay đổi, hiển thị thông báo khởi động lại
            if (settingsChanged) {
                // Một số điều chỉnh có thể áp dụng ngay lập tức
                updateUIWithCurrentSettings();
            }
        });
        
        settingsMenu.add(displaySettingsItem);
        
        // Menu Trợ giúp
        JMenu helpMenu = new JMenu("Trợ giúp");
        
        JMenuItem rulesItem = new JMenuItem("Luật chơi");
        rulesItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "<html><body width='400'>" +
                    "<h1>Luật chơi Monopoly</h1>" +
                    "<p>1. Mỗi người chơi bắt đầu với $1500.<br>" +
                    "2. Người chơi di chuyển dựa trên kết quả tung xúc xắc.<br>" +
                    "3. Khi đến ô đất trống, người chơi có thể mua.<br>" +
                    "4. Khi đến ô đất của người khác, phải trả tiền thuê.<br>" +
                    "5. Người chơi có thể xây nhà khi sở hữu tất cả ô cùng màu.<br>" +
                    "6. Người chơi phá sản sẽ bị loại.<br>" +
                    "7. Người chơi cuối cùng còn lại sẽ thắng.</p>" +
                    "</body></html>",
                    "Luật chơi Monopoly",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        
        JMenuItem aboutItem = new JMenuItem("Giới thiệu");
        aboutItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "<html><body width='350'>" +
                    "<h1>Monopoly Game</h1>" +
                    "<p>Phiên bản: 1.0<br><br>" +
                    "Game Monopoly là trò chơi chiến thuật kinh doanh bất động sản cổ điển.<br><br>" +
                    "© 2023 All Rights Reserved.</p>" +
                    "</body></html>",
                    "Giới thiệu",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        
        helpMenu.add(rulesItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);
        
        // Thêm menu vào thanh menu
        menuBar.add(gameMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);
        
        // Thêm menu vào frame
        setJMenuBar(menuBar);
    }
    
    /**
     * Cập nhật giao diện với cài đặt hiện tại
     */
    private void updateUIWithCurrentSettings() {
        // Cập nhật font chữ và kích thước các thành phần có thể thay đổi ngay
        if (statusLabel != null) {
            statusLabel.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 16));
        }
        
        // Cập nhật font của log area
        if (logArea != null) {
            logArea.setFont(GameUtils.getScaledFont("Monospaced", Font.PLAIN, 12));
        }
        
        // Cập nhật các panel thông tin người chơi
        if (playerInfoPanels != null) {
            for (PlayerInfoPanel panel : playerInfoPanels) {
                panel.updateUIScale();
            }
        }
        
        // Thông báo cần khởi động lại để áp dụng đầy đủ
        showNotification("Cài đặt đã thay đổi", 
                "Khởi động lại game để áp dụng đầy đủ cài đặt mới.", 
                new Color(0, 100, 200));
    }

    /**
     * Shows a popup notification.
     * 
     * @param title The notification title
     * @param message The notification message
     * @param color The background color
     */
    private void showNotification(String title, String message, Color color) {
        notificationPanel.showNotification(title, message, color);
        
        // Position notifications to not cover dice - place in the upper part of the screen
        notificationPanel.setLocation(
                (layeredPane.getWidth() - notificationPanel.getWidth()) / 2,
                layeredPane.getHeight() / 4); // Position in the top quarter of the screen
    }
    
    /**
     * Shows a dialog to get the number of players.
     * 
     * @return Number of players selected, or -1 if canceled
     */
    private int showNumPlayersDialog() {
        // Create a custom panel with a vertical layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(new Color(240, 255, 240));
        
        // Title label
        JLabel titleLabel = new JLabel("MONOPOLY GAME", JLabel.CENTER);
        titleLabel.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(0, 102, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Message label
        JLabel messageLabel = new JLabel("Select number of players:", JLabel.CENTER);
        messageLabel.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 24));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

        // Create large, styled buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 10));
        buttonPanel.setBackground(new Color(240, 255, 240));
        
        // Add components to main panel
        mainPanel.add(titleLabel);
        mainPanel.add(messageLabel);
        mainPanel.add(buttonPanel);
        
        // Create a result holder since we need to access it from the action listener
        final int[] result = new int[] { -1 };
        
        // Create the buttons
        for (int i = 2; i <= 4; i++) {
            final int numPlayers = i;
            JButton playerButton = new JButton(String.valueOf(i));
            playerButton.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 28));
            playerButton.setBackground(new Color(60, 179, 113)); // Medium sea green
            playerButton.setForeground(Color.WHITE);
            playerButton.setFocusPainted(false);
            playerButton.setPreferredSize(new Dimension(120, 120));
            
            // Make the button round
            playerButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            playerButton.setMargin(new Insets(10, 10, 10, 10));
            
            // Add hover effect
            playerButton.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    playerButton.setBackground(new Color(46, 139, 87)); // Sea green
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    playerButton.setBackground(new Color(60, 179, 113));
                }
            });
            
            playerButton.addActionListener(e -> {
                result[0] = numPlayers;
                // Find and close the dialog
                Window dialog = SwingUtilities.getWindowAncestor(playerButton);
                dialog.dispose();
            });
            
            buttonPanel.add(playerButton);
        }
        
        // Cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 20));
        cancelButton.setBackground(new Color(220, 220, 220));
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setFocusPainted(false);
        cancelButton.setPreferredSize(new Dimension(150, 50));
        cancelButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        cancelButton.addActionListener(e -> {
            result[0] = -1;
            // Find and close the dialog
            Window dialog = SwingUtilities.getWindowAncestor(cancelButton);
            dialog.dispose();
        });
        
        JPanel cancelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cancelPanel.setBackground(new Color(240, 255, 240));
        cancelPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        cancelPanel.add(cancelButton);
        
        mainPanel.add(cancelPanel);
        
        // Create and show a custom dialog
        JDialog dialog = new JDialog(this, "Monopoly Game", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(mainPanel);
        dialog.pack();
        dialog.setSize(600, 500); // Set a fixed size
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        
        return result[0];
    }
    
    /**
     * Attempts to buy the property the current player is on.
     */
    private void buyCurrentProperty() {
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
                    
                    // Log the purchase
                    appendToLog(currentPlayer.getName() + " purchased " + 
                                  property.getName() + " for $" + property.getPrice() + ".\n");
                    
                    // Show a notification
                    showNotification("Property Purchased!", 
                            currentPlayer.getName() + " now owns " + property.getName(), 
                            new Color(0, 150, 0));
                    
                    // Update UI elements
                    updatePlayerInfoPanels();
                    buyPropertyButton.setEnabled(false);
                    
                    // Update board to show ownership
                    gameBoardPanel.updatePropertyOwnership(property);
                    
                    // Update property info panel
                    propertyInfoPanel.updateForPlayer(currentPlayer);
                    
                } else {
                    // Can't afford
                    appendToLog(currentPlayer.getName() + " cannot afford " + 
                                  property.getName() + " ($" + property.getPrice() + ").\n");
                    
                    // Show a notification
                    showNotification("Cannot Afford Property", 
                            "You need $" + property.getPrice() + " to buy " + property.getName(), 
                            new Color(200, 0, 0));
                }
            }
        }
    }

    /**
     * Called when dice are rolled.
     */
    @Override
    public void onDiceRolled(int die1, int die2) {
        Player currentPlayer = gameEngine.getCurrentPlayer();
        int steps = dicePanel.getDiceTotal(); // Lấy tổng tất cả (4) xúc xắc
        boolean isDouble = dicePanel.isDouble();
        
        appendToLog("\n------------------------------\n");
        appendToLog(currentPlayer.getName() + " rolled for a total of " + steps + (isDouble ? " (DOUBLES!)" : "") + "\n");
        
        // Track if player has purchased a property this turn
        final boolean[] propertyPurchased = {false};
        
        // KHÔNG gọi currentPlayer.move ở đây!
        // Animate player movement và xử lý logic sau khi animation kết thúc
        gameBoardPanel.animatePlayerMovement(currentPlayer, steps, () -> {
            // Sau khi animation xong, xử lý logic game
            int oldPosition = currentPlayer.getPosition() - steps;
            if (oldPosition < 0) oldPosition += gameEngine.getBoard().size();
            int newPosition = currentPlayer.getPosition();
            boolean passedGo = newPosition < oldPosition;
        
            // Handle passed GO
            if (passedGo) {
                currentPlayer.addMoney(200); // GO Salary
                appendToLog(currentPlayer.getName() + " passed GO and collected $200.\n");
            }
            
            // Get the current space
            Space currentSpace = gameEngine.getBoard().get(currentPlayer.getPosition());
            appendToLog(currentPlayer.getName() + " landed on " + currentSpace.getName() + ".\n");
            
            // Handle landed on space logic
            handleLandedOnSpace(currentPlayer, currentSpace);
            
            // Update UI
            updatePlayerInfoPanels();
            
            // Disable dice rolling until next turn
            dicePanel.setRollEnabled(false);
            
            // If doubles, show a notification
            if (isDouble) {
                showNotification("Doubles Rolled!", 
                            currentPlayer.getName() + " can roll again", 
                        new Color(255, 140, 0));
            }
            
            // Check for bankruptcy
            if (currentPlayer.isBankrupt()) {
                appendToLog(currentPlayer.getName() + " went bankrupt!\n");
                gameEngine.handleBankruptcy(currentPlayer);
                
                // Show bankruptcy notification
                showNotification("Bankruptcy!", 
                        currentPlayer.getName() + " is out of the game", 
                        new Color(200, 0, 0));
                
                // Check if game is over
                if (gameEngine.isGameOver()) {
                    endGame();
                    return; // Don't continue
                }
            }
            
            // Tự động chuyển người chơi sau 2 giây, trừ khi player đã roll double
            if (isDouble && !currentPlayer.isInJail() && !currentPlayer.isBankrupt() && !gameEngine.isGameOver()) {
                appendToLog(currentPlayer.getName() + " rolled doubles and gets another turn!\n");
                
                // Cho phép roll lại sau 1.5 giây
                Timer diceTimer = new Timer(1500, e -> {
                    dicePanel.setRollEnabled(true);
                });
                diceTimer.setRepeats(false);
                diceTimer.start();
            } else {
                // Override the buy property button action to track purchases
                if (buyPropertyButton.isEnabled()) {
                    // Create a new ActionListener that will set the propertyPurchased flag
                    buyPropertyButton.removeActionListener(buyPropertyButton.getActionListeners()[0]);
                    buyPropertyButton.addActionListener(e -> {
                        buyCurrentProperty();
                        propertyPurchased[0] = true;
                        
                        // After purchase, wait 2 seconds then advance to next player
                        Timer nextPlayerTimer = new Timer(2000, event -> {
                            advanceToNextPlayer();
                        });
                        nextPlayerTimer.setRepeats(false);
                        nextPlayerTimer.start();
                    });
                    
                    // If player doesn't buy within 8 seconds, auto-advance
                    Timer autoAdvanceTimer = new Timer(8000, e -> {
                        if (!propertyPurchased[0]) {
                            appendToLog(currentPlayer.getName() + " did not purchase the property.\n");
                            advanceToNextPlayer();
                        }
                    });
                    autoAdvanceTimer.setRepeats(false);
                    autoAdvanceTimer.start();
                } else {
                    // If there's no property to buy, advance after 2 seconds as before
                    Timer nextPlayerTimer = new Timer(2000, e -> {
                        advanceToNextPlayer();
                    });
                    nextPlayerTimer.setRepeats(false);
                    nextPlayerTimer.start();
                }
            }
        });
    }
    
    /**
     * Animates a money transfer between players (for rent payment).
     * 
     * @param fromPlayer The player paying
     * @param toPlayer The player receiving payment
     * @param amount The amount being transferred
     */
    private void animateMoneyTransfer(Player fromPlayer, Player toPlayer, int amount) {
        // Get player info panels for both players
        PlayerInfoPanel fromPanel = null;
        PlayerInfoPanel toPanel = null;
        
        for (PlayerInfoPanel panel : playerInfoPanels) {
            if (panel.getPlayer() == fromPlayer) {
                fromPanel = panel;
            }
            if (panel.getPlayer() == toPlayer) {
                toPanel = panel;
            }
        }
        
        // If we don't have both panels, can't animate
        if (fromPanel == null || toPanel == null) {
            return;
        }
        
        // Create the money transfer animation label
        JLabel moneyLabel = new JLabel("$" + amount);
        moneyLabel.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 24));
        moneyLabel.setForeground(new Color(0, 100, 0));
        moneyLabel.setOpaque(false);
        moneyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Get the screen locations
        Point fromLocation = fromPanel.getLocationOnScreen();
        Point toLocation = toPanel.getLocationOnScreen();
        
        // Convert to layered pane coordinates
        SwingUtilities.convertPointFromScreen(fromLocation, layeredPane);
        SwingUtilities.convertPointFromScreen(toLocation, layeredPane);
        
        // Position and size the money label
        moneyLabel.setBounds(
            fromLocation.x + fromPanel.getWidth() / 2 - 50,
            fromLocation.y + fromPanel.getHeight() / 2 - 20,
            100, 40);
        
        // Add to layered pane
        layeredPane.add(moneyLabel, JLayeredPane.DRAG_LAYER);
        layeredPane.revalidate();
        layeredPane.repaint();
        
        // Calculate destination and start animation
        final Point startPoint = new Point(moneyLabel.getX(), moneyLabel.getY());
        final Point endPoint = new Point(
            toLocation.x + toPanel.getWidth() / 2 - 50,
            toLocation.y + toPanel.getHeight() / 2 - 20);
        
        // Use a timer for animation
        final int totalFrames = 30;
        final int delay = 33; // ~30fps
        
        Timer animationTimer = new Timer(delay, new ActionListener() {
            int frame = 0;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                frame++;
                if (frame <= totalFrames) {
                    // Calculate current position with easing
                    double progress = (double) frame / totalFrames;
                    // Ease in-out curve
                    double easedProgress = 0.5 - 0.5 * Math.cos(Math.PI * progress);
                    
                    int currentX = startPoint.x + (int)((endPoint.x - startPoint.x) * easedProgress);
                    int currentY = startPoint.y + (int)((endPoint.y - startPoint.y) * easedProgress);
                    
                    // Add a bouncy arc to the movement
                    int bounceHeight = 50;
                    int bounceY = (int)(Math.sin(Math.PI * progress) * bounceHeight);
                    
                    moneyLabel.setLocation(currentX, currentY - bounceY);
                    
                    // Fade in at start, fade out at end
                    float alpha = 1.0f;
                    if (progress < 0.2) {
                        alpha = (float)(progress / 0.2);
                    } else if (progress > 0.8) {
                        alpha = (float)((1.0 - progress) / 0.2);
                    }
                    
                    moneyLabel.setForeground(new Color(0, 100, 0, (int)(alpha * 255)));
                    
                    // Make it grow and shrink
                    int size = (int)(24 + 8 * Math.sin(Math.PI * progress));
                    moneyLabel.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, size));
                } else {
                    // End of animation
                    ((Timer)e.getSource()).stop();
                    layeredPane.remove(moneyLabel);
                    layeredPane.repaint();
                    
                    // Update player info panels
                    updatePlayerInfoPanels();
                }
            }
        });
        
        animationTimer.start();
    }
    
    /**
     * Handles the logic for a player landing on a space.
     */
    private void handleLandedOnSpace(Player player, Space space) {
        // This is a simplified version of the GameEngine's handleLandedOnSpace
        
        // Handle property spaces
        if (space instanceof Buyable) {
            Buyable property = (Buyable) space;
            
            if (property.getOwner() == null) {
                // Property is unowned - can be purchased
                appendToLog(property.getName() + " is unowned and can be purchased for $" + 
                              property.getPrice() + ".\n");
                
                // Display property info
                propertyInfoPanel.displayProperty(property);
                
                // Enable the buy button
                buyPropertyButton.setEnabled(true);
                
                // Show notification about being able to buy
                showNotification("Property Available", 
                        property.getName() + " is available to purchase for " + 
                        GameUtils.formatMoney(property.getPrice()), 
                        new Color(0, 150, 0));
                
            } else if (property.getOwner() != player) {
                // Property is owned by another player - pay rent
                final int rentAmount = property.calculateRent();
                
                // Special handling for utilities which need dice roll
                int finalRent;
                if (property instanceof UtilitySpace) {
                    // Get last dice roll
                    int diceSum = dicePanel.getLastRoll();
                    
                    appendToLog(player.getName() + " landed on " + property.getName() + 
                                 ", owned by " + property.getOwner().getName() + ".\n");
                    appendToLog("Rent is calculated as " + rentAmount + " × dice roll (" + 
                                 diceSum + ") = $" + (rentAmount * diceSum) + ".\n");
                    finalRent = rentAmount * diceSum;
                } else {
                    appendToLog(player.getName() + " must pay $" + rentAmount + " rent to " + 
                                  property.getOwner().getName() + ".\n");
                    finalRent = rentAmount;
                }
                
                // Display property info
                propertyInfoPanel.displayProperty(property);
                
                // Show rent notification with a red background
                showNotification("Rent Due!", 
                        "You must pay " + GameUtils.formatMoney(finalRent) + " to " + 
                        property.getOwner().getName(), 
                        new Color(200, 0, 0));
                
                // Pay rent
                if (player.pay(finalRent)) {
                    property.getOwner().addMoney(finalRent);
                    appendToLog(player.getName() + " paid $" + finalRent + " to " + 
                                  property.getOwner().getName() + ".\n");
                    
                    // Animate the money transfer
                    animateMoneyTransfer(player, property.getOwner(), finalRent);
                    
                    // Show payment confirmation after a delay
                    Timer paymentTimer = new Timer(1500, e -> {
                        showNotification("Rent Paid", 
                                "You paid " + GameUtils.formatMoney(finalRent) + " to " + 
                                property.getOwner().getName(), 
                                new Color(150, 150, 0));
                    });
                    paymentTimer.setRepeats(false);
                    paymentTimer.start();
                    
                    // Update UI for both players
                    updatePlayerInfoPanels();
                    
                } else {
                    // Not enough money - will need to handle bankruptcy
                    appendToLog(player.getName() + " doesn't have enough money to pay rent!\n");
                    player.setBankrupt(true);
                    
                    // Show bankruptcy notification
                    showNotification("Bankruptcy!", 
                            player.getName() + " can't pay the rent and is bankrupt", 
                            new Color(200, 0, 0));
                    
                    // Handle bankruptcy
                    gameEngine.handleBankruptcy(player);
                    
                    // Check if game is over
                    if (gameEngine.isGameOver()) {
                        endGame();
                    }
                }
            } else {
                // Player owns this property
                appendToLog(player.getName() + " owns " + property.getName() + ".\n");
                
                // Display property info
                propertyInfoPanel.displayProperty(property);
                
                // Show welcome-home notification
                showNotification("Home Sweet Home", 
                        "Welcome back to your property: " + property.getName(), 
                        new Color(0, 100, 0));
            }
        }
        
        // Handle other space types (simplified)
        else if (space.getType() == SpaceType.GO_TO_JAIL) {
            appendToLog(player.getName() + " is going to jail!\n");
            player.goToJail(10); // Assuming jail is at position 10
            gameBoardPanel.updatePlayerPosition(player);
            
            // Show jail notification
            showNotification("Go to Jail!", 
                    player.getName() + " is being sent to jail", 
                    new Color(200, 0, 0));
            
        } else if (space.getType() == SpaceType.CHANCE) {
            // Display a simple chance card effect
            appendToLog(player.getName() + " landed on Chance.\n");
            
            // Simple chance card effect - collect or pay money
            int amount = (new Random().nextInt(20) - 10) * 10; // -100 to +100
            
            if (amount >= 0) {
                appendToLog("Chance card: Collect $" + amount + " from the bank.\n");
                player.addMoney(amount);
                showNotification("Chance Card", 
                        "Collect " + GameUtils.formatMoney(amount) + " from the bank", 
                        new Color(255, 165, 0));
            } else {
                appendToLog("Chance card: Pay $" + (-amount) + " to the bank.\n");
                player.pay(-amount);
                showNotification("Chance Card", 
                        "Pay " + GameUtils.formatMoney(-amount) + " to the bank", 
                        new Color(255, 165, 0));
            }
            
            updatePlayerInfoPanels();
            
        } else if (space.getType() == SpaceType.COMMUNITY_CHEST) {
            // Display a simple community chest card effect
            appendToLog(player.getName() + " landed on Community Chest.\n");
            
            // Simple chest card effect - collect money
            int amount = (new Random().nextInt(10) + 1) * 20; // 20 to 200
            appendToLog("Community Chest card: Collect $" + amount + " from the bank.\n");
            player.addMoney(amount);
            
            showNotification("Community Chest Card", 
                    "Collect " + GameUtils.formatMoney(amount) + " from the bank", 
                    new Color(0, 100, 255));
            
            updatePlayerInfoPanels();
        } else if (space.getType() == SpaceType.TAX) {
            // Handle tax spaces
            int taxAmount = 100; // Default tax amount
            
            // Income Tax is typically 200 or 10% of assets
            if (space.getName().contains("Income")) {
                taxAmount = 200;
            } 
            // Luxury Tax is typically 75
            else if (space.getName().contains("Luxury")) {
                taxAmount = 75;
            }
            
            appendToLog(player.getName() + " must pay $" + taxAmount + " in taxes.\n");
            
            if (player.pay(taxAmount)) {
                showNotification("Tax Payment", 
                        "You paid " + GameUtils.formatMoney(taxAmount) + " in taxes", 
                        new Color(150, 0, 0));
            } else {
                appendToLog(player.getName() + " can't afford to pay taxes!\n");
                player.setBankrupt(true);
                
                showNotification("Bankruptcy!", 
                        player.getName() + " can't pay taxes and is bankrupt", 
                        new Color(200, 0, 0));
                
                // Handle bankruptcy
                gameEngine.handleBankruptcy(player);
            }
            
            updatePlayerInfoPanels();
        }
    }
    
    /**
     * Advances to the next player's turn.
     */
    private void advanceToNextPlayer() {
        gameEngine.advanceToNextPlayer();
        
        // Skip bankrupt players
        while (gameEngine.getCurrentPlayer().isBankrupt() && !gameEngine.isGameOver()) {
            gameEngine.advanceToNextPlayer();
        }
        
        // Update UI for the new player
        updateStatusLabel();
        updatePlayerHighlights();
        
        // Show next player notification
        Player currentPlayer = gameEngine.getCurrentPlayer();
        showNotification("Next Player's Turn", 
                currentPlayer.getName() + ", it's your turn!", 
                new Color(180, 180, 240));
        
        // Reset controls for new turn
        dicePanel.setRollEnabled(true);
        buyPropertyButton.setEnabled(false);
        
        // Update property info panel for current player
        propertyInfoPanel.updateForPlayer(currentPlayer);
        
        appendToLog("\n------------------------------\n");
        appendToLog(gameEngine.getCurrentPlayer().getName() + "'s turn.\n");
    }
    
    /**
     * Handles the surrender action when a player clicks the Surrender button.
     */
    private void handleSurrender() {
        Player currentPlayer = gameEngine.getCurrentPlayer();
        
        // Ask for confirmation
        int confirm = JOptionPane.showConfirmDialog(
            this,
            currentPlayer.getName() + ", are you sure you want to surrender?\n" +
            "The game will end and rankings will be displayed based on current assets.",
            "Confirm Surrender",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            appendToLog("\n--- " + currentPlayer.getName() + " has surrendered! ---\n");
            appendToLog("Game Over.\n");
            
            // Mark player as surrendered
            currentPlayer.surrender();
            
            // Explicitly set the game as over
            gameEngine.setGameOver();
            
            // End the game and show rankings
            showGameRankings();
        }
    }
    
    /**
     * Shows the game rankings with animations when a player surrenders or game ends.
     */
    private void showGameRankings() {
        // Disable controls
        dicePanel.setRollEnabled(false);
        buyPropertyButton.setEnabled(false);
        
        // Calculate player rankings based on net worth
        List<Player> rankedPlayers = new ArrayList<>(gameEngine.getPlayers());
        
        // Sort players by net worth in descending order
        rankedPlayers.sort((p1, p2) -> Integer.compare(p2.getNetWorth(), p1.getNetWorth()));
        
        // Create and show the ranking dialog using the external class
        SwingUtilities.invokeLater(() -> {
            RankingDialog.showDialog(this, rankedPlayers);
        });
    }
    
    /**
     * Handle game over condition.
     */
    private void endGame() {
        // Display game over message
        appendToLog("\n--- GAME OVER ---\n");
        
        // Find the winner (non-bankrupt player)
        Player winner = null;
        for (Player player : gameEngine.getPlayers()) {
            if (!player.isBankrupt()) {
                winner = player;
                break;
            }
        }
        
        if (winner != null) {
            appendToLog("WINNER: " + winner.getName() + "!\n");
        }
        
        // Disable controls
        dicePanel.setRollEnabled(false);
        buyPropertyButton.setEnabled(false);
        
        // Show game over notification
        if (winner != null) {
            showNotification("Game Over!", 
                    winner.getName() + " wins the game!", 
                    new Color(0, 100, 0));
        } else {
            showNotification("Game Over!", 
                    "All players are bankrupt!", 
                    new Color(100, 0, 0));
        }
        
        // Show rankings after a short delay
        Timer rankingTimer = new Timer(2000, e -> {
            showGameRankings();
        });
        rankingTimer.setRepeats(false);
        rankingTimer.start();
    }
    
    /**
     * Updates the status label with current player info.
     */
    private void updateStatusLabel() {
        if (!gameEngine.isGameOver()) {
            Player currentPlayer = gameEngine.getCurrentPlayer();
            statusLabel.setText("Current Turn: " + currentPlayer.getName() +
                                " ($ " + currentPlayer.getMoney() +
                                ") | Position: " + currentPlayer.getPosition() +
                                (currentPlayer.isInJail() ? " (In Jail)" : ""));
        }
    }
    
    /**
     * Updates all player info panels.
     */
    private void updatePlayerInfoPanels() {
        for (PlayerInfoPanel infoPanel : playerInfoPanels) {
            infoPanel.updateDisplay();
        }
        // Update property info panel for current player
        propertyInfoPanel.updateForPlayer(gameEngine.getCurrentPlayer());
    }
    
    /**
     * Highlights the current player's panel.
     */
    private void updatePlayerHighlights() {
        if (gameEngine.isGameOver()) return;
        
        Player currentPlayer = gameEngine.getCurrentPlayer();
        List<Player> players = gameEngine.getPlayers();
        
        for (int i = 0; i < players.size(); i++) {
            playerInfoPanels.get(i).setCurrentPlayer(players.get(i) == currentPlayer);
        }
    }

    /**
     * Appends text to the log area and scrolls to make it visible.
     * 
     * @param text The text to append
     */
    private void appendToLog(String text) {
        logArea.append(text);
        // Auto-scroll to the bottom
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        // Ensure all game images are created
        GameUtils.ensureGameImages();
        
        // Force recreation of prison image
        File prisonFile = new File("images/prision.png");
        if (prisonFile.exists()) {
            prisonFile.delete();
        }
        GameUtils.createPrisonImage();
        
        // Do not force recreation of car image - use the user's provided image
        GameUtils.createRedCarImage(); // This will now keep the existing image if it exists
        
        // Do not force recreation of dog image - use the user's provided image
        GameUtils.createBlueDogImage(); // This will now keep the existing image if it exists
        
        // Do not force recreation of hat image - use the user's provided image
        GameUtils.createHatImage(); // This will now keep the existing image if it exists
        
        // Run the GUI construction in the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Main();
            }
        });
    }
}