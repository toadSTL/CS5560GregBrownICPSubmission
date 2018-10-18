package OpenIE;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.Quadruple;

import java.util.Iterator;

public class CoreNLP {
    public static String returnTriplets(String sentence){
        Document doc = new Document(sentence);
        String triplets = "";
        int noOfTriplets = 0;
        for(Sentence sent : doc.sentences()) {
            triplets += sent+"\t";
            Iterator<Quadruple<String, String, String, Double>> l = sentence.openie().iterator();
            while(l.hasNext()){
                Quadruple<String, String, String, Double> n = l.next();
                String subject = n.first;
                String predicate = n.second;
                String object = n.third;

                triplets+=subject+";"+predicate+";"+object+"\t";
                noOfTriplets++;
            }
            System.out.println(triplets);
        }
        return triplets+"\n";
    }
}
