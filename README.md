#DataStore

Ari Krakauer

This Data Store is the semester project for the COM1310 Data Structures course in Yeshiva University, spring semester 2023.

Every part of this project was custom implemented, instead of using data structures built in to Java, in order to gain a deeper understanding of how each data structure works.

DocumentStoreImpl is the main class, using BTreeImpl for storage, MinHeapImpl for memory management, StackImpl for undo capabilities, and TrieImpl for search.

BTreeImpl provides storage for documents, with get and put commands (delete included in put), and moves entries from memory to disk, by serializing using persistence manager (custom implemented DocumuntPersistenceManager in this case).

MinHeapImpl keeps track of entries with least value, and is used to determine which document was least recently used and should be pushed to disk.

StackImpl is used to store commands that were called, to add or delete documents from the data store, and enables them to be undone.

TrieImpl is used to keep track of which documents contain a certain word, by storing documents at the end of each word, to enable search capabilities.