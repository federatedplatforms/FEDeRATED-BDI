# Changelog

All notable changes to this project will be documented in this file. The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## TODO / Wishlist

Documentation:

- How to use the Webhooks API
  - Add/edit/delete
  - Structure of the webhook notification message
- How to configure event types and use the for new events
  - How to add/edit/delete new event types
  - How to use the Event-Type and Event-Destination headers
- How to use the Data Pull flow
  - Structure of the request
  - Configuring a Data Fetcher
- How to enable API security
  - What properties
  - How to extend

Implementation:

- Store distribution rule configuration in the database
- Implement paging and sorting for the event viewer UI
- Implement viewing a single events details in the event viewer UI
- We are currently using Corda 4.9 but the next major release is Corda 5.x, consider upgrading.

Ideas worth investigating:

- It might be useful if events would include links to relevant data that a sender of an event is able to provide. 
  Similar to what HATEOS is for RESTful services or Hydra for JSON-LD https://www.markus-lanthaler.com/hydra/spec/latest/core/
  This way a data pull can be initiated based on links in an event, without having to figure out what additional information a party, 
  sending the initial event, has to offer.


## [Unreleased]

## [0.4]

- Add an API to add/update/delete event types
- Store the event types in the database
- Add h2 database for storing the Webhooks so that Webhooks can survive a restart now.
- Add polling mechanism for the Corda vault which retrieves all the (new) events and publishes them to the registered webhooks.
- Add Spring Security and configured Basic Ath for to the /api/** endpoints.
- Add event viewer UI which shows all the Events and Data Pulls that this node participated in.
- Add /sparql endpoint for querying the local triple store (graphdb).
- Update documentation.

## [0.3] - 2023-10-04

### Added

- Add introspection endpoints like `/event-types` and `/distribution-rules`
- Made distribution rules configurable
- Made event types and rml + shacl configurable for a node
- Added SHACL validation for events
- Added a generic `/events` endpoint instead of specific APIs for individual event types

### Changed

- Removed all specific APIs

## [Released]

## [0.2] - 2022-12-20

### Added

- iSHARE specific Corda flows
- iSHARE module containing all the iSHARE specific logic
- Kotlin based version of the semantic adapter that was previously written in Python.

### Changed

- Restructure the project into api, corda and semantic-adapter components.
- Cleaned up all unused code.
- Rewrote gradle build scripts.
- Moved all graphdb related calls to corda flows.

## [0.1] - 2022-11-05

### Added

- New Event flow
- Data Pull flow

