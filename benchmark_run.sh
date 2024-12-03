#!/usr/bin/env bash

set -uox

script_dir=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &>/dev/null && pwd)

benchmark_stamp=$(date +'%Y.%m.%d_%H.%M.%S')
benchmark_dir=benchmark_run_${benchmark_stamp}
rm -rf "$benchmark_dir"
mkdir "$benchmark_dir"

# Build JMH driver and gRPC Java server
./gradlew distZip jmhJar
cd "$benchmark_dir" || exit
unzip ../app/build/distributions/app.zip
cp ../app/build/libs/app-jmh.jar .

# Setup Python environment
cp -r ../python_server .
python -m venv --upgrade-deps --clear venv
source venv/bin/activate
python -m pip install --requirement python_server/requirements.txt

# Compile .proto files
cd python_server || exit
python -m grpc_tools.protoc \
	-I "${script_dir}/app/src/main/proto/com/fandreuz/grpc/uds/benchmark/echo" \
	--python_out=. \
	--pyi_out=. \
	--grpc_python_out=. \
	"${script_dir}/app/src/main/proto/com/fandreuz/grpc/uds/benchmark/echo/helloworld.proto"
cd .. || exit

$JAVA_HOME/bin/java -jar app-jmh.jar -rf json
