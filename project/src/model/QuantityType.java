package model;

public enum QuantityType {
	UNITS("units", "", true),
	GRAMMES("g", "g", false),
	MILLILITRES("ml", "ml", false),
	PACKETS("packets", "pkts", false);
	
	public static QuantityType fromString(String text) {
		if (text != null) {
			for (QuantityType b : QuantityType.values()) {
				if (text.equalsIgnoreCase(b.getName())) {
					return b;
				}
			}
		}
		return null;
	}

	private String name;
	private String displayName;
	private boolean requiresPluralisation;
	
	QuantityType(String name, String displayName, boolean requiresPluralisation) {
		this.name = name;
		this.displayName = displayName;
		this.requiresPluralisation = requiresPluralisation;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean getRequiresPluralisation() {
		return requiresPluralisation;
	}
	 
	@Override
	public String toString() {
		return getName();
	}
}
