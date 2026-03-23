package edu.upb.chatupb_v2.VIews;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class ThemesUtil {

    // =========================================================================
    // DEFINICIÓN DE TEMAS
    // =========================================================================

    public record ChatTheme(
            String id, String label,
            Color bgChat, Color bgSidebar, Color bgInput,
            Color accent, Color accentHover,
            Color bubbleOwn, Color bubbleThem,
            Color textPrimary, Color textSecondary, Color borderColor
    ) {}

    public static final List<ChatTheme> THEMES = List.of(
            new ChatTheme("1", "🔵 Default",
                    new Color(0xF7F8FA), new Color(0xFFFFFF), new Color(0xFFFFFF),
                    new Color(0x5B5BD6), new Color(0x4848C0),
                    new Color(0x5B5BD6), new Color(0xE8E9F0),
                    new Color(0x111118), new Color(0x666680), new Color(0xDDDDE8)),
            new ChatTheme("2", "🌿 Forest",
                    new Color(0xEDF4EE), new Color(0xF4FAF5), new Color(0xFFFFFF),
                    new Color(0x2D6A4F), new Color(0x1B4332),
                    new Color(0x2D6A4F), new Color(0xD8F3DC),
                    new Color(0x081C15), new Color(0x52796F), new Color(0xB7E4C7)),
            new ChatTheme("3", "🌅 Sunset",
                    new Color(0xFFF3E0), new Color(0xFFFBF5), new Color(0xFFFFFF),
                    new Color(0xE65100), new Color(0xBF360C),
                    new Color(0xE65100), new Color(0xFFE0B2),
                    new Color(0x1A0A00), new Color(0x8D4E00), new Color(0xFFCC80)),
            new ChatTheme("4", "🌙 Midnight",
                    new Color(0x1A1B2E), new Color(0x16213E), new Color(0x0F3460),
                    new Color(0xE94560), new Color(0xC73652),
                    new Color(0xE94560), new Color(0x0F3460),
                    new Color(0xEAEAEA), new Color(0x8892A4), new Color(0x2A2D4A)),
            new ChatTheme("5", "🌸 Rose",
                    new Color(0xFFF0F3), new Color(0xFFF8FA), new Color(0xFFFFFF),
                    new Color(0xC9184A), new Color(0xA4133C),
                    new Color(0xC9184A), new Color(0xFFD6E0),
                    new Color(0x1A0010), new Color(0x8B5260), new Color(0xFFB3C6))
    );

    public static final ChatTheme DEFAULT_THEME = THEMES.getFirst();

    // =========================================================================
    // BÚSQUEDA
    // =========================================================================

    public static Optional<ChatTheme> findById(String id) {
        return THEMES.stream().filter(t -> t.id().equals(id)).findFirst();
    }

    // =========================================================================
    // APLICAR TEMA
    // =========================================================================

    /**
     * Aplica el tema a los componentes principales del chat.
     */
    public static void applyTheme(ChatTheme theme, ChatTheme previous,
                                  Component root, JPanel messagesPanel,
                                  javax.swing.JScrollPane scrollPane) {
        messagesPanel.setBackground(theme.bgChat());
        scrollPane.setBackground(theme.bgChat());
        scrollPane.getViewport().setBackground(theme.bgChat());
        root.setBackground(theme.bgChat());
        applyBgRecursive((Container) root, theme, previous);
        messagesPanel.repaint();
        root.repaint();
    }

    /**
     * Recorre recursivamente los paneles y reemplaza los colores del tema anterior
     * por los del nuevo.
     */
    public static void applyBgRecursive(Container container, ChatTheme next, ChatTheme prev) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel p && p.isOpaque()) {
                Color bg = p.getBackground();
                if (bg.equals(prev.bgChat()))
                    p.setBackground(next.bgChat());
                else if (bg.equals(prev.bgSidebar()))
                    p.setBackground(next.bgSidebar());
            }
            if (comp instanceof Container sub) applyBgRecursive(sub, next, prev);
        }
    }
}