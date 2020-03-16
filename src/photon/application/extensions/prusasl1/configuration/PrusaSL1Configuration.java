package photon.application.extensions.prusasl1.configuration;

import photon.application.extensions.prusasl1.configuration.utilites.ConfigurationFile;
import photon.application.extensions.prusasl1.configuration.utilites.Persistent;

import java.awt.*;
import java.io.File;
import java.net.URISyntaxException;

@Persistent("prusaSL1Import")
public final class PrusaSL1Configuration extends ConfigurationFile {
    private static final String CONFIG_FILE_NAME = "PhotonFileValidatorConfig.xml";

    private static final int DEFAULT_BACKGROUND_COLOR = 0xFFFFFF;
    private static final int DEFAULT_AMBIENT_COLOR = 0x3083FF;
    private static final int DEFAULT_DIFFUSE_COLOR = 0x0074CC;

    // Prusa SL1 Server Settings
    @Persistent
    private int prusaSL1ServerPort = 8080;

    @Persistent
    private boolean prusaSL1ServerEnabled = true;

    // Output Folder for .sl1 & .photon files
    @Persistent
    private String fileOutputDirectory = System.getProperty("user.home");

    @Persistent
    private boolean removeSl1FileAfterImport = true;

    // UI Options
    @Persistent
    private boolean bringWindowToFgOnImport = true;

    // Preview image generation
    @Persistent
    private boolean useProvidedPreviewImages = false;

    @Persistent
    private int largePreviewMaxWidth = 1024;

    @Persistent
    private int largePreviewMaxHeight = 1024;

    @Persistent
    private int smallPreviewMaxWidth = 256;

    @Persistent
    private int smallPreviewMaxHeight = 256;

    @Persistent
    private String backgroundColor = String.format("0x%06X", DEFAULT_BACKGROUND_COLOR);

    @Persistent
    private String ambientColor = String.format("0x%06X", DEFAULT_AMBIENT_COLOR);

    @Persistent
    private String diffuseColor = String.format("0x%06X", DEFAULT_DIFFUSE_COLOR);

    // Photon File generation Settings

    // Common
    @Persistent
    private float bedSizeX = 68.04f;

    @Persistent
    private float bedSizeY = 120.96f;

    @Persistent
    private float bedSizeZ = 150.0f;

    @Persistent
    private int resolutionX = 1440;

    @Persistent
    private int resolutionY = 2560;

    @Persistent
    private float exposureOffTime = 2.0f;

    @Persistent
    private int photonFileVersion = 2;

    // Print Parameters
    @Persistent
    private float bottomLiftDistance = 5.0f;

    @Persistent
    private float bottomLiftSpeed = 65.0f;

    @Persistent
    private float liftingDistance = 5.0f;

    @Persistent
    private float liftingSpeed = 65.0f;

    @Persistent
    private float retractSpeed = 150.0f;

    @Persistent
    private float bottomLightOffDelay = 2.0f;

    @Persistent
    private float lightOffDelay = 2.0f;

    private static volatile PrusaSL1Configuration instance = null;

    public PrusaSL1Configuration() {
        System.out.println("CONFIG PATH: " + getConfigurationDirectory());
    }

    public static synchronized PrusaSL1Configuration getInstance() {
        if(null == instance) {
            instance = new PrusaSL1Configuration();
            final File file = getConfigurationFile();
            if(file.exists()) {
                try {
                    instance.load(file);
                }
                catch (final Exception e) {
                    System.err.println("Error reading configuration file " + file + ": " + e);
                    instance = new PrusaSL1Configuration();
                }
            }
        }

        if(null == instance)
            instance = new PrusaSL1Configuration();

        return instance;
    }

    public static synchronized void resetToDefault() throws Exception {
        new PrusaSL1Configuration().save();
        getInstance().load(getConfigurationFile());
    }

    public void save() throws Exception {
        clear();
        save(getConfigurationFile());
    }

    public String getFileOutputDirectory() {
        if(fileOutputDirectory.endsWith(File.separator))
            return fileOutputDirectory;
        return fileOutputDirectory + File.separator;
    }

    public void setFileOutputDirectory(String fileOutputDirectory) {
        this.fileOutputDirectory = fileOutputDirectory;
    }


    public int getPrusaSL1ServerPort() {
        return prusaSL1ServerPort;
    }

    public void setPrusaSL1ServerPort(int prusaSL1ServerPort) {
        this.prusaSL1ServerPort = prusaSL1ServerPort;
    }

    public boolean isPrusaSL1ServerEnabled() {
        return prusaSL1ServerEnabled;
    }

    public void setPrusaSL1ServerEnabled(boolean prusaSL1ServerEnabled) {
        this.prusaSL1ServerEnabled = prusaSL1ServerEnabled;
    }

    public boolean isRemoveSl1FileAfterImport() {
        return removeSl1FileAfterImport;
    }

    public void setRemoveSl1FileAfterImport(boolean removeSl1FileAfterImport) {
        this.removeSl1FileAfterImport = removeSl1FileAfterImport;
    }

    public boolean isBringWindowToFgOnImport() {
        return bringWindowToFgOnImport;
    }

    public void setBringWindowToFgOnImport(boolean bringWindowToFgOnImport) {
        this.bringWindowToFgOnImport = bringWindowToFgOnImport;
    }

