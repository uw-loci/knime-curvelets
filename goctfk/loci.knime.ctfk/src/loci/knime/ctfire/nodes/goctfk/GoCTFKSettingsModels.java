
package loci.knime.ctfire.nodes.goctfk;

import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class GoCTFKSettingsModels {

	// Input options

	//FIXME these names may not be correct?

	static SettingsModelString createImgColumnSelectionModel() {
		return new SettingsModelString("img_column", "");
	}

	static SettingsModelString createMATColumnSelectionModel() {
		return new SettingsModelString("mat_column", "");
	}

	static SettingsModelBoolean createPostProcessModel() {
		return new SettingsModelBoolean("post_process", false);
	}

	// Fiber extraction

	static SettingsModelDouble createCurveletCoefficientModel() {
		return new SettingsModelDouble("curvelet_coefficient_percent", 0.2);
	}

	static SettingsModelInteger createSelectedScalesModel() {
		return new SettingsModelInteger("selected_scales", 3);
	}

	static SettingsModelInteger createThresholdModel() {
		return new SettingsModelInteger("threshold", 5);
	}

	static SettingsModelInteger createXLinkBoxModel() {
		return new SettingsModelInteger("xlinkbox", 8);
	}

	static SettingsModelInteger createExtendedAngleModel() {
		return new SettingsModelInteger("extended_angle", 70);
	}

	static SettingsModelInteger createDanglerLengthModel() {
		return new SettingsModelInteger("dangler_length_threshold", 15);
	}

	static SettingsModelInteger createShortLengthModel() {
		return new SettingsModelInteger("short_length_threshold", 15);
	}

	static SettingsModelInteger createNodeEndsModel() {
		return new SettingsModelInteger("node_ends", 4);
	}

	static SettingsModelInteger createLinkDistanceModel() {
		return new SettingsModelInteger("link_distance_threshold", 15);
	}

	static SettingsModelInteger createLinkAngleModel() {
		return new SettingsModelInteger("link_angle_threshold", 150);
	}

	static SettingsModelInteger createFreeLengthModel() {
		return new SettingsModelInteger("free_length_threshold", 15);
	}

	// Width options

	static SettingsModelBoolean createWidthCalcModel() {
		return new SettingsModelBoolean("width_calc", true);
	}

	static SettingsModelInteger createWidthMinMaxModel() {
		return new SettingsModelInteger("width_min_max", 10);
	}

	static SettingsModelInteger createMinFiberModel() {
		return new SettingsModelInteger("min_fiber_points", 6);
	}

	static SettingsModelDouble createWidthConfidenceModel() {
		return new SettingsModelDouble("width_confidence_sigma", 1);
	}

	static SettingsModelBoolean createCalcMaxModel() {
		return new SettingsModelBoolean("calc_max", false);
	}

	// Output options
	static SettingsModelDouble createFiberLineWidthModel() {
		return new SettingsModelDouble("fiber_line_width", 0.5);
	}

	static SettingsModelInteger createFiberLengthOutputModel() {
		return new SettingsModelInteger("fiber_length_threshold", 30);
	}

	static SettingsModelInteger createMaxFiberOutputModel() {
		return new SettingsModelInteger("max_fiber_count", 99999);
	}

	static SettingsModelInteger createOutputResolutionModel() {
		return new SettingsModelInteger("output_res", 300);
	}

	static SettingsModelInteger createFiberWidthOutputModel() {
		return new SettingsModelInteger("max_fiber_width", 15);
	}

	// CSV output
	static SettingsModelBoolean createOutputAngleModel() {
		return new SettingsModelBoolean("output_angle", true);
	}

	static SettingsModelBoolean createOutputLengthModel() {
		return new SettingsModelBoolean("output_length", true);
	}

	static SettingsModelBoolean createOutputWidthModel() {
		return new SettingsModelBoolean("otuput_width", true);
	}

	static SettingsModelBoolean createOutputStraightnessModel() {
		return new SettingsModelBoolean("output_straightness", true);
	}
}
