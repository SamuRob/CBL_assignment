import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.*;
import java.io.*;

public class Obstacles {
    private ArrayList<Rectangle> obstacles;
    private ArrayList<String> obstacleTypes; // Track the type of each obstacle ("banana", "bomb", "regular")
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
    private Image bombImage;
    private int bananaWidth;
    private int bananaHeight;
    private int bombWidth;
    private int bombHeight;

    private GamePanel gamePanel;

    public Obstacles(int roadWidth, int roadX, int roadY, int laneHeight, int maxLane, int scrollSpeed, GamePanel gamePanel) {
        this.roadWidth = roadWidth;
        this.roadX = roadX;
        this.roadY = roadY;
        this.laneHeight = laneHeight;
        this.maxLane = maxLane;
        this.scrollSpeed = scrollSpeed;
        this.gamePanel = gamePanel;

        obstacles = new ArrayList<>();
        obstacleTypes = new ArrayList<>();
        rand = new Random();

        // Load the banana image
        try {
            bananaImage = ImageIO.read(getClass().getResource("/banana.png"));
            bananaWidth = (int) (laneHeight * 0.8);
            bananaHeight = bananaWidth;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load the bomb image
        try {
            bombImage = ImageIO.read(getClass().getResource("/Bomb.png"));
            bombWidth = (int) (laneHeight * 0.8);
            bombHeight = bombWidth;
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
        int xPos = roadX + roadWidth;
        int yPos = roadY + (lane * laneHeight) + (laneHeight - obstacleHeight) / 2;

        // Randomly choose between banana, bomb, and regular obstacle
        int obstacleType = rand.nextInt(3);
        if (obstacleType == 0) {
            // Banana
            int bananaYPos = roadY + (lane * laneHeight) + (laneHeight - bananaHeight) / 2;
            obstacles.add(new Rectangle(xPos, bananaYPos, bananaWidth, bananaHeight));
            obstacleTypes.add("banana");
        } else if (obstacleType == 1) {
            // Bomb
            int bombYPos = roadY + (lane * laneHeight) + (laneHeight - bombHeight) / 2;
            obstacles.add(new Rectangle(xPos, bombYPos, bombWidth, bombHeight));
            obstacleTypes.add("bomb");
        } else {
            // Regular obstacle
            obstacles.add(new Rectangle(xPos, yPos, obstacleWidth, obstacleHeight));
            obstacleTypes.add("regular");
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
                obstacleTypes.remove(i);
                i--;  // Adjust index after removal
            }
        }
    }

    // Check for collision with the truck
    public boolean checkCollision(int truckX, int truckY, int truckWidth, int truckHeight, boolean isImmune) {
        if (isImmune) {
            System.out.println("Vehicle is immune, skipping collision check.");
            return false;
        }

        Rectangle truckRect = new Rectangle(truckX, truckY, truckWidth - 10, truckHeight);  // -10 fixes gap between car and obstacle
        for (int i = 0; i < obstacles.size(); i++) {
            Rectangle obstacle = obstacles.get(i);
            String type = obstacleTypes.get(i);
            Rectangle obstacleRect = new Rectangle(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
            if (truckRect.intersects(obstacleRect)) {
                if (type.equals("banana")) {
                    // Banana collision detected
                    gamePanel.handleBananaCollision();
                    return false;  // No crash, just sliding
                } else if (type.equals("bomb")) {
                    // Bomb collision detected
                    gamePanel.handleBombCollision();  // End the game
                    return true;  // Game over
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
        for (int i = 0; i < obstacles.size(); i++) {
            Rectangle obstacle = obstacles.get(i);
            String type = obstacleTypes.get(i);
            if (type.equals("banana")) {
                g.drawImage(bananaImage, obstacle.x, obstacle.y, bananaWidth, bananaHeight, null);
            } else if (type.equals("bomb")) {
                g.drawImage(bombImage, obstacle.x, obstacle.y, bombWidth, bombHeight, null);
            } else {
                g.setColor(Color.BLUE);  // Regular obstacle
                g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
            }
        }
    }
}
