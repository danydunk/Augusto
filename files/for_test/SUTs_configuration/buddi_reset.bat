kkill.exe /F /IM firefox.exe
Taskkill.exe /F /IM iexplore.exe
del C:\Users\usi\AppData\Roaming\Buddi\Buddi3_Prefs.xml
copy C:\workspace\GUISemantics_RFT\files\for_test\SUTs_configuration\Buddi3_Prefs.xml C:\Users\usi\AppData\Roaming\Buddi\Buddi3_Prefs.xml
del /Q C:\Users\usi\AppData\Roaming\Buddi\Reports\*
set targetdir=C:\Users\usi\AppData\Roaming\Buddi\Reports
for /d %%x in (%targetdir%\*) do @rd /S /Q %%x
del /Q C:\Users\usi\AppData\Roaming\Buddi\Languages\*
set targetdir=C:\Users\usi\AppData\Roaming\Buddi\Languages
for /d %%x in (%targetdir%\*) do @rd /S /Q %%x
for /d %%p in (C:\Users\usi\abtMyDocuments\\*) Do rd /Q /S "%%p"
copy C:\workspace\GUISemantics_RFT\files\for_test\SUTs_configuration\Languages\* C:\Users\usi\AppData\Roaming\Buddi\Languages\*
del /Q C:\Users\usi\abtMyDocuments\*.buddi3autosave
del /Q C:\Users\usi\AppData\Roaming\Buddi\*.buddi3autosave
del /Q C:\Users\usi\AppData\Roaming\Buddi\*autosave
del /Q C:\Users\usi\AppData\Roaming\Buddi\*bak
del /Q C:\Users\usi\abtMyDocuments\*bak
del /Q C:\Users\usi\abtMyDocuments\*