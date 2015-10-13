package loci.knime.curvlets.nodes.gocak;

public enum BoundaryModes {
	NB("No Boundary"), DB("Draw Boundary"), CSV("CSV"), TIFF("TIFF Boundary");

	private String name;

	private BoundaryModes(final String name) {
		this.name = name;
	}

	public static int ordinalForName(String name) {
		for (BoundaryModes mode : values()) {
			if (mode.getName().equals(name)) {
				return mode.ordinal();
			}
		}

		return -1;
	}

	public String getName() {
		return name;
	}

	public static BoundaryModes valueOfName(final String name) {
		return values()[ordinalForName(name)];
	}

	public static String[] names() {
		final BoundaryModes[] values = values();
		final String[] names = new String[values.length];
		for (int i = 0; i < values.length; ++i) {
			names[i] = values[i].getName();
		}

		return names;
	}
};