package flooferland.showbiz.lowlevel;

import flooferland.chirp.safety.Result;
import flooferland.showbiz.lowlevel.show.ShowData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Common functions between formats */
public abstract interface IShowFormat {
    /** Generally used for loading the data from disk */
    public Result<ShowData, String> readFromStream(InputStream stream) throws IOException;
    
    /** Generally used for saving the data to disk */
    public void writeToStream(ShowData format, OutputStream stream) throws Exception;
}
