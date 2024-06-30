package flooferland.chirp.types;

import jdk.jfr.Unsigned;

public class SemanticVersion {
    @Unsigned private final int major;
    @Unsigned private final int minor;
    @Unsigned private final int patch;
    
    // region | Constructors
    public SemanticVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public SemanticVersion(int major, int minor) {
        this(major, minor, 0);
    }

    public SemanticVersion(int major) {
        this(major, 0, 0);
    }
}
