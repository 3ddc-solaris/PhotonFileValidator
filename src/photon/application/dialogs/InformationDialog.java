/*
 * MIT License
 *
 * Copyright (c) 2018 Bonosoft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package photon.application.dialogs;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import photon.file.PhotonFile;
import photon.file.parts.IFileHeader;
import photon.file.parts.photon.PhotonFileHeader;
import photon.file.parts.PhotonFilePrintParameters;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InformationDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel buildAreaX;
    private JLabel buildAreaY;
    private JLabel resolutionX;
    private JLabel resolutionY;
    private JLabel layerHeight;
    private JLabel pixels;
    private JLabel printVolume;
    private JLabel printTime;
    private JLabel fileTime;
    private JLabel fileVolumne;
    private JLabel fileWeight;
    private JLabel fileCost;
    private JLabel fileHeader;

    private float peel = 5.5f;

    public InformationDialog(Frame frame) {
        super(frame);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("Information");

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
    }

    public void setInformation(PhotonFile photonFile) {
        if (photonFile != null) {
            IFileHeader photonFileHeader = photonFile.getPhotonFileHeader();

            buildAreaX.setText(String.format("%8.2f mm", photonFileHeader.getBuildAreaX()));
            buildAreaY.setText(String.format("%8.2f mm", photonFileHeader.getBuildAreaY()));

            resolutionX.setText(String.format("%8d px", photonFileHeader.getResolutionX()));
            resolutionY.setText(String.format("%8d px", photonFileHeader.getResolutionY()));

            layerHeight.setText(String.format("%.2f mm", photonFileHeader.getLayerHeight()));
            pixels.setText(String.format("%,d px", photonFile.getPixels()));

            float xmmPx = photonFileHeader.getBuildAreaX() / photonFileHeader.getResolutionX();
            float ymmPx = photonFileHeader.getBuildAreaY() / photonFileHeader.getResolutionY();
            float mm3Px = xmmPx * ymmPx * photonFileHeader.getLayerHeight();
            float mm3 = mm3Px * photonFile.getPixels();
            float cm3 = mm3 / 1000;
            printVolume.setText(String.format("%,.1f ml", cm3));

            float peelTime = peel;
            float baseTime = photonFileHeader.getBottomLayers() * (photonFileHeader.getBottomExposureTimeSeconds() + Float.max(peelTime, photonFileHeader.getOffTimeSeconds()));
            float topTime = (photonFileHeader.getNumberOfLayers() - photonFileHeader.getBottomLayers()) * (photonFileHeader.getExposureTimeSeconds() + Float.max(peelTime, photonFileHeader.getOffTimeSeconds()));
            long totalSeconds = (long) (baseTime + topTime);
            printTime.setText(getDuration(totalSeconds));


            fileHeader.setText("File version " + photonFile.getVersion());
            if (photonFile.getVersion() > 1) {
                PhotonFilePrintParameters parameters = ((PhotonFileHeader) photonFile.getPhotonFileHeader()).photonFilePrintParameters;
                fileCost.setText(String.format("%8.4f $", parameters.costDollars));
                fileWeight.setText(String.format("%8.2f g", parameters.weightG));
                fileVolumne.setText(String.format("%,.1f ml", parameters.volumeMl));
                fileTime.setText(getDuration(photonFile.getPhotonFileHeader().getPrintTimeSeconds()));
            } else {
                fileCost.setText("");
                fileWeight.setText("");
                fileVolumne.setText("");
                fileTime.setText("");
            }

        }
    }

    private String getDuration(long sec) {
        if (sec < 60) {
            return sec + " s";
        } else {
            long min = sec / 60;
            sec = sec % 60;
            if (min < 60) {
                return min + " m, " + sec + " s";
            } else {
                long hour = min / 60;
                min = min % 60;
                return hour + " h, " + min + " m, " + sec + " s";
            }
        }
    }


    private void onOK() {
        dispose();
    }

    public void setPeel(float peel) {
        this.peel = peel;
    }

}
