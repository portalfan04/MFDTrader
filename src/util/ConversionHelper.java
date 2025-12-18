package util;

public class ConversionHelper
{
    public static double dayToSecond(double days)
    {
        return days * 86400;
    }
    public static double secondToDay(double second)
    {
        return second / 86400;
    }
    public static double roundToSigFigs(double num, int sigFigs)
    {
        if (num == 0) return 0;
        final double d = Math.ceil(Math.log10(Math.abs(num)));
        final int power = sigFigs - (int) d;
        final double magnitude = Math.pow(10, power);
        return Math.round(num * magnitude) / magnitude;
    }
    public static double meterToAU(double meters)
    {
        return meters / 149_597_870_700.0;
    }
    public static double AUtoMeter(double AU)
    {
        return AU * 149_597_870_700.0;
    }


}
