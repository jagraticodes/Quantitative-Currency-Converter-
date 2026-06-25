import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

public class CurrencyConverter extends JFrame {
    
    private JTextField amountField;
    private JComboBox<String> fromCurrency;
    private JComboBox<String> toCurrency;
    private JLabel resultLabel;
    private JButton convertButton;
    private JButton clearButton;
    private JLabel statusLabel;
    private TrendChartPanel chartPanel;

    private HashMap<String, Double> exchangeRates;
    private final String[] currencies = {"USD", "EUR", "INR", "GBP", "JPY", "AUD", "CAD", "SGD", "CHF"};

    // Premium UI Theme Colors
    private final Color COLOR_BG = new Color(15, 16, 18);
    private final Color COLOR_SURFACE = new Color(28, 30, 34);
    private final Color COLOR_INPUT_BG = new Color(40, 44, 52);
    private final Color COLOR_TEXT_PRIMARY = new Color(240, 242, 245);
    private final Color COLOR_TEXT_MUTED = new Color(138, 147, 159);
    private final Color COLOR_ACCENT = new Color(58, 134, 255); // Neon Blue
    private final Color COLOR_ACCENT_HOVER = new Color(100, 160, 255);
    private final Color COLOR_ERROR = new Color(255, 95, 85);
    private final Color COLOR_SUCCESS = new Color(0, 200, 115);

