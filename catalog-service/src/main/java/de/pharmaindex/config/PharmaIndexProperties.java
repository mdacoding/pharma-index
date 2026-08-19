package de.pharmaindex.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pharma-index")
public class PharmaIndexProperties {

    private final Security security = new Security();
    private final Matching matching = new Matching();
    private final Quality quality = new Quality();
    private final Ratelimit ratelimit = new Ratelimit();

    public Security getSecurity() {
        return security;
    }

    public Matching getMatching() {
        return matching;
    }

    public Quality getQuality() {
        return quality;
    }

    public Ratelimit getRatelimit() {
        return ratelimit;
    }

    public static class Security {
        private String demoApiKey = "demo-partner-key";

        public String getDemoApiKey() {
            return demoApiKey;
        }

        public void setDemoApiKey(String demoApiKey) {
            this.demoApiKey = demoApiKey;
        }
    }

    public static class Matching {
        private double minScore = 0.42;
        private int maxCandidates = 8;

        public double getMinScore() {
            return minScore;
        }

        public void setMinScore(double minScore) {
            this.minScore = minScore;
        }

        public int getMaxCandidates() {
            return maxCandidates;
        }

        public void setMaxCandidates(int maxCandidates) {
            this.maxCandidates = maxCandidates;
        }
    }

    public static class Quality {
        private double priceWarningEur = 250;

        public double getPriceWarningEur() {
            return priceWarningEur;
        }

        public void setPriceWarningEur(double priceWarningEur) {
            this.priceWarningEur = priceWarningEur;
        }
    }

    public static class Ratelimit {
        private int requestsPerMinute = 120;

        public int getRequestsPerMinute() {
            return requestsPerMinute;
        }

        public void setRequestsPerMinute(int requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }
    }
}
