import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

public class FightScreen extends JDialog {
    private Character character;
    private TmxParser.MapObject enemy;
    private JLabel playerHealthLabel;
    private JLabel enemyHealthLabel;
    private int playerHealth;
    private int enemyHealth = 100;
    private Random random = new Random();
    private Timer animationTimer;
    private TmxParser.Animation enemyAnimation;
    private int currentFrame = 0;
    private boolean fightWon = false;
    private TmxParser parser;
    private List<Question> questions;
    private int currentQuestionIndex = 0;

    public FightScreen(JFrame parent, Character character, TmxParser.MapObject enemy, TmxParser parser, List<Question> questions) {
        super(parent, "Fight Screen", true);
        this.character = character;
        this.enemy = enemy;
        this.parser = parser;
        this.playerHealth = character.getHealth();
        this.questions = questions;
        this.enemyAnimation = loadEnemyAnimation();

        setupUI(parent);
        startAnimationTimer();
    }

    private TmxParser.Animation loadEnemyAnimation() {
        TmxParser.Animation animation = parser.getAnimations().get(enemy.gid);
        if (animation == null) {
            throw new IllegalStateException("No animation found for enemy GID: " + enemy.gid);
        }
        return animation;
    }

    private void setupUI(JFrame parent) {
        setLayout(new BorderLayout());
        setSize(500, 400);
        setLocationRelativeTo(parent);

        JPanel fightPanel = createFightPanel();
        add(fightPanel, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createFightPanel() {
        JPanel fightPanel = new JPanel(new GridLayout(1, 2));
        fightPanel.add(createPlayerPanel());
        fightPanel.add(createEnemyPanel());
        return fightPanel;
    }

    private JPanel createPlayerPanel() {
        JPanel playerPanel = new JPanel(new BorderLayout());
        JLabel playerLabel = new JLabel("Player", SwingConstants.CENTER);
        playerLabel.setFont(new Font("Arial", Font.BOLD, 18));

        playerHealthLabel = new JLabel("Health: " + playerHealth, SwingConstants.CENTER);
        playerPanel.add(playerLabel, BorderLayout.NORTH);
        playerPanel.add(new JLabel(new ImageIcon("resources/assets/rabbit.png")), BorderLayout.CENTER);
        playerPanel.add(playerHealthLabel, BorderLayout.SOUTH);
        return playerPanel;
    }

    private JPanel createEnemyPanel() {
        JPanel enemyPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                renderEnemyAnimation(g);
            }
        };

        JLabel enemyLabel = new JLabel(enemy.name, SwingConstants.CENTER);
        enemyLabel.setFont(new Font("Arial", Font.BOLD, 18));
        enemyHealthLabel = new JLabel("Health: " + enemyHealth, SwingConstants.CENTER);

        enemyPanel.add(enemyLabel, BorderLayout.NORTH);
        enemyPanel.add(enemyHealthLabel, BorderLayout.SOUTH);
        return enemyPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton runButton = createRunButton();
        JButton attackButton = createAttackButton();
        JButton defendButton = createDefendButton();

        buttonPanel.add(runButton);
        buttonPanel.add(attackButton);
        buttonPanel.add(defendButton);

        return buttonPanel;
    }

    private JButton createRunButton() {
        JButton runButton = new JButton("Run");
        runButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "You chose to run! You lost the fight.");
            fightWon = false;
            dispose();
        });
        return runButton;
    }

    private JButton createAttackButton() {
        JButton attackButton = new JButton("Attack");
        attackButton.addActionListener(e -> {
            stopAnimation();
            askQuestion(() -> {
                attackEnemy(25);  // More damage for correct answer
                if (enemyHealth <= 0) {
                    JOptionPane.showMessageDialog(this, "You won the fight!");
                    fightWon = true;
                    dispose();
                } else {
                    enemyAttack();
                }
                startAnimation();
            });
        });
        return attackButton;
    }

    private JButton createDefendButton() {
        JButton defendButton = new JButton("Defend");
        defendButton.addActionListener(e -> {
            stopAnimation();
            askQuestion(() -> {
                defendAgainstEnemy(50);  // More defense for correct answer
                enemyAttack();
                startAnimation();
            });
        });
        return defendButton;
    }

    private void startAnimationTimer() {
        animationTimer = new Timer(100, e -> {
            currentFrame = (currentFrame + 1) % enemyAnimation.frames.size();
            repaint();
        });
        animationTimer.start();
    }

    private void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    private void startAnimation() {
        if (animationTimer != null) {
            animationTimer.start();
        }
    }

    private void renderEnemyAnimation(Graphics g) {
        TmxParser.Frame frame = enemyAnimation.frames.get(currentFrame);
        BufferedImage frameImage = getEnemyFrameImage(frame);
        if (frameImage != null) {
            g.drawImage(frameImage, 100, 100, 80, 64, null);
        }
    }

    private BufferedImage getEnemyFrameImage(TmxParser.Frame frame) {
        return ImageUtils.getTileImage(parser, frame.tileId + enemy.gid);
    }

    private void attackEnemy(int damage) {
        enemyHealth -= damage;
        enemyHealth = Math.max(enemyHealth, 0);
        enemyHealthLabel.setText("Health: " + enemyHealth);
        JOptionPane.showMessageDialog(this, "You attacked the enemy and dealt " + damage + " damage!");
    }

    private void defendAgainstEnemy(int damageReduction) {
        JOptionPane.showMessageDialog(this, "You chose to defend! Enemy's attack will be reduced by " + damageReduction + "%.");
    }

    private void enemyAttack() {
        if (enemyHealth > 0) {
            int baseDamage = random.nextInt(15) + 5;
            int damage = baseDamage;
            character.removeHealth(damage);
            playerHealth -= damage;
            playerHealth = Math.max(playerHealth, 0);
            playerHealthLabel.setText("Health: " + playerHealth);
            JOptionPane.showMessageDialog(this, "The enemy attacked you and dealt " + damage + " damage!");

            if (playerHealth <= 0) {
                JOptionPane.showMessageDialog(this, "You lost the fight!");
                fightWon = false;
                dispose();
            }
        }
    }

    private void askQuestion(Runnable onComplete) {
        if (questions != null && !questions.isEmpty()) {
            Question question = questions.get(currentQuestionIndex);
            currentQuestionIndex = (currentQuestionIndex + 1) % questions.size();

            SwingUtilities.invokeLater(() -> {
                String userAnswer = JOptionPane.showInputDialog(this, question.getQuestion(), "Answer the Question", JOptionPane.QUESTION_MESSAGE);
                if (userAnswer != null && userAnswer.equalsIgnoreCase(question.getAnswer())) {
                    JOptionPane.showMessageDialog(this, "Correct Answer!", "Result", JOptionPane.INFORMATION_MESSAGE);
                    character.addXP(10);
                } else {
                    JOptionPane.showMessageDialog(this, "Wrong Answer!", "Result", JOptionPane.ERROR_MESSAGE);
                }
                onComplete.run();  // Proceed after question is answered
            });
        } else {
            System.out.println("No questions available.");
            onComplete.run();  // Proceed if no questions
        }
    }

    @Override
    public void dispose() {
        animationTimer.stop();
        super.dispose();
    }

    public boolean isFightWon() {
        return fightWon;
    }
}
