package simu.model;

import simu.framework.IEventType;

/**
 * Event types of the Smart Recycling Facility model.
 * ARRIVAL = a new waste batch arrives at the facility.
 * Each DEP_ event = a batch finishes service at that station.
 */
public enum EventType implements IEventType {
	ARRIVAL,          // new waste batch arrives
	DEP_RECEPTION,    // reception & weighing finished
	DEP_SORTING,      // sorting finished (batch is routed or rejected)
	DEP_PROCESSING,   // material processing finished
	DEP_DISPATCH;     // storage & dispatch finished, batch leaves the facility
}
