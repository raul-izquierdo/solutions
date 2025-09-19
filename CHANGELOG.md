# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [1.2.0]

### Changed
- Improved user prompts in automatic selection mode.

## [1.1.0]

### Added

- Error messages now include the organization name to help identify issues when working with the wrong organization.
- The schedule file name now defaults to `schedule.csv` instead of requiring it to be specified with `-s`.

### Changed

- Groups in `schedule.csv` are no longer required to exist as teams, allowing use of the full `schedule.csv` even when some groups don't have their teams created yet.

### Fixed

- When there are no teams or solutions, the app now warns and stops instead of showing a confusing error message.

## [1.0.0]


### Added

- Automatic granting of access to solutions based on a schedule.
- Manual selection of group and solution when automatic detection is not possible.
