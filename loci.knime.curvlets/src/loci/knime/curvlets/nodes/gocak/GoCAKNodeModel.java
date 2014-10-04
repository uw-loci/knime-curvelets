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
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
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
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.knip.base.data.img.ImgPlusCell;
import org.knime.knip.base.data.img.ImgPlusCellFactory;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.io.ScifioImgSource;
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
		// first port contains images 
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
		final DataColumnSpec[] spec = new DataColumnSpec[4];

		spec[0] = new DataColumnSpecCreator("Source Image", ImgPlusCell.TYPE)
				.createSpec();
		spec[1] = new DataColumnSpecCreator("Overlay", ImgPlusCell.TYPE)
				.createSpec();
		spec[2] = new DataColumnSpecCreator("ProcMap", ImgPlusCell.TYPE)
				.createSpec();
		spec[3] = new DataColumnSpecCreator("Reconstructed", ImgPlusCell.TYPE)
				.createSpec();

		return spec;
	}

	private DataColumnSpec[] createFeatureSpec() {
		DataColumnSpec[] spec = new DataColumnSpec[FEATURE_NAMES.length + 1];

		spec[0] = new DataColumnSpecCreator("Source Image", ImgPlusCell.TYPE)
				.createSpec();

		for (int i = 1; i <= FEATURE_NAMES.length; i++) {
			spec[i] = new DataColumnSpecCreator(FEATURE_NAMES[i - 1],
					DoubleCell.TYPE).createSpec();
		}

		return spec;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {

		// create tmp dir
		String tmpDirPath = System.getProperty("java.io.tmpdir") + "/goCAK"
				+ System.currentTimeMillis() + "/";

		File tmpDir = new File(tmpDirPath);
		tmpDir.mkdir();

		// create temporary output directory

		// write images on disc and remember paths to them

		exec.setMessage("Preparing temporary files and parameters");
		
		// TODO idxImg and idxBitMask may not be the same
		int idxImg = inData[0].getDataTableSpec().findColumnIndex(
				m_imgColumnNameModel.getStringValue());
		int idxBitMask = inData[0].getDataTableSpec().findColumnIndex(
				m_bitMaskColumNameModel.getStringValue());

		if (idxImg == idxBitMask) {
			throw new IllegalArgumentException(
					"Image and BitMask Column may not be the same");
		}

		final ImgWriter imgWriter = new ImgWriter();
		final String[] imgNames = new String[inData[0].getRowCount()];

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

			final String imgPath = tmpDir.getAbsolutePath() + "\\" + imgName;

			final String bitMaskPath = tmpDir.getAbsolutePath() + "\\"
					+ "mask for " + imgName + ".tif";

			imgWriter.writeImage(value.getImgPlus(), imgPath,
					"Tagged Image File Format (tif)", "Uncompressed", map);

			imgWriter.writeImage(moreValue.getImgPlus(), bitMaskPath,
					"Tagged Image File Format (tif)", "Uncompressed", map);

			imgNames[idx++] = imgName;
		}

		// read parameters
		final ParameterSet parameterSet = new ParameterSet(
				tmpDir.getAbsolutePath(),
				null,
				FiberModes.valueOfName(m_fiberModeModel.getStringValue()),
				BoundaryModes.valueOfName(m_boundaryModeModel.getStringValue()),
				m_keepModel.getDoubleValue(), m_distThresholdModel
						.getDoubleValue(), m_makeAssocModel.getBooleanValue(),
				m_makeFeatModel.getBooleanValue(), m_makeOverModel
						.getBooleanValue(), m_makeMapModel.getBooleanValue());

		// Create parameters file
		final File parameters = new File(tmpDir.getAbsolutePath()
				+ "/parameters.txt");
		parameters.createNewFile();

		BufferedDataContainer featureContainter = exec
				.createDataContainer(new DataTableSpec(createFeatureSpec()));

		BufferedDataContainer imgContainer = exec
				.createDataContainer(new DataTableSpec(createImgSpec()));
		
		exec.setMessage("Processing image data in matlab.");
		try {
			idx = 0;
			final CloseableRowIterator it2 = inData[0].iterator();
			while (it2.hasNext()) {
				final DataRow next = it2.next();

				exec.setProgress((double) inData[0].getRowCount()
						/ (double) idx);

				parameterSet.updateImgPath(imgNames[idx]);
				BufferedWriter writer = new BufferedWriter(new FileWriter(
						parameters));
				writer.append(parameterSet.createParameterString());
				writer.close();

				ProcessBuilder processBuilder = new ProcessBuilder(
						getGoCAKPath(), parameters.getAbsolutePath());

				Process process = processBuilder.start();
				copy(process.getErrorStream());
				process.waitFor();

				// Write results back. For now only CSV
				BufferedReader csvReader = new BufferedReader(new FileReader(
						new File(tmpDir.getAbsolutePath() + "/CA_Out/"
								+ imgNames[idx].replace(".tif", "")
								+ "_fibFeatures.csv")));

				String line = null;
				int o = 0;
				while ((line = csvReader.readLine()) != null) {
					String[] entries = line.split(",");
					DataCell[] cells = new DataCell[entries.length + 1];
					for (int i = 1; i <= entries.length; i++) {
						cells[i] = new DoubleCell(
								Double.valueOf(entries[i - 1]));
					}

					cells[0] = next.getCell(idxImg);

					featureContainter.addRowToTable(new DefaultRow(next
							.getKey() + "#" + o++, cells));
				}

				csvReader.close();

				// read created images
				final DataCell[] imgs = new DataCell[4];
				final ScifioImgSource imgOpener = new ScifioImgSource();

				imgs[0] = next.getCell(idxImg);

				imgs[1] = new ImgPlusCellFactory(exec).createCell(imgOpener
						.getImg(tmpDir.getAbsolutePath() + "/CA_Out/"
								+ imgNames[idx].replace(".tif", "")
								+ "_overlay.tiff", 0));

				imgs[2] = new ImgPlusCellFactory(exec).createCell(imgOpener
						.getImg(tmpDir.getAbsolutePath() + "/CA_Out/"
								+ imgNames[idx].replace(".tif", "")
								+ "_procmap.tiff", 0));

				imgs[3] = new ImgPlusCellFactory(exec).createCell(imgOpener
						.getImg(tmpDir.getAbsolutePath() + "/CA_Out/"
								+ imgNames[idx].replace(".tif", "")
								+ "_reconstructed.tiff", 0));

				imgOpener.close();

				imgContainer.addRowToTable(new DefaultRow(next.getKey() + "#"
						+ idx, imgs));

				idx++;
			}

		} finally {
			FileUtils.deleteDirectory(tmpDir);
			featureContainter.close();
			imgContainer.close();
		}

		return new BufferedDataTable[] { featureContainter.getTable(),
				imgContainer.getTable() };
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Nothing to do here
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Nothing to do here
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
		StringBuffer buffer = new StringBuffer();
		while (true) {
			int c = in.read();
			if (c == -1)
				break;

			buffer.append((char) c);
		}

		NodeLogger.getLogger(GoCAKNodeModel.class).warn(
				"MatLab GoCAK: " + buffer.toString());
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

		private final double makeAssoc;

		private final double makeFeatFlag;

		private final double makeOverFlag;

		private final double makeMapFlag;

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
			this.makeAssoc = makeAssoc ? 1.0 : 0.0;
			this.makeFeatFlag = makeFeatFlag ? 1.0 : 0.0;
			this.makeOverFlag = makeOverFlag ? 1.0 : 0.0;
			this.makeMapFlag = makeMapFlag ? 1.0 : 0.0;

		}

		public void updateImgPath(String imgPath) {
			this.imgPath = imgPath;
		}

		public String createParameterString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append(OS + "\n");
			buffer.append(outPath + "\n");
			buffer.append(imgPath + "\n");
			buffer.append((double) fibMode.ordinal() + "\n");
			buffer.append((double) boundaryMode.ordinal() + "\n");
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

	/**
	 * Helper Function to resolve platform urls.
	 * 
	 * @param _platformurl
	 * @return the eclipse path
	 */
	protected String getGoCAKPath() {
		// TODO: Adapt for various platforms
		try {
			final URL url = new URL(
					"platform:/plugin/loci.knime.curvlets/res/goCAK/goCAK.exe");
			final File exe = new File(FileLocator.resolve(url).getFile());
			return exe.getAbsolutePath();
		} catch (final IOException e) {
			return null;
		}
	}

}
