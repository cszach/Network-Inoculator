Stop the Contagion
==================

A graphical program that constructs and displays a network graph from a given
input in a file, and inoculates that network by finding and isolating the nodes
with the highest degree or collective influence.

When run in GUI mode, inoculation is done step-by-step by clicking the Isolate
button. Options to reset to x1 zoom and to default view are available. The user
can interact with the graph visuals by dragging the mouse to drag the graph or
scrolling the mouse wheel to adjust zoom level. Nodes that are connected
together regardless of depth are colored using the same color. Color groups are
consistent even after an isolation.

When run in CLI mode, inoculation is done automatically. GUI mode is chosen by
default. To select CLI mode, invoke `--no-gui`.

In either mode, output to stdout is given to indicate the node that has just
been isolated and its respective degree or collective influence, depending on
which method was selected.

Compilation
-----------

```
javac StopContagion.java
```

Arguments
---------

```
java StopContagion [--no-gui] [-d] [-r RADIUS] [-t] NUM_NODES FILE
```
where:
- `--no-gui` specifies that the program only runs in command line mode;
- `-d` specifies that inoculation should be done by finding the node with the
  highest degree instead of collective influence;
- `-r RADIUS` specifies the radius to effectively calculate collective influence
  if inoculation is done using collective influence (default to 2 if not
  specified);
- `-t` specifies that after each isolation, a list of connected nodes shall be
  printed to stdout;
- `NUM_NODES` is the number of nodes to isolate during the inoculation process;
- `FILE` is the name of the file that contains connection information of nodes
  in the input network.

Examples
--------

- Initialize visualizer that inoculates 4 nodes by collective influence (radius
  = 2):
```
java StopContagion 4 test1.txt
```
- Inoculate 6 nodes by degree in a network in CLI mode, printing the nodes with
  the highest degrees and their respective degrees after each isolation:
```
java StopContagion -d --no-gui 6 test2.txt
```

Structure
---------

- `NetworkGraph.java`: contains the implementation of undirected & unweighted
  graphs and operations necessary for the rest of the program to be functional,
  including methods to dis/connect nodes, mark nodes as un/visited, get
  connected nodes, get shortest paths to every node from a node using Dijkstra's
  algorithm, get the degree/collective influence of a node, isolate a node, and
  inoculate the network.
- `NetworkGraphCanvas.java`: extends `JPanel` to mainly implement the paint
  method used to illustrate the network graph in a circular layout.
- `StopContagion.java`: acts as the main piece of the program that takes
  everything together, including setting up a window frame and the buttons,
  reading input from file, and initializing a network graph and a network graph
  canvas.
- `test1.txt`, `test2.txt`: example tests.

Algorithm
---------

We generate an adjacency matrix based on inputs read from the input file. The
adjacency matrix is a 2D array of booleans since the graph is unweighted.

To determine the degree of a node, we run through its corresponding row in the
adjacency matrix and count the number of elements in the row that is `true`.

To determine the collective influence of a node, we first determine the shortest
path between the node and every other node using Dijkstra's algorithm, and use
it to compare against the given radius. Once we can determine which nodes are on
the radius (shortest path = radius), we can compute the collective influence.
