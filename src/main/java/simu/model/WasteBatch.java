package simu.model;

import simu.framework.Clock;
import simu.framework.Trace;

/**
 * A batch of waste moving through the recycling facility
 * (this is the "customer" of the simulation model).
 */
public class WasteBatch {
	private static int nextId = 1;
	private static double totalTimeInSystem = 0;
	private static int completedBatches = 0;

	private final int id;
	private final WasteType type;
	private final double quantityKg;
	private final double arrivalTime;
	private double removalTime;
	private double queueEntryTime;   // when the batch joined the current station's queue

	public WasteBatch() {
		id = nextId++;
		type = WasteType.random();
		quantityKg = 100 + Math.random() * 400;   // 100 - 500 kg
		arrivalTime = Clock.getInstance().getTime();
		Trace.out(Trace.Level.INFO, String.format("New waste batch #%d arrived: %s, %.0f kg, at time %.2f",
				id, type, quantityKg, arrivalTime));
	}

	public int getId() { return id; }
	public WasteType getType() { return type; }
	public double getQuantityKg() { return quantityKg; }
	public double getArrivalTime() { return arrivalTime; }
	public double getRemovalTime() { return removalTime; }
	public void setRemovalTime(double removalTime) { this.removalTime = removalTime; }
	public double getQueueEntryTime() { return queueEntryTime; }
	public void setQueueEntryTime(double queueEntryTime) { this.queueEntryTime = queueEntryTime; }

	/** Prints the batch's journey and updates the mean time in the facility. */
	public void reportResults(String outcome) {
		double timeInSystem = removalTime - arrivalTime;
		totalTimeInSystem += timeInSystem;
		completedBatches++;
		Trace.out(Trace.Level.INFO, String.format("Batch #%d (%s, %.0f kg) %s at %.2f, time in facility %.2f",
				id, type, quantityKg, outcome, removalTime, timeInSystem));
		Trace.out(Trace.Level.INFO, String.format("Current mean time in facility: %.2f", getMeanTimeInSystem()));
	}

	public static double getMeanTimeInSystem() {
		return completedBatches == 0 ? 0 : totalTimeInSystem / completedBatches;
	}

	public static int getCompletedBatches() { return completedBatches; }

	/** Resets the static counters so a new simulation run starts from zero. */
	public static void reset() {
		nextId = 1;
		totalTimeInSystem = 0;
		completedBatches = 0;
	}
}
