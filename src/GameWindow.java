import java.util.List;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.Random;

public class GameWindow extends Canvas implements Runnable {

    private boolean running = true;
    private final FindWords findWords;
    protected Random rand;
    protected int boardStartX = 550;
    protected int boardStartY = 80;
    protected int boardCellSize = 40;
    protected int listWordsStartX = 10;
    protected int listWordsStartY = 40;
    protected int listWordsCellSize = 30;
    protected int clickX = -1, clickY = -1;
    protected final char[][] board;
    protected final boolean[][] selected;
    protected final Color[][] colors;
    protected final List<String> words;
    protected final boolean[] wordsChange;

    public GameWindow() throws IOException {
        findWords = new FindWords();
        rand = new Random();
        board = findWords.getBoardChars();
        selected = new boolean[board.length][board[0].length];
        colors = new Color[board.length][board[0].length];
        words = findWords.getChosenWords();
        wordsChange = new boolean[words.size()];
        JFrame frame = new JFrame("Find Words");

        setPreferredSize(new Dimension(1920, 1060));

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

                int col = (mouseX - boardStartX) / boardCellSize;
                int row = (mouseY - boardStartY) / boardCellSize;

                if (row >= 0 && row < board.length && col >= 0 && col < board[0].length) {
                    if (clickX != -1 && clickY != -1) {
                        int dx = Integer.compare(col, clickX);
                        int dy = Integer.compare(row, clickY);

                        int deltaX = Math.abs(col - clickX);
                        int deltaY = Math.abs(row - clickY);

                        if (deltaX == 0 || deltaY == 0 || deltaX == deltaY) {

                            boolean newState = !selected[clickY][clickX];

                            int x = clickX;
                            int y = clickY;
                            Color color = getRandomColor();

                            while (true) {
                                selected[y][x] = newState;
                                colors[y][x] = color;

                                if (x == col && y == row) {
                                    break;
                                }

                                x += dx;
                                y += dy;
                            }

                        }
                        clickX = -1;
                        clickY = -1;
                    } else {
                        clickX = col;
                        clickY = row;
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
            for (int i = 0; i < words.size(); i++) {
                g.drawString(words.get(i), listWordsStartX, listWordsStartY+listWordsCellSize*i);
                if (!wordsChange[i]) {
                    g.drawLine(listWordsStartX, listWordsCellSize*i+listWordsStartY-17+rand.nextInt(18), listWordsStartX+words.get(i).length()*17, listWordsCellSize*i+listWordsStartY-17+rand.nextInt(18));
                }
            }

            FontMetrics fm = g.getFontMetrics();

            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {

                    int cellX = boardStartX + j * boardCellSize;
                    int cellY = boardStartY + i * boardCellSize;

                    if (selected[i][j]) {
                        g.setColor(colors[i][j]);
                        g.fillRect(cellX, cellY, boardCellSize, boardCellSize);
                    }

                    g.setColor(Color.WHITE);

                    int textX = cellX + (boardCellSize - fm.charWidth(board[i][j])) / 2;
                    int textY = cellY + ((boardCellSize - fm.getHeight()) / 2) + fm.getAscent();

                    g.drawString(String.valueOf(board[i][j]), textX, textY);
                }
            }

            g.dispose();
            bs.show();

            Toolkit.getDefaultToolkit().sync();

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public Color getRandomColor() {
        Color color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
        return color;
    }

    public static void main(String[] args) throws IOException {
        GameWindow game = new GameWindow();
        new Thread(game).start();
    }
}