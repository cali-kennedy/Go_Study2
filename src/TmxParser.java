import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TmxParser {
    private int width, height, tileWidth, tileHeight;
    private List<Tileset> tilesets = new ArrayList<>();
    private List<Layer> layers = new ArrayList<>();
    private List<ObjectLayer> objectLayers = new ArrayList<>();
    private Map<Integer, Animation> animations = new HashMap<>();  // Map to store tile GIDs with animations

    public TmxParser(String filePath) {
        parse(filePath);
    }

    private void parse(String filePath) {
        try {
            File file = new File(filePath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            Element root = document.getDocumentElement();
            this.width = Integer.parseInt(root.getAttribute("width"));
            this.height = Integer.parseInt(root.getAttribute("height"));
            this.tileWidth = Integer.parseInt(root.getAttribute("tilewidth"));
            this.tileHeight = Integer.parseInt(root.getAttribute("tileheight"));

            // Parse tilesets and referenced .tsx files
            NodeList tilesetNodes = root.getElementsByTagName("tileset");
            for (int i = 0; i < tilesetNodes.getLength(); i++) {
                Element tilesetElement = (Element) tilesetNodes.item(i);
                int firstGid = Integer.parseInt(tilesetElement.getAttribute("firstgid"));
                String tsxSource = tilesetElement.getAttribute("source");
                tsxSource = "resources/assets/" + tsxSource;
                Tileset tileset = parseTileset(tsxSource, firstGid);
                tilesets.add(tileset);
            }

            // Parse tile layers
            NodeList layerNodes = root.getElementsByTagName("layer");
            for (int i = 0; i < layerNodes.getLength(); i++) {
                Element layerElement = (Element) layerNodes.item(i);
                Layer layer = new Layer(
                        layerElement.getAttribute("name"),
                        Integer.parseInt(layerElement.getAttribute("width")),
                        Integer.parseInt(layerElement.getAttribute("height")),
                        layerElement.getElementsByTagName("data").item(0).getTextContent().trim()
                );
                layers.add(layer);
            }

            // Parse object layers
            NodeList objectLayerNodes = root.getElementsByTagName("objectgroup");
            for (int i = 0; i < objectLayerNodes.getLength(); i++) {
                Element objectLayerElement = (Element) objectLayerNodes.item(i);
                ObjectLayer objectLayer = new ObjectLayer(objectLayerElement.getAttribute("name"));

                NodeList objectNodes = objectLayerElement.getElementsByTagName("object");
                for (int j = 0; j < objectNodes.getLength(); j++) {
                    Element objectElement = (Element) objectNodes.item(j);

                    int id = Integer.parseInt(objectElement.getAttribute("id"));
                    int gid = objectElement.hasAttribute("gid") ? Integer.parseInt(objectElement.getAttribute("gid")) : 0;
                    String name = objectElement.hasAttribute("name") ? objectElement.getAttribute("name") : "Unnamed";
                    float x = Float.parseFloat(objectElement.getAttribute("x"));
                    float y = Float.parseFloat(objectElement.getAttribute("y"));
                    float width = objectElement.hasAttribute("width") ? Float.parseFloat(objectElement.getAttribute("width")) : 0;
                    float height = objectElement.hasAttribute("height") ? Float.parseFloat(objectElement.getAttribute("height")) : 0;

                    // Get properties for each object
                    Map<String, String> properties = getObjectProperties(objectElement);

                    // Add object to the object layer
                    MapObject mapObject = new MapObject(id, gid, name, x, y, width, height, properties);
                    objectLayer.addObject(mapObject);
                }

                objectLayers.add(objectLayer);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Tileset parseTileset(String tsxFilePath, int firstGid) {
        Tileset tileset = null;
        try {
            File file = new File(tsxFilePath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            Element root = document.getDocumentElement();
            Element imageElement = (Element) root.getElementsByTagName("image").item(0);
            String imageSource = imageElement.getAttribute("source");

            int tileWidth = Integer.parseInt(root.getAttribute("tilewidth"));
            int tileHeight = Integer.parseInt(root.getAttribute("tileheight"));
            int tileCount = Integer.parseInt(root.getAttribute("tilecount"));
            int columns = Integer.parseInt(root.getAttribute("columns"));

            tileset = new Tileset(firstGid, imageSource, tileWidth, tileHeight, tileCount, columns);

            // Parse animations within the tileset
            NodeList tileNodes = root.getElementsByTagName("tile");
            for (int i = 0; i < tileNodes.getLength(); i++) {
                Element tileElement = (Element) tileNodes.item(i);
                int localTileId = Integer.parseInt(tileElement.getAttribute("id"));

                // Check if the tile has an animation
                NodeList animationNodes = tileElement.getElementsByTagName("animation");
                if (animationNodes.getLength() > 0) {
                    List<Frame> frames = new ArrayList<>();
                    NodeList frameNodes = ((Element) animationNodes.item(0)).getElementsByTagName("frame");
                    for (int j = 0; j < frameNodes.getLength(); j++) {
                        Element frameElement = (Element) frameNodes.item(j);
                        int frameTileId = Integer.parseInt(frameElement.getAttribute("tileid"));
                        int duration = Integer.parseInt(frameElement.getAttribute("duration"));
                        frames.add(new Frame(frameTileId, duration));
                    }
                    // Add animation to the map (keyed by the global tile id)
                    animations.put(firstGid + localTileId, new Animation(frames));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return tileset;
    }

    // New method to parse object properties
    private Map<String, String> getObjectProperties(Element objectElement) {
        Map<String, String> properties = new HashMap<>();
        NodeList propertyNodes = objectElement.getElementsByTagName("property");

        for (int i = 0; i < propertyNodes.getLength(); i++) {
            Element propertyElement = (Element) propertyNodes.item(i);
            String name = propertyElement.getAttribute("name");
            String value = propertyElement.getAttribute("value");
            properties.put(name, value);
        }

        return properties;
    }

    // Method to find the tileset that contains a given tileId
    public Tileset getTilesetForTile(int tileId) {
        for (Tileset tileset : tilesets) {
            if (tileId >= tileset.firstGid) {
                return tileset;
            }
        }
        return null;  // Return null if no matching tileset is found
    }

    // Method to retrieve tile animations
    public Animation getTileAnimation(int tileId) {
        return animations.get(tileId);
    }

    // Getter methods for tilesets, layers, object layers, and animations
    public List<Tileset> getTilesets() {
        return tilesets;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public int getTileWidth() {
        return this.tileWidth;
    }

    public int getTileHeight() {
        return this.tileHeight;
    }

    public List<ObjectLayer> getObjectLayers() {
        return objectLayers;
    }

    public Map<Integer, Animation> getAnimations() {
        return animations;
    }

    public int getMapWidth() {
        return width;
    }

    public int getMapHeight() {
        return height;
    }

    // Inner class to represent a tileset
    class Tileset {
        int firstGid;
        String imageSource;
        int tileWidth, tileHeight;
        int tileCount;
        int columns;

        Tileset(int firstGid, String imageSource, int tileWidth, int tileHeight, int tileCount, int columns) {
            this.firstGid = firstGid;
            this.imageSource = imageSource;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
            this.tileCount = tileCount;
            this.columns = columns;
        }
    }

    // Inner class to represent a tile layer
    class Layer {
        String name;
        int width, height;
        String data;

        Layer(String name, int width, int height, String data) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }

    // Inner class to represent an object
    class MapObject {
        int id, gid;
        String name;
        float x, y, width, height;
        Map<String, String> properties;

        MapObject(int id, int gid, String name, float x, float y, float width, float height, Map<String, String> properties) {
            this.id = id;
            this.gid = gid;
            this.name = name;
            this.x = x;
            this.y = y - 20;
            this.width = width;
            this.height = height;
            this.properties = properties;
        }

        // Method to check if the object is an enemy based on properties
        public boolean isEnemy() {
            return "true".equals(properties.get("is_enemy"));
        }
    }

    // Inner class to represent an object layer
    class ObjectLayer {
        String name;
        List<MapObject> objects = new ArrayList<>();

        ObjectLayer(String name) {
            this.name = name;
        }

        void addObject(MapObject object) {
            objects.add(object);
        }

        public List<MapObject> getObjects() {
            return objects;
        }
    }

    // Inner class to represent an animation frame
    class Frame {
        int tileId;
        int duration;  // Duration in milliseconds

        Frame(int tileId, int duration) {
            this.tileId = tileId;
            this.duration = duration;
        }
    }

    // Inner class to represent an animation sequence
    class Animation {
        List<Frame> frames;
        int totalDuration;

        Animation(List<Frame> frames) {
            this.frames = frames;
            this.totalDuration = frames.stream().mapToInt(frame -> frame.duration).sum();
        }

        Frame getFrameForTime(int time) {
            int currentTime = 0;
            for (Frame frame : frames) {
                currentTime += frame.duration;
                if (time < currentTime) {
                    return frame;
                }
            }
            return frames.get(0);  // Fallback to the first frame
        }
    }
}
