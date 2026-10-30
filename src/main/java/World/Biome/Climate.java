package World.Biome;

/*
 * A point in climate space. Every column of the world samples one of these from
 * broad noise fields, and every biome declares the climate it prefers. The biome
 * whose preferred climate is closest to a column's climate wins that column.
 *
 * All three values sit roughly in [-1, 1]:
 *  - temperature : cold to hot
 *  - humidity    : dry to wet
 *  - elevation   : lowland to highland, decides where the land rises into ranges
 */
public record Climate(double temperature, double humidity, double elevation) {

    public double distanceTo(Climate other) {
        double dt = temperature - other.temperature;
        double dh = humidity - other.humidity;
        double de = elevation - other.elevation;
        return Math.sqrt(dt * dt + dh * dh + de * de);
    }
}
