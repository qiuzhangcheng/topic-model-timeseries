import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        String filename = "src/main/msg.txt";
        String res = getTopicFromFile(filename);

        System.out.println(res);
    }

    private static TopicModeler trainTopicModel(List<String> docs) throws IOException {
        TopicModeler topicModeler = new TopicModeler(20, 5000);
        List<String> preprocessDocs = new TopicModelPreprocessor().preProcess(docs);
        topicModeler.trainTopicModel(preprocessDocs);

        return topicModeler;
    }

    private static List<Integer> getDocumentTopics(TopicModeler tm, List<String> docs) {
        List<Integer> topics = new ArrayList<>();

        for (String doc : docs) {
            topics.add(tm.predictTopic(doc));
        }

        return topics;
    }

    private static String getTopicFromFile(String filename) throws Exception {
        StringBuilder sb = new StringBuilder();

        List<String> docs = readFileToList(filename);
        List<String> dates = readFileToList("src/main/date.txt");
        List<String> realDates = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (String date : dates) {
            realDates.add(sdf.format(new Date(Long.parseLong(date) * 1000)));
        }

        String modelFilename = "model.bin";
        File modelFile = new File(modelFilename);
        TopicModeler tm;

        if (modelFile.exists()) {
            tm = new TopicModeler();
            tm.readModelFromFile(modelFilename);
        } else {
            tm = trainTopicModel(docs);
            tm.saveModelToFile(modelFilename);
        }

        String[][] topicWords = tm.getTopics(15);
        List<Integer> topics = getDocumentTopics(tm, docs);
        List<String> timeseries = getTimeseries(dates);

        for (int i = 0; i < 20; i++) {
            List<Long> hist = getHistogram(i, topics, realDates, timeseries);
            sb.append("Topic-").append(i).append(',');

            for (String word : topicWords[i]) {
                sb.append(word).append(' ');
            }

            sb.append('\n');

            for (int j = 0; j < hist.size(); j++) {
                sb.append(timeseries.get(j)).append(',').append(hist.get(j)).append('\n');
            }

            sb.append('\n');
        }

        return sb.toString();
    }

    private static List<String> getTimeseries(List<String> dates) {
        Date minDate = new Date(Long.parseLong(Collections.min(dates)) * 1000);
        Date maxDate = new Date(Long.parseLong(Collections.max(dates)) * 1000);

        List<String> res = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(minDate);
        cal.add(Calendar.DATE, -1);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        while (cal.getTime().before(maxDate)) {
            cal.add(Calendar.DATE, 1);
            res.add(sdf.format(cal.getTime()));
        }

        return res;
    }

    private static List<Long> getHistogram(int topic, List<Integer> topics, List<String> dates, List<String> ts) {
        List<Long> res = new ArrayList<>();

        for (int i = 0; i < ts.size(); i++) {
            res.add(0l);
        }

        for (int i = 0; i < topics.size(); i++) {
            int t = topics.get(i);

            if (t != topic) continue;

            int dateIdx = ts.indexOf(dates.get(i));
            long curr = res.get(dateIdx);
            res.set(dateIdx, ++curr);
        }

        return res;
    }

    private static List<String> readFileToList(String filename) throws IOException {
        List<String> docs = new ArrayList<>();

        Scanner s = new Scanner(new File(filename));

        while (s.hasNextLine()) {
            docs.add(s.nextLine());
        }

        s.close();

        return docs;
    }

}
