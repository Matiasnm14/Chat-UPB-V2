package edu.upb.chatupb_v2.VIews;

import edu.upb.chatupb_v2.Controller.CobroController;
import edu.upb.chatupb_v2.Controller.UIController;
import edu.upb.chatupb_v2.Model.entities.Cobro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

public class DialogUtil {

    void showConnectDialog(Component component, UIController uiController) {
        String ip = JOptionPane.showInputDialog(
                component, "Dirección IP del servidor:", "Nueva Conexión", JOptionPane.QUESTION_MESSAGE);
        if (ip != null && !ip.trim().isEmpty()) uiController.connect(ip.trim());
    }

    void showPaymentDialog(Component component, CobroController cobroController) {
        String[] options = {"🔐  Cripto", "🏦  Bob (Fiat)"};
        int choice = JOptionPane.showOptionDialog(
                component, "¿Tipo de pago?", "Nuevo Pago",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (choice < 0) return;

        Cobro res   = cobroController.cobrarController(choice);
        String tipo = res.getRed().isEmpty() ? "FIAT" : "CRIPTO";
        String red  = res.getRed().isEmpty() ? "" : "\nRed: " + res.getRed();
        JOptionPane.showMessageDialog(component,
                "Tipo: " + tipo + "\nImporte: " + res.getImporte() + "\nQR: " + res.getQr() + red,
                "Pago", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * @param onThemeSelected  callback que recibe el tema elegido — JUi lo usa
     *                         para llamar applyTheme() y UIController.sendTheme()
     */
    void showThemeDialog(Component parent,
                         ThemesUtil.ChatTheme activeTheme,
                         Font fontTitle,
                         Font fontBody,
                         Consumer<ThemesUtil.ChatTheme> onThemeSelected) {

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent),
                "Elegir Tema", true);
        dlg.setSize(320, 300);
        dlg.setLocationRelativeTo(parent);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(activeTheme.bgSidebar());

        JLabel title = new JLabel("  Tema de conversación");
        title.setFont(fontTitle);
        title.setForeground(activeTheme.textPrimary());
        title.setBorder(new EmptyBorder(16, 16, 10, 16));
        dlg.add(title, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(activeTheme.bgSidebar());
        list.setBorder(new EmptyBorder(0, 12, 12, 12));

        ButtonGroup group = new ButtonGroup();
        for (ThemesUtil.ChatTheme t : ThemesUtil.THEMES) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            row.setBorder(new EmptyBorder(4, 4, 4, 4));

            JPanel swatch = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(t.accent());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                }
            };
            swatch.setOpaque(false);
            swatch.setPreferredSize(new Dimension(28, 28));

            JRadioButton rb = new JRadioButton(t.label());
            rb.setFont(fontBody);
            rb.setForeground(activeTheme.textPrimary());
            rb.setOpaque(false);
            rb.setSelected(t.id().equals(activeTheme.id()));
            group.add(rb);
            rb.addActionListener(e -> {
                onThemeSelected.accept(t); // ← JUi decide qué hacer
                dlg.dispose();
            });

            row.add(swatch, BorderLayout.WEST);
            row.add(rb,     BorderLayout.CENTER);
            list.add(row);
        }

        dlg.add(list, BorderLayout.CENTER);
        dlg.setVisible(true);
    }
}