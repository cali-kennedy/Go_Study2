import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class ImageUtils {

    /**
     * Retrieves the tile image corresponding to the given GID from the tileset.
     *
     * @param parser The TmxParser instance containing the map and tileset information.
     * @param gid    The global tile ID to fetch the corresponding tile image.
     * @return The BufferedImage of the tile, or null if the tile could not be found.
     */
    public static BufferedImage getTileImage(TmxParser parser, int gid) {
        if (gid == 0) {
            return null;  // Skip blank tiles
        }

        for (TmxParser.Tileset tileset : parser.getTilesets()) {
            if (gid >= tileset.firstGid && gid < tileset.firstGid + tileset.tileCount) {
                int localTileId = gid - tileset.firstGid;
                int tileX = (localTileId % tileset.columns) * tileset.tileWidth;
                int tileY = (localTileId / tileset.columns) * tileset.tileHeight;
                try {
                    // Load the tileset image
                    BufferedImage tilesetImage = loadImage("resources/assets/" + tileset.imageSource);
                    if (tilesetImage != null) {
                        // Crop the specific tile from the tileset
                        return tilesetImage.getSubimage(tileX, tileY, tileset.tileWidth, tileset.tileHeight);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;  // Return null if no valid tile image is found
    }

    /**
     * Loads an image from a file path.
     *
     * @param path The path to the image file.
     * @return The BufferedImage if found, or null otherwise.
     */
    public static BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (Exception e) {
            System.err.println("Failed to load image: " + path);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Renders an animation frame based on the current time.
     *
     * @param parser       The TmxParser instance containing the map information.
     * @param gid          The global tile ID of the animated tile.
     * @param elapsedTime  The elapsed time since the start of the animation.
     * @param animationMap The map of animations for the tileset.
     * @return The BufferedImage of the current frame in the animation, or null if none exists.
     */
    public static BufferedImage getAnimationFrame(TmxParser parser, int gid, int elapsedTime,
                                                  List<TmxParser.Animation> animationMap) {
        TmxParser.Animation animation = parser.getAnimations().get(gid);
        if (animation != null) {
            int frameTime = elapsedTime % animation.totalDuration;
            TmxParser.Frame frame = animation.getFrameForTime(frameTime);
            return getTileImage(parser, frame.tileId + gid);  // Get the frame's tile ID relative to the GID
        }
        return null;
    }
}
