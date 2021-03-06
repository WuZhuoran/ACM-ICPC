import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

class Main {
    private static int L;
    private static int lastLineWords;
    private static ArrayList<String> words = new ArrayList<>();
    private static Map<String, int[]> memo = new HashMap<>();
    private static String answer = "";

    /**
     * Define w(i, j) as the width of the line containing words i through j, inclusive,
     * plus one blank space between each pair of words.
     *
     * @param i starting position
     * @param j ending position
     * @return w(i, j) value or 0
     */
    private static int w(int i, int j) {
        int width = 0;

        // out of bounds
        if (j + 1 > words.size()) {
            return 0;
        }

        for (String word : words.subList(i, j + 1)) {
            // length of a word + blank space between each pair of words
            width += word.length() + 1;
        }
        // remove the last blank space
        width -= 1;

        return (width > 0 && width <= L) ? width : 0;
    }

    /**
     * Define the raggedness of a line containing words i though j as
     * r(i, j) = (L − w(i, j))²
     *
     * @param i starting position
     * @param j ending position
     * @return r(i, j) value
     */
    private static int r(int i, int j) {
        return (int) Math.pow(L - w(i, j), 2);
    }

    /**
     * Let C(i, k, last) denote minimum raggedness of a line k containing ith word ignoring last words.
     * Then C(i, k, last) = min(r(i, i) + C(i + 1, k + 1, last), r(i, i + 1) + C(i + 2, k + 1, last), ...)
     *
     * @param i    starting position
     * @param k    line number
     * @param last number of words used in the last line
     * @return a minimum total raggedness and an index of the minimum value (the winner)
     */
    private static int[] c(int i, int k, int last) {
        ArrayList<Integer> raggedness = new ArrayList<>();

        // out of bounds
        if (i > words.size() - 1 - last) {
            return new int[]{0, -1};
        }

        int l = i;
        while (w(i, l) > 0 && l <= words.size() - 1 - last) {
            String key = (l + 1) + ", " + (k + 1);

            // add value to memo
            if (!memo.containsKey(key)) {
                memo.put(key, c(l + 1, k + 1, last));
            }

            // calculate all raggedness values for this c()
            raggedness.add(r(i, l) + memo.get(key)[0]);
            l++;
        }

        // find a total minimum raggedness
        int minimum = Collections.min(raggedness);
        memo.put(i + ", " + k, new int[]{minimum, raggedness.indexOf(minimum)});

        return new int[]{minimum, raggedness.indexOf(minimum)};
    }

    /**
     * Calculate raggedness values with varying number of last words and
     * return the minimum of them.
     *
     * @return a minimum total raggedness and a number of last line words
     */
    private static int[] minimizeTotalRaggedness() {
        // clean memo
        memo.clear();

        // set a minimum to a raggedness with 1 last word
        int last = 1, i = 2;
        int minimum = c(0, 0, last)[0];

        while (i <= words.size()) {
            // go as much as possible (all possible amounts of last words) to find a minimum
            // BTDT: 0 last words = all last words
            if (w(words.size() - i, words.size() - 1) > 0 && w(words.size() - i, words.size() - 1) <= L) {
                // clean memo
                memo.clear();

                // update minimum value
                if (minimum > c(0, 0, i)[0]) {
                    minimum = c(0, 0, i)[0];
                    last = i;
                }
            }
            i++;
        }

        // clean memo
        memo.clear();
        // call minimum once again to update memo
        c(0, 0, last);

        return new int[]{minimum, last};
    }


    /**
     * Build a string with a minimum total raggedness from a memo table
     *
     * @return a string with a minimum total raggedness
     */
    private static String backtracking() {
        String string = "";

        // if all the words are on one line
        if (lastLineWords == words.size()) {
            for (String word: words) {
                string += word + ' ';
            }
            // remove a trailing space
            string = string.substring(0, string.length() - 1);
            return string;
        }

        // start with the first word, i.e. the last winner
        int i = 0, k = 0, winnerIndex;

        try {
            winnerIndex = memo.get(i + ", " + k)[1];
        } catch (Exception e) {
            // why?
            return string;
        }

        // -1 if c() is out of bounds
        while (winnerIndex > -1) {
            // add words to the appropriate line
            if (i + winnerIndex + 1 <= words.size()) {
                for (String word : words.subList(i, i + winnerIndex + 1)) {
                    string += word + ' ';
                }
                // remove a trailing space
                string = string.substring(0, string.length() - 1);
                // move to the next line
                string += '\n';
            }

            // move to the next winner
            i += winnerIndex + 1;
            k += 1;
            try {
                winnerIndex = memo.get(i + ", " + k)[1];
            } catch (Exception e) {
                // why?
                break;
            }
        }

        // if there are last line words
        if (lastLineWords > 0 && lastLineWords < words.size()) {
            for (String word : words.subList(words.size() - lastLineWords, words.size())) {
                string += word + ' ';
            }
            // remove a trailing space
            string = string.substring(0, string.length() - 1);
        }

        // remove an empty line
        if (string.length() > 0 && string.charAt(string.length() - 1) == '\n') {
            string = string.substring(0, string.length() - 1);
        }

        return string;
    }

    public static void main(String[] args) {
        // create a buffer to read an input
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            // clear data
            words.clear();
            memo.clear();

            try {
                L = Integer.parseInt(buffer.readLine().trim());
            } catch (Exception e) {
                // why?
                break;
            }

            // a value of zero indicates the end of input
            if (L == 0) {
                if (answer.length() > 0) {
                    // remove last empty line from the answer
                    answer = answer.substring(0, answer.length() - 1);
                    // print answer
                    System.out.println(answer);
                }
                break;
            }

            // maximum number of lines
            int numberOfLines = 250;

            while (numberOfLines > 0) {
                String[] wordsInLine;

                try {
                    wordsInLine = buffer.readLine().split(" ");
                } catch (Exception e) {
                    // why?
                    break;
                }

                // terminate by an empty line
                if (wordsInLine[0].equals("")) {
                    break;
                }

                // add words from a new line
                Collections.addAll(words, wordsInLine);
                numberOfLines--;
            }

            if (words.size() == 1) {
                // add solution to the answer
                answer += words.get(0) + "\n===\n";
                continue;
            }

            int[] mtr = minimizeTotalRaggedness();
            lastLineWords = mtr[1];

            // add solution to the answer
            answer += backtracking() + "\n===\n";
        }
    }
}
