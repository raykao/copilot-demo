package com.contoso.storefront.discount;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectDirectories;
import org.junit.platform.suite.api.Suite;

/** Runs the shared Gherkin feature from ../specs through Cucumber. */
@Suite
@IncludeEngines("cucumber")
@SelectDirectories("../specs/discount-eligibility")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.contoso.storefront.discount")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
class DiscountEligibilityFeatureTest {}
