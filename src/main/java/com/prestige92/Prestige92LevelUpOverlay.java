package com.prestige92;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class Prestige92LevelUpOverlay extends Overlay
{
    private static final long DISPLAY_TIME = 5000;
    private static final long FIREWORK_LIFETIME = 1200;

    private final Client client;
    private final Random random = new Random();

    private final List<FireworkParticle> particles = new ArrayList<>();

    private boolean active = false;
    private long startTime;

    private Skill skill;
    private int prestigeLevel;

    @Inject
    public Prestige92LevelUpOverlay(Client client)
    {
        this.client = client;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.HIGHEST);
    }

    public void showLevelUp(Skill skill, int prestigeLevel)
    {
        this.skill = skill;
        this.prestigeLevel = prestigeLevel;
        this.startTime = System.currentTimeMillis();
        this.active = true;

        particles.clear();

        createFirework(-180, -80);
        createFirework(180, -80);
        createFirework(-120, 80);
        createFirework(120, 80);
        createFirework(0, -130);
    }

    public void hide()
    {
        active = false;
        particles.clear();
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!active || skill == null)
        {
            return null;
        }

        long elapsed = System.currentTimeMillis() - startTime;

        if (elapsed >= DISPLAY_TIME)
        {
            hide();
            return null;
        }

        int canvasWidth = client.getCanvasWidth();
        int canvasHeight = client.getCanvasHeight();

        int centerX = canvasWidth / 2;
        int centerY = canvasHeight / 2;

        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        drawFireworks(graphics, centerX, centerY, elapsed);
        drawLevelUpMessage(graphics, centerX, centerY);

        return null;
    }

    private void drawLevelUpMessage(
            Graphics2D graphics,
            int centerX,
            int centerY
    )
    {
        String title = "Congratulations!";
        String line1 = "You've reached Prestige level "
                + prestigeLevel;
        String line2 = "in " + skill.getName() + "!";

        Font titleFont = new Font(
                "Arial",
                Font.BOLD,
                24
        );

        Font messageFont = new Font(
                "Arial",
                Font.BOLD,
                18
        );

        graphics.setFont(titleFont);

        int titleWidth = graphics
                .getFontMetrics()
                .stringWidth(title);

        int titleX = centerX - titleWidth / 2;
        int titleY = centerY - 35;

        drawOutlinedText(
                graphics,
                title,
                titleX,
                titleY,
                new Color(255, 215, 0)
        );

        graphics.setFont(messageFont);

        int line1Width = graphics
                .getFontMetrics()
                .stringWidth(line1);

        int line2Width = graphics
                .getFontMetrics()
                .stringWidth(line2);

        drawOutlinedText(
                graphics,
                line1,
                centerX - line1Width / 2,
                centerY,
                Color.WHITE
        );

        drawOutlinedText(
                graphics,
                line2,
                centerX - line2Width / 2,
                centerY + 25,
                Color.WHITE
        );
    }

    private void drawOutlinedText(
            Graphics2D graphics,
            String text,
            int x,
            int y,
            Color colour
    )
    {
        graphics.setColor(Color.BLACK);

        graphics.drawString(text, x - 2, y);
        graphics.drawString(text, x + 2, y);
        graphics.drawString(text, x, y - 2);
        graphics.drawString(text, x, y + 2);

        graphics.setColor(colour);
        graphics.drawString(text, x, y);
    }

    private void createFirework(int offsetX, int offsetY)
    {
        for (int i = 0; i < 25; i++)
        {
            double angle =
                    random.nextDouble() * Math.PI * 2;

            double speed =
                    1.0 + random.nextDouble() * 3.0;

            Color colour = getRandomFireworkColour();

            particles.add(
                    new FireworkParticle(
                            offsetX,
                            offsetY,
                            Math.cos(angle) * speed,
                            Math.sin(angle) * speed,
                            colour
                    )
            );
        }
    }

    private Color getRandomFireworkColour()
    {
        Color[] colours =
                {
                        new Color(255, 215, 0),
                        new Color(255, 255, 255),
                        new Color(255, 120, 0),
                        new Color(255, 80, 80),
                        new Color(100, 180, 255),
                        new Color(150, 255, 120)
                };

        return colours[random.nextInt(colours.length)];
    }

    private void drawFireworks(
            Graphics2D graphics,
            int centerX,
            int centerY,
            long elapsed
    )
    {
        Iterator<FireworkParticle> iterator =
                particles.iterator();

        while (iterator.hasNext())
        {
            FireworkParticle particle =
                    iterator.next();

            if (elapsed > FIREWORK_LIFETIME)
            {
                iterator.remove();
                continue;
            }

            double time = elapsed / 16.0;

            int x = centerX
                    + particle.startX
                    + (int) (particle.velocityX * time);

            int y = centerY
                    + particle.startY
                    + (int) (
                    particle.velocityY * time
                            + 0.015 * time * time
            );

            float fade =
                    1.0f
                            - ((float) elapsed / FIREWORK_LIFETIME);

            fade = Math.max(
                    0.0f,
                    Math.min(1.0f, fade)
            );

            Color colour = new Color(
                    particle.colour.getRed(),
                    particle.colour.getGreen(),
                    particle.colour.getBlue(),
                    (int) (255 * fade)
            );

            graphics.setColor(colour);

            graphics.fillOval(
                    x - 3,
                    y - 3,
                    6,
                    6
            );
        }
    }

    private static class FireworkParticle
    {
        private final int startX;
        private final int startY;

        private final double velocityX;
        private final double velocityY;

        private final Color colour;

        private FireworkParticle(
                int startX,
                int startY,
                double velocityX,
                double velocityY,
                Color colour
        )
        {
            this.startX = startX;
            this.startY = startY;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.colour = colour;
        }
    }
}