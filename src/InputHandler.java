import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import java.util.List;

public class InputHandler extends KeyAdapter {
    private static final int MOVE_DISTANCE = 15;  // Movement distance
    private static final int COOLDOWN_PERIOD = 100;  // Cooldown period in milliseconds

    private Character character;
    private TmxParser parser;
    private JFrame parentFrame;
    private long lastMoveTime;
    private List<Question> questions;  // List of questions
    private int currentQuestionIndex = 0;  // Track current question

    public InputHandler(Character character, TmxParser parser, JFrame parentFrame, List<Question> questions) {
        this.character = character;
        this.parser = parser;
        this.parentFrame = parentFrame;
        this.questions = questions;  // Inject questions
        this.lastMoveTime = 0;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMoveTime >= COOLDOWN_PERIOD) {
            handleMovement(e.getKeyCode());
            lastMoveTime = currentTime;
        }
    }

    private void handleMovement(int keyCode) {
        Question currentQuestion = getNextQuestion();
        switch (keyCode) {
            case KeyEvent.VK_W:
                character.move(0, -MOVE_DISTANCE, parser, parentFrame, currentQuestion, questions);
                break;
            case KeyEvent.VK_S:
                character.move(0, MOVE_DISTANCE, parser, parentFrame, currentQuestion, questions);
                break;
            case KeyEvent.VK_A:
                character.move(-MOVE_DISTANCE, 0, parser, parentFrame, currentQuestion, questions);
                break;
            case KeyEvent.VK_D:
                character.move(MOVE_DISTANCE, 0, parser, parentFrame, currentQuestion, questions);
                break;
        }
    }

    private Question getNextQuestion() {
        // Return the next question in the list
        if (questions != null && !questions.isEmpty()) {
            Question question = questions.get(currentQuestionIndex);
            currentQuestionIndex = (currentQuestionIndex + 1) % questions.size();  // Loop back to the start
            return question;
        }
        return null;  // Return null if no questions are available
    }
}
