package loci.knime.curvlets.nodes.gocak;

import static loci.knime.curvlets.nodes.gocak.GoCAKSettingsModels.createBitMaskSelectionModel;
import static loci.knime.curvlets.nodes.gocak.GoCAKSettingsModels.createBoundaryModel;
import static loci.knime.curvlets.nodes.gocak.GoCAKSettingsModels.createDistThresholdModel;
import static loci.knime.curvlets.nodes.gocak.GoCAKSettingsModels.createFiberModeModel;
import static loci.knime.curvlets.nodes.gocak.GoCAKSettingsModels.createImgColumnSelectionModel;
import static loci.knime.curvlets.nodes.gocak.GoCAKSettingsModels.createKeepModel;
import static loci.knime.curvlets.nodes.gocak.GoCAKSettingsModels.createMakeAssocModel;
import static loci.knime.curvlets.nodes.gocak.GoCAKSettingsModels.createMakeFeatModel;
import static loci.knime.curvlets.nodes.gocak.GoCAKSettingsModels.createMakeMapModel;
import static loci.knime.curvlets.nodes.gocak.GoCAKSettingsModels.createMakeOverModel;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.knip.base.data.img.ImgPlusValue;

@SuppressWarnings("unchecked")
public class GoCAKNodeDialog extends DefaultNodeSettingsPane {

	{

		createNewGroup("Column Selection");

		addDialogComponent(new DialogComponentColumnNameSelection(
				createImgColumnSelectionModel(), "Image Column", 0,
				ImgPlusValue.class));

		addDialogComponent(new DialogComponentColumnNameSelection(
				createBitMaskSelectionModel(), "Bit Mask Column", 0,
				ImgPlusValue.class));

		createNewGroup("Parameters");

		addDialogComponent(new DialogComponentStringSelection(
				createFiberModeModel(), "Fiber Mode", FiberModes.names()));

		addDialogComponent(new DialogComponentStringSelection(
				createBoundaryModel(), "Boundary Mode", BoundaryModes.names()));

		addDialogComponent(new DialogComponentNumber(createKeepModel(), "Keep",
				0.005));

		addDialogComponent(new DialogComponentNumber(
				createDistThresholdModel(), "Dist Threshold", 1));

		createNewTab("Advanced Settings");
		createNewGroup("Addtional Checks");
		addDialogComponent(new DialogComponentBoolean(createMakeAssocModel(),
				"Make Assoc?"));

		addDialogComponent(new DialogComponentBoolean(createMakeFeatModel(),
				"Make Feat?"));

		addDialogComponent(new DialogComponentBoolean(createMakeOverModel(),
				"Make Over?"));

		addDialogComponent(new DialogComponentBoolean(createMakeMapModel(),
				"Make Map?"));
	}

}
