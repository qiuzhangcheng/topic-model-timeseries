import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveNonAlpha;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.util.ArrayUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Agustinus Kristiadi
 */
public class TopicModeler {

    private static final int DEF_N_TOPICS = 10;
    private static final int DEF_N_ITERATION = 2000;
    private static final int DEF_N_THREADS = 1;
    private static final int DEF_GIBBS_ITER = 100;
    private static final int DEF_GIBBS_BURNIN = 10;
    private static final int DEF_GIBBS_THINNING = 10;
    private static final String TOKENIZER_REGEX = "\\p{L}[\\p{L}\\p{P}]+\\p{L}";
    private int nTopic;
    private List<Pipe> pipeList;
    private ParallelTopicModel model;

    /**
     * Create TopicModeler with n topics = 3
     */
    public TopicModeler() {
        this(DEF_N_TOPICS, DEF_N_ITERATION, DEF_N_THREADS);
    }

    /**
     * Create TopicModeler with custom number of topics
     *
     * @param nTopic Number of topic to be detected
     */
    public TopicModeler(int nTopic) {
        this(nTopic, DEF_N_ITERATION, DEF_N_THREADS);
    }

    /**
     * Create TopicModeler with custom number of topics and iteration
     *
     * @param nTopic Number of topic to be detected
     * @param nIter Number of iteration the LDA algorithm will run
     */
    public TopicModeler(int nTopic, int nIter) {
        this(nTopic, nIter, DEF_N_THREADS);
    }

    /**
     * Create TopicModeler with custom number of topics, iterations, and threads
     *
     * @param nTopic Number of topic to be detected
     * @param nIter Number of iteration the LDA algorithm will run
     * @param nThread Number of threads LDA will use
     */
    public TopicModeler(int nTopic, int nIter, int nThread) {
        this.nTopic = nTopic;
        this.model = new ParallelTopicModel(nTopic);
        this.model.setNumIterations(nIter);
        this.model.setNumThreads(nThread);
        this.model.showTopicsInterval = 0;
        this.model.saveModelInterval = 0;
        this.model.saveStateInterval = 0;
        this.model.printLogLikelihood = false;
        constructPipeList();
    }

    /**
     * Train model with documents to learn topics
     *
     * @param documents Documents used to learn
     * @throws IOException If training failed
     */
    public void trainTopicModel(List<String> documents) throws IOException {
        InstanceList instances = new InstanceList(new SerialPipes(pipeList));
        instances.addThruPipe(new ArrayIterator(documents));
        model.addInstances(instances);
        model.estimate();
    }

    /**
     * Get topics top n words
     *
     * @param nWordsPerTopic Number of words per topic
     * @return Array of array of words (topic x n words)
     */
    public String[][] getTopics(int nWordsPerTopic) {
        Object[][] objMatrix = model.getTopWords(nWordsPerTopic);

        // Convert matrix of object to matrix of string with the same
        // dimension
        String[][] topics = new String[objMatrix.length][objMatrix[0].length];

        for (int i = 0; i < objMatrix.length; i++) {
            for (int j = 0; j < objMatrix[0].length; j++) {
                topics[i][j] = (String) objMatrix[i][j];
            }
        }
        return topics;
    }

    /**
     * Get topics summary string
     *
     * @param nWordsPerTopic Number of words per topic
     * @return String of topics summary
     */
    public String getTopicsSummary(int nWordsPerTopic) {
        return model.displayTopWords(nWordsPerTopic, false);
    }

    /**
     * Get distribution of topics in a single document
     *
     * @param document Document the topic to be inferred
     * @return Vector of topic distribution
     */
    public double[] predictTopicDistribution(String document) {
        // Use inferencer/predictor from built model
        TopicInferencer inferencer = model.getInferencer();
        // Create instanceList to preprocess & extract features from document
        InstanceList instances = new InstanceList(new SerialPipes(pipeList));
        instances.addThruPipe(new Instance(document, null, null, null));

        return inferencer.getSampledDistribution(instances.get(0), DEF_GIBBS_ITER, DEF_GIBBS_THINNING, DEF_GIBBS_BURNIN);
    }

    /**
     * Predict which topic a document belong to, based on the learned model
     *
     * @param document Document the topic to be inferred
     * @return Topic index of the document, one document will be assigned to one
     * topic
     */
    public int predictTopic(String document) {
        return ArrayUtils.argmax(predictTopicDistribution(document));
    }

    /**
     * Save topic model to file
     *
     * @param fileName File name
     */
    public void saveModelToFile(String fileName) {
        model.write(new File(fileName));
    }

    /**
     * Read topic model from file
     *
     * @param fileName File name
     * @throws Exception If failed reading model file
     */
    public void readModelFromFile(String fileName)
            throws Exception {
        try {
            model = ParallelTopicModel.read(new File(fileName));
        } catch (Exception ex) {
            throw new Exception(ex.getMessage(), ex);
        }
    }

    private void constructPipeList() {
        this.pipeList = new ArrayList<>();

        pipeList.add(new CharSequence2TokenSequence(TOKENIZER_REGEX));
        pipeList.add(new TokenSequenceRemoveNonAlpha());
        pipeList.add(new TokenSequenceRemoveStopwords(
                new File("stopword"), "utf-8", true, false, false));
        pipeList.add(new TokenSequence2FeatureSequence());
    }

    /**
     * @return the number of topic
     */
    public int getnTopic() {
        return nTopic;
    }

    /**
     * @param nTopic the number of topic to set
     */
    public void setnTopic(int nTopic) {
        this.nTopic = nTopic;
    }

    /**
     * @return the nIter
     */
    public int getnIter() {
        return model.numIterations;
    }

    /**
     * @param nIter the nIter to set
     */
    public void setnIter(int nIter) {
        this.model.setNumIterations(nIter);
    }
}