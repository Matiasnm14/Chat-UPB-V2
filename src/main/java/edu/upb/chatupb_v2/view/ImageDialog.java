package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.controller.Controller;
import edu.upb.chatupb_v2.model.entities.commands.Chat;
import edu.upb.chatupb_v2.model.entities.commands.ImageMesagge;

import javax.swing.*;
//import javax.swing.border.DashedBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class ImageDialog extends JDialog {

    private JLabel dropLabel;
    private String destinationId;
    private Controller controller;

    public ImageDialog(Frame parent, String destinationId, Controller controller) {
        super(parent, "Enviar Imagen", true);
        this.destinationId = destinationId;
        this.controller = controller;

        setSize(300, 250);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Panel de arrastre con borde punteado
        JPanel dropPanel = new JPanel(new BorderLayout());
        dropPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createDashedBorder(Color.GRAY, 3, 3, 2, true)
        ));
        dropPanel.setBackground(new Color(245, 245, 250));

        dropLabel = new JLabel("Arrastra tu imagen aquí", SwingConstants.CENTER);
        dropLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dropLabel.setForeground(Color.DARK_GRAY);
        dropPanel.add(dropLabel, BorderLayout.CENTER);

        // Añadir el listener de Drag & Drop
        enableDragAndDrop(dropPanel);

        add(dropPanel, BorderLayout.CENTER);

        // Botón para cancelar
        JButton btnCancel = new JButton("Cancelar");
        btnCancel.addActionListener(e -> dispose());
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(btnCancel);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void enableDragAndDrop(JPanel panel) {
        new DropTarget(panel, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    // Aceptar el drop
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable t = dtde.getTransferable();

                    // Verificar si lo arrastrado son archivos
                    if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        List<File> fileList = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                        if (!fileList.isEmpty()) {
                            File file = fileList.get(0);

                            // Validar que sea una imagen (por extensión)
                            String name = file.getName().toLowerCase();
                            if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif")) {
                                processImageAndSend(file);
                            } else {
                                JOptionPane.showMessageDialog(ImageDialog.this,
                                        "Por favor arrastra un archivo de imagen (PNG, JPG, GIF).",
                                        "Formato inválido",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ImageDialog.this, "Error al procesar la imagen.");
                }
            }
        });
    }

    private void processImageAndSend(File file) {
        try {
            // Cambiar el texto mientras procesa
            dropLabel.setText("Procesando...");

            // 1. Leer el archivo a bytes
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fis.read(bytes);
            fis.close();

            // 2. Convertir a Base64
            // NOTA: Para no enviar solo el base64 a lo loco, le ponemos un prefijo
            // para que luego tu MessageRender sepa que es una imagen y no texto normal.
            String base64String = Base64.getEncoder().encodeToString(bytes);
            String messagePayload = base64String;

            // 3. Enviar a través del controller
            String miId = ((JUi) getParent()).getUserId();

            ImageMesagge imgCmd = new ImageMesagge(miId, UUID.randomUUID().toString(), base64String);

            // Reutilizamos tu método sendMessage actual.
            controller.sendMessage(imgCmd, destinationId);

            // 4. Cerrar el diálogo y avisar
            JOptionPane.showMessageDialog(this, "Imagen enviada con éxito.");
            dispose();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al codificar la imagen: " + e.getMessage());
        }
    }
}
