package openie;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.Quadruple;

import java.util.Collection;
import java.util.List;

/**
 * Created by Mayanka on 27-Jun-16.
 */
public class CoreNLP {
    public static String returnTriplets(String sentence) {
        //System.out.println("this Happened");
        Document doc = new Document(sentence);
        String lemma="";
        for (Sentence sent : doc.sentences()) {  // Will iterate over two sentences
            //System.out.println("for sentence: " +sent);
            Collection<Quadruple<String, String, String, Double>> l=sent.openie();
            for (int i = 0; i < l.toArray().length ; i++) {
                lemma+= l.toString();
                //System.out.println("openie returned: " + lemma);
            }
            //System.out.println(lemma);
        }

        return lemma;
    }

    public static String returnLemma(String sentence) {

        Document doc = new Document(sentence);
        String lemma="";
        for (Sentence sent : doc.sentences()) {  // Will iterate over two sentences
            List<String> l=sent.lemmas();
            for (int i = 0; i < l.size() ; i++) {
                lemma+= l.get(i) +" ";
            }
            //System.out.println(lemma);
        }

        return lemma;
    }

}
