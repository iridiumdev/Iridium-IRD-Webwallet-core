Feature: wallet api - create

  Scenario: Create a new wallet
    When I send a POST request to /api/v1/wallets with body:
      """
      {
          "name": "FooWallet",
          "password": "s3cr3t"
      }
      """
    And I keep the JSON response at "id" as "id"
    And I keep the JSON response at "address" as "address"
    Then the response should be 201 and match this json:
      """
      {
          "id": ${id},
          "name": "FooWallet",
          "address": ${address}
      }
      """
