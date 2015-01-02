package com.logginghub.utils;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ImageUtils
{
    
    public static BufferedImage toImage(Component c, int width, int height) {
        c.setSize(width, height);
        BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        c.paint(im.getGraphics());       
        return im;        
    }
    
    public static void toFile(Component c, int width, int height, File file) {
        BufferedImage im = toImage(c, width, height);
        try {
            ImageIO.write(im, "PNG", file);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }
    
    public static BufferedImage copy(BufferedImage bi)
    {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public static BufferedImage createScaledImage(BufferedImage image, int scaleFactor)
    {
        final BufferedImage zoomed = new BufferedImage(image.getWidth() * scaleFactor, image.getHeight() * scaleFactor, image.getType());
        Graphics2D graphics2d = zoomed.createGraphics();
        graphics2d.drawImage(image,
                             0,
                             0,
                             image.getWidth() * scaleFactor,
                             image.getHeight() * scaleFactor,
                             0,
                             0,
                             image.getWidth(),
                             image.getHeight(),
                             null);
        graphics2d.dispose();
        return zoomed;
    }

    public static BufferedImage copyTopHalf(BufferedImage image)
    {
        final BufferedImage cropped = new BufferedImage(image.getWidth(), image.getHeight() / 2, image.getType());
        Graphics2D graphics2d = cropped.createGraphics();
        graphics2d.drawImage(image, 0, 0, image.getWidth(), image.getHeight() / 2, 0, 0, image.getWidth(), image.getHeight() / 2, null);
        graphics2d.dispose();
        return cropped;
    }

    public static BufferedImage copyBottomHalf(BufferedImage image)
    {
        final BufferedImage cropped = new BufferedImage(image.getWidth(), image.getHeight() / 2, image.getType());
        Graphics2D graphics2d = cropped.createGraphics();
        graphics2d.drawImage(image,
                             0,
                             0,
                             image.getWidth(),
                             image.getHeight() / 2,
                             0,
                             image.getHeight() / 2,
                             image.getWidth(),
                             image.getHeight(),
                             null);
        graphics2d.dispose();
        return cropped;
    }

    public static BufferedImage[] splitVertically(BufferedImage top, int elements)
    {
        int currentY = 0;
        int stepY = top.getHeight() / elements;
        BufferedImage[] images = new BufferedImage[elements];
        for (int i = 0; i < elements; i++)
        {
            images[i] = croppedCopy(top, 0, currentY, top.getWidth(), currentY + stepY);
            currentY += stepY;
        }
        return images;
    }

    public static BufferedImage croppedCopy(BufferedImage image, int startX, int startY, int endX, int endY)
    {
        int width = endX - startX;
        int height = endY - startY;
        BufferedImage cropped = new BufferedImage(width, height, image.getType());
        Graphics2D graphics2d = cropped.createGraphics();
        graphics2d.drawImage(image, 0, 0, width, height, startX, startY, endX, endY, null);
        graphics2d.dispose();
        return cropped;
    }

    public static BufferedImage croppedCopy(BufferedImage championRow, Rectangle rect)
    {
        return croppedCopy(championRow, rect.x, rect.y, rect.x + rect.width, rect.y + rect.height);
    }

    public static BufferedImage[] splitHorizontally(BufferedImage image, int items, int itemWidth, int gap)
    {
        int currentX = 0;
        int stepX = itemWidth + gap;
        BufferedImage[] images = new BufferedImage[items];
        for (int i = 0; i < items; i++)
        {
            images[i] = croppedCopy(image, currentX, 0, currentX + itemWidth, image.getHeight());
            currentX += stepX;
        }
        return images;
    }

    public static void showInTestFrame(JComponent relativeParent, BufferedImage[] items, int scaleFactor)
    {
        JPanel panel = new JPanel(new FlowLayout());
        for (int i = 0; i < items.length; i++)
        {
            JLabel label = new JLabel();
            label.setIcon(new ImageIcon(createScaledImage(items[i], scaleFactor)));
            panel.add(label);
        }

        JFrame frame = new JFrame();
        frame.setSize(200, 200);
        frame.setLocationRelativeTo(relativeParent);
        frame.getContentPane().add(new JScrollPane(panel));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    public static JFrame createTestFrame()
    {
        JPanel panel = new JPanel(new FlowLayout());
        JFrame frame = new JFrame();
        frame.setSize(200, 200);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        return frame;
    }

    public static void showInTestFrame(JComponent parent, BufferedImage copy, int scaleFactor)
    {
        showInTestFrame(parent, new BufferedImage[] { copy }, scaleFactor);
    }

    public interface PixelFilter
    {
        boolean accept(int r, int g, int b);
    }

    public static class DarkPixelsFilter implements PixelFilter
    {

        private final int thresholdValue;

        public DarkPixelsFilter(int thresholdValue)
        {
            this.thresholdValue = thresholdValue;
        }

        public boolean accept(int r, int g, int b)
        {
            int total = r + g + b;
            return total > thresholdValue;
        }

    }

    public static int[][] generateHistogram(BufferedImage image, PixelFilter filter)
    {
        int height = image.getHeight();
        int width = image.getWidth();
        int[][] bins = new int[3][256];

        Raster raster = image.getRaster();
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                int r = raster.getSample(i, j, 0);
                int g = raster.getSample(i, j, 1);
                int b = raster.getSample(i, j, 2);

                if (filter.accept(r, g, b))
                {
                    bins[0][r]++;
                    bins[1][g]++;
                    bins[2][b]++;
                }
            }
        }

        return bins;
    }

    public static int compareHistograms(int[][] a, int[][] b)
    {
        int score = 0;

        int bands = a.length;
        int buckets = a[0].length;

        for (int i = 0; i < bands; i++)
        {
            for (int j = 0; j < buckets; j++)
            {
                int aCount = a[i][j];
                int bCount = b[i][j];

                int difference = Math.abs(aCount - bCount);
                score += difference;
            }
        }

        return score;
    }

    public static float compareColouration(int[][] a, int[][] b)
    {
        int score = 0;

        int bands = a.length;
        int buckets = a[0].length;

        for (int i = 0; i < bands; i++)
        {
            for (int j = 0; j < buckets; j++)
            {
                int aCount = a[i][j];
                int bCount = b[i][j];

                int difference = Math.abs(aCount - bCount);
                score += difference;
            }
        }

        int aRedSum = sum(a[0]);
        int aGreenSum = sum(a[1]);
        int aBlueSum = sum(a[2]);

        int bRedSum = sum(b[0]);
        int bGreenSum = sum(b[1]);
        int bBlueSum = sum(b[2]);

        float redRatio = aRedSum / (float) bRedSum;
        float greenRatio = aGreenSum / (float) bGreenSum;
        float blueRatio = aBlueSum / (float) bBlueSum;

        float redDelta = Math.abs(1 - redRatio);
        float greenDelta = Math.abs(1 - greenRatio);
        float blueDelta = Math.abs(1 - blueRatio);

        float totalDelta = redDelta + greenDelta + blueDelta;

        return totalDelta;
    }

    private static int sum(int[] is)
    {
        int total = 0;
        for (int i = 0; i < is.length; i++)
        {
            total += is[i] * i;
        }
        return total;
    }

    public static void showInTestFrame(JComponent panel)
    {
        JFrame frame = new JFrame();
        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    /**
     * Hardcoded to 4 sectors at the moment
     * 
     * @param a
     * @param b
     */
    public static int sectorHistorgramCompare(BufferedImage a, BufferedImage b)
    {
        int result = Integer.MAX_VALUE;

        int filter = 10;

        int sqrtSectors = 3;
        Rectangle[] aSectors = buildSectors(a, sqrtSectors);
        Rectangle[] bSectors = buildSectors(b, sqrtSectors);

        int[][] aAverages = buildPixelAverages(a, aSectors);
        int[][] bAverages = buildPixelAverages(b, bSectors);

        int totalDelta = 0;
        int totalSectors = sqrtSectors * sqrtSectors;
        for (int i = 0; i < totalSectors; i++)
        {
            int[] aAverage = aAverages[i];
            int[] bAverage = bAverages[i];

            int redDelta = Math.abs(aAverage[0] - bAverage[0]);
            int greenDelta = Math.abs(aAverage[0] - bAverage[0]);
            int blueDelta = Math.abs(aAverage[0] - bAverage[0]);

            if (redDelta > filter || greenDelta > filter || blueDelta > filter)
            {
                totalDelta = Integer.MAX_VALUE;
                break;
            }
            else
            {
                totalDelta += redDelta + greenDelta + blueDelta;
            }
        }

        result = totalDelta / 3 / totalSectors;
        return result;
    }

    private static int[][] buildPixelAverages(BufferedImage a, Rectangle[] sectors)
    {
        WritableRaster raster = a.getRaster();

        int[][] result = new int[sectors.length][];

        for (int s = 0; s < sectors.length; s++)
        {
            Rectangle sector = sectors[s];

            int[] total = new int[4];
            int[] pixel = new int[4];
            for (int x = sector.x; x < sector.x + sector.width; x++)
            {
                for (int y = sector.y; y < sector.y + sector.height; y++)
                {
                    pixel = raster.getPixel(x, y, pixel);
                    total[0] += pixel[0];
                    total[1] += pixel[1];
                    total[2] += pixel[2];
                }
            }

            int pixels = sector.width * sector.height;
            int[] average = new int[] { total[0] / pixels, total[1] / pixels, total[2] / pixels };
            result[s] = average;
        }

        return result;
    }

    private static Rectangle[] buildSectors(BufferedImage a, int sqrtSectors)
    {
        int rows = sqrtSectors;
        int columns = sqrtSectors;

        int sectorWidth = a.getWidth() / columns;
        int sectorHeight = a.getHeight() / columns;

        Rectangle[] sectors = new Rectangle[sqrtSectors * sqrtSectors];
        for (int x = 0; x < columns; x++)
        {
            for (int y = 0; y < rows; y++)
            {
                int sectorStartX = x * sectorWidth;
                int sectorStartY = y * sectorHeight;

                int index = x + y * columns;
                sectors[index] = new Rectangle(sectorStartX, sectorStartY, sectorWidth, sectorHeight);
            }
        }

        return sectors;
    }

    public static void showInDebugDialog(JComponent parent, BufferedImage image)
    {
        final JDialog dialog = new JDialog();

        dialog.setLayout(new FlowLayout());
        dialog.add(new JLabel(new ImageIcon(image)));
        JButton button = new JButton("OK");
        button.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                dialog.dispose();
            }
        });
        dialog.add(button);

//        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.setTitle("Debug image viewer");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLocationRelativeTo(parent);
        dialog.setSize(300, 200);
        dialog.setVisible(true);
    }
    
    public static class FilterSettings
    {
        public int[] foreground;
        public int foregroundThreshold;
        public int[] replaceForeground;
        public int[] replaceBackground;

        public boolean isForeground(int[] pixelValues)
        {
            int delta = Math.abs(pixelValues[0] - foreground[0]) +
                        Math.abs(pixelValues[1] - foreground[1]) +
                        Math.abs(pixelValues[2] - foreground[2]);

            delta = delta / 3;

            boolean isForeground = delta <= foregroundThreshold;
            return isForeground;
        }
    }

    public static BufferedImage replace(BufferedImage image, FilterSettings settings)
    {
        BufferedImage copy = ImageUtils.copy(image);

        WritableRaster bitmap = copy.getRaster();

        for (int x = 0; x < image.getWidth(); x++)
        {
            for (int y = 0; y < image.getHeight(); y++)
            {
                int[] values = new int[4];
                values = bitmap.getPixel(x, y, values);

                if (settings.isForeground(values))
                {
                    // This is foreground
                    if (settings.replaceForeground != null)
                    {
                        bitmap.setPixel(x, y, settings.replaceForeground);
                    }
                }
                else
                {
                    // This is background
                    if (settings.replaceBackground != null)
                    {
                        bitmap.setPixel(x, y, settings.replaceBackground);
                    }
                }
            }
        }

        copy.setData(bitmap);
        return copy;
    }

    
}
