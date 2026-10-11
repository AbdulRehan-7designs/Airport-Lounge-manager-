package com.alm.ui.components;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class QRScannerDialog extends JDialog {

    private Webcam webcam;
    private WebcamPanel panel;
    private Timer timer;
    private MultiFormatReader barcodeReader;
    private Consumer<String> onScanComplete;

    public QRScannerDialog(Frame owner, Consumer<String> onScanComplete) {
        super(owner, "Live Action QR Scanner \ud83d\udcf7", true);
        this.onScanComplete = onScanComplete;
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        barcodeReader = new MultiFormatReader();

        webcam = Webcam.getDefault();
        if (webcam != null) {
            Dimension[] sizes = webcam.getViewSizes();
            if (sizes != null && sizes.length > 0) {
                webcam.setViewSize(sizes[sizes.length - 1]); // Use highest resolution available for better scanning
            }
            panel = new WebcamPanel(webcam);
            panel.setMirrored(true);
            panel.setFPSDisplayed(true);
            panel.setImageSizeDisplayed(true);

            JLabel instr = new JLabel("Hold Passenger QR slightly away from the camera", SwingConstants.CENTER);
            instr.setFont(new Font("Inter", Font.BOLD, 14));
            instr.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            add(instr, BorderLayout.NORTH);
            add(panel, BorderLayout.CENTER);

            // Timer to capture frames and decode at 10 FPS
            timer = new Timer(100, e -> captureAndDecode());
            timer.start();
        } else {
            JLabel err = new JLabel("Error: No webcam connected or permissions denied.", SwingConstants.CENTER);
            err.setForeground(Color.RED);
            add(err, BorderLayout.CENTER);
        }

        setSize(660, 520);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void captureAndDecode() {
        if (!webcam.isOpen())
            return;

        BufferedImage image = webcam.getImage();
        if (image == null)
            return;

        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Result result = barcodeReader.decodeWithState(bitmap);
            if (result != null) {
                // Success! Found a QR code!
                timer.stop();
                webcam.close();
                String qrText = result.getText();
                dispose();
                if (onScanComplete != null) {
                    onScanComplete.accept(qrText);
                }
            }
        } catch (NotFoundException e) {
            // No QR detected in this frame, ignore and continue
        } finally {
            barcodeReader.reset();
        }
    }

    @Override
    public void dispose() {
        if (timer != null)
            timer.stop();
        if (webcam != null && webcam.isOpen())
            webcam.close();
        super.dispose();
    }
}
