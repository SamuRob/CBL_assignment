import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel {
    private javax.swing.Timer gameTimer;
    private int roadWidth;
    private int roadHeight;
    private int truckX, truckY;
    private int currentLane = 1;
    private int maxLane = 4;
    private int scrollSpeed = 5;
    private boolean GameRunning = false;
    private int windowHeight, windowWidth;

    private static final int START_SCREEN = 0;
    private static final int GAME_SCREEN = 1;
    private int gameState = START_SCREEN;

    private JButton startButton;

    public GamePanel() {
        this.roadWidth = 800;
        this.roadHeight = 200;
        this.windowWidth = 800;
        this.windowHeight = 600;
        this.truckX = windowWidth / 2 - 40; // Center initial position of vehicle
        this.truckY = (windowHeight / 2) + 50; // Vertical position of vehicle

        startButton = new JButton("Start Game");
        startButton.setBounds(windowWidth / 2 - 100, windowHeight / 2, 200, 50);
        startButton.addActionListener(e -> startGame());  // Action listener for button
        setLayout(null);  // Set layout to null for absolute positioning
        add(startButton);  // Add start button to the panel

        // Timer for scrolling effect
        gameTimer = new javax.swing.Timer(30, e -> {
            if (GameRunning) {
                scrollScreen();
                repaint();
            }
        });
    }

    // Start the game after clicking the start button
    public void startGame() {
        gameState = GAME_SCREEN;  // Switch to game screen state
        GameRunning = true;
        truckX = windowWidth / 2 - 40;  // Reset truck position
        currentLane = 1;
        remove(startButton);  // Remove the Start button after the game starts
        repaint();  // Repaint the screen without the Start screen
        gameTimer.start();  // Start the game timer
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameState == START_SCREEN) {
            drawStartScreen(g);  // Draw the start screen
        } else if (gameState == GAME_SCREEN) {
            drawRoad(g);  // Draw the road
            drawVehicle(g);  // Draw the vehicle
        }
    }

    private void scrollScreen() {
        scrollSpeed += 1;  // Increase speed gradually
        repaint();
    }

    // Draw the Start screen
    private void drawStartScreen(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, windowWidth, windowHeight);  // Background color

        // Title Text
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Hizmet Delivery Game", windowWidth / 2 - 180, windowHeight / 2 - 100);
    }

    // Draw the Road
    private void drawRoad(Graphics g) {
        g.setColor(Color.GRAY);
        int roadX = (windowWidth - roadWidth) / 2;
        int roadY = (windowHeight / 2) - (roadHeight / 2);
        g.fillRect(roadX, roadY, roadWidth, roadHeight);

        // Lanes
        g.setColor(Color.WHITE);
        int laneHeight = roadHeight / maxLane;
        for (int i = 1; i < maxLane; i++) {
            int laneY = roadY + (i * laneHeight);
            g.drawLine(roadX, laneY, roadX + roadWidth, laneY);
        }
    }

    // Handle vehicle movement based on key presses
    public void moveVehicle(int keyCode) {
        if (gameState != GAME_SCREEN) {
            return;;  // Ignore key events if the game hasn't started

        int laneHeight = roadHeight / maxLane;
        if (keyCode == KeyEvent.VK_UP && currentLane > 1) {
            currentLane--;
        } else if (keyCode == KeyEvent.VK_DOWN && currentLane < maxLane) {
            currentLane++;
        }
        truckY = ((windowHeight / 2) - (roadHeight / 2)) + (laneHeight * (currentLane - 1));
        repaint();
    }

    // Draw the Vehicle (Truck)
    private void drawVehicle(Graphics g) {
        g.setColor(Color.RED);
        int truckWidth = 80;
        int truckHeight = 40;
        g.fillRect(truckX, truckY, truckWidth, truckHeight);
    }
}
