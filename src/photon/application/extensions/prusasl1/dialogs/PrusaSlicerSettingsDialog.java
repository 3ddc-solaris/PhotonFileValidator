package photon.application.extensions.prusasl1.dialogs;

import photon.application.extensions.prusasl1.configuration.PrusaSL1Configuration;
import photon.application.extensions.prusasl1.printhost.PrusaSL1Server;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class PrusaSlicerSettingsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextPane text;

    public PrusaSlicerSettingsDialog() {
        setTitle("PrusaSlicer Settings");

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        final String url = "http://localhost:" + PrusaSL1Configuration.getInstance().getPrusaSL1ServerPort();
        final String warning = PrusaSL1Server.getInstance().isRunning() ? "" : "ATTENTION: Print Host is currently not active. Sending to PhotonFileValidator will therefore not be possible.";

        text.setText(
                getTemplate("resources/prusaSettings.html")
                .replaceAll("\\{URL\\}", url)
                .replaceAll("\\{WARNING\\}", warning)
        );
    }

    private String getTemplate(final String filename) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try(final InputStream in = getClass().getResourceAsStream("/" + getClass().getPackageName().replaceAll("\\.", "/") + "/" + filename)) {
            final byte[] buf = new byte[1024 * 64];
            int read = 0;
            while( (read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
        }
        catch(final Exception e) {
            e.printStackTrace(new PrintStream(out));
        }

        return new String(out.toByteArray());
    }
}
