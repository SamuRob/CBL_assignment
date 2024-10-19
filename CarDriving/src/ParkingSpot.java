import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class ParkingSpot {
    private ArrayList<Rectangle> parkingSpots;
    private int spotWidth = 90;
    private int spotHeight = 50;
    private int roadWidth;
    private int roadX;
    private int roadY;
    private int laneHeight;
    private Random random;
    private int maxLane = 4;

    private int distanceBeforeParking = 150;

   // private int[] laneYposition;



    private boolean nextSpotLeft = true;
    private Rectangle parkingRegion;  // The parking region

    private boolean playerParked = false;

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
        this.maxLane = maxLane;
        parkingSpots = new ArrayList<>();
        random = new Random();

        // Initialize parking lanes, offset from the main road
        parkingLanesY = new int[parkingLaneCount];
        for (int i = 0; i < parkingLaneCount; i++) {
            parkingLanesY[i] = roadY +  (i * laneHeight * 2);  // Adjust lane position
        }

        // Initialize parking region (this will be updated based on parking spots)
        parkingRegion = new Rectangle();
    }


    public void generateParkingSpot() {
        nextSpotLeft = false;  // Only generate on right side for now
    
        // Randomly select a parking lane
       // int laneIndex = random.nextInt(maxLane);
        
        int laneIndex = random.nextInt(parkingLanesY.length);

        // Use laneYPositions to determine the Y-position for the parking spot
        int yPos = parkingLanesY[laneIndex];  // Adjust Y position based on the lane

        int xPos = nextSpotLeft ? roadX - spotWidth : roadX + roadWidth;
        parkingSpots.add(new Rectangle(xPos, yPos, spotWidth, spotHeight));
    
        // Update the parking region to allow the vehicle to move into it
        parkingRegion.setBounds(xPos - 20, yPos - 20, spotWidth + 40, spotHeight + 40);
    }
    
    

    // Generate parking spots in lanes
   /*  public void generateParkingSpot() {
        
        
        nextSpotLeft = false;  // Only generate on right side of da car
        
        int selection = random.nextInt(2) + 1; // choses 1 or 2

        int yPos;
        int xPos;


        if (selection == 1) {

            GamePanel gamePanel = new GamePanel();
            int maxLane = gamePanel.getMaxLane();
            yPos = 200 / maxLane + 100;
            xPos = nextSpotLeft ? roadX - spotWidth : roadX + roadWidth;
            // Create the parking spot in the selected lane
             parkingSpots.add(new Rectangle(xPos, yPos, spotWidth, spotHeight));

            // Update the parking region to allow vehicle to move into it
            parkingRegion.setBounds(xPos - 20, yPos - 20, spotWidth + 40, spotHeight + 40);
        }
        else{ // BottomLane parking

            GamePanel gamePanel = new GamePanel();
            int maxLane = gamePanel.getMaxLane();
            yPos = 200 / maxLane + 350;
            xPos = nextSpotLeft ? roadX - spotWidth : roadX + roadWidth;
            // Create the parking spot in the selected lane
             parkingSpots.add(new Rectangle(xPos, yPos, spotWidth, spotHeight));

            // Update the parking region to allow vehicle to move into it
            parkingRegion.setBounds(xPos - 20, yPos - 20, spotWidth + 40, spotHeight + 40);
        }
        
    }*/

    // Move parking spots with the screen
    public void moveParkingSpots(int scrollSpeed) {
        for (int i = 0; i < parkingSpots.size(); i++) {
            Rectangle spot = parkingSpots.get(i);
            spot.x -= scrollSpeed;  // Scroll spots left with the road

            // Update the parking region to follow the parking spots
            parkingRegion.setBounds(spot.x - 20, spot.y - 20, spot.width + 40, spot.height + 40);

            // Remove spot if it moves off the screen
            if (spot.x + spotWidth < 0) {
                parkingSpots.remove(i);
                i--;  // Adjust index after removal
            }
        }
    }

    /*public boolean isPlayerParked(int truckX, int truckY, int truckWidth, int truckHeight){
        for (Rectangle spot : parkingSpots) {
            if (new Rectangle(truckX, truckY, truckWidth, truckHeight).intersects(spot)) {
                playerParked = true;
                return true;  // Player is parked
            }
        }
        playerParked = false;  // Player is no longer parked
        return false;
        }
    
        public void resetParkingStatus(){
            playerParked = false;
        }
*/
    public void resetParkingStatus() {
        playerParked = false;  // Reset the parking status
    }


   /*  public boolean isPlayerParked(int truckX, int truckY, int truckWidth, int truckHeight) {
        for (Rectangle spot : parkingSpots) {
            // Use contains to ensure the truck is fully within the parking spot
            if (spot.contains(truckX, truckY, truckWidth, truckHeight)) {
                playerParked = true;
                return true;  // Player is fully parked inside the spot
            }
        }
        playerParked = false;  // Player is no longer parked
        return false;
    }
        */

       /*(int truckX, int truckY, int truckWidth, int truckHeight) {
            for (Rectangle spot : parkingSpots) {
                Rectangle parkingBuffer = new Rectangle(
                    spot.x,
                    spot.y,
                    spot.width,
                    spot.height
                );
                if (parkingBuffer.contains(truckX, truckY, truckWidth, truckHeight)) {
                    playerParked = true;
                    return true;
                }
            }
            playerParked = false;
            return false;
        }*/
        public boolean isPlayerParked(int truckX, int truckY, int truckWidth, int truckHeight) {
            for (Rectangle spot : parkingSpots) {
                // Check if the truck is completely inside the parking spot
                if (spot.contains(truckX, truckY-100) && spot.contains(truckX + truckWidth, truckY + truckHeight-100)) {
                    playerParked = true;
                    return true;
                }
            }
            playerParked = false;
            return false;
        }
        
        

       /*  public boolean isPlayerParked(int truckX, int truckY, int truckWidth, int truckHeight) {
            for (Rectangle spot : parkingSpots) {
                // Add a small buffer (e.g., 10 pixels) to allow lenient parking
                Rectangle parkingBuffer = new Rectangle(
                    spot.x - 10, 
                    spot.y - 10, 
                    spot.width + 20, 
                    spot.height + 20
                );
                if (parkingBuffer.contains(truckX, truckY, truckWidth, truckHeight)) {
                    playerParked = true;
                    return true;  // Player is fully parked inside the spot
                }
            }
            playerParked = false;  // Player is no longer parked
            return false;
        }
        */


