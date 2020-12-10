import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
public class QuestionResult {
    public static void main(String[] args) throws IOException, ParseException {
        int numOfCorrect = 0;
        List<String> category = new ArrayList<>();
        List<String> question = new ArrayList<>();
        List<String> answer = new ArrayList<>();
        String questionPath = "dataset\\question_WOadv.txt";
        File questionFile = new File(questionPath);
        Scanner myReader = new Scanner(questionFile);
        while(myReader.hasNext()) {
            category.add(myReader.nextLine());
            question.add(myReader.nextLine());
            answer.add(myReader.nextLine());
            myReader.nextLine();
        }
        myReader.close();
        float MRRscore = 0;
        for(int i=0;i<answer.size();++i) {
//            System.out.println("The" + " " + (i + 1) + "th question");
            String[] gaussAnswer = QueryEng(question.get(i), 2);
            if (gaussAnswer[0] == null) {
                System.out.println("The" + " " + (i + 1) + "th question");
                System.out.println("No answer retrieved.");
//                System.out.println("Question:" + question.get(i));
//                System.out.println("Answer:" + answer.get(i));
            }
            else {
                if (answer.get(i).contains("|")){
                    String [] twoAnswer = answer.get(i).split("|");
                    int posi1 = Arrays.asList(gaussAnswer).indexOf(twoAnswer[0]);
                    int posi2 = Arrays.asList(gaussAnswer).indexOf(twoAnswer[1]);
//                    System.out.println("position:" + Math.min(posi1, posi2));
                    int position = Math.min(posi1, posi2);
                    if (position != -1) {
                        MRRscore = (float) (MRRscore + 1d / (position + 1));
                    }
                    if (gaussAnswer[0].equals(twoAnswer[0]) || gaussAnswer[0].equals(twoAnswer[1])) {
                        numOfCorrect = numOfCorrect + 1;
                        System.out.println("The" + " " + (i + 1) + "th question");
                        System.out.println("Question:" + question.get(i));
                        System.out.println("Answer:" + answer.get(i));
                        System.out.println("correct!");
                    }
//                    print out the information on wrong question
//                    else {
//                        System.out.println("The" + " " + (i + 1) + "th question");
//                        System.out.println("Question:" + question.get(i));
//                        System.out.println("Answer:" + answer.get(i));
//                    }
                }
                else {
                    int position = Arrays.asList(gaussAnswer).indexOf(answer.get(i));
//                    System.out.println("position:" + position);
                    if (position != -1) {
                        MRRscore = (float) (MRRscore + 1d / (position + 1));
                    }
                    if (gaussAnswer[0].equals(answer.get(i))) {
                        numOfCorrect = numOfCorrect + 1;
                        System.out.println("The" + " " + (i + 1) + "th question");
                        System.out.println("Question:" + question.get(i));
                        System.out.println("Answer:" + answer.get(i));
                        System.out.println("correct!");
                    }
//                    else {
//                        System.out.println("The" + " " + (i + 1) + "th question");
//                        System.out.println("Question:" + question.get(i));
//                        System.out.println("Answer:" + answer.get(i));
//                    }
                }
            }
        }
//        System.out.println("The MMR score:" + " " + MRRscore / 100);
        System.out.println("The number of correct answers:" + " " + numOfCorrect);
    }

    private static String[] QueryEng(String queryStr, int numOfHits) throws IOException, ParseException {
        String[] queryAnswer = new String[numOfHits];
        String indexPath = "index\\Index_StandardAnalyzer";
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        IndexSearcher searcher = new IndexSearcher(reader);
//        searcher.setSimilarity(new ClassicSimilarity());
        Analyzer analyzer = new StandardAnalyzer();//EnglishAnalyzer StandardAnalyzer
//        System.out.println(queryStr);
        Query q = new QueryParser("pageContext", analyzer).parse(queryStr.replaceAll("[-+.^:,!\"\"]",""));
        TopDocs docs = searcher.search(q, numOfHits);
        ScoreDoc[] hits = docs.scoreDocs;
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            queryAnswer[i] = d.get("pageTitle");
        }
        reader.close();
        return queryAnswer;
    }
}
