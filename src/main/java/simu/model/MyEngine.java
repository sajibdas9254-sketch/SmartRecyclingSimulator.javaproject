package simu.model;

import controller.IControllerMtoV;
import eduni.distributions.Negexp;
import eduni.distributions.Normal;
import simu.framework.ArrivalProcess;
import simu.framework.Clock;
import simu.framework.Engine;
import simu.framework.Event;

import java.util.EnumMap;
import java.util.Map;

/**
 * Smart Recycling Facility model.
 *
 * Flow of a waste batch:
 *   Arrival -> Reception & Weighing -> Sorting
 *        -> CONTAMINATED batches go to the reject area and leave
 *        -> other batches -> Processing -> Storage & Dispatch -> leave
 */
public class MyEngine extends Engine {
	private static final int RECEPTION = 0;
	private static final int SORTING = 1;
	private static final int PROCESSING = 2;
	private static final int DISPATCH = 3;

	private final ArrivalProcess arrivalProcess;

	// results
	private int arrivedBatches = 0;
	private int dispatchedBatches = 0;
	private int rejectedBatches = 0;
	private double dispatchedKg = 0;
	private double rejectedKg = 0;
	private final Map<WasteType, Integer> dispatchedByType = new EnumMap<>(WasteType.class);

	public MyEngine(IControllerMtoV controller) {
		super(controller);
		WasteBatch.reset();

		servicePoints = new ServicePoint[4];
		servicePoints[RECEPTION]  = new ServicePoint("Reception & Weighing", new Normal(5, 2),  eventList, EventType.DEP_RECEPTION);
		servicePoints[SORTING]    = new ServicePoint("Sorting",              new Normal(8, 3),  eventList, EventType.DEP_SORTING);
		servicePoints[PROCESSING] = new ServicePoint("Processing",           new Normal(10, 4), eventList, EventType.DEP_PROCESSING);
		servicePoints[DISPATCH]   = new ServicePoint("Storage & Dispatch",   new Normal(6, 2),  eventList, EventType.DEP_DISPATCH);

		// average time between waste batch arrivals: 12 time units
		arrivalProcess = new ArrivalProcess(new Negexp(12, 5), eventList, EventType.ARRIVAL);

		for (WasteType type : WasteType.values()) {
			dispatchedByType.put(type, 0);
		}
	}

	@Override
	protected void initialization() {
		arrivalProcess.generateNext(); // first waste batch arrival
	}

	@Override
	protected void runEvent(Event t) { // B-phase events
		WasteBatch batch;
		double now = Clock.getInstance().getTime();

		switch ((EventType) t.getType()) {
			case ARRIVAL:
				servicePoints[RECEPTION].addQueue(new WasteBatch());
				arrivedBatches++;
				arrivalProcess.generateNext();
				controller.visualiseCustomer();
				break;

			case DEP_RECEPTION:
				batch = servicePoints[RECEPTION].removeQueue();
				servicePoints[SORTING].addQueue(batch);
				break;

			case DEP_SORTING:
				batch = servicePoints[SORTING].removeQueue();
				if (batch.getType() == WasteType.CONTAMINATED) {
					// contaminated waste goes to the reject area and leaves the process
					batch.setRemovalTime(now);
					rejectedBatches++;
					rejectedKg += batch.getQuantityKg();
					batch.reportResults("REJECTED (contaminated)");
				} else {
					servicePoints[PROCESSING].addQueue(batch);
				}
				break;

			case DEP_PROCESSING:
				batch = servicePoints[PROCESSING].removeQueue();
				servicePoints[DISPATCH].addQueue(batch);
				break;

			case DEP_DISPATCH:
				batch = servicePoints[DISPATCH].removeQueue();
				batch.setRemovalTime(now);
				dispatchedBatches++;
				dispatchedKg += batch.getQuantityKg();
				dispatchedByType.merge(batch.getType(), 1, Integer::sum);
				batch.reportResults("DISPATCHED");
				break;
		}
	}

	@Override
	protected void results() {
		double simTime = Clock.getInstance().getTime();

		System.out.println("\n========== SMART RECYCLING FACILITY - RESULTS ==========");
		System.out.printf("Simulation ended at time %.2f%n", simTime);
		System.out.println("Batches arrived:     " + arrivedBatches);
		System.out.printf("Batches dispatched:  %d (%.0f kg)%n", dispatchedBatches, dispatchedKg);
		System.out.printf("Batches rejected:    %d (%.0f kg)%n", rejectedBatches, rejectedKg);
		System.out.println("Dispatched by material: " + dispatchedByType);
		System.out.printf("Mean time in facility: %.2f%n", WasteBatch.getMeanTimeInSystem());
		System.out.println("\nStation statistics:");
		for (ServicePoint p : servicePoints) {
			System.out.printf("  %-22s served %3d | utilization %5.1f %% | avg wait %6.2f | max queue %2d | in queue now %2d%n",
					p.getName(), p.getServedCount(), p.getUtilization(simTime) * 100,
					p.getAverageWaitingTime(), p.getMaxQueueLength(), p.getQueueLength());
		}
		System.out.println("=========================================================");

		controller.showEndTime(simTime);
	}
}