    public static String getConfigurationDirectory() {
        try {
            // return the directory of current jar file
            final File file = new File(PrusaSL1Configuration.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if(file.isDirectory()) // running from IDE
                return file.getPath();
            return file.getParent(); // running fron JAR
        }
        catch(final URISyntaxException e) {
            // return current working directory
            return System.getProperty("user.dir");
        }
    }

    public static File getConfigurationFile() {
        return new File (getConfigurationDirectory() + File.separator + CONFIG_FILE_NAME);
    }

    public boolean isUseProvidedPreviewImages() {
        return useProvidedPreviewImages;
    }

    public void setUseProvidedPreviewImages(boolean useProvidedPreviewImages) {
        this.useProvidedPreviewImages = useProvidedPreviewImages;
    }

    public int getLargePreviewMaxWidth() {
        return largePreviewMaxWidth;
    }

    public void setLargePreviewMaxWidth(int largePreviewMaxWidth) {
        this.largePreviewMaxWidth = largePreviewMaxWidth;
    }

    public int getLargePreviewMaxHeight() {
        return largePreviewMaxHeight;
    }

    public void setLargePreviewMaxHeight(int largePreviewMaxHeight) {
        this.largePreviewMaxHeight = largePreviewMaxHeight;
    }

    public int getSmallPreviewMaxWidth() {
        return smallPreviewMaxWidth;
    }

    public void setSmallPreviewMaxWidth(int smallPreviewMaxWidth) {
        this.smallPreviewMaxWidth = smallPreviewMaxWidth;
    }

    public int getSmallPreviewMaxHeight() {
        return smallPreviewMaxHeight;
    }

    public void setSmallPreviewMaxHeight(int smallPreviewMaxHeight) {
        this.smallPreviewMaxHeight = smallPreviewMaxHeight;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public int getBackgroundColorRGB() {
        return toRGB(getBackgroundColor(), DEFAULT_BACKGROUND_COLOR);
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = toColor(backgroundColor, this.backgroundColor);
    }

    public String getAmbientColor() {
        return ambientColor;
    }

    public int getAmbientColorRGB() {
        return toRGB(getAmbientColor(), DEFAULT_AMBIENT_COLOR);
    }

    public void setAmbientColor(String ambientColor) {
        this.ambientColor = toColor(ambientColor, this.ambientColor);
    }

    public String getDiffuseColor() {
        return diffuseColor;
    }

    public int getDiffuseColorRGB() {
        return toRGB(getDiffuseColor(), DEFAULT_DIFFUSE_COLOR);
    }

    public void setDiffuseColor(String diffuseColor) {
        this.diffuseColor = toColor(diffuseColor, this.diffuseColor);
    }

    public float getBedSizeX() {
        return bedSizeX;
    }

    public void setBedSizeX(float bedSizeX) {
        this.bedSizeX = bedSizeX;
    }

    public float getBedSizeY() {
        return bedSizeY;
    }

    public void setBedSizeY(float bedSizeY) {
        this.bedSizeY = bedSizeY;
    }

    public float getBedSizeZ() {
        return bedSizeZ;
    }

    public void setBedSizeZ(float bedSizeZ) {
        this.bedSizeZ = bedSizeZ;
    }

    public int getResolutionX() {
        return resolutionX;
    }

    public void setResolutionX(int resolutionX) {
        this.resolutionX = resolutionX;
    }

    public int getResolutionY() {
        return resolutionY;
    }

    public void setResolutionY(int resolutionY) {
        this.resolutionY = resolutionY;
    }

    public float getExposureOffTime() {
        return exposureOffTime;
    }

    public void setExposureOffTime(float exposureOffTime) {
        this.exposureOffTime = exposureOffTime;
    }

    public int getPhotonFileVersion() {
        return photonFileVersion;
    }

    public void setPhotonFileVersion(int photonFileVersion) {
        this.photonFileVersion = photonFileVersion;
    }

    public float getBottomLiftDistance() {
        return bottomLiftDistance;
    }

    public void setBottomLiftDistance(float bottomLiftDistance) {
        this.bottomLiftDistance = bottomLiftDistance;
    }

    public float getBottomLiftSpeed() {
        return bottomLiftSpeed;
    }

    public void setBottomLiftSpeed(float bottomLiftSpeed) {
        this.bottomLiftSpeed = bottomLiftSpeed;
    }

    public float getLiftingDistance() {
        return liftingDistance;
    }

    public void setLiftingDistance(float liftingDistance) {
        this.liftingDistance = liftingDistance;
    }

    public float getLiftingSpeed() {
        return liftingSpeed;
    }

    public void setLiftingSpeed(float liftingSpeed) {
        this.liftingSpeed = liftingSpeed;
    }

    public float getRetractSpeed() {
        return retractSpeed;
    }

    public void setRetractSpeed(float retractSpeed) {
        this.retractSpeed = retractSpeed;
    }

    public float getBottomLightOffDelay() {
        return bottomLightOffDelay;
    }

    public void setBottomLightOffDelay(float bottomLightOffDelay) {
        this.bottomLightOffDelay = bottomLightOffDelay;
    }

    public float getLightOffDelay() {
        return lightOffDelay;
    }

    public void setLightOffDelay(float lightOffDelay) {
        this.lightOffDelay = lightOffDelay;
    }

    private String toColor(final String color, final String defaultValue) {
        try {
            return String.format("0x%06X", Color.decode(color).getRGB() & 0xFFFFFF);
        }
        catch(final NumberFormatException e) {
            return defaultValue;
        }
    }

    private int toRGB(final String color, final int defaultValue) {
        try {
            return Color.decode(color).getRGB() & 0xFFFFFF;
        }
        catch(final NumberFormatException e) {
            return defaultValue;
        }
    }
}
