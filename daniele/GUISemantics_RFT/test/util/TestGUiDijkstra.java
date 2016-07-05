package test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;

import org.junit.Test;

import usi.guistructure.Action_widget;
import usi.guistructure.GUI;
import usi.guistructure.Input_widget;
import usi.guistructure.Window;
import usi.util.DijkstraAlgorithm;
import usi.util.Graph;
import usi.util.Vertex;

public class TestGUiDijkstra {

	@Test
	public void test() throws Exception {

		final GUI gui = new GUI();
		// w1
		final Window w1 = new Window("w1", "init");
		final Action_widget aw1 = new Action_widget("aw1", "add");
		final Action_widget aw2 = new Action_widget("aw2", "test");
		w1.addActionWidget(aw1);
		w1.addActionWidget(aw2);
		gui.addWindow(w1);
		// w2
		final Window w2 = new Window("w2", "form");
		final Action_widget aw3 = new Action_widget("aw3", "next");
		final Action_widget aw4 = new Action_widget("aw4", "back");
		final Input_widget iw1 = new Input_widget("iw1", "field1", "");
		final Input_widget iw2 = new Input_widget("iw2", "field2", "");
		w2.addActionWidget(aw3);
		w2.addActionWidget(aw4);
		w2.addInputWidget(iw1);
		w2.addInputWidget(iw2);
		gui.addWindow(w2);
		// w3
		final Window w3 = new Window("w3", "other");
		final Action_widget aw5 = new Action_widget("aw5", "add");
		final Input_widget iw3 = new Input_widget("iw3", "field3", "");
		w3.addActionWidget(aw5);
		w3.addInputWidget(iw3);
		gui.addWindow(w3);
		// w4
		final Window w4 = new Window("w4", "form");
		final Action_widget aw6 = new Action_widget("aw6", "next");
		final Action_widget aw7 = new Action_widget("aw7", "back");
		final Input_widget iw4 = new Input_widget("iw4", "field3", "");
		final Input_widget iw5 = new Input_widget("iw5", "field4", "");
		w4.addActionWidget(aw6);
		w4.addActionWidget(aw7);
		w4.addInputWidget(iw4);
		w4.addInputWidget(iw5);
		gui.addWindow(w4);
		// w5
		final Window w5 = new Window("w5", true, "confirm", false);
		final Action_widget aw8 = new Action_widget("aw8", "ok");
		final Action_widget aw9 = new Action_widget("aw9", "back");
		w5.addActionWidget(aw8);
		w5.addActionWidget(aw9);
		gui.addWindow(w5);
		// edges
		gui.addEdge(aw1, w4);
		gui.addEdge(aw6, w2);
		gui.addEdge(aw7, w1);
		gui.addEdge(aw2, w3);
		gui.addEdge(aw4, w4);
		gui.addEdge(aw3, w5);
		gui.addEdge(aw9, w2);

		// w1B
		final Window w1b = new Window("w1b", "init1");
		final Action_widget aw1b = new Action_widget("aw1b", "add");
		final Action_widget aw2b = new Action_widget("aw2b", "test");
		w1b.addActionWidget(aw1b);
		w1b.addActionWidget(aw2b);
		gui.addWindow(w1b);
		// w2B
		final Window w2b = new Window("w2b", "form2");
		final Action_widget aw3b = new Action_widget("aw3b", "next");
		final Action_widget aw4b = new Action_widget("aw4b", "back");
		final Input_widget iw1b = new Input_widget("iw1b", "field1", "");
		final Input_widget iw2b = new Input_widget("iw2b", "field2", "");
		w2b.addActionWidget(aw3b);
		w2b.addActionWidget(aw4b);
		w2b.addInputWidget(iw1b);
		w2b.addInputWidget(iw2b);
		gui.addWindow(w2b);
		// w3B
		final Window w3b = new Window("w3b", "form2");
		final Action_widget aw5b = new Action_widget("aw5b", "next");
		final Action_widget aw6b = new Action_widget("aw6b", "back");
		final Input_widget iw3b = new Input_widget("iw3b", "field1", "");
		w3b.addActionWidget(aw5b);
		w3b.addActionWidget(aw6b);
		w3b.addInputWidget(iw3b);
		gui.addWindow(w3b);
		// edges
		gui.addEdge(aw1b, w3b);
		gui.addEdge(aw6b, w1b);
		gui.addEdge(aw5b, w2b);
		gui.addEdge(aw4b, w3b);

		final Graph graph = Graph.convert(gui);
		assertNotNull(graph);

		assertEquals(gui.getWindows().size(), graph.getVertexes().size());

		assertEquals(11, graph.getEdges().size());

		final DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
		final Vertex vsource1 = graph.getVertex(w1.getId());

		dijkstra.execute(vsource1);

		final Vertex vtarget1 = graph.getVertex(w5.getId());
		final LinkedList<Vertex> path = dijkstra.getPath(vtarget1);

		assertNotNull(path);
		assertTrue(path.size() > 0);
		// System.out.println(path);

		final Vertex vtarget2 = graph.getVertex(w3b.getId());
		// Any connection
		LinkedList<Vertex> path2 = dijkstra.getPath(vtarget2);

		assertTrue(path2 == null);

		final Vertex vsource2 = graph.getVertex(w1b.getId());
		dijkstra.execute(vsource2);

		path2 = dijkstra.getPath(vtarget2);

		assertTrue(path2.size() > 0);
		// System.out.println(path2);
	}
}