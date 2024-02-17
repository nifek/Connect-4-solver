package Connect4;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class UI {
	private int n, m;
	private JFrame frame;
	private JButton[] buttons;
	private JLabel[][] board;
	private JLayeredPane layeredPane;
	private JPanel board_panel;	
	private JButton computer_move;
	private JButton take_back;
	private JButton reset;
    private Solver solver;
    private Stack<Integer> move_history;
    private int layered_pane_width, layered_pane_height;
    public static final int cellSize = 75;
    private static Icon red_disc = resizeIcon(new ImageIcon("src/Connect4/Files/Icons/red_disc.png"), cellSize, cellSize);
    private static Icon yellow_disc = resizeIcon(new ImageIcon("src/Connect4/Files/Icons/yellow_disc.png"), cellSize, cellSize);
    private static Icon red_disc_crossed = resizeIcon(new ImageIcon("src/Connect4/Files/Icons/red_disc_crossed.png"), cellSize, cellSize);
    private static Icon yellow_disc_crossed = resizeIcon(new ImageIcon("src/Connect4/Files/Icons/yellow_disc_crossed.png"), cellSize, cellSize);
    private static Icon empty_disc = resizeIcon(new ImageIcon("src/Connect4/Files/Icons/empty_disc.png"), cellSize, cellSize);
    
    public UI(int N, int M) {
    	n = N; m = M;
    	frame = new JFrame("Connect 4 Solver");
        layeredPane = new JLayeredPane();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    	solver = new Solver(n, m);
    	buttons = new JButton[n];
    	board = new JLabel[m][n];
    	move_history = new Stack<Integer>();

        board_panel = new JPanel();
        board_panel.setLayout(new GridLayout(m, n));
        board_panel.setPreferredSize(new Dimension(n * cellSize, m * cellSize));

        for (int j = 0; j < m; j++) {
            for (int i = 0; i < n; i++) {
                JLabel cell = new JLabel();
                cell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cell.setOpaque(true);
                cell.setBackground(Color.WHITE);
                cell.setIcon(empty_disc);
                board[j][i] = cell; 
                board_panel.add(cell);
            }
        }

        board_panel.setBounds(0, 0, n * cellSize, m * cellSize);
        layeredPane.add(board_panel, 1);

        for (int j = 0; j < n; j++) {
            JButton button = new JButton();
            button.setActionCommand((j + 1) + "");
            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.addActionListener(new ColumnButtonListener());
            button.setBounds(j * cellSize, 0, cellSize, n * cellSize);
            layeredPane.add(button, 2);
            buttons[j] = button;
        }
        
        computer_move = new JButton("Computer Move");
        computer_move.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                makeComputerMove();
            }
        });
        computer_move.setContentAreaFilled(false);
        computer_move.setFocusPainted(false);
        computer_move.setSize(150, 50);
        
        take_back = new JButton("Back");
        take_back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                unmake_move();
            }
        });
        take_back.setContentAreaFilled(false);
        take_back.setFocusPainted(false);
        take_back.setSize(150, 50);
        
        reset = new JButton("Reset");
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                while(!move_history.isEmpty()) unmake_move();
            }
        });
        reset.setContentAreaFilled(false);
        reset.setFocusPainted(false);
        reset.setSize(150, 50);

        layered_pane_width = board_panel.getPreferredSize().width;
        layered_pane_height = board_panel.getPreferredSize().height;
        
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
            	draw();
            }
        });
        
        frame.add(computer_move);
        frame.add(take_back);
        frame.add(reset);
        frame.add(layeredPane);
        
        frame.setLocationRelativeTo(null);
    }
    
    private void draw() {
    	Dimension frameSize = frame.getSize();
        int x = (frameSize.width - layered_pane_width) / 2;
        int y = (frameSize.height - layered_pane_height) / 2;
        layeredPane.setBounds(x, y, layered_pane_width, layered_pane_height);
        center_component(computer_move, frameSize.width / 2 - layered_pane_width / 2 - 150, frameSize.height / 2 - 30, false);
        center_component(take_back, frameSize.width / 2 - layered_pane_width / 2 - 150, frameSize.height / 2 + 30, false);
        center_component(reset, frameSize.width / 2 - layered_pane_width / 2 - 150, frameSize.height / 2 + 90, false);
    }
    
    private class ColumnButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int column = Integer.parseInt(e.getActionCommand()) - 1;
            make_move(column, false);
        }
    }

    private void makeComputerMove() {
    	if(solver.board.hasWon(solver.board.board ^ solver.board.player_board) || solver.board.hasWon(solver.board.player_board) || solver.board.isDraw()) return;
        int result[] = solver.getBestMove();
        make_move(solver.board.column_index[result[1]], true);
    }
    
    private void make_move(int j, boolean computerMove) {
    	if(!solver.board.isLegal(j) || solver.board.hasWon(solver.board.board ^ solver.board.player_board) || solver.board.hasWon(solver.board.player_board)) return;
    	for (int i = m - 1; i >= 0; i--) {
            if (board[i][j].getIcon() == empty_disc) {
            	board[i][j].setIcon(solver.board.mover? yellow_disc : red_disc);
                break;
            }
        }
        solver.board.make_move(solver.board.getMove(j));
        move_history.push(j);
        for(int i = 0; i < m; i++) {
        	for(int k = 0; k < n; k++) {
        		if(board[i][k].getIcon() == empty_disc) continue;
        		int dx[] = {1, 1, 0, 1};
        		int dy[] = {0, 1, 1, -1};
        		Icon need_one = red_disc;
        		Icon need_two = red_disc_crossed;
        		if(board[i][k].getIcon() == yellow_disc || board[i][k].getIcon() == yellow_disc_crossed) {
        			need_one = yellow_disc;
        			need_two = yellow_disc_crossed;
        		}
        		for(int p = 0; p < 4; p++) {
        			int cnt = 0;
        			while(i + cnt * dx[p] < m && k + cnt * dy[p] < n && k + cnt * dy[p] >= 0 && (board[i + cnt * dx[p]][k + cnt * dy[p]].getIcon() == need_one || board[i + cnt * dx[p]][k + cnt * dy[p]].getIcon() == need_two)) cnt++;
        			if(cnt < 4) continue;
        			Icon icon_set = red_disc_crossed;
        			if(board[i][k].getIcon() == yellow_disc || board[i][k].getIcon() == yellow_disc_crossed) icon_set = yellow_disc_crossed;
        			for(int c = 0; c < cnt; c++) {
        				int ni = i + c * dx[p];
        				int nk = k + c * dy[p];
        				board[ni][nk].setIcon(icon_set);
        			}
        		}
        	}
        }
    }
    
    private void unmake_move() {
    	if(move_history.isEmpty()) return;
    	int j = move_history.pop();
    	for(int i = 0; i < m; i++) {
    		if (board[i][j].getIcon() != empty_disc) {
            	board[i][j].setIcon(empty_disc);
            	int move = solver.board.getIndex(i + 1, j);
            	solver.board.unmake_move(move);
                break;
            }
    	}
    	for(int i = 0; i < m; i++) {
    		for(int k = 0; k < n; k++) {
    			if(board[i][k].getIcon() == yellow_disc_crossed) board[i][k].setIcon(yellow_disc);
    			if(board[i][k].getIcon() == red_disc_crossed) board[i][k].setIcon(red_disc);
    		}
    	}
    }
    
    private static ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }
    
    private static void center_component(JComponent component, int x, int y, boolean resize) {
		if(component.getWidth() == 0 || resize) {
			component.setSize(component.getPreferredSize());
		}
		int X = x - component.getWidth() / 2;
		int Y = y - component.getHeight() / 2;
		component.setBounds(X, Y, component.getWidth(), component.getHeight());
	}
    
    public void run() {
    	frame.setVisible(true);
    }
    
    public void clear() {
    	solver.clear();
    }
}
