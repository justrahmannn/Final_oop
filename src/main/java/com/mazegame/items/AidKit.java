package com.mazegame.items;

import com.mazegame.core.World;
import com.mazegame.characters.LivingBeing;
import com.mazegame.utils.Position;
import com.mazegame.utils.SpriteManager;

public class AidKit extends Item {
    private int healAmount;

    public AidKit(String name, World world, Position position, int healAmount) {
        super(name, world, position); // Position can be null if starting in inventory/chest
        this.healAmount = healAmount;
    }

    @Override
    public void use(LivingBeing user) {
        if (user == null) return;
        int healAmount = this.healAmount; // or whatever your field is called
        int before = user.getHealth();
        user.heal(healAmount);
        int after = user.getHealth();
        System.out.println(user.getName() + " used " + getName() + " and healed for " + (after - before) + " (now " + after + "/" + user.getMaxHealth() + ")");
        user.getInventory().remove(this);
        if (world != null) world.removeEntity(this);
    }

    @Override
    protected void loadSprite() {
        this.sprite = SpriteManager.getSprite("aid_kit.png");
        if (this.sprite == null) {
            System.out.println("Warning: Could not load sprite for aid kit");
        }
    }
}