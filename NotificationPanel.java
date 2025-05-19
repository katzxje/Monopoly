import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A panel that displays popup notifications during the game.
 */
public class NotificationPanel extends JPanel {
    private JLabel titleLabel;
    private JLabel messageLabel;
    private Timer fadeTimer;
    private int opacity = 255; // Full opacity
    
    /**
     * Creates a new notification panel.
     */
    public NotificationPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        setOpaque(false);
        
        // Title label
        titleLabel = new JLabel();
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Message label
        messageLabel = new JLabel();
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Add labels to panel
        JPanel contentPanel = new JPanel(new BorderLayout(5, 10));
        contentPanel.setOpaque(false);
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(messageLabel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Create fade timer
        fadeTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity -= 5;
                if (opacity <= 0) {
                    opacity = 0;
                    setVisible(false);
                    fadeTimer.stop();
                }
                repaint();
            }
        });
    }
    
    /**
     * Shows a notification with the specified title, message, and color.
     * 
     * @param title The notification title
     * @param message The notification message
     * @param color The background color
     */
    public void showNotification(String title, String message, Color color) {
        // Stop existing fade if in progress
        fadeTimer.stop();
        
        // Set notification content
        titleLabel.setText(title);
        messageLabel.setText(message);
        
        // Set title color based on background
        boolean isDarkBackground = isDarkColor(color);
        Color textColor = isDarkBackground ? Color.WHITE : Color.BLACK;
        titleLabel.setForeground(textColor);
        messageLabel.setForeground(textColor);
        
        // Reset opacity
        opacity = 255;
        
        // Show the notification
        setVisible(true);
        repaint();
        
        // Start fade out timer after 2.5 seconds
        Timer delayTimer = new Timer(2500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fadeTimer.start();
            }
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
    }
    
    /**
     * Determine if a color is dark (for text contrast).
     * 
     * @param color The color to check
     * @return True if the color is dark
     */
    private boolean isDarkColor(Color color) {
        // Simple algorithm to determine if a color is "dark"
        int brightness = (int) Math.sqrt(
            color.getRed() * color.getRed() * 0.299 +
            color.getGreen() * color.getGreen() * 0.587 +
            color.getBlue() * color.getBlue() * 0.114
        );
        return brightness < 130;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Create a semi-transparent background with rounded corners
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity / 255.0f));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Get background color from title
        Color bgColor = titleLabel.getBackground();
        
        // Fill rounded rectangle
        g2d.setColor(bgColor);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        
        // Draw border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
        
        g2d.dispose();
        
        super.paintComponent(g);
    }
    
    @Override
    public void setBackground(Color color) {
        super.setBackground(new Color(0, 0, 0, 0)); // Transparent
        if (titleLabel != null) {
            titleLabel.setBackground(color);
        }
    }
} 