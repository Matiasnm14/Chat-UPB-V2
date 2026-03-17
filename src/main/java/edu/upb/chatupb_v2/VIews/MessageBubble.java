package edu.upb.chatupb_v2.VIews;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MessageBubble extends JPanel {
    private final String text;
    private final boolean isOwn;
    private final ImageIcon image;
    private final String timestamp;   // Hora de llegada, e.g. "14:35"
    private final boolean isRead;     // true = doble check azul, false = doble check gris

    private static final int ARC = 16;
    private static final int MAX_WIDTH = 420;
    private static final int IMAGE_MAX_HEIGHT = 250;
    private static final Font META_FONT = new Font("SF Pro Text", Font.PLAIN, 10);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // ── Constructores ────────────────────────────────────────────────────────

    /** Texto + imagen, con hora e indicador de lectura. */
    MessageBubble(String text, ImageIcon image, boolean isOwn,
                  LocalTime time, boolean isRead) {
        this.text      = text;
        this.image     = image;
        this.isOwn     = isOwn;
        this.timestamp = (time != null) ? time.format(TIME_FMT) : "";
        this.isRead    = isRead;
        setOpaque(false);
    }

    /** Solo texto, con hora e indicador de lectura. */
    MessageBubble(String text, boolean isOwn, LocalTime time, boolean isRead) {
        this(text, null, isOwn, time, isRead);
    }

    /** Solo texto, hora actual, sin leer (compatibilidad con código existente). */
    MessageBubble(String text, boolean isOwn) {
        this(text, null, isOwn, LocalTime.now(), false);
    }

    /** Texto + imagen, hora actual, sin leer (compatibilidad con código existente). */
    MessageBubble(String text, ImageIcon image, boolean isOwn) {
        this(text, image, isOwn, LocalTime.now(), false);
    }

    // ── Tamaño preferido ─────────────────────────────────────────────────────

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm     = getFontMetrics(new Font("SF Pro Text", Font.PLAIN, 13));
        FontMetrics fmMeta = getFontMetrics(META_FONT);

        int bubbleW = 0;
        int bubbleH = 0;

        // Imagen
        if (image != null) {
            int rawW = image.getIconWidth();
            int rawH = image.getIconHeight();
            double scaleW = (double)(MAX_WIDTH - 28) / rawW;
            double scaleH = (double)IMAGE_MAX_HEIGHT / rawH;
            double scale  = Math.min(Math.min(scaleW, scaleH), 1.0);
            bubbleW = (int)(rawW * scale);
            bubbleH = (int)(rawH * scale);
        }

        // Texto
        if (text != null && !text.isEmpty()) {
            int textW      = fm.stringWidth(text);
            int wrappedW   = Math.min(textW + 28, MAX_WIDTH);
            bubbleW        = Math.max(bubbleW, wrappedW);
            int lines      = (int) Math.ceil((double)(textW + 28) / MAX_WIDTH);
            bubbleH       += (fm.getHeight() * lines) + 20;
        } else {
            bubbleH += 20;
        }

        // Espacio extra para la franja de metadatos (hora + check)
        bubbleH += fmMeta.getHeight() + 6;

        return new Dimension(bubbleW + 30, bubbleH + 10);
    }

    // ── Pintura ──────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,  RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w  = getWidth();
        Dimension pref = getPreferredSize();
        int bw = Math.min(w - 10, pref.width - 10);
        int x  = isOwn ? w - bw - 5 : 5;
        int h  = getHeight() - 5;

        // Sombra
        g2.setColor(new Color(0, 0, 0, 20));
        g2.fillRoundRect(x + 1, 2, bw, h, ARC, ARC);

        // Fondo del bubble (estilo iOS)
        Color bubbleColor = isOwn ? new Color(0x007AFF) : new Color(0xE9E9EB);
        g2.setColor(bubbleColor);
        g2.fillRoundRect(x, 0, bw, h, ARC, ARC);

        int currentY = 10;

        // ── Imagen ───────────────────────────────────────────────────────────
        if (image != null) {
            int rawW = image.getIconWidth();
            int rawH = image.getIconHeight();
            double scaleW = (double)(bw - 20) / rawW;
            double scaleH = (double)IMAGE_MAX_HEIGHT / rawH;
            double scale  = Math.min(Math.min(scaleW, scaleH), 1.0);
            int imgW = (int)(rawW * scale);
            int imgH = (int)(rawH * scale);

            g2.setClip(new RoundRectangle2D.Float(x + 10, currentY, imgW, imgH, 10, 10));
            g2.drawImage(image.getImage(), x + 10, currentY, imgW, imgH, null);
            g2.setClip(null);
            currentY += imgH + 10;
        }

        // ── Texto ────────────────────────────────────────────────────────────
        Font textFont = new Font("SF Pro Text", Font.PLAIN, 13);
        g2.setFont(textFont);
        g2.setColor(isOwn ? Color.WHITE : Color.BLACK);
        FontMetrics fm = g2.getFontMetrics();

        if (text != null && !text.isEmpty()) {
            String[] words   = text.split(" ");
            StringBuilder line = new StringBuilder();
            int maxTextW     = bw - 24;

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
            currentY += fm.getHeight();
        }

        // ── Metadatos: hora + check (solo mensajes propios) ──────────────────
        g2.setFont(META_FONT);
        FontMetrics fmMeta = g2.getFontMetrics();

        // Color semitransparente sobre el fondo del bubble
        Color metaColor = isOwn ? new Color(255, 255, 255, 180) : new Color(0, 0, 0, 100);
        g2.setColor(metaColor);

        int metaY = h - 6; // baseline alineado al fondo del bubble

        // Hora
        if (!timestamp.isEmpty()) {
            int tsW  = fmMeta.stringWidth(timestamp);
            int tsX  = isOwn
                    ? x + bw - tsW - (isOwn ? 22 : 8)  // deja espacio al check propio
                    : x + bw - tsW - 8;
            g2.drawString(timestamp, tsX, metaY);
        }

        // Doble-check (solo mensajes propios)
        if (isOwn) {
            Color checkColor = isRead ? new Color(0x4FC3F7) : new Color(255, 255, 255, 160);
            drawDoubleCheck(g2, x + bw - 18, metaY, checkColor);
        }

        g2.dispose();
    }

    /**
     * Dibuja un doble-check estilo WhatsApp/Telegram.
     *
     * @param g2    contexto gráfico
     * @param rx    x derecha del área de checks
     * @param ry    y (baseline)
     * @param color color de los checks
     */
    private void drawDoubleCheck(Graphics2D g2, int rx, int ry, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Primer check (izquierdo)
        int x1 = rx - 14;
        drawCheck(g2, x1, ry);

        // Segundo check (derecho, ligeramente solapado)
        int x2 = rx - 8;
        drawCheck(g2, x2, ry);
    }

    /** Dibuja un único ✓ con vértice en (cx, cy-baseline). */
    private void drawCheck(Graphics2D g2, int cx, int cy) {
        // Palomita pequeña: trazo corto hacia abajo-derecha, luego largo hacia arriba-derecha
        int[] xPts = { cx,     cx + 3, cx + 7 };
        int[] yPts = { cy - 3, cy,     cy - 5 };
        g2.drawPolyline(xPts, yPts, 3);
    }
}