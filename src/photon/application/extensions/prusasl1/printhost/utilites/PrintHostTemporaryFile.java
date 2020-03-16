package photon.application.extensions.prusasl1.printhost.utilites;

import org.jetbrains.annotations.NotNull;
import photon.application.extensions.prusasl1.configuration.PrusaSL1Configuration;

import java.io.File;

public class PrintHostTemporaryFile extends File {
    public PrintHostTemporaryFile(@NotNull String pathname) {
        super(pathname);
    }

    public void cleanUp() {
        if(PrusaSL1Configuration.getInstance().isRemoveSl1FileAfterImport())
            delete();
    }
}
