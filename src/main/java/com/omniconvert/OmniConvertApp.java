package com.omniconvert;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OmniConvertApp extends JFrame {

    private UniversalConverter converter = new UniversalConverter();
    private List<File> selectedFiles = new ArrayList<>();
    private boolean isExpanded = false;
    private JLabel lblFileCount;
    private JButton btnToggle;
    private JPanel fileListPanel;
    private JScrollPane scrollPane;
    private JComboBox<String> comboFormat;
    private GradientButton btnConvert;
    private JLabel lblStatus;

    public OmniConvertApp() {
        setTitle("OmniConvert Pro");
        setSize(480, 520);
        setMinimumSize(new Dimension(420, 500));
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        JLabel titleLabel = new JLabel("OmniConvert Pro");
        titleLabel.setFont(new Font("Segoe UI Black", Font.ITALIC, 28));
        titleLabel.setForeground(new Color(59, 130, 246));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JButton btnSelect = new JButton("Выбрать файлы");
        btnSelect.setFont(new Font("Segoe UI Black", Font.BOLD, 14));
        btnSelect.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSelect.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSelect.addActionListener(e -> chooseFiles());
        mainPanel.add(btnSelect);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel accordionHeader = new JPanel(new BorderLayout());
        accordionHeader.setOpaque(false);
        accordionHeader.setMaximumSize(new Dimension(800, 30));

        lblFileCount = new JLabel("Перетащите файлы сюда");
        lblFileCount.setForeground(Color.GRAY);
        lblFileCount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        accordionHeader.add(lblFileCount, BorderLayout.WEST);

        btnToggle = new JButton("▼ Развернуть");
        btnToggle.setContentAreaFilled(false);
        btnToggle.setBorderPainted(false);
        btnToggle.setForeground(new Color(148, 163, 184));
        btnToggle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggle.setVisible(false);
        btnToggle.addActionListener(e -> toggleFileList());
        accordionHeader.add(btnToggle, BorderLayout.EAST);

        mainPanel.add(accordionHeader);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        fileListPanel = new JPanel();
        fileListPanel.setLayout(new BoxLayout(fileListPanel, BoxLayout.Y_AXIS));
        fileListPanel.setBackground(new Color(30, 41, 59));

        scrollPane = new JScrollPane(fileListPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1, true));
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrollPane.setVisible(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        mainPanel.add(scrollPane);

        mainPanel.add(Box.createVerticalGlue());

        JLabel lblFormatTitle = new JLabel("Целевой формат:");
        lblFormatTitle.setFont(new Font("Segoe UI Black", Font.PLAIN, 14));
        lblFormatTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblFormatTitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        comboFormat = new JComboBox<>(new String[]{"-"});
        comboFormat.setEnabled(false);
        comboFormat.setMaximumSize(new Dimension(200, 35));
        comboFormat.setFont(new Font("Segoe UI", Font.BOLD, 14));
        comboFormat.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(comboFormat);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        btnConvert = new GradientButton("Конвертировать");
        btnConvert.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConvert.setMaximumSize(new Dimension(250, 45));
        btnConvert.setEnabled(false);
        btnConvert.addActionListener(e -> startConversion());
        mainPanel.add(btnConvert);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        lblStatus = new JLabel("");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatus.setForeground(Color.GRAY);
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblStatus);

        add(mainPanel);
        setupDragAndDrop();
    }

    // --- ЛОГИКА ШТОРКИ ---
    private void toggleFileList() {
        if (selectedFiles.isEmpty()) return;

        isExpanded = !isExpanded;
        if (isExpanded) {
            btnToggle.setText("▲ Свернуть");
            scrollPane.setVisible(true);
        } else {
            btnToggle.setText("▼ Развернуть");
            scrollPane.setVisible(false);
        }
        revalidate();
        repaint();
    }

    private void forceCloseAccordion() {
        isExpanded = false;
        btnToggle.setText("▼ Развернуть");
        btnToggle.setVisible(false);
        scrollPane.setVisible(false);
        revalidate();
        repaint();
    }

    // --- ЛОГИКА ФАЙЛОВ ---
    private void chooseFiles() {
        FileDialog fileDialog = new FileDialog(this, "Выберите файлы", FileDialog.LOAD);
        fileDialog.setMultipleMode(true);
        fileDialog.setVisible(true);

        File[] files = fileDialog.getFiles();
        if (files != null && files.length > 0) {
            addFilesToQueue(files);
        }
    }

    private void setupDragAndDrop() {
        new DropTarget(this, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent event) {
                try {
                    event.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    addFilesToQueue(droppedFiles.toArray(new File[0]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void addFilesToQueue(File[] files) {
        for (File f : files) {
            if (!selectedFiles.contains(f)) {
                selectedFiles.add(f);
            }
        }
        refreshUI();
    }

    private void removeFile(File file) {
        selectedFiles.remove(file);
        refreshUI();
    }

    private void refreshUI() {
        fileListPanel.removeAll();
        int count = selectedFiles.size();

        if (count == 0) {
            lblFileCount.setText("Перетащите файлы сюда");
            lblFileCount.setForeground(Color.GRAY);
            forceCloseAccordion();
            comboFormat.setModel(new DefaultComboBoxModel<>(new String[]{"-"}));
            comboFormat.setEnabled(false);
            btnConvert.setEnabled(false);
            lblStatus.setText("");
            return;
        }

        // Обновляем текст заголовка
        if (count == 1) {
            String name = selectedFiles.get(0).getName();
            String displayName = name.length() > 25 ? name.substring(0, 22) + "..." : name;
            lblFileCount.setText("Выбран: " + displayName);
        } else {
            lblFileCount.setText("Выбрано файлов: " + count + " шт.");
        }
        lblFileCount.setForeground(Color.WHITE);
        btnToggle.setVisible(true);

        // Динамическая высота списка (максимум 120px)
        int preferredHeight = Math.min(count * 35, 120);
        scrollPane.setMaximumSize(new Dimension(800, preferredHeight));
        scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().width, preferredHeight));

        // Рисуем плашки файлов
        for (File file : selectedFiles) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(5, 10, 5, 10));

            String name = file.getName();
            String displayName = name.length() > 35 ? name.substring(0, 32) + "..." : name;

            JLabel nameLbl = new JLabel(displayName);
            nameLbl.setForeground(new Color(248, 250, 252));
            row.add(nameLbl, BorderLayout.CENTER);

            // Кнопка удаления (Крестик)
            JButton btnRemove = new JButton("x");
            btnRemove.setContentAreaFilled(false);
            btnRemove.setBorderPainted(false);
            btnRemove.setForeground(new Color(239, 68, 68)); // Красный
            btnRemove.setFont(new Font("Segoe UI Black", Font.BOLD, 14));
            btnRemove.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRemove.setMargin(new Insets(0, 0, 0, 0));
            btnRemove.addActionListener(e -> removeFile(file));

            row.add(btnRemove, BorderLayout.EAST);
            fileListPanel.add(row);
        }

        // Пересечение форматов
        List<String> commonFormats = null;
        for (File f : selectedFiles) {
            String ext = f.getName().substring(f.getName().lastIndexOf("."));
            List<String> formatsForThisFile = converter.getAvailableFormats(ext);

            if (commonFormats == null) {
                commonFormats = new ArrayList<>(formatsForThisFile);
            } else {
                commonFormats.retainAll(formatsForThisFile);
            }
        }

        if (commonFormats != null && !commonFormats.isEmpty()) {
            comboFormat.setModel(new DefaultComboBoxModel<>(commonFormats.toArray(new String[0])));
            comboFormat.setEnabled(true);
            btnConvert.setEnabled(true);
            lblStatus.setText("");
        } else {
            comboFormat.setModel(new DefaultComboBoxModel<>(new String[]{"-"}));
            comboFormat.setEnabled(false);
            btnConvert.setEnabled(false);
            if (count > 1) {
                lblStatus.setText("Конфликт форматов! Разверните и удалите лишние");
                lblStatus.setForeground(new Color(245, 158, 11)); // Оранжевый
            } else {
                lblStatus.setText("Формат не поддерживается");
                lblStatus.setForeground(new Color(239, 68, 68)); // Красный
            }
        }

        // Обновляем отрисовку окна
        revalidate();
        repaint();
    }

    private void startConversion() {
        btnConvert.setEnabled(false);
        comboFormat.setEnabled(false);
        String targetFormat = (String) comboFormat.getSelectedItem();

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            int successCount = 0;
            @Override
            protected Void doInBackground() {
                Map<String, String> labels = Map.ofEntries(
                        Map.entry(".png", "картинка"), Map.entry(".jpg", "картинка"),
                        Map.entry(".mp4", "видео"), Map.entry(".mp3", "аудио"),
                        Map.entry(".gif", "гифка"), Map.entry(".json", "конфиг"),
                        Map.entry(".yaml", "конфиг"), Map.entry(".csv", "таблица")
                );
                String label = labels.getOrDefault(targetFormat.toLowerCase(), "конвертировано");

                // Делаем копию списка, чтобы можно было безопасно итерироваться
                List<File> filesToProcess = new ArrayList<>(selectedFiles);

                for (int i = 0; i < filesToProcess.size(); i++) {
                    publish("Конвертация... " + (i + 1) + " из " + filesToProcess.size());

                    File file = filesToProcess.get(i);
                    String absolutePath = file.getAbsolutePath();
                    int dotIndex = absolutePath.lastIndexOf(".");
                    String outputPath = absolutePath.substring(0, dotIndex) + " (" + label + ")" + targetFormat;

                    try {
                        converter.convert(absolutePath, outputPath);
                        successCount++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                lblStatus.setText(chunks.get(chunks.size() - 1));
                lblStatus.setForeground(new Color(59, 130, 246));
            }

            @Override
            protected void done() {
                btnConvert.setEnabled(true);
                comboFormat.setEnabled(true);

                int total = selectedFiles.size();

                if (successCount == total) {
                    lblStatus.setText("Все файлы успешно сохранены!");
                    lblStatus.setForeground(new Color(16, 185, 129));
                    selectedFiles.clear(); // Автоочистка очереди
                    refreshUI();
                    sendWindowsNotification("Успех! ✨", "Конвертация завершена: " + total + " шт.");
                } else {
                    lblStatus.setText("Готово с ошибками: " + successCount + "/" + total);
                    lblStatus.setForeground(new Color(245, 158, 11));
                    sendWindowsNotification("Завершено ⚠️", "Сконвертировано " + successCount + " из " + total);
                }
            }
        };
        worker.execute();
    }

    private void sendWindowsNotification(String title, String message) {
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
                TrayIcon trayIcon = new TrayIcon(image, "OmniConvert");
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);
                trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
                new Thread(() -> {
                    try { Thread.sleep(5000); tray.remove(trayIcon); } catch (InterruptedException ignored) {}
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        UIManager.put("Button.arc", 15);
        UIManager.put("Component.arc", 15);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));

        SwingUtilities.invokeLater(() -> {
            new OmniConvertApp().setVisible(true);
        });
    }

    // ==========================================
    // КАСТОМНАЯ КНОПКА
    // ==========================================
    class GradientButton extends JButton {
        public GradientButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI Black", Font.PLAIN, 16));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            if (!isEnabled()) {
                g2.setColor(new Color(71, 85, 105));
                g2.fillRoundRect(0, 0, width, height, 20, 20);
            } else {
                GradientPaint gp = new GradientPaint(0, 0, new Color(16, 185, 129), width, height, new Color(5, 150, 105));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, width, height, 20, 20);
            }

            super.paintComponent(g);
            g2.dispose();
        }
    }
}
