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
        this.roadWidth = 800;
        this.roadHeight = 200;
        this.windowWidth = 800;
        this.windowHeight = 600;
        this.truckX = windowWidth / 2 - 40; // Center initial position of vehicle
        this.truckY = (windowHeight / 2) + 50; // Vertical position of vehicle
        
        this.roadX = (windowWidth - roadWidth) / 2;
        this.roadY = (windowHeight / 2) - (roadHeight / 2);

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

        obstacles = new Obstacles(roadWidth, roadX,
         roadY, roadHeight / maxLane, maxLane, scrollSpeed);

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
           // drawShoulder(g);
            drawBottomLane(g);
            drawTopLane(g);
            g.setColor(Color.GRAY);
            g.fillRect(roadX + roadWidth, roadY, SHOULDER_WIDTH, roadHeight);
            
            
            drawVehicle(g);  // Draw the vehicle
            parkingSpot.drawParkingSpots(g);  // Draw parking lanes and spots
            obstacles.drawObstacles(g);  // Draw obstacles
            drawAnticipationArrow(g);  // Draw arrow
        }
    }
    

    private void drawAnticipationArrow(Graphics g) {
        g.setColor(Color.RED);
        int arrowX = parkingSpot.isNextSpotLeft() ? roadX - 50 : roadX + roadWidth + 10;
        int arrowY = roadY - 30;
        g.fillPolygon(new int[]{arrowX, arrowX + 20, arrowX},
             new int[]{arrowY, arrowY + 10, arrowY + 20}, 3);  // Triangle shape for arrow
    }

    /* 
    private void scrollScreen() {
        
        parkingSpotSpawnCount++;
        if(parkingSpotSpawnCount >= 50){
            parkingSpot.generateParkingSpot();
            parkingSpotSpawnCount = 0;
        }
        parkingSpot.moveParkingSpots(scrollSpeed);

        // Check for parking success
        if (parkingSpot.checkParking(truckX, truckY, 80, 40)) {
            ((GameFrame) SwingUtilities.getWindowAncestor(this))
            .getScorePanel().successfulDelivery();
            
            parkSuccess = true;  // Track if the player parked correctly
        } else if (!parkSuccess) {
            ((GameFrame) SwingUtilities.getWindowAncestor(this)).getScorePanel().missedDelivery();
        }

        laneMoved = scrollSpeed + laneMoved;
        if (laneMoved >= roadHeight / maxLane) {
            laneMoved = 0;
        }

        obstacleSpawnCount++;
        if (obstacleSpawnCount >= 100){
            obstacles.generateObstacle();
            obstacleSpawnCount = 0;
        }

        obstacles.moveObstacles();
        // Check for collisions
        if (obstacles.checkCollision(truckX, truckY, 80, 40)) {
            // Stop the game when a collision happens
            GameRunning = false;
            gameTimer.stop();

            // Show a "Game Over" message to the user
            int response = JOptionPane.showOptionDialog(this, 
                "Game Over! Do you want to play again?", 
                "Game Over", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.INFORMATION_MESSAGE, 
                null, null, null);

            // If the player chooses to restart the game
            if (response == JOptionPane.YES_OPTION) {
                restartGame();  // Custom method to reset and restart
            } else {
            // Close the window or return to the start screen
            ((JFrame) SwingUtilities.getWindowAncestor(this)).dispose();  // Close game
        }
    }

        repaint();
    }
*/

   /*  private void scrollScreen() {
        if (!parkingSpot.isPlayerParked(truckX, truckY, 80, 40)) {  // Only scroll if the player is not parked
            parkingSpotSpawnCount++;
            if (parkingSpotSpawnCount >= 50) {
                parkingSpot.generateParkingSpot();
                parkingSpotSpawnCount = 0;
            }
            parkingSpot.moveParkingSpots(scrollSpeed);

            // Check for parking success
            if (parkingSpot.isPlayerParked(truckX, truckY, 80, 40)) {
                parkSuccess = true;  // Track if the player parked correctly
            } else if (!parkSuccess) {
                ((GameFrame) SwingUtilities.getWindowAncestor(this)).getScorePanel().missedDelivery();
            }

            laneMoved = scrollSpeed + laneMoved;
            if (laneMoved >= roadHeight / maxLane) {
                laneMoved = 0;
            }

            obstacleSpawnCount++;
            if (obstacleSpawnCount >= 100) {
                obstacles.generateObstacle();
                obstacleSpawnCount = 0;
            }

            obstacles.moveObstacles();
            // Check for collisions
            if (obstacles.checkCollision(truckX, truckY, 80, 40)) {
                GameRunning = false;
                gameTimer.stop();

                // Show "Game Over" message
                int response = JOptionPane.showOptionDialog(this, "Game Over! Do you want to play again?", 
                                                            "Game Over", JOptionPane.YES_NO_OPTION, 
                                                            JOptionPane.INFORMATION_MESSAGE, null, null, null);
                if (response == JOptionPane.YES_OPTION) {
                    restartGame();
                } else {
                    ((JFrame) SwingUtilities.getWindowAncestor(this)).dispose();
                }
            }

            repaint();
        } else {
            // Pause the game, waiting for user to exit the parking spot
            System.out.println("Player is parked, scrolling stopped.");
        }
    }*/
   /*  private void scrollScreen() {
        // If the player is parked, stop scrolling but allow movement to exit the parking spot
        if (!parkingSpot.isPlayerParked(truckX, truckY, 80, 40)) {
            parkingSpotSpawnCount++;
            if (parkingSpotSpawnCount >= 50) {
                parkingSpot.generateParkingSpot();
                parkingSpotSpawnCount = 0;
            }
            parkingSpot.moveParkingSpots(scrollSpeed);
    
            // Handle lane scrolling
            laneMoved = scrollSpeed + laneMoved;
            if (laneMoved >= roadHeight / maxLane) {
                laneMoved = 0;
            }
    
            obstacleSpawnCount++;
            if (obstacleSpawnCount >= 100) {
                obstacles.generateObstacle();
                obstacleSpawnCount = 0;
            }
    
            obstacles.moveObstacles();
    
            // Check for collisions
            if (obstacles.checkCollision(truckX, truckY, 80, 40)) {
                GameRunning = false;
                gameTimer.stop();
    
                // Show a "Game Over" message to the user
                int response = JOptionPane.showOptionDialog(this, 
                    "Game Over! Do you want to play again?", 
                    "Game Over", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.INFORMATION_MESSAGE, 
                    null, null, null);
    
                if (response == JOptionPane.YES_OPTION) {
                    restartGame();
                } else {
                    ((JFrame) SwingUtilities.getWindowAncestor(this)).dispose();
                }
            }
        } else {
            // When parked, pause scrolling but allow movement to exit
            System.out.println("Player is parked, scrolling stopped.");
        }
    
        repaint();
    }
    */
    

    /*private void scrollScreen() {
        if (gamePaused) {
            return;  // If the game is paused, do nothing
        }

        // Check if the player is parked
        if (parkingSpot.isPlayerParked(truckX, truckY, 80, 40)) {
            gamePaused = true;  // Stop the game when parked
            gameTimer.stop();   // Stop the timer, pausing the game
            System.out.println("Game paused. Player parked successfully.");
        } else {
            // Continue scrolling and spawning obstacles
            parkingSpotSpawnCount++;
            if (parkingSpotSpawnCount >= 50) {
                parkingSpot.generateParkingSpot();
                parkingSpotSpawnCount = 0;
            }
            parkingSpot.moveParkingSpots(scrollSpeed);

            laneMoved = scrollSpeed + laneMoved;
            if (laneMoved >= roadHeight / maxLane) {
                laneMoved = 0;
            }

            obstacleSpawnCount++;
            if (obstacleSpawnCount >= 100) {
                obstacles.generateObstacle();
                obstacleSpawnCount = 0;
            }

            obstacles.moveObstacles();

            // Check for collisions
            if (obstacles.checkCollision(truckX, truckY, 80, 40)) {
                GameRunning = false;
                gameTimer.stop();

                // Show "Game Over" message
                int response = JOptionPane.showOptionDialog(this, "Game Over! Do you want to play again?", 
                                                            "Game Over", JOptionPane.YES_NO_OPTION, 
                                                            JOptionPane.INFORMATION_MESSAGE, null, null, null);
                if (response == JOptionPane.YES_OPTION) {
                    restartGame();
                } else {
                    ((JFrame) SwingUtilities.getWindowAncestor(this)).dispose();
                }
            }

            repaint();  // Continue the game and scroll screen
        }
    }
        */

      /*   private void scrollScreen() {
            // If the game is paused, exit the method immediately
            if (gamePaused) {
                return;
            }
        
            // Check if the player is parked
            if (parkingSpot.isPlayerParked(truckX, truckY, 80, 40)) {
                gamePaused = true;  // Set the game to paused state
                System.out.println("Game paused. Player parked successfully.");
        
                // Optionally, display a message to the player
                JOptionPane.showMessageDialog(this, "Car parked! Game paused.");
                return;  // Exit the method after pausing
            }
        
            // Continue normal game logic: scrolling screen, spawning obstacles
            parkingSpotSpawnCount++;
            if (parkingSpotSpawnCount >= 50) {
                parkingSpot.generateParkingSpot();
                parkingSpotSpawnCount = 0;
            }
            parkingSpot.moveParkingSpots(scrollSpeed);
        
            laneMoved = scrollSpeed + laneMoved;
            if (laneMoved >= roadHeight / maxLane) {
                laneMoved = 0;
            }
        
            obstacleSpawnCount++;
            if (obstacleSpawnCount >= 100) {
                obstacles.generateObstacle();
                obstacleSpawnCount = 0;
            }
        
            obstacles.moveObstacles();
        
            // Check for collisions
            if (obstacles.checkCollision(truckX, truckY, 80, 40)) {
                GameRunning = false;
                gameTimer.stop();
        
                // Show "Game Over" message
                int response = JOptionPane.showOptionDialog(this, "Game Over! Do you want to play again?", 
                                                            "Game Over", JOptionPane.YES_NO_OPTION, 
                                                            JOptionPane.INFORMATION_MESSAGE, null, null, null);
                if (response == JOptionPane.YES_OPTION) {
                    restartGame();
                } else {
                    ((JFrame) SwingUtilities.getWindowAncestor(this)).dispose();
                }
            }
        
            repaint();  // Continue the game and scroll screen
        }
       */
      
       private void scrollScreen() {
        // If the game is paused, exit the method immediately
        if (gamePaused) {
            return;
        }
    
        // Check if the player is parked
        if (parkingSpot.isPlayerParked(truckX, truckY, 40, 40)) {
            gamePaused = true;  // Set the game to paused state
            GameRunning = false;  // Stop game logic
            gameTimer.stop();  // Stop the game timer
            System.out.println("Game paused. Player parked successfully.");
    
            // Optionally, display a message to the player
            JOptionPane.showMessageDialog(this, "Car parked! Game paused.");
            return;  // Exit the method after pausing
        }
    
        // Continue normal game logic: scrolling screen, spawning obstacles
        parkingSpotSpawnCount++;
        if (parkingSpotSpawnCount >= 50) {
            parkingSpot.generateParkingSpot();
            parkingSpotSpawnCount = 0;
        }
        parkingSpot.moveParkingSpots(scrollSpeed);
    
        laneMoved = scrollSpeed + laneMoved;
        if (laneMoved >= roadHeight / maxLane) {
            laneMoved = 0;
        }
    
        obstacleSpawnCount++;
        if (obstacleSpawnCount >= 100) {
            obstacles.generateObstacle();
            obstacleSpawnCount = 0;
        }
    
        obstacles.moveObstacles();
    
        // Check for collisions
        if (obstacles.checkCollision(truckX, truckY, 80, 40)) {
            GameRunning = false;
            gameTimer.stop();
    
            // Show "Game Over" message
            int response = JOptionPane.showOptionDialog(this, "Game Over! Do you want to play again?", 
                                                        "Game Over", JOptionPane.YES_NO_OPTION, 
                                                        JOptionPane.INFORMATION_MESSAGE, null, null, null);
            if (response == JOptionPane.YES_OPTION) {
                restartGame();
            } else {
                ((JFrame) SwingUtilities.getWindowAncestor(this)).dispose();
            }
        }
    
        repaint();  // Continue the game and scroll screen
    }
    
          

    

    // Add a KeyListener to resume the game when a key is pressed



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
        //int roadX = (windowWidth - roadWidth) / 2;
        int roadY = (windowHeight / 2) - (roadHeight / 2);
    
        // Draw the pre-rendered road, moving from right to left
        g.drawImage(roadImage, roadX - laneMoved, roadY, this);
        if (laneMoved > 0) {
            g.drawImage(roadImage, roadX + roadWidth - laneMoved, roadY, this);
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
        g.fillRect(truckX, truckY, 40, 40);
    }
    

    // Handle vehicle movement based on key presses
  // Handle vehicle movement based on key presses
  
  /* 
  public void moveVehicle(int keyCode) {
    if (gameState != GAME_SCREEN) {
        return;
    }

    int targetLane = currentLane;
    
    int laneHeight = roadHeight/ maxLane;

    // Determine the new lane based on key press
    if (keyCode == KeyEvent.VK_UP && currentLane > 1) {
        targetLane = currentLane - 1;
    } else if (keyCode == KeyEvent.VK_DOWN && currentLane < maxLane) {
        targetLane = currentLane + 1;
    }
    
    // Smooth transition between lanes by gradually adjusting truckY
    final int targetY = ((windowHeight / 2) - (roadHeight / 2)) + (laneHeight * (targetLane - 1));
    Timer moveTimer = new Timer(10, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (truckY < targetY) {
                truckY = Math.min(truckY + 5, targetY);  // Move truckY upwards
            } else if (truckY > targetY) {
                truckY = Math.max(truckY - 5, targetY);  // Move truckY downwards
            }
            repaint();  // Repaint the panel to reflect the updated position
            
            if (truckY == targetY) {
                ((Timer)e.getSource()).stop();  // Stop the timer once the target position is reached
            }
        }
    });
    moveTimer.start();  // Start the movement timer
    
    currentLane = targetLane;  // Update the current lane after the transition
}
    */

    public void moveVehicle(int keyCode) {
        if (gameState != GAME_SCREEN) {
            return;
        }
    
        if (parkingSpot.isPlayerParked(truckX, truckY, 80, 40)) {
            // If player is parked, allow exiting with UP or DOWN arrow
            if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN) {
                parkingSpot.resetParkingStatus();  // Player is exiting the parking spot
                parkSuccess = false;  // Reset parking success status
            }
        } else {
            // Normal vehicle movement logic
            int targetLane = currentLane;
            int laneHeight = roadHeight / maxLane;
    
            if (keyCode == KeyEvent.VK_UP && currentLane > 1) {
                targetLane = currentLane - 1;
            } else if (keyCode == KeyEvent.VK_DOWN && currentLane < maxLane) {
                targetLane = currentLane + 1;
            }
    
            final int targetY = ((windowHeight / 2) - (roadHeight / 2)) + (laneHeight * (targetLane - 1));
            Timer moveTimer = new Timer(10, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (truckY < targetY) {
                        truckY = Math.min(truckY + 5, targetY);  // Move truckY upwards
                    } else if (truckY > targetY) {
                        truckY = Math.max(truckY - 5, targetY);  // Move truckY downwards
                    }
                    repaint();
                    
                    if (truckY == targetY) {
                        ((Timer)e.getSource()).stop();  // Stop the timer once the target position is reached
                    }
                }
            });
            moveTimer.start();  // Start the movement timer
    
            currentLane = targetLane;  // Update the current lane after the transition
        }
    }



    
}