import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.*;
import java.io.*;

public class Obstacles {
    private ArrayList<Rectangle> obstacles;
    private int obstacleWidth = 60;
    private int obstacleHeight = 40;
    private int laneHeight;
    private int roadX;  // X position where the road starts
    private int roadY;  // Y position where the road starts
    private int roadWidth;
    private int scrollSpeed;
    private int maxLane;
    private Random rand;

    private Image bananaImage;
    private int bananaWidth;
    private int bananaHeight;

    private GamePanel gamePanel;

    public Obstacles(int roadWidth, int roadX, int roadY, int laneHeight, int maxLane, int scrollSpeed, GamePanel gamePanel) {
        this.roadWidth = roadWidth;
        this.roadX = roadX;
        this.roadY = roadY;  // Pass the road's Y position
        this.laneHeight = laneHeight;
        this.maxLane = maxLane;
        this.scrollSpeed = scrollSpeed;
        this.gamePanel = gamePanel;

        obstacles = new ArrayList<>();
        rand = new Random();

        // Load the banana image and adjust its size to fit within the lane
        try {
            bananaImage = ImageIO.read(getClass().getResource("/banana.png"));
            bananaWidth = (int) (laneHeight * 0.8);  // Set the width to fit within the lane
            bananaHeight = bananaWidth;  // Set the height proportionally
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setScrollSpeed(int newScrollSpeed) {
        this.scrollSpeed = newScrollSpeed;
    }

    // Generate random obstacles within the lanes of the road
    public void generateObstacle() {
        int lane = rand.nextInt(maxLane - 2) + 1;  // Obstacles spawn only in intermediate lanes 2, 3, or 4
        int xPos = roadX + roadWidth;  // Start the obstacle just off the right edge of the road
        int yPos = roadY + (lane * laneHeight) + (laneHeight - obstacleHeight) / 2;  // Center obstacle in the lane
    
        // Check if the obstacle is a banana or a rectangular block
        boolean isBanana = rand.nextBoolean();  // 50% chance for banana
        
        if (isBanana) {
            int bananaYPos = roadY + (lane * laneHeight) + (laneHeight - bananaHeight) / 2;  // Center within lane
            obstacles.add(new Rectangle(xPos, bananaYPos, bananaWidth, bananaHeight));  // Add banana obstacle
        } else {
            // Rectangular block
            obstacles.add(new Rectangle(xPos, yPos, obstacleWidth, obstacleHeight));  // Add normal obstacle
        }
    }

    // Move obstacles with the screen
    public void moveObstacles() {
        for (int i = 0; i < obstacles.size(); i++) {
            Rectangle obstacle = obstacles.get(i);
            obstacle.x -= scrollSpeed;  // Move obstacle left as the road scrolls

            // Remove obstacle if it moves off the screen
            if (obstacle.x + obstacle.width < 0) {
                obstacles.remove(i);
                i--;  // Adjust index after removal
            }
        }
    }

// Check for collision with the truck
        public boolean checkCollision(int truckX, int truckY, int truckWidth, int truckHeight, boolean isImmune) {
            if (isImmune) {
                System.out.println("Vehicle is immune, skipping collision check.");
                return false;  // If the vehicle is immune, no collision is detected
            }

            Rectangle truckRect = new Rectangle(truckX, truckY, truckWidth - 10, truckHeight);  // -10 fixes gap between car and obstacle
            for (Rectangle obstacle : obstacles) {
                Rectangle obstacleRect = new Rectangle(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
                if (truckRect.intersects(obstacleRect)) {
                    if (obstacle.width == bananaWidth && obstacle.height == bananaHeight) {
                        // Banana collision detected, call the banana sliding logic in GamePanel
                        gamePanel.handleBananaCollision(); // Move car to neighboring lane on banana collision
                        return false;  // No crash, just sliding
                    } else {
                        // Regular obstacle collision
                        System.out.println("Collision detected with regular obstacle!");
                        return true;  // Collision detected
                    }
                }
            }
            return false;
        }

    

    // Draw the obstacles
    public void drawObstacles(Graphics g) {
        for (Rectangle obstacle : obstacles) {
            // Check if the obstacle is a banana (by comparing its size to the banana image dimensions)
            if (obstacle.width == bananaWidth && obstacle.height == bananaHeight) {
                // Draw the banana image at the obstacle's position
                g.drawImage(bananaImage, obstacle.x, obstacle.y, bananaWidth, bananaHeight, null);
            } else {
                // Draw regular rectangular obstacles
                g.setColor(Color.BLUE);  // Set obstacle color
                g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
            }
        }
    }
}
