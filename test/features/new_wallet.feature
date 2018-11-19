Feature: wallet api - create/import wallets

  Scenario: Create a new wallet
    Given I am logged in as "testuser"
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

  @ignore
  Scenario: Import a wallet from keys
    When I send a POST request to /api/v1/wallets with body:
      """
      {
          "name": "Test Wallet ir2ku...",
          "password": "s3cr3t",
          "viewSecretKey": "a950d88d6b10c04805e70f876418209bf16e528f182ba776b5b276562ec5db05",
          "spendSecretKey": "78b4a1e37d40b84a0ae96c60597b0638639e20058f59b5dbc74929294c712002"
      }
      """
    And I keep the JSON response at "id" as "id"
    Then the response should be 201 and match this json:
      """
      {
          "id": ${id},
          "name": "Test Wallet ir2ku...",
          "address": "ir2ku6Rgh69WqEfzAnQfBLTSsoYW17bEJbPUptFedjzG6yWu3o4mNNC23zyGS74KWQ92XhLXhm9uTUhrSPbTc5zK1QGSA63rz"
      }
      """