package flooferland.showbiz.showformat.data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShowData {
    public final int[] Signal;
    public final byte[] Audio;
    public final byte[] Video;
    public final ShowMetadata Metadata;

    public ShowData(@Nonnull int[] signal, @Nonnull byte[] audio, @Nullable byte[] video) {
        this.Signal = signal;
        this.Audio = audio;
        this.Video = video;
        this.Metadata = new ShowMetadata();
    }
    
    public ShowData(@Nonnull int[] signal, @Nonnull byte[] audio, @Nullable byte[] video, @Nonnull ShowMetadata metadata) {
        this.Signal = signal;
        this.Audio = audio;
        this.Video = video;
        this.Metadata = metadata;
    }
}

