import re
import matplotlib
# matplotlib.use('Agg')
import matplotlib.pyplot as plt

DATAFILE = "match-analyze.log"

def parselog(filepath):
    with open(DATAFILE) as f:
        lines = f.read().splitlines()
    return map(decompose_logline, lines)

def decompose_logline(line):
    # example log line:
    #   "     [java] [B:HQ#11689@26] ANALYZE team ore 430.0"
    p = "     \[java\] \[(?P<team>\w):(?P<robot>\w+)#(?P<rid>\d+)@(?P<round>\d+)\] ANALYZE (?P<msg>.*)"
    m = re.search(p, line)
    if m == None:
        raise RuntimeError("Couldn't match log line: " + line)

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
        "count miners_alive (?P<miners_alive>.*)"))

def extract_msg(msg, pattern):
    m = re.search(pattern, msg)
    if m != None:
        return m.groupdict()
    else:
        return {}

def plot(log, field, round_max=float('inf')):
    xs = []
    ys = []
    for entry in log:
        if field in entry and entry['round'] <= round_max:
            xs.append(entry['round'])
            ys.append(entry[field])
    plt.plot(xs, ys)
    plt.xlabel("Round")
    plt.ylabel(field)

def commit():
    fig = plt.gcf()
    fig.savefig("localgraphs/latest.png")
    print "Graph saved to localgraphs/latest.png"
    plt.show()

log = parselog(DATAFILE)
ROUND_MAX = 800
plot(log, 'miners_alive', round_max=ROUND_MAX)
# plot(log, 'team_ore', round_max=ROUND_MAX)
commit()
