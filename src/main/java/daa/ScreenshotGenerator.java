package daa;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ScreenshotGenerator {
    private static final int WIDTH = 1_280;

    private ScreenshotGenerator() {
    }

    public static void writeTerminalScreenshot(Path output, String title, List<String> lines) {
        int height = Math.max(360, 145 + lines.size() * 31);
        BufferedImage image = new BufferedImage(WIDTH, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(new Color(25, 29, 36));
        graphics.fillRect(0, 0, WIDTH, height);
        graphics.setColor(new Color(48, 55, 66));
        graphics.fillRect(0, 0, WIDTH, 58);
        graphics.setColor(new Color(239, 83, 80));
        graphics.fillOval(22, 22, 13, 13);
        graphics.setColor(new Color(255, 202, 40));
        graphics.fillOval(45, 22, 13, 13);
        graphics.setColor(new Color(102, 187, 106));
        graphics.fillOval(68, 22, 13, 13);
        graphics.setColor(new Color(235, 239, 244));
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        graphics.drawString(title, 105, 38);
        graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        int y = 108;
        for (String line : lines) {
            graphics.setColor(new Color(119, 212, 154));
            graphics.drawString("> ", 35, y);
            graphics.setColor(new Color(231, 235, 241));
            graphics.drawString(line, 66, y);
            y += 31;
        }
        graphics.dispose();
        save(image, output);
    }

    public static void writePlotsScreenshot(Path timePlot, Path depthPlot, Path output) {
        try {
            BufferedImage timeImage = ImageIO.read(timePlot.toFile());
            BufferedImage depthImage = ImageIO.read(depthPlot.toFile());
            if (timeImage == null || depthImage == null) {
                throw new IOException("Could not read generated plot images");
            }
            int height = 1_500;
            BufferedImage image = new BufferedImage(WIDTH, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, WIDTH, height);
            graphics.setColor(new Color(35, 43, 55));
            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
            graphics.drawString("Experimental plots", 24, 30);
            graphics.drawImage(timeImage, 0, 42, WIDTH, 720, null);
            graphics.drawImage(depthImage, 0, 770, WIDTH, 720, null);
            graphics.dispose();
            save(image, output);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static void save(BufferedImage image, Path output) {
        try {
            Files.createDirectories(output.getParent());
            ImageIO.write(image, "png", output.toFile());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
