import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

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
            spot.x -= scrollSpeed;  // Move spot with the road scrolling

            if (spot.x + spotWidth < 0) {
                parkingSpots.remove(i);
                i--;  // Adjust index after removal
            }
        }
    }

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
    
    

    public boolean isParkingSpotApproaching(int truckX, int scrollSpeed) {
        // Calculate the distance in pixels the truck will cover in 1.5 seconds
        int distanceIn1_5Seconds = (int) (scrollSpeed * 30 * 1.5);  // 30 is because the game updates every 30ms
    
        // Check if any parking spot is within this distance ahead of the truck
        for (Rectangle spot : parkingSpots) {
            if (spot.x - truckX <= distanceIn1_5Seconds && spot.x > truckX) {
                return true;  // Parking spot is approaching
            }
        }
        return false;  // No parking spot approaching
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
        /*(int truckX, int truckY, int truckWidth, int truckHeight) {
            for (Rectangle spot : parkingSpots) {
                // Check if the truck is completely inside the parking spot
                if (spot.contains(truckX, truckY-100) && spot.contains(truckX + truckWidth, truckY + truckHeight-100)) {
                    playerParked = true;
                    return true;
                }
            }
            playerParked = false;
            return false;
        }*/

        public boolean isPlayerParked(int truckX, int truckY, int truckWidth, int truckHeight) {
            for (Rectangle spot : parkingSpots) {
                // Check if the car's entire bounding box is inside the parking spot
                if (spot.contains(truckX, truckY) && spot.contains(truckX + truckWidth, truckY + truckHeight)) {
                    playerParked = true;
                    return true;  // The player is fully parked
                }
            }
            playerParked = false;
            return false;  // The player is not parked
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
/*public void generateParkingSpot() {
    nextSpotLeft = false;  // You can set this based on your game's requirement (left/right parking)

    // Only choose between lane 1 (index 0) and lane 4 (index 3)
    int laneIndex = random.nextBoolean() ? 0 : 3;  // Randomly choose either lane 1 or lane 4

    // Use the `parkingLanesY` array to get the correct Y position for lane 1 or lane 4
    int yPos = parkingLanesY[laneIndex];  // Get the Y position based on the lane

    int xPos = nextSpotLeft ? roadX - spotWidth : roadX + roadWidth;
    parkingSpots.add(new Rectangle(xPos, yPos, spotWidth, spotHeight));

    // Update the parking region to allow the vehicle to move into it
    parkingRegion.setBounds(xPos - 20, yPos - 20, spotWidth + 40, spotHeight + 40);
}*/
public void removeCurrentSpot() {
    if (!parkingSpots.isEmpty()) {
        parkingSpots.remove(0);  // Assuming the first parking spot is the active one
    }
}


    // Adjust parking spot generation logic
    public void generateParkingSpot() {
        nextSpotLeft = false;
    
        int laneIndex = random.nextBoolean() ? 0 : 4;  // Only generate in lane 1 or 5
        int yPos = roadY + (laneIndex * laneHeight);  // Align the top of the parking spot with the lane's top
        int xPos = nextSpotLeft ? roadX - spotWidth : roadX + roadWidth;
    
        parkingSpots.add(new Rectangle(xPos, yPos, spotWidth, spotHeight));
    
        // Update parking region
        parkingRegion.setBounds(xPos - 20, yPos - 20, spotWidth + 40, spotHeight + 40);
    }
    



// Draw parking lanes and spots
    public void drawParkingSpots(Graphics g) {
        g.setColor(Color.GREEN);  // Green for parking spots
        for (Rectangle spot : parkingSpots) {
            g.fillRect(spot.x, spot.y, spot.width, spot.height);  // Render parking spots to fit the lane
        }

        // Draw the parking region (optional visual aid)
        g.setColor(Color.YELLOW);
        g.drawRect(parkingRegion.x, parkingRegion.y, parkingRegion.width, parkingRegion.height);
    }

}