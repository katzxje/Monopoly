import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Dialog to display player rankings with fancy animations.
 */
public class RankingDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private List<JLabel> medalLabels = new ArrayList<>();
    private List<JLabel> playerNameLabels = new ArrayList<>();
    private List<JLabel> playerMoneyLabels = new ArrayList<>();
    private List<JLabel> confettiLabels = new ArrayList<>();
    private final Color goldColor = new Color(255, 215, 0);
    private final Color silverColor = new Color(192, 192, 192);
    private final Color bronzeColor = new Color(205, 127, 50);
    private final Random random = new Random();
    
    /**
     * Creates a new ranking dialog.
     * 
     * @param parent The parent frame
     * @param rankedPlayers The list of players, sorted by rank
     */
    public RankingDialog(Frame parent, List<Player> rankedPlayers) {
        super(parent, "RANKING", true); 
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(65, 105, 225), 0, height, new Color(138, 43, 226));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, width, height);
            }
        };
        
        mainPanel.setLayout(new BorderLayout(0, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Title Panel with "RANKING"
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        JLabel titleLabel = new JLabel("RANKING");
        titleLabel.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 2, new Color(0, 0, 0, 80)),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        titlePanel.add(titleLabel);
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Winner section - show the top player in a special panel
        Player winner = rankedPlayers.get(0);
        JPanel winnerPanel = createWinnerPanel(winner);
        mainPanel.add(winnerPanel, BorderLayout.CENTER);
        
        // Ranking table section
        JPanel rankingPanel = createRankingTable(rankedPlayers);
        mainPanel.add(rankingPanel, BorderLayout.SOUTH);
        
        // Set main content pane
        setContentPane(mainPanel);
        
        // Start animations
        startConfettiAnimation();
        startMedalAnimation();
        startTextAnimation();
        playCelebrationSound();
    }
    
    /**
     * Creates a panel to display the winner
     */
    private JPanel createWinnerPanel(Player winner) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(goldColor, GameUtils.scale(3)),
            BorderFactory.createEmptyBorder(GameUtils.scale(15), GameUtils.scale(15), GameUtils.scale(15), GameUtils.scale(15))
        ));
        panel.setBackground(new Color(255, 250, 205));
        
        // Congratulation message
        String[] celebrationMessages = {
            "Congratulations!",
            "You are amazing!",
            "A well-deserved victory!",
            "Outstanding real estate investor!",
            "Monopoly business master!"
        };
        
        String celebrationMsg = celebrationMessages[random.nextInt(celebrationMessages.length)];
        
        JLabel congratsLabel = new JLabel(celebrationMsg);
        congratsLabel.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 24));
        congratsLabel.setForeground(new Color(178, 34, 34));
        congratsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel winnerLabel = new JLabel("WINNER");
        winnerLabel.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 20));
        winnerLabel.setForeground(new Color(0, 100, 0));
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Winner info panel
        JPanel winnerInfoPanel = new JPanel();
        winnerInfoPanel.setOpaque(false);
        
        // Character icon
        JLabel characterLabel = new JLabel();
        if (winner.getCharacter() != null && winner.getCharacter().getIcon() != null) {
            ImageIcon icon = winner.getCharacter().getIcon();
            Image scaledImage = icon.getImage().getScaledInstance(
                    GameUtils.scale(80), GameUtils.scale(80), Image.SCALE_SMOOTH);
            characterLabel.setIcon(new ImageIcon(scaledImage));
        }
        
        // Add player name with gold medal
        JLabel nameLabel = new JLabel(winner.getName());
        nameLabel.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 26));
        playerNameLabels.add(nameLabel);
        
        winnerInfoPanel.add(characterLabel);
        winnerInfoPanel.add(Box.createRigidArea(new Dimension(GameUtils.scale(10), 0)));
        winnerInfoPanel.add(nameLabel);
        
        // Total assets
        JLabel assetsLabel = new JLabel("Total assets: " + GameUtils.formatMoney(winner.getNetWorth()));
        assetsLabel.setFont(GameUtils.getScaledFont("Arial", Font.PLAIN, 18));
        assetsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerMoneyLabels.add(assetsLabel);
        
        panel.add(congratsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, GameUtils.scale(10))));
        panel.add(winnerLabel);
        panel.add(Box.createRigidArea(new Dimension(0, GameUtils.scale(15))));
        panel.add(winnerInfoPanel);
        panel.add(Box.createRigidArea(new Dimension(0, GameUtils.scale(10))));
        panel.add(assetsLabel);
        
        return panel;
    }
    
    /**
     * Creates a panel with a table showing player rankings
     */
    private JPanel createRankingTable(List<Player> rankedPlayers) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY, GameUtils.scale(1)),
            "RANKINGS",
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.TOP,
            GameUtils.getScaledFont("Arial", Font.BOLD, 16),
            Color.WHITE
        ));
        panel.setOpaque(false);
        
        // Table column names
        String[] columnNames = {"Rank", "Player", "Cash", "Property Value", "Total Assets", "Status"};
        
        // Create table data
        Object[][] data = new Object[rankedPlayers.size()][6];
        
        for (int i = 0; i < rankedPlayers.size(); i++) {
            Player player = rankedPlayers.get(i);
            int propertyValue = calculatePropertyValue(player);
            int totalAssets = player.getNetWorth();
            
            data[i][0] = i + 1; // Rank
            data[i][1] = player.getName(); // Player name
            data[i][2] = GameUtils.formatMoney(player.getMoney()); // Cash
            data[i][3] = GameUtils.formatMoney(propertyValue); // Property value
            data[i][4] = GameUtils.formatMoney(totalAssets); // Total assets
            
            // Status
            String status;
            if (player.isBankrupt()) {
                status = player.hasSurrendered() ? "Surrendered" : "Bankrupt";
            } else if (i == 0) {
                status = "Winner";
            } else {
                status = "In Game";
            }
            data[i][5] = status;
        }
        
        // Create and configure table
        JTable table = new JTable(data, columnNames);
        table.setFont(GameUtils.getScaledFont("Arial", Font.PLAIN, 14));
        table.setRowHeight(GameUtils.scale(30));
        table.getTableHeader().setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 14));
        table.setBackground(new Color(240, 248, 255));
        table.getTableHeader().setBackground(new Color(65, 105, 225));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setShowGrid(true);
        table.setGridColor(new Color(200, 200, 200));
        
        // Custom renderer for cells
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (row == 0) {
                    c.setBackground(new Color(255, 250, 205)); // Light yellow for winner
                    if (c instanceof JLabel) {
                        ((JLabel) c).setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 14));
                    }
                } else {
                    c.setBackground(row % 2 == 0 ? new Color(240, 248, 255) : Color.WHITE);
                    if (c instanceof JLabel) {
                        ((JLabel) c).setFont(GameUtils.getScaledFont("Arial", Font.PLAIN, 14));
                    }
                }
                
                // Center cell content
                if (c instanceof JLabel) {
                    ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                }
                
                return c;
            }
        });
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(GameUtils.scale(50)); // Rank
        table.getColumnModel().getColumn(1).setPreferredWidth(GameUtils.scale(120)); // Player
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(panel.getWidth(), GameUtils.scale(200)));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        
        JButton newGameButton = new JButton("New Game");
        newGameButton.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 14));
        newGameButton.setPreferredSize(new Dimension(GameUtils.scale(150), GameUtils.scale(35)));
        newGameButton.setBackground(new Color(65, 105, 225));
        newGameButton.setForeground(Color.WHITE);
        newGameButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        newGameButton.setFocusPainted(false);
        newGameButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { newGameButton.setBackground(new Color(25, 25, 112)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { newGameButton.setBackground(new Color(65, 105, 225)); }
        });
        newGameButton.addActionListener(e -> {
            dispose();
            ((JFrame)getOwner()).dispose();
            SwingUtilities.invokeLater(() -> {
                new Main();
            });
        });
        
        JButton okButton = new JButton("OK");
        okButton.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 14));
        okButton.setPreferredSize(new Dimension(GameUtils.scale(100), GameUtils.scale(35)));
        okButton.setBackground(new Color(65, 105, 225));
        okButton.setForeground(Color.WHITE);
        okButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        okButton.setFocusPainted(false);
        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { okButton.setBackground(new Color(25, 25, 112)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { okButton.setBackground(new Color(65, 105, 225)); }
        });
        okButton.addActionListener(e -> {
            dispose();
            System.exit(0);
        });
        
        buttonPanel.add(newGameButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(GameUtils.scale(20), 0)));
        buttonPanel.add(okButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Calculate property value for a player
     */
    private int calculatePropertyValue(Player player) {
        int total = 0;
        
        for (Buyable property : player.getProperties()) {
            total += property.getPrice();
            // Add property value only, without house/hotel details
            // since we don't know the exact interface of PropertySpace
        }
        
        return total;
    }
    
    /**
     * Creates confetti particles falling animation
     */
    private void startConfettiAnimation() {
        // Create confetti particles
        for (int i = 0; i < 100; i++) {
            JLabel confetti = new JLabel();
            int size = random.nextInt(15) + 8; // Increased size
            confetti.setBounds(
                random.nextInt(getWidth()),
                -50 - random.nextInt(300),
                size,
                size
            );
            
            // Random colors for confetti with improved brightness
            Color[] colors = {
                new Color(255, 0, 0),     // Bright Red
                new Color(0, 0, 255),     // Bright Blue
                new Color(0, 255, 0),     // Bright Green
                new Color(255, 255, 0),   // Bright Yellow
                new Color(255, 0, 255),   // Bright Magenta
                new Color(0, 255, 255),   // Bright Cyan
                new Color(255, 165, 0),   // Bright Orange
                new Color(255, 192, 203), // Pink
                new Color(255, 215, 0),   // Gold
                new Color(123, 104, 238)  // Medium Slate Blue
            };
            
            Color color = colors[random.nextInt(colors.length)];
            confetti.setOpaque(true);
            confetti.setBackground(color);
            
            // Make some confetti square and some round for variety
            if (random.nextBoolean()) {
                confetti.setBorder(BorderFactory.createLineBorder(color.darker(), 1));
            } else {
                // Make some confetti round
                confetti.setBorder(new RoundedBorder(10));
            }
            
            getContentPane().add(confetti);
            confettiLabels.add(confetti);
        }
        
        // Animate confetti falling
        Timer confettiTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (JLabel confetti : confettiLabels) {
                    // Get current position
                    int x = confetti.getX();
                    int y = confetti.getY();
                    
                    // Update position (add some horizontal movement for realistic effect)
                    x += (random.nextInt(3) - 1);
                    y += (random.nextInt(6) + 4);
                    
                    // Add some wobble for a more natural falling effect
                    if (random.nextInt(5) == 0) {
                        x += (random.nextInt(5) - 2);
                    }
                    
                    // Reset if it goes off screen
                    if (y > getHeight()) {
                        y = -20;
                        x = random.nextInt(getWidth());
                    }
                    
                    confetti.setLocation(x, y);
                }
            }
        });
        
        confettiTimer.start();
    }
    
    // Custom rounded border for some confetti pieces
    private class RoundedBorder implements Border {
        private int radius;
        
        public RoundedBorder(int radius) {
            this.radius = radius;
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius, this.radius, this.radius, this.radius);
        }
        
        @Override
        public boolean isBorderOpaque() {
            return true;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(c.getBackground());
            g2d.drawRoundRect(x, y, width-1, height-1, radius, radius);
            g2d.dispose();
        }
    }
    
    /**
     * Animates the medal icons with a pulsing effect
     */
    private void startMedalAnimation() {
        // Only animate top 3 medals if they exist
        int medals = Math.min(3, medalLabels.size());
        
        Timer medalTimer = new Timer(80, new ActionListener() {
            float scale = 1.0f;
            boolean growing = true;
            int tick = 0;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                tick++;
                
                // Adjust scale
                if (growing) {
                    scale += 0.05f;
                    if (scale >= 1.2f) growing = false;
                } else {
                    scale -= 0.05f;
                    if (scale <= 0.9f) growing = true;
                }
                
                // Apply scale to medals
                for (int i = 0; i < medals; i++) {
                    JLabel medal = medalLabels.get(i);
                    
                    // Only animate if it's time for this medal
                    // Stagger the animations for the 3 medals
                    if ((tick / 5) % 3 == i) {
                        Font currentFont = medal.getFont();
                        float newSize = currentFont.getSize() * scale;
                        medal.setFont(currentFont.deriveFont(newSize));
                        
                        // Also adjust position to keep it centered
                        int x = medal.getX();
                        int y = medal.getY();
                        int width = medal.getWidth();
                        int height = medal.getHeight();
                        
                        int newWidth = (int)(width * scale);
                        int newHeight = (int)(height * scale);
                        
                        int newX = x - (newWidth - width) / 2;
                        int newY = y - (newHeight - height) / 2;
                        
                        medal.setBounds(newX, newY, newWidth, newHeight);
                    }
                }
            }
        });
        
        medalTimer.start();
    }
    
    /**
     * Animates the text with a typing effect
     */
    private void startTextAnimation() {
        // Animate player names and money with a typing effect
        for (int i = 0; i < playerNameLabels.size(); i++) {
            final JLabel nameLabel = playerNameLabels.get(i);
            final JLabel moneyLabel = playerMoneyLabels.get(i);
            
            final String fullName = nameLabel.getText();
            final String fullMoney = moneyLabel.getText();
            
            // Clear initially
            nameLabel.setText("");
            moneyLabel.setText("");
            
            // Staggered start for each player
            Timer textTimer = new Timer(50, null);
            textTimer.setInitialDelay(i * 500); // Stagger by 500ms per player
            
            final int[] nameIndex = {0};
            final int[] moneyIndex = {0};
            
            textTimer.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Animate name typing
                    if (nameIndex[0] < fullName.length()) {
                        nameLabel.setText(fullName.substring(0, ++nameIndex[0]));
                    }
                    // After name is done, animate money typing
                    else if (moneyIndex[0] < fullMoney.length()) {
                        moneyLabel.setText(fullMoney.substring(0, ++moneyIndex[0]));
                    }
                    // Stop timer when both are done
                    else {
                        ((Timer)e.getSource()).stop();
                    }
                }
            });
            
            textTimer.start();
        }
    }
    
    /**
     * Plays a celebration sound sequence
     */
    private void playCelebrationSound() {
        new Thread(() -> {
            try {
                // Play a sequence of beeps for celebration
                for (int i = 0; i < 3; i++) {
                    Toolkit.getDefaultToolkit().beep();
                    Thread.sleep(300);
                }
            } catch (InterruptedException e) {
                // Ignore interruption
            }
        }).start();
    }
    
    /**
     * Shows a ranking dialog
     * 
     * @param parent The parent frame
     * @param rankedPlayers The list of ranked players
     */
    public static void showDialog(Frame parent, List<Player> rankedPlayers) {
        RankingDialog dialog = new RankingDialog(parent, rankedPlayers);
        dialog.setVisible(true);
    }
} 