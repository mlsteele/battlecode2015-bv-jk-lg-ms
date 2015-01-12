"""Graph some analysis.
Usage:
  graph.py <quantity> <logfile>...
  graph.py <quantity>
  graph.py --list <logfile>
  graph.py (-h | --help)

Examples:
  graph.py team_ore locallogs/latest.log

Options:
  -h, --help
"""

import re
import os
import sys
import glob
from collections import defaultdict, namedtuple
from pprint import pprint
from docopt import docopt
import matplotlib
# matplotlib.use('Agg')
import matplotlib.pyplot as plt

DEFAULT_LOG_DIR = "locallogs"

Log = namedtuple('Log', ['entries', 'types'])

def parselogfile(filepath):
    with open(filepath) as f:
        lines = f.read().splitlines()
    entries = [x for x in map(decompose_logline, lines) if x != None]
    types = infer_types(entries)
    return Log(entries, types)

def infer_types(entries):
    types = {}
    for entry in entries:
        typ = entry['type']
        if entry['field'] not in types:
            types[entry['field']] = typ
        else:
            assert typ == types[entry['field']]
    return types

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

def validate_sample(log, field):
    """ print warning messages about corrupt logs """
    # check for multiple samples in a round
    rounds_covered = defaultdict(lambda: [])
    for entry in log.entries:
        if not (entry['field'] == field and entry['type'] == 'sample'):
            continue
        rounds_covered[entry['round']].append(entry)
        if len(rounds_covered[entry['round']]) > 1:
            print "WARNING: duplicate entry for sample {} on round {}".format(field, entry['round'])
            print rounds_covered[entry['round']]

def validate_aggregate(log, field):
    """ print warning messages about corrupt logs """
    # check for multiple aggregates per robot in a round
    rounds_covered = defaultdict(lambda: [])
    for entry in log.entries:
        if not (entry['field'] == field and entry['type'] == 'aggregate'):
            continue
        key = (entry['round'], entry['rid'])
        rounds_covered[key].append(entry)
        if len(rounds_covered[key]) > 1:
            print "WARNING: duplicate entry for aggregate {} on {}".format(field, key)
            print rounds_covered[key]

def plot_sample(log, field, label, round_max=float('inf')):
    xs = []
    ys = []
    for entry in log.entries:
        if entry['field'] == field and entry['round'] <= round_max:
            xs.append(entry['round'])
            ys.append(entry['value'])
    plt.plot(xs, ys, label=label, marker='.')

def plot_aggregate(log, field, label, round_max=float('inf')):
    aggregates = {}
    for entry in log.entries:
        if entry['field'] == field and entry['round'] <= round_max:
            aggregates[entry['round']] = aggregates.get(entry['round'], 0) + float(entry['value'])

    xs = []
    ys = []
    for round in range(0, round_max):
        if round in aggregates:
            xs.append(round)
            ys.append(aggregates[round])
    plt.plot(xs, ys, label=label)

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

    if arguments['--list']:
        log = parselogfile(logfiles[0])
        pprint(log.types)
        sys.exit(0)

    ROUND_MAX = 2000
    plt.xlim([0, ROUND_MAX])
    plt.xlabel("Round")
    plt.ylabel(field)
    for logfile in logfiles:
        log = parselogfile(logfile)
        label = os.path.splitext(logfile.split("/")[-1])[0]
        print label
        if not field in log.types:
            print "missing", field
            continue
        if log.types[field] == 'sample':
            validate_sample(log, field)
            plot_sample(log, field, label, round_max=ROUND_MAX)
        elif log.types[field] == 'aggregate':
            validate_aggregate(log, field)
            plot_aggregate(log, field, label, round_max=ROUND_MAX)
    plt.legend()

    commit()
