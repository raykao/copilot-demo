package com.contoso.storefront.loyalty;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LoyaltyControllerTest {

  @Autowired private MockMvc mockMvc;

  @Nested
  class Balance {

    @Test
    void givenExistingCustomer_whenBalanceRequested_thenSeededPointsAreReturned()
        throws Exception {
      mockMvc
          .perform(get("/api/customers/C-100/loyalty"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.customerId").value("C-100"))
          .andExpect(jsonPath("$.points").value(120));
    }

    @Test
    void givenUnknownCustomer_whenBalanceRequested_thenNotFound() throws Exception {
      mockMvc
          .perform(get("/api/customers/C-999/loyalty"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }
  }

  @Nested
  class Redeem {

    @Test
    void givenEnoughPoints_whenRedeemed_thenRemainingBalanceIsReturned() throws Exception {
      mockMvc
          .perform(redeem("C-102", "{\"points\":55}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.redeemed").value(55))
          .andExpect(jsonPath("$.points").value(400));
    }

    @Test
    void givenTooFewPoints_whenRedeemed_thenInsufficientPoints() throws Exception {
      mockMvc
          .perform(redeem("C-101", "{\"points\":10}"))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.error.code").value("INSUFFICIENT_POINTS"));
    }

    @Test
    void givenZeroPoints_whenRedeemed_thenValidationError() throws Exception {
      mockMvc
          .perform(redeem("C-100", "{\"points\":0}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
  }

  @Nested
  class Earn {

    @Test
    void givenCustomerOrder_whenPlaced_thenPointsAreCredited() throws Exception {
      mockMvc
          .perform(
              post("/api/orders")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      "{\"items\":[{\"sku\":\"SKU-1001\",\"quantity\":1}],\"customerId\":\"C-101\"}"))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.pointsEarned").value(97));

      mockMvc
          .perform(get("/api/customers/C-101/loyalty"))
          .andExpect(jsonPath("$.points").value(97));
    }

    @Test
    void givenGuestOrder_whenPlaced_thenNoPointsAreEarned() throws Exception {
      mockMvc
          .perform(
              post("/api/orders")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"items\":[{\"sku\":\"SKU-1001\",\"quantity\":1}]}"))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.pointsEarned").value(0));
    }
  }

  private static org.springframework.test.web.servlet.RequestBuilder redeem(
      String customerId, String body) {
    return post("/api/customers/" + customerId + "/loyalty/redeem")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body);
  }
}
