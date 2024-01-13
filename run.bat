@echo off
prompt $g$g
taskkill /F /IM javaw.exe > nul
@echo on
javac engine/utils/Tuple.java
javac engine/utils/MinesweeperDifficulty.java
javac -cp .;lanterna-3.1.1.jar engine/utils/Utils.java
javac -cp .;lanterna-3.1.1.jar engine/utils/Constants.java
javac -cp .;lanterna-3.1.1.jar engine/utils/MinesweeperDifficulty.java
javac -cp .;lanterna-3.1.1.jar engine/Leaderboard.java
javac -cp .;lanterna-3.1.1.jar engine/Minesweeper.java
::javac -cp .;lanterna-3.1.1.jar Program.java <-- not necessary
javaw -cp .;lanterna-3.1.1.jar Program.java
