import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPanel;

/**
 * The canvas on which the graph will be drawn.
 *
 * @author Duong Nguyen
 */
class NetworkGraphCanvas extends JPanel
    implements MouseListener, MouseMotionListener, MouseWheelListener {
  /** The network graph to be drawn on this canvas. */
  NetworkGraph network;
  /** Vertical drawing offset used to preserve mouse drag effect. */
  int offsetX;
  /** Horizontal drawing offset used to preserve mouse drag effect. */
  int offsetY;
  /** X coordinate of the last mouse click, use to compute X offset. */
  int lastMouseX;
  /** Y coordinate of the last mouse click, use to compute Y offset. */
  int lastMouseY;
  /** Current amount of zoom. */
  double zoom = 1;
  /** On-screen message. */
  String onScreenMessage;
  /**
   * A dictionary of colors used to maintain consistent coloring of groups of connected nodes.
   *
   * <p>The dictionary maps a source node to a color. When painting, that source node and its
   * connected nodes (all depth) will share the same color.
   */
  HashMap<Integer, Color> colorGroups;
  /** The force-directed layout simulator. */
  ForceDirected forceDirectedSimulator;

  private final int NORMAL_CIRCLE_NODE_RADIUS = 20;
  private final Color BACKGROUND_COLOR = new Color(238, 238, 238);
  private final Color CIRCLE_NODE_COLOR = new Color(174, 213, 129);
  private final Color CIRCLE_NODE_BORDER_COLOR = new Color(0, 0, 0);
  private final Color CIRCLE_NODE_TEXT_COLOR = new Color(0, 0, 0);
  private final Color CIRCLE_LAST_ISOLATED_NODE_COLOR = new Color(255, 255, 255);
  private final Color LINE_COLOR = new Color(0, 0, 0);
  private final Color ON_SCREEN_MESSAGE_COLOR = new Color(0, 0, 0);

  /**
   * Constructs a visualizer for a given network.
   *
   * @param network
   */
  NetworkGraphCanvas(NetworkGraph network) {
    super();
    this.network = network;
    this.offsetX = 0;
    this.offsetY = 0;

    this.addMouseListener(this);
    this.addMouseMotionListener(this);
    this.addMouseWheelListener(this);
  }

  /** Sets the zoom such that the whole graph fits into the panel. */
  void setDefaultZoom() {
    int canvasWidth = this.getWidth();
    int canvasHeight = this.getHeight();

    if (canvasWidth > canvasHeight)
      this.zoom =
          canvasHeight / ((double) this.getLayoutRadius() * 2 + this.NORMAL_CIRCLE_NODE_RADIUS * 2);
    else
      this.zoom =
          canvasHeight / ((double) this.getLayoutRadius() * 2 + this.NORMAL_CIRCLE_NODE_RADIUS * 2);
  }

  void initializeForceDirectedSimulator() {
    this.forceDirectedSimulator =
        new ForceDirected(this.network, this.getWidth(), this.getHeight(), 1000);
    this.forceDirectedSimulator.simulate();
    this.update(this.getGraphics());
  }

  /**
   * Returns the ideal radius for the circular layout that we use to draw the circle nodes.
   *
   * @return The ideal drawing radius for the circular layout.
   */
  int getLayoutRadius() {
    return (this.NORMAL_CIRCLE_NODE_RADIUS * (this.network.data.length * 3)) / 8;
  }

  /**
   * Returns the center of the circle node, taking into account zoom and offset factors.
   *
   * @param node
   * @return The center of the circle node if it were to be drawn.
   */
  Point getCoords(int node) {
    int r = this.getLayoutRadius();
    int halfWidth = this.getWidth() / 2;
    int halfHeight = this.getHeight() / 2;

    Vertex v = this.forceDirectedSimulator.vertices[node];
    int x = (int) v.position.x;
    int y = (int) v.position.y;

    // x = r * cosθ
    // y = r *sinθ
    // θ = (2π / n) * i

    // int i = node - 1;
    // double angle = (Math.PI * 2 / (this.network.data.length - 1)) * i;
    // int x = (int) (r * Math.cos(angle));
    // int y = (int) (r * Math.sin(angle));

    // return new Point(
    //   (int) (this.zoom * (halfWidth + x + this.offsetX)),
    //   (int) (this.zoom * (halfHeight + y + this.offsetY))
    // );

    return new Point(
        (int) (halfWidth + this.zoom * (this.offsetX + x)),
        (int) (halfHeight + this.zoom * (this.offsetY + y)));
  }

  /**
   * Returns the radius of a circle node taking zoom into account.
   *
   * @return The radius of a circle node multiplied by zoom.
   */
  int getCircleNodeRadius() {
    return (int) (NORMAL_CIRCLE_NODE_RADIUS * this.zoom);
  }

  @Override
  public void mouseClicked(MouseEvent e) {}

  @Override
  public void mouseEntered(MouseEvent e) {}

  @Override
  public void mouseExited(MouseEvent e) {}

  @Override
  public void mousePressed(MouseEvent e) {
    this.lastMouseX = e.getX();
    this.lastMouseY = e.getY();
    this.onScreenMessage = "";
  }

  @Override
  public void mouseReleased(MouseEvent e) {}

  @Override
  public void mouseDragged(MouseEvent e) {
    int x = e.getX();
    int y = e.getY();

    this.offsetX += (x - this.lastMouseX);
    this.offsetY += (y - this.lastMouseY);

    this.update(this.getGraphics());

    this.lastMouseX = x;
    this.lastMouseY = y;
  }

  @Override
  public void mouseMoved(MouseEvent e) {}

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    // Update zoom
    double delta = e.getPreciseWheelRotation() / -10;
    if (zoom + delta > 0.5 || delta > 0) zoom += delta;
    this.onScreenMessage = "";
    this.update(this.getGraphics());
  }

  /**
   * Returns a random fill color for circle nodes.
   *
   * @return A random fill color for cicle nodes.
   */
  Color getRandomCircleNodeFillColor() {
    int r = (int) (Math.random() * 128) + 96;
    int g = (int) (Math.random() * 128) + 96;
    int b = (int) (Math.random() * 128) + 96;

    return new Color(r, g, b);
  }

  /**
   * Devises a stable color groups for the attached network graphs.
   *
   * <p>Every group of connected nodes is assigned a unique fill color.
   */
  void computeColorGroups() {
    this.colorGroups = new HashMap<Integer, Color>();
    this.network.markAllUnvisited();

    int unfilledCircleNode = this.network.getFirstUnvisitedNode();

    while (unfilledCircleNode != 0) {
      this.colorGroups.put(unfilledCircleNode, getRandomCircleNodeFillColor());

      ArrayList<Integer> connectedNodes = new ArrayList<Integer>();
      this.network.getConnectedNodes(unfilledCircleNode, connectedNodes);

      unfilledCircleNode = this.network.getFirstUnvisitedNode();
    }

    this.network.markAllUnvisited();
  }

  /**
   * Recomputes the color groups after a node has been isolated.
   *
   * @param isolationInfo The isolation info generated from isolating the node.
   */
  void recomputeColorGroups(IsolationInfo isolationInfo) {
    // First, extract the nodes previously connected to the isolated node
    ArrayList<Integer> connectedNodes = isolationInfo.connectedNodes;

    // Then find the color group that contains the isolated node (which also
    // covers its previously connected nodes) and remove it from the color
    // groups, since we will build new ones for the nodes now that their
    // connections are different
    for (Integer sourceColor : this.colorGroups.keySet()) {
      if (connectedNodes.contains(sourceColor)) {
        this.colorGroups.remove(sourceColor);
        break;
      }
    }

    // Later, we use connectedNodes to assign new color groups to previously
    // connected nodes. The isolated node gets its own color, so we put it into
    // its own color group and remove it from connectedNodes.
    this.colorGroups.put(isolationInfo.node, getRandomCircleNodeFillColor());
    connectedNodes.remove(connectedNodes.indexOf(isolationInfo.node));

    this.network.markAllUnvisited();

    // Assign new color groups for previously connected nodes whose connections
    // are now affected because of the isolation

    while (connectedNodes.size() > 0) {
      int sourceNode = connectedNodes.get(0);
      this.colorGroups.put(sourceNode, getRandomCircleNodeFillColor());
      ArrayList<Integer> newConnectedNodes = new ArrayList<Integer>();
      this.network.getConnectedNodes(sourceNode, newConnectedNodes);

      for (int i = 0; i < newConnectedNodes.size(); i++)
        connectedNodes.remove(connectedNodes.indexOf(newConnectedNodes.get(i)));
    }

    this.network.markAllUnvisited();
  }

  /**
   * Paints circle node.
   *
   * @param node The starting node
   * @param g This canvas's graphics context
   * @param color The color used to draw the nodes, will be drawn from a color group
   */
  private void drawCircleNode(
      int node, Graphics g, Color color, FontMetrics fontMetrics, int fontHeight, int fontAscent) {
    Point circleNodeCoords = this.getCoords(node);
    int circleNodeRadius = this.getCircleNodeRadius();

    // Draw the outline

    g.setColor(this.CIRCLE_NODE_BORDER_COLOR);
    g.drawOval(
        circleNodeCoords.x - circleNodeRadius,
        circleNodeCoords.y - circleNodeRadius,
        circleNodeRadius * 2,
        circleNodeRadius * 2);

    // Fill in the color

    g.setColor((node == network.lastIsolatedNode) ? this.CIRCLE_LAST_ISOLATED_NODE_COLOR : color);
    g.fillOval(
        circleNodeCoords.x - circleNodeRadius,
        circleNodeCoords.y - circleNodeRadius,
        circleNodeRadius * 2,
        circleNodeRadius * 2);

    // Draw the label (must be centered on the circle node)

    g.setColor(this.CIRCLE_NODE_TEXT_COLOR);
    String label = Integer.toString(node);
    // FontMetrics fontMetrics = g.getFontMetrics(g.getFont());
    g.drawString(
        label,
        circleNodeCoords.x - fontMetrics.stringWidth(label) / 2,
        circleNodeCoords.y - fontHeight / 2 + fontAscent);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    // Fill the background of the canvas before drawing

    int canvasWidth = this.getWidth();
    int canvasHeight = this.getHeight();

    g.setColor(this.BACKGROUND_COLOR);
    g.fillRect(0, 0, canvasWidth, canvasHeight);

    // Draw the connecting lines first because we want all of them to be under
    // the circles

    g.setColor(this.LINE_COLOR);

    for (int i = 1; i < this.network.data.length; i++)
      for (int j = i + 1; j < this.network.data.length; j++) {
        if (this.network.isConnected(i, j)) {
          Point iCoords = this.getCoords(i);
          Point jCoords = this.getCoords(j);

          g.drawLine(iCoords.x, iCoords.y, jCoords.x, jCoords.y);
        }
      }

    // Get some font metrics

    FontMetrics fontMetrics = g.getFontMetrics(g.getFont());
    int fontHeight = fontMetrics.getHeight();
    int fontAscent = fontMetrics.getAscent();

    for (HashMap.Entry<Integer, Color> colorGroupEntry : this.colorGroups.entrySet()) {
      ArrayList<Integer> connectedNodes = this.network.getConnectedNodes(colorGroupEntry.getKey());
      Color color = colorGroupEntry.getValue();
      for (int i = 0; i < connectedNodes.size(); i++)
        drawCircleNode(connectedNodes.get(i), g, color, fontMetrics, fontHeight, fontAscent);
    }

    // Draw the on-screen message

    g.setColor(this.ON_SCREEN_MESSAGE_COLOR);
    g.drawString(
        this.onScreenMessage,
        canvasWidth / 2 - fontMetrics.stringWidth(this.onScreenMessage) / 2,
        canvasHeight / 2 - fontHeight / 2 + fontAscent);
  }
}
