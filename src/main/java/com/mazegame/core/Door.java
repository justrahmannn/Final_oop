package com.mazegame.core;

import com.mazegame.interfaces.Activatable;
import com.mazegame.ui.SpriteManager;
import com.mazegame.utils.Position;
import java.awt.Graphics; // For drawing
import java.awt.Image;
import java.awt.Color;
// import com.mazegame.ui.SpriteManager; // For sprites

public class Door extends Entity implements Activatable {
    private Room room1;
    private Room room2;
    private boolean locked;
    private String keyId; // Optional: ID of the key required to unlock this door
    private boolean isCurrentlyOpen = false; // Initial state is closed
    private boolean forceable = false; // By default, doors are not forceable

    // Position of this door within room1 and room2
    private Position positionInRoom1;
    private Position positionInRoom2;

    private Image spriteOpen;
    private Image spriteClosed;

    public Door(String name, World world, Room room1, Position posInRoom1, Room room2, Position posInRoom2, boolean locked, String keyId) {
        super(name, world, posInRoom1); // Primary position, e.g., in room1
        this.room1 = room1;
        this.room2 = room2;
        this.positionInRoom1 = posInRoom1;
        this.positionInRoom2 = posInRoom2;
        this.locked = locked;
        this.keyId = keyId; // Can be null if no specific key needed

        this.spriteOpen = SpriteManager.getSprite("door_open.png");
        this.spriteClosed = SpriteManager.getSprite("door_closed.png");
        if (this.spriteOpen == null || this.spriteClosed == null) {
            System.err.println("Error: Door sprites not loaded for " + getName());
        }

        // Add this door to the rooms and update their tile maps
        if (room1 != null) {
            room1.addDoor(this);
            Tile doorTile1 = room1.getTile(posInRoom1.getX(), posInRoom1.getY());
            if (doorTile1 != null) {
                doorTile1.setType(Tile.TileType.DOOR);
                doorTile1.setEntityOnTile(this);
                // doorTile1.setSprite(SpriteManager.getSprite(locked ? "door_closed" : "door_open"));
            }
        }
        if (room2 != null) {
            // Note: A door entity is singular. When adding to room2, it's the same door.
            // Room2 might have its own list of doors, or it relies on finding doors in room1.
            // For simplicity, let's assume each room knows its doors.
            room2.addDoor(this); // You might need to adjust how rooms store/reference doors that lead *to* them
            Tile doorTile2 = room2.getTile(posInRoom2.getX(), posInRoom2.getY());
             if (doorTile2 != null) {
                doorTile2.setType(Tile.TileType.DOOR);
                doorTile2.setEntityOnTile(this);
                // doorTile2.setSprite(SpriteManager.getSprite(locked ? "door_closed" : "door_open"));
            }
        }
    }

    // New constructor with forceable parameter
    public Door(String name, World world, Room room1, Position posInRoom1, Room room2, Position posInRoom2,
                boolean locked, String keyId, boolean isForceable) {
        super(name, world, posInRoom1);
        this.room1 = room1;
        this.room2 = room2;
        this.positionInRoom1 = posInRoom1;
        this.positionInRoom2 = posInRoom2;
        this.locked = locked;
        this.keyId = keyId;
        this.forceable = isForceable;

        this.spriteOpen = SpriteManager.getSprite("door_open.png");
        this.spriteClosed = SpriteManager.getSprite("door_closed.png");
        if (this.spriteOpen == null || this.spriteClosed == null) {
            System.err.println("Error: Door sprites not loaded for " + getName());
        }

        if (room1 != null) {
            room1.addDoor(this);
            Tile doorTile1 = room1.getTile(posInRoom1.getX(), posInRoom1.getY());
            if (doorTile1 != null) {
                doorTile1.setType(Tile.TileType.DOOR);
                doorTile1.setEntityOnTile(this);
            }
        }
        if (room2 != null) {
            room2.addDoor(this);
            Tile doorTile2 = room2.getTile(posInRoom2.getX(), posInRoom2.getY());
            if (doorTile2 != null) {
                doorTile2.setType(Tile.TileType.DOOR);
                doorTile2.setEntityOnTile(this);
            }
        }
    }

    public Room getOtherRoom(Room currentRoom) {
        if (currentRoom == room1) return room2;
        if (currentRoom == room2) return room1;
        return null;
    }
    
    public Position getPositionInRoom(Room room) {
        if (room == room1) return positionInRoom1;
        if (room == room2) return positionInRoom2;
        return null;
    }

