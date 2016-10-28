java -Xmx7g -Xss512m -ea -classpath "C:\Program Files\IBM\SDP\FunctionalTester\bin\rational_ft.jar";"C:\workspace\GUISemantics_RFT\lib\*";"C:\workspace\GUISemantics_RFT\lib\cobertura\*";"C:\workspace\GUISemantics_RFT\lib\cobertura\lib\*" com.rational.test.ft.rational_ft -datastore "C:\workspace\GUISemantics_RFT" -playback test.rft.Refinement_upmfull_crud 1> stdout.log 2>stdErr.log

pause