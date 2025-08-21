# Solutions

Automate showing or hiding solutions to GitHub Classroom assignments by granting or revoking a team’s read access to solution repositories (often with a single confirmation, thanks to automatic group and solution detection).

> NOTE: This application is part of a toolkit for managing classes with GitHub Classroom. It’s recommended to first read the [main repository](https://github.com/raul-izquierdo/classroom-tools) to get an overview of the project and understand where this tool fits.

## See it in action

The key feature is automatic group and solution detection:
- Detects the group that’s in session now by using the current time and the schedule CSV.
- Finds the appropriate solution repository for the current class.

This means you can walk into class, run the tool, press `y`, and the right solution is immediately visible to the right group.

Consider the following `schedule.csv` file:
```csv
G1,wednesday 08:00
G2,wednesday 09:00
G-english-1,wednesday 10:00
```

Then, running `solutions.jar` results in:
```bash
$ java -jar solutions.jar -s schedule.csv

Connecting with GitHub... done.

(now: 'wednesday' 08:15) Proceed to show 'factorial-solution' to group 'G1'? (y/N): y
Access granted.
```

When ambiguity exists, it falls back to an interactive picker:

```bash
Choose the group:
(type to filter or use arrows ↑/↓): G1
> G1
  G2
  G-english-1

Choose the solution:
(type to filter or use arrows ↑/↓):
  linked-list-solution [accessible]
> factorial-solution [hidden]

Grant access? (y/N): y
Access granted.
```

When a solution is selected manually, it simply flips its state (granting or revoking read access) without further prompts.

## Usage

```bash
java -jar solutions.jar [-s schedule.csv] [-o <organization>] [-t <token>]
```

| Option              | Description                                           |
| ------------------- | ----------------------------------------------------- |
| `-s <schedule.csv>` | CSV file with the group schedule (default: `schedule.csv`). |
| `-o <organization>` | GitHub organization name.                             |
| `-t <token>`        | GitHub API access token.                              |

If `-o` or `-t` are not provided, the app tries to read the `GITHUB_ORG` and `GITHUB_TOKEN` variables from a `.env` file in the working directory:
```dotenv
GITHUB_ORG=<your-org>
GITHUB_TOKEN=<token>
```

For more information, see [Obtaining the GitHub token](https://github.com/raul-izquierdo/classroom-tools#obtaining-the-github-token).

## Schedule file format

The CSV file must have one line per group with its schedule, following this format:

```csv
<GroupLabel>, <weekday>, <start-time>[, <duration>]
```

Columns:
- groupLabel: The name of the group. Any text is accepted.
- weekday: Day of the week — monday, tuesday, wednesday, thursday, friday, saturday, sunday.
- start-time: One of `H:mm`, `HH:mm`, or `H` (e.g., `8:15`, `09:30`, `8`). Must be between 08:00 and 21:00.
- duration (optional):
  - If omitted, defaults to 2 hours.
  - Accepts:
    - hours, with suffix `h` or no suffix: `2` or `2h`
    - minutes, with suffix `m`: `120m`

Notes:
- No header row is expected.
- If a group appears multiple times, the last row wins.

This example shows four groups, each with a duration of 2 hours:
```csv
G4, thursday, 12:00
G3, wednesday, 8:15, 2
G1, monday, 10:00, 2h
G2, tuesday, 09:30, 120m
```

> NOTE: At the moment, more than one timeslot per group (multiple classes per week) is not supported. It wouldn’t be hard to add; if requested, it can be considered.

## Naming rules

To let this tool identify which Teams correspond to practice groups and which repositories are solutions to assignments (since you may have teams and repositories for other purposes), it’s important to follow some naming conventions.

### Team names for groups

A team is considered a group when its display name starts with the prefix `group` followed by a space and the group name.

| Group | Team name       |
|------ | ----------------|
| 01    | group 01        |
| i02   | group i02       |
| lab1  | group lab1      |
| 01_english | group 01_english |

### Repository names for solutions

Requirements:
1. A repository is considered a solution when its name ends with the suffix `solution`.

    | Assignment name   | Solution repository name |
    |-------------------|--------------------------|
    | factorial         | factorial .solution      |
    | listas_enlazadas  | linked-list-solution     |
    | pila              | stack_solution           |
    | strategy pattern  | strategy pattern solution|

2. Additionally, to enable automatic selection of solutions, solution repositories should be named so that, when sorted by name, they match the order in which you want to show them during the course. A simple approach is to prefix the class number the solution belongs to, e.g., `01 factorial-solution`.

In summary, it’s recommended that:
- The repository name starts with the class number the solution belongs to.
- It ends with `solution`.

Examples of valid repository names for solutions: `01 factorial solution`, `02_listas_enlazadas_solution`, `03-pila-solution`, `04. strategy-pattern .solution` (note that the character separating the class number from the rest of the name is irrelevant).

## License

See `LICENSE`.
Copyright (c) 2025 Raul Izquierdo Castanedo
