package test.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import src.usi.util.*;

public class TestDijkstraAlgorithm {

	private List<Vertex> nodes;
	private List<Edge> edges;

	@Test
	public void testExcute() {

		this.nodes = new ArrayList<Vertex>();
		this.edges = new ArrayList<Edge>();
		for (int i = 0; i < 11; i++) {
			final Vertex location = new Vertex("Node_" + i, "Node_" + i);
			this.nodes.add(location);
		}

		this.addLane("Edge_0", 0, 1, 85);
		this.addLane("Edge_1", 0, 2, 217);
		this.addLane("Edge_2", 0, 4, 173);
		this.addLane("Edge_3", 2, 6, 186);
		this.addLane("Edge_4", 2, 7, 103);
		this.addLane("Edge_5", 3, 7, 183);
		this.addLane("Edge_6", 5, 8, 250);
		this.addLane("Edge_7", 8, 9, 84);
		this.addLane("Edge_8", 7, 9, 167);
		this.addLane("Edge_9", 4, 9, 502);
		this.addLane("Edge_10", 9, 10, 40);
		this.addLane("Edge_11", 1, 10, 600);

		// Lets check from location Loc_1 to Loc_10
		final Graph graph = new Graph(this.nodes, this.edges);
		final DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
		dijkstra.execute(this.nodes.get(0));
		final LinkedList<Vertex> path = dijkstra.getPath(this.nodes.get(10));

		assertNotNull(path);
		assertTrue(path.size() > 0);

		// for (final Vertex vertex : path) {
		// System.out.println(vertex);
		// }
	}

	private void addLane(final String laneId, final int sourceLocNo, final int destLocNo, final int duration) {

		final Edge lane = new Edge(laneId, this.nodes.get(sourceLocNo), this.nodes.get(destLocNo), duration);
		this.edges.add(lane);
	}
}
