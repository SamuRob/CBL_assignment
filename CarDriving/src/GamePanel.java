import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

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

    private int laneMoved = 0; // track how much road scroleld

    private JButton startButton;

    private BufferedImage roadImage;

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

        setFocusable(true);
        requestFocusInWindow();

        setDoubleBuffered(true); //BufferedImage to prevent lag when scrolling screen
        this.roadImage = new BufferedImage(roadWidth, roadHeight, BufferedImage.TYPE_INT_ARGB);
        drawRoadImage();

        // Timer for scrolling effect
        gameTimer = new javax.swing.Timer(30, e -> {
            if (GameRunning) {
                scrollScreen();
                repaint();
            }
        });
    }

    private void drawRoadImage() {
        Graphics g = roadImage.getGraphics();
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, roadWidth, roadHeight);
    
        g.setColor(Color.WHITE);
        int laneHeight = roadHeight / maxLane;
        for (int i = 1; i < maxLane; i++) {
            int laneY = i * laneHeight;
            for (int x = 0; x <= roadWidth; x += 40) {
                g.drawLine(x, laneY, x + 20, laneY);  // Draw dashes
            }
        }
        g.dispose();  // Clean up resources
    }

    // Start the game after clicking the start button
    public void startGame() {
        gameState = GAME_SCREEN;  // Switch to game screen state
        GameRunning = true;
        truckX = windowWidth / 2 - 40;  // Reset truck position
        currentLane = 1;
        
        remove(startButton);  // Remove the Start button after the game starts
        startButton.setFocusable(false);// prevent start button from focus
        repaint();  // Repaint the screen without the Start screen
               
        gameTimer.start();  // Start the game timer
        
        requestFocusInWindow(); // ensure focus is on game panel after game begins
        
        System.out.println("Is GamePanel Focused: " + isFocusOwner());


        //THE HOLY GRAIL WHICH MAKES THE CAR MOVE DO NOT DELETE PLEASE PLEASE PLEASE
        ((GameFrame) SwingUtilities.getWindowAncestor(this)).setGameStarted(true);
        //Notify GameFrame that game started
    
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
        
        laneMoved = scrollSpeed + laneMoved;
        if (laneMoved >= roadHeight / maxLane) {
            laneMoved = 0;
        }
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
        int roadX = (windowWidth - roadWidth) / 2;
        int roadY = (windowHeight / 2) - (roadHeight / 2);
    
        // Draw the pre-rendered road, moving from right to left
        g.drawImage(roadImage, roadX - laneMoved, roadY, this);
        if (laneMoved > 0) {
            g.drawImage(roadImage, roadX + roadWidth - laneMoved, roadY, this);
        }
    }
    
    
    

    // Handle vehicle movement based on key presses
    public void moveVehicle(int keyCode) {
        if (gameState != GAME_SCREEN) {
            return;
        } // Ignore key events if the game hasn't started
        
        //Up and down movement
        int laneHeight = roadHeight / maxLane;
        if (keyCode == KeyEvent.VK_UP && currentLane > 1) {
            currentLane--;
            System.out.println("Key pressed" + keyCode);
        } else if (keyCode == KeyEvent.VK_DOWN && currentLane < maxLane) {
            currentLane++;
        }
        truckY = ((windowHeight / 2) - (roadHeight / 2)) + (laneHeight * (currentLane - 1));
        
        //Left to right movement
        if (keyCode == KeyEvent.VK_LEFT && truckX > 0) {
            truckX = truckX - 10;
        } else if (keyCode == KeyEvent.VK_RIGHT && truckX < windowWidth -80) {
            truckX = truckX + 10;
        }
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
