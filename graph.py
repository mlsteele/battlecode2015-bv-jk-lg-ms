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
from docopt import docopt
import matplotlib
# matplotlib.use('Agg')
import matplotlib.pyplot as plt

DEFAULT_LOG_DIR = "locallogs"

def parselog(filepath):
    with open(filepath) as f:
        lines = f.read().splitlines()
    return (x for x in map(decompose_logline, lines) if x != None)

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
        "team ore (?P<team_ore>.*)"))
    entry.update(extract_msg(msg,
        "count beavers (?P<beavers>.*)"))
    entry.update(extract_msg(msg,
        "count miners (?P<miners>.*)"))
    entry.update(extract_msg(msg,
        "count tanks (?P<tanks>.*)"))
    entry.update(extract_msg(msg,
        "count drones (?P<drones>.*)"))

def extract_msg(msg, pattern):
    m = re.search(pattern, msg)
    if m != None:
        return m.groupdict()
    else:
        return {}

def plot(log, field, label, round_max=float('inf')):
    xs = []
    ys = []
    for entry in log:
        if field in entry and entry['round'] <= round_max:
            xs.append(entry['round'])
            ys.append(entry[field])
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
        log = parselog(logfile)
        label = os.path.splitext(logfile.split("/")[-1])[0]
        print label
        plot(log, field, label, round_max=ROUND_MAX)

    commit()

    # log = parselog(DATAFILE)
    # plot(log, 'miners_alive', round_max=ROUND_MAX)
    # # plot(log, 'team_ore', round_max=ROUND_MAX)
    # commit()
