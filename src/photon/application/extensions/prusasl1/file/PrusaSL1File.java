package photon.application.extensions.prusasl1.file;

import photon.application.extensions.prusasl1.configuration.PrusaSL1Configuration;
import photon.application.extensions.prusasl1.file.parts.PrusaSL1FileHeader;
import photon.application.extensions.prusasl1.file.parts.PrusaSL1FileLayer;
import photon.application.extensions.prusasl1.file.utilites.RenderedImage;
import photon.application.extensions.prusasl1.printhost.utilites.PrintHostTemporaryFile;

import javax.imageio.ImageIO;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public class PrusaSL1File implements Closeable {
    private final File file;
    private final ZipFile zipFile; // will not work with ZipInputStream because of "java.util.zip.ZipException: only DEFLATED entries can have EXT descriptor"
    private final PrusaSL1FileHeader header;
    private final PrusaSL1FileLayer[] layers;

    private final RenderedImage smallThumbnail;
    private final RenderedImage largeThumbnail;

    public PrusaSL1File(final File file) throws IOException {
        this.file = file;
        zipFile = new ZipFile(file);

        // read header
        header = new PrusaSL1FileHeader();
        zipFile.stream().parallel().forEach(entry -> {
            final String name = entry.getName();
            if(name.endsWith(".ini")) {
                try {
                    header.read(name.substring(0, name.length()-4), zipFile.getInputStream(entry));
                }
                catch(final IOException e) {
                    throw new RuntimeException("Error reading config file '" + name + "': " + e.getMessage());
                }
            }
        });

        if(!header.isValid())
            throw new IllegalArgumentException("No valid config.ini found in file " + file);

        // read layers and previews
        final List<RenderedImage> thumbnails = Collections.synchronizedList(new ArrayList<>());
        layers = new PrusaSL1FileLayer[header.getNumberOfLayers()];
        {
            final Pattern layerPattern = Pattern.compile( "^" + Pattern.quote(header.getLayerFilePrefix()) + "([0-9]+)\\.png$");
            final Pattern thumbnailPattern = Pattern.compile( "^thumbnail/thumbnail([0-9]+)x([0-9]+)\\.png$");

            zipFile.stream().parallel().forEach(entry -> {
                final String name = entry.getName();
                final Matcher layerMatcher = layerPattern.matcher(name);
                final Matcher thumbnailMatcher = thumbnailPattern.matcher(name);

                if(layerMatcher.matches()) {
                    try {
                        final int index = Integer.parseInt(layerMatcher.group(1), 10);
                        layers[index] = new PrusaSL1FileLayer(index, zipFile.getInputStream(entry));
                    }
                    catch(final IOException e) {
                        throw new RuntimeException("Error reading Layer '" + name + "': " + e.getMessage());
                    }
                }
                else if(thumbnailMatcher.matches()) {
                    try {
                        thumbnails.add(new RenderedImage(ImageIO.read(new BufferedInputStream(zipFile.getInputStream(entry)))));
                    }
                    catch(final Exception e) {
                        System.err.println("Error reading thumbnail '" + name + "': " + e.getMessage());
                    }
                }
            });
        }

        // order thumbnails by size
        thumbnails.sort((a,b) -> {
            return a.getImageDataSize() - b.getImageDataSize();
        });

        if(!thumbnails.isEmpty()) {
            smallThumbnail = thumbnails.get(0);
            largeThumbnail = thumbnails.get(thumbnails.size()-1);
        }
        else {
            smallThumbnail = null;
            largeThumbnail = null;
        }
    }

    public PrusaSL1FileHeader getHeader() {
        return header;
    }

    public PrusaSL1FileLayer[] getLayers() {
        return layers;
    }

    public RenderedImage getOrCreateSmallThumbnail(final RenderedImage source) {
        final PrusaSL1Configuration configuration = PrusaSL1Configuration.getInstance();

        if(configuration.isUseProvidedPreviewImages() && null != smallThumbnail)
            return smallThumbnail;

        return source.getScaledImage(configuration.getSmallPreviewMaxWidth(), configuration.getSmallPreviewMaxHeight());
    }

    public RenderedImage getOrCreateLargeThumbnail(final RenderedImage source) {
        final PrusaSL1Configuration configuration = PrusaSL1Configuration.getInstance();

        if(configuration.isUseProvidedPreviewImages() && null != largeThumbnail)
            return largeThumbnail;

        return source.getScaledImage(configuration.getLargePreviewMaxWidth(), configuration.getLargePreviewMaxHeight());
    }

    @Override
    public void close() throws IOException {
        zipFile.close();

        if(file instanceof PrintHostTemporaryFile) {
            ((PrintHostTemporaryFile) file).cleanUp();
        }
    }
}
