package edu.yu.cs.com1320.project.stage5.impl;

import com.google.gson.*;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import jakarta.xml.bind.DatatypeConverter;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
    private File dir = new File(System.getProperty("user.dir"));

    public DocumentPersistenceManager(File baseDir){
        if (baseDir != null) {
            this.dir = baseDir;
        }
    }

    @Override
    public void serialize(URI uri, Document val) throws IOException {
        if (uri == null || val == null) {
            throw new IllegalArgumentException();
        }
        Gson gson = new GsonBuilder().registerTypeAdapter(DocumentImpl.class, new JsonSerializer<Document>() {
            @Override
            public JsonElement serialize(Document document, Type type, JsonSerializationContext jsonSerializationContext) {
                JsonObject obj = new JsonObject();
                if (document.getDocumentTxt() == null) {
                    obj.addProperty("Byte Contents", DatatypeConverter.printBase64Binary(document.getDocumentBinaryData()));
                } else {
                    obj.addProperty("String Contents", document.getDocumentTxt());
                    obj.addProperty("Word Count Map", document.getWordMap().toString());
                }
                obj.addProperty("URI", document.getKey().toString());
                return obj;
            }
        }).create();
        File file = this.uriToPath(uri);
        file.mkdirs();
        file.delete();
        file.createNewFile();
        FileWriter fw = new FileWriter(file);
        gson.toJson(val, fw);
        fw.close();
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        Gson gson = new GsonBuilder().registerTypeAdapter(DocumentImpl.class, new JsonDeserializer<Document>() {
            @Override
            public Document deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                JsonObject obj = jsonElement.getAsJsonObject();
                Document d;
                URI u;
                try {
                    String str = obj.get("URI").toString();
                    u = new URI(str.substring(1, str.length() - 1));
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                if (obj.get("String Contents") != null) {
                    HashMap<String, Integer> words = new HashMap<>();
                    String[] mappings = obj.get("Word Count Map").getAsString().split(", ");
                    for (String mapping : mappings) {
                        String[] entry = mapping.split("=");
                        if (entry[0].charAt(0) == '{') {
                            entry[0] = entry[0].substring(1);
                        }
                        if (entry[1].charAt(entry[1].length() - 1) == '}') {
                            entry[1] = entry[1].substring(0, entry[1].length() - 1);
                        }
                        words.put(entry[0], Integer.parseInt(entry[1]));
                    }
                    d = new DocumentImpl(u, obj.get("String Contents").getAsString(), words);
                } else {
                    d = new DocumentImpl(u, DatatypeConverter.parseBase64Binary(obj.get("Byte Contents").getAsString()));
                }
                return d;
            }
        }).create();
        File file = this.uriToPath(uri);
        if (file.exists()) {
            FileReader fr = new FileReader(file);
            Document d = gson.fromJson(fr, DocumentImpl.class);
            fr.close();
            return d;
        }
        return null;
    }

    @Override
    public boolean delete(URI uri) throws IOException {
        return this.uriToPath(uri).delete();
    }

    private File uriToPath(URI uri) {
        String path = uri.toString().replace(uri.getScheme(), "").replaceAll("[\"=+#$%'&*^@!<>{}|: `]", "") + ".json";
        return new File(this.dir, path);
    }
}