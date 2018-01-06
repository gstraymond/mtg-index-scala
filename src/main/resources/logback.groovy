import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import fr.gstraymond.constant.Conf

def logPattern = "%d{yyyy/MM/dd HH:mm:ss,SSS} [%thread] %-5level %logger{36} - %m%n"
if (Conf.coloredLogs()) {
    logPattern = "%white(%d{HH:mm:ss.SSS}) %highlight(%-5level) %green(%logger{15}) - %msg%n"
}

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = logPattern
    }
}

logger 'com.ning', WARN
logger 'io.netty', INFO
logger 'org.asynchttpclient.netty', INFO

root(DEBUG, ["STDOUT"])
