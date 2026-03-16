package edu.upb.chatupb_v2.VIews;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class MessageBubble extends JPanel {
    private final String text;
    private final boolean isOwn;
    private final ImageIcon image; // Nuevo: Soporte para imagen
    private static final int ARC = 16;
    private static final int MAX_WIDTH = 420;
    private static final int IMAGE_MAX_HEIGHT = 250; // Altura máxima para imágenes

    // Constructor para texto e imagen
    MessageBubble(String text, ImageIcon image, boolean isOwn) {
        this.text = text;
        this.isOwn = isOwn;
        this.image = image;
        setOpaque(false);
    }

    // Sobrecarga para solo texto
    MessageBubble(String text, boolean isOwn) {
        this(text, null, isOwn);
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(new Font("SF Pro Text", Font.PLAIN, 13));
        int bubbleW = 0;
        int bubbleH = 0;

        // 1. Calcular dimensiones si hay imagen
        if (image != null) {
            int rawW = image.getIconWidth();
            int rawH = image.getIconHeight();
            // Scale to fit within MAX_WIDTH and IMAGE_MAX_HEIGHT, preserving aspect ratio
            double scaleW = (double) (MAX_WIDTH - 28) / rawW;
            double scaleH = (double) IMAGE_MAX_HEIGHT / rawH;
            double scale = Math.min(Math.min(scaleW, scaleH), 1.0);
            bubbleW = (int) (rawW * scale);
            bubbleH = (int) (rawH * scale);
        }

        // 2. Calcular dimensiones del texto
        if (text != null && !text.isEmpty()) {
            int textW = fm.stringWidth(text);
            int wrappedTextW = Math.min(textW + 28, MAX_WIDTH);
            bubbleW = Math.max(bubbleW, wrappedTextW);

            // Estimación simple de líneas
            int lines = (int) Math.ceil((double)(textW + 28) / MAX_WIDTH);
            bubbleH += (fm.getHeight() * lines) + 20;
        } else {
            bubbleH += 20; // Padding si no hay texto
        }

        return new Dimension(bubbleW + 30, bubbleH + 10);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        Dimension pref = getPreferredSize();
        int bw = Math.min(w - 10, pref.width - 10);
        int x = isOwn ? w - bw - 5 : 5;
        int h = getHeight() - 5;

        // Sombra
        g2.setColor(new Color(0, 0, 0, 20));
        g2.fillRoundRect(x + 1, 2, bw, h, ARC, ARC);

        // Fondo del Bubble
        Color bubbleColor = isOwn ? new Color(0x007AFF) : new Color(0xE9E9EB); // Colores tipo iOS
        g2.setColor(bubbleColor);
        g2.fillRoundRect(x, 0, bw, h, ARC, ARC);

        int currentY = 10;

        // Renderizar Imagen
        if (image != null) {
            int rawW = image.getIconWidth();
            int rawH = image.getIconHeight();
            double scaleW = (double) (bw - 20) / rawW;
            double scaleH = (double) IMAGE_MAX_HEIGHT / rawH;
            double scale = Math.min(Math.min(scaleW, scaleH), 1.0);
            int imgW = (int) (rawW * scale);
            int imgH = (int) (rawH * scale);

            // Dibujar imagen centrada en el bubble
            g2.setClip(new RoundRectangle2D.Float(x + 10, currentY, imgW, imgH, 10, 10));
            g2.drawImage(image.getImage(), x + 10, currentY, imgW, imgH, null);
            g2.setClip(null);

            currentY += imgH + 10;
        }

        // Renderizar Texto
        if (text != null && !text.isEmpty()) {
            g2.setFont(new Font("SF Pro Text", Font.PLAIN, 13));
            g2.setColor(isOwn ? Color.WHITE : Color.BLACK);
            FontMetrics fm = g2.getFontMetrics();

            String[] words = text.split(" ");
            StringBuilder line = new StringBuilder();
            int maxTextW = bw - 24;

            for (String word : words) {
                String test = line.isEmpty() ? word : line + " " + word;
                if (fm.stringWidth(test) > maxTextW) {
                    g2.drawString(line.toString(), x + 12, currentY + fm.getAscent());
                    currentY += fm.getHeight();
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(test);
                }
            }
            g2.drawString(line.toString(), x + 12, currentY + fm.getAscent());
        }

        g2.dispose();
    }
}
