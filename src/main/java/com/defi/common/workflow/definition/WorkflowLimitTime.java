package com.defi.common.workflow.definition;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Represents a time constraint for a workflow with support for skipping weekends.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowLimitTime {
    private int value;                  // Amount of time units
    private String unit;                // Time unit ("DAY" or "MONTH")
    private boolean skipSaturday;       // Whether to skip Saturdays when calculating days
    private boolean skipSunday;         // Whether to skip Sundays when calculating days

    public enum TIME_UNIT {
        DAY, MONTH
    }

    /**
     * Calculates the time limit from the current moment.
     * @return the time limit in milliseconds
     */
    public long getLimitTimeInMilliseconds() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limitTime = calculateLimitTime(now);

        // Convert to milliseconds
        return java.time.Duration.between(now, limitTime).toMillis();
    }

    /**
     * Calculates the limit time from a given starting time.
     * @param fromTime the starting point for calculation
     * @return the calculated limit time
     */
    public LocalDateTime calculateLimitTime(LocalDateTime fromTime) {
        if (unit == null) {
            throw new IllegalStateException("Time unit must not be null");
        }

        TIME_UNIT timeUnit = TIME_UNIT.valueOf(unit.toUpperCase());
        LocalDateTime limitTime = fromTime;

        switch (timeUnit) {
            case DAY:
                limitTime = calculateDayLimit(fromTime);
                break;
            case MONTH:
                limitTime = fromTime.plusMonths(value);
                break;
            default:
                throw new IllegalArgumentException("Unsupported time unit: " + unit);
        }

        return limitTime;
    }

    /**
     * Calculates the time limit by days, optionally skipping weekends.
     * @param fromTime the starting time
     * @return the calculated limit time
     */
    private LocalDateTime calculateDayLimit(LocalDateTime fromTime) {
        LocalDateTime limitTime = fromTime;
        int daysToAdd = value;

        if (skipSaturday || skipSunday) {
            // Add working days only
            int addedDays = 0;
            while (addedDays < daysToAdd) {
                limitTime = limitTime.plusDays(1);
                DayOfWeek dayOfWeek = limitTime.getDayOfWeek();

                boolean isWeekend = (skipSaturday && dayOfWeek == DayOfWeek.SATURDAY) ||
                        (skipSunday && dayOfWeek == DayOfWeek.SUNDAY);

                if (!isWeekend) {
                    addedDays++;
                }
            }
        } else {
            // Simply add days
            limitTime = limitTime.plusDays(daysToAdd);
        }

        return limitTime;
    }

    /**
     * Checks whether the current time has passed the calculated limit time.
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return getLimitTimeInMilliseconds() <= 0;
    }

    /**
     * Returns the remaining time in milliseconds.
     * @return the remaining time, may be negative if already expired
     */
    public long getRemainingTimeInMilliseconds() {
        return getLimitTimeInMilliseconds();
    }

    /**
     * Returns the remaining time in a specific time unit.
     * @param timeUnit the desired unit
     * @return the remaining time converted to the given unit
     */
    public long getRemainingTime(TimeUnit timeUnit) {
        long remainingMs = getRemainingTimeInMilliseconds();
        return timeUnit.convert(remainingMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public String toString() {
        return "WorkflowLimitTime{" +
                "value=" + value +
                ", unit='" + unit + '\'' +
                ", skipSaturday=" + skipSaturday +
                ", skipSunday=" + skipSunday +
                '}';
    }
}
