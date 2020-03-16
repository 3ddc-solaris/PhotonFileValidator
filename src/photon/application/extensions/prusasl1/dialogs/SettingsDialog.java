package photon.application.extensions.prusasl1.dialogs;

import photon.application.extensions.prusasl1.configuration.PrusaSL1Configuration;
import photon.application.extensions.prusasl1.printhost.PrusaSL1Server;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.*;
import java.util.function.Consumer;

public class SettingsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField serverState;
    private JButton toggleServerStateBtn;
    private JSpinner serverPort;
    private JCheckBox doStartupServer;
    private JTextField outputDirectory;
    private JButton selectOutputDirectory;
    private JCheckBox autoremoveSLFiles;
    private JSpinner largePreviewMaxWidth;
    private JTextField backgroundColor;
    private JTextField ambientColor;
    private JTextField diffuseColor;
    private JSpinner largePreviewMaxHeight;
    private JSpinner smallPreviewMaxHeight;
    private JSpinner smallPreviewMaxWidth;
    private JTabbedPane photonFileSettings;
    private JPanel photonCommonSettings;
    private JPanel photonPrintParameters;
    private JSpinner bedSizeX;
    private JSpinner bedSizeY;
    private JSpinner bedSizeZ;
    private JSpinner resolutionX;
    private JSpinner resolutionY;
    private JSpinner exposureOffTime;
    private JSpinner bottomLiftDistance;
    private JSpinner bottomLiftSpeed;
    private JSpinner liftingDistance;
    private JSpinner liftingSpeed;
    private JSpinner retractSpeed;
    private JSpinner bottomLightOffDelay;
    private JSpinner lightOffDelay;
    private JCheckBox version2Format;
    private JButton showPrusaSlicerSettings;
    private JButton buttonApply;
    private JButton resetButton;
    private JCheckBox useProvidedPreviewImages;
    private JCheckBox bringWindowToFgOnImport;

    private final PrusaSL1Configuration configuration;
    private final PrusaSL1Server server;

    public SettingsDialog() {
        configuration = PrusaSL1Configuration.getInstance();
        server = PrusaSL1Server.getInstance();

        setTitle("Prusa SL1 File Import Settings");

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        mkIntegerSpinner(serverPort, 1024, 65535);

        mkIntegerSpinner(largePreviewMaxWidth, 0, 65535);
        mkIntegerSpinner(largePreviewMaxHeight, 0, 65535);
        mkIntegerSpinner(smallPreviewMaxWidth, 0, 65535);
        mkIntegerSpinner(smallPreviewMaxHeight, 0, 65535);

        mkFloatSpinner(bedSizeX, 0, 65535);
        mkFloatSpinner(bedSizeY, 0, 65535);
        mkFloatSpinner(bedSizeZ, 0, 65535);

        mkIntegerSpinner(resolutionX, 0, 65535);
        mkIntegerSpinner(resolutionY, 0, 65535);

        mkFloatSpinner(exposureOffTime, 0, 100);

        mkFloatSpinner(bottomLiftDistance, 0, 65535);
        mkFloatSpinner(bottomLiftSpeed, 0, 65535);
        mkFloatSpinner(liftingDistance, 0, 65535);
        mkFloatSpinner(liftingSpeed, 0, 65535);
        mkFloatSpinner(retractSpeed, 0, 65535);
        mkFloatSpinner(bottomLightOffDelay, 0, 65535);
        mkFloatSpinner(lightOffDelay, 0, 65535);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        buttonApply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onApply();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        toggleServerStateBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                serverAction(server -> {
                    if (server.isRunning())
                        server.stop();
                    else {
                        if( getInt(serverPort) !=  configuration.getPrusaSL1ServerPort() )
                            configuration.setPrusaSL1ServerPort(getInt(serverPort));

                        server.start();
                    }
                });
            }
        });

        selectOutputDirectory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                final JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new java.io.File(outputDirectory.getText()) );
                chooser.setDialogTitle("Select output directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);

                if (chooser.showOpenDialog(SettingsDialog.this) == JFileChooser.APPROVE_OPTION) {
                    outputDirectory.setText( chooser.getSelectedFile().getAbsolutePath() );
                }
            }
        });

        version2Format.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                onVersionChange();
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onReset();
            }
        });

        showPrusaSlicerSettings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                final PrusaSlicerSettingsDialog dialog = new PrusaSlicerSettingsDialog();

                dialog.setSize(dialog.getPreferredSize());
                dialog.setLocationRelativeTo(SettingsDialog.this);
                dialog.setVisible(true);
            }
        });
    }

    public void setInformation() {
        // ------ Prusa SL1 Server Settings ------
        serverPort.setValue(configuration.getPrusaSL1ServerPort());
        serverState.setText(server.getMessage());
        toggleServerStateBtn.setText(server.isRunning() ? "Stop" : "Start");
        doStartupServer.setSelected(configuration.isPrusaSL1ServerEnabled());
        outputDirectory.setText(configuration.getFileOutputDirectory());
        autoremoveSLFiles.setSelected(configuration.isRemoveSl1FileAfterImport());

        // ------ UI Options ------
        bringWindowToFgOnImport.setSelected( configuration.isBringWindowToFgOnImport() );

        //  ------ Preview image generation ------
        useProvidedPreviewImages.setSelected( configuration.isUseProvidedPreviewImages() );
        largePreviewMaxWidth.setValue( configuration.getLargePreviewMaxWidth() );
        largePreviewMaxHeight.setValue( configuration.getLargePreviewMaxHeight() );

        smallPreviewMaxWidth.setValue( configuration.getSmallPreviewMaxWidth() );
        smallPreviewMaxHeight.setValue( configuration.getSmallPreviewMaxHeight() );

        backgroundColor.setText( configuration.getBackgroundColor() );
        ambientColor.setText( configuration.getAmbientColor() );
        diffuseColor.setText( configuration.getDiffuseColor() );

        // ------ Photon File generation Settings ------

        version2Format.setSelected( 2 == configuration.getPhotonFileVersion() );
        onVersionChange();

        // Common Tab
        bedSizeX.setValue( configuration.getBedSizeX() );
        bedSizeY.setValue( configuration.getBedSizeY() );
        bedSizeZ.setValue( configuration.getBedSizeZ() );

        resolutionX.setValue( configuration.getResolutionX() );
        resolutionY.setValue( configuration.getResolutionY() );

        exposureOffTime.setValue( configuration.getExposureOffTime() );

        // Print Parameters Tab
        bottomLiftDistance.setValue( configuration.getBottomLiftDistance() );
        bottomLiftSpeed.setValue( configuration.getBottomLiftSpeed() );
        liftingDistance.setValue( configuration.getLiftingDistance() );
        liftingSpeed.setValue( configuration.getLiftingSpeed() );
        retractSpeed.setValue( configuration.getRetractSpeed() );
        bottomLightOffDelay.setValue( configuration.getBottomLightOffDelay() );
        lightOffDelay.setValue( configuration.getLightOffDelay() );
    }

    public boolean applyChanges() {
        try {
            //  ------ Prusa SL1 Server Settings ------
            if( getInt(serverPort) !=  configuration.getPrusaSL1ServerPort() ) {
                configuration.setPrusaSL1ServerPort(getInt(serverPort));
                serverAction(PrusaSL1Server::restart);
            }
            configuration.setPrusaSL1ServerEnabled(doStartupServer.isSelected());
            configuration.setFileOutputDirectory(outputDirectory.getText());
            configuration.setRemoveSl1FileAfterImport(autoremoveSLFiles.isSelected());

            // ------ UI Options ------
            configuration.setBringWindowToFgOnImport( bringWindowToFgOnImport.isSelected() );

            //  ------ Preview image generation ------
            configuration.setUseProvidedPreviewImages( useProvidedPreviewImages.isSelected() );
            configuration.setLargePreviewMaxWidth( getInt(largePreviewMaxWidth) );
            configuration.setLargePreviewMaxHeight( getInt(largePreviewMaxHeight) );

            configuration.setSmallPreviewMaxWidth( getInt(smallPreviewMaxWidth) );
            configuration.setSmallPreviewMaxHeight( getInt(smallPreviewMaxHeight) );

            configuration.setBackgroundColor( backgroundColor.getText() );
            configuration.setAmbientColor( ambientColor.getText() );
            configuration.setDiffuseColor( diffuseColor.getText() );

            //  ------ Photon File generation Settings ------

            configuration.setPhotonFileVersion( version2Format.isSelected() ? 2 : 1 );

            // Common Tab
            configuration.setBedSizeX( getFloat(bedSizeX) );
            configuration.setBedSizeY( getFloat(bedSizeY) );
            configuration.setBedSizeZ( getFloat(bedSizeZ) );

            configuration.setResolutionX( getInt(resolutionX) );
            configuration.setResolutionY( getInt(resolutionY) );

            configuration.setExposureOffTime( getFloat(exposureOffTime) );

            // Print Parameters Tab
            configuration.setBottomLiftDistance( getFloat(bottomLiftDistance) );
            configuration.setBottomLiftSpeed( getFloat(bottomLiftSpeed) );
            configuration.setLiftingDistance( getFloat(liftingDistance) );
            configuration.setLiftingSpeed( getFloat(liftingSpeed) );
            configuration.setRetractSpeed( getFloat(retractSpeed) );
            configuration.setBottomLightOffDelay( getFloat(bottomLightOffDelay) );
            configuration.setLightOffDelay( getFloat(lightOffDelay) );

            configuration.save();
            return true;
        }
        catch(final Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        return false;
    }

    private void onApply() {
        if(applyChanges())
            setInformation();
    }

    private void onOK() {
        if(applyChanges())
            dispose();
    }

    private void onCancel() {
        dispose();
    }

    private void onReset() {
        final int port = getInt(serverPort);
        try {
            configuration.resetToDefault();
        }
        catch (final Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
        if( port !=  configuration.getPrusaSL1ServerPort() ) {
            serverAction(PrusaSL1Server::restart);
        }
        setInformation();
    }

    private int getInt(final JSpinner spinner) {
        try {
            spinner.commitEdit();
        } catch ( java.text.ParseException e ) {}

        return ((Number) spinner.getValue()).intValue();
    }

    private float getFloat(final JSpinner spinner) {
        try {
            spinner.commitEdit();
        } catch ( java.text.ParseException e ) {}

        return ((Number) spinner.getValue()).floatValue();
    }

    private void mkIntegerSpinner(final JSpinner spinner, int min, int max) {
        spinner.setModel(new SpinnerNumberModel(min, min, max, 1));
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "0"));
    }

    private void mkFloatSpinner(final JSpinner spinner, float min, float max) {
        spinner.setModel(new SpinnerNumberModel(min, min, max, 0.1));
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.00"));
    }

    private void serverAction(final Consumer<PrusaSL1Server> action) {
        toggleServerStateBtn.setEnabled(false);
        try {
            action.accept(server);
        }
        finally {
            serverState.setText(server.getMessage());
            toggleServerStateBtn.setText(server.isRunning() ? "Stop" : "Start");
            toggleServerStateBtn.setEnabled(true);
        }
    }

    private void onVersionChange() {
        if(version2Format.isSelected()) {
            photonFileSettings.setEnabledAt(1, true);
        }
        else {
            photonFileSettings.setEnabledAt(1, false);
            photonFileSettings.setSelectedIndex(0);
        }
    }
}
