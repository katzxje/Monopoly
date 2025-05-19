import java.io.*;
import java.util.Properties;

/**
 * Lớp quản lý cài đặt người dùng cho game Monopoly.
 * Cho phép lưu và đọc các tùy chọn như tỷ lệ hiển thị.
 */
public class UserSettings {
    private static final String SETTINGS_FILE = "monopoly_settings.properties";
    private static Properties settings = new Properties();
    private static UserSettings instance;
    
    // Giá trị mặc định
    private static final double DEFAULT_SCALE_FACTOR = 1.0;
    private static final boolean DEFAULT_SIMPLIFIED_UI = false;
    
    // Khóa cài đặt
    public static final String KEY_SCALE_FACTOR = "scale_factor";
    public static final String KEY_SIMPLIFIED_UI = "simplified_ui";
    
    private UserSettings() {
        loadSettings();
    }
    
    /**
     * Lấy thể hiện duy nhất của UserSettings (Singleton pattern)
     */
    public static UserSettings getInstance() {
        if (instance == null) {
            instance = new UserSettings();
        }
        return instance;
    }
    
    /**
     * Tải cài đặt từ tệp
     */
    private void loadSettings() {
        File file = new File(SETTINGS_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                settings.load(fis);
                System.out.println("Đã tải cài đặt người dùng");
            } catch (IOException e) {
                System.err.println("Lỗi khi tải cài đặt: " + e.getMessage());
                setDefaultSettings();
            }
        } else {
            setDefaultSettings();
        }
    }
    
    /**
     * Đặt cài đặt mặc định
     */
    private void setDefaultSettings() {
        settings.setProperty(KEY_SCALE_FACTOR, String.valueOf(DEFAULT_SCALE_FACTOR));
        settings.setProperty(KEY_SIMPLIFIED_UI, String.valueOf(DEFAULT_SIMPLIFIED_UI));
        saveSettings();
    }
    
    /**
     * Lưu cài đặt vào tệp
     */
    public void saveSettings() {
        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            settings.store(fos, "Monopoly Game User Settings");
            System.out.println("Đã lưu cài đặt người dùng");
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu cài đặt: " + e.getMessage());
        }
    }
    
    /**
     * Lấy tỷ lệ hiển thị tùy chỉnh
     */
    public double getScaleFactor() {
        try {
            return Double.parseDouble(settings.getProperty(KEY_SCALE_FACTOR, String.valueOf(DEFAULT_SCALE_FACTOR)));
        } catch (NumberFormatException e) {
            return DEFAULT_SCALE_FACTOR;
        }
    }
    
    /**
     * Đặt tỷ lệ hiển thị tùy chỉnh
     */
    public void setScaleFactor(double scaleFactor) {
        // Giới hạn giá trị hợp lý
        if (scaleFactor < 0.5) scaleFactor = 0.5;
        if (scaleFactor > 2.0) scaleFactor = 2.0;
        
        settings.setProperty(KEY_SCALE_FACTOR, String.valueOf(scaleFactor));
        saveSettings();
    }
    
    /**
     * Kiểm tra giao diện đơn giản hóa được bật
     */
    public boolean isSimplifiedUI() {
        return Boolean.parseBoolean(settings.getProperty(KEY_SIMPLIFIED_UI, String.valueOf(DEFAULT_SIMPLIFIED_UI)));
    }
    
    /**
     * Đặt chế độ giao diện đơn giản hóa
     */
    public void setSimplifiedUI(boolean simplified) {
        settings.setProperty(KEY_SIMPLIFIED_UI, String.valueOf(simplified));
        saveSettings();
    }
    
    /**
     * Khởi tạo lại cài đặt về mặc định
     */
    public void resetToDefaults() {
        setDefaultSettings();
    }
} 