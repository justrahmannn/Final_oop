package com.mazegame.utils;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class SpriteManager {
    public static BufferedImage getSprite(String spriteName) {
        try {
            return ImageIO.read(SpriteManager.class.getResourceAsStream("/sprites/" + spriteName));
        } catch (IOException e) {
            System.err.println("Could not load sprite: " + spriteName);
            return null;
        }
    }
}