package photon.application.extensions.prusasl1.file.parts;

import photon.application.extensions.prusasl1.configuration.PrusaSL1Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrusaSL1FileHeader {
    private final Map<String,String> properties = Collections.synchronizedMap( new HashMap<>() );

    public void read(final String prefix, final InputStream in) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        final Pattern propertyPattern = Pattern.compile("^([^=]+)=(.+)$");

        // parse ini file into map
        String line;
        while ((line = reader.readLine()) != null ) {
            final Matcher matcher = propertyPattern.matcher(line);
            if(matcher.matches()) {
                properties.put(prefix + "." + matcher.group(1).trim(), matcher.group(2).trim());
            }
        }
    }

    public boolean isValid() {
        return getNumberOfLayers() > 0;
    }

    public float getUsedMaterialMl() {
        return getOptFloat("config.usedMaterial", 0);
    }

    public float getWeightGr() {
        return getUsedMaterialMl() * getDensityGrMl();
    }

    public float getCost() {
        final float bottle_volume_ml = getOptFloat("prusaslicer.bottle_volume", 1000);
        final float bottle_cost = getOptFloat("prusaslicer.bottle_cost", 0);
        final float cost_per_ml = bottle_cost / bottle_volume_ml;

        return getUsedMaterialMl() * cost_per_ml;
    }

    public float getDensityGrMl() {
        final float bottle_volume_ml = getOptFloat("prusaslicer.bottle_volume", 1000);
        final float bottle_weight_kg = getOptFloat("prusaslicer.bottle_weight", 1);

        return (bottle_weight_kg*1000) / bottle_volume_ml;
    }

    public String getLayerFilePrefix() {
        return getString("config.jobDir");
    }

    public float getExposureTimeSeconds() {
        return getFloat("config.expTime");
    }

    public float getExposureBottomTimeSeconds() {
        return getFloat("config.expTimeFirst");
    }

    public float getLayerHeightMilimeter() {
        return getFloat("config.layerHeight");
    }

    public int getBottomLayers() {
        return getInteger("config.numFade");
    }

    public int getNumberOfLayers() {
        return getInteger("config.numFast");
    }

    public int getPrintTimeSeconds() {
        return getInteger("config.printTime");
    }

    private int getInteger(final String key) {
        return (int) getFloat(key);
    }

    public int getPhotonFileVersion() {
        return PrusaSL1Configuration.getInstance().getPhotonFileVersion();
    }

    public float getBedSizeX() {
        return PrusaSL1Configuration.getInstance().getBedSizeX();
    }

    public float getBedSizeY() {
        return PrusaSL1Configuration.getInstance().getBedSizeY();
    }

    public float getBedSizeZ() {
        return PrusaSL1Configuration.getInstance().getBedSizeZ();
    }

    public int getResolutionX() {
        return PrusaSL1Configuration.getInstance().getResolutionX();
    }

    public int getResolutionY() {
        return PrusaSL1Configuration.getInstance().getResolutionY();
    }

    public float getExposureOffTime() {
        return PrusaSL1Configuration.getInstance().getExposureOffTime();
    }

    public float getBottomLiftDistance() {
        return PrusaSL1Configuration.getInstance().getBottomLiftDistance();
    }

    public float getBottomLiftSpeed() {
        return PrusaSL1Configuration.getInstance().getBottomLiftSpeed();
    }

    public float getLiftingDistance() {
        return PrusaSL1Configuration.getInstance().getLiftingDistance();
    }

    public float getLiftingSpeed() {
        return PrusaSL1Configuration.getInstance().getLiftingSpeed();
    }

    public float getRetractSpeed() {
        return PrusaSL1Configuration.getInstance().getRetractSpeed();
    }

   public float getBottomLightOffDelay() {
        return PrusaSL1Configuration.getInstance().getBottomLightOffDelay();
    }

    public float getLightOffDelay() {
        return PrusaSL1Configuration.getInstance().getLightOffDelay();
    }

    private String getString(final String key) {
        final String value = properties.get(key);
        if(null == value)
            throw new IllegalArgumentException("Property '" + key + "' not found");
        return value.trim();
    }

    private float getFloat(final String key) {
        final String value = properties.get(key);
        if(null == value || value.trim().isEmpty())
            throw new IllegalArgumentException("Property '" + key + "' not found");
        try {
            return Float.parseFloat(value);
        }
        catch(final NumberFormatException e) {
            throw new NumberFormatException("Unable to parse value " + value + " for property '" + key + "' as Float");
        }
    }

    private float getOptFloat(final String key, final float defaultValue) {
        final String value = properties.get(key);
        if(null == value || value.trim().isEmpty())
            return defaultValue;
        try {
            return Float.parseFloat(value);
        }
        catch(final NumberFormatException e) {
            return defaultValue;
        }
    }
}
