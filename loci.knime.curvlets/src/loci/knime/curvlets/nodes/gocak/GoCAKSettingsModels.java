package loci.knime.curvlets.nodes.gocak;

import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class GoCAKSettingsModels {

	static SettingsModelString createImgColumnSelectionModel() {
		return new SettingsModelString("img_column", "");
	}

	static SettingsModelString createBitMaskSelectionModel() {
		return new SettingsModelString("bit_mask_column", "");
	}

	static SettingsModelString createFiberModeModel() {
		return new SettingsModelString("fiber_mode", FiberModes.CTFF.getName());
	}

	static SettingsModelString createBoundaryModel() {
		return new SettingsModelString("boundary_mode",
				BoundaryModes.TIFF.getName());
	}

	static SettingsModelDouble createKeepModel() {
		return new SettingsModelDouble("keep", 0.05);
	}

	static SettingsModelDouble createDistThresholdModel() {
		return new SettingsModelDouble("distThreshold", 100.00);
	}

	static SettingsModelBoolean createMakeAssocModel() {
		return new SettingsModelBoolean("make_assoc", true);
	}

	static SettingsModelBoolean createMakeFeatModel() {
		return new SettingsModelBoolean("make_feat", true);
	}

	static SettingsModelBoolean createMakeOverModel() {
		return new SettingsModelBoolean("make_over", true);
	}

	static SettingsModelBoolean createMakeMapModel() {
		return new SettingsModelBoolean("make_map", true);
	}
}
