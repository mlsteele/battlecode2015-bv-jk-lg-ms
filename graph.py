"""Graph some analysis.
Usage:
  graph.py <quantity> <logfile>...
  graph.py <quantity>
  graph.py (-h | --help)

Examples:
  graph.py team_ore locallogs/latest.log

Options:
  -h, --help
"""

import re
import os
import glob
from collections import defaultdict
from docopt import docopt
import matplotlib
# matplotlib.use('Agg')
import matplotlib.pyplot as plt

DEFAULT_LOG_DIR = "locallogs"

def parselogfile(filepath):
    with open(filepath) as f:
        lines = f.read().splitlines()
    log = [x for x in map(decompose_logline, lines) if x != None]
    return log

def decompose_logline(line):
    # example log line:
    #   "     [java] [B:HQ#11689@26] ANALYZE team ore 430.0"
    p = "     \[java\] \[(?P<team>\w):(?P<robot>\w+)#(?P<rid>\d+)@(?P<round>\d+)\] ANALYZE (?P<msg>.*)"
    m = re.search(p, line)
    if m == None:
        return

    entry = {}
    entry.update(m.groupdict())
    decompose_logmsg(entry)
    entry['line'] = line
    entry['round'] = int(entry['round'])
    return entry

def decompose_logmsg(entry):
    "decompose just the user msg part of the logline"
    msg = entry['msg']
    entry.update(extract_msg(msg,
        "(?P<type>\w+) (?P<field>\w+) (?P<value>.*)"))

def extract_msg(msg, pattern):
    m = re.search(pattern, msg)
    if m != None:
        return m.groupdict()
    else:
        return {}

def validate(log, field):
    """ print warning messages about corrupt logs """
    # check for multiple samples in a round
    rounds_covered = defaultdict(lambda: [])
    for entry in log:
        if not (entry['field'] == field and entry['type'] == 'sample'):
            continue
        rounds_covered[entry['round']].append(entry)
        if len(rounds_covered[entry['round']]) > 1:
            print "WARNING: duplicate entry for sample {} on round {}".format(field, entry['round'])
            print rounds_covered[entry['round']]

def plot(log, field, label, round_max=float('inf')):
    xs = []
    ys = []
    for entry in log:
        if entry['field'] == field and entry['round'] <= round_max:
            xs.append(entry['round'])
            ys.append(entry['value'])
    plt.plot(xs, ys, label=label)
    plt.legend()
    plt.xlabel("Round")
    plt.ylabel(field)

def commit():
    """ Save and show the figure. """
    fig = plt.gcf()
    fig.savefig("localgraphs/latest.png")
    print "Graph saved to localgraphs/latest.png"
    plt.show()


if __name__ == "__main__":
    arguments = docopt(__doc__)
    field = arguments['<quantity>']
    logfiles = arguments['<logfile>']
    if not logfiles:
        logfiles = glob.glob(os.path.join(DEFAULT_LOG_DIR, "*.log"))

    ROUND_MAX = 2000
    for logfile in logfiles:
        log = parselogfile(logfile)
        validate(log, field)
        label = os.path.splitext(logfile.split("/")[-1])[0]
        print label
        plot(log, field, label, round_max=ROUND_MAX)

    commit()

    # log = parselogfile(DATAFILE)
    # plot(log, 'miners_alive', round_max=ROUND_MAX)
    # # plot(log, 'team_ore', round_max=ROUND_MAX)
    # commit()
