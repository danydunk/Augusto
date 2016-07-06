package test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;

import org.junit.Test;

import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Input_widget;
import usi.gui.structure.Window;
import usi.util.DijkstraAlgorithm;
import usi.util.Graph;
import usi.util.Vertex;

public class TestGUiDijkstra {

	public class Action_widget_test extends Action_widget {

		public Action_widget_test(final String id, final String label) throws Exception {

			super(id, label, "class", 1, 1);
		}
	}

	public class Input_widget_test extends Input_widget {

		public Input_widget_test(final String id, final String label, final String value)
				throws Exception {

			super(id, label, "class", 1, 1, value);
		}
	}

	public class Window_test extends Window {

		public Window_test(final String id, final String label) throws Exception {

			super(id, label, "class", 1, 1, false);
		}

		public Window_test(final String id, final boolean b, final String label) throws Exception {

			super(id, label, "class", 1, 1, b);
		}
	}

	@Test
	public void test() throws Exception {

		final GUI gui = new GUI();
		// w1
		final Window_test w1 = new Window_test("w1", "init");
		final Action_widget_test aw1 = new Action_widget_test("aw1", "add");
		final Action_widget_test aw2 = new Action_widget_test("aw2", "test");
		w1.addWidget(aw1);
		w1.addWidget(aw2);
		gui.addWindow(w1);
		// w2
		final Window_test w2 = new Window_test("w2", "form");
		final Action_widget_test aw3 = new Action_widget_test("aw3", "next");
		final Action_widget_test aw4 = new Action_widget_test("aw4", "back");
		final Input_widget_test iw1 = new Input_widget_test("iw1", "field1", "");
		final Input_widget_test iw2 = new Input_widget_test("iw2", "field2", "");
		w2.addWidget(aw3);
		w2.addWidget(aw4);
		w2.addWidget(iw1);
		w2.addWidget(iw2);
		gui.addWindow(w2);
		// w3
		final Window_test w3 = new Window_test("w3", "other");
		final Action_widget_test aw5 = new Action_widget_test("aw5", "add");
		final Input_widget_test iw3 = new Input_widget_test("iw3", "field3", "");
		w3.addWidget(aw5);
		w3.addWidget(iw3);
		gui.addWindow(w3);
		// w4
		final Window_test w4 = new Window_test("w4", "form");
		final Action_widget_test aw6 = new Action_widget_test("aw6", "next");
		final Action_widget_test aw7 = new Action_widget_test("aw7", "back");
		final Input_widget_test iw4 = new Input_widget_test("iw4", "field3", "");
		final Input_widget_test iw5 = new Input_widget_test("iw5", "field4", "");
		w4.addWidget(aw6);
		w4.addWidget(aw7);
		w4.addWidget(iw4);
		w4.addWidget(iw5);
		gui.addWindow(w4);
		// w5
		final Window_test w5 = new Window_test("w5", true, "confirm");
		final Action_widget_test aw8 = new Action_widget_test("aw8", "ok");
		final Action_widget_test aw9 = new Action_widget_test("aw9", "back");
		w5.addWidget(aw8);
		w5.addWidget(aw9);
		gui.addWindow(w5);
		// edges
		gui.addStaticEdge(aw1, w4);
		gui.addStaticEdge(aw6, w2);
		gui.addStaticEdge(aw7, w1);
		gui.addStaticEdge(aw2, w3);
		gui.addStaticEdge(aw4, w4);
		gui.addStaticEdge(aw3, w5);
		gui.addStaticEdge(aw9, w2);

		// w1B
		final Window_test w1b = new Window_test("w1b", "init1");
		final Action_widget_test aw1b = new Action_widget_test("aw1b", "add");
		final Action_widget_test aw2b = new Action_widget_test("aw2b", "test");
		w1b.addWidget(aw1b);
		w1b.addWidget(aw2b);
		gui.addWindow(w1b);
		// w2B
		final Window_test w2b = new Window_test("w2b", "form2");
		final Action_widget_test aw3b = new Action_widget_test("aw3b", "next");
		final Action_widget_test aw4b = new Action_widget_test("aw4b", "back");
		final Input_widget_test iw1b = new Input_widget_test("iw1b", "field1", "");
		final Input_widget_test iw2b = new Input_widget_test("iw2b", "field2", "");
		w2b.addWidget(aw3b);
		w2b.addWidget(aw4b);
		w2b.addWidget(iw1b);
		w2b.addWidget(iw2b);
		gui.addWindow(w2b);
		// w3B
		final Window_test w3b = new Window_test("w3b", "form2");
		final Action_widget_test aw5b = new Action_widget_test("aw5b", "next");
		final Action_widget_test aw6b = new Action_widget_test("aw6b", "back");
		final Input_widget_test iw3b = new Input_widget_test("iw3b", "field1", "");
		w3b.addWidget(aw5b);
		w3b.addWidget(aw6b);
		w3b.addWidget(iw3b);
		gui.addWindow(w3b);
		// edges
		gui.addStaticEdge(aw1b, w3b);
		gui.addStaticEdge(aw6b, w1b);
		gui.addStaticEdge(aw5b, w2b);
		gui.addStaticEdge(aw4b, w3b);

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