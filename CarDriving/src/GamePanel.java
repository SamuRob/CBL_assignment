import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.io.*;
import javax.imageio.*;

public class GamePanel extends JPanel {
    private javax.swing.Timer gameTimer;
    private Timer speedIncreaseTimer; // Timer to increase scroll speed
    private Timer moveRightTimer; // Timer to handle smooth right movement

    private int roadWidth;
    private int roadHeight;
    private int truckX, truckY;
    private int currentLane = 1;
    private int maxLane = 5;
    private int scrollSpeed = 5;
    private int level = 1;

    private boolean GameRunning = false;
    private int windowHeight, windowWidth;
    private int countdown = 3;

    private boolean isImmune = false; // Immunity to vehicle after leaving parking

    private ScorePanel scorePanel;
    private GamePanel gamePanel;

    private boolean collisionDisabled = false;

    private int carWidth;
    private int carHeight;
    private int laneHeight;

    private Obstacles obstacles;
    private int obstacleSpawnCount = 0; // Control spawn rate
    private int roadX;
    private int roadY;

    private boolean gamePaused = false;

    private static final int SHOULDER_WIDTH = 50;

    private ParkingSpot parkingSpot;
    private int parkingSpotSpawnCount = 0;
    private boolean parkSuccess = false;

    private static final int START_SCREEN = 0;
    private static final int GAME_SCREEN = 1;
    private int gameState = START_SCREEN;

    private int laneMoved = 0; // Track how much road scrolled

    private JButton startButton;

    private BufferedImage roadImage;
    private BufferedImage carImage;
    private BufferedImage backgroundImage;

    public GamePanel(ScorePanel scorePanel) {
       // this.scorePanel = scorePanel;

        setFocusable(true);
        requestFocusInWindow();

        this.roadWidth = 800;
        this.roadHeight = 200;
        this.windowWidth = 800;
        this.windowHeight = 600;

        this.laneHeight = roadHeight / maxLane;
        this.carHeight = (int) (laneHeight * 0.8);
        this.carWidth = carHeight * 2;

        this.roadX = (windowWidth - roadWidth) / 2;
        this.roadY = (windowHeight / 2) - (roadHeight / 2);

        // Align the initial truckY to currentLane = 2 (which is visually the second lane)
        this.currentLane = 2;
        this.truckX = windowWidth / 2 - carWidth / 2;
        this.truckY = roadY + (laneHeight * (currentLane - 1)) + (laneHeight - carHeight) / 2;

        startButton = new JButton("Start Game");
        startButton.setBounds(windowWidth / 2 - 100, windowHeight / 2, 200, 50);
        startButton.addActionListener(e -> startGame()); // Action listener for button
        setLayout(null); // Set layout to null for absolute positioning
        add(startButton); // Add start button to the panel

        setFocusable(true);
        requestFocusInWindow();

        setDoubleBuffered(true); // BufferedImage to prevent lag when scrolling screen
        this.roadImage = new BufferedImage(roadWidth, roadHeight, BufferedImage.TYPE_INT_ARGB);
        drawRoadImage();

        try {
            carImage = ImageIO.read(getClass().getResource("/HizmetTruck.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
            backgroundImage = ImageIO.read(getClass().getResource("/BackgroundImage.png"));
        } catch(IOException e) {
            e.printStackTrace();
        }

        // Timer for scrolling effect
        gameTimer = new Timer(30, e -> {
            if (GameRunning) {
                scrollScreen();
                repaint();
            }
        });

        obstacles = new Obstacles(roadWidth, roadX, roadY, roadHeight / maxLane, maxLane, scrollSpeed, this);
        parkingSpot = new ParkingSpot(roadWidth, roadX, roadY, roadHeight / maxLane);

        // Initialize the speed increase timer (e.g., every 30 seconds)
        speedIncreaseTimer = new Timer(10000, e -> {
            scrollSpeed++; // Increase scroll speed
            level++; // Increase the level
            scorePanel.updateLevel(level); // Update the level display
            obstacles.setScrollSpeed(scrollSpeed);
            System.out.println("Level increased: " + level + " | Scroll Speed: " + scrollSpeed);
        });
    }

    public void handleBananaCollision() {
        System.out.println("Hit a banana! Sliding to a neighboring lane...");
    
        int targetLane = currentLane;
    
        // Determine the new lane based on the current lane
        if (currentLane == 2) {
            targetLane = 3;  // If in lane 2, move to lane 3
        } else if (currentLane == 3) {
            // If in lane 3, randomly pick lane 2 or 4
            Random random = new Random();
            targetLane = random.nextBoolean() ? 2 : 4;
        } else if (currentLane == 4) {
            targetLane = 3;  // If in lane 4, move to lane 3
        }
    
        // Move the vehicle to the target lane
        moveToLane(targetLane);
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
                g.drawLine(x, laneY, x + 20, laneY); // Draw dashes
            }
        }
        g.dispose(); // Clean up resources
    }

