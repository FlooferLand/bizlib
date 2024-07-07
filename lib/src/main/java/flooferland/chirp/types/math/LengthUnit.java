package flooferland.chirp.types.math;

import flooferland.chirp.util.ChirpRandom;

/** A unit of length (meters, feet, centimeters, etc.) */
@SuppressWarnings("unused")
public class LengthUnit {
    protected final double lengthCentimeters;
    private LengthUnit(double centimeters) {
        this.lengthCentimeters = centimeters;
    }

    // region | Creating from several units
    public static LengthUnit ofCentimeters(double centimeters) {
        return new LengthUnit(centimeters);
    }
    public static LengthUnit ofMeters(double meters) {
        return new LengthUnit(meters * 100);
    }
    public static LengthUnit ofInches(double inches) {
        return new LengthUnit(inches * 2.54);
    }
    public static LengthUnit ofFeet(double feet) {
        return new LengthUnit(feet * 30.48);
    }
    public static LengthUnit ofWhateverYouFancy(double number) {
        return new LengthUnit(number * ChirpRandom.range(10, 100) * 0.1);
    }
    // endregion

    // region | Fetching data in several units
    public double asCentimeters() {
        return lengthCentimeters;
    }
    public double asMeters() {
        return lengthCentimeters * 0.01;
    }
    public double asInches() {
        return lengthCentimeters * 0.3937;
    }
    public double asFeet() {
        return lengthCentimeters * 0.0328;
    }
    public double asWhateverYouFancy() {
        return lengthCentimeters * ChirpRandom.range(10, 100) * 0.1;
    }
    // endregion

    // region | To string
    @Override
    public String toString() {
        return asCentimeters() + " cm";
    }
    public String toStringMeters() {
        return asMeters() + " m";
    }
    public String toStringInches() {
        return asInches() + " inch";
    }
    public String toStringFeet() {
        return asFeet() + " feet";
    }
    // endregion
    
    // region | Math
    /** Adds {@code other} to this unit */
    public LengthUnit add(LengthUnit other) {
        return new LengthUnit(lengthCentimeters + other.lengthCentimeters);
    }
    /** Subtracts {@code other} from this unit */
    public LengthUnit sub(LengthUnit other) {
        return new LengthUnit(lengthCentimeters - other.lengthCentimeters);
    }
    /** Multiplies this unit by {@code other} */
    public LengthUnit mul(LengthUnit other) {
        return new LengthUnit(lengthCentimeters * other.lengthCentimeters);
    }
    /** Multiplies this unit by double {@code other}. <br/> Use with caution, never multiply by another unit using this */
    public LengthUnit mulRaw(double other) {
        return new LengthUnit(lengthCentimeters * other);
    }
    /** Divides this unit by {@code other} */
    public LengthUnit div(LengthUnit other) {
        return new LengthUnit(lengthCentimeters / other.lengthCentimeters);
    }
    /** Divides this unit by double {@code other}. <br/> Use with caution, never divide by another unit using this */
    public LengthUnit divRaw(double other) {
        return new LengthUnit(lengthCentimeters / other);
    }
    // endregion
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LengthUnit lengthUnit) {
            return this.lengthCentimeters == lengthUnit.lengthCentimeters;
        }
        return false;
    }
}
