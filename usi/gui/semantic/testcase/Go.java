package usi.gui.semantic.testcase;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import usi.gui.structure.Action_widget;
import usi.gui.structure.GUI;
import usi.gui.structure.Window;
import usi.util.DijkstraAlgorithm;
import usi.util.Graph;
import usi.util.Vertex;

public class Go extends GUIAction {

	public Go(final Window source, final Window oracle, final Window target) throws Exception {

		super(source, target, oracle);
	}

	@Override
	public boolean isSame(final GUIAction act) {

		if (!(act instanceof Go)) {
			return false;
		}
		return this.same(act);
	}

	public List<GUIAction> getActionSequence(final Window current, final GUI gui) throws Exception {

		final List<GUIAction> out = new ArrayList<>();
		final Graph g = Graph.convertGUI(gui);

		Vertex source = g.getVertex(current.getId());
		Vertex target = g.getVertex(this.getWidget().getId());

		final DijkstraAlgorithm alg = new DijkstraAlgorithm(g);
		alg.execute(source);
		final LinkedList<Vertex> path = alg.getPath(target);

		if (path == null) {
			throw new Exception(
					"GUIAction - getActionSequence: action sequence could not be found.");
		}

		source = path.pop();
		while (!path.isEmpty()) {
			target = path.pop();
			Click click = null;
			final Window s = gui.getWindow(source.getId());
			final Window t = gui.getWindow(target.getId());
			for (final Action_widget aw : gui.getStaticBackwardLinks(t.getId())) {
				if (s.getWidget(aw.getId()) != null) {
					click = new Click(s, null, aw);
					break;
				}
			}
			if (click == null) {
				throw new Exception(
						"GUIAction - getActionSequence: error generating action sequence.");
			}
			out.add(click);
			source = target;
		}

		return out;
	}
}