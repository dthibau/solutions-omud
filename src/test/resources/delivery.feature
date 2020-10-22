Feature: All deliveries can be retrieved
  Scenario: client makes call to GET /api/livraison
    When the client calls /api/livraison
    Then the client receives status code of 200
