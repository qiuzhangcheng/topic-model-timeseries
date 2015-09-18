import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Agustinus Kristiadi
 * @since May 20, 2014
 */
public class TopicModelPreprocessor extends Preprocessor {

    public TopicModelPreprocessor() {
        super();
    }

    public TopicModelPreprocessor(List<String> emoticons) {
        super(emoticons);
    }

    public List<String> preProcess(List<String> documents) {
        List<String> res = new ArrayList<String>();

        for (String newStr : documents) {
            res.add(preprocessAll(newStr));
        }

        return res;
    }

    protected String preprocessAll(String newStr) {
        String result;
        result = newStr.toLowerCase(Locale.US);
//        result = removeEmail(result);
        result = removeNumber(result);
//        result = removeHashtag(result);
//        result = removeUsername(result);
        result = removeLink(result);
        result = removeRepeat(result);
        result = removeEmoticon(result);
        result = convertNumbers(result);
//        result = convertNumberTwo(result);
        result = removePunctuation(result);
        result = removeSingleCharacter(result);
        result = removeNegMark(result);
        result = result.trim().replaceAll(" +", Preprocessor.SPACE);
        return result;
    }
}