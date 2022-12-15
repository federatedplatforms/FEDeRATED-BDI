# Testing

This document describes the test setup.

## Unit testing

The individual modules all contain a set of Unit tests. Unit tests have been written using JUnit. 
The tests are automatically being run when building the project with Gradle.

## Integration testing

[Corda workflows](../corda/workflows) module includes integration tests that use the Corda MockNetwork to verify if
Corda workflows are working as expected. 

## Gitlab CI pipeline

A Gitlab CI pipeline definition is included in the root of the project.