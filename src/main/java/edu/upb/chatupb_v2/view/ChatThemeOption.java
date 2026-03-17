package edu.upb.chatupb_v2.view;

import java.awt.Color;

public enum ChatThemeOption {
    DEFAULT("default", "Default",
            new Color(0xF0F2F5),
            new Color(0xFFFFFF),
            new Color(0xE9EDF1),
            new Color(0x25D366),
            new Color(0x111B21),
            new Color(0x667781),
            new Color(0xE1E7EC),
            new Color(0xDCF8C6),
            new Color(0xFFFFFF),
            new Color(0x667781),
            new Color(0x25D366),
            new Color(0xFFF3CD)),
    OCEANO("oceano", "Oceano",
            new Color(0xE7F3F8),
            new Color(0xF9FCFD),
            new Color(0xDDEEF5),
            new Color(0x0E7490),
            new Color(0x102A43),
            new Color(0x486581),
            new Color(0xBFD7E2),
            new Color(0xBEE3F8),
            new Color(0xFFFFFF),
            new Color(0x486581),
            new Color(0x0EA5A4),
            new Color(0xFFE3B3)),
    ARENA("arena", "Arena",
            new Color(0xF7F1E5),
            new Color(0xFFFDF8),
            new Color(0xF2E7D5),
            new Color(0xC67C2E),
            new Color(0x4B3621),
            new Color(0x7B5E3B),
            new Color(0xDEC7A1),
            new Color(0xF6D7A7),
            new Color(0xFFFDF8),
            new Color(0x7B5E3B),
            new Color(0x8BAA36),
            new Color(0xFCE6A8)),
    BOSQUE("bosque", "Bosque",
            new Color(0xEEF4EC),
            new Color(0xFBFDF9),
            new Color(0xE0EBDD),
            new Color(0x2F6B3B),
            new Color(0x1F3524),
            new Color(0x5D735F),
            new Color(0xBFD1C1),
            new Color(0xCFE7C9),
            new Color(0xFFFFFF),
            new Color(0x5D735F),
            new Color(0x4F8A3F),
            new Color(0xE8DFA9)),
    CORAL("coral", "Coral",
            new Color(0xFFF3EE),
            new Color(0xFFFCFA),
            new Color(0xFFE3DA),
            new Color(0xE76F51),
            new Color(0x5A2A22),
            new Color(0x8C5A52),
            new Color(0xF0C2B5),
            new Color(0xFFD1C4),
            new Color(0xFFFFFF),
            new Color(0x8C5A52),
            new Color(0xD96C4D),
            new Color(0xFFE6A7));

    private final String id;
    private final String displayName;
    private final Color bgApp;
    private final Color panel;
    private final Color chatBg;
    private final Color accent;
    private final Color textPrimary;
    private final Color textMuted;
    private final Color border;
    private final Color bubbleOut;
    private final Color bubbleIn;
    private final Color timeText;
    private final Color presenceOnline;
    private final Color uniqueBubble;

    ChatThemeOption(
            String id,
            String displayName,
            Color bgApp,
            Color panel,
            Color chatBg,
            Color accent,
            Color textPrimary,
            Color textMuted,
            Color border,
            Color bubbleOut,
            Color bubbleIn,
            Color timeText,
            Color presenceOnline,
            Color uniqueBubble
    ) {
        this.id = id;
        this.displayName = displayName;
        this.bgApp = bgApp;
        this.panel = panel;
        this.chatBg = chatBg;
        this.accent = accent;
        this.textPrimary = textPrimary;
        this.textMuted = textMuted;
        this.border = border;
        this.bubbleOut = bubbleOut;
        this.bubbleIn = bubbleIn;
        this.timeText = timeText;
        this.presenceOnline = presenceOnline;
        this.uniqueBubble = uniqueBubble;
    }

    public static ChatThemeOption fromId(String id) {
        if (id != null) {
            for (ChatThemeOption option : values()) {
                if (option.id.equalsIgnoreCase(id.trim())) {
                    return option;
                }
            }
        }
        return DEFAULT;
    }

    public static ChatThemeOption fromDisplayName(String displayName) {
        if (displayName != null) {
            for (ChatThemeOption option : values()) {
                if (option.displayName.equalsIgnoreCase(displayName.trim())) {
                    return option;
                }
            }
        }
        return DEFAULT;
    }

    public static String[] displayNames() {
        ChatThemeOption[] options = values();
        String[] names = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            names[i] = options[i].displayName;
        }
        return names;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getBgApp() {
        return bgApp;
    }

    public Color getPanel() {
        return panel;
    }

    public Color getChatBg() {
        return chatBg;
    }

    public Color getAccent() {
        return accent;
    }

    public Color getTextPrimary() {
        return textPrimary;
    }

    public Color getTextMuted() {
        return textMuted;
    }

    public Color getBorder() {
        return border;
    }

    public Color getBubbleOut() {
        return bubbleOut;
    }

    public Color getBubbleIn() {
        return bubbleIn;
    }

    public Color getTimeText() {
        return timeText;
    }

    public Color getPresenceOnline() {
        return presenceOnline;
    }

    public Color getUniqueBubble() {
        return uniqueBubble;
    }
}
