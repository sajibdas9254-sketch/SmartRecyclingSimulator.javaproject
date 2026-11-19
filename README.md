# Smart Recycling Facility Simulator

Programming Project (TX00EY22-3015), Metropolia UAS.

Discrete-event simulation of a recycling facility, built on the course simulator framework.
Only the `simu.model` package has been changed; `simu.framework`, `controller`, `view` and `eduni` are the course originals.

## Model (first version)

```
Waste batch arrives -> Reception & Weighing -> Sorting
      -> CONTAMINATED -> Reject area (leaves)
      -> other types  -> Processing -> Storage & Dispatch -> leaves
```

- `WasteBatch` – the "customer": waste type, quantity (kg), arrival/removal time
- `WasteType` – PLASTIC, PAPER, GLASS, METAL, CONTAMINATED with arrival shares
- `ServicePoint` – station with FIFO queue and statistics (served, utilization, waiting time, max queue)
- `MyEngine` – creates the 4 stations, routes batches and prints results at the end
- `EventType` – ARRIVAL, DEP_RECEPTION, DEP_SORTING, DEP_PROCESSING, DEP_DISPATCH

## Run

Open as a Maven project in IntelliJ and run `Main`. Enter a simulation time (e.g. 1000) and delay (e.g. 50), then press **Start simulation**. Batch movements and the final statistics are printed to the console.
