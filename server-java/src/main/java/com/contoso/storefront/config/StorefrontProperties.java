package com.contoso.storefront.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings bound from {@code storefront.*} in application.properties.
 *
 * @param seedPath path to the shared seed JSON file
 * @param taxRate sales tax rate, e.g. 0.08
 * @param logFile path of the application log file
 */
@ConfigurationProperties(prefix = "storefront")
public record StorefrontProperties(String seedPath, BigDecimal taxRate, String logFile) {}
