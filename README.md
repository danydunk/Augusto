# Augusto: Automatic GUI Semantic Testing and Oracles
[![Build Status](https://travis-ci.org/danydunk/Augusto.svg?branch=master)](https://travis-ci.org/danydunk/Augusto)


Augusto generates test cases for interactive application exploiting the general semantics of Application Independent functionalities.

To generate test cases Augusto requires an application under test and a set of modelled Application Independent Functionalities. Currently the functionalities modelled are CRUD, SAVE, and AUTHENTICATION.

## Building Augusto
To build Augusto run `.\gradlew.bat build`

Building Augusto requires a Window 7+ machine with Java JDK 1.8+ and IBM Rational Functional Tester 8.6+ (https://www.ibm.com/developerworks/downloads/r/rft/index.html) installed.

## Running Augusto
For information about how to run Augusto on a application under test check the tutorial `augusto_tutorial.bat`

## Adding a Application Independent Functionality
If you want to add a new Application Independent Functionality to Augusto you must model using two types of models: the Gui pattern model and the Alloy Semantic model.
For examples of this models check `files/guipatterns` and `files/alloy`.

Once the new functionality is modelled it must be added to the ENUM `usi.pattern.Patterns`

## Support
For support contact danydunk@gmail.com