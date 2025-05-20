package com.mazegame.core;

import com.mazegame.characters.LivingBeing;
import com.mazegame.characters.Player; // For instanceof check in draw
import com.mazegame.items.Item;
// import com.mazegame.items.Lever; // Lever is an Entity, not necessarily an Item for this context
import com.mazegame.utils.Position;
import com.mazegame.ui.SpriteManager;

import java.util.ArrayList;
import java.util.List;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Color;

public class Room extends Entity { // Room is an Entity, its 'position' is its logical grid pos in the world
    private final int roomID;
    private final Tile[][] tiles;
    private final List<Door> connectedDoors; // For game logic, e.g., finding exits
    private final List<LivingBeing> livingBeingsInRoom;
    private final List<Item> itemsInRoom; // Items on the floor

    public static final int ROOM_WIDTH_TILES = 12;
    public static final int ROOM_HEIGHT_TILES = 12;

    public Room(int roomID, String name, World world, Position worldGridPosition) {
        super(name, world, worldGridPosition); // Entity constructor
        this.roomID = roomID;
        this.tiles = new Tile[ROOM_WIDTH_TILES][ROOM_HEIGHT_TILES];
        this.connectedDoors = new ArrayList<>();
        this.livingBeingsInRoom = new ArrayList<>();
        this.itemsInRoom = new ArrayList<>();
        initializeDefaultTiles();
    }

    private void initializeDefaultTiles() {
        Image wallSprite = SpriteManager.getSprite("wall.png");
        Image floorSprite = SpriteManager.getSprite("floor.png");

        boolean spritesAvailable = (wallSprite != null && floorSprite != null);
        if (!spritesAvailable) {
            System.err.println("Warning: Essential wall/floor sprites missing for room " + getName() + ". Using placeholders.");
        }

        for (int y = 0; y < ROOM_HEIGHT_TILES; y++) {
            for (int x = 0; x < ROOM_WIDTH_TILES; x++) {
                boolean isWall = (x == 0 || x == ROOM_WIDTH_TILES - 1 || y == 0 || y == ROOM_HEIGHT_TILES - 1);
                Image currentTileSprite = isWall ? wallSprite : floorSprite;

                tiles[x][y] = new Tile(
                    isWall ? Tile.TileType.WALL : Tile.TileType.FLOOR,
                    !isWall, // walkable if not a wall
                    spritesAvailable ? currentTileSprite : null // Use null if sprites aren't loaded to force placeholder
                );
            }
        }

        // Add specific internal walls for Room 0 as an example
        if (this.roomID == 0 && wallSprite != null) { // Check wallSprite to avoid NPE if it failed to load
            // Make sure these coordinates are within bounds (1 to 10 for internal)
            if (tiles[5][3] != null) tiles[5][3] = new Tile(Tile.TileType.WALL, false, wallSprite);
            if (tiles[3][4] != null) tiles[3][4] = new Tile(Tile.TileType.WALL, false, wallSprite);
            if (tiles[3][5] != null) tiles[3][5] = new Tile(Tile.TileType.WALL, false, wallSprite);
            if (tiles[7][6] != null) tiles[7][6] = new Tile(Tile.TileType.WALL, false, wallSprite);
            if (tiles[7][7] != null) tiles[7][7] = new Tile(Tile.TileType.WALL, false, wallSprite);
        }
         // Example for another room (roomID == 1)
        if (this.roomID == 1 && wallSprite != null) {
            if (tiles[8][5] != null) tiles[8][5] = new Tile(Tile.TileType.WALL, false, wallSprite);
            if (tiles[5][6] != null) tiles[5][6] = new Tile(Tile.TileType.WALL, false, wallSprite);
        }


        if (!spritesAvailable) {
            System.out.println("Room " + getName() + " initialized with some placeholder graphics due to missing sprites.");
        } else {
            System.out.println("Room " + getName() + " initialized successfully with sprites.");
        }
    }

    public Tile getTile(int x, int y) {
        if (x >= 0 && x < ROOM_WIDTH_TILES && y >= 0 && y < ROOM_HEIGHT_TILES) {
            if (tiles[x][y] == null) { // Should not happen after initializeDefaultTiles
                System.err.println("CRITICAL: Tile at (" + x + "," + y + ") in Room " + getName() + " is null!");
                // Create a fallback tile to prevent NullPointerExceptions elsewhere
                tiles[x][y] = new Tile(Tile.TileType.EMPTY, false, null);
            }
            return tiles[x][y];
        }
        // Consider throwing an IllegalArgumentException for out-of-bounds access
        // or return a special "VOID" tile object. For now, null is okay if handled by callers.
        System.err.println("Warning: Accessing out-of-bounds tile at ("+x+","+y+") in Room " + getName());
        return null;
    }

    // setTile might be used by world generation or special events to change a tile fundamentally
    public void setTile(int x, int y, Tile tile) {
        if (x >= 0 && x < ROOM_WIDTH_TILES && y >= 0 && y < ROOM_HEIGHT_TILES && tile != null) {
            tiles[x][y] = tile;
        } else {
            System.err.println("Cannot set tile at invalid coordinates or with null tile.");
        }
    }

