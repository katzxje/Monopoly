import java.awt.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class with helper methods for the Monopoly game.
 */
public class GameUtils {
    
    // Base screen resolution for scaling calculations
    private static final int BASE_WIDTH = 1920; 
    private static final int BASE_HEIGHT = 1080;
    
    // Scale factor based on current screen resolution
    private static double scaleFactor = 1.0;
    private static boolean scaleInitialized = false;
    private static boolean smallScreenDetected = false;
    
    // Cache for scaled fonts
    private static Map<String, Font> fontCache = new HashMap<>();
    
    // Currency formatter for displaying money values consistently
    private static NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    
    /**
     * Initializes the scale factor based on current screen resolution.
     * Should be called once at application startup.
     */
    public static void initializeScaling() {
        if (scaleInitialized) return;
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle screenRect = ge.getMaximumWindowBounds();
        
        // Calculate scale factor based on the smaller of width or height ratio
        double widthRatio = screenRect.width / (double) BASE_WIDTH;
        double heightRatio = screenRect.height / (double) BASE_HEIGHT;
        
        // Check if this is a small screen
        if (screenRect.width < 1366 || screenRect.height < 768) {
            smallScreenDetected = true;
        }
        
        // Determine automatic scale factor
        double autoScaleFactor = Math.min(widthRatio, heightRatio);
        
        // Apply minimum and maximum boundaries to avoid extreme scaling
        if (autoScaleFactor < 0.6) autoScaleFactor = 0.6;
        if (autoScaleFactor > 1.5) autoScaleFactor = 1.5;
        
        // Check if user has customized scale factor
        try {
            // Get user's custom scale factor
            UserSettings settings = UserSettings.getInstance();
            scaleFactor = settings.getScaleFactor();
            
            // If the screen is very small and user hasn't changed the scale,
            // automatically suggest a better default
            if (smallScreenDetected && scaleFactor == 1.0) {
                // Suggest the user a better scale factor for small screens
                scaleFactor = Math.max(0.7, autoScaleFactor);
                settings.setScaleFactor(scaleFactor);
            }
        } catch (Exception e) {
            // If UserSettings can't be loaded, use auto scale factor
            System.err.println("Không thể tải cài đặt người dùng: " + e.getMessage());
            scaleFactor = autoScaleFactor;
        }
        
        scaleInitialized = true;
    }
    
    /**
     * Scale a size value by the global scale factor.
     * 
     * @param size Original size
     * @return Scaled size
     */
    public static int scale(int size) {
        if (!scaleInitialized) initializeScaling();
        return (int) Math.round(size * scaleFactor);
    }
    
    /**
     * Scale a dimension by the global scale factor.
     * 
     * @param width Original width
     * @param height Original height
     * @return Scaled dimension
     */
    public static Dimension scaleDimension(int width, int height) {
        return new Dimension(scale(width), scale(height));
    }
    
    /**
     * Get a scaled font based on a base font size.
     * 
     * @param family Font family
     * @param style Font style (e.g., Font.BOLD)
     * @param size Base font size
     * @return Scaled font
     */
    public static Font getScaledFont(String family, int style, int size) {
        String key = family + ":" + style + ":" + size;
        if (fontCache.containsKey(key)) {
            return fontCache.get(key);
        }
        
        Font font = new Font(family, style, scale(size));
        fontCache.put(key, font);
        return font;
    }
    
    /**
     * Scale an existing font.
     * 
     * @param font Original font
     * @return Scaled font
     */
    public static Font scaleFont(Font font) {
        return font.deriveFont((float) scale(font.getSize()));
    }
    
    /**
     * Create a responsive border for components.
     * 
     * @param color Border color
     * @param thickness Base thickness to be scaled
     * @return Scaled border
     */
    public static javax.swing.border.Border createScaledBorder(Color color, int thickness) {
        return BorderFactory.createLineBorder(color, scale(thickness));
    }
    
    /**
     * Create a scaled empty border (padding).
     * 
     * @param top Top padding
     * @param left Left padding
     * @param bottom Bottom padding
     * @param right Right padding
     * @return Scaled empty border
     */
    public static javax.swing.border.Border createScaledEmptyBorder(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(
            scale(top), scale(left), scale(bottom), scale(right)
        );
    }
    
