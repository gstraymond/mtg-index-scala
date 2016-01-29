import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import static ch.qos.logback.classic.Level.DEBUG

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%highlight(%-5level) %green(%logger{15}) - %white(%msg) %n"
    }
}

root(DEBUG, ["STDOUT"])