import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Character {
    private int x, y;
    private BufferedImage sprite;
    private int width, height;
    private int health;
    private int XP;
    private static final int MAX_HEALTH = 100;

    public Character(String spritePath, int startX, int startY, int width, int height) {
        this.x = startX;
        this.y = startY;
        this.width = width;
        this.height = height;
        this.health = MAX_HEALTH;  // Starting health

        try {
            sprite = ImageIO.read(new File(spritePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics g) {
        g.drawImage(sprite, x, y, width, height, null);
    }

    public void move(int dx, int dy, TmxParser parser, JFrame parentFrame, Question question, List<Question> questions) {
        int oldX = x;
        int oldY = y;


        // Check for collisions before moving
        boolean collidedWithWall = CollisionDetector.checkCollisions(this, parser, parentFrame, questions);
        // If the player collided with a wall, revert the movement
        if (collidedWithWall) {
            x = oldX - 2;
            y = oldY - 2;
            return;  // Stop further processing if a wall is hit
        } else {
            // Move the character to the new position
            x += dx;
            y += dy;
        }
        // Check for collisions after moving
      //  boolean collidedWithWall = CollisionDetector.checkCollisions(this, parser, parentFrame, questions);

        // If the player collided with a wall, revert the movement
//if (collidedWithWall) {
  //          x = oldX;
    //        y = oldY;
      //      return;  // Stop further processing if a wall is hit
        //}
    }


    // Getters and setters for position and attributes
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(health, MAX_HEALTH));  // Ensure health stays within valid range
    }

    // Add health, but not above max health
    public void addHealth(int healthToAdd) {
        this.health = Math.min(this.health + healthToAdd, MAX_HEALTH);
        System.out.println("Health increased! Current health: " + this.health);
    }

    // Remove health, ensuring it doesn't drop below zero
    public void removeHealth(int healthToRemove) {
        this.health = Math.max(this.health - healthToRemove, 0);
        System.out.println("Health decreased! Current health: " + this.health);
    }

    public int getXP() {
        return XP;
    }

    public void setXP(int XP) {
        this.XP = XP;
    }

    // Add experience points to the character
    public void addXP(int XPToAdd) {
        this.XP += XPToAdd;
        System.out.println("Gained " + XPToAdd + " XP! Total XP: " + this.XP);
    }
}
