package src.usi.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import src.usi.gui.structure.Action_widget;
import src.usi.gui.structure.GUI;
import src.usi.gui.structure.Window;

public class Graph {

	private final List<Vertex> vertexes;
	private final List<Edge> edges;

	public Graph(final List<Vertex> vertexes, final List<Edge> edges) {

		this.vertexes = vertexes;
		this.edges = edges;
	}

	public List<Vertex> getVertexes() {

		return this.vertexes;
	}

	public List<Edge> getEdges() {

		return this.edges;
	}

	public Vertex getVertex(final String id) {

		for (final Vertex vertex : this.vertexes) {
			if (vertex.getId().equals(id)) {
				return vertex;
			}
		}
		return null;
	}

	public static Graph convertGUI(final GUI gui) throws Exception {

		final List<Vertex> vertexes = new ArrayList<>();
		final List<Edge> edges = new ArrayList<>();

		// First, we create a vertex for each window
		for (final Window window : gui.getWindows()) {

			final Vertex vertex = new Vertex(window.getId(), window.getLabel());
			vertexes.add(vertex);
		}

		// Now, we create the edges
		for (final Window window : gui.getWindows()) {
			final Vertex vsrc = getVertex(vertexes, window.getId());
			for (final Action_widget actionWindows : window.getActionWidgets()) {
				final Collection<Window> links = gui.getStaticForwardLinks(actionWindows.getId());
				// we remove the terminal edges
				if (links.size() > 1) {
					continue;
				}
				for (final Window linkedWin : links) {
					final Vertex vdest = getVertex(vertexes, linkedWin.getId());
					final int weight = 1;
					final String edgeKey = vsrc.getId() + "#" + vdest.getId();
					final Edge edge = new Edge(edgeKey, vsrc, vdest, weight);
					edges.add(edge);
				}
			}
		}

		final Graph graph = new Graph(vertexes, edges);
		return graph;
	}

	private static Vertex getVertex(final List<Vertex> vertexes, final String id) {

		for (final Vertex vertex : vertexes) {
			if (vertex.getId().equals(id)) {
				return vertex;
			}
		}
		// if we arrive here, there is a problem with the graph..
		throw new IllegalStateException("Any vertex");
	}
}
