call scripts\AUTreset.bat
java -version

TITLE case_study
java -Xbootclasspath/p:C:\workspace\GUISemantics_RFT\lib\abtJFileChooser.jar; -cp %CLASSPATH%;C:\workspace\GUISemantics_RFT\bin;C:\workspace\GUISemantics_RFT\lib\cobertura\cobertura.jar;C:\workspace\upm\lib\*;C:\workspace\upm\instrumented;C:\workspace\upm\bin;C:\workspace\GUISemantics_RFT\lib\jmockit\jmockit.jar; -javaagent:C:\workspace\GUISemantics_RFT\lib\jmockit\jmockit.jar -Dnet.sourceforge.cobertura.datafile="C:\workspace\GUISemantics_RFT\lib\cobertura\cobertura.ser" usi.rmi.AUTMain com._17od.upm.gui.MainWindow 1> "C:\workspace\GUISemantics_RFT\output\stdOUT.log" 2> "C:\workspace\GUISemantics_RFT\output\stdERR.log"