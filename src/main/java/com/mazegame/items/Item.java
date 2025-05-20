package com.mazegame.items;

import com.mazegame.core.Entity;
import com.mazegame.core.World;
import com.mazegame.characters.LivingBeing;
import com.mazegame.utils.Position;
import com.mazegame.ui.SpriteManager;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Color;

public abstract class Item extends Entity {
    protected LivingBeing owner;
    protected Image sprite;

    // Basic constructor
    public Item(String name) {
        super(name, null, null); // No world or position initially
        this.owner = null;
    }

    // Full constructor
    public Item(String name, World world, Position position) {
        super(name, world, position);
        this.owner = null;
        loadSprite();
    }

    // Template method for sprite loading - subclasses will override
    protected void loadSprite() {
        // Default implementation tries to load based on class name
        String spriteName = this.getClass().getSimpleName().toLowerCase() + ".png";
        this.sprite = SpriteManager.getSprite(spriteName);
    }

    public LivingBeing getOwner() {
        return owner;
    }

    public void setOwner(LivingBeing owner) {
        this.owner = owner;
        // If item is picked up (has owner), clear its position
        if (owner != null) {
            setPosition(null);
        }
    }

    public abstract void use(LivingBeing user);

    public void draw(Graphics g, int tileWidth, int tileHeight) {
        if (owner == null && position != null) {
            draw(g, position.getX() * tileWidth, position.getY() * tileHeight, 
                 tileWidth, tileHeight);
        }
    }

    @Override // From Entity
    public void draw(Graphics g, int screenX, int screenY, int tilePixelWidth, int tilePixelHeight) {
        // This draw method is called if an Item instance is set as tile.entityOnTile (less common)
        // OR if Room.draw specifically calls it for items on the floor, passing calculated screenX/Y.

        if (owner == null && this.sprite != null) { // Only draw if on the ground (not in inventory)
            g.drawImage(this.sprite,
                    screenX, // Use screenX provided by the caller (Room or Tile)
                    screenY, // Use screenY provided by the caller
                    tilePixelWidth, tilePixelHeight, null); // Use tile dimensions for item size
        } else if (owner == null) {
            // Placeholder for item on floor
            g.setColor(Color.GREEN);
            g.fillRect(screenX + tilePixelWidth / 3,
                       screenY + tilePixelHeight / 3,
                       tilePixelWidth / 3, tilePixelHeight / 3);
        }
        // If owned, it's in inventory, typically not drawn on the map this way.
    }

    protected void drawPlaceholder(Graphics g, int screenX, int screenY, int width, int height) {
        g.setColor(Color.GREEN);
        int margin = width / 3;
        g.fillRect(screenX + margin, screenY + margin, width - 2*margin, height - 2*margin);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(screenX + margin, screenY + margin, width - 2*margin, height - 2*margin);
    }

    @Override
    public String toString() {
        return String.format("%s[owner=%s, position=%s]", 
            getName(), 
            owner != null ? owner.getName() : "none",
            position != null ? position.toString() : "in inventory");
    }

    @Override
    public boolean isPassable() {
        return true; // Items on the floor are generally passable (player can stand on them to pick up)
    }
}