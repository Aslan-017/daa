package daa;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

final class ResultPlotter {
    private static final int WIDTH = 1_200;
    private static final int HEIGHT = 720;
    private static final int LEFT = 105;
    private static final int RIGHT = 110;
    private static final int TOP = 95;
    private static final int BOTTOM = 100;
    private static final Color[] COLORS = {
            new Color(36, 99, 180),
            new Color(224, 103, 38),
            new Color(39, 145, 91),
            new Color(156, 73, 163)
    };

    private ResultPlotter() {
    }

    static void generate(Path csvFile, Path outputDirectory) throws IOException {
        Map<String, Map<Integer, List<Row>>> randomRows = readRandomRows(csvFile);
        Files.createDirectories(outputDirectory);
        draw(randomRows, true, outputDirectory.resolve("execution-time-vs-n.png"));
        draw(randomRows, false, outputDirectory.resolve("recursion-depth-vs-n.png"));
    }

    private static Map<String, Map<Integer, List<Row>>> readRandomRows(Path csvFile) throws IOException {
        Map<String, Map<Integer, List<Row>>> result = new LinkedHashMap<>();
        List<String> lines = Files.readAllLines(csvFile, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            String[] columns = lines.get(i).split(",");
            if (!columns[1].equals("random")) {
                continue;
            }
            Row row = new Row(columns[0], Integer.parseInt(columns[2]),
                    Long.parseLong(columns[4]), Integer.parseInt(columns[5]));
            result.computeIfAbsent(row.algorithm, ignored -> new TreeMap<>())
                    .computeIfAbsent(row.size, ignored -> new ArrayList<>()).add(row);
        }
        return result;
    }

    private static void draw(Map<String, Map<Integer, List<Row>>> rows, boolean timePlot, Path output)
            throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
        graphics.setColor(new Color(35, 43, 55));
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 25));
        graphics.drawString(timePlot ? "Execution time vs. input size (random input)"
                : "Maximum recursion depth vs. input size (random input)", LEFT, 48);

        int plotWidth = WIDTH - LEFT - RIGHT;
        int plotHeight = HEIGHT - TOP - BOTTOM;
        int bottom = TOP + plotHeight;
        graphics.setColor(new Color(235, 238, 242));
        for (int tick = 0; tick <= 5; tick++) {
            int y = TOP + plotHeight * tick / 5;
            graphics.drawLine(LEFT, y, LEFT + plotWidth, y);
        }
        graphics.setColor(new Color(65, 73, 82));
        graphics.setStroke(new BasicStroke(2));
        graphics.drawLine(LEFT, TOP, LEFT, bottom);
        graphics.drawLine(LEFT, bottom, LEFT + plotWidth, bottom);

        List<Integer> sizes = rows.values().stream()
                .flatMap(bySize -> bySize.keySet().stream()).distinct().sorted().toList();
        double maximum = rows.values().stream()
                .flatMap(bySize -> bySize.values().stream())
                .flatMap(List::stream)
                .mapToDouble(row -> timePlot ? row.elapsedNanos / 1_000_000.0 : row.recursionDepth)
                .max().orElse(1.0);
        maximum = Math.max(1.0, maximum * 1.1);

        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        for (int tick = 0; tick <= 5; tick++) {
            double value = maximum * (5 - tick) / 5.0;
            int y = TOP + plotHeight * tick / 5;
            String label = timePlot
                    ? String.format(Locale.ROOT, "%.2f", value)
                    : String.format(Locale.ROOT, "%.0f", value);
            graphics.drawString(label, LEFT - 70, y + 5);
        }
        for (int i = 0; i < sizes.size(); i++) {
            int x = pointX(i, sizes.size(), plotWidth);
            graphics.drawLine(x, bottom, x, bottom + 5);
            graphics.drawString(Integer.toString(sizes.get(i)), x - 24, bottom + 28);
        }
        graphics.drawString("Input size n", LEFT + plotWidth / 2 - 34, HEIGHT - 24);
        graphics.rotate(-Math.PI / 2);
        graphics.drawString(timePlot ? "Time (milliseconds)" : "Maximum recursion depth",
                -TOP - plotHeight / 2 - 80, 30);
        graphics.rotate(Math.PI / 2);

        int legendX = WIDTH - RIGHT - 250;
        int legendY = TOP + 10;
        int seriesIndex = 0;
        for (Map.Entry<String, Map<Integer, List<Row>>> series : rows.entrySet()) {
            Color color = COLORS[seriesIndex % COLORS.length];
            graphics.setColor(color);
            graphics.setStroke(new BasicStroke(3));
            List<Integer> seriesSizes = new ArrayList<>(series.getValue().keySet());
            seriesSizes.sort(Comparator.naturalOrder());
            int previousX = 0;
            int previousY = 0;
            for (int i = 0; i < seriesSizes.size(); i++) {
                int size = seriesSizes.get(i);
                double value = median(series.getValue().get(size), timePlot);
                int x = pointX(sizes.indexOf(size), sizes.size(), plotWidth);
                int y = bottom - (int) Math.round(value / maximum * plotHeight);
                if (i > 0) {
                    graphics.drawLine(previousX, previousY, x, y);
                }
                graphics.fillOval(x - 5, y - 5, 10, 10);
                previousX = x;
                previousY = y;
            }
            graphics.fillRect(legendX, legendY + seriesIndex * 28 - 12, 22, 4);
            graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
            graphics.drawString(series.getKey(), legendX + 32, legendY + seriesIndex * 28 - 6);
            seriesIndex++;
        }
        graphics.dispose();
        ImageIO.write(image, "png", output.toFile());
    }

    private static int pointX(int index, int count, int plotWidth) {
        return count <= 1 ? LEFT + plotWidth / 2 : LEFT + index * plotWidth / (count - 1);
    }

    private static double median(List<Row> rows, boolean timePlot) {
        double[] values = rows.stream()
                .mapToDouble(row -> timePlot ? row.elapsedNanos / 1_000_000.0 : row.recursionDepth)
                .sorted().toArray();
        return values[values.length / 2];
    }

    private record Row(String algorithm, int size, long elapsedNanos, int recursionDepth) {
    }
}
