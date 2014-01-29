Online-QuEst:

The Online-QuEst is an extension of QuEst open source software  aimed at quality estimation (QE) for machine translation on the fly. 
The original code is changed by adapting / adding number of classes to load the models in memory before extracting features.  
It supports extracting the features sentence by sentence on the fly without building/ reloading the models again.   

In the src folder, the folders that we have added are :
updatedQuEst: where we have built the interface for the 'on the fly' QuEst
ClientIrstLM: where there is the class that replaces the old way of getting the log probability from the lm
ClientQuest: where there are two classes that replaces the old way of calling the feature extractor.. 

The new version of the QuEst is in the folder shef/mt/enes in the class FeatureExtractionSimple.java.

We have modified also other classes, in that case I have left the original class with the suffix Original. We have modified (probably some class more)
FileModel in shef/mt/tools
PPLProcessor in shef/mt/tools
Feature1036 in shef/mt/features/impl/bb

Please note that we have tested the new implementation using the baseline features, but we believe that it can work also with other features.

Also, we have used IRSTLM to convert arpa files into binary that make things faster ..  

### Server LM ###
To embedded the IRST lm into the server, you need to use the perl script myFaucet.pl. It opens a permanent connection in the standard input/output to any software which accepts input and output strings in the standard input/output. It should be run on the machine where you want to have the lm running.

The syntax is:
perl myFaucet.pl -d port executable_of_the_software parameters_of_the_software

For IrstLM see below how to call it

English LM:

perl myFaucet.pl -d 9887 compile-lm lm.europarl-nc.en.blm —sentence=yes --eval /dev/stdin

Spanish LM:

perl myFaucet.pl -d 9888 compile-lm lm.europarl-interpolated-nc.es.blm —sentence=yes --eval /dev/stdin

Spanish POS LM:

perl myFaucet.pl -d 9889 compile-lm pos_lm.es.blm —sentence=yes --eval /dev/stdin

Note that IrstLM can use binarized lm. Given the ARPA file, you can easily build the binarized version running:

compile-lm train.lm tran.blm



e.g commands:


perl myFaucet.pl -d 9111 java -Xmx1g -XX:+UseConcMarkSweepGC -classpath build/classes:lib/commons-cli-1.2.jar:lib/stanford-postagger.jar:lib/BerkeleyParser-1.7.jar:lib/log4j-1.2.17.jar updatedQuEst.UpdatedQuEstMain  config/config_en-es.properties_corr /Users/kashif/Documents/projects/QuestOnline/output false false

last three arguments are ‘output-folder’ debug<true/false> already-tokenize<true/false>  

Before running above command please make sure required LMs are in memory:
e.g command:

perl myFaucet.pl -d 9888 compile-lm lm.europarl-nc.en.blm -sentence=yes --eval /dev/stdin


We would need to change config file as well.. 

spanish.lm.url							= localhost
spanish.lm.port							= 9888
english.lm.url							= localhost
english.lm.port							= 9887

Then make a fake call by: (to run client for extracting features: )

java -Xmx1g -XX:+UseConcMarkSweepGC -classpath build/classes:lib/* ClientQuest.ClientQuEst

Now it should build and load the models into memory… and calculate the features sentence by sentence

