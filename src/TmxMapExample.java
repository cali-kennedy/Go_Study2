import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TmxMapExample extends JPanel {
    private TmxParser parser;
    private TmxRenderer renderer;
    private Character character;
    private InputHandler inputHandler;
    private Camera camera;
    private List<Question> questions;  // Questions for the fight

    public TmxMapExample(String tmxFilePath, List<Question> questions) {  // Accept questions
        this.questions = questions;
        initializeGame(tmxFilePath);
        startGameLoop();
        setUpInputHandler();  // Use InputHandler for key management
    }

    private void initializeGame(String tmxFilePath) {
        parser = new TmxParser(tmxFilePath);
        renderer = new TmxRenderer(parser);

        // Initialize the character
        character = new Character("resources/assets/rabbit.png", 50, 50, 32, 32);

        // Initialize the camera with the screen size and zoom level
        camera = new Camera(800, 600, 2.0f);  // 800x600 viewport, 2x zoom

        // Initialize input handler
        inputHandler = new InputHandler(character, parser, (JFrame) SwingUtilities.getWindowAncestor(this), questions);
    }

    private void startGameLoop() {
        Timer gameTimer = new Timer(100, e -> repaint());
        gameTimer.start();
    }

    private void setUpInputHandler() {
        setFocusable(true);
        addKeyListener(inputHandler);  // Add the input handler as a key listener
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        camera.update(character, parser.getMapWidth() * parser.getTileWidth(), parser.getMapHeight() * parser.getTileHeight());
        camera.applyTransform(g2d);

        renderer.render(g2d);
        character.draw(g2d);

        drawHealth(g);
        drawXP(g);
    }

    private void drawHealth(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Health: " + character.getHealth(), 10, 20);
    }

    private void drawXP(Graphics g) {
        g.setColor(Color.BLUE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("XP: " + character.getXP(), 10, 40);
    }
}
