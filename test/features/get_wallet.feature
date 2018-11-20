Feature: wallet api - get wallets

  Scenario: Get all wallets for current user
    Given I am logged in as "testuser"
    And I send a POST request to "/api/v1/wallets" with body:
      """
      {
          "name": "FooWallet",
          "password": "s3cr3tpa$$"
      }
      """
    And the response should be 201
    And I send a POST request to "/api/v1/wallets" with body:
      """
      {
          "name": "BarWallet",
          "password": "s3cr3tpa$$"
      }
      """
    And the response should be 201
    When I send a GET request to "/api/v1/wallets"
    And I keep the JSON response at "0.id" as "id0"
    And I keep the JSON response at "0.address" as "address0"
    And I keep the JSON response at "1.id" as "id1"
    And I keep the JSON response at "1.address" as "address1"
    Then the response should be 200 and match this json:
      """
      [
        {
            "id": ${id0},
            "name": "FooWallet",
            "address": ${address0},
            "owner": "testuser"
        },
        {
            "id": ${id1},
            "name": "BarWallet",
            "address": ${address1},
            "owner": "testuser"
        }
      ]
      """