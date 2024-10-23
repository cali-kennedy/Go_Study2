import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class MainMenu extends JFrame {
    private List<Question> questions;  // Store questions globally

    public MainMenu() {
        // Set up the main frame
        setTitle("Game Menu");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create buttons
        JButton inputQuestionsButton = new JButton("Input Questions");
        JButton startGameButton = new JButton("Start Game");

        // Set layout and add buttons
        setLayout(new GridLayout(2, 1));
        add(inputQuestionsButton);
        add(startGameButton);

        // Action for the "Input Questions" button
        inputQuestionsButton.addActionListener(e -> {
            // Open the Question Input screen
            QuestionInputScreen questionInputScreen = new QuestionInputScreen();
            questionInputScreen.setVisible(true);
            questions = questionInputScreen.getQuestions();  // Retrieve questions
        });

        // Action for the "Start Game" button
        startGameButton.addActionListener(e -> {
            // Start the game (pass questions to game logic)
            startGame();
        });
    }

    // Method to start the game
    private void startGame() {
        if (questions == null || questions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please input questions before starting the game.", "No Questions", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create a new JFrame for the game
        JFrame gameFrame = new JFrame("TMX Map Example with Camera, Zoom, and Collision Detection");
        TmxMapExample gamePanel = new TmxMapExample("resources/assets/med_5.tmx", questions);  // Pass questions
        gameFrame.setContentPane(gamePanel);
        gameFrame.setSize(800, 600);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setVisible(true);

        // Hide the main menu once the game starts
        this.setVisible(false);
    }

    public static void main(String[] args) {
        // Show the main menu
        MainMenu menu = new MainMenu();
        menu.setVisible(true);
    }
}
