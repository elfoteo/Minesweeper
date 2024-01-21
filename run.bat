@echo off
prompt $g$g$s
taskkill /F /IM javaw.exe > nul
@echo on

:: Create a directory for compiled classes if it doesn't exist
mkdir out

:: Basic records
javac -d out engine/utils/Tuple.java

:: Enums
javac -d out engine/utils/MinesweeperDifficulty.java
javac -d out engine/utils/CellType.java

:: Utilities
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/utils/Utils.java
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/utils/Constants.java
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/utils/Cell.java
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/utils/LocalLeaderboardAPI.java

:: Options
javac -d out engine/options/OptionsInstance.java
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/options/Options.java

:: Skins
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/skins/SkinManager.java
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/skins/ISkin.java
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/skins/impl/DefaultSkin.java
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/skins/impl/MoneySkin.java
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/skins/impl/HeartsSkin.java
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/skins/impl/MysterySkin.java

:: Themes
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/themes/ThemeManager.java
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/themes/IGameTheme.java
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/themes/impl/DefaultGameTheme.java
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/themes/impl/PurpleGameTheme.java
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/themes/impl/LimeGameTheme.java
:: Engine
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/Leaderboard.java
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/Minesweeper.java
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/UIManager.java
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar engine/Game.java

:: Main
javac -d out -cp .;lanterna-3.1.1.jar;json-java.jar Program.java

:: Old version (javaw)
:: The main program needs to be executed with all the jars else it won't work
:: javaw -cp .;lanterna-3.1.1.jar;json-java.jar;out Program

:: New version (jar)
:: Create a JAR file
jar cfm Minesweeper.jar Manifest.txt -C out .

:: Run the program using the JAR file
javaw -jar Minesweeper.jar