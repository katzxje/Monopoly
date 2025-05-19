import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

/**
 * Hộp thoại cài đặt cho phép người dùng điều chỉnh tỷ lệ hiển thị và các tùy chọn khác.
 */
public class SettingsDialog extends JDialog {
    private JSlider scaleSlider;
    private JLabel scaleValueLabel;
    private JCheckBox simplifiedUICheckbox;
    private boolean settingsChanged = false;
    
    /**
     * Tạo hộp thoại cài đặt mới
     * 
     * @param parent Frame cha
     */
    public SettingsDialog(Frame parent) {
        super(parent, "Cài đặt hiển thị", true); // Modal dialog
        
        // Lấy cài đặt hiện tại
        UserSettings settings = UserSettings.getInstance();
        double currentScale = settings.getScaleFactor();
        boolean simplifiedUI = settings.isSimplifiedUI();
        
        // Tạo panel chính với viền đệm
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Tạo tiêu đề
        JLabel titleLabel = new JLabel("Tùy chỉnh hiển thị game Monopoly");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        
        // Thêm một dòng giải thích
        JLabel infoLabel = new JLabel("Điều chỉnh tỷ lệ hiển thị để phù hợp với màn hình của bạn");
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(infoLabel);
        
        // Tạo panel cho thanh trượt tỷ lệ
        JPanel scalePanel = new JPanel(new BorderLayout(10, 0));
        scalePanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        // Nhãn thanh trượt
        JLabel scaleLabel = new JLabel("Tỷ lệ hiển thị:");
        scalePanel.add(scaleLabel, BorderLayout.WEST);
        
        // Tạo thanh trượt với giá trị từ 50% đến 200%
        scaleSlider = new JSlider(JSlider.HORIZONTAL, 50, 200, (int)(currentScale * 100));
        scaleSlider.setMajorTickSpacing(50);
        scaleSlider.setMinorTickSpacing(10);
        scaleSlider.setPaintTicks(true);
        scaleSlider.setPaintLabels(true);
        scalePanel.add(scaleSlider, BorderLayout.CENTER);
        
        // Hiển thị giá trị số
        DecimalFormat df = new DecimalFormat("#.##");
        scaleValueLabel = new JLabel(df.format(currentScale) + "x");
        scalePanel.add(scaleValueLabel, BorderLayout.EAST);
        
        // Cập nhật nhãn khi thanh trượt thay đổi
        scaleSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double value = scaleSlider.getValue() / 100.0;
                scaleValueLabel.setText(df.format(value) + "x");
            }
        });
        
        mainPanel.add(scalePanel);
        
        // Thêm tùy chọn Giao diện đơn giản
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JPanel uiPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        simplifiedUICheckbox = new JCheckBox("Sử dụng giao diện đơn giản hóa", simplifiedUI);
        simplifiedUICheckbox.setToolTipText("Giao diện đơn giản hơn, phù hợp cho màn hình nhỏ");
        uiPanel.add(simplifiedUICheckbox);
        
        mainPanel.add(uiPanel);
        
        // Thêm cảnh báo cho màn hình nhỏ
        if (GameUtils.isSmallScreenDetected()) {
            JPanel warningPanel = new JPanel();
            warningPanel.setLayout(new BoxLayout(warningPanel, BoxLayout.Y_AXIS));
            warningPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 0, 10, 0),
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 150, 0), 1),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                )
            ));
            
            JLabel warningLabel = new JLabel("Phát hiện màn hình độ phân giải thấp!");
            warningLabel.setForeground(new Color(180, 0, 0));
            warningLabel.setFont(new Font("Arial", Font.BOLD, 14));
            warningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel warningTextLabel = new JLabel(
                "<html>Để có trải nghiệm tốt nhất, bạn nên:<br>" +
                "- Giảm tỷ lệ hiển thị (đề xuất: 0.7x)<br>" + 
                "- Chọn giao diện đơn giản hóa<br>" +
                "Các thay đổi sẽ có hiệu lực ở lần khởi động tiếp theo.</html>");
            warningTextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            warningPanel.add(warningLabel);
            warningPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            warningPanel.add(warningTextLabel);
            
            mainPanel.add(warningPanel);
        }
        
        // Thêm nút Áp dụng và Hủy bỏ
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JPanel buttonPanel = new JPanel();
        JButton resetButton = new JButton("Khôi phục mặc định");
        JButton applyButton = new JButton("Áp dụng");
        JButton cancelButton = new JButton("Hủy bỏ");
        
        // Xử lý sự kiện nút
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scaleSlider.setValue(100); // Đặt lại 1.0x
                simplifiedUICheckbox.setSelected(false);
            }
        });
        
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applySettings();
                settingsChanged = true;
                dispose();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        buttonPanel.add(resetButton);
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel);
        
        // Hoàn thiện hộp thoại
        setContentPane(mainPanel);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }
    
    /**
     * Áp dụng cài đặt mới
     */
    private void applySettings() {
        double newScale = scaleSlider.getValue() / 100.0;
        boolean simplified = simplifiedUICheckbox.isSelected();
        
        UserSettings settings = UserSettings.getInstance();
        settings.setScaleFactor(newScale);
        settings.setSimplifiedUI(simplified);
        
        // Cập nhật tỷ lệ trong GameUtils
        GameUtils.setScaleFactor(newScale);
        
        // Hiển thị thông báo cần khởi động lại
        JOptionPane.showMessageDialog(this,
                "Cài đặt đã được lưu.\nMột số thay đổi sẽ có hiệu lực sau khi khởi động lại game.",
                "Cài đặt đã lưu",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Kiểm tra xem cài đặt có được thay đổi không
     * 
     * @return true nếu cài đặt đã thay đổi
     */
    public boolean isSettingsChanged() {
        return settingsChanged;
    }
    
    /**
     * Hiển thị hộp thoại cài đặt
     * 
     * @param parent Frame cha
     * @return true nếu cài đặt đã thay đổi
     */
    public static boolean showDialog(Frame parent) {
        SettingsDialog dialog = new SettingsDialog(parent);
        dialog.setVisible(true);
        return dialog.isSettingsChanged();
    }
} 