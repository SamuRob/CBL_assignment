import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.*;
import java.util.*;

public class GamePanel extends JPanel {
    private javax.swing.Timer gameTimer;
    private javax.swing.Timer speedIncreaseTimer; // Timer to increase scroll speed
    private javax.swing.Timer moveRightTimer; // Timer to handle smooth right movement

    private ArrayList<Tree> trees = new ArrayList<>();
    private Random random = new Random();

    private int roadWidth;
    private int roadHeight;
    private int truckX, truckY;
    private int currentLane = 1;
    private int maxLane = 5;
    private int scrollSpeed = 5;
    private int level = 1;



    private boolean isMovingRight;
    private boolean isMovingLeft;
    private boolean isSliding = false;
    private boolean isCountdownRunning = true;

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

    private int monkeyX = 100;
    private int monkeyY = 300;

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
    private static final int INSTRUCTION_SCREEN = 2;
    private int gameState = START_SCREEN;

    private int laneMoved = 0; // Track how much road scrolled

    private JButton startButton;
    private JButton instructionButton;

    private BufferedImage roadImage;
    private BufferedImage carImage;
    private BufferedImage backgroundImage;
    private BufferedImage streetImage;
    private BufferedImage arrowImage;
    private BufferedImage resizedArrowImage;

    //private ArrayList<BufferedImage> carImages = new ArrayList<>();
    //private Random random = new Random();
    

    private int streetMoved;
    private int buildingX;

    private JLabel gifLabel;

    public GamePanel(ScorePanel scorePanel) {
       // this.scorePanel = scorePanel;


        setLayout(null);

        setFocusable(true);
        requestFocusInWindow();

        this.roadWidth = 800;
        this.roadHeight = 200;
        this.windowWidth = 800;
        this.windowHeight = 600;

        this.laneHeight = roadHeight / maxLane;
        this.carHeight = laneHeight;
        this.carWidth = carHeight * 2;

        this.roadX = (windowWidth - roadWidth) / 2;
        this.roadY = (windowHeight / 2) - (roadHeight / 2);

        // Align the initial truckY to currentLane = 2 (which is visually the second lane)
        this.currentLane = 2;
        this.truckX = windowWidth / 2 - carWidth / 2;
        this.truckY = roadY + (laneHeight * (currentLane - 1)) + (laneHeight - carHeight) / 2;

        startButton = new JButton("Start");
        startButton.setFont(new Font("Verdana", Font.BOLD, 22));
        startButton.setBounds(windowWidth / 2 - 100, windowHeight / 2, 200, 50);
        
        startButton.setBackground(Color.GREEN);
        startButton.setFocusPainted(false); // Removes the focus border
        startButton.setOpaque(true);
        startButton.setBorderPainted(false);

        // Add hover effect using a MouseListener
        startButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                startButton.setBackground(new Color(0,255,255)); // Change background color when hovered
            }

