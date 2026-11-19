package simu.model;

/**
 * Material categories of incoming waste batches.
 * Each type has a share (probability) of all arriving batches.
 */
public enum WasteType {
	PLASTIC(0.30),
	PAPER(0.25),
	GLASS(0.15),
	METAL(0.15),
	CONTAMINATED(0.15);

	private final double share;

	WasteType(double share) {
		this.share = share;
	}

	public double getShare() {
		return share;
	}

	/** Picks a random waste type according to the shares above. */
	public static WasteType random() {
		double r = Math.random();
		double cumulative = 0;
		for (WasteType type : values()) {
			cumulative += type.share;
			if (r < cumulative) {
				return type;
			}
		}
		return PLASTIC;
	}
}
