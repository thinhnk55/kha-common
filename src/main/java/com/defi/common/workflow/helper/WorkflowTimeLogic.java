package com.defi.common.workflow.helper;
import com.defi.common.workflow.definition.WorkflowLimitTime;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Static helper class for calculating time constraints based on WorkflowLimitTime.
 */
public final class WorkflowTimeLogic {

    private WorkflowTimeLogic() {
        // Prevent instantiation
    }

    public static long getLimitTimeInMilliseconds(WorkflowLimitTime config) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limitTime = calculateLimitTime(config, now);
        return java.time.Duration.between(now, limitTime).toMillis();
    }

    public static LocalDateTime calculateLimitTime(WorkflowLimitTime config, LocalDateTime fromTime) {
        if (config.getUnit() == null) {
            throw new IllegalStateException("Time unit must not be null");
        }

        return switch (config.getUnit()) {
            case DAY -> calculateDayLimit(config, fromTime);
            case MONTH -> fromTime.plusMonths(config.getValue());
        };
    }

    private static LocalDateTime calculateDayLimit(WorkflowLimitTime config, LocalDateTime fromTime) {
        LocalDateTime limitTime = fromTime;
        int daysToAdd = config.getValue();

        if (config.isSkipSaturday() || config.isSkipSunday()) {
            int addedDays = 0;
            while (addedDays < daysToAdd) {
                limitTime = limitTime.plusDays(1);
                DayOfWeek day = limitTime.getDayOfWeek();
                boolean isWeekend =
                        (config.isSkipSaturday() && day == DayOfWeek.SATURDAY) ||
                                (config.isSkipSunday() && day == DayOfWeek.SUNDAY);
                if (!isWeekend) {
                    addedDays++;
                }
            }
        } else {
            limitTime = limitTime.plusDays(daysToAdd);
        }

        return limitTime;
    }

    public static boolean isExpired(WorkflowLimitTime config) {
        return getLimitTimeInMilliseconds(config) <= 0;
    }

    public static long getRemainingTimeInMilliseconds(WorkflowLimitTime config) {
        return getLimitTimeInMilliseconds(config);
    }

    public static long getRemainingTime(WorkflowLimitTime config, TimeUnit unit) {
        return unit.convert(getRemainingTimeInMilliseconds(config), TimeUnit.MILLISECONDS);
    }
}
