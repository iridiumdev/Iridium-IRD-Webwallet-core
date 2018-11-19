Feature: auth api - register & login

  Scenario: Register new user and login
    When I send a POST request to /auth/register with body:
      """
      {
          "username": "jdoe",
          "email": "jdoe@foobar.com",
          "password": "secr3tPw"
      }
      """
    Then the response should be 201
    # register with the same username should fail
    When I send a POST request to /auth/register with body:
      """
      {
          "username": "jdoe",
          "email": "jdoe@foobar.com",
          "password": "secr3tPw"
      }
      """
    Then the response should be 400 and match this json:
      """
      {
          "error": "registration failed"
      }
      """
    # register with short password should fail
    When I send a POST request to /auth/register with body:
      """
      {
          "username": "ndoe",
          "email": "ndoe@foobar.com",
          "password": "123"
      }
      """
    Then the response should be 400
    # login with previously registered user
    When I send a POST request to /auth/login with body:
      """
      {
          "username": "jdoe",
          "password": "secr3tPw"
      }
      """
    Then the response should be 200



  Scenario: Successful login
    When I send a POST request to /auth/login with body:
      """
      {
          "username": "testuser",
          "password": "secr3tPw"
      }
      """
    And I keep the JSON response at "access_token" as "access_token"
    And I keep the JSON response at "refresh_token" as "refresh_token"
    And I keep the JSON response at "expire" as "expire"
    Then the response should be 200 and match this json:
      """
      {
          "code": 200,
          "access_token": ${access_token},
          "refresh_token": ${refresh_token},
          "expire": ${expire}
      }
      """

  Scenario: Failed login
    When I send a POST request to /auth/login with body:
      """
      {
          "username": "testuser",
          "password": "dontknowthatpw"
      }
      """
    Then the response should be 401 and match this json:
      """
      {
          "error":"incorrect Username or Password"
      }
      """