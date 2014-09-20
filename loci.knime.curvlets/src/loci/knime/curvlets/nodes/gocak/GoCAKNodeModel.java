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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.knip.base.data.img.ImgPlusCell;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.io.nodes.imgwriter.ImgWriter;

public class GoCAKNodeModel extends NodeModel {

	final static String[] FEATURE_NAMES = new String[] {
			"Fiber Key into CT Fire List", "End Point Row",
			"Fiber Absolute Angle", "Fiber Weight", "Total Length",
			"End to End Length", "Curvature", "Width", "Distance to Nearest 2",
			"Distance to Nearest 4", "Distance to Nearest 8",
			"Distance to Nearest 16", "Mean Nearest Distance",
			"Standard Deviation Nearest Distance", "Box Density 32",
			"Box Density 64", "Box Density 128", "Alginment of Nearest 2",
			"Alignment of Nearest 4", "Alignment of Nearest 8",
			"Alignment of Nearest 16", "Mean Nearest Alignment",
			" Standard Deviation Nearest Alignment", "Box Alignment 32",
			"Box Alignment 64", "Box Alignment 128",
			"Nearest Distance to Bound", "Inside EPI Region",
			"Nearest Relative Boundary Angle", "Extension Point Distance",
			"Extension Point Angle", "BOundary Point Row",
			"Boundary Point Column" };

	protected GoCAKNodeModel() {
		// first port contains images (TODO, IGNORED FOR NOW)
		// second port contains measurements of source images
		// third node contains per cell statistics
		super(1, 2);
	}

	private SettingsModelString m_imgColumnNameModel = createImgColumnSelectionModel();

	private SettingsModelString m_bitMaskColumNameModel = createBitMaskSelectionModel();

	private SettingsModelDouble m_distThresholdModel = createDistThresholdModel();

	private SettingsModelString m_fiberModeModel = createFiberModeModel();

	private SettingsModelString m_boundaryModeModel = createBoundaryModel();

	private SettingsModelDouble m_keepModel = createKeepModel();

	private SettingsModelBoolean m_makeAssocModel = createMakeAssocModel();

	private SettingsModelBoolean m_makeFeatModel = createMakeFeatModel();

	private SettingsModelBoolean m_makeOverModel = createMakeOverModel();

