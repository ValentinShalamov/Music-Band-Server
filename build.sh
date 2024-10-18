#Create jar-file
javac -d build/compile -cp lib/gson-2.11.0.jar:lib/postgresql-42.7.4.jar:lib/commons-codec-1.17.1.jar:src/main/java src/main/java/Main.java
cd build/compile
jar cfe ../app.jar Main ./*/*.class Main.class