    public void addDoor(Door door) {
        if (door == null) return;
        if (!connectedDoors.contains(door)) {
            connectedDoors.add(door);
            // The Door object, when created, should use this Room instance to find its
            // corresponding tile and call tile.setEntityOnTile(door).
            // This method primarily tracks the logical connection.
            // The visual placement of the Door on the tile grid is handled when the Door is created
            // and its position is passed to its constructor, then tile.setEntityOnTile() is called.
            // The Room.addDoor in World.java seems to be doing this correctly.
            Position doorPosInThisRoom = door.getPositionInRoom(this);
            if (doorPosInThisRoom != null) {
                Tile tile = getTile(doorPosInThisRoom.getX(), doorPosInThisRoom.getY());
                if (tile != null) {
                    tile.setEntityOnTile(door); // This sets the Entity on the Tile
                } else {
                     System.err.println("Room.addDoor: Tile at " + doorPosInThisRoom + " is null for door " + door.getName());
                }
            } else {
                 System.err.println("Room.addDoor: Door " + door.getName() + " has no specified position in this room " + getName());
            }
        }
    }

    public void addLivingBeing(LivingBeing being) {
        if (being == null) return;
        if (!livingBeingsInRoom.contains(being)) {
            livingBeingsInRoom.add(being);
        }
    }

    public void removeLivingBeing(LivingBeing being) {
        if (being == null) return;
        livingBeingsInRoom.remove(being);
    }

    public void addItem(Item item) { // For items on the floor
        if (item == null || item.getPosition() == null) {
             System.err.println("Room " + getName() + ": Attempted to add null item or item with null position.");
            return;
        }
        if (!itemsInRoom.contains(item)) {
            itemsInRoom.add(item);
            // World should be notified if items are also global entities
            if (world != null && item instanceof Entity) { // All our Items are Entities
                world.addEntity((Entity)item); // Ensure world tracks it IF NECESSARY
                                              // Typically, items on floor are just in room's list
            }
        }
    }

    public void removeItem(Item item) { // From floor
        if (item == null) return;
        boolean removed = itemsInRoom.remove(item);
        if (removed && world != null && item instanceof Entity) {
            // world.removeEntity((Entity)item); // If world was tracking it globally
        }
    }

    public int getRoomID() { return roomID; }
    public List<LivingBeing> getLivingBeingsInRoom() { return new ArrayList<>(livingBeingsInRoom); } // Return copy
    public List<Item> getItemsInRoom() { return new ArrayList<>(itemsInRoom); } // Return copy

    /**
     * This is the primary method called by GamePanel to draw the entire room.
     * It iterates through its tiles, then floor items, then living beings.
     */
    public void drawContents(Graphics g, int tilePixelWidth, int tilePixelHeight) {
        // 1. Draw all Tiles (Tiles will call entityOnTile.draw(g, screenX, screenY, w, h) for static entities)
        for (int y = 0; y < ROOM_HEIGHT_TILES; y++) {
            for (int x = 0; x < ROOM_WIDTH_TILES; x++) {
                Tile currentTile = tiles[x][y];
                if (currentTile != null) {
                    currentTile.draw(g, x * tilePixelWidth, y * tilePixelHeight, tilePixelWidth, tilePixelHeight);
                } else {
                    g.setColor(Color.BLACK); // Fallback for null tile
                    g.fillRect(x * tilePixelWidth, y * tilePixelHeight, tilePixelWidth, tilePixelHeight);
                }
            }
        }

        // 2. Draw items on the floor
        for (Item item : new ArrayList<>(itemsInRoom)) {
            if (item.getOwner() == null && item.getPosition() != null) {
                item.draw(g,
                          item.getPosition().getX() * tilePixelWidth,
                          item.getPosition().getY() * tilePixelHeight,
                          tilePixelWidth, tilePixelHeight);
            }
        }

        // 3. Draw living beings (Players, NPCs)
        for (LivingBeing being : new ArrayList<>(livingBeingsInRoom)) {
            if (being.getHealth() > 0 || (being instanceof Player && world != null && world.isGameOver())) {
                if (being.getPosition() != null) { // Ensure being has a position
                    int beingScreenX = being.getPosition().getX() * tilePixelWidth;
                    int beingScreenY = being.getPosition().getY() * tilePixelHeight;
                    being.draw(g, beingScreenX, beingScreenY, tilePixelWidth, tilePixelHeight); // <<< CORRECTED CALL
                } else {
                    System.err.println("Room.drawContents: LivingBeing " + being.getName() + " has null position. Cannot draw.");
                }
            }
        }
    }

    /**
     * This method satisfies the Entity abstract contract.
     * A Room as a single entity is typically not drawn directly on the map like a character or item.
     * Its contents are drawn by drawContents().
     */
    @Override
    public void draw(Graphics g, int screenX, int screenY, int tilePixelWidth, int tilePixelHeight) {
        // This would be for drawing the Room itself as one object, if ever needed.
        // For example, on a mini-map. For the main game view, drawContents is used.
        // For now, it can be a no-op or draw a simple representation.
        // To avoid confusion, let's make it clear this isn't the main draw:
        // System.out.println("Room.draw(screenX, screenY, ...) called for " + getName() + " - Usually drawContents() is used.");

        // If you wanted to draw a border around the entire room if it was, for example, an icon:
        // g.setColor(Color.BLUE);
        // g.drawRect(screenX, screenY, ROOM_WIDTH_TILES * tilePixelWidth, ROOM_HEIGHT_TILES * tilePixelHeight);
    }

    // addLever method was specific and might be better handled by generic tile.setEntityOnTile
    // public void addLever(Lever lever, Position position) { ... }
    // Instead, in World.setupLeverPuzzle:
    // Lever lever1 = new Lever(...);
    // puzzleRoom.getTile(3,3).setEntityOnTile(lever1);
    // world.addEntity(lever1); // Ensure lever is also an Executable if it needs an update()
}