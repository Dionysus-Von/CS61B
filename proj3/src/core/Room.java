package core;
import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class Room {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    public enum RoomType {
        RECTANGLE,
        L_SHAPE,
        DIAMOND
    }

    Set<Point> floorTiles = new HashSet<>();
    Set<Point> edgeTiles = new HashSet<>();

    Room(int x, int y, int width, int height, RoomType type) {
        if (width < 4 || height < 4) {
            throw new IllegalArgumentException("房间宽高至少为 4");
        }
        if (type == null) {
            throw new IllegalArgumentException("房间类型不能为空");
        }

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        generateFloor(type);
        generateBoundary();
    }

    private void generateFloor(RoomType type) {
        for (int dx = 0; dx < width; dx++) {
            for (int dy = 0; dy < height; dy++) {
                if (containsFloor(dx, dy, width, height, type)) {
                    floorTiles.add(new Point(x + dx, y + dy));
                }
            }
        }
    }

    private boolean containsFloor(int x, int y, int width, int height, RoomType type) {
        return switch (type) {
            case RECTANGLE -> true;
            case L_SHAPE -> x < width / 2 || y < height / 2;
            case DIAMOND -> Math.abs(2L * x - (width - 1)) * height
                    + Math.abs(2L * y - (height - 1)) * width <= (long) width * height;
        };
    }

    void generateBoundary() {
        edgeTiles.clear();
        for (Point p : floorTiles) {
            boolean left = floorTiles.contains(new Point(p.x - 1, p.y));
            boolean right = floorTiles.contains(new Point(p.x + 1, p.y));
            boolean up = floorTiles.contains(new Point(p.x, p.y + 1));
            boolean down = floorTiles.contains(new Point(p.x, p.y - 1));

            if (!left || !right || !up || !down) {
                edgeTiles.add(new Point(p.x, p.y));
            }
        }
    }

    public int size(){
        return floorTiles.size();
    }
}
