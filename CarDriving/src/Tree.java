import java.awt.*;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Random;

public class Tree {
    private int x, y;
    private Image treeImage;
    private int width, height;

    public Tree(int x, int y) {
        this.x = x;
        this.y = y;
        
        // Load tree image
        try {
            treeImage = ImageIO.read(getClass().getResource("/tree.png")); // Ensure tree.png is in resources
            width = treeImage.getWidth(null); 
            height = treeImage.getHeight(null);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Tree image not found.");
        }
    }

    public void move(int speed) {
        x -= speed;
    }

    public void draw(Graphics g) {
        if (treeImage != null) {
            g.drawImage(treeImage, x, y, width, height, null);
        }
    }

    public int getX() {
        return x;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
