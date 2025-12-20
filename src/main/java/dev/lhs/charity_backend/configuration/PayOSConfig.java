package dev.lhs.charity_backend.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;
import vn.payos.core.ClientOptions;

@Configuration
public class PayOSConfig {

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

//    @Value("${payos.payout-client-id}")
//    private String payoutClientId;
//
//    @Value("${payos.payout-api-key}")
//    private String payoutApiKey;
//
//    @Value("${payos.payout-checksum-key}")
//    private String payoutChecksumKey;

//    @Value("${payos.log-level}")
//    private String logLevel;

    @Bean
    public PayOS payOS() {
        ClientOptions options = ClientOptions.builder()
                .clientId(clientId)
                .apiKey(apiKey)
                .checksumKey(checksumKey)
//                .logLevel(ClientOptions.LogLevel.valueOf(logLevel.toUpperCase()))
                .build();

        return new PayOS(options);
    }
//
//    @Bean
//    public PayOS payOSPayout() {
//        ClientOptions options = ClientOptions.builder()
//                .clientId(payoutClientId)
//                .apiKey(payoutApiKey)
//                .checksumKey(payoutChecksumKey)
//                .logLevel(ClientOptions.LogLevel.valueOf(logLevel.toUpperCase()))
//                .build();
//
//        return new PayOS(options);
//    }
}
