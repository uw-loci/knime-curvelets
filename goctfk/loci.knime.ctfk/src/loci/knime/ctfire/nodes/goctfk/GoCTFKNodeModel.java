
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
import io.scif.SCIFIO;
import io.scif.img.ImgSaver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.blob.BinaryObjectCellFactory;
import org.knime.core.data.blob.BinaryObjectDataCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortType;
import org.knime.knip.base.data.img.ImgPlusCell;
import org.knime.knip.base.data.img.ImgPlusCellFactory;
import org.knime.knip.base.data.img.ImgPlusValue;
import org.knime.knip.io.ScifioGateway;
import org.knime.knip.io.ScifioImgSource;

public class GoCTFKNodeModel extends NodeModel {

	private static String OS = System.getProperty("os.name").toLowerCase();

	final static String[] FEATURE_NAMES = new String[] { "FIBER_LENGTH",
		"FIBER_WIDTH", "FIBER_ANGLE", "FIBER_STRAIGHTNESS"
	// only first 4 supported right now
	// "BULK_ALIGNMENT", "FIBER_COUNT"
		};

	protected GoCTFKNodeModel() {
		// first port contains images
		// second port is path to MAT data file and is optional
		// third port contains output MAT data file
		// fourth node contains calculated features
		// fifth node contains overlay images
		super(
			new PortType[]{BufferedDataTable.TYPE, BufferedDataTable.TYPE_OPTIONAL},
			new PortType[]{
			BufferedDataTable.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE});
	}

	private SettingsModelString m_imgColumnNameModel =
			createImgColumnSelectionModel();
	private SettingsModelString m_matColumnNameModel =
			createMATColumnSelectionModel();
	private SettingsModelBoolean m_postProcessModel = createPostProcessModel();
	private SettingsModelDouble m_curveletCoefficientModel =
		createCurveletCoefficientModel();
	private SettingsModelInteger m_selectedScalesModel =
		createSelectedScalesModel();
	private SettingsModelInteger m_thresholdModel = createThresholdModel();
	private SettingsModelInteger m_xlinkBoxModel = createXLinkBoxModel();
	private SettingsModelInteger m_extendedAngleModel =
		createExtendedAngleModel();
	private SettingsModelInteger m_danglerLengthModel =
		createDanglerLengthModel();
	private SettingsModelInteger m_shortLengthModel = createShortLengthModel();
	private SettingsModelInteger m_nodeEndsModel = createNodeEndsModel();
	private SettingsModelInteger m_linkDistanceModel = createLinkDistanceModel();
	private SettingsModelInteger m_linkAngleModel = createLinkAngleModel();
	private SettingsModelInteger m_freeLengthModel = createFreeLengthModel();
	private SettingsModelBoolean m_widthCalcModel = createWidthCalcModel();
	private SettingsModelInteger m_widthMinMaxModel = createWidthMinMaxModel();
	private SettingsModelInteger m_minFiberModel = createMinFiberModel();
	private SettingsModelDouble m_widthCIModel = createWidthConfidenceModel();
	private SettingsModelBoolean m_calcMaxModel = createCalcMaxModel();
	private SettingsModelDouble m_fiberLineWidthModel =
		createFiberLineWidthModel();
	private SettingsModelInteger m_outputFiberLengthModel =
		createFiberLengthOutputModel();
	private SettingsModelInteger m_outputMaxFiberModel =
		createMaxFiberOutputModel();
	private SettingsModelInteger m_OutputResolutionModel =
		createOutputResolutionModel();
	private SettingsModelInteger m_OutputFiberWidthModel =
		createFiberWidthOutputModel();
	private SettingsModelBoolean m_OutputAngleModel = createOutputAngleModel();
	private SettingsModelBoolean m_OutputLengthModel = createOutputLengthModel();
	private SettingsModelBoolean m_OutputWidthModel = createOutputWidthModel();
	private SettingsModelBoolean m_OutputStraightnessModel =
		createOutputStraightnessModel();

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
		throws InvalidSettingsException
	{
		return new DataTableSpec[] { new DataTableSpec(createMATDataSpec()),
			new DataTableSpec(createCSVDataSpec()), new DataTableSpec(createOverlaySpec()) };
	}

