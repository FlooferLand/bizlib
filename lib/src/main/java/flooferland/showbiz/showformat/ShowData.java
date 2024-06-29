package flooferland.showbiz.showformat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShowData {
    public int[] Signal;
    public byte[] Audio;
    public byte[] Video;

    public ShowData(@Nonnull int[] signal, @Nonnull byte[] audio, @Nullable byte[] video) {
        this.Signal = signal;
        this.Audio = audio;
        this.Video = video;
    }
}
