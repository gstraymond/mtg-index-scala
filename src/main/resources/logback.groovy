import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import static ch.qos.logback.classic.Level.DEBUG

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%boldWhite(%d{HH:mm:ss.SSS}) %highlight(%-5level) %boldGreen(%logger{15}) - %msg%n"
    }
}

root(DEBUG, ["STDOUT"])