	private DataColumnSpec[] createOverlaySpec() {
		final DataColumnSpec[] spec = new DataColumnSpec[3];

		spec[0] =
			new DataColumnSpecCreator("Source Image", ImgPlusCell.TYPE).createSpec();
		spec[1] =
			new DataColumnSpecCreator("Overlay", ImgPlusCell.TYPE).createSpec();
		spec[2] =
			new DataColumnSpecCreator("Reconstruction", ImgPlusCell.TYPE).createSpec();

		return spec;
	}

	private DataColumnSpec[] createCSVDataSpec() {

		final List<DataColumnSpec> csvColumns = new ArrayList<DataColumnSpec>();

		if (m_OutputAngleModel.getBooleanValue()) csvColumns
			.add(new DataColumnSpecCreator("Angle", DoubleCell.TYPE).createSpec());

		if (m_OutputLengthModel.getBooleanValue()) csvColumns
			.add(new DataColumnSpecCreator("Length", DoubleCell.TYPE).createSpec());

		if (m_OutputWidthModel.getBooleanValue()) csvColumns
			.add(new DataColumnSpecCreator("Width", DoubleCell.TYPE).createSpec());

		if (m_OutputStraightnessModel.getBooleanValue()) csvColumns
			.add(new DataColumnSpecCreator("Straightness", DoubleCell.TYPE).createSpec());

		return csvColumns.toArray(new DataColumnSpec[csvColumns.size()]);
	}

