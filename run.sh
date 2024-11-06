#run
./clean.sh
./build.sh
java -cp lib/gson-2.11.0.jar:lib/postgresql-42.7.4.jar:lib/commons-codec-1.17.1.jar:lib/commons-logging-1.3.4.jar:lib/commons-dbcp2-2.12.0.jar:lib/commons-pool2-2.12.0.jar:build/app.jar Main