// Method to check if the truck can enter the parking region
    public boolean canEnterParkingRegion(int truckX, int truckY, int truckWidth, int truckHeight) {
        Rectangle truckRect = new Rectangle(truckX, truckY, truckWidth, truckHeight);
        if (truckRect.intersects(parkingRegion)) {
            playerParked = true;
            return true;  // Player can enter the parking region
        }
        return false;
    }

/* 
    // Check if the truck can enter the parking region
    public boolean canEnterParkingRegion(int truckX, int truckY, int truckWidth, int truckHeight) {
        Rectangle truckRect = new Rectangle(truckX, truckY, truckWidth, truckHeight);
        return truckRect.intersects(parkingRegion);
    }

        // Check if the vehicle is successfully parked within a parking spot
        public boolean checkParking(int truckX, int truckY, int truckWidth, int truckHeight) {
            for (Rectangle spot : parkingSpots) {
                if (new Rectangle(truckX, truckY, truckWidth, truckHeight).intersects(spot)) {
                    return true;  // Truck is parked correctly
                }
            }
            return false;
        }
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


// Draw parking lanes and spots
public void drawParkingSpots(Graphics g) {
        // Draw parking lanes
      //  g.setColor(Color.BLUE);  // Use blue for parking lanes
        for (int laneY : parkingLanesY) {
            g.drawLine(roadX - spotWidth, laneY, roadX + roadWidth + spotWidth, laneY);
        }
        // Draw parking spots
        g.setColor(Color.GREEN);  // Green for parking spots
        for (Rectangle spot : parkingSpots) {
            g.fillRect(spot.x, spot.y+100, spot.width, spot.height);
        }

        // Draw the parking region
        g.setColor(Color.YELLOW);
        g.drawRect(parkingRegion.x, parkingRegion.y, parkingRegion.width, parkingRegion.height);
    }
}