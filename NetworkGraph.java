import java.util.ArrayList;

/**
 * Data type for storing the information of the isolated node.
 *
 * <p>Serves the graphics program.
 *
 * @author Duong Nguyen
 */
class IsolationInfo {
  /** The node that is isolated. */
  int node;
  /** The influence of the node in either degree or collective influence. */
  int influence;
  /** The unit ("degree" or "collective influence"). */
  String unit;
  /** The nodes that were connected to the isolated node before the isolation. */
  ArrayList<Integer> connectedNodes;

  IsolationInfo(int node, int influence, String unit, ArrayList<Integer> connectedNodes) {
    this.node = node;
    this.influence = influence;
    this.unit = unit;
    this.connectedNodes = connectedNodes;
  }
}

/**
 * Data type for a network graph (undirected and unweighted).
 *
 * <p>Nodes are identified by a number (their index) and assumed to be indexed from 1.
 *
 * <p>We store the data into a (n + 1) * (n + 1) adjacency matrix where n is the number of nodes.
 * The first row (row 0) of the matrix is reserved to store the visit status of each node in our
 * program in particular, and it will come in handy in the graphics code. The real adjacency matrix
 * start from (1, 1) to (n + 1, n + 1). Value <code>true</code> corresponds to a connection between
 * the node identified by the row number and the node identified by the column number, while <code>
 * false</code> means disconnection. Since the graph is undirected, it is symmetric from (1, 1) to
 * (n + 1, n + 1) (the first row and first column are reserved for any data the programmer wishes to
 * store).
 *
 * @author Duong Nguyen
 * @author Ahmed Chaudhry
 */
class NetworkGraph {
  /** The adjacency matrix of the graph. */
  boolean[][] data;
  /**
   * The last node that was isolated.
   *
   * <p>Used to color the node in the graphics program.
   */
  int lastIsolatedNode;
  /**
   * The collective influences of every node in the network.
   *
   * @see computeCollectiveInfluences
   */
  int[] collectiveInfluences;

  /** @param n The number of vertices */
  NetworkGraph(int n) {
    // We plus one because we assume that nodes are indexed starting from 1.
    // Then we can simply pass the node's index e.g. data[1] for vertices
    // adjacent to node 1.
    data = new boolean[n + 1][n + 1];
  }

  /**
   * Connects two nodes.
   *
   * @param x
   * @param y
   */
  void connect(int x, int y) {
    data[x][y] = true;
    data[y][x] = true;
  }

  /**
   * Disconnects two nodes.
   *
   * @param x
   * @param y
   */
  void disconnect(int x, int y) {
    data[x][y] = false;
    data[y][x] = false;
  }

  /**
   * Returns whether two nodes are connected
   *
   * @param x
   * @param y
   * @return <code>true</code> if the two nodes are connected, otherwise <code>false</code>.
   */
  boolean isConnected(int x, int y) {
    return data[x][y];
  }

  /**
   * Checks if a node is visited.
   *
   * @param node
   */
  boolean isVisited(int node) {
    return data[0][node];
  }

  /**
   * Mark a node as visited.
   *
   * @param node
   */
  void markVisited(int node) {
    data[0][node] = true;
  }

  /**
   * Mark a node as unvisited.
   *
   * @param node
   */
  void markUnvisited(int node) {
    data[0][node] = false;
  }

  /**
   * Mark all nodes as unvisited.
   *
   * @see getConnectedNodes
   */
  void markAllUnvisited() {
    for (int i = 1; i < data.length; i++) markUnvisited(i);
  }

  /**
   * Returns the first node found to be unvisited.
   *
   * @return The first node that is found to be unvisited, <code>0</code> if all have been visited.
   */
  int getFirstUnvisitedNode() {
    for (int i = 1; i < data.length; i++) if (!isVisited(i)) return i;

    return 0;
  }

