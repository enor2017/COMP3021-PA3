package hk.ust.cse.comp3021.pa3.util;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class TimeIntervalGenerator implements Generator<Long> {

    /**
     * @return an instance of {@link TimeIntervalGenerator} that produces time interval complying to a gaussian
     * distribution with mean=1 second and standard deviation=1.
     */
    public static TimeIntervalGenerator everySecond() {
        return new TimeIntervalGenerator(1000, 1);
    }

    /**
     * @return an instance of {@link TimeIntervalGenerator} that produces time interval complying to a gaussian
     * distribution with mean=10 millisecond and standard deviation=1.
     */
    public static @NotNull TimeIntervalGenerator veryFast() {
        return new TimeIntervalGenerator(10, 1);
    }

    /**
     * @param milliseconds The expectation of the time interval returned by {@link TimeIntervalGenerator#next()}.
     * @return an instance of {@link TimeIntervalGenerator} that produces time interval complying to a gaussian
     * distribution with mean={@code milliseconds} and standard deviation=1.
     */
    public static @NotNull TimeIntervalGenerator expectedMilliseconds(int milliseconds) {
        return new TimeIntervalGenerator(milliseconds, 1);
    }

    private final int mean;
    private final int std;

    /**
     * @return return the time interval in milliseconds.
     */
    @Override
    public Long next() {
        var rng = new Random();
        return (long) rng.nextGaussian(mean, std);
    }

    private TimeIntervalGenerator(int mean, int std) {
        this.mean = mean;
        this.std = std;
    }
}
