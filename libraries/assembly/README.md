
Bidirectional POS Tagger

Copyright (c) 2006
Libin Shen
All Rights Reserved.

This code is compatible with J2SE 5.0 or up.

Usage: java -classpath bpos.jar bpos <beam width> <test file> <weights> <lables>

For example,

> java -classpath bpos.jar bpos 1 data/sample.raw data/k3.fea data/postag.txt > sample.hypo

We can evaluate the result with the pos_compare script in the util/ directory. For example, 

> util/pos_compare.perl data/sample.gold data/sample.hypo.ver

