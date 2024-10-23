import java.awt.*;
import java.util.Iterator;
import javax.swing.*;
import java.util.List;

public class CollisionDetector {

    public static boolean checkCollisions(Character character, TmxParser parser, JFrame parentFrame, List<Question> questions) {
        boolean collidedWithWall = false;  // Flag to track if character collides with a wall

        for (TmxParser.ObjectLayer objectLayer : parser.getObjectLayers()) {
            Iterator<TmxParser.MapObject> iterator = objectLayer.getObjects().iterator();
            while (iterator.hasNext()) {
                TmxParser.MapObject mapObject = iterator.next();

                // Handle apple collision
                if (mapObject.name.equals("apple") && isColliding(character, mapObject)) {
                    handleAppleCollision(character, mapObject, iterator);
                }

                // Handle wall collision
                if (mapObject.name.equals("wall") && isColliding(character, mapObject)) {
                    collidedWithWall = true;  // Character collided with a wall
                }

                // Handle enemy collision
                if (mapObject.isEnemy() && isColliding(character, mapObject)) {
                    handleEnemyCollision(character, mapObject, parentFrame, iterator, parser, questions);
                }
            }
        }
        return collidedWithWall;  // Return if character hit a wall
    }

    // Handle apple collision and health increment
    private static void handleAppleCollision(Character character, TmxParser.MapObject mapObject, Iterator<TmxParser.MapObject> iterator) {
        character.addHealth(10);  // Increase health by 10
        System.out.println("Collected an apple! Health: " + character.getHealth());
        iterator.remove();  // Remove apple from the map
    }

    // Handle enemy collision and trigger fight screen with questions
    private static void handleEnemyCollision(Character character, TmxParser.MapObject enemy, JFrame parentFrame, Iterator<TmxParser.MapObject> iterator, TmxParser parser, List<Question> questions) {
        // Start the fight screen, passing the parser for animations and questions for combat
        FightScreen fightScreen = new FightScreen(parentFrame, character, enemy, parser, questions);
        fightScreen.setVisible(true);

        // After the fight, if the player wins, remove the enemy from the map
        if (fightScreen.isFightWon()) {
            iterator.remove();  // Remove enemy after the fight
            System.out.println("You defeated the enemy!");
        } else {
            System.out.println("You lost the fight!");
        }
    }

    // Check if character is colliding with an object
    private static boolean isColliding(Character character, TmxParser.MapObject object) {
        Rectangle charBounds = new Rectangle(character.getX(), character.getY(), character.getWidth(), character.getHeight());
        Rectangle objectBounds = new Rectangle((int) object.x, (int) object.y, (int) object.width, (int) object.height);
        return charBounds.intersects(objectBounds);
    }
}
