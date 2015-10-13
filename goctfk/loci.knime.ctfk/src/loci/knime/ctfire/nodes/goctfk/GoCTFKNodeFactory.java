package loci.knime.ctfire.nodes.goctfk;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * Factory class to provice {@link GoCTFKNodeDialog} and
 * {@link GoCTFKNodeModel}
 * 
 * @author Christian Dietz (University of Konstanz)
 * 
 */
public class GoCTFKNodeFactory extends NodeFactory<GoCTFKNodeModel> {

	@Override
	public GoCTFKNodeModel createNodeModel() {
		return new GoCTFKNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<GoCTFKNodeModel> createNodeView(int viewIndex,
			GoCTFKNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new GoCTFKNodeDialog();
	}

}
