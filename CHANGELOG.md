# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0]

### Added

The biggest addition is that now roster.jar is not just a tool to add teams or students, but a tool to synchronize a github organization teams with the current state of the groups in the CSV file. This means:
- Now deletes teams that are no longer needed, in addition to creating teams.
- Now removes students from teams that are no longer needed, in addition to adding students to teams.

Other additions include:
- Added the `--clean` option to remove all teams created from groups (to prepare for a new year).
- Updated README.md to include instructions to get the required GitHub token.

### Removed

- The prefix is no longer configurable.

## [1.1.0]

### Added

- Added the "-p" option to specify a prefix for team names.
- Added this CHANGELOG file to document changes.
- The organization can now be specified in the .env file.

### Changed

- Updated the README with clearer instructions and examples.
- The JAR file is now called `teams.jar` instead of `create_teams.jar`.
- Added repository URL to the help message.

### Fixed

- Minor fixes.


## [1.0.0]


### Added

- Functionality to read a CSV file with student identifiers and GitHub usernames, and create GitHub teams based on the group ID in the student identifier.
- Adds students to their corresponding teams.
