import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

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

    public Obstacles(int roadWidth, int roadX, int roadY, int laneHeight, int maxLane, int scrollSpeed) {
        this.roadWidth = roadWidth;
        this.roadX = roadX;
        this.roadY = roadY;  // Pass the road's Y position
        this.laneHeight = laneHeight;
        this.maxLane = maxLane;
        this.scrollSpeed = scrollSpeed;

        obstacles = new ArrayList<>();
        rand = new Random();
    }

    // Generate random obstacles within the lanes of the road
    public void generateObstacle() {
        int lane = rand.nextInt(maxLane);  // Random lane number
        int xPos = roadX + roadWidth;  // Start the obstacle just off the right edge of the road
        int yPos = roadY + (lane * laneHeight) + (laneHeight - obstacleHeight) / 2;  // Center obstacle in the lane

        // Add a new obstacle to the list
        obstacles.add(new Rectangle(xPos, yPos, obstacleWidth, obstacleHeight));
    }

    // Move obstacles with the screen
    public void moveObstacles() {
        for (int i = 0; i < obstacles.size(); i++) {
            Rectangle obstacle = obstacles.get(i);
            obstacle.x -= scrollSpeed;  // Move obstacle left as the road scrolls

            // Remove obstacle if it moves off the screen
            if (obstacle.x + obstacleWidth < 0) {
                obstacles.remove(i);
                i--;  // Adjust index after removal
            }
        }
    }

    // Check for collisions with the truck
    public boolean checkCollision(int truckX, int truckY, int truckWidth, int truckHeight) {
        Rectangle truckRect = new Rectangle(truckX, truckY, truckWidth, truckHeight);
        for (Rectangle obstacle : obstacles) {
            if (truckRect.intersects(obstacle)) {
                return true;  // Collision detected
            }
        }
        return false;
    }

    // Draw the obstacles
    public void drawObstacles(Graphics g) {
        g.setColor(Color.BLUE);  // Set obstacle color
        for (Rectangle obstacle : obstacles) {
            g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
        }
    }
}
