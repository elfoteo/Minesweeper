@echo off
prompt $g$g$s
taskkill /F /IM javaw.exe > nul
@echo on
::Basic records
javac engine/utils/Tuple.java
:: Enums
javac engine/utils/MinesweeperDifficulty.java
javac engine/utils/CellType.java
:: Utilities
javac -cp .;lanterna-3.1.1.jar engine/utils/Utils.java
javac -cp .;lanterna-3.1.1.jar engine/utils/Constants.java
javac -cp .;lanterna-3.1.1.jar;json-java.jar engine/utils/Cell.java
:: Options
javac engine/options/OptionsInstance.java
javac -cp .;json-java.jar engine/options/Options.java
:: Skins
javac -cp .;lanterna-3.1.1.jar engine/skins/SkinManager.java
javac -cp .;lanterna-3.1.1.jar engine/skins/Skin.java
javac -cp .;lanterna-3.1.1.jar engine/skins/impl/DefaultSkin.java
javac -cp .;lanterna-3.1.1.jar engine/skins/impl/MoneySkin.java
javac -cp .;lanterna-3.1.1.jar engine/skins/impl/HeartsSkin.java
javac -cp .;lanterna-3.1.1.jar engine/skins/impl/MysterySkin.java
:: Themes
javac -cp .;lanterna-3.1.1.jar engine/themes/ThemeManager.java
javac -cp .;lanterna-3.1.1.jar engine/themes/GameTheme.java
javac -cp .;lanterna-3.1.1.jar engine/themes/impl/DefaultGameTheme.java
javac -cp .;lanterna-3.1.1.jar engine/themes/impl/PurpleGameTheme.java
:: Engine
javac -cp .;lanterna-3.1.1.jar;json-java.jar engine/Leaderboard.java
javac -cp .;lanterna-3.1.1.jar engine/Minesweeper.java
javac -cp .;lanterna-3.1.1.jar engine/UIManager.java
javac -cp .;lanterna-3.1.1.jar engine/Game.java
:: Main
::javac -cp .;lanterna-3.1.1.jar Program.java <-- not necessary
::The main program needs to be executed with all the jars else it won't work
javaw -cp .;lanterna-3.1.1.jar;json-java.jar Program.java