    public void passThrough() {
        if (isLocked()) {
            System.out.println(getName() + " is locked. Cannot pass.");
            return;
        }
        if (!isCurrentlyOpen) {
            isCurrentlyOpen = true;
            System.out.println(getName() + " opened for passage.");
            updateTileWalkability(true);
        }
    }

    @Override
    public void open() {
        if (isLocked()) {
            System.out.println(getName() + " is locked. Cannot open.");
            return;
        }
        if (!isCurrentlyOpen) {
            isCurrentlyOpen = true;
            System.out.println(getName() + " is now open.");
            updateTileWalkability(true);
        }
    }

    @Override
    public void close() {
        if (isCurrentlyOpen) {
            isCurrentlyOpen = false;
            System.out.println(getName() + " is now closed.");
            updateTileWalkability(false);
        }
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    public void unlock() {
        if (locked) {
            this.locked = false;
            System.out.println(getName() + " unlocked.");
            // Door isn't necessarily "open" just because it's unlocked.
        }
    }
    
    public void forceOpen() {
        if (forceable && isLocked()) {
            System.out.println(getName() + " is being forced open!");
            unlock(); // Sets locked = false
            open();   // Sets isCurrentlyOpen = true
        } else if (!isLocked()) {
            System.out.println(getName() + " is already unlocked/open, no need to force.");
        } else {
            System.out.println(getName() + " cannot be forced.");
        }
    }

    public String getKeyId() {
        return keyId;
    }

    public void setForceable(boolean forceable) {
        this.forceable = forceable;
    }

    public boolean canBeForcedOpen() {
        return forceable;
    }
    
    /**
     * Sets the locked state of the door.
     * @param lockedStatus true to lock the door, false to unlock.
     */
    public void setLockedState(boolean lockedStatus) {
        this.locked = lockedStatus;
        // Optionally update visuals if needed:
        // updateSpriteAndTile(isCurrentlyOpen);
    }
    
    private void updateSpriteAndTile(boolean openState) {
        if (room1 != null && positionInRoom1 != null) {
            Tile tile1 = room1.getTile(positionInRoom1.getX(), positionInRoom1.getY());
            setTileAppearance(tile1, openState);
        }
        if (room2 != null && positionInRoom2 != null) {
            Tile tile2 = room2.getTile(positionInRoom2.getX(), positionInRoom2.getY());
            setTileAppearance(tile2, openState);
        }
    }

    private void setTileAppearance(Tile tile, boolean openState) {
        if (tile != null && tile.getType() == Tile.TileType.DOOR) {
            // Sprite handling will be implemented when SpriteManager is ready
            tile.setEntityOnTile(this);
        }
    }

    private void updateTileWalkability(boolean isOpenAndThusWalkable) {
        if (room1 != null && positionInRoom1 != null) {
            Tile t1 = room1.getTile(positionInRoom1.getX(), positionInRoom1.getY());
            if (t1 != null && t1.getType() == Tile.TileType.DOOR) t1.setWalkable(isOpenAndThusWalkable);
        }
        if (room2 != null && positionInRoom2 != null) {
            Tile t2 = room2.getTile(positionInRoom2.getX(), positionInRoom2.getY());
            if (t2 != null && t2.getType() == Tile.TileType.DOOR) t2.setWalkable(isOpenAndThusWalkable);
        }
    }

    public boolean isCurrentlyOpen() {
        return this.isCurrentlyOpen;
    }

    @Override
    public void draw(Graphics g, int screenX, int screenY, int tilePixelWidth, int tilePixelHeight) {
        Image spriteToDraw = this.isCurrentlyOpen ? this.spriteOpen : this.spriteClosed;

        if (spriteToDraw != null) {
            g.drawImage(spriteToDraw,
                    screenX, // Use the screenX provided by the Tile
                    screenY, // Use the screenY provided by the Tile
                    tilePixelWidth, tilePixelHeight, null);
        } else {
            // Fallback placeholder drawing AT screenX, screenY
            Color doorColor = this.isCurrentlyOpen ? Color.GREEN.darker() : (this.isLocked() ? Color.RED.darker() : new Color(139, 69, 19));
            g.setColor(doorColor);
            g.fillRect(screenX + tilePixelWidth / 4, // Offset within the tile for placeholder
                       screenY,
                       tilePixelWidth / 2, tilePixelHeight);
        }
    }
}