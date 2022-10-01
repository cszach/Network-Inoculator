import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * The main StopContagion GUI program.
 *
 * <p>Its significant component, the <code>NetworkGraphCanvas</code>, is implemented in its own
 * file. The constructor prepares the rest of the UI and implements their functionality. The program
 * does not always have to be started in GUI mode—the --no-gui flag can be invoked for quick
 * testing.
 */
class StopContagion extends JFrame implements ActionListener {
  NetworkGraph network;
  boolean useDegree;
  int radius;
  int numNodes;
  boolean trace;
  int step;
  final int INITIAL_WIDTH = 1080;
  final int INITIAL_HEIGHT = 800;
  NetworkGraphCanvas canvas;
  JButton isolateButton;
  JButton x1ZoomButton;
  JButton resetViewButton;

  StopContagion(NetworkGraph network, int numNodes, boolean useDegree, int radius, boolean trace) {
    super();

    this.network = network;
    this.numNodes = numNodes;
    this.useDegree = useDegree;
    this.radius = radius;
    this.trace = trace;
    this.step = 0;

    this.setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

    JPanel controlPanel = new JPanel();
    controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
    controlPanel.setMinimumSize(new Dimension(800, 25));
    controlPanel.setMaximumSize(new Dimension(800, 25));
    controlPanel.setPreferredSize(new Dimension(800, 25));
    controlPanel.setBackground(Color.WHITE);

    this.isolateButton = new JButton((this.numNodes == 0) ? "Finish" : "Isolate");
    this.isolateButton.addActionListener(this);

    this.x1ZoomButton = new JButton("x1 zoom");
    this.x1ZoomButton.addActionListener(this);

    this.resetViewButton = new JButton("Reset view");
    this.resetViewButton.addActionListener(this);

    controlPanel.add(this.isolateButton);
    controlPanel.add(this.x1ZoomButton);
    controlPanel.add(this.resetViewButton);

    this.canvas = new NetworkGraphCanvas(network);
    this.canvas.computeColorGroups();
    this.canvas.onScreenMessage =
        (this.numNodes == 0)
            ? "Not inoculating, view only"
            : "Click the Isolate button to begin the inoculation process";

    this.add(controlPanel);
    this.add(canvas);

    this.setVisible(true);
    this.canvas.initializeForceDirectedSimulator();
    // this.canvas.setDefaultZoom();
    // this.canvas.update(this.canvas.getGraphics());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();

    if (source == this.isolateButton) {
      if (this.step < this.numNodes) {
        IsolationInfo isolationInfo = this.network.isolate(this.useDegree, this.radius, this.trace);
        this.canvas.recomputeColorGroups(isolationInfo);
        this.canvas.onScreenMessage =
            String.format(
                "Isolated node %d with %s %d",
                isolationInfo.node, isolationInfo.unit, isolationInfo.influence);
        this.canvas.update(this.canvas.getGraphics());

        if (++this.step == this.numNodes) this.isolateButton.setText("Finish");
      } else {
        System.exit(0);
      }
    } else if (source == this.x1ZoomButton) {
      this.canvas.zoom = 1;
      this.canvas.update(this.canvas.getGraphics());
    } else if (source == this.resetViewButton) {
      this.canvas.offsetX = 0;
      this.canvas.offsetY = 0;
      this.canvas.setDefaultZoom();
      this.canvas.update(this.canvas.getGraphics());
    }
  }

  static void throwFatalError(String message, int exitCode) {
    System.out.println(message);
    System.out.println("Usage: StopContagion [--no-gui] [-d] [-r RADIUS] NUM_NODES FILE");
    System.exit(exitCode);
  }

  /**
   * Reads input from a file into a network graph.
   *
   * @param file
   * @return The new network
   */
  static NetworkGraph makeNetworkGraphFromFile(File file) throws FileNotFoundException {
    int inputSize = 0;

    Scanner scanner = new Scanner(file);

    while (scanner.hasNextInt()) {
      int nextInt = scanner.nextInt();
      if (nextInt > inputSize) inputSize = nextInt;
    }

    scanner.close();

    NetworkGraph network = new NetworkGraph(inputSize);
    scanner = new Scanner(file);

    while (scanner.hasNextInt()) network.connect(scanner.nextInt(), scanner.nextInt());
    scanner.close();

    return network;
  }

  public static void main(String[] args) {
    // Initialize arguments

    boolean useDegree = false;
    int radius = 2;
    int numNodes = 0;
    String inputFile = null;
    boolean trace = false;
    boolean noGUI = false;

    // Parse arguments from the command line

    int i;

    for (i = 0; i < args.length - 2; i++) {
      switch (args[i]) {
        case "-d":
          useDegree = true;
          break;
        case "-r":
          try {
            radius = Integer.parseInt(args[++i]);
          } catch (NumberFormatException e) {
            throwFatalError("Invalid value given for '-r'", 1);
          }
          break;
        case "-t":
          trace = true;
          break;
        case "--no-gui":
          noGUI = true;
          break;
        default:
          throwFatalError("Invalid argument " + args[i], 1);
      }
    }

    if (args.length - i != 2) {
      throwFatalError("Missing required arguments", 1);
    }

    try {
      numNodes = Integer.parseInt(args[i++]);
      inputFile = args[i];
    } catch (NumberFormatException e) {
      throwFatalError("Invalid value given for a required argument", 1);
    }

    // ARGUMENTS PARSING DEBUG
    // System.out.printf("useDegree = %b\n", useDegree);
    // System.out.printf("radius    = %d\n", radius);
    // System.out.printf("numNodes  = %d\n", numNodes);
    // System.out.printf("inputFile = %s\n", inputFile);

    // Read input from file

    File file = new File(inputFile);
    NetworkGraph network = null;

    try {
      network = StopContagion.makeNetworkGraphFromFile(file);
    } catch (FileNotFoundException e) {
      throwFatalError("Error reading input file, please check the file name", 2);
    }

    network.computeCollectiveInfluences(radius);
    // for (int a = 1; a < network.collectiveInfluences.length; a++) {
    //   System.out.println(a + " " + network.collectiveInfluences[a]);
    // }

    // Start the damn program

    if (noGUI) {
      network.inoculate(numNodes, useDegree, radius, trace);
    } else {
      StopContagion visualizer = new StopContagion(network, numNodes, useDegree, radius, trace);
    }
  }
}
