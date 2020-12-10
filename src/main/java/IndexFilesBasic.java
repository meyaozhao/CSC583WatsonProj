import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
public class IndexFilesBasic {
    public static void main(String[] args) throws IOException {
//        coreNLP configuration
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
//        index configuration
        String indexPath = "index\\Index_TFIDF";
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        Analyzer analyzer = new StandardAnalyzer();//EnglishAnalyzer StandardAnalyzer
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setSimilarity(new ClassicSimilarity());
        IndexWriter writer = new IndexWriter(dir, iwc);
//        read the wiki files from the folder
        File folder = new File("dataset\\wiki-subset-20140602");
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        for (File f : listOfFiles) {
            Scanner myReader = new Scanner(f); // test for one file
            StringBuilder sb = new StringBuilder();
            while(myReader.hasNext()) {
                sb.append(myReader.nextLine());
            }
            myReader.close();
            //        arrOfStr: array of all pages in the single file, each one has ]]
            String outString = sb.toString().replaceAll("\\[\\[(File|Image):.*?\\]\\]", " ");
//            String outString = outString1.replaceAll("==See also==.*?\n\\[\\[", "\\[\\[");
            String[] arrOfStr_raw = outString.split("\\[\\["); // arrOfStr_shortStr includes elements who has [[]] in the main body
//            remove the first element as it is empty
            String[] arrOfStr = Arrays.copyOfRange(arrOfStr_raw, 1, arrOfStr_raw.length);
            for (String s : arrOfStr) {
                String[] titleAndContext = s.split("\\]\\]");
                if (titleAndContext.length == 2) {
//                    lemmatization with the coreNLP
//                    CoreDocument document = pipeline.processToCoreDocument(titleAndContext[1]);
//                    StringBuilder contextStr = new StringBuilder();
//                    for (CoreLabel tok : document.tokens()) {
//                        contextStr.append(tok.lemma() + ' ');
//                    }
//                    addDoc(writer, contextStr.toString(), titleAndContext[0]);
                    addDoc(writer, titleAndContext[1], titleAndContext[0]);
                }
            }
            System.out.println("Current indexing file:" + " " + f.getName());
        }
        //        close the index writer
        writer.close();
    }
    private static void addDoc(IndexWriter writer, String context, String title) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("pageContext", context, Field.Store.YES));
        doc.add(new StringField("pageTitle", title, Field.Store.YES));
        writer.addDocument(doc);
    }
}
