import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;

public class Obstacles {
    private ArrayList<Rectangle> obstacles;
    private ArrayList<String> obstacleTypes; // Track the type of each obstacle ("banana", "bomb", "regular")
    private ArrayList<BufferedImage> obstacleImages; // Track the image for each obstacle
    private ArrayList<Float> obstacleSpeeds; // Speed for each obstacle to simulate acceleration
    private int obstacleWidth = 60;
    private int obstacleHeight = 40;
    private int laneHeight;
    private int roadX; // X position where the road starts
    private int roadY; // Y position where the road starts
    private int roadWidth;
    private int scrollSpeed;
    private int maxLane;
    private Random rand;
    private int obstacleGenerationDelay = 1000;
    private int obstacleGenerationCounter = 0; // Counter to track delay
    private double bananaProbability = 0.2;

    private ArrayList<BufferedImage> carImages; // List of car images to randomly select from
    private Random random;

    private int minObstacleSpacing = 200; // Minimum space between obstacles
    private Image bananaImage;
    private Image bombImage;
    private int bananaWidth;
    private int bananaHeight;
    private int bombWidth;
    private int bombHeight;

    private final int minObstacleGenerationDelay = 300; // Minimum delay for obstacle generation

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
        obstacleImages = new ArrayList<>();
        obstacleSpeeds = new ArrayList<>();
        rand = new Random();
        carImages = new ArrayList<>();
        random = new Random();

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

        // Load the car images
        try {
            carImages.add(ImageIO.read(getClass().getResource("/car1.png")));
            carImages.add(ImageIO.read(getClass().getResource("/car2.png")));
            carImages.add(ImageIO.read(getClass().getResource("/car3.png")));
            carImages.add(ImageIO.read(getClass().getResource("/car4.png")));
            carImages.add(ImageIO.read(getClass().getResource("/car5.png")));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("One or more car images could not be loaded.");
        }
    }

    public void increaseBananaProbability(int level) {
        bananaProbability = Math.min(0.5, bananaProbability + level * 0.01);
        // Max 50% banana spawn, original level is 20%
    }

    public BufferedImage getRandomCarImage() {
        return carImages.get(random.nextInt(carImages.size()));
    }

    public void setScrollSpeed(int newScrollSpeed) {
        this.scrollSpeed = newScrollSpeed;
    }

    public void generateObstacle() {
        int lane = rand.nextInt(maxLane - 2) + 1; // Random lane between 1 and maxLane - 1
        int xPos = roadX + roadWidth + rand.nextInt(100) + 150; // Starting X position
        int yPos = roadY + (lane * laneHeight) + (laneHeight - obstacleHeight) / 2;

        // Check if the new obstacle is far enough from the last obstacle
        boolean isFarEnough = true;
        for (Rectangle existingObstacle : obstacles) {
            if (Math.abs(existingObstacle.x - xPos) < minObstacleSpacing) {
                isFarEnough = false;
                break;
            }
        }

        // Only add the obstacle if it's far enough from others
        if (isFarEnough) {
            double randomValue = rand.nextDouble(); // Generate a random value between 0.0 and 1.0

            if (randomValue < bananaProbability) {
                // Spawn a banana obstacle
                obstacles.add(new Rectangle(xPos, yPos, bananaWidth, bananaHeight));
                obstacleTypes.add("banana");
                obstacleImages.add(null); // No image needed for banana
                obstacleSpeeds.add((float) scrollSpeed); // Fixed speed for banana
            } else if (randomValue < bananaProbability + 0.2) {
                // Spawn a bomb obstacle (20% probability if banana didn't spawn)
                obstacles.add(new Rectangle(xPos, yPos, bombWidth, bombHeight));
                obstacleTypes.add("bomb");
                obstacleImages.add(null); // No image needed for bomb
                obstacleSpeeds.add((float) scrollSpeed); // Fixed speed for bomb
            } else {
                // Spawn a regular car obstacle
                BufferedImage carImage = getRandomCarImage();
                obstacles.add(new Rectangle(xPos, yPos, obstacleWidth, obstacleHeight));
                obstacleTypes.add("regular");
                obstacleImages.add(carImage);
                obstacleSpeeds.add((float) scrollSpeed); // Starting speed for car obstacles
            }
        }
    }

    public void generateObstaclesIfReady() {
        obstacleGenerationCounter += scrollSpeed;

        if (obstacleGenerationCounter >= obstacleGenerationDelay) {
            generateObstacle();
            obstacleGenerationCounter = 0;
        }
    }

    public void increaseDifficulty(int level) {
        //obstacleGenerationDelay = Math.max(300, 1000 - (level * 50));
        int newDelay = Math.max(300, 1000 - (level * 50));
        setObstacleGenerationDelay(newDelay);
    }

    public void setObstacleGenerationDelay(int delay) {
        obstacleGenerationDelay = delay;
    }
    

    // Move obstacles with acceleration
    public void moveObstacles(int playerX, int playerY) {
        for (int i = 0; i < obstacles.size(); i++) {
            Rectangle obstacle = obstacles.get(i);
            String type = obstacleTypes.get(i);

            if (type.equals("regular")) {
                // Get the current speed for this obstacle
                float currentSpeed = obstacleSpeeds.get(i);

                // Adjust the speed based on distance to player's car
                if (obstacle.x > playerX) {
                    currentSpeed += 0.2f; // Accelerate gradually
                }
                obstacleSpeeds.set(i, currentSpeed); // Update the speed in obstacleSpeeds list

                obstacle.x -= currentSpeed; // Move obstacle with updated speed
            } else {
                obstacle.x -= scrollSpeed; // Move other obstacles with the standard scroll speed
            }

            // Remove obstacle if it moves off the screen
            if (obstacle.x + obstacle.width < 0) {
                obstacles.remove(i);
                obstacleTypes.remove(i);
                obstacleImages.remove(i);
                obstacleSpeeds.remove(i);
                i--;
            }
        }
    }

    // Check for collision with the truck
    public boolean checkCollision(int truckX, int truckY, int truckWidth, int truckHeight, boolean isImmune) {
        if (isImmune) {
            System.out.println("Vehicle is immune, skipping collision check.");
            return false;
        }

        Rectangle truckRect = new Rectangle(truckX, truckY, truckWidth - 10, truckHeight); // -10 fixes gap between car and obstacle
        for (int i = 0; i < obstacles.size(); i++) {
            Rectangle obstacle = obstacles.get(i);
            String type = obstacleTypes.get(i);
            Rectangle obstacleRect = new Rectangle(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
            if (truckRect.intersects(obstacleRect)) {
                if (type.equals("banana")) {
                    gamePanel.handleBananaCollision(); // Banana collision detected
                    return false; // No crash, just sliding
                } else {
                    gamePanel.handleCollision(); // Collision with bomb or regular obstacle
                    System.out.println("Collision detected with " + type + " obstacle! Game Over.");
                    return true; // Game over
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
                BufferedImage carImage = obstacleImages.get(i);
                if (carImage != null) {
                    g.drawImage(carImage, obstacle.x, obstacle.y, obstacle.width, obstacle.height, null);
                } else {
                    g.setColor(Color.BLUE);
                    g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
                }
            }
        }
    }
}
