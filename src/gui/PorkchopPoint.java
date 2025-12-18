package gui;

public class PorkchopPoint {
    public double deltaV;
    public double departureTime;
    public double transferTime;

    public PorkchopPoint(double deltaV, double departureTime, double transferTime) {
        this.deltaV = deltaV;
        this.departureTime = departureTime;
        this.transferTime = transferTime;
    }
}