import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


// The main game frame class
public class GameFrame extends JFrame {

    private GamePanel gamePanel;
    private JButton startButton;
    private JButton instructionButton;
    private boolean gameStarted = false;

    public GameFrame(){
        super("Hizmet delivery game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Closes when press x in right corner
        setLayout(new BorderLayout());
        setResizable(false); // Stops player from resizing window
        
        gamePanel = new GamePanel();
        add(gamePanel, BorderLayout.CENTER);

        startButton = new JButton("Start Game:");

        instructionButton = new JButton("Instructions");
        
        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();

        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameStarted) {
                    gamePanel.moveVehicle(e.getKeyCode());
                }
            }
        });

        
        setVisible(true); // Makes window visible to the player
    }

    public static void main(String[] args){
        new GameFrame();
    }
}
