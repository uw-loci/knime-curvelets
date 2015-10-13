
package loci.knime.ctfire.nodes.goctfk;

import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createCalcMaxModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createCurveletCoefficientModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createDanglerLengthModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createExtendedAngleModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createFiberLengthOutputModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createFiberLineWidthModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createFiberWidthOutputModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createFreeLengthModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createImgColumnSelectionModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createLinkAngleModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createLinkDistanceModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createMATColumnSelectionModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createMaxFiberOutputModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createMinFiberModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createNodeEndsModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createOutputAngleModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createOutputLengthModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createOutputResolutionModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createOutputStraightnessModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createOutputWidthModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createPostProcessModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createSelectedScalesModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createShortLengthModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createThresholdModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createWidthCalcModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createWidthConfidenceModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createWidthMinMaxModel;
import static loci.knime.ctfire.nodes.goctfk.GoCTFKSettingsModels.createXLinkBoxModel;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.knip.base.data.img.ImgPlusValue;

@SuppressWarnings("unchecked")
public class GoCTFKNodeDialog extends DefaultNodeSettingsPane {

	// Static initializer to build the dialog
	{
		// Input
		createNewGroup("Column Selection");

		addDialogComponent(new DialogComponentColumnNameSelection(
				createImgColumnSelectionModel(), "Image Column", 0,
				ImgPlusValue.class));

		addDialogComponent(new DialogComponentColumnNameSelection(
			createMATColumnSelectionModel(), "MAT Data path", 1,
			false, StringValue.class));

		createNewGroup("Input Options");

		addDialogComponent(new DialogComponentBoolean(createPostProcessModel(),
			"Post-process"));

		// Fiber extraction
		createNewGroup("Fiber Extraction Parameters");

		addDialogComponent(new DialogComponentNumber(
			createCurveletCoefficientModel(), "Curvelet coefficient (%)", 0.01));

		addDialogComponent(new DialogComponentNumber(createSelectedScalesModel(),
			"Selected scales", 1));

		addDialogComponent(new DialogComponentNumber(createThresholdModel(),
			"Threshold (intensity)", 1));

		addDialogComponent(new DialogComponentNumber(createXLinkBoxModel(),
			"Crosslink box radius (pixels)", 1));

		addDialogComponent(new DialogComponentNumber(createExtendedAngleModel(),
			"Required angle similarity (degrees)", 1));

		addDialogComponent(new DialogComponentNumber(createDanglerLengthModel(),
			"Dangler length threshold (pixels)", 1));

		addDialogComponent(new DialogComponentNumber(createShortLengthModel(),
			"Short length threshold (pixels)", 1));

		addDialogComponent(new DialogComponentNumber(createNodeEndsModel(),
			"Node count for calculation fiber ends", 1));

		addDialogComponent(new DialogComponentNumber(createLinkDistanceModel(),
			"Distance for linking same-oriented fibers (pixels)", 1));

		addDialogComponent(new DialogComponentNumber(createLinkAngleModel(),
			"Minimum angle between fiber ends for linking (degrees)", 1));

		addDialogComponent(new DialogComponentNumber(createFreeLengthModel(),
			"Minimum length of a free fiber (pixels)", 1));

		// Width calculations
		createNewGroup("Parameters for Width Calculations");

		addDialogComponent(new DialogComponentBoolean(createWidthCalcModel(),
			"Use all points in width calc"));

		addDialogComponent(new DialogComponentNumber(createWidthMinMaxModel(),
			"Minimum fiber width (pixels)", 1));

		addDialogComponent(new DialogComponentNumber(createMinFiberModel(),
			"Minimum point count to apply fiber selection", 1));

		addDialogComponent(new DialogComponentNumber(createWidthConfidenceModel(),
			"Width confidence region [-1, 1]", 0.001));

		addDialogComponent(new DialogComponentBoolean(createCalcMaxModel(),
			"Calculate max width of each fiber"));

		// Output
		createNewTab("Output Settings");

		// Image output
		createNewGroup("Output Image Format Control");

		addDialogComponent(new DialogComponentNumber(createFiberLineWidthModel(),
			"Fiber width in overlaid image (pixels)", 1));

		addDialogComponent(new DialogComponentNumber(
			createFiberLengthOutputModel(), "Fiber length threshold (pixels)", 1));

		addDialogComponent(new DialogComponentNumber(createMaxFiberOutputModel(),
			"Maximum number of fibers in an individual image", 1));

		addDialogComponent(new DialogComponentNumber(createOutputResolutionModel(),
			"Output resolution (dpi)", 1));

		addDialogComponent(new DialogComponentNumber(createFiberWidthOutputModel(),
			"Maximum width at any fiber point (pixels)", 1));

		// CSV output
		createNewGroup("CSV Output Control");

		addDialogComponent(new DialogComponentBoolean(createOutputAngleModel(),
			"Output fiber angles"));

		addDialogComponent(new DialogComponentBoolean(createOutputLengthModel(),
			"Output fiber lengths"));

		addDialogComponent(new DialogComponentBoolean(createOutputWidthModel(),
			"Output fiber widths"));

		addDialogComponent(new DialogComponentBoolean(
			createOutputStraightnessModel(), "Output fiber straightness"));
	}
}
