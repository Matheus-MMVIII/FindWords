import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class FindWords {
    private static final int boardSize = 20;
    private static final String alphabet = "abcdefghijklmnopqrstuvwxyzáàâãçéèêíìîóòôõúùû";
    private static final Random random = new Random();

    private final char[] lyrics;
    private final char[][] board = new char[boardSize][boardSize];
    private final boolean[][] boardUsed = new boolean[boardSize][boardSize];
    private final List<String> words;
    private final List<String> chosenWords;

    public FindWords() throws IOException {
        chosenWords = new ArrayList<>();
        words = generateWords();
        lyrics = alphabet.toCharArray();
        generateBoard();
        printBoard();
    }
    public List<String> generateWords() throws IOException {
        List<String> words = Files.readAllLines(Path.of("words.txt"));
        Collections.shuffle(words);
        return words;
    }
    /*
Direction Logic
    1
  8   2
7   .   3
  6   4
    5
    */

    public void generateBoard() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (boardUsed[i][j]) {
                    continue;
                }
                if (random.nextInt(2) == 0) {
                    boolean possible = true;
                    int numWord = 0;
                    String word = "";
                    if (words.isEmpty())
                        possible = false;
                    else {
                        numWord = random.nextInt(words.size());
                        word = words.get(numWord);
                    }
                    int deltaLine = 0;
                    int deltaColumn = 0;
                    int[] directions = {
                            3,3,3,3,
                            5,5,
                            1,1,
                            2,2,
                            4,4,
                            7,
                            6,
                            8
                    };
                    int direction = directions[random.nextInt(directions.length)];

                    // Set Direction
                    switch (direction) {
                        case 1 -> {
                            deltaLine = -1;
                            deltaColumn = 0;
                        }
                        case 2 -> {
                            deltaLine = -1;
                            deltaColumn = 1;
                        }
                        case 3 -> {
                            deltaLine = 0;
                            deltaColumn = 1;
                        }
                        case 4 -> {
                            deltaLine = 1;
                            deltaColumn = 1;
                        }
                        case 5 -> {
                            deltaLine = 1;
                            deltaColumn = 0;
                        }
                        case 6 -> {
                            deltaLine = 1;
                            deltaColumn = -1;
                        }
                        case 7 -> {
                            deltaLine = 0;
                            deltaColumn = -1;
                        }
                        case 8 -> {
                            deltaLine = -1;
                            deltaColumn = -1;
                        }
                    }

                    // Validation
                    for (int k = 0; k < word.length(); k++) {
                        int line = i + deltaLine * k;
                        int column = j + deltaColumn * k;

                        if (line < 0 || line >= boardSize ||
                                column < 0 || column >= boardSize) {
                            possible = false;
                            break;
                        }

                        if (boardUsed[line][column] &&
                                board[line][column] != word.charAt(k)) {
                            possible = false;
                            break;
                        }
                    }
                    // Write Word
                    if (possible) {
                        for (int k = 0; k < word.length(); k++) {
                            int line = i + deltaLine * k;
                            int column = j + deltaColumn * k;

                            board[line][column] = word.charAt(k);
                            boardUsed[line][column] = true;
                        }
                        chosenWords.add(word);
                        System.out.println(word);
                        words.remove(numWord);

                    }else {
                        board[i][j] = randomWord();
                    }

                }else {
                    board[i][j] = randomWord();
                }
            }
        }
    }

    public void printBoard() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                System.out.print(" "+board[i][j]+" ");
            }
            System.out.print("  ");
            for (int j = 0; j < boardUsed[i].length; j++) {
                System.out.print(" "+(boardUsed[i][j] ? 1 : 0)+" ");
            }
            System.out.println(" ");
        }
    }

    public String getBoard() {
        String boardString = "";
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                boardString += " " + board[i][j] + " ";
            }
        }
        return boardString;
    }

    public List<String> getBoardLines() {
        List<String> boardLines = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            String boardLine = "";
            for (int j = 0; j < board[i].length; j++) {
                boardLine += " " + board[i][j] + " ";
            }
            boardLines.add(boardLine);
        }
        return boardLines;
    }

    public char[][] getBoardChars() {
        return board;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public List<String> getChosenWords() {
        return chosenWords;
    }

    public char randomWord() {
        return lyrics[random.nextInt(26)];
    }
}
