import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.*;

public class ParkingSpot {
    private ArrayList<Rectangle> parkingSpots;
    private int spotWidth;
    private int spotHeight;
    private int laneHeight;

    private int roadWidth;
    private int roadX;
    private int roadY;

    private Random random;
    private int maxLane = 5;
    private int roadHeight;

    private int distanceBeforeParking = 150;

    private boolean nextSpotLeft = true;
    private Rectangle parkingRegion;  // The parking region

    private boolean playerParked = false;
    private BufferedImage parkingImage;

    // Define parking lanes
    private int[] parkingLanesY;  // Array for y-coordinates of parking lanes
    private int parkingLaneCount = 3;  // Number of parking lanes

    public boolean isNextSpotLeft() {
        return nextSpotLeft;
    }

    public ParkingSpot(int roadWidth, int roadX, int roadY, int laneHeight) {
        this.roadWidth = roadWidth;
        this.roadX = roadX;
        this.roadY = roadY;
        this.laneHeight = laneHeight;
        this.roadHeight = roadHeight;
    
        // Set the parking spot height equal to the lane height
        this.spotHeight = laneHeight;  
        this.spotWidth = spotHeight * 2;  // Keep the width proportional to the height
    
        parkingSpots = new ArrayList<>();
        random = new Random();
        parkingRegion = new Rectangle();
    
        // Initialize parking lanes for lane 1 to lane 4
        parkingLanesY = new int[4];  // Correct length for 4 lanes
        parkingLanesY[0] = roadY;  // Lane 1 Y-position
        parkingLanesY[1] = roadY + laneHeight;  // Lane 2 Y-position
        parkingLanesY[2] = roadY + (2 * laneHeight);  // Lane 3 Y-position
        parkingLanesY[3] = roadY + (3 * laneHeight);  // Lane 4 Y-position

        try {
            parkingImage = ImageIO.read(getClass().getResource("/ParkingSpot.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Move parking spots with the screen
    public void moveParkingSpots(int scrollSpeed) {
        for (int i = 0; i < parkingSpots.size(); i++) {
            Rectangle spot = parkingSpots.get(i);
            spot.x -= scrollSpeed;  // Move spot with the road scrolling

            if (spot.x + spotWidth < 0) {
                parkingSpots.remove(i);
                i--;  // Adjust index after removal
            }
        }
    }

    /**
     * Removes a parking spot from the list when the truck is fully parked in it.
     * The method checks if the truck's bounding box is fully contained within
     * a parking spot's bounding box. If so, it removes that parking spot from
     * the list of parking spots.
     * @param truckX the X-coordinate of the truck's top-left corner
     * @param truckY the Y-coordinate of the truck's top-left corner
     * @param truckWidth the width of the truck
     * @param truckHeight the height of the truck
     */
    public void removeParkedSpot(int truckX, int truckY, int truckWidth, int truckHeight) {
        for (int i = 0; i < parkingSpots.size(); i++) {
            Rectangle spot = parkingSpots.get(i);
            // Check if the car is parked in this spot
            if (spot.contains(truckX, truckY) && spot.contains(truckX + truckWidth, truckY + truckHeight)) {
                parkingSpots.remove(i);  // Remove the spot that the car is parked in
                break;
            }
        }
    }
    
    


    /**
     * Checks if any parking spot is approaching the truck,
     *  given the truck's X-coordinate and the current scroll speed.
     * A parking spot is considered approaching if it is within 1.5 seconds
     *  of travel time ahead of the truck.
     * @param truckX the X-coordinate of the truck's top-left corner
     * @param scrollSpeed the current road scroll speed
     * @return true if a parking spot is approaching, false otherwise
     */
    public boolean isParkingSpotApproaching(int truckX, int scrollSpeed) {
        // Calculate the distance in pixels the truck will cover in 1.5 seconds
        int distanceIn1_5Seconds = (int) (scrollSpeed * 30 * 1);  // 30 is because the game updates every 30ms
    
        // Check if any parking spot is within this distance ahead of the truck
        for (Rectangle spot : parkingSpots) {
            if (spot.x - truckX <= distanceIn1_5Seconds && spot.x > truckX) {
                return true;  // Parking spot is approaching
            }
        }
        return false;  // No parking spot approaching
    }

    public void resetParkingStatus() {
        playerParked = false;  // Reset the parking status
    }

    /**
     * Checks if the player's truck is fully parked within any of the parking spots.
     * The method verifies if the entire bounding box of the truck is contained
     * within a parking spot, considering a reduction factor of 0.8 for both width
     * and height to allow slight leeway in parking precision.
     *
     * @param truckX the X-coordinate of the truck's top-left corner
     * @param truckY the Y-coordinate of the truck's top-left corner
     * @param truckWidth the width of the truck
     * @param truckHeight the height of the truck
     * @return true if the truck is fully parked inside a parking spot, false otherwise
     */
    public boolean isPlayerParked(int truckX, int truckY, int truckWidth, int truckHeight) {
        for (Rectangle spot : parkingSpots) {
            // Check if the car's entire bounding box is inside the parking spot
            if (spot.contains(truckX, truckY) && spot.contains(truckX + (int) (truckWidth*0.8), truckY + (int) (truckHeight*0.8))) {
                playerParked = true;
                return true;  // The player is fully parked
            }
        }
        playerParked = false;
        return false;  // The player is not parked
    }


    // Method to check if the truck can enter the parking region
    public boolean canEnterParkingRegion(int truckX, int truckY, int truckWidth, int truckHeight) {
        Rectangle truckRect = new Rectangle(truckX, truckY, truckWidth, truckHeight);
        if (truckRect.intersects(parkingRegion)) {
            playerParked = true;
            return true;  // Player can enter the parking region
        }
        return false;
    }


    /**
     * Checks if any parking spot is approaching the truck, given the truck's X-coordinate.
     * A parking spot is considered approaching if it is within the distanceBeforeParking
     * (currently set to 2300 pixels) before it reaches the truck.
     * @param truckX the X-coordinate of the truck's top-left corner
     * @return true if a parking spot is approaching, false otherwise
     */
    public boolean isSpotApproaching(int truckX) {
      for (Rectangle spot : parkingSpots) {
            // Check if the parking spot is within the required distance before it reaches the truck
            if (spot.x - truckX <= distanceBeforeParking && spot.x > truckX) {
                System.out.println("Parking spot is approaching");
                return true;
            }
        }
        return false;
    }

    public void removeCurrentSpot() {
        if (!parkingSpots.isEmpty()) {
            parkingSpots.remove(0);  // Assuming the first parking spot is the active one
        }
    }


    /**
     * Generates a new parking spot in either lane 1 or lane 5.
     * The parking spot's position is determined randomly and 
     * aligned with the top of the selected lane. It is then 
     * added to the list of parking spots and the parking region 
     * is updated accordingly.
     */
    public void generateParkingSpot() {
        nextSpotLeft = false;
    
        int laneIndex = random.nextBoolean() ? 0 : 4;  // Only generate in lane 1 or 5
        int yPos = roadY + (laneIndex * laneHeight);  
        int xPos = nextSpotLeft ? roadX - spotWidth : roadX + roadWidth;
    
        parkingSpots.add(new Rectangle(xPos, yPos, spotWidth, spotHeight));
    
        // Update parking region
        parkingRegion.setBounds(xPos - 20, yPos - 20, spotWidth + 40, spotHeight + 40);
    }
    

    /**
     * Renders the parking spots on the screen using the given Graphics object.
     * If a parking image is available, it is drawn for each parking spot;
     * otherwise, green rectangles are used to represent the spots.
     * Optional visual aids such as anticipation arrows and parking region
     * outlines can also be drawn.
     *
     * @param g the Graphics object used for drawing
     */
    public void drawParkingSpots(Graphics g) {
        for (Rectangle spot : parkingSpots) {
            if (parkingImage != null) {
                g.drawImage(parkingImage, spot.x, spot.y, spot.width, spot.height, null);
            }
            else {
                g.setColor(Color.GREEN);  // Green for parking spots
                g.fillRect(spot.x, spot.y, spot.width, spot.height); 
                // Render parking spots to fit the lane
            }
        
        }

        // Draw the parking region (optional visual aid)
        //g.setColor(Color.YELLOW);
        //g.drawRect(parkingRegion.x, parkingRegion.y, parkingRegion.width, parkingRegion.height);
    }

    public boolean isPlayerInParkingLane(int truckY) {
        return (truckY == parkingLanesY[0] 
            || truckY == parkingLanesY[3]);  // Lane 1 and 5 are parking lanes
    }


}