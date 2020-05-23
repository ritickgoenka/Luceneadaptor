# LuceneAdaptorTesting

Luceneadp.java is the main adapter file which accesses two JSON file(one mapping.json file and other message.json ), edit the message_path and mapping_path in the program to the location where these files are located. It uses Lucene analysis-common jar, Lucene core jar, Lucene QueryParser jar, json-simple-1.1.jar.  Two test queries have been hard coded and I'm facing problem with the StringField query.

I created Lucene.java to test StringField and LongPoint queries to ensure that the problem lies in the way I query for StringField and LongPoint and not in adapter, in this file I hardcoded three fields (one TextField, one StringField and one LongPoint) and searched for 3 queries but I was unable to get the desired result for StringField and LongPoint, I think I'm making the same mistake here.