  /**
   * Returns the shortest path to every node from a given node using the Dijkstra algorithm.
   *
   * @param sourceNode
   */
  int[] getShortestPaths(int sourceNode) {
    int[] shortestPaths = new int[data.length];
    boolean[] isInShortestPathTree = new boolean[data.length];

    for (int i = 1; i < data.length; i++) {
      shortestPaths[i] = Integer.MAX_VALUE;
      isInShortestPathTree[i] = false;
    }

    shortestPaths[sourceNode] = 0;

    for (int i = 1; i < data.length - 1; i++) {
      // Get the min shortest distance

      int minDistance = Integer.MAX_VALUE;
      int minDistanceIndex = 0;

      for (int j = 1; j < data.length; j++)
        if (!isInShortestPathTree[j] && shortestPaths[j] <= minDistance) {
          minDistance = shortestPaths[j];
          minDistanceIndex = j;
        }

      isInShortestPathTree[minDistanceIndex] = true;

      // Relax

      for (int v = 1; v < data.length; v++)
        if (!isInShortestPathTree[v]
            && isConnected(minDistanceIndex, v)
            && shortestPaths[minDistanceIndex] != Integer.MAX_VALUE
            && shortestPaths[minDistanceIndex] + 1 < shortestPaths[v])
          shortestPaths[v] = shortestPaths[minDistanceIndex] + 1;
    }

    return shortestPaths;
  }

  /**
   * Writes the nodes connected to a given node in this network, regardless of depth into an <code>
   * ArrayList</code>.
   *
   * @param node The source node.
   * @param connectedNodes The <code>ArrayList</code> to write the results to.
   */
  void getConnectedNodes(int node, ArrayList<Integer> connectedNodes) {
    markVisited(node);
    connectedNodes.add(node);

    for (int i = 1; i < data.length; i++)
      if (data[node][i] && !isVisited(i)) this.getConnectedNodes(i, connectedNodes);
  }

  /**
   * Returns the nodes connected to a given node in this network, regardless of depth in an <code>
   * ArrayList</code>.
   *
   * @param node The source node.
   */
  ArrayList<Integer> getConnectedNodes(int node) {
    markAllUnvisited();

    ArrayList<Integer> connectedNodes = new ArrayList<Integer>();
    getConnectedNodes(node, connectedNodes);

    markAllUnvisited();
    return connectedNodes;
  }

  /**
   * Returns the degree of a node.
   *
   * @param node
   */
  int degree(int node) {
    int degree = 0;
    for (int i = 1; i < data.length; i++) if (isConnected(node, i)) degree++;
    return degree;
  }

  /**
   * Returns the collective influence of a node.
   *
   * @param node
   * @param radius
   * @return The collective influence of the given node in the given radius.
   */
  int collectiveInfluence(int node, int radius) {
    int k = degree(node) - 1;
    int sum = 0;
    int[] shortestPaths = getShortestPaths(node);

    for (int i = 1; i < shortestPaths.length; i++)
      if (shortestPaths[i] == radius) sum += degree(i) - 1;

    return k * sum;
  }

  /**
   * Returns the computed collective influence of a node.
   *
   * <p>This assumes that a call to <code>collectiveInfluence(int, int)</code> has been made.
   *
   * @param node
   * @return The computed collective influence of a node.
   */
  private int collectiveInfluence(int node) {
    return collectiveInfluences[node];
  }

  /** Computes the collective influence of every node. */
  void computeCollectiveInfluences(int radius) {
    collectiveInfluences = new int[data.length];
    for (int i = 1; i < data.length; i++) collectiveInfluences[i] = collectiveInfluence(i, radius);
  }

  /**
   * Isolates a specific node.
   *
   * <p>Isolates a node by disconnecting it with every other node.
   */
  void isolateNode(int node) {
    for (int i = 1; i < data.length; i++) disconnect(node, i);
  }

