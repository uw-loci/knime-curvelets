package loci.knime.curvlets.nodes.gocak;

public enum FiberModes {
	CT("CT"), CTFS("CT-Fire Segments"), CTFF("CT-Fire Fibers"), CTFE(
		"CT-Fire Endpoints");

	private String name;

	private FiberModes(final String name) {
		this.name = name;
	}

	public static int ordinalForName(String name) {
		for (FiberModes mode : values()) {
			if (mode.getName().equals(name)) {
				return mode.ordinal();
			}
		}

		return -1;
	}

	public static FiberModes valueOfName(final String name) {
		return values()[ordinalForName(name)];
	}

	public String getName() {
		return name;
	}

	public static String[] names() {
		final FiberModes[] values = values();
		final String[] names = new String[values.length];
		for (int i = 0; i < values.length; ++i) {
			names[i] = values[i].getName();
		}

		return names;
	}
}