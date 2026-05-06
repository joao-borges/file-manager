# Oneshot CLI Mode

The application can be invoked from the command line to run a single operation
and exit, instead of starting the embedded web server. This is intended for
scripting (cron, CI, shell pipelines).

## Usage

Two arg forms are accepted (mutually exclusive):

```bash
# Inline JSON
java -jar file-manager.jar --oneshot='<json>'

# JSON read from a file (recommended for large payloads or to avoid shell quoting)
java -jar file-manager.jar --oneshot-file=/path/to/payload.json
```

In oneshot mode:

- The embedded web server, WebSocket endpoints, and CORS/rate-limiting filters
  are not started — no HTTP port is bound.
- The Spring banner and startup-info logs are suppressed.
- A JSON summary is written to **stdout**.
- The JVM exits with code **0** on success, **1** on any error (parse failure,
  validation failure, operation failure).
- Application logs go to **stderr** at the level configured in `application.yml`.

## Request envelope

Every payload has the same top-level shape:

```json
{
  "operation": "<operation-id>",
  "params": { ... operation-specific fields ... }
}
```

| Field       | Type   | Required | Notes                                                                 |
| ----------- | ------ | -------- | --------------------------------------------------------------------- |
| `operation` | string | yes      | One of `rename`, `organize`, `extract`, `photo-organize`, `find-duplicates` |
| `params`    | object | yes      | Operation-specific. Field names match the REST DTOs.                  |

The `params` object is bound to the same DTO classes used by the REST API and
is validated with the same Jakarta Validation rules — paths must be non-blank
and may not contain `<>:"|?*` or control characters.

## Operations

### `rename`

Renames files in a directory using the configured patterns and exclusion rules.

| Param                   | Type    | Required | Default | Description                            |
| ----------------------- | ------- | -------- | ------- | -------------------------------------- |
| `sourceDirectory`       | string  | yes      |         | Directory containing files to rename.  |
| `includeSubDirectories` | boolean | no       | `false` | Recurse into subdirectories.           |

Example:
```json
{
  "operation": "rename",
  "params": {
    "sourceDirectory": "/Users/me/Downloads",
    "includeSubDirectories": true
  }
}
```

### `organize`

Moves files from a source directory into category folders (by extension) under
a destination directory.

| Param                  | Type   | Required | Description                       |
| ---------------------- | ------ | -------- | --------------------------------- |
| `sourceDirectory`      | string | yes      | Directory whose files to organize.|
| `destinationDirectory` | string | yes      | Where category folders are created.|

Example:
```json
{
  "operation": "organize",
  "params": {
    "sourceDirectory": "/data/inbox",
    "destinationDirectory": "/data/sorted"
  }
}
```

### `extract`

Recursively walks `sourceDirectory` and moves all matching files up into
`destinationDirectory` (flattening nested subdirectories).

| Param                  | Type   | Required | Description                       |
| ---------------------- | ------ | -------- | --------------------------------- |
| `sourceDirectory`      | string | yes      | Directory to walk.                |
| `destinationDirectory` | string | yes      | Flat directory to extract into.   |

Example:
```json
{
  "operation": "extract",
  "params": {
    "sourceDirectory": "/data/nested",
    "destinationDirectory": "/data/flat"
  }
}
```

### `photo-organize`

Reads EXIF metadata from photos/videos under `sourceDirectory` and moves them
into date-based folders under `destinationDirectory`. Accepted extensions:
`jpg`, `jpeg`, `png`, `bmp`, `mov`, `mp4`, `avi`, `wmv`, `mpeg`, `mpg`.

| Param                  | Type   | Required | Description                          |
| ---------------------- | ------ | -------- | ------------------------------------ |
| `sourceDirectory`      | string | yes      | Directory containing photos/videos.  |
| `destinationDirectory` | string | yes      | Where date folders are created.      |

Example:
```json
{
  "operation": "photo-organize",
  "params": {
    "sourceDirectory": "/Volumes/Camera/import",
    "destinationDirectory": "/Volumes/Photos"
  }
}
```

### `find-duplicates`

Finds and removes duplicate files in `directory` based on the MD5 hashes
listed in `md5sumfiles.txt` inside the same directory.

| Param       | Type   | Required | Description                     |
| ----------- | ------ | -------- | ------------------------------- |
| `directory` | string | yes      | Directory to scan for duplicates.|

Example:
```json
{
  "operation": "find-duplicates",
  "params": {
    "directory": "/data/photos"
  }
}
```

## Response envelope

### Success

```json
{
  "success": true,
  "operation": "organize",
  "summary": {
    "source": "/data/inbox",
    "destination": "/data/sorted",
    "filesMoved": 42
  },
  "result": { ... full operation result object ... }
}
```

`summary` carries the headline numbers per operation:

| Operation         | Summary fields                                                       |
| ----------------- | -------------------------------------------------------------------- |
| `rename`          | `source`, `includeSubDirectories`, `filesRenamed`, `duplicates`      |
| `organize`        | `source`, `destination`, `filesMoved`                                |
| `extract`         | `source`, `destination`, `filesMoved`                                |
| `photo-organize`  | `source`, `destination`, `filesMoved`                                |
| `find-duplicates` | `directory`, `duplicatesRemoved`                                     |

`result` is the raw operation result object (paths of moved/renamed files,
etc.) and is best-effort serialized — its exact shape depends on the
operation class.

### Error

```json
{
  "success": false,
  "message": "Validation failed: sourceDirectory: Source directory cannot be empty",
  "error": "ConstraintViolationException"
}
```

The `error` field (the exception class name) is only included for unexpected
errors; validation and CLI-argument errors omit it.

## Examples

```bash
# Inline, organize files
java -jar target/file-manager-*.jar \
  --oneshot='{"operation":"organize","params":{"sourceDirectory":"/in","destinationDirectory":"/out"}}'

# From file
cat > /tmp/rename.json <<'JSON'
{
  "operation": "rename",
  "params": {
    "sourceDirectory": "/Users/me/Downloads",
    "includeSubDirectories": false
  }
}
JSON
java -jar target/file-manager-*.jar --oneshot-file=/tmp/rename.json

# Pipe stdout into jq
java -jar target/file-manager-*.jar --oneshot-file=/tmp/rename.json \
  | jq '.summary.filesRenamed'
```

## Notes & limitations

- `--oneshot` and `--oneshot-file` are mutually exclusive — passing both is an error.
- Paths are subject to the `filemanager.allowed-paths` configuration in
  `application.yml`; running outside those roots will fail with a security error.
- The same DTO validation rules (`@NotBlank`, `@Pattern`) apply as for the REST API.
