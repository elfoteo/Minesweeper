@echo off
prompt $g$g$s
taskkill /F /IM javaw.exe > nul
@echo on
javac engine/utils/Tuple.java
javac engine/utils/MinesweeperDifficulty.java
javac -cp .;lanterna-3.1.1.jar engine/utils/Utils.java
javac -cp .;lanterna-3.1.1.jar engine/utils/Constants.java
javac -cp .;lanterna-3.1.1.jar engine/utils/MinesweeperDifficulty.java
javac -cp .;lanterna-3.1.1.jar;json-java.jar engine/Leaderboard.java
javac -cp .;lanterna-3.1.1.jar engine/Minesweeper.java
javac -cp .;lanterna-3.1.1.jar engine/UIManager.java
javac -cp .;lanterna-3.1.1.jar engine/Game.java
::javac -cp .;lanterna-3.1.1.jar Program.java <-- not necessary
::The main program needs to be compiled with all the jars else it won't work
javaw -cp .;lanterna-3.1.1.jar;json-java.jar Program.java
