Feature: wallet api - get wallets

  Scenario: Get all wallets for current user
    Given I am logged in as "testuser"
    And I create a test wallet with name "FooWallet" and password "s3cr3tpa$$"
    And I create a test wallet with name "BarWallet" and password "s3cr3tpa$$"
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
            "owner": ${testuser.id},
            "status": "RUNNING"
        },
        {
            "id": ${id1},
            "name": "BarWallet",
            "address": ${address1},
            "owner": ${testuser.id},
            "status": "RUNNING"
        }
      ]
      """