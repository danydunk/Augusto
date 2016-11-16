package src.usi.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DijkstraAlgorithm {

	private final List<Vertex> nodes;
	private final List<Edge> edges;
	private Set<Vertex> settledNodes;
	private Set<Vertex> unSettledNodes;
	private Map<Vertex, Vertex> predecessors;
	private Map<Vertex, Integer> distance;

	public DijkstraAlgorithm(final Graph graph) {
		// create a copy of the array so that we can operate on this array
		this.nodes = new ArrayList<Vertex>(graph.getVertexes());
		this.edges = new ArrayList<Edge>(graph.getEdges());
	}

	public void execute(final Vertex source) {

		this.settledNodes = new HashSet<Vertex>();
		this.unSettledNodes = new HashSet<Vertex>();
		this.distance = new HashMap<Vertex, Integer>();
		this.predecessors = new HashMap<Vertex, Vertex>();
		this.distance.put(source, 0);
		this.unSettledNodes.add(source);
		while (this.unSettledNodes.size() > 0) {
			final Vertex node = this.getMinimum(this.unSettledNodes);
			this.settledNodes.add(node);
			this.unSettledNodes.remove(node);
			this.findMinimalDistances(node);
		}
	}

	private void findMinimalDistances(final Vertex node) {

		final List<Vertex> adjacentNodes = this.getNeighbors(node);
		for (final Vertex target : adjacentNodes) {
			if (this.getShortestDistance(target) > this.getShortestDistance(node) + this.getDistance(node, target)) {
				this.distance.put(target, this.getShortestDistance(node) + this.getDistance(node, target));
				this.predecessors.put(target, node);
				this.unSettledNodes.add(target);
			}
		}

	}

	private int getDistance(final Vertex node, final Vertex target) {

		for (final Edge edge : this.edges) {
			if (edge.getSource().equals(node) && edge.getDestination().equals(target)) {
				return edge.getWeight();
			}
		}
		throw new RuntimeException("Should not happen");
	}

	private List<Vertex> getNeighbors(final Vertex node) {

		final List<Vertex> neighbors = new ArrayList<Vertex>();
		for (final Edge edge : this.edges) {
			if (edge.getSource().equals(node) && !this.isSettled(edge.getDestination())) {
				neighbors.add(edge.getDestination());
			}
		}
		return neighbors;
	}

	private Vertex getMinimum(final Set<Vertex> vertexes) {

		Vertex minimum = null;
		for (final Vertex vertex : vertexes) {
			if (minimum == null) {
				minimum = vertex;
			} else {
				if (this.getShortestDistance(vertex) < this.getShortestDistance(minimum)) {
					minimum = vertex;
				}
			}
		}
		return minimum;
	}

	private boolean isSettled(final Vertex vertex) {

		return this.settledNodes.contains(vertex);
	}

	private int getShortestDistance(final Vertex destination) {

		final Integer d = this.distance.get(destination);
		if (d == null) {
			return Integer.MAX_VALUE;
		} else {
			return d;
		}
	}

	/*
	 * This method returns the path from the source to the selected target and
	 * NULL if no path exists
	 */
	public LinkedList<Vertex> getPath(final Vertex target) {

		final LinkedList<Vertex> path = new LinkedList<Vertex>();
		Vertex step = target;
		// check if a path exists
		if (this.predecessors.get(step) == null) {
			return null;
		}
		path.add(step);
		while (this.predecessors.get(step) != null) {
			step = this.predecessors.get(step);
			path.add(step);
		}
		// Put it into the correct order
		Collections.reverse(path);
		return path;
	}

}
