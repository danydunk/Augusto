CALL .\gradlew.bat build

REM the AUT is deployed, this app has a CRUD and a SAVE functionality
CALL .\gradlew.bat deployUPMSmall

REM to run Augusto the information required to run the application under test must be specified in the file ./aut.properties
cp .\files\for_test\config\tutorial.properties .\aut.properties

REM the second step of the workflow is rip the application under test to produce a model of its GUI
REM if the application requires some initial actions (i.e. putting a password) they must be defined in the initial_actions file (check ./files/for_test/xml/tutorial_initial_actions.xml)
REM if the application requires some widgets to be skipped during the ripping they must be defined in the ripper_filters file (check ./files/for_test/xml/tutorial_ripper_filters.xml)
CALL .\Ripper.bat

REM the second step of the workflow is to look for Application Independent functionalities in the application under test, generete test cases for them and run them
REM if the application under test requires some specific input data they must be defined in ./files/inputdata/dataset.xml
REM the results will be saved in ./results_TIMESTAMP
CALL .\Main.bat