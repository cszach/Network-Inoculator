class Vector {
  double x;
  double y;

  Vector(double x, double y) {
    this.x = x;
    this.y = y;
  }

  Vector add(Vector v) {
    this.x += v.x;
    this.y += v.y;

    return this;
  }

  Vector subtract(Vector v) {
    this.x -= v.x;
    this.y -= v.y;

    return this;
  }

  Vector multiply(Vector v) {
    this.x *= v.x;
    this.y *= v.y;

    return this;
  }

  Vector multiply(double d) {
    this.x *= d;
    this.y *= d;

    return this;
  }

  Vector getSum(Vector v) {
    return new Vector(this.x + v.x, this.y + v.y);
  }

  Vector getDifference(Vector v) {
    return new Vector(this.x - v.x, this.y - v.y);
  }

  Vector getProduct(double d) {
    return new Vector(this.x * d, this.y * d);
  }

  Vector getProduct(Vector v) {
    return new Vector(this.x * v.x, this.y * v.y);
  }

  Vector getQuotient(Vector v) {
    return new Vector(this.x / v.x, this.y / v.y);
  }

  Vector getAbsoluteVector() {
    return new Vector(Math.abs(this.x), Math.abs(this.y));
  }

  Vector getDirectionVector() {
    Vector directionVector = new Vector(0, 0);

    if (this.x != 0) directionVector.x = this.x / Math.abs(this.x);
    if (this.y != 0) directionVector.y = this.y / Math.abs(this.y);

    return directionVector;
  }

  Vector getMin(double d) {
    return new Vector(Math.min(this.x, d), Math.min(this.y, d));
  }
}

class Vertex {
  Vector position;
  Vector displacement;

  Vertex(Vector position, Vector displacement) {
    this.position = position;
    this.displacement = displacement;
  }
}

class ForceDirected {
  NetworkGraph graph;
  /** The width of the drawing frame. */
  int width;
  /** The height of the drawing frame. */
  int height;
  /** The area of the drawing frame. */
  int area;
  /** The optimal distance between vertices. */
  double k;

  double x;
  /** The temperature of this simulation. */
  double temperature;
  /** The set of graphical vertices. */
  Vertex[] vertices;
  /** The total number of iterations allowed for this simulation. */
  int iterations;

  private double attractiveForce(Vector v) {
    double d = Math.sqrt(v.x * v.x + v.y * v.y); // The euclidean length of v
    return d * d / this.k;
  }

  private double repulsiveForce(Vector v) {
    double d = Math.sqrt(v.x * v.x + v.y * v.y);
    return (d == 0) ? 0 : this.k * this.k / d;
  }

  ForceDirected(NetworkGraph graph, int width, int height, int iterations) {
    this.graph = graph;
    this.width = width;
    this.height = height;
    this.area = this.width * this.height;
    this.k = Math.sqrt(this.area / (this.graph.data.length - 1));
    this.x = 100;
    // this.temperature = this.width / 10;
    this.iterations = iterations;

    // Assigned random initial positions to the vertices

    this.vertices = new Vertex[this.graph.data.length];
    this.vertices[0] = null;

    for (int i = 1; i < this.vertices.length; i++) {
      this.vertices[i] =
          new Vertex(
              new Vector(
                  (Math.random() * this.width) - this.width / 2,
                  (Math.random() * this.height) - this.height / 2),
              new Vector(0, 0));
    }

    this.cool();
  }

  void iterate() {
    // Calculate repulsive forces

    for (int i = 1; i < this.vertices.length; i++) {
      Vertex v = this.vertices[i];

      v.displacement = new Vector(0, 0);

      for (int j = 1; j < this.vertices.length; j++) {
        if (i == j) continue;

        Vertex u = this.vertices[j];

        Vector diff = v.position.getDifference(u.position);
        Vector diffDirection = diff.getDirectionVector();
        v.displacement.add(diffDirection.getProduct(repulsiveForce(diff.getAbsoluteVector())));
      }
    }

    // Calculate attractive forces

    for (int i = 1; i < this.vertices.length; i++) {
      for (int j = i + 1; j < this.vertices.length; j++) {
        if (!this.graph.isConnected(i, j)) continue;

        Vertex v = this.vertices[i];
        Vertex u = this.vertices[j];

        Vector diff = v.position.getDifference(u.position);
        Vector diffDirection = diff.getDirectionVector();
        Vector p = diffDirection.getProduct(attractiveForce(diff.getAbsoluteVector()));

        v.displacement.subtract(p);
        u.displacement.add(p);
      }
    }

    // Limit the maximum displacement to the temperature and prevent
    // displacement outside the frame

    for (int i = 1; i < this.vertices.length; i++) {
      Vertex v = this.vertices[i];

      v.position.add(
          v.displacement.getDirectionVector().multiply(v.displacement.getMin(this.temperature)));
      v.position.x = Math.min(this.width / 2, Math.max(-this.width / 2, v.position.x));
      v.position.y = Math.min(this.height / 2, Math.max(-this.height / 2, v.position.y));
    }

    this.cool();
  }

  void cool() {
    this.x--;

    if (this.x >= 2) {
      this.temperature = Math.log10(this.x);
    }
  }

  void simulate() {
    for (int i = 0; i < this.iterations; i++) this.iterate();
  }
}
