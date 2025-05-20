package com.mazegame.core;

import com.mazegame.utils.Position;
import java.awt.Graphics;
import java.awt.Image;

public abstract class Entity {
    protected String name;
    protected World world;
    protected Position position; // Tile coordinates (e.g., 5, 3)
    protected Image sprite;      // Optional: a default sprite for the entity type

    public Entity(String name, World world, Position position) {
        this.name = name;
        this.world = world;
        this.position = position; // This is the entity's position in tile coordinates
    }

    public String getName() {
        return name;
    }

    public World getWorld() {
        return world;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Image getSprite() {
        return this.sprite;
    }

    /**
     * Draws the entity at the given screen coordinates.
     * @param g Graphics context
     * @param screenX The top-left X screen coordinate where this entity (occupying a tile) should be drawn
     * @param screenY The top-left Y screen coordinate
     * @param tilePixelWidth The width of a tile cell in pixels
     * @param tilePixelHeight The height of a tile cell in pixels
     */
    public abstract void draw(Graphics g, int screenX, int screenY, int tilePixelWidth, int tilePixelHeight);

    /**
     * Whether this entity can be walked over.
     * Override in subclasses for open doors, items, etc.
     */
    public boolean isPassable() {
        return false; // Default: most entities are not passable
    }
}