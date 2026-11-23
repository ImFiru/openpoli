---
name: Penpot Design
description: Inspect, export, and modify Penpot design files through the project's Penpot MCP server (high_level_overview, penpot_api_info, export_shape, execute_code)
---

# Penpot Design

Use this skill when a task involves a live Penpot design file: reading pages,
layers, components or tokens; measuring a layout; exporting a shape to look at
it; or creating and changing design content.

## Tools

| Tool | Use |
| --- | --- |
| `high_level_overview` | Orientation. Read this before writing any plugin code. |
| `penpot_api_info` | Look up the exact `penpot` API surface before calling it. |
| `export_shape` | Render a shape, the selection, or the whole page to PNG/SVG to check a result. |
| `execute_code` | Run JavaScript against the `penpot` API inside the connected plugin. |

## Workflow

1. Call `high_level_overview` first in any session that touches a design file.
2. Confirm the exact API you need with `penpot_api_info` instead of guessing
   method names or object shapes.
3. Do read-only inspection first, and report what you found before mutating.
4. When writing code for `execute_code`:
   - The code runs as a function body, so `return` what you need back.
   - No need to `JSON.stringify`; return plain JS values.
   - Put anything reusable in the provided `storage` object so later calls can
     read it, including functions.
   - Do not log data you are already returning: it comes back twice.
5. Verify visually with `export_shape` after any layout or styling change.

## Rules

- Ask before deleting or overwriting user content; prefer additive edits.
- Do not touch pages, components, or layers outside the requested scope.
- The connection requires the Penpot MCP plugin to be open and connected in the
  browser tab holding the file. If calls fail, tell the user to open the plugin
  and click "Connect to MCP server" rather than retrying blindly.
