package flooferland.showbiz.lowlevel.show;

import flooferland.showbiz.lowlevel.types.SignalContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShowData {
    public final SignalContainer Signal;
    public final byte[] Audio;
    public final byte[] Video;
    public final flooferland.showbiz.lowlevel.show.Metadata Metadata;

    public ShowData(@Nonnull int[] signal, @Nonnull byte[] audio, @Nullable byte[] video) {
        this(new SignalContainer(signal), audio, video);
    }
    public ShowData(@Nonnull SignalContainer signal, @Nonnull byte[] audio, @Nullable byte[] video) {
        this.Signal = signal;
        this.Audio = audio;
        this.Video = video;
        this.Metadata = new Metadata();
    }

    public ShowData(@Nonnull int[] signal, @Nonnull byte[] audio, @Nullable byte[] video, @Nonnull Metadata metadata) {
        this(new SignalContainer(signal), audio, video, metadata);
    }
    public ShowData(@Nonnull SignalContainer signal, @Nonnull byte[] audio, @Nullable byte[] video, @Nonnull Metadata metadata) {
        this.Signal = signal;
        this.Audio = audio;
        this.Video = video;
        this.Metadata = metadata;
    }
}

