"""Standardize java headers.
Usage:
  rebuild_headers.py <filepath>
  rebuild_headers.py (-h | --help)

Examples:
  rebuild_headers.py team017

Options:
  -h, --help
"""

import os
from docopt import docopt

IMPORT_BLOCK = """
import java.util.*;
import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import team017.*;
import team017.radio.*;
import team017.structures.*;
import team017.units.*;
import static team017.Strategy.*;
"""

def java_body(lines):
    """ Strip package and imports. """
    for index, line in enumerate(lines):
        in_header = (line == "") or line.startswith("import") or line.startswith("package")
        if not in_header:
            return lines[index:]

if __name__ == "__main__":
    arguments = docopt(__doc__)
    filepath = arguments['<filepath>']

    package = os.path.split(filepath)[0].replace("/", ".")

    with open(filepath, 'r') as f:
        lines = [l.rstrip("\n") for l in f.readlines()]

    lines = java_body(lines)

    pkgline = "package {};".format(package)
    contents = "{}\n{}\n{}".format(
        pkgline,
        IMPORT_BLOCK,
        "\n".join(lines)).rstrip("\n") + "\n"

    with open(filepath, 'w') as f:
        f.write(contents)
