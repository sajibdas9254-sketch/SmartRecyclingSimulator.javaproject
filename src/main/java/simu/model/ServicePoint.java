package simu.model;

import eduni.distributions.ContinuousGenerator;
import simu.framework.Clock;
import simu.framework.Event;
import simu.framework.EventList;
import simu.framework.Trace;

import java.util.LinkedList;

/**
 * A station in the recycling facility (reception, sorting, processing, dispatch).
 * Keeps a FIFO queue of waste batches and collects statistics:
 * served batches, busy time (utilization) and waiting time.
 */
public class ServicePoint {
	private final LinkedList<WasteBatch> queue = new LinkedList<>(); // first batch in the queue is the one in service
	private final ContinuousGenerator generator;
	private final EventList eventList;
	private final EventType eventTypeScheduled;
	private final String name;
	private boolean reserved = false;

	// statistics
	private int servedCount = 0;
	private double busyTime = 0;
	private double serviceStartTime = 0;
	private double totalWaitingTime = 0;
	private int maxQueueLength = 0;

	public ServicePoint(String name, ContinuousGenerator generator, EventList eventList, EventType type) {
		this.name = name;
		this.generator = generator;
		this.eventList = eventList;
		this.eventTypeScheduled = type;
	}

	public void addQueue(WasteBatch batch) {
		batch.setQueueEntryTime(Clock.getInstance().getTime());
		queue.add(batch);
		maxQueueLength = Math.max(maxQueueLength, queue.size());
	}

	/** Removes the batch that has finished service. */
	public WasteBatch removeQueue() {
		reserved = false;
		busyTime += Clock.getInstance().getTime() - serviceStartTime;
		servedCount++;
		return queue.poll();
	}

	/** Starts serving the first batch in the queue and schedules its departure event. */
	public void beginService() {
		reserved = true;
		double now = Clock.getInstance().getTime();
		serviceStartTime = now;
		WasteBatch batch = queue.peek();
		totalWaitingTime += now - batch.getQueueEntryTime();
		double serviceTime = Math.max(0.1, generator.sample()); // no negative service times from Normal
		Trace.out(Trace.Level.INFO, String.format("%s starts batch #%d (%s), service time %.2f",
				name, batch.getId(), batch.getType(), serviceTime));
		eventList.add(new Event(eventTypeScheduled, now + serviceTime));
	}

	public boolean isReserved() { return reserved; }
	public boolean isOnQueue() { return !queue.isEmpty(); }

	public String getName() { return name; }
	public int getServedCount() { return servedCount; }
	public int getQueueLength() { return queue.size(); }
	public int getMaxQueueLength() { return maxQueueLength; }

	public double getUtilization(double simulationTime) {
		return simulationTime <= 0 ? 0 : busyTime / simulationTime;
	}

	public double getAverageWaitingTime() {
		return servedCount == 0 ? 0 : totalWaitingTime / servedCount;
	}
}
