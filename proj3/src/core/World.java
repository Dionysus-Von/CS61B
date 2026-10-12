package core;

import core.Room.RoomType;
import tileengine.TETile;
import tileengine.Tileset;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class World {
    private static final int MIN_ROOM_SIZE = 8;
    private static final int MAX_FAILED_ATTEMPTS = 500;
    private static final int[][] DIRECTIONS = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
    private final int worldWidth;
    private final int worldHeight;
    private final int worldSize;
    private int roomSize;
    private final Map<Room, Set<Room>> rooms = new LinkedHashMap<>();
    private Set<Point> occupiedTiles = new HashSet<>();
    private Set<Point> boundaryTiles = new HashSet<>();
    private final Random random;
    private TETile[][] tiles;

    public World(int width, int height, long seed) {
        if (width < MIN_ROOM_SIZE + 2 || height < MIN_ROOM_SIZE + 2) {
            throw new IllegalArgumentException("地图宽高至少为 " + (MIN_ROOM_SIZE + 2));
        }

        worldWidth = width;
        worldHeight = height;
        worldSize = width * height;
        random = new Random(seed);
        tiles = new TETile[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }

        roomGenerate();
        roomGrow();
        generateWalls();
        connectRooms();
        addTurningConnection();
        generateWalls();
    }
    private boolean roomGenerate() {
        int x = 1 + random.nextInt(worldWidth - MIN_ROOM_SIZE - 1);
        int y = 1 + random.nextInt(worldHeight - MIN_ROOM_SIZE - 1);
        Room room = roomTry(x, y);
        if (room == null) {
            return false;
        }
        roomPut(room);
        return true;
    }

    private void roomPut(Room room) {
        for(Point p:room.floorTiles){
            tiles[p.x][p.y]=Tileset.FLOOR;
        }
        rooms.put(room, new LinkedHashSet<>());
        occupiedTiles.addAll(room.floorTiles);
        roomSize += room.size();
    }


    private void generateWalls() {
        for (Point p : boundaryTiles) {
            if (tiles[p.x][p.y] == Tileset.WALL) {
                tiles[p.x][p.y] = Tileset.NOTHING;
            }
        }
        boundaryTiles.clear();
        for (Point p : occupiedTiles) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int x = p.x + dx;
                    int y = p.y + dy;
                    if (x >= 0 && x < worldWidth && y >= 0 && y < worldHeight
                            && tiles[x][y] == Tileset.NOTHING) {
                        boundaryTiles.add(new Point(x, y));
                    }
                }
            }
        }
        for (Point p : boundaryTiles) {
            tiles[p.x][p.y] = Tileset.WALL;
        }
    }

    private void roomGrow() {
        int failures = 0;
        while ((double) roomSize / worldSize < 0.7 && failures < MAX_FAILED_ATTEMPTS) {
            if (roomGenerate()) {
                failures = 0;
            } else {
                failures++;
            }
        }
    }



    private Room roomTry(int x, int y) {
        int maxWidth = worldWidth - x - 1;
        int maxHeight = worldHeight - y - 1;
        if (x < 1 || y < 1 || maxWidth < MIN_ROOM_SIZE || maxHeight < MIN_ROOM_SIZE) {
            return null;
        }
        int width = MIN_ROOM_SIZE + random.nextInt(maxWidth - MIN_ROOM_SIZE + 1);
        int height = MIN_ROOM_SIZE + random.nextInt(maxHeight - MIN_ROOM_SIZE + 1);
        RoomType[] values = RoomType.values();
        RoomType type = rooms.isEmpty() ? RoomType.RECTANGLE : values[random.nextInt(values.length)];
        Room candidate = new Room(x, y, width, height, type);
        return isAreaOccup(candidate) ? null : candidate;
    }

    private boolean isAreaOccup(Room room) {
        for (Point p : room.floorTiles) {
            if (occupiedTiles.contains(p)) {
                return true;
            }
        }
        return false;
    }


    private void connectRooms() {
        if (rooms.isEmpty()) {
            return;
        }
        Map<Point, Room> owners = new HashMap<>();
        for (Room room : rooms.keySet()) {
            for (Point p : sortedFloor(room)) {
                Room previous = owners.putIfAbsent(p, room);
                if (previous != null) {
                    addConnection(previous, room);
                }
                for (int[] direction : DIRECTIONS) {
                    Room neighbor = owners.get(new Point(p.x + direction[0], p.y + direction[1]));
                    if (neighbor != null) {
                        addConnection(neighbor, room);
                    }
                }
            }
        }


        boolean opened;
        do {
            opened = false;
            for (Room room : rooms.keySet()) {
                Connection connection = findConnection(connectedRooms(room), owners, true);
                if (connection != null) {
                    openConnection(connection, owners);
                    opened = true;
                    break;
                }
            }
        } while (opened);

        Room first = rooms.keySet().iterator().next();
        Set<Room> connected = connectedRooms(first);
        while (connected.size() < rooms.size()) {
            Connection connection = findConnection(connected, owners, false);
            if (connection == null) {
                throw new IllegalStateException("无法连接剩余房间");
            }
            openConnection(connection, owners);
            connected = connectedRooms(first);
        }
    }

    private void addTurningConnection() {
        if (rooms.size() < 2) {
            return;
        }
        Map<Point, Room> owners = new HashMap<>();
        List<Point> edges = new ArrayList<>();
        for (Room room : rooms.keySet()) {
            edges.addAll(room.edgeTiles);
            for (Point p : room.floorTiles) {
                owners.put(p, room);
            }
        }
        edges.sort(Comparator.comparingInt((Point p) -> p.x).thenComparingInt(p -> p.y));
        for (int attempt = 0; attempt < 6000; attempt++) {
            Point start = edges.get(random.nextInt(edges.size()));
            Point end = edges.get(random.nextInt(edges.size()));
            if (owners.get(start) == owners.get(end)
                    || Math.abs(start.x - end.x) < 2 || Math.abs(start.y - end.y) < 2) {
                continue;
            }
            List<Point> path = new ArrayList<>();
            int x = start.x;
            int y = start.y;
            path.add(new Point(x, y));
            while (x != end.x) {
                x += Integer.compare(end.x, x);
                path.add(new Point(x, y));
            }
            while (y != end.y) {
                y += Integer.compare(end.y, y);
                path.add(new Point(x, y));
            }
            if (isClearHallway(path, start, end)) {
                openConnection(new Connection(owners.get(start), owners.get(end), path), owners);
                return;
            }
        }
    }

    private boolean isClearHallway(List<Point> path, Point start, Point end) {
        for (int i = 1; i < path.size() - 1; i++) {
            Point p = path.get(i);
            if (occupiedTiles.contains(p)) {
                return false;
            }
            for (int[] direction : DIRECTIONS) {
                Point neighbor = new Point(p.x + direction[0], p.y + direction[1]);
                if (occupiedTiles.contains(neighbor) && !neighbor.equals(start) && !neighbor.equals(end)) {
                    return false;
                }
            }
        }
        return true;
    }

    private List<Point> sortedFloor(Room room) {
        List<Point> points = new ArrayList<>(room.floorTiles);
        points.sort(Comparator.comparingInt((Point p) -> p.x).thenComparingInt(p -> p.y));
        return points;
    }

    private void addConnection(Room first, Room second) {
        if (first != second) {
            rooms.get(first).add(second);
            rooms.get(second).add(first);
        }
    }

    private Set<Room> connectedRooms(Room start) {
        Set<Room> connected = new LinkedHashSet<>();
        ArrayDeque<Room> queue = new ArrayDeque<>();
        connected.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            for (Room neighbor : rooms.get(queue.remove())) {
                if (connected.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
        return connected;
    }

    private record Connection(Room from, Room to, List<Point> path) { }

    private Connection findConnection(Set<Room> sources, Map<Point, Room> owners,
                                      boolean wallsOnly) {
        ArrayDeque<Point> queue = new ArrayDeque<>();
        Map<Point, Point> parents = new HashMap<>();
        Map<Point, Room> origins = new HashMap<>();
        Map<Point, Integer> distances = new HashMap<>();
        for (Room room : rooms.keySet()) {
            if (!sources.contains(room)) {
                continue;
            }
            for (Point p : sortedFloor(room)) {
                if (!parents.containsKey(p)) {
                    parents.put(p, null);
                    origins.put(p, room);
                    distances.put(p, 0);
                    queue.add(p);
                }
            }
        }
        while (!queue.isEmpty()) {
            Point p = queue.remove();
            for (int[] direction : DIRECTIONS) {
                Point next = new Point(p.x + direction[0], p.y + direction[1]);
                if (next.x < 1 || next.y < 1 || next.x >= worldWidth - 1
                        || next.y >= worldHeight - 1 || parents.containsKey(next)) {
                    continue;
                }
                Room target = owners.get(next);
                if (target != null && !sources.contains(target)) {
                    List<Point> path = new ArrayList<>();
                    path.add(next);
                    for (Point step = p; step != null; step = parents.get(step)) {
                        path.add(step);
                    }
                    return new Connection(origins.get(p), target, path);
                }
                int distance = distances.get(p) + 1;
                if (wallsOnly && (tiles[next.x][next.y] != Tileset.WALL || distance > 2)) {
                    continue;
                }
                parents.put(next, p);
                origins.put(next, origins.get(p));
                distances.put(next, distance);
                queue.add(next);
            }
        }
        return null;
    }

    private void openConnection(Connection connection, Map<Point, Room> owners) {
        for (Point p : connection.path()) {
            tiles[p.x][p.y] = Tileset.FLOOR;
            occupiedTiles.add(p);
            boundaryTiles.remove(p);
        }
        addConnection(connection.from(), connection.to());

        for (Point p : connection.path()) {
            Room owner = owners.get(p);
            if (owner != null) {
                addConnection(connection.from(), owner);
            }
            for (int[] direction : DIRECTIONS) {
                Room neighbor = owners.get(new Point(p.x + direction[0], p.y + direction[1]));
                if (neighbor != null) {
                    addConnection(connection.from(), neighbor);
                }
            }
        }
    }

    public TETile[][] getTiles() {
        return tiles;
    }
}
