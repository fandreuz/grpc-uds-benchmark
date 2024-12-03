# grpc-uds-benchmark

Benchmark gRPC over UDS vs TCP/IP

## Running the benchmark

There's some plumbing needed due to the fact the JMH code needs to spin up a separated JVM for the gRPC server, in
addition to the forked subprocess which holds the gRPC client and the JMH benchmark. To make sure everything is in
place, use the following script:

```bash
./benchmark_run.sh
```
