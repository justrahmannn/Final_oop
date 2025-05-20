package com.mazegame.items;

import com.mazegame.core.Door;
import com.mazegame.core.Entity;
import com.mazegame.core.Tile;
import com.mazegame.core.World;
import com.mazegame.characters.LivingBeing;
import com.mazegame.interfaces.Activatable;
import com.mazegame.utils.Position;
// import com.mazegame.ui.SpriteManager;

public class Crowbar extends Item {
    private int durability;
    private static final int MAX_DURABILITY = 5; // Example: 5 uses

    public Crowbar(String name, World world, Position position) {
        super(name, world, position);
        this.durability = MAX_DURABILITY;
        // this.sprite = SpriteManager.getSprite("crowbar.png");
    }

    public int getDurability() {
        return durability;
    }

    @Override
    public void use(LivingBeing user) {
        System.out.println("Crowbar.use() called by: " + user.getName() + " for item: " + this.getName()); // VERY FIRST LINE

        if (durability <= 0) {
            System.out.println(getName() + " is broken!");
            user.getInventory().remove(this); // Optionally remove if found broken in inventory
            if (world != null) world.removeEntity(this);
            return;
        }

        System.out.println(user.getName() + " tries to use " + getName() + " (Durability: " + durability + "/" + MAX_DURABILITY + ")");
        boolean actionTakenOnObject = false;

        // Iterate through adjacent tiles to find a target
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue; // Skip self tile

                Position adjacentPos = new Position(user.getPosition().getX() + dx, user.getPosition().getY() + dy);
                if (user.getCurrentRoom() == null) return; // Should not happen
                Tile adjacentTile = user.getCurrentRoom().getTile(adjacentPos.getX(), adjacentPos.getY());

                if (adjacentTile != null && adjacentTile.getEntityOnTile() != null) {
                    Entity entityOnTile = adjacentTile.getEntityOnTile();
                    // Check if it's a Door
                    if (entityOnTile instanceof Door) {
                        Door door = (Door) entityOnTile;
                        System.out.println("  Crowbar found adjacent Door: " + door.getName() + ", Locked: " + door.isLocked() + ", Forceable: " + door.canBeForcedOpen());
                        if (door.isLocked() && door.canBeForcedOpen()) {
                            door.forceOpen();
                            System.out.println("  SUCCESS: " + getName() + " forced open " + door.getName() + "!");
                            actionTakenOnObject = true;
                            break;
                        } else if (!door.isLocked()) {
                            System.out.println("  INFO: " + door.getName() + " is already open/unlocked.");
                        } else {
                            System.out.println("  FAIL: " + door.getName() + " cannot be forced open by a crowbar.");
                        }
                    }
                    // Check if it's a Chest
                    else if (entityOnTile instanceof Chest) {
                        Chest chest = (Chest) entityOnTile;
                        System.out.println("  Crowbar found adjacent Chest: " + chest.getName() + ", Locked: " + chest.isLocked() + ", Forceable: " + chest.canBeForcedOpen());
                        if (chest.isLocked() && chest.canBeForcedOpen()) {
                            chest.forceOpen();
                            System.out.println("  SUCCESS: " + getName() + " forced open " + chest.getName() + "!");
                            actionTakenOnObject = true;
                            break;
                        } else if (!chest.isLocked()) {
                            System.out.println("  INFO: " + chest.getName() + " is already unlocked/open.");
                        } else {
                            System.out.println("  FAIL: " + chest.getName() + " cannot be forced open by a crowbar.");
                        }
                    }
                    if (actionTakenOnObject) break;
                }
            }
            if (actionTakenOnObject) break;
        }

        if (actionTakenOnObject) {
            this.durability--;
            System.out.println("  " + getName() + " durability: " + this.durability + "/" + MAX_DURABILITY);
            if (this.durability <= 0) {
                System.out.println("  " + getName() + " broke!");
                user.getInventory().remove(this);
                if (world != null) world.removeEntity(this);
            }
        } else {
            System.out.println("  Nothing nearby that is locked and can be forced open with " + getName() + ".");
        }
    }
}