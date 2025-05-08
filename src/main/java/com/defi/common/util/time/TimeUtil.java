package com.defi.common.util.time;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * {@code TimeUtil} provides utility methods for converting between Java 8 {@link ZonedDateTime}
 * and legacy SQL types such as {@link java.sql.Timestamp} and {@link java.sql.Date}.
 *
 * <p>All conversions use the default time zone: {@code Asia/Ho_Chi_Minh}.</p>
 */
public class TimeUtil {
    /**
     * Private constructor to prevent instantiation.
     */
    private TimeUtil() {
        // Utility class
    }


    /**
     * Default time zone used for all conversions.
     */
    private static final ZoneId defaultZoneId = ZoneId.of("Asia/Ho_Chi_Minh");

    /**
     * Converts a {@link ZonedDateTime} to a {@link Timestamp}.
     *
     * @param dateTime the {@link ZonedDateTime} to convert
     * @return the corresponding {@link Timestamp}, or {@code null} if input is null
     */
    public static Timestamp convertToSqlTimestamp(ZonedDateTime dateTime) {
        return dateTime == null ? null : Timestamp.from(dateTime.toInstant());
    }

    /**
     * Converts a {@link Timestamp} to a {@link ZonedDateTime} using the default zone.
     *
     * @param timestamp the SQL {@link Timestamp}
     * @return the corresponding {@link ZonedDateTime}
     */
    public static ZonedDateTime convertToZonedDateTime(Timestamp timestamp) {
        return timestamp.toLocalDateTime().atZone(defaultZoneId);
    }

    /**
     * Converts a {@link ZonedDateTime} to a {@link Date} (sql date, no time).
     *
     * @param date the {@link ZonedDateTime} to convert
     * @return the corresponding SQL {@link Date}
     */
    public static Date convertToSqlDate(ZonedDateTime date) {
        return new Date(date.toInstant().toEpochMilli());
    }

    /**
     * Converts a SQL {@link Date} to a {@link ZonedDateTime} using the default zone.
     *
     * @param date the SQL {@link Date}
     * @return the corresponding {@link ZonedDateTime}
     */
    public static ZonedDateTime convertToZonedDateTime(Date date) {
        return date.toInstant().atZone(defaultZoneId);
    }

    /**
     * Gets the current date and time in {@code Asia/Ho_Chi_Minh} time zone.
     *
     * @return the current {@link ZonedDateTime}
     */
    public static ZonedDateTime getZonedDateTimeNow() {
        return ZonedDateTime.now(defaultZoneId);
    }
}