    /**
     * Create a scaled compound border.
     * 
     * @param outer Outer border
     * @param inner Inner border
     * @return Scaled compound border
     */
    public static javax.swing.border.Border createScaledCompoundBorder(
            javax.swing.border.Border outer, 
            javax.swing.border.Border inner) {
        return BorderFactory.createCompoundBorder(outer, inner);
    }
    
    /**
     * Create a scaled titled border.
     * 
     * @param border Base border
     * @param title Title text
     * @param titleJustification Title justification
     * @param titlePosition Title position
     * @param titleFont Base title font
     * @param titleColor Title color
     * @return Scaled titled border
     */
    public static javax.swing.border.TitledBorder createScaledTitledBorder(
            javax.swing.border.Border border, 
            String title, 
            int titleJustification, 
            int titlePosition, 
            Font titleFont, 
            Color titleColor) {
        
        javax.swing.border.TitledBorder titledBorder = BorderFactory.createTitledBorder(
                border, title, titleJustification, titlePosition, titleFont, titleColor);
        
        titledBorder.setTitleFont(scaleFont(titleFont));
        return titledBorder;
    }
    
    /**
     * Get the current scale factor.
     * 
     * @return Current scale factor
     */
    public static double getScaleFactor() {
        if (!scaleInitialized) initializeScaling();
        return scaleFactor;
    }
    
    /**
     * Set a new scale factor and clear cache.
     * 
     * @param newScaleFactor The new scale factor to use
     */
    public static void setScaleFactor(double newScaleFactor) {
        // Set bounds
        if (newScaleFactor < 0.5) newScaleFactor = 0.5;
        if (newScaleFactor > 2.0) newScaleFactor = 2.0;
        
        scaleFactor = newScaleFactor;
        
        // Clear font cache to recalculate with new scale
        fontCache.clear();
        
        // Save to user settings
        try {
            UserSettings.getInstance().setScaleFactor(newScaleFactor);
        } catch (Exception e) {
            System.err.println("Không thể lưu cài đặt tỷ lệ: " + e.getMessage());
        }
    }
    
    /**
     * Returns true if a small screen was detected.
     */
    public static boolean isSmallScreenDetected() {
        if (!scaleInitialized) initializeScaling();
        return smallScreenDetected;
    }
    
    /**
     * Returns true if simplified UI should be used.
     */
    public static boolean useSimplifiedUI() {
        try {
            return UserSettings.getInstance().isSimplifiedUI();
        } catch (Exception e) {
            return smallScreenDetected; // Default to simplified UI on small screens
        }
    }
    
    /**
     * Creates the Monopoly logo image if it doesn't exist.
     */
    public static void createMonopolyLogo() {
        File logoFile = new File("images/monopoly-logo.png");
        if (logoFile.exists()) {
            return; // Logo already exists
        }
        
        // Create directory if it doesn't exist
        logoFile.getParentFile().mkdirs();
        
        // Create a new image for the logo
        int width = 300;
        int height = 100;
        BufferedImage logoImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = logoImage.createGraphics();
        
        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw background (transparent)
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, width, height);
        
        // Create a fancy "MONOPOLY" text
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "MONOPOLY";
        Rectangle2D textBounds = fm.getStringBounds(text, g2d);
        
        // Center the text
        int textX = (int) ((width - textBounds.getWidth()) / 2);
        int textY = (int) ((height - textBounds.getHeight()) / 2 + fm.getAscent());
        
        // Draw text shadow
        g2d.setColor(Color.BLACK);
        g2d.drawString(text, textX + 2, textY + 2);
        
        // Draw main text with gradient
        g2d.setColor(new Color(0, 102, 0)); // Dark green
        g2d.drawString(text, textX, textY);
        
        // Draw border around text
        g2d.setColor(new Color(255, 215, 0)); // Gold
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawRect(5, 5, width - 10, height - 10);
        
        g2d.dispose();
        
        // Save the logo to file
        try {
            ImageIO.write(logoImage, "png", logoFile);
            System.out.println("Created Monopoly logo: " + logoFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save Monopoly logo: " + e.getMessage());
        }
    }
    
