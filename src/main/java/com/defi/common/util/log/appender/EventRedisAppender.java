package com.defi.common.util.log.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.defi.common.util.log.ErrorLogger;
import com.defi.common.util.redis.Redisson;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.stream.StreamAddArgs;

/**
 * A custom Logback appender that sends log events to a Redis stream.
 * <p>
 * This appender pushes the log message to a specified Redis stream using Redisson client.
 * It can be configured with a stream name and a field name.
 * </p>
 *
 * Example usage in logback.xml:
 * <pre>{@code
 * <appender name="REDIS" class="com.defi.common.util.log.appender.EventRedisAppender">
 *     <streamName>log-stream</streamName>
 *     <fieldName>message</fieldName>
 * </appender>
 * }</pre>
 *
 * @author YourName
 */
@Slf4j
@Setter
public class EventRedisAppender extends AppenderBase<ILoggingEvent> {

    /**
     * The name of the Redis stream where log messages will be appended.
     */
    private String streamName;

    /**
     * The field name to use for storing log messages in the Redis stream.
     */
    private String fieldName;

    /**
     * Appends a log event to the Redis stream.
     *
     * @param eventObject the log event to be appended
     */
    @Override
    protected void append(ILoggingEvent eventObject) {
        try {
            RedissonClient client = Redisson.getInstance().getClient();
            RStream<String, String> stream = client.getStream(streamName);
            String message = eventObject.getFormattedMessage();
            stream.add(StreamAddArgs.entry(fieldName, message));
        } catch (Exception e) {
            ErrorLogger.create(e).log();
        }
    }
}
