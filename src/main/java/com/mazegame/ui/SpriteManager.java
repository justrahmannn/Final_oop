package com.mazegame.ui;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpriteManager {
    // Use ConcurrentHashMap for thread safety and stability
    private static Map<String, BufferedImage> sprites = new ConcurrentHashMap<>();
    private static final String SPRITE_PATH_PREFIX = "/sprites/";

    public static void loadAllSprites() {
        System.out.println("Loading sprites...");
        loadSprite("player.png");
        loadSprite("goblin.png");
        loadSprite("wall.png");
        loadSprite("floor.png");
        loadSprite("door_closed.png");
        loadSprite("door_open.png");
        loadSprite("chest_closed.png");
        loadSprite("chest_open.png");
        loadSprite("chest.png");
        loadSprite("key.png");
        loadSprite("aid_kit.png");
        loadSprite("ammo.png");
        loadSprite("gun.png");
        loadSprite("treasure.png");
        loadSprite("crowbar.png");
        loadSprite("lever_on.png");
        loadSprite("lever_off.png");
        // --- Trap sprites ---
        loadSprite("trap_idle.png");
        loadSprite("trap_warning.png");
        loadSprite("trap_active.png");
        // --------------------
        System.out.println("Sprites loaded: " + sprites.size());
        if (sprites.containsKey("floor.png") && sprites.get("floor.png") != null) {
            System.out.println("VERIFY_LOAD: floor.png successfully loaded and retrievable immediately after loading.");
        } else {
            System.err.println("VERIFY_LOAD_FAIL: floor.png FAILED to be retrieved immediately after loading or not in map.");
        }
        if (sprites.containsKey("door_open.png") && sprites.get("door_open.png") != null) {
            System.out.println("VERIFY_LOAD: door_open.png successfully loaded and retrievable immediately after loading.");
        } else {
            System.err.println("VERIFY_LOAD_FAIL: door_open.png FAILED to be retrieved immediately after loading or not in map.");
        }
        System.out.println("Sprites loaded successfully");
    }

    private static void loadSprite(String fileName) {
        String cleanFileName = fileName.trim();
        System.out.println("SpriteManager.loadSprite: Attempting to load and cache with key: '[" + cleanFileName + "]'");
        try (InputStream is = SpriteManager.class.getResourceAsStream(SPRITE_PATH_PREFIX + cleanFileName)) {
            if (is == null) {
                System.err.println("Error: Sprite resource not found - " + SPRITE_PATH_PREFIX + cleanFileName);
                sprites.remove(cleanFileName); // Ensure key is not present if loading failed
                return;
            }
            BufferedImage sprite = ImageIO.read(is);
            if (sprite != null) {
                sprites.put(cleanFileName, sprite); // THIS IS THE PUT
                System.out.println("Loaded sprite: " + cleanFileName + " (Dimensions: " + sprite.getWidth() + "x" + sprite.getHeight() + ")");
                // ADD VERIFICATION IMMEDIATELY AFTER PUT
                if (!sprites.containsKey(cleanFileName) || sprites.get(cleanFileName) == null) {
                    System.err.println("!!!!!!!!!! CRITICAL CACHE FAILURE for [" + cleanFileName + "] IMMEDIATELY AFTER PUT !!!!!!!!!!");
                } else {
                    System.out.println("!!!!!!!!!! VERIFY PUT SUCCESS for key: [" + cleanFileName + "] - get returned valid image immediately after put. Cache size: " + sprites.size() + " !!!!!!!!!!");
                }
            } else {
                System.err.println("Error: ImageIO.read returned null for " + cleanFileName);
                sprites.remove(cleanFileName); // Ensure key is not present if loading failed
            }
        } catch (IOException e) {
            System.err.println("Error loading sprite " + cleanFileName + ": " + e.getMessage());
            sprites.remove(cleanFileName); // Ensure key is not present if loading failed
            e.printStackTrace();
        }
    }

    public static BufferedImage getSprite(String spriteName) {
        String cleanSpriteName = spriteName.trim();

        if (!sprites.containsKey(cleanSpriteName)) {
            System.err.println("SPRITE_ERROR: Key '[" + cleanSpriteName + "]' NOT PRESENT in cache keys.");
            printCacheState();
            return null;
        }

        BufferedImage sprite = sprites.get(cleanSpriteName);

        if (sprite == null) {
            System.err.println("SPRITE_ERROR: Key '[" + cleanSpriteName + "]' IS PRESENT in cache, but its value is NULL.");
            printCacheState();
        }
        return sprite;
    }

    private static void printCacheState() {
        System.err.print("SPRITE_CACHE_STATE: Size: " + sprites.size() + ", Keys: [");
        for (String key : sprites.keySet()) {
            System.err.print("'" + key + "'" + (sprites.get(key) == null ? " (VALUE IS NULL!)" : "") + ", ");
        }
        System.err.println("]");
    }
}