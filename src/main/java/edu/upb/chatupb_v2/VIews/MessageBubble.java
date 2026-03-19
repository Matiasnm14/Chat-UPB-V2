package edu.upb.chatupb_v2.VIews;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MessageBubble extends JPanel {

    private final String    text;
    private final boolean   isOwn;
    private final ImageIcon image;
    private final String    timestamp;
    private final boolean   isRead;

    // ── Constantes de diseño ─────────────────────────────────────────────────
    private static final int  ARC              = 16;
    private static final int  MAX_WIDTH        = 420;
    private static final int  IMAGE_MAX_HEIGHT = 250;
    private static final int  PAD_H            = 12;   // padding horizontal interior
    private static final int  PAD_V            = 10;   // padding vertical interior
    private static final int  META_MARGIN_R    = 8;    // margen derecho de la franja meta
    private static final int  DOUBLE_CHECK_W   = 20;   // ancho total de los dos checks
    private static final int  META_GAP         = 6;    // espacio entre hora y checks

    private static final Font             FONT_TEXT = new Font("SF Pro Text", Font.PLAIN, 13);
    private static final Font             FONT_META = new Font("SF Pro Text", Font.PLAIN, 10);
    private static final DateTimeFormatter TIME_FMT  = DateTimeFormatter.ofPattern("HH:mm");

    // =========================================================================
    // CONSTRUCTORES
    // =========================================================================

    /** Constructor canónico. */
    MessageBubble(String text, ImageIcon image, boolean isOwn,
                  LocalTime time, boolean isRead) {
        this.text      = text;
        this.image     = image;
        this.isOwn     = isOwn;
        this.timestamp = (time != null) ? time.format(TIME_FMT) : "";
        this.isRead    = isRead;
        setOpaque(false);
    }

    /** Solo texto, con indicador de lectura. */
    MessageBubble(String text, boolean isOwn, boolean isRead) {
        this(text, null, isOwn, LocalTime.now(), isRead);
    }

    /** Texto + imagen, hora actual. */
    MessageBubble(String text, ImageIcon image, boolean isOwn, boolean isRead) {
        this(text, image, isOwn, LocalTime.now(), isRead);
    }

    // =========================================================================
    // TAMAÑO PREFERIDO
    // =========================================================================

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm     = getFontMetrics(FONT_TEXT);
        FontMetrics fmMeta = getFontMetrics(FONT_META);

        int bubbleW = 0;
        int bubbleH = PAD_V;

        // Imagen
        if (image != null) {
            int[] scaled = scaledImageSize(image, MAX_WIDTH - PAD_H * 2);
            bubbleW  = Math.max(bubbleW, scaled[0]);
            bubbleH += scaled[1] + PAD_V;
        }

        // Texto
        if (text != null && !text.isEmpty()) {
            int textW    = fm.stringWidth(text);
            int wrappedW = Math.min(textW + PAD_H * 2, MAX_WIDTH);
            bubbleW      = Math.max(bubbleW, wrappedW);
            int lines    = (int) Math.ceil((double)(textW + PAD_H * 2) / MAX_WIDTH);
            bubbleH     += fm.getHeight() * lines + PAD_V;
        } else {
            bubbleH += PAD_V;
        }

        // Franja de metadatos: hora + (checks si es propio)
        // Anchura mínima del bubble para que quepan hora y checks en la misma línea
        int metaW = metaRowWidth(fmMeta);
        bubbleW   = Math.max(bubbleW, metaW + PAD_H * 2);
        bubbleH  += fmMeta.getHeight() + PAD_V;

        return new Dimension(bubbleW + 30, bubbleH + 10);
    }

    // =========================================================================
    // PINTURA
    // =========================================================================

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int w    = getWidth();
        int h    = getHeight() - 5;
        int pref = getPreferredSize().width - 10;
        int bw   = Math.min(w - 10, pref);
        int x    = isOwn ? w - bw - 5 : 5;

        // Sombra
        g2.setColor(new Color(0, 0, 0, 20));
        g2.fillRoundRect(x + 1, 2, bw, h, ARC, ARC);

        // Fondo
        g2.setColor(isOwn ? new Color(0x016D3B) : new Color(0xE9E9EB));
        g2.fillRoundRect(x, 0, bw, h, ARC, ARC);

        int currentY = PAD_V;

        // ── Imagen ───────────────────────────────────────────────────────────
        if (image != null) {
            int[] sz   = scaledImageSize(image, bw - PAD_H * 2);
            int   imgW = sz[0], imgH = sz[1];
            g2.setClip(new RoundRectangle2D.Float(x + PAD_H, currentY, imgW, imgH, 10, 10));
            g2.drawImage(image.getImage(), x + PAD_H, currentY, imgW, imgH, null);
            g2.setClip(null);
            currentY += imgH + PAD_V;
        }

        // ── Texto ────────────────────────────────────────────────────────────
        if (text != null && !text.isEmpty()) {
            g2.setFont(FONT_TEXT);
            g2.setColor(isOwn ? Color.WHITE : Color.BLACK);
            FontMetrics fm   = g2.getFontMetrics();
            int         maxW = bw - PAD_H * 2;

            String[] words = text.split(" ");
            StringBuilder line = new StringBuilder();

            for (String word : words) {
                String test = line.isEmpty() ? word : line + " " + word;
                if (fm.stringWidth(test) > maxW) {
                    g2.drawString(line.toString(), x + PAD_H, currentY + fm.getAscent());
                    currentY += fm.getHeight();
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(test);
                }
            }
            g2.drawString(line.toString(), x + PAD_H, currentY + fm.getAscent());
            currentY += fm.getHeight() + 4;
        }

        // ── Franja de metadatos (hora + doble-check) ─────────────────────────
        // Se dibuja en una sola línea alineada a la derecha del bubble.
        // Layout (de derecha a izquierda):
        //   [META_MARGIN_R] [checks: DOUBLE_CHECK_W] [META_GAP] [timestamp] …
        g2.setFont(FONT_META);
        FontMetrics fmMeta   = g2.getFontMetrics();
        Color       metaColor = isOwn
                ? new Color(255, 255, 255, 180)
                : new Color(0, 0, 0, 100);
        g2.setColor(metaColor);

        int metaBaseline = h - META_MARGIN_R;  // baseline de los metadatos

        if (isOwn) {
            // Posición de los checks: pegados al borde derecho del bubble
            int checksX = x + bw - META_MARGIN_R - DOUBLE_CHECK_W;
            Color checkColor = isRead
                    ? new Color(0x4FC3F7)
                    : new Color(255, 255, 255, 160);
            drawDoubleCheck(g2, checksX, metaBaseline, checkColor);

            // Hora: a la izquierda de los checks, separada por META_GAP
            if (!timestamp.isEmpty()) {
                int tsW = fmMeta.stringWidth(timestamp);
                int tsX = checksX - META_GAP - tsW;
                g2.setColor(metaColor);
                g2.drawString(timestamp, tsX, metaBaseline);
            }
        } else {
            // Sin checks: solo la hora pegada al borde derecho
            if (!timestamp.isEmpty()) {
                int tsW = fmMeta.stringWidth(timestamp);
                int tsX = x + bw - META_MARGIN_R - tsW;
                g2.drawString(timestamp, tsX, metaBaseline);
            }
        }

        g2.dispose();
    }

    // =========================================================================
    // HELPERS PRIVADOS
    // =========================================================================

    /**
     * Calcula el ancho total de la franja de metadatos para el tamaño preferido.
     * Hora + gap + checks (si es propio) + margen derecho.
     */
    private int metaRowWidth(FontMetrics fmMeta) {
        int tsW = timestamp.isEmpty() ? 0 : fmMeta.stringWidth(timestamp);
        if (isOwn) return tsW + META_GAP + DOUBLE_CHECK_W + META_MARGIN_R;
        return tsW + META_MARGIN_R;
    }

    /**
     * Devuelve {ancho, alto} escalados para que la imagen quepa dentro de
     * {@code maxW} y {@link #IMAGE_MAX_HEIGHT} sin ampliar.
     */
    private static int[] scaledImageSize(ImageIcon img, int maxW) {
        double scaleW = (double) maxW          / img.getIconWidth();
        double scaleH = (double) IMAGE_MAX_HEIGHT / img.getIconHeight();
        double scale  = Math.min(Math.min(scaleW, scaleH), 1.0);
        return new int[]{ (int)(img.getIconWidth() * scale),
                (int)(img.getIconHeight() * scale) };
    }

    /**
     * Dibuja un doble-check estilo mensajería a partir de la esquina
     * superior-izquierda {@code (rx, ry-baseline)}.
     */
    private void drawDoubleCheck(Graphics2D g2, int rx, int ry, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawCheck(g2, rx,     ry);   // check izquierdo
        drawCheck(g2, rx + 6, ry);   // check derecho, separado 6px
    }

    /** Dibuja un único ✓ con vértice central en (cx, cy). */
    private void drawCheck(Graphics2D g2, int cx, int cy) {
        int[] xp = { cx,     cx + 3, cx + 7 };
        int[] yp = { cy - 3, cy,     cy - 5 };
        g2.drawPolyline(xp, yp, 3);
    }
}