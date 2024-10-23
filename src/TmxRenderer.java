import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TmxRenderer {
    private TmxParser parser;
    private Map<Integer, TilesetImageData> tilesetImages = new HashMap<>();
    private long animationStartTime;

    public TmxRenderer(TmxParser parser) {
        this.parser = parser;
        loadTilesets();
        this.animationStartTime = System.currentTimeMillis();
    }

    class TilesetImageData {
        BufferedImage image;
        int tileWidth, tileHeight;
        int tileCount;
        int columns;

        TilesetImageData(BufferedImage image, int tileWidth, int tileHeight, int tileCount, int columns) {
            this.image = image;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
            this.tileCount = tileCount;
            this.columns = columns;
        }
    }

    private void loadTilesets() {
        for (TmxParser.Tileset tileset : parser.getTilesets()) {
            try {
                BufferedImage tilesetImage = ImageIO.read(new File("resources/assets/" + tileset.imageSource));
                TilesetImageData data = new TilesetImageData(tilesetImage, tileset.tileWidth, tileset.tileHeight, tileset.tileCount, tileset.columns);
                tilesetImages.put(tileset.firstGid, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void render(Graphics g) {
        renderTileLayers(g);
        renderObjectLayers(g);
    }

    private void renderTileLayers(Graphics g) {
        List<TmxParser.Layer> layers = parser.getLayers();
        for (TmxParser.Layer layer : layers) {
            String[] tiles = layer.getData().split(",");
            int tileIndex = 0;

            for (int y = 0; y < layer.height; y++) {
                for (int x = 0; x < layer.width; x++) {
                    int gid = Integer.parseInt(tiles[tileIndex++].trim());
                    if (gid != 0) {
                        BufferedImage tileImage = getTileImage(gid);
                        if (tileImage != null) {
                            g.drawImage(tileImage, x * parser.getTileWidth(), y * parser.getTileHeight(), parser.getTileWidth(), parser.getTileHeight(), null);
                        }
                    }
                }
            }
        }
    }

    private void renderObjectLayers(Graphics g) {
        List<TmxParser.ObjectLayer> objectLayers = parser.getObjectLayers();
        for (TmxParser.ObjectLayer objectLayer : objectLayers) {
            for (TmxParser.MapObject mapObject : objectLayer.getObjects()) {
                BufferedImage objectImage = getTileImage(mapObject.gid);
                if (objectImage != null) {
                    g.drawImage(objectImage, (int) mapObject.x, (int) mapObject.y, (int) mapObject.width, (int) mapObject.height, null);
                }
            }
        }
    }

    // Corrected method to get the tile image with proper animation handling and tileset correction
    private BufferedImage getTileImage(int gid) {
        // Check if the tile is animated
        TmxParser.Animation animation = parser.getAnimations().get(gid);
        if (animation != null) {
            int elapsedTime = (int) ((System.currentTimeMillis() - animationStartTime) % animation.totalDuration);
            TmxParser.Frame frame = animation.getFrameForTime(elapsedTime);
            gid = frame.tileId + findTilesetFirstGid(gid);  // Adjust gid using the correct tileset
        }

        // Ensure correct tile indexing and alignment within the tileset
        for (Map.Entry<Integer, TilesetImageData> entry : tilesetImages.entrySet()) {
            int firstGid = entry.getKey();
            TilesetImageData data = entry.getValue();

            int lastGid = firstGid + data.tileCount - 1;
            if (gid >= firstGid && gid <= lastGid) {
                int localId = gid - firstGid;
                int tilesetX = (localId % data.columns) * data.tileWidth;
                int tilesetY = (localId / data.columns) * data.tileHeight;
                return data.image.getSubimage(tilesetX, tilesetY, data.tileWidth, data.tileHeight);
            }
        }
        return null;
    }

    // Helper method to find the correct tileset firstGid for the given gid
    private int findTilesetFirstGid(int gid) {
        int resultFirstGid = 0;
        for (TmxParser.Tileset tileset : parser.getTilesets()) {
            if (gid >= tileset.firstGid) {
                resultFirstGid = tileset.firstGid;
            }
        }
        return resultFirstGid;
    }
}