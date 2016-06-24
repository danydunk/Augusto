#!/bin/bash
#  
#  Copyright (c) 2009-@year@. The GUITAR group at the University of Maryland. Names of owners of this group may
#  be obtained by sending an e-mail to atif@cs.umd.edu
# 
#  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
#  documentation files (the "Software"), to deal in the Software without restriction, including without 
#  limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
#  the Software, and to permit persons to whom the Software is furnished to do so, subject to the following 
#  conditions:
# 
#  The above copyright notice and this permission notice shall be included in all copies or substantial 
#  portions of the Software.
#
#  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
#  LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO 
#  EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
#  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
#  THE USE OR OTHER DEALINGS IN THE SOFTWARE. 

####################################################
# JFC replayer launching script
#
#   By   baonn@cs.umd.edu
#   Date:    06/08/2011
####################################################

function usage {
   echo "Usage: `basename $0` -cp <aut classpath> [guitar arguments]"
}

base_dir=`dirname $0`
guitar_lib=$base_dir/jars

if [ $# -lt 1 ];
then
   echo "Invalid parameter(s)"
   usage
   exit
fi

# Replayer main classes 
replayer_launcher=edu.umd.cs.guitar.replayer.JFCReplayerMain

#
# Application classpath 
#
if [ $1 == "-cp" -a $# -ge 2 ]
then
   addtional_classpath=$2
   guitar_args=${@:3}
elif [ $1 == "-?" ]
then
   guitar_args=("-?")
else
   echo "Invalid parameter(s)"
   usage
   exit
fi


#
# GUITAR classpath 
#
for file in `find $guitar_lib/ -name "*.jar"`
do
   guitar_classpath=${file}:${guitar_classpath}
done
guitar_classpath=$base_dir:$guitar_classpath

if [ ! -z $addtional_classpath ] 
then
   classpath=$addtional_classpath:$guitar_classpath
else
   classpath=$guitar_classpath
fi

# Change GUITAR_OPTS variable to run with the clean log file  
GUITAR_OPTS="$GUITAR_OPTS -Dlog4j.configuration=log/guitar-clean.glc"

if [ -z "$JAVA_CMD_PREFIX" ];
then
    JAVA_CMD_PREFIX="java"
fi

REPLAYER_CMD="$JAVA_CMD_PREFIX $GUITAR_OPTS -cp $classpath $replayer_launcher $guitar_args"

$REPLAYER_CMD
exit $?
