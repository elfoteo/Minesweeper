@echo off
prompt $g$g
taskkill /F /IM javaw.exe > nul
@echo on
javac Tuple.java
javac MinesweeperDifficulty.java
javac -cp .;lanterna-3.1.1.jar Utils.java
javac -cp .;lanterna-3.1.1.jar Constants.java
javac -cp .;lanterna-3.1.1.jar MinesweeperDifficulty.java
::javac -cp .;lanterna-3.1.1.jar Program.java <-- not necessary
javaw -cp .;lanterna-3.1.1.jar Program.java
