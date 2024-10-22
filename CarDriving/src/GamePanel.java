import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class GamePanel extends JPanel {
    private javax.swing.Timer gameTimer;
    private int roadWidth;
    private int roadHeight;
    private int truckX, truckY;
    private int currentLane = 1;
    private int maxLane = 5;
    private int scrollSpeed = 5;
    private boolean GameRunning = false;
    private int windowHeight, windowWidth;

    private boolean collisionDisabled = false;

    private int carWidth;
    private int carHeight;
    private int laneHeight;

    private Obstacles obstacles;
    private int obstacleSpawnCount = 0; // control spawn rate
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

    private int laneMoved = 0; // track how much road scroleld

    private JButton startButton;

    private BufferedImage roadImage;

    public GamePanel() {
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
        startButton.addActionListener(e -> startGame());  // Action listener for button
        setLayout(null);  // Set layout to null for absolute positioning
        add(startButton);  // Add start button to the panel
    
        setFocusable(true);
        requestFocusInWindow();
    
        setDoubleBuffered(true);  // BufferedImage to prevent lag when scrolling screen
        this.roadImage = new BufferedImage(roadWidth, roadHeight, BufferedImage.TYPE_INT_ARGB);
        drawRoadImage();
    
        // Timer for scrolling effect
        gameTimer = new Timer(30, e -> {
            if (GameRunning) {
                scrollScreen();
                repaint();
            }
        });
    
        obstacles = new Obstacles(roadWidth, roadX, roadY, roadHeight / maxLane, maxLane, scrollSpeed);
        parkingSpot = new ParkingSpot(roadWidth, roadX, roadY, roadHeight / maxLane);
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

    public int getMaxLane(){
        return maxLane;
    }

    // Start the game after clicking the start button
    public void startGame() {
        gameState = GAME_SCREEN;  // Switch to game screen state
        GameRunning = true;
    
        // Set the car to start in the middle lane (lane 3)
        currentLane = 3;
        
        // Update truckY based on currentLane
        truckY = roadY + (laneHeight * (currentLane - 1)) + (laneHeight - carHeight) / 2;
        truckX = windowWidth / 2 - carWidth / 2;
    
        remove(startButton);  // Remove the Start button after the game starts
        startButton.setFocusable(false);  // Prevent start button from focus
        repaint();  // Repaint the screen without the Start screen
    
        gameTimer.start();  // Start the game timer
        requestFocusInWindow();  // Ensure focus is on game panel after game begins
    
        System.out.println("Is GamePanel Focused: " + isFocusOwner());
    
        // THE HOLY GRAIL WHICH MAKES THE CAR MOVE DO NOT DELETE PLEASE
        ((GameFrame) SwingUtilities.getWindowAncestor(this)).setGameStarted(true);
    }
    

    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    
        if (gameState == START_SCREEN) {
            drawStartScreen(g);  // Draw the start screen
        } else if (gameState == GAME_SCREEN) {
            drawRoad(g);  // Draw the road
            
            // Draw parking spots before drawing the car
            parkingSpot.drawParkingSpots(g);  // Draw parking lanes and spots
            
            obstacles.drawObstacles(g);  // Draw obstacles
            
            drawVehicle(g);  // Draw the vehicle (car) after the parking spot, so it's on top
            
            drawAnticipationArrow(g);  // Draw anticipation arrow
        }
    }
    
    

    private void drawAnticipationArrow(Graphics g) {
        g.setColor(Color.RED);
        int arrowX = parkingSpot.isNextSpotLeft() ? roadX - 50 : roadX + roadWidth + 10;
        int arrowY = roadY - 30;
        g.fillPolygon(new int[]{arrowX, arrowX + 20, arrowX},
             new int[]{arrowY, arrowY + 10, arrowY + 20}, 3);  // Triangle shape for arrow
    }

    private void scrollScreen() {
        if (gamePaused) {
            return;  // If the game is paused, do nothing
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
            gamePaused = true;  // Pause the game
            gameTimer.stop();  // Stop the game timer to stop scrolling
            System.out.println("Game paused. Player parked successfully.");
            return;  // Exit the method after pausing
        }
    
        // Check for collisions with obstacles
        if (obstacles.checkCollision(truckX, truckY, carWidth, carHeight)) {
            GameRunning = false;
            gameTimer.stop();
            // Handle game over logic (e.g., restarting the game or ending it)
        }
    
        repaint();  // Continue scrolling and repaint the screen
    }

    private void restartGame() {
        // Reset game variables
        truckX = windowWidth / 2 - 40;
        truckY = (windowHeight / 2) + 50;
        currentLane = 1;
        laneMoved = 0;
        obstacles = new Obstacles(roadWidth, roadX,
         roadY, roadHeight / maxLane, maxLane, scrollSpeed);
    
        GameRunning = true;
        gameTimer.start();
        requestFocusInWindow();  // Ensure the panel has focus
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
            int laneHeight = roadHeight / maxLane;
        
            // Draw lanes 1 to 5
            for (int i = 1; i <= maxLane; i++) {
                int laneY = roadY + (i - 1) * laneHeight;
        
                // Top and bottom lanes in light grey
                if (i == 1 || i == 5) {
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(roadX, laneY, roadWidth, laneHeight);  // Fill the lane with light grey
                } 
                // Middle lanes (2, 3, 4) in dark grey
                else {
                    g.setColor(Color.DARK_GRAY);
                    g.fillRect(roadX, laneY, roadWidth, laneHeight);  // Fill the lane with dark grey
        
                    // Draw dashed lines between lanes 2-3 and 3-4
                    if (i == 2 || i == 3) {  // Only between lanes 2-3 and 3-4
                        g.setColor(Color.WHITE);
                        for (int x = 0; x <= roadWidth; x += 40) {
                            g.drawLine(x, laneY + laneHeight, x + 20, laneY + laneHeight);  // Dashed line
                        }
                    }
                }
            }
        }
 
    private void drawBottomLane(Graphics g) {
        
        int laneHeight = 200 / maxLane;
        int bottomLaneY = roadY + (maxLane * laneHeight);
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(roadX,bottomLaneY,roadWidth,SHOULDER_WIDTH);
    }

    private void drawTopLane(Graphics g) {
        int topLaneY = roadY - 50;  // Y-coordinate of the top lane
        int laneHeight = 200 / maxLane;
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(roadX, topLaneY, roadWidth, laneHeight);  // Draw the top lane
    }
    
    

    private void drawVehicle(Graphics g){
        g.setColor(Color.RED);
        g.fillRect(truckX, truckY, carWidth, carHeight);
    }

    public void moveVehicle(int keyCode) {
        if (gameState != GAME_SCREEN) {
            return;
        }
    
        // If the car is parked, remove the parking spot and force move to a middle lane
        if (parkingSpot.isPlayerParked(truckX, truckY, carWidth, carHeight)) {
            if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN) {
                // Remove the current parking spot
                parkingSpot.removeParkedSpot(truckX, truckY, carWidth, carHeight);
    
                parkingSpot.resetParkingStatus();  // Reset the parking status
                gamePaused = false;  // Unpause the game
                gameTimer.start();  // Restart the game timer to resume scrolling
                collisionDisabled = true;  // Disable collisions during transition
    
                System.out.println("Player exited parking spot. Parking spot removed. Game resumed.");
    
                // Automatically move the car to a random middle lane (2, 3, or 4)
                final int middleLane = new Random().nextInt(3) + 2;  // Randomly choose lane 2, 3, or 4
                moveToLane(middleLane);  // Move the car to a chosen middle lane
            }
        } else {
            // Normal vehicle movement logic (same as your existing logic)
            final int targetLane;
            int laneHeight = roadHeight / maxLane;
    
            if (keyCode == KeyEvent.VK_UP && currentLane > 1) {
                targetLane = currentLane - 1;
            } else if (keyCode == KeyEvent.VK_DOWN && currentLane < maxLane) {
                targetLane = currentLane + 1;
            } else {
                return;  // If no valid movement, exit early
            }
    
            final int targetY = ((windowHeight / 2) - (roadHeight / 2)) + (laneHeight * (targetLane - 1));
            Timer moveTimer = new Timer(10, e -> {
                if (truckY < targetY) {
                    truckY = Math.min(truckY + 5, targetY);  // Move truck upwards
                } else if (truckY > targetY) {
                    truckY = Math.max(truckY - 5, targetY);  // Move truck downwards
                }
                repaint();  // Repaint the panel to reflect the updated position
    
                if (truckY == targetY) {
                    ((Timer) e.getSource()).stop();  // Stop the timer once the target position is reached
                    collisionDisabled = false;  // Re-enable collisions after reaching a middle lane
                    currentLane = targetLane;  // Update current lane
                }
            });
            moveTimer.start();  // Start the movement timer
    
            currentLane = targetLane;  // Update the current lane after the transition
        }
    }
    
    
    
    private void moveToLane(int targetLane) {
        int laneHeight = roadHeight / maxLane;
    
        // Calculate the Y position for the target lane
        final int targetY = ((windowHeight / 2) - (roadHeight / 2)) + (laneHeight * (targetLane - 1));
    
        // Smooth transition to the target lane
        Timer moveTimer = new Timer(10, e -> {
            if (truckY < targetY) {
                truckY = Math.min(truckY + 5, targetY);  // Move truck upwards
            } else if (truckY > targetY) {
                truckY = Math.max(truckY - 5, targetY);  // Move truck downwards
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