	private SettingsModelBoolean m_makeMapModel = createMakeMapModel();

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		return new DataTableSpec[] { new DataTableSpec(createFeatureSpec()),
				new DataTableSpec(createImgSpec()) };
	}

	private DataColumnSpec[] createImgSpec() {
		final DataColumnSpec[] spec = new DataColumnSpec[2];

		spec[0] = new DataColumnSpecCreator("Overlay", ImgPlusCell.TYPE)
				.createSpec();
		spec[1] = new DataColumnSpecCreator("ProcMap", ImgPlusCell.TYPE)
				.createSpec();

		return spec;
	}

	private DataColumnSpec[] createFeatureSpec() {
		DataColumnSpec[] spec = new DataColumnSpec[FEATURE_NAMES.length + 1];

		for (int i = 0; i < spec.length; i++) {
			spec[i] = new DataColumnSpecCreator(FEATURE_NAMES[i],
					DoubleCell.TYPE).createSpec();
		}

		spec[FEATURE_NAMES.length] = new DataColumnSpecCreator("Source Img",
				ImgPlusCell.TYPE).createSpec();

		return spec;
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {

		// create tmp dir
		String tmpDirPath = System.getProperty("java.io.tmpdir") + "/cp"
				+ System.currentTimeMillis() + "/";

		File tmpDir = new File(tmpDirPath);
		tmpDir.mkdir();

		// create temporary output directory
		File outDir = new File(tmpDirPath + "out");
		outDir.mkdir();

		// create temporary input directory
		File inDir = new File(tmpDirPath + "in");
		inDir.mkdir();

		// write images on disc and remember paths to them
		final String[] imgPaths = new String[inData[0].getRowCount()];

		// TODO idxImg and idxBitMask may not be the same
		int idxImg = inData[0].getDataTableSpec().findColumnIndex(
				m_imgColumnNameModel.getStringValue());
		int idxBitMask = inData[0].getDataTableSpec().findColumnIndex(
				m_bitMaskColumNameModel.getStringValue());

		final ImgWriter imgWriter = new ImgWriter();

		int idx = 0;
		final CloseableRowIterator it = inData[0].iterator();
		while (it.hasNext()) {
			DataRow next = it.next();
			final ImgPlusValue<?> value = (ImgPlusValue<?>) next
					.getCell(idxImg);
			final ImgPlusValue<?> moreValue = (ImgPlusValue<?>) next
					.getCell(idxBitMask);

			final int[] map = new int[value.getDimensions().length];

			for (int i = 0; i < map.length; i++) {
				map[i] = i;
			}

			final String imgName = next.getKey().getString() + ".tif";

			final String imgPath = inDir.getAbsolutePath() + "/" + imgName;

			final String bitMaskPath = "mask for " + imgName;

			imgWriter.writeImage(value.getImgPlus(), imgPath,
					"Tagged Image File Format (tif)", "Uncompressed", map);

			imgWriter.writeImage(moreValue.getImgPlus(), bitMaskPath,
					"Tagged Image File Format (tif)", "Uncompressed", map);

			imgPaths[++idx] = imgPath;

		}

		// read parameters
		final ParameterSet parameterSet = new ParameterSet(
				outDir.getAbsolutePath(),
				null,
				FiberModes.valueOfName(m_fiberModeModel.getStringValue()),
				BoundaryModes.valueOfName(m_boundaryModeModel.getStringValue()),
				m_keepModel.getDoubleValue(), m_distThresholdModel
						.getDoubleValue(), m_makeAssocModel.getBooleanValue(),
				m_makeFeatModel.getBooleanValue(), m_makeOverModel
						.getBooleanValue(), m_makeMapModel.getBooleanValue());

		// Create parameters file
		final File parameters = new File(inDir.getAbsolutePath()
				+ "/parameters.txt");
		parameters.createNewFile();

		BufferedDataContainer featureContainter = exec
				.createDataContainer(new DataTableSpec(createFeatureSpec()));

		BufferedDataContainer imgContainer = exec
				.createDataContainer(new DataTableSpec(createFeatureSpec()));

		idx = 0;
		final CloseableRowIterator it2 = inData[0].iterator();
		while (it2.hasNext()) {
			final DataRow next = it2.next();

			parameterSet.updateImgPath(imgPaths[idx]);
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					parameters));
			writer.append(parameterSet.createParameterString());
			writer.close();

			// run process on current file
			ProcessBuilder processBuilder = new ProcessBuilder(
					"/cmd goCAK.exe", parameters.getAbsolutePath());

			Process process = processBuilder.start();
			copy(process.getInputStream());
			process.waitFor();

			// Write results back. For now only CSV
			BufferedReader csvReader = new BufferedReader(new FileReader(
					new File(outDir.getAbsolutePath() + "/CA_Out/"
							+ imgPaths[idx] + "_fibFeatures.csv")));

			String line = null;
			int o = 0;
			while ((line = csvReader.readLine()) != null) {
				String[] row = line.split(",");
				DataCell[] cells = new DataCell[row.length + 1];
				for (int i = 0; i < row.length; i++) {
					cells[i] = new DoubleCell(Double.valueOf(row[i]));
				}

				cells[cells.length] = next.getCell(idxImg);
				featureContainter.addRowToTable(new DefaultRow(next.getKey()
						+ "#" + o, row));
			}

			idx++;
		}

		featureContainter.close();
		imgContainer.close();
		
		return new BufferedDataTable[] { featureContainter.getTable(),
				imgContainer.getTable() };
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_imgColumnNameModel.saveSettingsTo(settings);
		m_bitMaskColumNameModel.saveSettingsTo(settings);
		m_distThresholdModel.saveSettingsTo(settings);
		m_fiberModeModel.saveSettingsTo(settings);
		m_boundaryModeModel.saveSettingsTo(settings);
		m_keepModel.saveSettingsTo(settings);
		m_makeAssocModel.saveSettingsTo(settings);
		m_makeFeatModel.saveSettingsTo(settings);
		m_makeOverModel.saveSettingsTo(settings);
		m_makeMapModel.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_imgColumnNameModel.validateSettings(settings);
		m_bitMaskColumNameModel.validateSettings(settings);
		m_distThresholdModel.validateSettings(settings);
		m_fiberModeModel.validateSettings(settings);
		m_boundaryModeModel.validateSettings(settings);
		m_keepModel.validateSettings(settings);
		m_makeAssocModel.validateSettings(settings);
		m_makeFeatModel.validateSettings(settings);
		m_makeOverModel.validateSettings(settings);
		m_makeMapModel.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_imgColumnNameModel.loadSettingsFrom(settings);
		m_bitMaskColumNameModel.loadSettingsFrom(settings);
		m_distThresholdModel.loadSettingsFrom(settings);
		m_fiberModeModel.loadSettingsFrom(settings);
		m_boundaryModeModel.loadSettingsFrom(settings);
		m_keepModel.loadSettingsFrom(settings);
		m_makeAssocModel.loadSettingsFrom(settings);
		m_makeFeatModel.loadSettingsFrom(settings);
		m_makeOverModel.loadSettingsFrom(settings);
		m_makeMapModel.loadSettingsFrom(settings);
	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}

	static void copy(InputStream in) throws IOException {
		while (true) {
			int c = in.read();
			if (c == -1)
				break;
			Logger.getLogger(GoCAKNodeModel.class).info("GoCAK: " + (char) c);
		}
	}

	class ParameterSet {

		// TODO change later automatically by discovering OS
		private final int OS = 1;

		private final String outPath;

		private String imgPath;

		private final FiberModes fibMode;

		private final BoundaryModes boundaryMode;

		private final double keep;

		private final double distThreshold;

		private final boolean makeAssoc;

		private final boolean makeFeatFlag;

		private final boolean makeOverFlag;

		private final boolean makeMapFlag;

		private final String infoLabel = "delete later";

		public ParameterSet(final String outPath, final String imgPath,
				final FiberModes fibMode, final BoundaryModes boundaryMode,
				final double keep, final double distThreshold,
				final boolean makeAssoc, final boolean makeFeatFlag,
				final boolean makeOverFlag, final boolean makeMapFlag) {

			this.outPath = outPath;
			this.imgPath = imgPath;
			this.fibMode = fibMode;
			this.boundaryMode = boundaryMode;
			this.keep = keep;
			this.distThreshold = distThreshold;
			this.makeAssoc = makeAssoc;
			this.makeFeatFlag = makeFeatFlag;
			this.makeOverFlag = makeOverFlag;
			this.makeMapFlag = makeMapFlag;

		}

		public void updateImgPath(String imgPath) {
			this.imgPath = imgPath;
		}

		public String createParameterString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append(OS + "\n");
			buffer.append(outPath + "\n");
			buffer.append(imgPath + "\n");
			buffer.append(fibMode + "\n");
			buffer.append(boundaryMode + "\n");
			buffer.append(keep + "\n");
			buffer.append(distThreshold + "\n");
			buffer.append(makeAssoc + "\n");
			buffer.append(makeFeatFlag + "\n");
			buffer.append(makeOverFlag + "\n");
			buffer.append(makeMapFlag + "\n");
			buffer.append(infoLabel + "\n");

			return buffer.toString();
		}

	}

}
