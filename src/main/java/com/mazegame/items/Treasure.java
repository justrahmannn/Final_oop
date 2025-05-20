package com.mazegame.items;

import com.mazegame.core.World;
import com.mazegame.characters.LivingBeing;
import com.mazegame.utils.Position;

public class Treasure extends Item {

    public Treasure(String name, World world, Position position) {
        super(name, world, position);
        // this.sprite = SpriteManager.getSprite("treasure.png"); // Or "exit_portal.png"
    }

    @Override
    public void use(LivingBeing user) {
        // "Using" the treasure means the player has won.
        winGame(user);
    }

    // Call this when player interacts with the Treasure on the ground OR uses it from inventory
    public void winGame(LivingBeing user) {
        if (world != null) {
            System.out.println(user.getName() + " has found the " + getName() + "! YOU WIN!");
            world.setPlayerWon(true);
            // Optional: remove treasure from world if it's an entity picked up
            // if (this.owner == user) { // If picked up
            //    user.getInventory().remove(this);
            // }
            // world.removeEntity(this);
        }
    }
}