            @Override
            public void mouseExited(MouseEvent e) {
                startButton.setBackground(Color.GREEN); // Reset background color when not hovered
            }
        });

        startButton.addActionListener(e -> startGame()); // Action listener for button
        setLayout(null); // Set layout to null for absolute positioning
        add(startButton); // Add start button to the panel

        instructionButton = new JButton("Instructions");
        instructionButton.setFont(new Font("Verdana", Font.BOLD, 22));
        instructionButton.setBounds(windowWidth / 2 - 100, windowHeight / 2 + 60, 200, 50);
       
        instructionButton.setBackground(Color.LIGHT_GRAY);
        instructionButton.setFocusPainted(false); // Removes the focus border
        instructionButton.setOpaque(true);
        instructionButton.setBorderPainted(false);

        // Add hover effect using a MouseListener
        instructionButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                instructionButton.setBackground(Color.GRAY); // Change background color when hovered
            }

            @Override
            public void mouseExited(MouseEvent e) {
                instructionButton.setBackground(Color.LIGHT_GRAY); // Reset background color when not hovered
            }
        });

        instructionButton.addActionListener(e -> showInstructions());
        add(instructionButton);


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

        try {
            streetImage = ImageIO.read(getClass().getResource("/HousesBackground.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Building image not found.");
        }

        try {
            arrowImage = ImageIO.read(getClass().getResource("/arrow_buttons.png"));
            resizedArrowImage = resizeImage(arrowImage, 5);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Instruction image not found.");
        }

        ImageIcon gifIcon = new ImageIcon(getClass().getResource("/monkey.gif"));
        Image scaledGifImage = gifIcon.getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT); //resize to dimensions
        ImageIcon resizedGifIcon = new ImageIcon(scaledGifImage);
        gifLabel = new JLabel(resizedGifIcon);

        gifLabel.setBounds(100, 100, gifIcon.getIconWidth(), gifIcon.getIconHeight());

        add(gifLabel);

        speedIncreaseTimer = new javax.swing.Timer(10000, e -> {
            scrollSpeed++; 
            level++; 
            scorePanel.updateLevel(level); //update level
            obstacles.setScrollSpeed(scrollSpeed);
            obstacles.increaseDifficulty(level); //increase obstacle frequency based on level
            System.out.println("Level increased: " + level + " | Scroll Speed: " + scrollSpeed);
        });        

        // Timer for scrolling effect
        gameTimer = new javax.swing.Timer(30, e -> {
            if (GameRunning) {
                scrollScreen();
                repaint();
            }
        });

        obstacles = new Obstacles(roadWidth, roadX, roadY, roadHeight / maxLane, maxLane, scrollSpeed, this);
        parkingSpot = new ParkingSpot(roadWidth, roadX, roadY, roadHeight / maxLane);

        // Initialize the speed increase timer (e.g., every 30 seconds)
        speedIncreaseTimer = new javax.swing.Timer(10000, e -> {
            scrollSpeed++; // Increase scroll speed

            level++; // Increase the level
            scorePanel.updateLevel(level); // Update the level display
            obstacles.setScrollSpeed(scrollSpeed);
            System.out.println("Level increased: " + level + " | Scroll Speed: " + scrollSpeed);
        });
    }
    

    private BufferedImage resizeImage(BufferedImage originalImage, double scale) {
        int newWidth = (int) (originalImage.getWidth() * scale);
        int newHeight = (int) (originalImage.getHeight() * scale);
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
        
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        return resizedImage;
    }
    

    private void showInstructions() {
        gameState = INSTRUCTION_SCREEN; // Switch to the instruction screen
    
        // Safely remove the start and instruction buttons if they exist
        if (startButton.getParent() != null) {
            remove(startButton); // Remove the Start button
        }
        if (instructionButton.getParent() != null) {
            remove(instructionButton); // Remove the Instructions button
        }
    
        repaint(); // Repaint to show the new screen
    }
    
    public void handleBananaCollision() {
        // Avoid triggering the slide if already sliding
        if (isSliding){
            return;
        } 
    
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
    
        // Set sliding flag to true to prevent re-triggering the slide
        isSliding = true;
        moveToLane(targetLane);
    
        // Reset the sliding flag after a short delay to allow for other collisions
        javax.swing.Timer slideResetTimer = new javax.swing.Timer(500, e -> isSliding = false);
        slideResetTimer.setRepeats(false); // Only run the timer once
        slideResetTimer.start();
    }
    
    

    private void drawRoadImage() {
        Graphics g = roadImage.getGraphics();
    
        g.setColor(Color.WHITE);
        int laneHeight = roadHeight / maxLane;
        int lineLength = 30;
        for (int i = 2; i < maxLane - 1; i++) {
            int laneY = i * laneHeight;
            for (int x = 0; x <= roadWidth; x += 2 * lineLength) {
                g.drawLine(x, laneY, x + lineLength, laneY);  // Draw dashes
            }
        }
        g.dispose();  // Clean up resources
    }
    
    public int getMaxLane() {
        return maxLane;
    }

    // Start the game after clicking the start button
    public void startGame() {
        gameState = GAME_SCREEN; // Switch to game screen state
        GameRunning = true; // Set the game as running
        countdown = 3; // Start countdown from 3
        
        isCountdownRunning = true; // countdown true at start of game

        gameTimer.start(); // Start the game timer
        speedIncreaseTimer.start(); // Start the speed increase timer
        
        // Set the car to start in the middle lane (lane 3)
        currentLane = 3;
    
        // Update truckY based on currentLane
        truckY = roadY + (laneHeight * (currentLane - 1)) + (laneHeight - carHeight) / 2;
        truckX = windowWidth / 2 - carWidth / 2;
        
        remove(startButton); // Remove the Start button after the game starts
        remove(instructionButton); // Remove the Instructions button after the game starts
        
        startButton.setFocusable(false); // Prevent start button from focus
        instructionButton.setFocusable(false); // Prevent instruction button from focus
    
        repaint(); // Repaint the screen without the Start and Instructions buttons
    
        // Timer for the countdown
        javax.swing.Timer countdownTimer = new javax.swing.Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countdown--; // Decrease countdown each second
                repaint(); // Repaint to show the updated countdown
    
                if (countdown <= 0) {
                    ((javax.swing.Timer) e.getSource()).stop(); // Stop the countdown timer
                    GameRunning = true; // Set the game as running
                    gameTimer.start(); // Start the game timer and the scrolling effect
                    requestFocusInWindow(); // Ensure focus is on game panel after game begins
    
                    // Notify the game frame that the game has started
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
    
        // Draw the background image if it exists
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, -50, windowWidth, 
                windowHeight, null); // Draw background to cover the entire window
        }
    
        for(Tree tree : trees) {
            tree.draw(g);
        }

        // Handle different game states
        if (gameState == START_SCREEN) {
            drawStartScreen(g); // Draw the start screen
            remove(gifLabel);
        } else if (gameState == GAME_SCREEN) {
            drawRoad(g); // Draw the road
            parkingSpot.drawParkingSpots(g); // Draw parking lanes and spots
            obstacles.drawObstacles(g); // Draw obstacles
            drawVehicle(g); // Draw the vehicle (car)
            drawStreet(g);
           // Rectangle spot = parkingSpot.getNextParkingSpot();
           // drawAnticipationArrow(g); // Draw anticipation arrow
    
            // Only add the monkey gif when the game is running
            if (!isAncestorOf(gifLabel)) {
                add(gifLabel);
            }

            // Draw the countdown on top of the road and other elements, if applicable
            if (countdown > 0) {
                drawCountdown(g); // Draw the countdown
            }
        } else if (gameState == INSTRUCTION_SCREEN) {
            drawInstructionScreen(g); // Draw the instruction screen
            remove(gifLabel);
        }
    }
    
    private void drawInstructionScreen(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, windowWidth, windowHeight);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Trebuchet MS", Font.BOLD, 50));
        g.drawString("Instructions", windowWidth / 2 - 100, 100);

        g.setFont(new Font("Trebuchet MS", Font.PLAIN, 25));
        g.drawString("Use the arrow keys to move the vehicle.", 100, 200);
        g.drawString("Avoid obstacles and park in designated spots.", 100, 240);
        g.drawString("Press UP or DOWN to change lanes.", 100, 280);
        g.drawString("Press SPACE to pause the game.", 100, 320);
    
        if (arrowImage != null) {
            int imageX = (windowWidth - resizedArrowImage.getWidth()) / 2; // Center horizontally
            int imageY = windowHeight / 2 - resizedArrowImage.getHeight() / 2; // Center vertically
            g.drawImage(resizedArrowImage, imageX + 200, imageY - 100, null);
        }

        JButton backButton = new JButton("Back to Start");
        backButton.setBounds(windowWidth / 2 - 100, windowHeight - 150, 200, 50); 
        backButton.addActionListener(e -> backToStart()); // return to home screen
        setLayout(null); 
        add(backButton); 
    }


    private void backToStart() {
        gameState = START_SCREEN; // Switch back to the start screen
        removeAll();
        add(startButton); 
        add(instructionButton); 
        repaint(); // Repaint to show the start screen
    }
    

    private void drawCountdown(Graphics g) {
        g.setColor(Color.WHITE); // Set the text color
        g.setFont(new Font("Trebuchet MS", Font.BOLD, 100)); // Set a large font for the countdown

        String countdownText = countdown > 0 ? String.valueOf(countdown) : "GO!"; 
        int textWidth = g.getFontMetrics().stringWidth(countdownText); 
        int textX = (windowWidth - textWidth) / 2; // Center the text horizontally
        int textY = windowHeight / 2; // Center the text vertically

        g.drawString(countdownText, textX, textY); // Draw the countdown text
    }

    private void drawAnticipationArrow(Graphics g, Rectangle parkingSpot) {
        g.setColor(Color.RED);
        int arrowX = parkingSpot.x; // Adjust as needed
        int arrowY = parkingSpot.y - 30;
        g.fillPolygon(new int[]{arrowX, arrowX + 20, arrowX},
                      new int[]{arrowY, arrowY + 10, arrowY + 20}, 3); // Triangle shape for arrow
    }
    
    
    // In your scrollScreen method
    private void scrollScreen() {
        if (gamePaused) {
            return; // If the game is paused, do nothing
        }

        // Update monkey position along with the scroll
        monkeyX -= scrollSpeed;
    
        // Define spawn regions for trees
        int regionAboveTopLaneMinY = roadY - 220; // 100 pixels above the road
        int regionAboveTopLaneRange = 100; // Vertical range for tree spawn above road
    
        int regionBelowBottomLaneMinY = roadY + (laneHeight * maxLane)  - 10; // 50 pixels below the road
        int regionBelowBottomLaneRange = 100; // Vertical range for tree spawn below road
    
        // Randomly decide if a tree should spawn in the above-top-lane region
        if (random.nextInt(100) < 20) { // Adjust the probability to control spawn rate
            int treeYAbove = regionAboveTopLaneMinY + random.nextInt(regionAboveTopLaneRange);
            trees.add(new Tree(roadX - 80, treeYAbove)); // Left side of the road
            trees.add(new Tree(roadX + roadWidth + 60, treeYAbove)); // Right side of the road
        }
    
        // Randomly decide if a tree should spawn in the below-bottom-lane region
        if (random.nextInt(100) < 20) { // Adjust the probability to control spawn rate
            int treeYBelow = regionBelowBottomLaneMinY + random.nextInt(regionBelowBottomLaneRange);
            trees.add(new Tree(roadX - 80, treeYBelow)); // Left side of the road
            trees.add(new Tree(roadX + roadWidth + 60, treeYBelow)); // Right side of the road
        }
    
        // Move each tree leftward to create scrolling effect
        for (Tree tree : trees) {
            tree.move(scrollSpeed);
        }
    
        // Remove trees that have moved off-screen to the left
        trees.removeIf(tree -> tree.getX() + tree.getWidth() < 0);
    
        // Reset monkey position when it goes off-screen
        if (monkeyX + gifLabel.getWidth() < 0) {
            monkeyX = windowWidth; // Reset to the right side of the screen
        }
    
        // Update gifLabel position to reflect new monkeyX
        gifLabel.setBounds(monkeyX, monkeyY, gifLabel.getWidth(), gifLabel.getHeight());
    
        // Remaining scroll logic here
        parkingSpotSpawnCount += scrollSpeed;
        if (parkingSpotSpawnCount >= 2300) {
            parkingSpot.generateParkingSpot();
            parkingSpotSpawnCount = 0;
        }
        parkingSpot.moveParkingSpots(scrollSpeed);
    
        laneMoved += scrollSpeed;
        if (laneMoved >= 2 * 30) {
            laneMoved = 0;
        }

        streetMoved += scrollSpeed;
        if (streetMoved > windowWidth) {
            streetMoved -= windowWidth;
        }
    
        if (isMovingLeft && truckX > roadX) {
            truckX -= 7;
        }
        if (isMovingRight && (truckX + 60) < roadWidth) {
            truckX += 7;
        }
    
        if (isPlayerInParkingLane(truckY) && 
            !parkingSpot.isParkingSpotApproaching(truckX, scrollSpeed) && 
            !parkingSpot.isPlayerParked(truckX, truckY, carWidth, carHeight)) {
            
            System.out.println("Player is in a parking lane but not near a parking spot. Moving back to a middle lane.");
            moveToMiddleLane();
        }
    
        obstacleSpawnCount += scrollSpeed;
        if (obstacleSpawnCount >= 500) {
            obstacles.generateObstacle();
            obstacleSpawnCount = 0;
        }
    
        obstacles.generateObstaclesIfReady();
        obstacles.moveObstacles(truckX, truckY);



        if (parkingSpot.isPlayerParked(truckX, truckY, carWidth, carHeight)) {
            gamePaused = true;
            gameTimer.stop();
            System.out.println("Game paused. Player parked successfully.");
            return;
        }
    
        if (obstacles.checkCollision(truckX, truckY, carWidth, carHeight, isImmune)) {
            GameRunning = false;
            gameTimer.stop();
        }
    
        //drawStreet(Graphics g);
        repaint();
    }
    
    
    

    private boolean isPlayerInParkingLane(int truckY) {
        // Get the lane Y positions
        int lane1Y = roadY; // Lane 1
        int lane5Y = roadY + 4 * laneHeight; // Lane 5
        
        // Check if the truck is in either of the parking lanes (lane 1 or lane 5)
        return (truckY == lane1Y || truckY == lane5Y);
    }
    

    private void moveToMiddleLane() {
        Random random = new Random();
        int randomMiddleLane = random.nextInt(3) + 2;  // Random lane 2, 3, or 4
    
        // Update the player's Y position to the middle lane
        truckY = roadY + (randomMiddleLane - 1) * laneHeight;
    
        System.out.println("Player moved to lane " + randomMiddleLane);
    }
    

    private void restartGame() {
        // Reset game variables
        truckX = windowWidth / 2 - carWidth / 2;
        truckY = (windowHeight / 2) - carHeight / 2;
        currentLane = 3; // Reset to middle lane
        laneMoved = 0;
        scrollSpeed = 5; // Reset the scroll speed
        level = 15; // Reset the level
        scorePanel.reset();
    
        // Recreate obstacles and parking spots for a fresh start
        obstacles = new Obstacles(roadWidth, roadX, roadY, roadHeight / maxLane, maxLane, scrollSpeed, this);
    
        // Reset game state to START_SCREEN
        gameState = START_SCREEN;
        GameRunning = false;
    
        // Remove any game-related components (if necessary)
        removeAll(); 
    
        // Add the Start button and Instructions button back to the panel
        add(startButton);
        add(instructionButton);
    
        // Make sure the buttons are focusable again
        startButton.setFocusable(true);
        instructionButton.setFocusable(true);
    
        // Repaint the panel to reflect the changes
        repaint();
    }
    
    

    // Draw the Start screen
    private void drawStartScreen(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, windowWidth, windowHeight); // Background color

        // Title Text
        g.setColor(Color.BLACK);
        g.setFont(new Font("Trebuchet MS", Font.BOLD, 60));
        g.drawString("Hizmet", windowWidth / 2 - 100, windowHeight / 2 - 100);
        g.drawString("Delivery Game", windowWidth / 2 - 200, windowHeight / 2 - 50);
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
            }
        }
        
        g.drawImage(roadImage, roadX - laneMoved, roadY, this); // Draw lines (lane separators)
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

    private void drawStreet(Graphics g) {
        if (streetImage != null) {
            int buildingY = roadY - 4 * streetImage.getHeight();
            g.drawImage(streetImage, -streetMoved, buildingY, windowWidth, 200, null);
            if (streetMoved > 0) {
                g.drawImage(streetImage, windowWidth - streetMoved, buildingY, windowWidth, 200, null);
            }
        }
    }
    
    
    
    // Add this method to GamePanel
    public void handleCollision() {
        gameTimer.stop();            // Stop the game timer
        speedIncreaseTimer.stop();    // Stop the speed increase timer
        
        // Show a dialog to restart or exit
        int response = JOptionPane.showConfirmDialog(this, 
                "Game Over! You collided with an obstacle. Do you want to play again?", 
                "Game Over", JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            restartGame(); // Restart if the player chooses "Yes"
        } else {
            System.exit(0); // Exit if the player chooses "No"
        }
    }

    
    
    public void handleBombCollision() {
        System.out.println("Hit a bomb! Game Over.");
        GameRunning = false;
        gameTimer.stop(); // Stop the game timer
        speedIncreaseTimer.stop(); // Stop the speed increase timer
    
        int response = JOptionPane.showConfirmDialog(this, 
                "Game Over! You hit a bomb. Do you want to play again?",
                     "Game Over", JOptionPane.YES_NO_OPTION);
    
        if (response == JOptionPane.YES_OPTION) {
            restartGame(); // Take them to the start screen
        } else {
            System.exit(0); // Exit the game if they choose "No"
        }
    }
    
    public void setMovingLeft(boolean isMovingLeft) {
        this.isMovingLeft = isMovingLeft;
    }
    
    public void setMovingRight(boolean isMovingRight) {
        this.isMovingRight = isMovingRight;
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
                javax.swing.Timer immunityTimer = new javax.swing.Timer(1000, new ActionListener() {
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
        final int targetY = roadY + (laneHeight * (targetLane - 1)) + (laneHeight - carHeight) / 2;
    
        // Ensure smooth transition but snap into place once close to the target
        javax.swing.Timer moveTimer = new javax.swing.Timer(10, e -> {
            if (truckY < targetY) {
                truckY = Math.min(truckY + 10, targetY);  // Move truck downwards
            } else if (truckY > targetY) {
                truckY = Math.max(truckY - 10, targetY);  // Move truck upwards
            }
            repaint();  // Repaint the panel to reflect the updated position
    
            // Once the truck is aligned with the lane, stop the timer
            if (truckY == targetY) {
                ((javax.swing.Timer) e.getSource()).stop();  // Stop the timer once the target position is reached
                currentLane = targetLane;  // Update the current lane after reaching target
            }
        });
        moveTimer.start();  // Start the movement timer
    }
    
}