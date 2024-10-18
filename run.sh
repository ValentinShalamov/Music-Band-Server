#run
./clean.sh
./build.sh
java -cp lib/gson-2.11.0.jar:lib/postgresql-42.7.4.jar:lib/commons-codec-1.17.1.jar:build/app.jar Main
