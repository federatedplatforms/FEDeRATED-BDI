# Changelog
All notable changes to this project will be documented in this file. The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [Released]

## [0.2] - 2022-12-21

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
