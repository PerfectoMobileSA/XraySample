@Web
Feature: Google Search

  @WebSearch @PERFECTO-1
  Scenario: Google Search Test
    Given I am on Google Search Page
    When I search for "quantum perfecto"
    Then it should have "Introducing Quantum Framework" in search results
    Then I am on Google Search Page

	@WebSearch @PERFECTO-2
  Scenario: Google Search Results Test
    Given I am on Google Search Page
    When I search for "quantum perfecto"
    Then it should have "Introducinggggggg Quantum Framework" in search results
    Then I am on Google Search Page
    
  