import java.util.List;
import java.util.Arrays;

/**
 *
 * @author Agustinus Kristiadi
 */
public abstract class Preprocessor {

    private static final String[] EMOTICONS = {
            ":\\(", ":P", ":\\)", ":D", ":d", ":b", ":p", ":-D", ":=\\)", ":=D",
            ":-\\)", ":-\\(", ":=\\(", "-_-", "-_-'", "-_-\""
    };
    private static final String[] HAPPY_EMOTICONS = {
            ":P", ":\\)", ":D", ":d", ":b", ":p", ":-D", ":=\\)", ":=D",
            ":-\\)"
    };
    private static final String[] SAD_EMOTICONS = {
            ":\\(", ":-\\(", ":=\\(", "-_-", "-_-'", "-_-\""
    };
    protected String[] negationWords = {
            "tidak", "nggak", "ngga", "ga", "g", "gak", "nda", "nd", "ndak", "nga", "belum"
    };
    protected static final String[] PUNC_LIST = {
            ".", ":", "!", ";", "?", ","
    };
    private List<String> emoticons;
    protected static final String NEG_MARK = "_NEG";
    protected static final String SPACE = " ";
    protected static final String EMPTY_STRING = "";
    protected static final String TOKEN_HASHTAG = "hashtagtoken";

    /**
     * Default Preprocessor Constructor.
     *
     * Create Preprocessor class with default emoticons list and default list of
     * negation words.
     */
    public Preprocessor() {
        this.emoticons = Arrays.asList(EMOTICONS);
    }

    /**
     * Custom Preprocessor Constructor.
     *
     * Create Preprocessor class with custom emoticons matching regex and list
     * of negation words.
     *
     * @param emoticons List of emoticons, must be regex compatible. E.g. ':('
     * should be ':\\('.
     */
    public Preprocessor(List<String> emoticons) {
        if (emoticons != null) {
            this.emoticons = emoticons;
        } else {
            this.emoticons = Arrays.asList(EMOTICONS);
        }
    }

    protected String removeHashtag(String doc) {
        return doc.replaceAll("#\\w+", EMPTY_STRING);
    }

    protected String removeRepeat(String doc) {
        // Replace repeated letter or syllable (3 or more) into one instance
        return doc.replaceAll("(.+?)\\1{3,}", "$1");
    }

    protected String removePunctuation(String doc) {
        return doc.replaceAll("[\\W]", " ");
    }

    protected String removeSingleCharacter(String doc) {
        return doc.replaceAll("\\b[A-Za-z0-9]\\b", EMPTY_STRING);
    }

    protected String removeNumber(String doc) {
        return doc.replaceAll("\\b[0-9]+\\b", EMPTY_STRING);
    }

    protected String removeEmoticon(String doc) {
        String newStr = doc;

        for (String emo : this.emoticons) {
            newStr = newStr.replaceAll(emo, EMPTY_STRING);
        }

        return newStr;
    }

    protected String convertEmoticon(String doc) {
        String newStr = doc;
        final String happyEmoString = "happyemot";
        final String sadEmoString = "sademot";

        for (String emo : HAPPY_EMOTICONS) {
            newStr = newStr.replaceAll(emo, happyEmoString);
        }

        for (String emo : SAD_EMOTICONS) {
            newStr = newStr.replaceAll(emo, sadEmoString);
        }

        return newStr;
    }

    protected String removeNegMark(String doc) {
        return doc.replaceAll(SPACE + NEG_MARK, EMPTY_STRING);
    }

    protected String removeRetweet(String doc) {
        return doc.replaceAll("(@user:).+", EMPTY_STRING);
    }

    protected String convertUsername(String doc) {
        // Convert username tag of twitter as user_
        return doc.replaceAll("@+[\\w_]+", "@user");
    }

    protected String removeUsername(String doc) {
        // Convert username tag of twitter as user_
        return doc.replaceAll("@+[\\w_]+", "");
    }

    protected String convertEmail(String doc) {
        return doc.replaceAll("\\b[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})\\b", "email");
    }

    protected String removeEmail(String doc) {
        return doc.replaceAll("\\b[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})\\b", "");
    }

    protected String convertLink(String doc) {
        // Convert any string with prefix http, https, and www as url_
        String newStr = doc.replaceAll("(http(s?)://[^ ]+)", "url");
        newStr = newStr.replaceAll("(www.[^ ]+)", "url");
        return newStr;
    }

    protected String removeLink(String doc) {
        String newStr = doc.replaceAll("(http(s?)://[^ ]+)", "");
        newStr = newStr.replaceAll("(www.[^ ]+)", "");
        return newStr;
    }

    protected String convertNumbers(String doc) {
        StringBuilder newDoc = new StringBuilder();

        for (String s : doc.split(" ")) {
            String res = s;

            if (!isNumeric(res)) {
                res = res.replaceAll("00", "u");
                res = res.replaceAll("0", "o");
                res = res.replaceAll("1", "i");
                res = res.replaceAll("3", "e");
                res = res.replaceAll("4", "a");
                res = res.replaceAll("5", "s");
                res = res.replaceAll("6|9", "g");
                res = res.replaceAll("7", "t");
                res = res.replaceAll("8", "b");
            }

            newDoc.append(res);
            newDoc.append(" ");
        }

        return newDoc.toString().trim();
    }

    protected String convertNumberTwo(String doc) {
        String newStr = doc;

        // Check occurence of string 2 or \
        if (newStr.contains("2") || newStr.contains("\\\\")) {
            // For each word in documents
            for (String word : newStr.split(SPACE)) {
                // Get index of 2 or \
                int idx;
                int idx1 = word.indexOf('2');
                int idx2 = word.indexOf("\\\\");

                // Find the biggest index because if the string is not found,
                // it will return -1
                if (idx1 > idx2) {
                    idx = idx1;
                } else {
                    idx = idx2;
                }
                // If nothing is found, just skip this word
                if (idx == -1) {
                    continue;
                }

                // Only repeat word if 2 or \ in the middle of that word
                if (idx == 0) {
                    String newWord = word.substring(1);
                    newStr = newStr.replace(word, newWord);
                } else if (idx == word.length() - 1) {
                    String newWord = word.substring(0, idx);
                    newStr = newStr.replace(word, newWord + SPACE + newWord);
                } else {
                    String repeat = word.substring(0, idx);
                    String newWord = repeat + repeat + word.substring(idx + 1);
                    newStr = newStr.replace(word, newWord);
                }
            }
        }

        return newStr;
    }

    protected boolean isNumeric(String str) {
        try {
            // Try to cast string to double, if it raises exception, then
            // it's not a number
            Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }

    /**
     * @return the default list of emoticon regexes
     */
    public String[] getDefaultEmoticons() {
        return Arrays.copyOf(EMOTICONS, EMOTICONS.length);
    }

    /**
     * @return the list of emoticon regexes
     */
    public List<String> getEmoticons() {
        return emoticons;
    }

    /**
     * @param emoticons list of emoticons to set
     */
    public void setEmoticons(List<String> emoticons) {
        this.emoticons = emoticons;
    }
}