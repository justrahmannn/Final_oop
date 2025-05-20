package com.mazegame.core;

import com.mazegame.items.Chest; // Assuming Chest is in items
import com.mazegame.items.Lever; // Assuming Lever is in items
import com.mazegame.ui.SpriteManager;

import java.awt.Image;
import java.awt.Graphics;
import java.awt.Color;
// import java.awt.image.BufferedImage; // Not strictly needed if only using Image

public class Tile {
    public enum TileType {
        FLOOR, WALL, DOOR, CHEST, EMPTY, PLAYER_SPAWN, ENEMY_SPAWN,
        LEVER, TRAP_FLOOR
    }

    private TileType type;
    private boolean baseWalkable;   // Walkability of the tile type itself
    private Image sprite;           // Base sprite for the tile (e.g., "floor.png", "wall.png")
    private Entity entityOnTile;    // The interactive/blocking entity ON this tile

    // Placeholder colors
    private static final Color WALL_COLOR = new Color(100, 100, 100);
    private static final Color FLOOR_COLOR = new Color(210, 180, 140);
    private static final Color DOOR_PLACEHOLDER_COLOR = new Color(139, 69, 19); // Brown for door area
    private static final Color CHEST_PLACEHOLDER_COLOR = FLOOR_COLOR; // Chest is on floor
    private static final Color LEVER_PLACEHOLDER_COLOR = FLOOR_COLOR; // Lever is on floor
    private static final Color TRAP_PLACEHOLDER_COLOR = FLOOR_COLOR; // Trap is on floor
    private static final Color DEFAULT_COLOR = Color.BLACK;
    private static final Color BORDER_COLOR = new Color(50, 50, 50);

    public Tile(TileType type, boolean walkable, Image sprite) {
        this.type = type;
        this.baseWalkable = walkable;
        this.sprite = sprite; // This is the base sprite (e.g. "floor.png" or "wall.png")
        this.entityOnTile = null;
    }

    public TileType getType() { return type; }

    public void setType(TileType type) {
        this.type = type;
        switch (type) {
            case WALL:
                this.baseWalkable = false;
                break;
            case FLOOR:
            case EMPTY:
            case PLAYER_SPAWN:
            case ENEMY_SPAWN:
            case LEVER:     // The tile itself is walkable, the lever entity might not be "on"
            case TRAP_FLOOR: // The tile itself is walkable, trap state determines hazard
                this.baseWalkable = true;
                break;
            case DOOR:      // The space a door occupies is not walkable unless open
            case CHEST:     // Cannot walk on a chest
                this.baseWalkable = false;
                break;
            default:
                this.baseWalkable = false; // Default to not walkable for unknown types
                break;
        }
    }

    public boolean isWalkable() {
        if (!this.baseWalkable && this.type != TileType.DOOR) { // If it's a wall, definitely not walkable
            return false;
        }

        if (entityOnTile != null) {
            if (entityOnTile instanceof Door) {
                return ((Door) entityOnTile).isCurrentlyOpen();
            }
            // Entities like Chest, Lever make the specific tile they are ON not walkable through
            if (entityOnTile instanceof Chest || entityOnTile instanceof Lever) {
                return false;
            }
            // Traps don't inherently block movement, they deal damage. So, the tile remains "walkable".
            if (entityOnTile instanceof Trap) {
                return this.baseWalkable; // Depends on the base tile (e.g. TRAP_FLOOR might be on FLOOR)
            }
            // For any other entity type that might be on a tile and block it:
            // return false; or check a property on the entity like entityOnTile.isBlocking();
        }
        return this.baseWalkable; // If no blocking entity, rely on base walkability
    }

    public void setWalkable(boolean walkable) {
        this.baseWalkable = walkable;
    }

    public Image getSprite() { return sprite; } // Returns the base tile sprite
    public void setSprite(Image sprite) { this.sprite = sprite; }
    public Entity getEntityOnTile() { return entityOnTile; }

    public void setEntityOnTile(Entity entity) {
        this.entityOnTile = entity;
        if (entity instanceof Door) {
            this.type = TileType.DOOR;
            // Walkability is handled by Door.isCurrentlyOpen() via isWalkable()
        } else if (entity instanceof Chest) {
            this.type = TileType.CHEST;
            this.baseWalkable = false; // Can't walk ON a chest tile
        } else if (entity instanceof Lever) {
            this.type = TileType.LEVER;
            // baseWalkable might still be true (e.g. lever on floor)
            // isWalkable() will return false because an entity is on it.
        } else if (entity instanceof Trap) {
            this.type = TileType.TRAP_FLOOR; // Or could keep as FLOOR
            // baseWalkable is true, damage is separate
        }
    }

    /**
     * Main draw method for a tile.
     * It draws the base tile first, then any entity that is on this tile.
     */
    // @Override // Remove if Tile does not extend a class with this exact method signature
    public void draw(Graphics g, int screenX, int screenY, int tilePixelWidth, int tilePixelHeight) {
        // 1. Draw the base tile sprite (e.g., floor, or a generic door frame tile if type is DOOR)
        if (this.sprite != null) { // this.sprite is the base sprite of the TILE itself
            g.drawImage(this.sprite, screenX, screenY, tilePixelWidth, tilePixelHeight, null);
        } else {
            // Fallback if base sprite is null, draw placeholder based on type
            drawPlaceholderBase(g, screenX, screenY, tilePixelWidth, tilePixelHeight);
        }

        // 2. Draw the INTERACTIVE ENTITY on top of the base tile, if one exists
        if (entityOnTile != null) {
            // Pass the TILE's screen coordinates to the entity's draw method
            // The Entity's draw method is then responsible for drawing itself AT these coordinates.
            entityOnTile.draw(g, screenX, screenY, tilePixelWidth, tilePixelHeight);
        }
    }

    private void drawPlaceholderBase(Graphics g, int screenX, int screenY, int width, int height) {
        g.setColor(getColorForType());
        g.fillRect(screenX, screenY, width, height);
        g.setColor(BORDER_COLOR);
        g.drawRect(screenX, screenY, width - 1, height - 1);

        // Draw 'X' only for WALL type if it's using a placeholder
        if (this.type == TileType.WALL && this.sprite == null) {
            g.setColor(BORDER_COLOR.brighter());
            g.drawLine(screenX, screenY, screenX + width, screenY + height);
            g.drawLine(screenX + width, screenY, screenX, screenY + height);
        }
    }

    private Color getColorForType() {
        switch (type) {
            case WALL: return WALL_COLOR;
            case FLOOR: return FLOOR_COLOR;
            case DOOR: return DOOR_PLACEHOLDER_COLOR; // Base for door tile, actual door drawn by entity
            case CHEST: return CHEST_PLACEHOLDER_COLOR; // Base for chest tile
            case LEVER: return LEVER_PLACEHOLDER_COLOR; // Base for lever tile
            case TRAP_FLOOR: return TRAP_PLACEHOLDER_COLOR; // Base for trap tile
            case EMPTY: return Color.DARK_GRAY;
            case PLAYER_SPAWN: return FLOOR_COLOR;
            case ENEMY_SPAWN: return FLOOR_COLOR;
            default: return DEFAULT_COLOR;
        }
    }

    @Override // toString from Object is fine to override
    public String toString() {
        return String.format("Tile[type=%s, baseWalkable=%b, entityOnTile=%s]",
            type, baseWalkable, (entityOnTile != null ? entityOnTile.getName() : "none"));
    }
}