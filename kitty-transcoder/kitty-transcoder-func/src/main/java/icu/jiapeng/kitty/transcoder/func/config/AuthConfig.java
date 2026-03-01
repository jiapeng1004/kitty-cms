package icu.jiapeng.kitty.transcoder.func.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthConfig {

    private Session session = new Session();
    private Signature signature = new Signature();

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    public static class Session {
        private long ttlSeconds = 86400;

        public long getTtlSeconds() {
            return ttlSeconds;
        }

        public void setTtlSeconds(long ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
        }
    }

    public static class Signature {
        private int timestampDriftSeconds = 300;

        public int getTimestampDriftSeconds() {
            return timestampDriftSeconds;
        }

        public void setTimestampDriftSeconds(int timestampDriftSeconds) {
            this.timestampDriftSeconds = timestampDriftSeconds;
        }
    }
}
