import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

/**
 * A panel that displays dice roll button.
 */
public class DicePanel extends JPanel {
    private JButton rollButton;
    private Random random = new Random();
    
    // Kết quả xúc xắc gần nhất
    private int[] lastDiceValues = {1, 1, 1, 1}; // 4 xúc xắc
    private DiceRollListener rollListener;
    
    /**
     * Interface for dice roll listeners.
     */
    public interface DiceRollListener {
        /**
         * Called when dice finish rolling.
         * 
         * @param die1 Value of first die
         * @param die2 Value of second die
         */
        void onDiceRolled(int die1, int die2);
    }
    
    /**
     * Creates a new dice panel.
     */
    public DicePanel() {
        // Initialize scaling
        GameUtils.initializeScaling();
        
        int panelWidth = GameUtils.scale(300);
        int panelHeight = GameUtils.scale(100); // Giảm kích thước panel vì không cần vẽ xúc xắc
        
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        
        // Roll button với giao diện đẹp hơn
        rollButton = new JButton("Roll Dice");
        rollButton.setFont(GameUtils.getScaledFont("Arial", Font.BOLD, 20));
        rollButton.setBackground(new Color(200, 220, 255));
        rollButton.setForeground(new Color(20, 20, 150));
        rollButton.setFocusPainted(false);
        rollButton.setPreferredSize(new Dimension(GameUtils.scale(180), GameUtils.scale(50)));
        rollButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 150, 255), GameUtils.scale(2)),
                BorderFactory.createEmptyBorder(
                    GameUtils.scale(8), GameUtils.scale(15), 
                    GameUtils.scale(8), GameUtils.scale(15))
        ));
        
        // Hiệu ứng rollover cho button
        rollButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                rollButton.setBackground(new Color(170, 200, 255));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                rollButton.setBackground(new Color(200, 220, 255));
            }
        });
        
        // Chuyển sang gọi DicePopupDialog khi click Roll Dice
        rollButton.addActionListener(e -> showDiceRollDialog());
        
        // Panel cho button với căn giữa
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(rollButton);
        
        // Thêm components vào panel
        add(buttonPanel, BorderLayout.CENTER);
    }
    
    /**
     * Hiển thị popup dialog quay 4 xúc xắc.
     */
    private void showDiceRollDialog() {
        if (!rollButton.isEnabled()) {
            return;
        }
        
        // Disable roll button
        rollButton.setEnabled(false);
        
        // Hiệu ứng nhấn nút
        rollButton.setBackground(new Color(150, 180, 255));
        Timer resetButtonTimer = new Timer(100, e -> rollButton.setBackground(new Color(200, 220, 255)));
        resetButtonTimer.setRepeats(false);
        resetButtonTimer.start();
        
        // Hiển thị dialog với 4 xúc xắc
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        
        DicePopupDialog.showDiceRoll(frame, (diceValues, total) -> {
            // Lưu kết quả xúc xắc
            lastDiceValues = diceValues.clone();
            
            // Enable roll button sau khi animation kết thúc
            Timer enableTimer = new Timer(300, event -> {
                rollButton.setEnabled(true);
                
                // Thông báo cho listener
                if (rollListener != null) {
                    // Truyền 2 xúc xắc đầu tiên làm tham số, tổng dùng để di chuyển
                    rollListener.onDiceRolled(diceValues[0], diceValues[1]);
                }
            });
            enableTimer.setRepeats(false);
            enableTimer.start();
        });
    }
    
    /**
     * Set a listener for dice roll events.
     * 
     * @param listener The listener to notify when dice are rolled
     */
    public void setDiceRollListener(DiceRollListener listener) {
        this.rollListener = listener;
    }
    
    /**
     * Enable or disable the roll button.
     * 
     * @param enabled True to enable, false to disable
     */
    public void setRollEnabled(boolean enabled) {
        rollButton.setEnabled(enabled);
    }
    
    /**
     * Get the sum of all dice.
     * 
     * @return The total of all dice
     */
    public int getDiceTotal() {
        int total = 0;
        for (int value : lastDiceValues) {
            total += value;
        }
        return total;
    }
    
    /**
     * Get the sum of the last dice roll.
     * Alias for getDiceTotal() for clearer method naming.
     * 
     * @return The total of the last dice roll
     */
    public int getLastRoll() {
        return getDiceTotal();
    }
    
    /**
     * Check if the first two dice show doubles.
     * 
     * @return True if both dice show the same value
     */
    public boolean isDouble() {
        return lastDiceValues[0] == lastDiceValues[1];
    }
} 