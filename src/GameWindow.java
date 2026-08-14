import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class GameWindow extends Canvas implements Runnable {

    private static final int WINDOW_WIDTH = 1920;
    private static final int WINDOW_HEIGHT = 1060;

    private static final int BOARD_START_X = 550;
    private static final int BOARD_START_Y = 80;
    private static final int BOARD_CELL_SIZE = 40;

    private static final int WORD_LIST_START_X = 10;
    private static final int WORD_LIST_START_Y = 30;
    private static final int WORD_LIST_CELL_SIZE = 30;

    private static final int WINDOW_TITLE_BAR_HEIGHT = 20;
    private static final int FRAME_DELAY_MS = 16; // +- 60 fps

    private boolean running = true;

    private FindWords findWords;
    private Random random;

    private char[][] board;
    private boolean[][] selectedCells;
    private Color[][] cellColors;
    private List<String> selectedWords;
    private List<WordStatus> wordStatuses;

    private int selectedColumn = -1;
    private int selectedRow = -1;

    private record WordStatus(boolean found, int startOffsetY, int endOffsetY) {}

    public GameWindow() throws IOException {
        random = new Random();
        resetGame();

        JFrame frame = new JFrame("Find Words");

        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

        JButton restartButton = new JButton();
        restartButton.addActionListener(e -> {
            try {
                resetGame();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        restartButton.setBackground(Color.GRAY);
        restartButton.setSize(100, 50);
        restartButton.setBorderPainted(false);
        restartButton.setLocation((int) (WINDOW_WIDTH*0.92), (int) (WINDOW_HEIGHT*0.85));
        restartButton.setText("New Game");
        restartButton.setFocusPainted(false);

        frame.add(restartButton);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        createBufferStrategy(3);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();

                int colBoard = (mouseX - BOARD_START_X) / BOARD_CELL_SIZE;
                int rowBoard = (mouseY - BOARD_START_Y) / BOARD_CELL_SIZE;
                int colListWords = (mouseX - WORD_LIST_START_X) / WORD_LIST_CELL_SIZE;
                int rowListWords = (mouseY - WORD_LIST_START_Y +WINDOW_TITLE_BAR_HEIGHT) / WORD_LIST_CELL_SIZE;

                if (rowBoard >= 0 && rowBoard < board.length && colBoard >= 0 && colBoard < board[0].length) {
                    if (selectedColumn != -1 && selectedRow != -1) {
                        int dx = Integer.compare(colBoard, selectedColumn);
                        int dy = Integer.compare(rowBoard, selectedRow);

                        int deltaX = Math.abs(colBoard - selectedColumn);
                        int deltaY = Math.abs(rowBoard - selectedRow);

                        if (deltaX == 0 || deltaY == 0 || deltaX == deltaY) {

                            boolean newState = !selectedCells[selectedRow][selectedColumn];

                            int x = selectedColumn;
                            int y = selectedRow;
                            Color color = getRandomColor();

                            while (true) {
                                selectedCells[y][x] = newState;
                                cellColors[y][x] = color;

                                if (x == colBoard && y == rowBoard) {
                                    break;
                                }

                                x += dx;
                                y += dy;
                            }

                        }
                        selectedColumn = -1;
                        selectedRow = -1;
                    } else {
                        selectedColumn = colBoard;
                        selectedRow = rowBoard;
                    }
                }

                if (rowListWords >= 0 &&
                        rowListWords < selectedWords.size() &&
                        colListWords >= 0 &&
                        colListWords < 10) {
                    WordStatus wc = wordStatuses.get(rowListWords);

                    if (wc.found) {
                        wordStatuses.set(rowListWords,
                                new WordStatus(false, wc.startOffsetY, wc.endOffsetY));
                    } else {
                        wordStatuses.set(
                                rowListWords,
                                new WordStatus(true, random.nextInt(16), random.nextInt(16))
                        );
                    }
                }
            }
        });
    }

    @Override
    public void run() {
        BufferStrategy bs = getBufferStrategy();
        Font font = new Font("Monospaced", Font.BOLD, 28);

        while (running) {

            Graphics g = bs.getDrawGraphics();

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(font);

            g.setColor(Color.WHITE);
            for (int i = 0; i < selectedWords.size(); i++) {
                g.drawString(selectedWords.get(i),
                        WORD_LIST_START_X,
                        WORD_LIST_START_Y + WORD_LIST_CELL_SIZE *i);
                WordStatus wc = wordStatuses.get(i);
                if (wc.found) {
                    g.drawLine(WORD_LIST_START_X,
                            WORD_LIST_CELL_SIZE * i + WORD_LIST_START_Y - 15 + wc.startOffsetY,
                            WORD_LIST_START_X + selectedWords.get(i).length() * 17,
                            WORD_LIST_CELL_SIZE * i + WORD_LIST_START_Y - 15 + wc.endOffsetY
                    );
                }
            }

            FontMetrics fm = g.getFontMetrics();

            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {

                    int cellX = BOARD_START_X + j * BOARD_CELL_SIZE;
                    int cellY = BOARD_START_Y + i * BOARD_CELL_SIZE;

                    if (selectedCells[i][j]) {
                        g.setColor(cellColors[i][j]);
                        g.fillRect(cellX, cellY, BOARD_CELL_SIZE, BOARD_CELL_SIZE);
                    }

                    g.setColor(Color.WHITE);

                    int textX = cellX + (BOARD_CELL_SIZE - fm.charWidth(board[i][j])) / 2;
                    int textY = cellY + ((BOARD_CELL_SIZE - fm.getHeight()) / 2) + fm.getAscent();

                    g.drawString(String.valueOf(board[i][j]), textX, textY);
                }
            }

            g.dispose();
            bs.show();

            Toolkit.getDefaultToolkit().sync();

            try {
                Thread.sleep(FRAME_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public Color getRandomColor() {
        Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        return color;
    }

    private void resetGame() throws IOException {
        findWords = new FindWords();

        board = findWords.getBoardChars();
        selectedCells = new boolean[board.length][board[0].length];
        cellColors = new Color[board.length][board[0].length];

        selectedWords = new ArrayList<>(findWords.getChosenWords());
        Collections.shuffle(selectedWords, random);

        wordStatuses = new ArrayList<>();
        for (int i = 0; i < selectedWords.size(); i++) {
            wordStatuses.add(new WordStatus(false, 0, 0));
        }

        selectedColumn = -1;
        selectedRow = -1;
    }

    public static void main(String[] args) throws IOException {
        GameWindow game = new GameWindow();
        new Thread(game).start();
    }
}