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

  Scenario: Get a single running wallet with details for current user
    Given I am logged in as "testuser"
    And I create a test wallet with name "testwallet1" and password "s3cr3tpa$$"
    When I send a GET request to "/api/v1/wallets/${testwallet1.id}"
    And I keep the JSON response at "id" as "id"
    And I keep the JSON response at "address" as "address"
    And I keep the JSON response at "blockHeight.top" as "blockHeightTop"
    And I keep the JSON response at "blockHeight.current" as "blockHeightCurrent"
    And I keep the JSON response at "peerCount" as "peerCount"
    Then the response should be 200 and match this json:
      """
      {
          "id": ${id},
          "name": "testwallet1",
          "address": ${address},
          "owner": ${testuser.id},
          "status": "RUNNING",
          "balance": {
            "total": 0,
            "locked": 0
          },
          "blockHeight": {
            "current": ${blockHeightCurrent},
            "top": ${blockHeightTop}
          },
          "peerCount": ${peerCount}
      }
      """

  Scenario: Start/Stop a wallet
    Given I am logged in as "testuser"
    And I create a test wallet with name "testwallet1" and password "s3cr3tpa$$"
    When I send a DELETE request to "/api/v1/wallets/${testwallet1.id}/instance"
    And I keep the JSON response at "id" as "id"
    And I keep the JSON response at "address" as "address"
    Then the response should be 200 and match this json:
      """
      {
            "id": ${id},
            "name": "testwallet1",
            "address": ${address},
            "owner": ${testuser.id},
            "status": "STOPPED"
        }
      """
    When I send a POST request to "/api/v1/wallets/${testwallet1.id}/instance" with body:
      """
      {
          "password": "s3cr3tpa$$"
      }
      """
    And I keep the JSON response at "id" as "id"
    And I keep the JSON response at "address" as "address"
    And I keep the JSON response at "blockHeight.current" as "bHeightCurrent"
    And I keep the JSON response at "blockHeight.top" as "bHeightTop"
    And I keep the JSON response at "peerCount" as "peerCount"
    Then the response should be 201 and match this json:
      """
        {
            "id": ${id},
            "name": "testwallet1",
            "address": ${address},
            "owner": ${testuser.id},
            "status": "RUNNING",
            "balance": {
              "total": 0,
              "locked": 0
            },
            "blockHeight": {
              "current": ${bHeightCurrent},
              "top": ${bHeightTop}
            },
            "peerCount": ${peerCount}
        }
      """


  Scenario: Start a already RUNNING wallet leads to an error
    Given I am logged in as "testuser"
    And I create a test wallet with name "testwallet1" and password "s3cr3tpa$$"
    When I send a POST request to "/api/v1/wallets/${testwallet1.id}/instance" with body:
      """
      {
        "password": "s3cr3tpa$$"
      }
      """
    Then the response should be 400 and match this json:
      """
      {
        "error":"wallet already running"
      }
      """
    When I send a DELETE request to "/api/v1/wallets/${testwallet1.id}/instance"
    Then the response should be 200
    When I send a DELETE request to "/api/v1/wallets/${testwallet1.id}/instance"
    Then the response should be 200

