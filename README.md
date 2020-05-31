# LuceneAdaptorTesting

Luceneadp.java is the Lucene adapter file which accesses two JSON file(one mapping.json file and other message.json ), edit the message_path and mapping_path in the program to the location where these files are located. This java program uses Lucene analysis-common jar, Lucene core jar, Lucene QueryParser jar, json-simple-1.1.jar.  

MonitorADP.java is the Monitor adapter file which accesses two JSON file(one mapping.json file and other message.json ), edit the message_path and mapping_path in the program to the location where these files are located. This java program uses Lucene analysis-common jar, Lucene core jar, Lucene QueryParser jar, json-simple-1.1.jar and Lucene monitor jar. If you are using Lucene monitor in version 8.5.1 you will face Null Pointer Exception when you try to match a document that contains a LongPoint Field, this is because of some bug in TermFilteredPresearcher of Lucene monitor in version 8.5.1. This bug has been fixed and new code is available here "https://github.com/apache/lucene-solr/blob/master/lucene/monitor/src/java/org/apache/lucene/monitor/TermFilteredPresearcher.java". Replace this file and build jar files again to fix the Null Pointer Exception.

Lucene.java is just for testing and experimenting
