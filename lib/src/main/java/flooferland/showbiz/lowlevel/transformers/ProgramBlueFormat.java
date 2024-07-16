package flooferland.showbiz.lowlevel.transformers;

import flooferland.chirp.safety.Result;
import flooferland.showbiz.lowlevel.IShowFormat;
import flooferland.showbiz.lowlevel.show.ShowData;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Format for .shw files */
public class ProgramBlueFormat implements IShowFormat {
    @Nullable
    @Override
    public Result<ShowData, String> readFromStream(InputStream stream) throws IOException {
        return Result.err("Unimplemented");
    }

    @Override
    public void writeToStream(ShowData format, OutputStream stream) throws Exception {
        throw new RuntimeException("Unimplemented");
    }
}
