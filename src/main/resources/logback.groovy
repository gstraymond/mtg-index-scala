import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import static ch.qos.logback.classic.Level.INFO

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%white(%d{HH:mm:ss.SSS}) %highlight(%-5level) %green(%logger{15}) - %msg%n"
    }
}

root(INFO, ["STDOUT"])