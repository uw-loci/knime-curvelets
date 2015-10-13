package loci.knime.curvlets.nodes.gocak;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * Factory class to provice {@link GoCAKNodeDialog} and
 * {@link GoCAKNodeModel}
 * 
 * @author Christian Dietz (University of Konstanz)
 * 
 */
public class GoCAKNodeFactory extends NodeFactory<GoCAKNodeModel> {

	@Override
	public GoCAKNodeModel createNodeModel() {
		return new GoCAKNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<GoCAKNodeModel> createNodeView(int viewIndex,
			GoCAKNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new GoCAKNodeDialog();
	}

}