  /**
   * Isolates the node with the highest degree or the highest collective influence.
   *
   * @param useDegree If <code>true</code>, isolates the node with the highest degree. If <code>
   *     false</code>, use collective influence instead.
   * @param radius The radius to search within if collective influence is used.
   * @param trace If <code>true</code>, prints the connecting nodes (that is, nodes that are
   *     connected to at least one other node) to the console after the isolation.
   * @return The information of the isolated node.
   * @see IsolationInfo
   */
  IsolationInfo isolate(boolean useDegree, int radius, boolean trace) {
    IsolationInfo isolationInfo =
        (useDegree) ? degreeIsolate() : collectiveInfluenceIsolate(radius, true);

    // Print trace if necessary and return IsolationInfo

    if (trace) {
      System.out.print("Connected components:");
      for (int i = 1; i < data.length; i++) if (degree(i) > 0) System.out.print(" " + i);
      System.out.println();
    }

    return isolationInfo;
  }

  IsolationInfo degreeIsolate() {
    // Calculate the degree of every node and store it in an array.

    int[] degrees = new int[data.length];
    for (int i = 1; i < data.length; i++) degrees[i] = degree(i);

    // Find the node with the highest influence

    int maxDegreeNode = 0;
    int maxDegree = 0;

    for (int i = 1; i < degrees.length; i++)
      if (degrees[i] > degrees[maxDegreeNode]) {
        maxDegreeNode = i;
        maxDegree = degrees[maxDegreeNode];
      }

    // Before we isolate, we get the nodes connected to the node we are about to
    // isolate. This will help the graphics program later.

    ArrayList<Integer> connectedNodes = getConnectedNodes(maxDegreeNode);

    // Isolate that node

    isolateNode(maxDegreeNode);
    System.out.printf("%d %d\n", maxDegreeNode, maxDegree);
    lastIsolatedNode = maxDegreeNode;

    return new IsolationInfo(maxDegreeNode, maxDegree, "degree", connectedNodes);
  }

  /**
   * Isolates the node in this network with the highest collective influence.
   *
   * @param radius The radius to search within the h.
   * @param computeCollectiveInfluences Whether or not collective influences have to be computed for
   *     the given radius before isolation can begin.
   */
  IsolationInfo collectiveInfluenceIsolate(int radius, boolean computeCollectiveInfluences) {
    if (computeCollectiveInfluences) {
      computeCollectiveInfluences(radius);
      return collectiveInfluenceIsolate(radius, false);
    }

    // Find the node with the highest collective influence

    int maxCINode = 0;
    int maxCI = 0;

    for (int i = 1; i < data.length; i++)
      if (collectiveInfluence(i) > collectiveInfluence(maxCINode)) {
        maxCINode = i;
        maxCI = collectiveInfluence(maxCINode);
      }

    // Before we isolate, we get the nodes connected to the node we are about to
    // isolate. This will help the graphics program later.

    ArrayList<Integer> connectedNodes = getConnectedNodes(maxCINode);

    // Isolate that node

    int[] shortestPaths = getShortestPaths(maxCINode);

    isolateNode(maxCINode);
    System.out.printf("%d %d\n", maxCINode, maxCI);
    lastIsolatedNode = maxCINode;

    // Update the collective influences of nodes inside the radius

    for (int i = 1; i < shortestPaths.length; i++) {
      if (shortestPaths[i] <= radius) collectiveInfluences[i] = collectiveInfluence(i, radius);
    }

    return new IsolationInfo(maxCINode, maxCI, "collective influence", connectedNodes);
  }

  /**
   * Inoculates this network.
   *
   * @param numNodes The number of nodes to isolate.
   * @param useDegree If <code>true</code>, isolates the node with the highest degree. If <code>
   *     false</code>, use collective influence instead.
   * @param radius The radius to search within if collective influence is used.
   * @param trace If <code>true</code>, prints the connecting nodes (that is, nodes that are
   *     connected to at least one other node) to the console after the isolation.
   */
  void inoculate(int numNodes, boolean useDegree, int radius, boolean trace) {
    for (int i = 0; i < numNodes; i++) isolate(useDegree, radius, trace);
  }
}
