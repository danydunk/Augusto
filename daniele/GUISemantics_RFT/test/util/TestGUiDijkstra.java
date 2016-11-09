package test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;

import org.junit.Test;

import test.gui.GUIStructureMaker;
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

		final GUI gui = GUIStructureMaker.instance6();

		final Graph graph = Graph.convertGUI(gui);
		assertNotNull(graph);

		assertEquals(gui.getWindows().size(), graph.getVertexes().size());

		assertEquals(11, graph.getEdges().size());

		final DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
		final Vertex vsource1 = graph.getVertex("w1");

		dijkstra.execute(vsource1);

		final Vertex vtarget1 = graph.getVertex("w5");
		final LinkedList<Vertex> path = dijkstra.getPath(vtarget1);

		assertNotNull(path);
		assertTrue(path.size() > 0);
		// System.out.println(path);

		final Vertex vtarget2 = graph.getVertex("w3b");
		// Any connection
		LinkedList<Vertex> path2 = dijkstra.getPath(vtarget2);

		assertTrue(path2 == null);

		final Vertex vsource2 = graph.getVertex("w1b");
		dijkstra.execute(vsource2);

		path2 = dijkstra.getPath(vtarget2);

		assertTrue(path2.size() > 0);
		// System.out.println(path2);
	}
}