    /**
     * Creates a prison image for the jail tile if it doesn't exist.
     */
    public static void createPrisonImage() {
        File prisonFile = new File("images/prision.png");
        if (prisonFile.exists()) {
            return; // Prison image already exists
        }
        
        // Create directory if it doesn't exist
        prisonFile.getParentFile().mkdirs();
        
        // Create a new image for the prison
        int size = 100;
        BufferedImage prisonImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = prisonImage.createGraphics();
        
        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw background - white/light gray
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(0, 0, size, size);
        
        // Draw prison bars
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3f));
        
        // Vertical bars
        int barSpacing = size / 8;
        for (int i = 0; i <= size; i += barSpacing) {
            g2d.drawLine(i, 0, i, size);
        }
        
        // Horizontal bars at top and bottom
        g2d.drawLine(0, 0, size, 0);
        g2d.drawLine(0, size-1, size, size-1);
        
        // Draw person behind bars
        // Draw head
        g2d.setColor(new Color(70, 70, 70));
        int headSize = size / 5;
        g2d.fillOval(size/2 - headSize/2, size/4, headSize, headSize);
        
        // Draw body
        int bodyWidth = size / 6;
        int bodyHeight = size / 3;
        g2d.fillRect(size/2 - bodyWidth/2, size/4 + headSize, bodyWidth, bodyHeight);
        
        // Draw arms
        g2d.setStroke(new BasicStroke(bodyWidth/2));
        g2d.drawLine(size/2 - bodyWidth, size/4 + headSize + bodyHeight/4, 
                    size/2 + bodyWidth, size/4 + headSize + bodyHeight/4);
        
        // Draw "JAIL" text at the bottom
        g2d.setFont(new Font("Arial", Font.BOLD, size/6));
        g2d.setColor(Color.BLACK);
        FontMetrics fm = g2d.getFontMetrics();
        String text = "JAIL";
        g2d.drawString(text, (size - fm.stringWidth(text))/2, size - size/10);
        
        g2d.dispose();
        
        // Save the prison image to file
        try {
            ImageIO.write(prisonImage, "png", prisonFile);
            System.out.println("Created prison image: " + prisonFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save prison image: " + e.getMessage());
        }
    }
    
    /**
     * Creates token images for characters if they don't exist.
     */
    public static void createCharacterTokens() {
        for (String characterName : Character.DEFAULT_CHARACTERS) {
            File tokenFile = new File("images/" + characterName.toLowerCase() + ".png");
            if (tokenFile.exists()) {
                continue; // Token already exists
            }
            
            // Create directory if it doesn't exist
            tokenFile.getParentFile().mkdirs();
            
            // Create a token image
            int size = 40;
            BufferedImage tokenImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = tokenImage.createGraphics();
            
            // Set rendering hints for better quality
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Get color based on character name index
            int index = -1;
            for (int i = 0; i < Character.DEFAULT_CHARACTERS.length; i++) {
                if (Character.DEFAULT_CHARACTERS[i].equals(characterName)) {
                    index = i;
                    break;
                }
            }
            
            Color tokenColor;
            if (index >= 0) {
                // Colors that stand out well
                Color[] colors = {
                    new Color(255, 0, 0),      // Red
                    new Color(0, 0, 255),      // Blue
                    new Color(0, 128, 0),      // Green
                    new Color(255, 165, 0),    // Orange
                    new Color(128, 0, 128),    // Purple
                    new Color(0, 128, 128),    // Teal
                    new Color(139, 69, 19),    // Brown
                    new Color(255, 215, 0)     // Gold
                };
                tokenColor = colors[index % colors.length];
            } else {
                tokenColor = new Color(100, 100, 100); // Gray fallback
            }
            
            // Draw token background
            g2d.setColor(tokenColor);
            g2d.fillOval(0, 0, size - 1, size - 1);
            
            // Draw token border
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawOval(0, 0, size - 1, size - 1);
            
            // Draw character initial
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g2d.getFontMetrics();
            String initial = characterName.substring(0, 1).toUpperCase();
            Rectangle2D textBounds = fm.getStringBounds(initial, g2d);
            
            int textX = (int) ((size - textBounds.getWidth()) / 2);
            int textY = (int) ((size - textBounds.getHeight()) / 2 + fm.getAscent());
            g2d.drawString(initial, textX, textY);
            
            g2d.dispose();
            
            // Save the token to file
            try {
                ImageIO.write(tokenImage, "png", tokenFile);
                System.out.println("Created token image: " + tokenFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Failed to save token image: " + e.getMessage());
            }
        }
    }
    
    /**
     * Creates a red car image for the Car character token if it doesn't exist.
     */
    public static void createRedCarImage() {
        File carFile = new File("images/car.png");
        // Only create a new image if the file doesn't exist
        if (carFile.exists()) {
            System.out.println("Car image already exists at: " + carFile.getAbsolutePath());
            return; // Keep the existing car image that the user has added
        }
        
        // Create directory if it doesn't exist
        carFile.getParentFile().mkdirs();
        
        // The rest of the code will only run if the car.png file doesn't exist
        
        // Create a new image for the car
        int width = 100;
        int height = 50;
        BufferedImage carImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = carImage.createGraphics();
        
        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Draw the car body (red)
        g2d.setColor(new Color(220, 0, 0)); // Bright red color
        
        // Car body outline
        int[] bodyX = {5, 20, 30, 75, 90, 95, 95, 5};
        int[] bodyY = {30, 30, 15, 15, 30, 30, 40, 40};
        g2d.fillPolygon(bodyX, bodyY, 8);
        
        // Add white/silver grill lines on the front
        g2d.setColor(new Color(220, 220, 220)); // Light gray
        for (int i = 0; i < 8; i++) {
            int x = 22 + i * 3;
            g2d.fillRect(x, 20, 2, 10);
        }
        
        // Draw wheels
        g2d.setColor(Color.BLACK);
        g2d.fillOval(15, 35, 20, 20); // Left wheel
        g2d.fillOval(65, 35, 20, 20); // Right wheel
        
        // Wheel rims
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillOval(20, 40, 10, 10); // Left rim
        g2d.fillOval(70, 40, 10, 10); // Right rim
        
        // Steering wheel/cockpit
        g2d.setColor(Color.BLACK);
        g2d.fillRect(45, 15, 3, 10);
        
        // Windshield highlight
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawArc(60, 15, 20, 15, 0, 90);
        
        g2d.dispose();
        
        // Save the car image to file
        try {
            ImageIO.write(carImage, "png", carFile);
            System.out.println("Created car image: " + carFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save car image: " + e.getMessage());
        }
    }
    
    /**
     * Creates a blue dog image for the Dog character token if it doesn't exist.
     */
    public static void createBlueDogImage() {
        File dogFile = new File("images/dog.png");
        // Only create a new image if the file doesn't exist
        if (dogFile.exists()) {
            System.out.println("Dog image already exists at: " + dogFile.getAbsolutePath());
            return; // Keep the existing dog image that the user has added
        }
        
        // Create directory if it doesn't exist
        dogFile.getParentFile().mkdirs();
        
        // The rest of the code will only run if the dog.png file doesn't exist
        
        // Create a new image for the dog
        int width = 80;
        int height = 70;
        BufferedImage dogImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dogImage.createGraphics();
        
        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Colors for the dog
        Color lightBlue = new Color(173, 216, 230);
        Color mediumBlue = new Color(95, 158, 209);
        Color darkBlue = new Color(65, 105, 225);
        
        // Draw the dog body (light blue)
        g2d.setColor(lightBlue);
        
        // Main body
        int[] bodyX = {10, 50, 70, 65, 55, 45, 20, 5};
        int[] bodyY = {30, 30, 45, 55, 60, 55, 55, 40};
        g2d.fillPolygon(bodyX, bodyY, 8);
        
        // Head
        g2d.fillOval(5, 5, 25, 30);
        
        // Tail
        int[] tailX = {65, 75, 70};
        int[] tailY = {40, 35, 45};
        g2d.fillPolygon(tailX, tailY, 3);
        
        // Draw the legs
        g2d.setColor(mediumBlue);
        // Front leg
        g2d.fillRect(15, 50, 8, 15);
        // Back leg
        g2d.fillRect(50, 50, 8, 15);
        
        // Add darker blue details
        g2d.setColor(darkBlue);
        g2d.setStroke(new BasicStroke(1.5f));
        
        // Outline the body
        g2d.drawPolygon(bodyX, bodyY, 8);
        
        // Outline the head
        g2d.drawOval(5, 5, 25, 30);
        
        // Outline the tail
        g2d.drawPolygon(tailX, tailY, 3);
        
        // Outline the legs
        g2d.drawRect(15, 50, 8, 15);
        g2d.drawRect(50, 50, 8, 15);
        
        // Add face details
        g2d.fillOval(10, 15, 4, 4); // Eye
        g2d.drawLine(15, 22, 20, 25); // Mouth
        
        // Add ear
        g2d.drawLine(15, 5, 20, 1);
        
        g2d.dispose();
        
        // Save the dog image to file
        try {
            ImageIO.write(dogImage, "png", dogFile);
            System.out.println("Created dog image: " + dogFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save dog image: " + e.getMessage());
        }
    }
    
    /**
     * Creates a hat image for the Hat character token if it doesn't exist.
     */
    public static void createHatImage() {
        File hatFile = new File("images/hat.png");
        // Only create a new image if the file doesn't exist
        if (hatFile.exists()) {
            System.out.println("Hat image already exists at: " + hatFile.getAbsolutePath());
            return; // Keep the existing hat image that the user has added
        }
        
        // Create directory if it doesn't exist
        hatFile.getParentFile().mkdirs();
        
        // The rest of the code will only run if the hat.png file doesn't exist
        
        // Create a new image for the hat
        int width = 60;
        int height = 40;
        BufferedImage hatImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = hatImage.createGraphics();
        
        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Draw a basic green top hat
        g2d.setColor(new Color(0, 100, 0)); // Dark green
        
        // Hat top
        g2d.fillRect(15, 5, 30, 20);
        
        // Hat brim
        g2d.fillRect(10, 25, 40, 5);
        
        // Hat band
        g2d.setColor(new Color(139, 69, 19)); // Brown
        g2d.fillRect(15, 20, 30, 5);
        
        g2d.dispose();
        
        // Save the hat image to file
        try {
            ImageIO.write(hatImage, "png", hatFile);
            System.out.println("Created hat image: " + hatFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save hat image: " + e.getMessage());
        }
    }
    
    /**
     * Ensures all game images are created.
     */
    public static void ensureGameImages() {
        // Create Monopoly logo if it doesn't exist
        createMonopolyLogo();
        
        // Create prison image if it doesn't exist
        createPrisonImage();
        
        // Create red car image
        createRedCarImage();
        
        // Create blue dog image
        createBlueDogImage();
        
        // Create hat image
        createHatImage();
        
        // Create character tokens if they don't exist
        createCharacterTokens();
    }
    
    /**
     * Loại bỏ nền trắng của hình ảnh, biến nó thành trong suốt.
     * 
     * @param originalImage Hình ảnh gốc cần xử lý
     * @return Hình ảnh mới với nền trong suốt
     */
    public static BufferedImage removeWhiteBackground(Image originalImage) {
        // Chuyển Image thành BufferedImage nếu cần
        BufferedImage bufferedImage;
        if (originalImage instanceof BufferedImage) {
            bufferedImage = (BufferedImage) originalImage;
        } else {
            // Tạo một BufferedImage mới từ Image
            bufferedImage = new BufferedImage(
                originalImage.getWidth(null), 
                originalImage.getHeight(null), 
                BufferedImage.TYPE_INT_ARGB);
            
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.drawImage(originalImage, 0, 0, null);
            g2d.dispose();
        }
        
        // Tạo một BufferedImage mới cho kết quả
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        // Xử lý từng pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = bufferedImage.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                
                // Tính độ sáng của pixel
                float brightness = (red + green + blue) / (3.0f * 255.0f);
                
                // Nếu pixel gần trắng và không hoàn toàn trong suốt
                if (brightness > 0.9f && red > 240 && green > 240 && blue > 240 && alpha > 0) {
                    // Làm cho pixel này trong suốt
                    result.setRGB(x, y, 0); // Hoàn toàn trong suốt
                } else {
                    // Giữ nguyên pixel
                    result.setRGB(x, y, rgb);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Loads an image with fallback support, trying different paths.
     * This method first tries to load from classpath resources,
     * then from direct file path.
     * 
     * @param path Image path (relative to classpath or file system)
     * @return ImageIcon with the loaded image, or with empty image if loading failed
     */
    public static ImageIcon loadImageWithFallback(String path) {
        // Try to ensure the image file exists before loading
        File imageFile = new File(path);
        if (!imageFile.exists()) {
            // If using the boot token but not found, try to check if we should use the
            // monopoly-shoe-512.png instead
            if (path.contains("boot.png")) {
                File alternateFile = new File("images/monopoly-shoe-512.png");
                if (alternateFile.exists()) {
                    // Switch to the monopoly-shoe-512.png file
                    path = "images/monopoly-shoe-512.png";
                }
            }
        }
        
        // First try loading from class resources
        ImageIcon icon = new ImageIcon(GameUtils.class.getResource("/" + path));
        
        // If that didn't work, try direct file path
        if (icon.getIconWidth() <= 0) {
            icon = new ImageIcon(path);
        }
        
        // Special handling for images that need transparent background
        if (icon.getIconWidth() > 0) {
            // Get the image from the icon
            Image originalImage = icon.getImage();
            
            // Remove white background for specific images
            if (path.contains("dog.png") || path.contains("car.png") || path.contains("hat.png")) {
                BufferedImage transparentImage = removeWhiteBackground(originalImage);
                return new ImageIcon(transparentImage);
            }
        }
        
        return icon;
    }
    
    /**
     * Format an amount as currency.
     * 
     * @param amount The amount to format
     * @return A formatted currency string
     */
    public static String formatMoney(int amount) {
        return currencyFormatter.format(amount);
    }
    
    /**
     * Calculate and process rent payment between players.
     * 
     * @param space The property space landed on
     * @param payer The player who needs to pay rent
     * @param logArea The text area for game log updates
     * @return A formatted message about the rent transaction
     */
    public static String processRentPayment(Buyable property, Player payer, JTextArea logArea) {
        StringBuilder message = new StringBuilder();
        
        // Calculate rent based on property type
        int rentAmount = property.calculateRent();
        
        // For utilities, we need the dice roll (this would normally come from GameEngine)
        if (property instanceof UtilitySpace) {
            // This is a simplified version; the actual multiplier is applied in GameEngine
            message.append(payer.getName() + " landed on " + property.getName() + 
                ", owned by " + property.getOwner().getName() + ".\n");
            message.append("Utilities use a rent multiplier based on dice roll.\n");
        } 
        // For regular properties
        else {
            message.append(payer.getName() + " landed on " + property.getName() + 
                ", owned by " + property.getOwner().getName() + ".\n");
            message.append("Rent due: " + formatMoney(rentAmount) + "\n");
            
            // Process payment
            boolean paymentSuccessful = payer.pay(rentAmount);
            
            if (paymentSuccessful) {
                // Transfer money to owner
                property.getOwner().addMoney(rentAmount);
                message.append(payer.getName() + " paid " + formatMoney(rentAmount) + 
                    " to " + property.getOwner().getName() + ".\n");
            } else {
                message.append(payer.getName() + " cannot afford the rent of " + 
                    formatMoney(rentAmount) + "!\n");
                message.append(payer.getName() + " is now bankrupt!\n");
                payer.setBankrupt(true);
            }
        }
        
        // Add message to log if provided
        if (logArea != null) {
            logArea.append(message.toString());
            // Auto-scroll to bottom
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
        
        return message.toString();
    }
    
    /**
     * Resize an image to the specified dimensions.
     * 
     * @param original The original image
     * @param width The target width
     * @param height The target height
     * @return The resized image
     */
    public static BufferedImage resizeImage(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }
    
    /**
     * Create a custom rounded button with appropriate styling.
     * 
     * @param text Button text
     * @param bgColor Button background color
     * @param fgColor Button text color
     * @return A styled JButton
     */
    public static JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setFont(getScaledFont("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fgColor.darker(), scale(2)),
                BorderFactory.createEmptyBorder(scale(5), scale(10), scale(5), scale(10))
        ));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(
                    Math.max(bgColor.getRed() - 20, 0),
                    Math.max(bgColor.getGreen() - 20, 0),
                    Math.max(bgColor.getBlue() - 20, 0)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
} 