	private DataColumnSpec[] createMATDataSpec() {
		DataColumnSpec[] spec = new DataColumnSpec[1];

		spec[0] =
			new DataColumnSpecCreator("MAT Data", BinaryObjectDataCell.TYPE).createSpec();

		return spec;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
		ExecutionContext exec) throws Exception
	{

		// create tmp dir
		String tmpDirPath =
			System.getProperty("java.io.tmpdir") + "/goCTFK" +
				System.currentTimeMillis() + "/";

		File tmpDir = new File(tmpDirPath);
		tmpDir.mkdir();


		exec.setMessage("Preparing temporary files and parameters");

		int idxImg = inData[0].getDataTableSpec().findColumnIndex(
				m_imgColumnNameModel.getStringValue());

		BufferedDataContainer matContainer =
			exec.createDataContainer(new DataTableSpec(createMATDataSpec()));

		BufferedDataContainer csvContainer =
			exec.createDataContainer(new DataTableSpec(createCSVDataSpec()));

		BufferedDataContainer overlayContainer =
			exec.createDataContainer(new DataTableSpec(createOverlaySpec()));

		if (idxImg == -1) {
			setWarningMessage("WARNING: No image column set. Please configure this node before execution.");
			matContainer.close();
			csvContainer.close();
			overlayContainer.close();
			return new BufferedDataTable[] { matContainer.getTable(),
				csvContainer.getTable(), overlayContainer.getTable() };
		}

		final SCIFIO scifio = ScifioGateway.getSCIFIO();
		final ImgSaver imgSaver = new ImgSaver(scifio.context());
		final String[] imgNames = new String[inData[0].getRowCount()];

		// write images on disc and remember paths to them
		int idx = 0;
		final CloseableRowIterator it = inData[0].iterator();
		while (it.hasNext()) {
			DataRow next = it.next();
			final ImgPlusValue<?> value = (ImgPlusValue<?>) next.getCell(idxImg);

			final String imgName = next.getKey().getString() + ".tif";

			final String imgPath =
				tmpDir.getAbsolutePath() + File.separator + imgName;

			imgSaver.saveImg(imgPath, value.getImgPlus());

			imgNames[idx++] = imgName;
		}

		String[] matPath;

		// get MATLAB file path
		if (inData[1] != null) {
			matPath = new String[inData[1].getRowCount()];
			int matIndex =  inData[1].getDataTableSpec().findColumnIndex(
				m_matColumnNameModel.getStringValue());
			final CloseableRowIterator it2 = inData[1].iterator();
			int idx2 = 0;
			while (it2.hasNext()) {
				matPath[idx2++] =
					((StringValue) it2.next().getCell(matIndex)).getStringValue();
			}
			it2.close();
		}
		else {
			final URL url =
				new URL(
					"platform:/plugin/loci.knime.ctfire/res/goCTFK/ctfDEF.mat");

			final File matFile = new File(FileLocator.resolve(url).getFile());
			matPath = new String[] {matFile.getAbsolutePath()};
		}

		// read parameters
		final ParameterSet parameterSet =
			new ParameterSet(matPath[0],
				imgNames[0],
				tmpDir.getAbsolutePath(),
				m_postProcessModel.getBooleanValue(),
				m_curveletCoefficientModel.getDoubleValue(),
				m_selectedScalesModel.getIntValue(),
				m_thresholdModel.getIntValue(),
				m_xlinkBoxModel.getIntValue(),
				m_extendedAngleModel.getIntValue(),
				m_danglerLengthModel.getIntValue(),
				m_shortLengthModel.getIntValue(),
				m_nodeEndsModel.getIntValue(),
				m_linkDistanceModel.getIntValue(),
				m_linkAngleModel.getIntValue(),
				m_freeLengthModel.getIntValue(),
				m_widthCalcModel.getBooleanValue(),
				m_widthMinMaxModel.getIntValue(),
				m_minFiberModel.getIntValue(),
				m_widthCIModel.getDoubleValue(),
				m_calcMaxModel.getBooleanValue(),
				m_fiberLineWidthModel.getDoubleValue(),
				m_outputFiberLengthModel.getIntValue(),
				m_outputMaxFiberModel.getIntValue(),
				m_OutputResolutionModel.getIntValue(),
				m_OutputFiberWidthModel.getIntValue(),
				m_OutputAngleModel.getBooleanValue(),
				m_OutputLengthModel.getBooleanValue(),
				m_OutputWidthModel.getBooleanValue(),
				m_OutputStraightnessModel.getBooleanValue());

		// Create parameters file
		final File parameters =
			new File(tmpDir.getAbsolutePath() + "/parameters.txt");
		parameters.createNewFile();

		exec.setMessage("Processing image data in matlab.");
		try {
			idx = 0;
			final CloseableRowIterator it2 = inData[0].iterator();
			while (it2.hasNext()) {
				final DataRow next = it2.next();

				exec.setProgress((double) inData[0].getRowCount() / (double) idx);

				BufferedWriter writer = new BufferedWriter(new FileWriter(parameters));
				writer.append(parameterSet.createParameterString());
				writer.close();

				ProcessBuilder processBuilder =
					new ProcessBuilder(getGoCTFKPath(), parameters.getAbsolutePath());
				fillGoCTFKEnv(processBuilder.environment());

				Process process = processBuilder.start();
				copy(process.getErrorStream());
				process.waitFor();

				// Read in output files to populate KNIME tables

				final String outDir = tmpDir.getAbsolutePath() + File.separator + "ctFIREout" +
						File.separator;

				// -- Read .MAT data --

				final String matFilePath = outDir +
						"ctFIREout_" + imgNames[idx].replace(".tif", ".mat");
				final FileInputStream inputStream = FileUtils.openInputStream(new File(matFilePath));

				final BinaryObjectCellFactory boFac = new BinaryObjectCellFactory(exec);
				final DataCell matCell =
					boFac.create(new BufferedInputStream(inputStream));
				matContainer.addRowToTable(new DefaultRow("MAT data #" + idx, matCell));

				// -- Reading CSV data --

				// initialize enabled readers
				final List<BufferedReader> csvReaders = new ArrayList<BufferedReader>();
				if (m_OutputAngleModel.getBooleanValue()) csvReaders.add(new BufferedReader(new FileReader(
					new File(outDir + "HistANG_ctFIRE_" + imgNames[idx].replace(".tif", ".csv")))));

				if (m_OutputLengthModel.getBooleanValue()) csvReaders.add(new BufferedReader(new FileReader(
					new File(outDir + "HistLEN_ctFIRE_" + imgNames[idx].replace(".tif", ".csv")))));

				if (m_OutputWidthModel.getBooleanValue()) csvReaders.add(new BufferedReader(new FileReader(
					new File(outDir + "HistWID_ctFIRE_" + imgNames[idx].replace(".tif", ".csv")))));

				if (m_OutputStraightnessModel.getBooleanValue()) csvReaders.add(new BufferedReader(new FileReader(
					new File(outDir + "HistSTR_ctFIRE_" + imgNames[idx].replace(".tif", ".csv")))));

				int o = 0;
				boolean foundData = true;
				// Populate rows (one row = one fiber)
				while (foundData) {
					foundData = false;
					DataCell[] cells = new DataCell[csvReaders.size()];
					for (int i = 0; i < csvReaders.size(); i++) {
						final BufferedReader reader = csvReaders.get(i);
						final String line = reader.readLine();
						if (line == null) cells[i] = new DoubleCell(Double.NaN);
						else {
							foundData = true;
							cells[i] = new DoubleCell(Double.valueOf(line));
						}
					}

					if (foundData) csvContainer.addRowToTable(new DefaultRow("Fiber #" +
							++o, cells));
				}

				// Close the readers
				for (final BufferedReader reader : csvReaders) reader.close();

				// -- Reading Image data --

				final DataCell[] imgs = new DataCell[3];
				ScifioImgSource imgOpener = new ScifioImgSource();

				// Original
				imgs[0] = next.getCell(idxImg);

				// Overlay
				imgs[1] =
					new ImgPlusCellFactory(exec).createCell(imgOpener
						.getImg(outDir + "OL_ctFIRE_" + imgNames[idx], 0));

				imgOpener.close();
				imgOpener = new ScifioImgSource();

				// Reconstruction
				imgs[2] =
					new ImgPlusCellFactory(exec).createCell(imgOpener
						.getImg(outDir + "CTRimg_" + imgNames[idx], 0));

				overlayContainer.addRowToTable(new DefaultRow(next.getKey() + "#" + idx,
					imgs));

				idx++;
			}

		}
		finally {
			matContainer.close();
			csvContainer.close();
			overlayContainer.close();
			scifio.getContext().dispose();
			FileUtils.deleteDirectory(tmpDir);
		}

		return new BufferedDataTable[] { matContainer.getTable(),
			csvContainer.getTable(), overlayContainer.getTable() };
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
		throws IOException, CanceledExecutionException
	{
		// Nothing to do here
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
		throws IOException, CanceledExecutionException
	{
		// Nothing to do here
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {

		m_imgColumnNameModel.saveSettingsTo(settings);
		m_matColumnNameModel.saveSettingsTo(settings);
		m_postProcessModel.saveSettingsTo(settings);
		m_curveletCoefficientModel.saveSettingsTo(settings);
		m_selectedScalesModel.saveSettingsTo(settings);
		m_thresholdModel.saveSettingsTo(settings);
		m_xlinkBoxModel.saveSettingsTo(settings);
		m_extendedAngleModel.saveSettingsTo(settings);
		m_danglerLengthModel.saveSettingsTo(settings);
		m_shortLengthModel.saveSettingsTo(settings);
		m_nodeEndsModel.saveSettingsTo(settings);
		m_linkDistanceModel.saveSettingsTo(settings);
		m_linkAngleModel.saveSettingsTo(settings);
		m_freeLengthModel.saveSettingsTo(settings);
		m_widthCalcModel.saveSettingsTo(settings);
		m_widthMinMaxModel.saveSettingsTo(settings);
		m_minFiberModel.saveSettingsTo(settings);
		m_widthCIModel.saveSettingsTo(settings);
		m_calcMaxModel.saveSettingsTo(settings);
		m_fiberLineWidthModel.saveSettingsTo(settings);
		m_outputFiberLengthModel.saveSettingsTo(settings);
		m_outputMaxFiberModel.saveSettingsTo(settings);
		m_OutputResolutionModel.saveSettingsTo(settings);
		m_OutputFiberWidthModel.saveSettingsTo(settings);
		m_OutputAngleModel.saveSettingsTo(settings);
		m_OutputLengthModel.saveSettingsTo(settings);
		m_OutputWidthModel.saveSettingsTo(settings);
		m_OutputStraightnessModel.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
		throws InvalidSettingsException
	{
		m_imgColumnNameModel.validateSettings(settings);
		m_matColumnNameModel.validateSettings(settings);
		m_postProcessModel.validateSettings(settings);
		m_curveletCoefficientModel.validateSettings(settings);
		m_selectedScalesModel.validateSettings(settings);
		m_thresholdModel.validateSettings(settings);
		m_xlinkBoxModel.validateSettings(settings);
		m_extendedAngleModel.validateSettings(settings);
		m_danglerLengthModel.validateSettings(settings);
		m_shortLengthModel.validateSettings(settings);
		m_nodeEndsModel.validateSettings(settings);
		m_linkDistanceModel.validateSettings(settings);
		m_linkAngleModel.validateSettings(settings);
		m_freeLengthModel.validateSettings(settings);
		m_widthCalcModel.validateSettings(settings);
		m_widthMinMaxModel.validateSettings(settings);
		m_minFiberModel.validateSettings(settings);
		m_widthCIModel.validateSettings(settings);
		m_calcMaxModel.validateSettings(settings);
		m_fiberLineWidthModel.validateSettings(settings);
		m_outputFiberLengthModel.validateSettings(settings);
		m_outputMaxFiberModel.validateSettings(settings);
		m_OutputResolutionModel.validateSettings(settings);
		m_OutputFiberWidthModel.validateSettings(settings);
		m_OutputAngleModel.validateSettings(settings);
		m_OutputLengthModel.validateSettings(settings);
		m_OutputWidthModel.validateSettings(settings);
		m_OutputStraightnessModel.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
		throws InvalidSettingsException
	{
		m_imgColumnNameModel.loadSettingsFrom(settings);
		m_matColumnNameModel.loadSettingsFrom(settings);
		m_postProcessModel.loadSettingsFrom(settings);
		m_curveletCoefficientModel.loadSettingsFrom(settings);
		m_selectedScalesModel.loadSettingsFrom(settings);
		m_thresholdModel.loadSettingsFrom(settings);
		m_xlinkBoxModel.loadSettingsFrom(settings);
		m_extendedAngleModel.loadSettingsFrom(settings);
		m_danglerLengthModel.loadSettingsFrom(settings);
		m_shortLengthModel.loadSettingsFrom(settings);
		m_nodeEndsModel.loadSettingsFrom(settings);
		m_linkDistanceModel.loadSettingsFrom(settings);
		m_linkAngleModel.loadSettingsFrom(settings);
		m_freeLengthModel.loadSettingsFrom(settings);
		m_widthCalcModel.loadSettingsFrom(settings);
		m_widthMinMaxModel.loadSettingsFrom(settings);
		m_minFiberModel.loadSettingsFrom(settings);
		m_widthCIModel.loadSettingsFrom(settings);
		m_calcMaxModel.loadSettingsFrom(settings);
		m_fiberLineWidthModel.loadSettingsFrom(settings);
		m_outputFiberLengthModel.loadSettingsFrom(settings);
		m_outputMaxFiberModel.loadSettingsFrom(settings);
		m_OutputResolutionModel.loadSettingsFrom(settings);
		m_OutputFiberWidthModel.loadSettingsFrom(settings);
		m_OutputAngleModel.loadSettingsFrom(settings);
		m_OutputLengthModel.loadSettingsFrom(settings);
		m_OutputWidthModel.loadSettingsFrom(settings);
		m_OutputStraightnessModel.loadSettingsFrom(settings);
	}

	@Override
	protected void reset() {
		// Nothing to do
	}

	static void copy(InputStream in) throws IOException {
		StringBuffer buffer = new StringBuffer();
		while (true) {
			int c = in.read();
			if (c == -1) break;

			buffer.append((char) c);
		}

		NodeLogger.getLogger(GoCTFKNodeModel.class).warn(
			"MatLab GoCTFK: " + buffer.toString());
	}

	class ParameterSet {

		private String defaultMatFile;
		private String imageName;
		private String imagePath;

		private int postProcess; // false = process, true = post process

		// -- Fiber extraction parameters
		private double curveletCoefficientPercent;
		private int selectedScales;
		private int threshold; // grayscale intensity
		private int xlinkBox;
		private int extendAngle;
		private int danglerLengthThreshold;
		private int shortLengthThreshold;
		private int nodeEnds;
		private int linkDistanceThreshold;
		private int linkAngleThreshold;
		private int freeLengthThreshold;

		// -- Parameters for width calculation --
		private int widthCalc; // 1 = use all, 0 = use following parameters
		private int widthMinMax;
		private int minFiberPoints;
		private double confidenceInterval;
		private int calculateMax; // 0 = don't calculate, 1 = calculate

		// -- output image --

		private double fiberLineWidth; // (0, 4], drawing instructions
		private int fiberLengthThreshold; // Minimum length for fibers (in pixels)
		private int maxFiberCount; // Maximum number of fibers to draw in one image
		private int outputRes; // Output image resolution (dpi)
		private int maxFiberWidth; // Maximum width of a fiber (in pixels)

		// -- csv file output --
		private int outputAngle;
		private int outputLength;
		private int outputWidth;
		private int outputStraightness;

		public ParameterSet(final String defaultMatFile, final String imageName,
			final String imagePath, final boolean postProcess,
			final double curveletCoefficientPercent, final int selectedScales,
			final int threshold, final int xlinkBox, final int extendAngle,
			final int danglerLengthThreshold, final int shortLengthThreshold,
			final int nodeEnds, final int linkDistanceThreshold,
			final int linkAngleThreshold, final int freeLengthThreshold,
			final boolean widthCalc, final int widthMinMax, final int minFiberPoints,
			final double confidenceInterval, final boolean calculateMax,
			final double fiberLineWidth, final int fiberLengthThreshold,
			final int maxFiberCount, final int outputRes, final int maxFiberWidth,
			final boolean outputAngle, final boolean outputLength,
			final boolean outputWidth, final boolean outputStraightness)
		{
			this.defaultMatFile = defaultMatFile;
			this.imageName = imageName;
			this.imagePath = imagePath;
			this.postProcess = postProcess ? 1 : 0;
			this.curveletCoefficientPercent = curveletCoefficientPercent;
			this.selectedScales = selectedScales;
			this.threshold = threshold;
			this.xlinkBox = xlinkBox;
			this.extendAngle = extendAngle;
			this.danglerLengthThreshold = danglerLengthThreshold;
			this.shortLengthThreshold = shortLengthThreshold;
			this.nodeEnds = nodeEnds;
			this.linkDistanceThreshold = linkDistanceThreshold;
			this.linkAngleThreshold = linkAngleThreshold;
			this.freeLengthThreshold = freeLengthThreshold;
			this.widthCalc = widthCalc ? 1 : 0;
			this.widthMinMax = widthMinMax;
			this.minFiberPoints = minFiberPoints;
			this.confidenceInterval = confidenceInterval;
			this.calculateMax = calculateMax ? 1 : 0;
			this.fiberLineWidth = fiberLineWidth;
			this.fiberLengthThreshold = fiberLengthThreshold;
			this.maxFiberCount = maxFiberCount;
			this.outputRes = outputRes;
			this.maxFiberWidth = maxFiberWidth;
			this.outputAngle = outputAngle ? 1 : 0;
			this.outputLength = outputLength ? 1 : 0;
			this.outputWidth = outputWidth ? 1 : 0;
			this.outputStraightness = outputStraightness ? 1 : 0;
		}

		public String createParameterString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append(defaultMatFile + "\n");
			buffer.append(imagePath + "\n");
			buffer.append(imageName + "\n");
			buffer.append(postProcess + "\n");
			buffer.append(curveletCoefficientPercent + "\n");
			buffer.append(selectedScales + "\n");
			buffer.append(threshold + "\n");
			buffer.append(xlinkBox + "\n");
			buffer.append(extendAngle + "\n");
			buffer.append(danglerLengthThreshold + "\n");
			buffer.append(shortLengthThreshold + "\n");
			buffer.append(nodeEnds + "\n");
			buffer.append(linkDistanceThreshold + "\n");
			buffer.append(linkAngleThreshold + "\n");
			buffer.append(freeLengthThreshold + "\n");
			buffer.append(widthCalc + "\n");
			buffer.append(widthMinMax + "\n");
			buffer.append(minFiberPoints + "\n");
			buffer.append(confidenceInterval + "\n");
			buffer.append(calculateMax + "\n");
			buffer.append(fiberLineWidth + "\n");
			buffer.append(fiberLengthThreshold + "\n");
			buffer.append(maxFiberCount + "\n");
			buffer.append(outputRes + "\n");
			buffer.append(maxFiberWidth + "\n");
			buffer.append(outputAngle + "\n");
			buffer.append(outputLength + "\n");
			buffer.append(outputWidth + "\n");
			buffer.append(outputStraightness + "\n");

			return buffer.toString();
		}

	}

	/**
	 * Helper Function to resolve platform urls.
	 * 
	 * @return the eclipse path
	 */
	protected String getGoCTFKPath() {
		// TODO: Adapt for various platforms
		try {
			URL url = null;

			if (isWindows()) {
				url = new URL("platform:/plugin/loci.knime.ctfire/res/goCTFK/goCTFK.exe");

			}
			else if (isMac()) {
				url =
					new URL(
						"platform:/plugin/loci.knime.ctfire/res/goCTFK/goCTFK.app/Contents/MacOS/goCTFK");
			}

			final File exe = new File(FileLocator.resolve(url).getFile());
			return exe.getAbsolutePath();
		}
		catch (final IOException e) {
			return null;
		}
	}

	protected void fillGoCTFKEnv(Map<String, String> env) {
		if (isMac()) {
			try {
				// TODO make the MCR path an option in the dialog ?

				File mcr = new File("/Applications/MATLAB/MATLAB_Compiler_Runtime/");
				if (!mcr.exists()) throw new IllegalStateException(
					"No MATLAB Compiler Runtime found!");

				// Enter the version subdirectory, e.g. v716
				if (mcr.listFiles().length == 1) {
					mcr = mcr.listFiles()[0];
				}

				String cwd;
				cwd =
					new File(FileLocator.resolve(
						new URL("platform:/plugin/loci.knime.ctfire/res/goCTFK/")).getFile())
						.getAbsolutePath();

				final String mcrRoot = mcr.getAbsolutePath() + File.separator;
				env.put("DYLD_LIBRARY_PATH", cwd + ":" + mcrRoot + "runtime/maci64:" +
					mcrRoot + "bin/maci64:" + mcrRoot + "sys/os/maci64");
				env.put("XAPPLRESDIR", mcrRoot + "X11/app-defaults");

			}
			catch (IOException e) {
				NodeLogger.getLogger(GoCTFKNodeModel.class).warn("MatLab GoCTFK: Failed to find MCR:\n\t" + e.getMessage());
				return;
			}
		}
	}

	private static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	private static boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}
}
