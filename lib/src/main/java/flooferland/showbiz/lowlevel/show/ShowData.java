package flooferland.showbiz.lowlevel.show;

import flooferland.chirp.safety.Result;
import flooferland.showbiz.lowlevel.types.SignalContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Container for the signal, video, metadata, and other storage show things */
public class ShowData {
    public final SignalContainer Signal;
    public final byte[] Audio;
    public final byte[] Video;
    public final Metadata Metadata;

    public ShowData(@Nonnull SignalContainer signal, @Nonnull byte[] audio, @Nullable byte[] video) {
        this.Signal = signal;
        this.Audio = audio;
        this.Video = video;
        this.Metadata = new Metadata();
    }
    
    public ShowData(@Nonnull SignalContainer signal, @Nonnull byte[] audio, @Nullable byte[] video, @Nonnull Metadata metadata) {
        this.Signal = signal;
        this.Audio = audio;
        this.Video = video;
        this.Metadata = metadata;
    }
    
    public static Result<ShowData, String> fromRshowSignal(int[] signal, @Nonnull byte[] audio, @Nullable byte[] video) {
        return SignalContainer.fromRshowSignal(signal).mapOk(o -> new ShowData(o, audio, video));
    }
    public static Result<ShowData, String> fromRshowSignal(int[] signal, @Nonnull byte[] audio, @Nullable byte[] video, @Nonnull Metadata metadata) {
        return SignalContainer.fromRshowSignal(signal).mapOk(o -> new ShowData(o, audio, video, metadata));
    }
}

