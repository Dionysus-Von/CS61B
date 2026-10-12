package core;

import tileengine.TERenderer;
import edu.princeton.cs.algs4.StdDraw;
import tileengine.TETile;
import tileengine.Tileset;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Game {
    private static final int HUD_HEIGHT = 2;
    private static final int WIDTH=90;
    private static final int HEIGHT=50;
    private int playerX;
    private int playerY;
    private boolean waitingForQuit = false;
    private long seed;
    private boolean limitedVision = true;
    private static final int VISION_RADIUS = 6;

    private enum State{
        MENU,
        SEED_INPUT,
        PLAYING
    }
    private State state= State.MENU;
    private final StringBuilder seedInput =new StringBuilder();
    private final TERenderer renderer= new TERenderer();
    private World world;

    private void initializePlayer(){
        TETile[][] tiles=world.getTiles();
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[x].length; y++) {
                if (tiles[x][y] == Tileset.FLOOR) {
                    playerX = x;
                    playerY = y;
                    return;
                }
            }
        }
    }
    public void run() {
        renderer.initialize(WIDTH, HEIGHT + HUD_HEIGHT);

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = Character.toLowerCase(StdDraw.nextKeyTyped());
                handleInput(key);
            }

            render();
            StdDraw.pause(16);
        }
    }

    private void handleInput(char key) {
        switch (state) {
            case MENU -> handleMenuInput(key);
            case SEED_INPUT -> handleSeedInput(key);
            case PLAYING -> {
                handlePlayingInput(key);
            }
        }
    }

    private void handleMenuInput(char key) {
        if (key == 'n') {
            seedInput.setLength(0);
            state = State.SEED_INPUT;
        }else if (key == 'q') {
            System.exit(0);
        }
        else if (key == 'l') {
            loadGame();
        }
    }

    private void handleSeedInput(char key) {
        if (key >= '0' && key <= '9') {
            seedInput.append(key);
        } else if (key == 's' && seedInput.length() > 0) {
            seed = Long.parseLong(seedInput.toString());
            world = new World(WIDTH, HEIGHT, seed);
            initializePlayer();
            state = State.PLAYING;
        }
    }

    private void render() {
        if (state == State.PLAYING) {
            StdDraw.clear(Color.BLACK);
            TETile[][] Tiles = world.getTiles();
            for(int x=0;x<Tiles.length;x++){
                for(int y=0;y<Tiles[x].length;y++){
                    if(isVisible(x,y)){
                        Tiles[x][y].draw(x,y);
                    }
                }

            }            Tileset.AVATAR.draw(playerX, playerY);
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.textLeft(
                    1,
                    HEIGHT + HUD_HEIGHT / 2.0,
                    getHoveredDescription()
            );
            StdDraw.show();
            return;
        }

        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);

        if (state == State.MENU) {
            StdDraw.text(WIDTH / 2.0, HEIGHT * 0.7, "Build Your Own World");
            StdDraw.text(WIDTH / 2.0, HEIGHT * 0.5, "N - New Game");
            StdDraw.text(WIDTH / 2.0, HEIGHT * 0.4, "L - Load Game");
            StdDraw.text(WIDTH / 2.0, HEIGHT * 0.3, "Q - Quit");
        } else {
            StdDraw.text(WIDTH / 2.0, HEIGHT * 0.6, "Enter Seed");
            StdDraw.text(WIDTH / 2.0, HEIGHT * 0.5, seedInput.toString());
            StdDraw.text(WIDTH / 2.0, HEIGHT * 0.4, "Press S to Start");
        }

        StdDraw.show();
    }
    private void movePlayer(char key) {
        int nextX = playerX;
        int nextY = playerY;

        switch (key) {
            case 'w' -> nextY++;
            case 's' -> nextY--;
            case 'a' -> nextX--;
            case 'd' -> nextX++;
            default -> {
                return;
            }
        }

        TETile[][] tiles = world.getTiles();

        if (nextX < 0 || nextX >= tiles.length
                || nextY < 0 || nextY >= tiles[0].length) {
            return;
        }


        if (tiles[nextX][nextY] == Tileset.FLOOR) {
            playerX = nextX;
            playerY = nextY;
        }
    }
    private String getHoveredDescription() {
        int x = (int) Math.floor(StdDraw.mouseX());
        int y = (int) Math.floor(StdDraw.mouseY());

        TETile[][] tiles = world.getTiles();

        if (x < 0 || x >= tiles.length
                || y < 0 || y >= tiles[0].length) {
            return "";
        }
        if (!isVisible(x, y)) {
            return "";
        }

        if (x == playerX && y == playerY) {
            return Tileset.AVATAR.description();
        }

        return tiles[x][y].description();
    }
    private void handlePlayingInput(char key) {

        if (waitingForQuit) {
            waitingForQuit = false;

            if (key == 'q') {
                if (saveGame()) {
                    System.exit(0);
                }
            }

            return;
        }
        if(key == 'v'){
            limitedVision=!limitedVision;
            System.out.println("视野限制"+limitedVision);
            return;
        }
        if (key == 'n') {
            seedInput.setLength(0);
            state = State.SEED_INPUT;
            return;
        }

        if (key == ':') {
            waitingForQuit = true;
        } else {
            movePlayer(key);
        }
    }
    private boolean saveGame() {
        String data = seed + "\n" + playerX + "\n" + playerY + "\n" + limitedVision;

        try {
            Files.writeString(Path.of("save.txt"), data);
            return true;
        } catch (IOException e) {
            System.err.println("保存失败：" + e.getMessage());
            return false;
        }
    }
    private void loadGame() {
        try {
            List<String> lines = Files.readAllLines(Path.of("save.txt"));

            long savedSeed = Long.parseLong(lines.get(0));
            int savedX = Integer.parseInt(lines.get(1));
            int savedY = Integer.parseInt(lines.get(2));
            boolean savedVision = lines.size() < 4 || Boolean.parseBoolean(lines.get(3));

            world = new World(WIDTH, HEIGHT, savedSeed);
            seed = savedSeed;
            playerX = savedX;
            playerY = savedY;
            limitedVision = savedVision;

            waitingForQuit = false;
            state = State.PLAYING;
        } catch (IOException | NumberFormatException
                 | IndexOutOfBoundsException e) {
            System.err.println("读取存档失败：" + e.getMessage());
        }
    }
    private boolean isVisible(int x, int y) {
        if (!limitedVision) {
            return true;
        }

        int dx = x - playerX;
        int dy = y - playerY;

        return dx * dx + dy * dy <= VISION_RADIUS * VISION_RADIUS&&hasLineOfSight(x,y);
    }
    private boolean hasLineOfSight(int targetX, int targetY) {
        TETile[][] tiles = world.getTiles();

        int x = playerX;
        int y = playerY;
        int dx = Math.abs(targetX - x);
        int dy = Math.abs(targetY - y);
        int stepX = Integer.compare(targetX, x);
        int stepY = Integer.compare(targetY, y);
        int error = dx - dy;

        while (x != targetX || y != targetY) {
            int twiceError = 2 * error;
            boolean moveX = twiceError > -dy;
            boolean moveY = twiceError < dx;

            if (moveX && moveY
                    && tiles[x + stepX][y] == Tileset.WALL
                    && tiles[x][y + stepY] == Tileset.WALL) {
                return false;
            }

            if (moveX) {
                error -= dy;
                x += stepX;
            }
            if (moveY) {
                error += dx;
                y += stepY;
            }


            if (x == targetX && y == targetY) {
                return true;
            }

            if (tiles[x][y] == Tileset.WALL) {
                return false;
            }
        }

        return true;
    }
}