    public int getMaxLane() {
        return maxLane;
    }

    // Start the game after clicking the start button
    public void startGame() {
        gameState = GAME_SCREEN; // Switch to game screen state
        GameRunning = true; // Set the game as running
        countdown = 3; // Start countdown from 3

        gameTimer.start(); // Start the game timer
        speedIncreaseTimer.start(); // Start the speed increase timer

        // Set the car to start in the middle lane (lane 3)
        currentLane = 3;

        // Update truckY based on currentLane
        truckY = roadY + (laneHeight * (currentLane - 1)) + (laneHeight - carHeight) / 2;
        truckX = windowWidth / 2 - carWidth / 2;

        remove(startButton); // Remove the Start button after the game starts
        startButton.setFocusable(false); // Prevent start button from focus
        repaint(); // Repaint the screen without the Start screen

        // Timer for the countdown
        Timer countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countdown--; // Decrease countdown each second
                repaint(); // Repaint to show the updated countdown

                if (countdown <= 0) {
                    ((Timer) e.getSource()).stop(); // Stop the countdown timer
                    GameRunning = true; // Set the game as running
                    gameTimer.start(); // Start the game timer and the scrolling effect
                    requestFocusInWindow(); // Ensure focus is on game panel after game begins

                    // The holy grail: ensures the game frame knows the game has started
                    ((GameFrame) SwingUtilities.getWindowAncestor(GamePanel.this)).setGameStarted(true);

                    System.out.println("Game started!");
                }
            }
        });

        countdownTimer.start(); // Start the countdown timer
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    
        // Draw the background image
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, -50, windowWidth, windowHeight, null); // Draw background to cover entire window
        }
    
        // Draw other game elements
        if (gameState == START_SCREEN) {
            drawStartScreen(g); // Draw the start screen
        } else if (gameState == GAME_SCREEN) {
            drawRoad(g); // Draw the road
            parkingSpot.drawParkingSpots(g); // Draw parking lanes and spots
            obstacles.drawObstacles(g); // Draw obstacles
            drawVehicle(g); // Draw the vehicle (car)
            drawAnticipationArrow(g); // Draw anticipation arrow
    
            // Draw the countdown on top of the road and other elements
            if (countdown > 0) {
                drawCountdown(g); // Draw the countdown if it's still ongoing
            }
        }
    }
    

    private void drawCountdown(Graphics g) {
        g.setColor(Color.BLACK); // Set the text color
        g.setFont(new Font("Arial", Font.BOLD, 72)); // Set a large font for the countdown

        String countdownText = countdown > 0 ? String.valueOf(countdown) : "GO!"; // Show numbers or "GO!"
        int textWidth = g.getFontMetrics().stringWidth(countdownText); // Measure the width of the text
        int textX = (windowWidth - textWidth) / 2; // Center the text horizontally
        int textY = windowHeight / 2; // Center the text vertically

        g.drawString(countdownText, textX, textY); // Draw the countdown text
    }

    private void drawAnticipationArrow(Graphics g) {
        g.setColor(Color.RED);
        int arrowX = parkingSpot.isNextSpotLeft() ? roadX - 50 : roadX + roadWidth + 10;
        int arrowY = roadY - 30;
        g.fillPolygon(new int[]{arrowX, arrowX + 20, arrowX},
                new int[]{arrowY, arrowY + 10, arrowY + 20}, 3); // Triangle shape for arrow
    }

    private void scrollScreen() {
        if (gamePaused) {
            return; // If the game is paused, do nothing
        }

        // Move parking spots and scroll screen
        parkingSpotSpawnCount++;
        if (parkingSpotSpawnCount >= 500) {
            parkingSpot.generateParkingSpot();
            parkingSpotSpawnCount = 0;
        }
        parkingSpot.moveParkingSpots(scrollSpeed);

        // Lane scrolling logic
        laneMoved = scrollSpeed + laneMoved;
        if (laneMoved >= roadHeight / maxLane) {
            laneMoved = 0;
        }

        // Spawn and move obstacles
        obstacleSpawnCount++;
        if (obstacleSpawnCount >= 100) {
            obstacles.generateObstacle();
            obstacleSpawnCount = 0;
        }
        obstacles.moveObstacles();

        // Check if the player is fully parked, and pause the game if true
        if (parkingSpot.isPlayerParked(truckX, truckY, carWidth, carHeight)) {
            gamePaused = true; // Pause the game
            gameTimer.stop(); // Stop the game timer to stop scrolling
            System.out.println("Game paused. Player parked successfully.");
            return; // Exit the method after pausing
        }

        // Check for collisions with obstacles
        if (obstacles.checkCollision(truckX, truckY, carWidth, carHeight, isImmune)) {
            GameRunning = false;
            gameTimer.stop();
            // Handle game over logic (e.g., restarting the game or ending it)
        }

        repaint(); // Continue scrolling and repaint the screen
    }

    private void restartGame() {
        // Reset game variables
        truckX = windowWidth / 2 - 40;
        truckY = (windowHeight / 2) + 50;
        currentLane = 1;
        laneMoved = 0;
        obstacles = new Obstacles(roadWidth, roadX,
                roadY, roadHeight / maxLane, maxLane, scrollSpeed, this);

        GameRunning = true;
        gameTimer.start();
        requestFocusInWindow(); // Ensure the panel has focus
    }

    // Draw the Start screen
    private void drawStartScreen(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, windowWidth, windowHeight); // Background color

        // Title Text
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Hizmet Delivery Game", windowWidth / 2 - 180, windowHeight / 2 - 100);
    }

    // Draw the Road
    private void drawRoad(Graphics g) {
        int laneHeight = roadHeight / maxLane;

        // Draw lanes 1 to 5
        for (int i = 1; i <= maxLane; i++) {
            int laneY = roadY + (i - 1) * laneHeight;

            // Top and bottom lanes in light grey
            if (i == 1 || i == 5) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(roadX, laneY, roadWidth, laneHeight); // Fill the lane with light grey
            }
            // Middle lanes (2, 3, 4) in dark grey
            else {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(roadX, laneY, roadWidth, laneHeight); // Fill the lane with dark grey

                // Draw dashed lines between lanes 2-3 and 3-4
                if (i == 2 || i == 3) { // Only between lanes 2-3 and 3-4
                    g.setColor(Color.WHITE);
                    for (int x = 0; x <= roadWidth; x += 40) {
                        g.drawLine(x, laneY + laneHeight, x + 20, laneY + laneHeight); // Dashed line
                    }
                }
            }
        }
    }

    private void drawVehicle(Graphics g) {
        if (carImage != null) { // handle if image not found
            g.drawImage(carImage, truckX, truckY, carWidth, carHeight, null); // Draw the car image
        } else {
            // case no image
            g.setColor(Color.RED);
            g.fillRect(truckX, truckY, carWidth, carHeight);
        }
    }
    

    public void moveVehicle(int keyCode) {
        if (gameState != GAME_SCREEN) {
            return;
        }
    
        // Check if the car is within 1 second of the parking spot
        boolean canEnterSpecialLanes = parkingSpot.isParkingSpotApproaching(truckX, scrollSpeed);
    
        // If the car is parked, remove the parking spot and force move to a middle lane
        if (parkingSpot.isPlayerParked(truckX, truckY, carWidth, carHeight)) {
            if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN) {
                // Remove the current parking spot
                parkingSpot.removeParkedSpot(truckX, truckY, carWidth, carHeight);
    
                // Increase money by a random amount between 10 and 50 when exiting the parking spot
                ((GameFrame) SwingUtilities.getWindowAncestor(this)).getScorePanel().addRandomMoney();
    
                parkingSpot.resetParkingStatus();  // Reset the parking status
                gamePaused = false;  // Unpause the game
                gameTimer.start();  // Restart the game timer to resume scrolling
                collisionDisabled = true;  // Disable collisions during transition
    
                // Enable 1 second of immunity after exiting parking
                isImmune = true;
                System.out.println("Vehicle is immune for 1 second!");
    
                // Timer to disable immunity after 1 second
                Timer immunityTimer = new Timer(1000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        isImmune = false;  // Disable immunity after 1 second
                        System.out.println("Immunity expired.");
                    }
                });
                immunityTimer.setRepeats(false);  // Ensure the timer only runs once
                immunityTimer.start();
    
                System.out.println("Player exited parking spot. Parking spot removed. Game resumed.");
    
                // Automatically move the car to a random middle lane (2, 3, or 4)
                final int middleLane = new Random().nextInt(3) + 2;  // Randomly choose lane 2, 3, or 4
                moveToLane(middleLane);  // Move the car to a chosen middle lane
            }
        } else {
            // Vertical movement: handle UP and DOWN keys
            final int targetLane;
            int laneHeight = roadHeight / maxLane;
    
            if (keyCode == KeyEvent.VK_UP && currentLane > 1) {
                targetLane = currentLane - 1;
            } else if (keyCode == KeyEvent.VK_DOWN && currentLane < maxLane) {
                targetLane = currentLane + 1;
            } else {
                targetLane = currentLane;  // No lane change
            }
    
            if ((targetLane == 1 || targetLane == 5) && !canEnterSpecialLanes) {
                System.out.println("You can't enter top or bottom lanes yet!");
                return;  // Prevent moving to top or bottom lanes unless near the parking spot
            }
    
            moveToLane(targetLane);  // Perform lane movement
    
            // Right movement: handle RIGHT key with fixed small increment
           /*  if (keyCode == KeyEvent.VK_RIGHT) {
                int moveDistance = 20;  // Adjust this value for the step size (small incremental step)
                truckX = Math.min(roadX + roadWidth - carWidth, truckX + moveDistance);  // Restrict to road boundary
                repaint();  // Update the screen to reflect the new position
            }*/
    
            repaint();  // Update the screen to reflect the movement
        }
    }
    

    private void moveToLane(int targetLane) {
        int laneHeight = roadHeight / maxLane;

        // Calculate the Y position for the target lane
        final int targetY = roadY + (laneHeight * (targetLane - 1));

        // Smooth transition to the target lane
        Timer moveTimer = new Timer(10, e -> {
            if (truckY < targetY) {
                truckY = Math.min(truckY + 5, targetY);  // Move truck downwards
            } else if (truckY > targetY) {
                truckY = Math.max(truckY - 5, targetY);  // Move truck upwards
            }
            repaint();  // Repaint the panel to reflect the updated position

            if (truckY == targetY) {
                ((Timer) e.getSource()).stop();  // Stop the timer once the target position is reached
                currentLane = targetLane;  // Update current lane
            }
        });
        moveTimer.start();  // Start the movement timer
    }
}
