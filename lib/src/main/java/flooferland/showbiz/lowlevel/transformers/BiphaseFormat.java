package flooferland.showbiz.lowlevel.transformers;

import flooferland.chirp.safety.Result;
import flooferland.showbiz.lowlevel.IShowFormat;
import flooferland.showbiz.lowlevel.show.ShowData;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Format that tries to closely replicate what Aaron and real life Rock-afire shows use.
 * Exporting music and signal data directly into a WAV file instead of packaging it all up in a 7z or a zshw or whatever.
 * [4 track audio, 2 soundtracks, 2 data signals]
 */
public class BiphaseFormat implements IShowFormat {
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