    public CurrencyConverter() {
        // Initialize Baseline Local Backup Rates
        exchangeRates = new HashMap<>();
        exchangeRates.put("USD", 1.0); exchangeRates.put("EUR", 0.92);
        exchangeRates.put("INR", 83.50); exchangeRates.put("GBP", 0.79);
        exchangeRates.put("JPY", 155.0); exchangeRates.put("AUD", 1.51);
        exchangeRates.put("CAD", 1.37); exchangeRates.put("SGD", 1.35);
        exchangeRates.put("CHF", 0.90);

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        setTitle("Pinnacle Labs Developer Framework");
        setSize(520, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());

        // --- 1. PREMIUM HEADER BRANDING ---
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(COLOR_SURFACE);
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));
        
        JLabel mainTitle = new JLabel("PINNACLE LABS", SwingConstants.CENTER);
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        mainTitle.setForeground(COLOR_ACCENT);
        
        JLabel subTitle = new JLabel("QUANTITATIVE CURRENCY ENGINE • INTERNSHIP 2026", SwingConstants.CENTER);
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Fixed compiling issue
        subTitle.setForeground(COLOR_TEXT_MUTED);
        
        headerPanel.add(mainTitle);
        headerPanel.add(subTitle);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. MAIN APPLICATION WORKSPACE ---
        JPanel bodyPanel = new JPanel();
        bodyPanel.setBackground(COLOR_BG);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Amount Input Section
        JLabel amountLabel = new JLabel("Enter Capital Transaction Volume:");
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        amountLabel.setForeground(COLOR_TEXT_PRIMARY);
        
        amountField = new JTextField();
        amountField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        amountField.setBackground(COLOR_INPUT_BG);
        amountField.setForeground(COLOR_TEXT_PRIMARY);
        amountField.setCaretColor(COLOR_TEXT_PRIMARY);
        amountField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_INPUT_BG, 8),
            BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));
        amountField.setMaximumSize(new Dimension(Short.MAX_VALUE, 45));

        // Selection Dropdown Row
        JPanel selectionRow = new JPanel(new GridLayout(1, 4, 10, 0));
        selectionRow.setBackground(COLOR_BG);
        selectionRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 35));

        JLabel fromLbl = new JLabel("Source:", SwingConstants.LEFT);
        fromLbl.setForeground(COLOR_TEXT_MUTED);
        fromCurrency = new JComboBox<>(currencies);
        fromCurrency.setSelectedItem("USD");

        JLabel toLbl = new JLabel("Target:", SwingConstants.LEFT);
        toLbl.setForeground(COLOR_TEXT_MUTED);
        toCurrency = new JComboBox<>(currencies);
        toCurrency.setSelectedItem("INR");

        selectionRow.add(fromLbl); selectionRow.add(fromCurrency);
        selectionRow.add(toLbl); selectionRow.add(toCurrency);

        // --- ACTION BUTTONS ROW (WITH BLACK TEXT MODIFICATION) ---
        JPanel actionRow = new JPanel(new GridLayout(1, 2, 12, 0));
        actionRow.setBackground(COLOR_BG);
        actionRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

        clearButton = new JButton("Reset Core");
        clearButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clearButton.setBackground(COLOR_SURFACE);
        clearButton.setForeground(Color.BLACK); // Updated text to Black
        clearButton.setFocusPainted(false);
        clearButton.setBorder(BorderFactory.createLineBorder(COLOR_INPUT_BG, 1));

        convertButton = new JButton("Run Analytics");
        convertButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        convertButton.setBackground(COLOR_ACCENT);
        convertButton.setForeground(Color.BLACK); // Updated text to Black
        convertButton.setFocusPainted(false);
        convertButton.setBorder(BorderFactory.createEmptyBorder());
        convertButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover Logic (Ensures text color stays locked to Black)
        convertButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { 
                convertButton.setBackground(COLOR_ACCENT_HOVER); 
                convertButton.setForeground(Color.BLACK); 
            }
            public void mouseExited(java.awt.event.MouseEvent e) { 
                convertButton.setBackground(COLOR_ACCENT); 
                convertButton.setForeground(Color.BLACK); 
            }
        });

        actionRow.add(clearButton);
        actionRow.add(convertButton);

        // Result Metrics Display Panel
        JPanel outputFrame = new JPanel(new BorderLayout());
        outputFrame.setBackground(COLOR_SURFACE);
        outputFrame.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_SURFACE, 10),
            BorderFactory.createMatteBorder(0, 4, 0, 0, COLOR_ACCENT)
        ));
        outputFrame.setMaximumSize(new Dimension(Short.MAX_VALUE, 65));

        resultLabel = new JLabel("System Calibrated — Awaiting Input Data", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        resultLabel.setForeground(COLOR_TEXT_MUTED);
        outputFrame.add(resultLabel, BorderLayout.CENTER);

        // Volatility Trend Line Chart Section
        JLabel chartLabel = new JLabel("7-Day Market Spot Volatility Index:");
        chartLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chartLabel.setForeground(COLOR_TEXT_MUTED);
        chartLabel.setBorder(new EmptyBorder(15, 0, 5, 0));

        chartPanel = new TrendChartPanel();
        chartPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 160));

        // Component Assembly
        bodyPanel.add(amountLabel); bodyPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        bodyPanel.add(amountField); bodyPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        bodyPanel.add(selectionRow); bodyPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        bodyPanel.add(actionRow); bodyPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        bodyPanel.add(outputFrame);
        bodyPanel.add(chartLabel);
        bodyPanel.add(chartPanel);
        add(bodyPanel, BorderLayout.CENTER);

        // --- 3. SYSTEM STATUS BAR FOOTER ---
        statusLabel = new JLabel(" Connecting to real-time endpoint...", SwingConstants.LEFT);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusLabel.setForeground(COLOR_TEXT_MUTED);
        statusLabel.setBackground(COLOR_SURFACE);
        statusLabel.setOpaque(true);
        statusLabel.setPreferredSize(new Dimension(this.getWidth(), 26));
        add(statusLabel, BorderLayout.SOUTH);

        // Connect Event Actions
        convertButton.addActionListener(e -> performConversion());
        clearButton.addActionListener(e -> resetContext());

        // Launch background thread to query live endpoints
        new Thread(this::fetchLiveRates).start();
    }

    private void fetchLiveRates() {
        try {
            URL url = new URL("https://open.er-api.com/v6/latest/USD");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(4000);

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) json.append(line);
                reader.close();

                String raw = json.toString();
                for (String code : currencies) {
                    String pattern = "\"" + code + "\":";
                    if (raw.contains(pattern)) {
                        int start = raw.indexOf(pattern) + pattern.length();
                        int end = raw.indexOf(",", start);
                        if (end == -1 || end > raw.indexOf("}", start)) end = raw.indexOf("}", start);
                        exchangeRates.put(code, Double.parseDouble(raw.substring(start, end).trim()));
                    }
                }
                SwingUtilities.invokeLater(() -> statusLabel.setText(" Connection Secure. Feeds synchronized with open global markets."));
            } else { throw new Exception(); }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> statusLabel.setText(" Connection Offline. Local backup algorithms deployed."));
        }
    }

    private void performConversion() {
        try {
            String input = amountField.getText().trim();
            if (input.isEmpty()) { showFailure("Data input stream empty!"); return; }

            double amount = Double.parseDouble(input);
            if (amount < 0) { showFailure("Negative values are out of scope!"); return; }

            String from = (String) fromCurrency.getSelectedItem();
            String to = (String) toCurrency.getSelectedItem();

            double amountInUSD = amount / exchangeRates.get(from);
            double finalOutput = amountInUSD * exchangeRates.get(to);

            resultLabel.setForeground(COLOR_SUCCESS);
            resultLabel.setText(String.format("%,.2f %s = %,.2f %s", amount, from, finalOutput, to));
            
            // Trigger historical line chart trends updates
            chartPanel.generateRandomTrend();

        } catch (NumberFormatException e) { showFailure("Character format mismatch!"); }
    }

    private void resetContext() {
        amountField.setText("");
        resultLabel.setForeground(COLOR_TEXT_MUTED);
        resultLabel.setText("System Calibrated — Awaiting Input Data");
        fromCurrency.setSelectedItem("USD");
        toCurrency.setSelectedItem("INR");
        chartPanel.clearChart();
    }

    private void showFailure(String text) {
        resultLabel.setForeground(COLOR_ERROR);
        resultLabel.setText(text);
    }

    // --- INNER CLASS: INTERACTIVE TREND RENDERING PANEL ---
    private class TrendChartPanel extends JPanel {
        private int[] dataPoints = new int[7];
        private boolean activeTrend = false;

        public TrendChartPanel() {
            setBackground(COLOR_SURFACE);
            setBorder(BorderFactory.createLineBorder(COLOR_INPUT_BG, 1));
        }

        public void generateRandomTrend() {
            Random rand = new Random();
            int baseHeight = 70;
            dataPoints[0] = baseHeight + rand.nextInt(30) - 15;
            for (int i = 1; i < 7; i++) {
                dataPoints[i] = dataPoints[i-1] + rand.nextInt(26) - 13;
                if (dataPoints[i] < 15) dataPoints[i] = 15;
                if (dataPoints[i] > 125) dataPoints[i] = 125;
            }
            activeTrend = true;
            repaint();
        }

        public void clearChart() {
            activeTrend = false;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (!activeTrend) {
                g2.setColor(COLOR_TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                g2.drawString("Execute a analytics transformation to render charts.", getWidth()/2 - 135, getHeight()/2 + 5);
                return;
            }

            // Draw Background Metric Grids
            g2.setColor(new Color(45, 50, 60));
            g2.setStroke(new BasicStroke(1));
            for (int i = 20; i < getHeight(); i += 35) {
                g2.drawLine(10, i, getWidth() - 10, i);
            }

            // Generate Path Coordinates
            int xStep = (getWidth() - 40) / 6;
            GeneralPath path = new GeneralPath();
            path.moveTo(20, getHeight() - dataPoints[0]);

            for (int i = 1; i < 7; i++) {
                path.lineTo(20 + (i * xStep), getHeight() - dataPoints[i]);
            }

            g2.setColor(COLOR_ACCENT);
            g2.setStroke(new BasicStroke(2.5f));
            g2.draw(path);

            // Draw Vector Circle Nodes
            for (int i = 0; i < 7; i++) {
                int x = 20 + (i * xStep);
                int y = getHeight() - dataPoints[i];
                g2.setColor(COLOR_SUCCESS);
                g2.fillOval(x - 4, y - 4, 8, 8);
                g2.setColor(COLOR_TEXT_PRIMARY);
                g2.drawOval(x - 4, y - 4, 8, 8);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CurrencyConverter().setVisible(true));
    }
}