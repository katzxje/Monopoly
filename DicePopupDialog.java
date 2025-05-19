import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Dialog hiển thị 4 viên xúc xắc 3D với animation.
 */
public class DicePopupDialog extends JDialog {
    private static final int ANIMATION_FRAMES = 40; // Increased frames for smoother animation
    private static final int ANIMATION_DELAY = 10; // Faster animation (~40fps)
    private static final int DICE_SIZE = 60;
    private static final int DOT_SIZE = 8;
    
    private int[] diceValues = new int[4];
    private double[] rotationAngles = new double[4];
    private double[] rotationSpeed = new double[4];
    private double[] scaleFactors = new double[4];
    private Point[] positions = new Point[4];
    
    private Timer animationTimer;
    private int frame = 0;
    private boolean animationComplete = false;
    private DiceRollListener listener;
    private Random random = new Random();
    
    /**
     * Interface cho callback khi xúc xắc quay xong.
     */
    public interface DiceRollListener {
        void onDiceRolled(int[] values, int total);
    }
    
    /**
     * Tạo dialog hiển thị 4 viên xúc xắc 3D quay.
     * 
     * @param parent Frame cha
     * @param listener Listener nhận kết quả
     */
    public DicePopupDialog(JFrame parent, DiceRollListener listener) {
        super(parent, "Rolling Dice", true);
        this.listener = listener;
        
        setUndecorated(true); // Không có title bar
        setSize(600, 500);
        setLocationRelativeTo(parent);
        setBackground(new Color(0, 0, 0, 0)); // Trong suốt
        
        // Panel với background gradient bán trong suốt
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Vẽ background gradient bán trong suốt
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(0, 50, 0, 220),
                    getWidth(), getHeight(), new Color(0, 100, 0, 220));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Vẽ viền
                g2d.setStroke(new BasicStroke(3f));
                g2d.setColor(new Color(255, 215, 0, 200)); // Gold
                g2d.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 18, 18);
                
                // Banner "ROLLING DICE"
                g2d.setFont(new Font("Arial", Font.BOLD, GameUtils.scale(32)));
                g2d.setColor(Color.WHITE);
                String title = "ROLLING DICE";
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(title);
                g2d.drawString(title, (getWidth() - textWidth) / 2, 60);
                
                // Vẽ các viên xúc xắc 3D nếu animation đã chạy
                if (frame > 0) {
                    for (int i = 0; i < 4; i++) {
                        drawDice3D(g2d, i);
                    }
                }
                
                // Hiển thị kết quả nếu animation hoàn thành
                if (animationComplete) {
                    // Vẽ tổng
                    int total = diceValues[0] + diceValues[1] + diceValues[2] + diceValues[3];
                    g2d.setFont(new Font("Arial", Font.BOLD, GameUtils.scale(48)));
                    g2d.setColor(new Color(255, 255, 0));
                    String result = "TOTAL: " + total;
                    textWidth = fm.stringWidth(result);
                    g2d.drawString(result, (getWidth() - textWidth) / 2, getHeight() - 60);
                }
                
                g2d.dispose();
            }
        };
        mainPanel.setOpaque(false);
        
        setContentPane(mainPanel);
        
        // Khởi tạo vị trí và giá trị cho từng viên xúc xắc
        positions[0] = new Point(150, 200);
        positions[1] = new Point(450, 200);
        positions[2] = new Point(150, 350);
        positions[3] = new Point(450, 350);
        
        for (int i = 0; i < 4; i++) {
            diceValues[i] = random.nextInt(6) + 1;
            rotationAngles[i] = random.nextDouble() * Math.PI * 2;
            rotationSpeed[i] = 0.2 + random.nextDouble() * 0.3;
            scaleFactors[i] = 0.8 + random.nextDouble() * 0.4;
        }
        
        // Bắt đầu animation ngay khi hiển thị
        animationTimer = new Timer(ANIMATION_DELAY, e -> {
            frame++;
            
            // Cập nhật animation
            for (int i = 0; i < 4; i++) {
                rotationAngles[i] += rotationSpeed[i];
                
                // Giảm dần tốc độ quay theo thời gian
                if (frame > 15) {
                    rotationSpeed[i] *= 0.93;
                }
                
                // Random giá trị mới cho xúc xắc trong khi quay
                if (frame < ANIMATION_FRAMES - 15) {
                    diceValues[i] = random.nextInt(6) + 1;
                }
                
                // Khi sắp kết thúc, cố định giá trị cuối cùng
                if (frame == ANIMATION_FRAMES - 15) {
                    diceValues[i] = random.nextInt(6) + 1;
                }
            }
            
            // Kết thúc animation sau số frame quy định
            if (frame >= ANIMATION_FRAMES) {
                animationComplete = true;
                animationTimer.stop();
                
                // Tự động đóng dialog sau khi hiển thị kết quả một khoảng thời gian ngắn
                Timer autoCloseTimer = new Timer(1000, event -> {
                    dispose();
                    if (listener != null) {
                        int total = diceValues[0] + diceValues[1] + diceValues[2] + diceValues[3];
                        listener.onDiceRolled(diceValues, total);
                    }
                });
                autoCloseTimer.setRepeats(false);
                autoCloseTimer.start();
            }
            
            repaint();
        });
        
        // Tự động bắt đầu animation khi dialog được hiển thị
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                animationTimer.start();
                
                // Phát âm thanh xúc xắc lắc
                playSound("dice_roll.wav");
            }
        });
    }
    
    /**
     * Vẽ một viên xúc xắc 3D với hiệu ứng đổ bóng và perspective.
     * 
     * @param g2d Graphics context
     * @param index Index của viên xúc xắc
     */
    private void drawDice3D(Graphics2D g2d, int index) {
        int x = positions[index].x;
        int y = positions[index].y;
        double angle = rotationAngles[index];
        int value = diceValues[index];
        double scale = scaleFactors[index];
        
        // Tạo transform cho hiệu ứng 3D
        AffineTransform oldTransform = g2d.getTransform();
        
        // Translate để viên xúc xắc xoay quanh tâm
        g2d.translate(x, y);
        
        // Các hiệu ứng phụ thuộc vào frame animation
        double bounceHeight = 0;
        double rotation = angle;
        
        // Enhanced animation with multiple effects
        if (frame < ANIMATION_FRAMES * 0.3) {
            // Initial bounce and heavy rotation
            bounceHeight = Math.sin(frame / (ANIMATION_FRAMES * 0.3) * Math.PI) * 40;
            rotation = angle * 1.5;
        } else if (frame < ANIMATION_FRAMES * 0.7) {
            // Middle smaller bounces
            bounceHeight = Math.sin((frame - ANIMATION_FRAMES * 0.3) / (ANIMATION_FRAMES * 0.4) * Math.PI * 2) * 20;
        } else {
            // Final settle with small bounces
            bounceHeight = Math.sin((frame - ANIMATION_FRAMES * 0.7) / (ANIMATION_FRAMES * 0.3) * Math.PI) * 10;
        }
        
        // Nhảy lên xuống
        g2d.translate(0, -bounceHeight);
        
        // Xoay với hiệu ứng 3D thực tế hơn
        g2d.rotate(rotation);
        
        // Thêm xoay 3D theo trục khác
        if (index % 2 == 0) {
            g2d.shear(0, Math.sin(angle * 0.5) * 0.1);
        } else {
            g2d.shear(Math.sin(angle * 0.5) * 0.1, 0);
        }
        
        // Scale to current size with pulse effect
        double pulseScale = scale + Math.sin(frame * 0.2) * 0.05;
        int adjustedSize = (int)(DICE_SIZE * pulseScale);
        
        // Vẽ hình vuông 3D của viên xúc xắc
        drawCube(g2d, adjustedSize, value);
        
        // Restore transform
        g2d.setTransform(oldTransform);
    }
    
    /**
     * Vẽ hình lập phương 3D cho viên xúc xắc với các chấm.
     * 
     * @param g2d Graphics context
     * @param size Kích thước của viên xúc xắc
     * @param value Giá trị hiển thị (1-6)
     */
    private void drawCube(Graphics2D g2d, int size, int value) {
        // Nửa kích thước
        int halfSize = size / 2;
        
        // Cải thiện rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Vẽ bề mặt chính của viên xúc xắc (mặt trước)
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(-halfSize, -halfSize, size, size, size/5, size/5);
        
        // Vẽ các cạnh viền
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(-halfSize, -halfSize, size, size, size/5, size/5);
        
        // Vẽ hiệu ứng đổ bóng bên trong
        GradientPaint shadowPaint = new GradientPaint(
            -halfSize, -halfSize, new Color(255, 255, 255, 100),
            halfSize, halfSize, new Color(100, 100, 100, 100));
        g2d.setPaint(shadowPaint);
        g2d.fillRoundRect(-halfSize + 2, -halfSize + 2, size - 4, size - 4, size/6, size/6);
        
        // Vẽ các chấm tương ứng với giá trị
        g2d.setColor(Color.BLACK);
        int dotSize = DOT_SIZE;
        
        switch (value) {
            case 1:
                drawDot(g2d, 0, 0, dotSize);
                break;
            case 2:
                drawDot(g2d, -halfSize/2, -halfSize/2, dotSize);
                drawDot(g2d, halfSize/2, halfSize/2, dotSize);
                break;
            case 3:
                drawDot(g2d, -halfSize/2, -halfSize/2, dotSize);
                drawDot(g2d, 0, 0, dotSize);
                drawDot(g2d, halfSize/2, halfSize/2, dotSize);
                break;
            case 4:
                drawDot(g2d, -halfSize/2, -halfSize/2, dotSize);
                drawDot(g2d, -halfSize/2, halfSize/2, dotSize);
                drawDot(g2d, halfSize/2, -halfSize/2, dotSize);
                drawDot(g2d, halfSize/2, halfSize/2, dotSize);
                break;
            case 5:
                drawDot(g2d, -halfSize/2, -halfSize/2, dotSize);
                drawDot(g2d, -halfSize/2, halfSize/2, dotSize);
                drawDot(g2d, 0, 0, dotSize);
                drawDot(g2d, halfSize/2, -halfSize/2, dotSize);
                drawDot(g2d, halfSize/2, halfSize/2, dotSize);
                break;
            case 6:
                drawDot(g2d, -halfSize/2, -halfSize/2, dotSize);
                drawDot(g2d, -halfSize/2, 0, dotSize);
                drawDot(g2d, -halfSize/2, halfSize/2, dotSize);
                drawDot(g2d, halfSize/2, -halfSize/2, dotSize);
                drawDot(g2d, halfSize/2, 0, dotSize);
                drawDot(g2d, halfSize/2, halfSize/2, dotSize);
                break;
        }
        
        // Vẽ highlight để tạo hiệu ứng 3D
        g2d.setColor(new Color(255, 255, 255, 120));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine(-halfSize + 5, -halfSize + 5, halfSize - 5, -halfSize + 5);
        g2d.drawLine(-halfSize + 5, -halfSize + 5, -halfSize + 5, halfSize - 5);
    }
    
    /**
     * Vẽ một chấm tròn trên viên xúc xắc.
     */
    private void drawDot(Graphics2D g2d, int x, int y, int size) {
        // Vẽ chấm với gradient và đổ bóng để trông 3D hơn
        RadialGradientPaint dotPaint = new RadialGradientPaint(
            new Point(x, y),
            size,
            new float[] {0.2f, 1.0f},
            new Color[] {
                new Color(80, 80, 80),
                Color.BLACK
            }
        );
        
        g2d.setPaint(dotPaint);
        g2d.fillOval(x - size/2, y - size/2, size, size);
        
        // Vẽ highlight nhỏ để tạo cảm giác 3D
        g2d.setColor(new Color(200, 200, 200, 150));
        g2d.fillOval(x - size/4, y - size/4, size/3, size/3);
    }
    
    /**
     * Phát âm thanh xúc xắc lắc.
     */
    private void playSound(String soundFile) {
        try {
            // Check for sound file in resources
            java.net.URL soundURL = getClass().getResource("/sounds/" + soundFile);
            if (soundURL != null) {
                javax.sound.sampled.AudioInputStream audioIn = 
                    javax.sound.sampled.AudioSystem.getAudioInputStream(soundURL);
                javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            }
        } catch (Exception e) {
            // Silently fail if sound can't be played
        }
    }
    
    /**
     * Hiển thị dialog và quay xúc xắc.
     */
    public static void showDiceRoll(JFrame parent, DiceRollListener listener) {
        DicePopupDialog dialog = new DicePopupDialog(parent, listener);
        dialog.setVisible(true);
    }
} 