package flooferland.showbiz.showformat;

import flooferland.showbiz.showformat.data.ShowData;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

/** Common functions between formats */
public abstract interface IShowFormat {
    /** Generally used for loading the data from disk */
    public @Nullable ShowData readFromStream(InputStream stream) throws IOException;
    
    /** Generally used for saving the data to disk */
    public InputStream writeToStream(ShowData format) throws IOException;
}
