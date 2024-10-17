import java.awt.*;

public class Item {
    public int x, y, width, height;
    private String type;

    public Item(int x, int y, int width, int height, String type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public String getType() {
        return type;
    }
}
