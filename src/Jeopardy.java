package org.superthread.jeopardy;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Trivia gameshow application in the style of Jeopardy.
 * 
 * @author <a href="mailto:drew@drewmjohnson.com">Drew Johnson</a>
 * @version 0.0.1
 */
class Jeopardy implements /*ActionListener*/ ComponentListener{
	
	private JFrame clueFrame;
	private JFrame controlFrame;
	private ClueData clueData;
	
	private static final Color jepBlue = new Color(15, 15, 125);
	
	/**
	 * Constructor for the Jeopardy class, creates the GUI.
	 */
	protected Jeopardy() {
		
		clueData = new ClueData();
		
		// Create components for clue board window
		Container textPaneContainer = new Container();
		
		// Create and add a 6x6 grid of JTextPanes to the Container
		for (int i=0; i<6; i++) {
			for (int j=0; j<6; j++) {
				JTextPane newTextPane = new JTextPane();
				newTextPane.setEditable(false);
				newTextPane.setFont(new Font("Helvetica", Font.BOLD, 25));
				newTextPane.setOpaque(true);
				newTextPane.setBackground(jepBlue);
				StyledDocument doc = newTextPane.getStyledDocument();
				SimpleAttributeSet center = new SimpleAttributeSet();
				StyleConstants.setAlignment(center,StyleConstants.ALIGN_CENTER);
				doc.setParagraphAttributes(0, doc.getLength(), center, false);
				//newTextPane.setFont(new Font("Helvetica", Font.BOLD, 25));
				if (i == 0)
					newTextPane.setForeground(Color.WHITE);
				else
					newTextPane.setForeground(Color.YELLOW);
				textPaneContainer.add(newTextPane);
			}
		}
		
		// Create text pane for enlarged clue text
		JTextPane bigCluePane = new JTextPane();
		bigCluePane.setEditable(false);
		bigCluePane.setFont(new Font(Font.SERIF, Font.PLAIN, 25));
		bigCluePane.setOpaque(true);
		bigCluePane.setBackground(jepBlue);
		StyledDocument doc = bigCluePane.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center,StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);
		bigCluePane.setForeground(Color.WHITE);
		
		Container bigClueContainer = new Container();
		bigClueContainer.setPreferredSize(new Dimension(30, 30));
		bigClueContainer.add(bigCluePane);

		// Create black background panel
		JPanel jeopardyPanel = new JPanel();
		jeopardyPanel.setPreferredSize(new Dimension(30, 30));
		jeopardyPanel.setBackground(Color.BLACK);

		// TODO: white Helvetica for Category Headings and yellow Dollar Values
		// TODO: ALL-CAPS ITC Korinna white w/ black drop shadow for Clues
		
		// Create components for control panel window
		Container buttonContainer = new Container();
		
		// Create and add a 6x6 grid of buttons corresponding to clues on board
		for (int i=0; i<6; i++) {
			for (int j=0; j<6; j++) {
				
				JButton newControlButton = new JButton("(" + i + "," + j + ")");
				newControlButton.addActionListener(new ButtonListener());
				newControlButton.setMargin(new Insets(0,0,0,0));
				buttonContainer.add(newControlButton);
			}
		}
		
		
		JMenuItem loadFirst = new JMenuItem("Load Round One Clues");
		loadFirst.addActionListener(new ButtonListener());
		JMenuItem loadSecond = new JMenuItem("Load Round Two Clues");
		loadSecond.addActionListener(new ButtonListener());
		JMenuItem loadFinal = new JMenuItem("Load Final Clue");
		loadFinal.addActionListener(new ButtonListener());
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(loadFirst);
		fileMenu.add(loadSecond);
		fileMenu.add(loadFinal);
		
		JMenuBar controlMenuBar = new JMenuBar();
		controlMenuBar.add(fileMenu);
		
		
		// Create and configure clue board window
		clueFrame = new JFrame();
		
		clueFrame.add(jeopardyPanel);
		clueFrame.add(textPaneContainer);
		clueFrame.add(bigClueContainer);
		
		clueFrame.setPreferredSize(new Dimension(800, 500));
		
		//clueFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		clueFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//clueFrame.setExtendedState(Frame.MAXIMIZED_BOTH); // Fullscreen
	    //clueFrame.setUndecorated(true); // Borderless
		clueFrame.setTitle("Superthread Trivia App");
		clueFrame.addComponentListener(this);
		
		// Create and configure control panel window
		controlFrame = new JFrame();
		
		controlFrame.add(buttonContainer);
		controlFrame.setJMenuBar(controlMenuBar);
		
		controlFrame.setTitle("Superthread Trivia Control Panel");
		controlFrame.setPreferredSize(new Dimension(300, 300));
		controlFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		controlFrame.addComponentListener(this);
		
		// Final adjustments and reveal
		resizeControlComponents();
		resizeClueComponents();
		controlFrame.pack();
		clueFrame.pack();
		controlFrame.setVisible(true);
		clueFrame.setVisible(true);
	}
	
	/**
	 * Helper method that gets the container holding the clue JTextPanes.
	 * 
	 * @return The container holding the JTextPanes.
	 */
	protected Container getClueContain() {
		return (Container) clueFrame.getContentPane().getComponent(1);
	}
	
	/**
	 * Helper method that gets a specific JTextPane from the clue container.
	 * 
	 * @param index the index of the JTextPane clue component to get
	 * @return The JTextPane clue component specified by the given index.
	 */
	protected JTextPane getClueComponent(int index) {
		return (JTextPane) getClueContain().getComponent(index);
	}
	
	/**
	 * Helper method that gets the container holding the controlFrame's JButtons.
	 * 
	 * @return The container holding the JButtons.
	 */
	protected Container getControlContain() {
		return (Container) controlFrame.getContentPane().getComponent(0);
	}
	
	/**
	 * Helper method that gets the specific JButton from the container.
	 * 
	 * @param index the index of the JButton component to get
	 * @return The JButton specified by the given index.
	 */
	protected JButton getControlComponent(int index) {
		return (JButton) getControlContain().getComponent(index);
	}
	
	/**
	 * Helper method to resize all the visible components in the clueFrame.
	 */
	private void resizeClueComponents() {
		
		// Resize background panel
		JPanel jeopardyPanel = (JPanel) clueFrame.getContentPane().getComponent(0);
		jeopardyPanel.setPreferredSize(
				new Dimension(clueFrame.getWidth(), clueFrame.getHeight()));
		
		// Resize and position the 6x6 grid of text panes
		int x = 0;
		int y = 0;
		Container textPaneContainer = getClueContain();
		for (int i=0; i<textPaneContainer.getComponentCount(); i++) {
			JTextPane tempPane = getClueComponent(i);
			tempPane.setPreferredSize( 
					new Dimension(
							(int)((clueFrame.getWidth() - 50) / 6.0), 
							(int)((clueFrame.getHeight() - 70) / 6.0)));
			
			tempPane.setLocation(
					tempPane.getWidth() * x + 5 * (x+1), 
					tempPane.getHeight() * y + 5 * (y+1));
			
			Font newFont;
			float sizeFactor = (float)Math.sqrt(
					Math.pow(clueFrame.getHeight(),2) + 
					Math.pow(clueFrame.getWidth(),2));
			if (y == 0)
				newFont = tempPane.getFont().deriveFont(sizeFactor / 60);
			else
				newFont = tempPane.getFont().deriveFont(sizeFactor/28);
			tempPane.setFont(newFont);
			
			x++;
			if (x > 5) {
				x = 0;
				y++;
			}
		}
		
		// Resize and position the (usually-invisible) bigCluePane
		Container bigClueContainer = (Container) clueFrame.getContentPane().getComponent(2);
		JTextPane bigCluePane = (JTextPane) bigClueContainer.getComponent(0);
		//bigCluePane.setSize(
		//		(clueFrame.getWidth() - 50), 
		//		(clueFrame.getHeight() - 70)*(5/6));
		//bigCluePane.setLocation(
		//		bigCluePane.getWidth() + 5,
		//		bigCluePane.getHeight() + 5 + (clueFrame.getHeight() - 70)/6 );
		//bigCluePane.setSize(20, 20);
		bigCluePane.setPreferredSize(
				new Dimension(
						(int)((clueFrame.getWidth() - 50) / 6.0), 
						(int)((clueFrame.getHeight() - 70) / 6.0)));
		
		bigCluePane.setLocation(
				bigCluePane.getWidth() * x + 5 * (x+1), 
				bigCluePane.getHeight() * y + 5 * (y+1));
	}
	
	/**
	 * Helper method to resize all the visible components in the controlFrame.
	 */
	private void resizeControlComponents() {
		
		int x = 0;
		int y = 0;
		Container buttonContainer = getControlContain();
		for (int i=0; i<buttonContainer.getComponentCount(); i++) {
			JButton tempBttn = getControlComponent(i);
			tempBttn.setPreferredSize( 
					new Dimension(
							(int)((controlFrame.getWidth() - 50) / 6.0), 
							(int)((controlFrame.getHeight() - 95) / 6.0)));
			tempBttn.setLocation(
					tempBttn.getWidth() * x + 5 * (x+1), 
					tempBttn.getHeight() * y + 5 * (y+1));
			
			Font newFont;
			float sizeFactor = (float)Math.sqrt(
					Math.pow(controlFrame.getHeight(),2) + 
					Math.pow(controlFrame.getWidth(),2));
			if (y == 0) {
				newFont = tempBttn.getFont().deriveFont(sizeFactor/32);
			} 
			else {
				newFont = tempBttn.getFont().deriveFont(sizeFactor/32);
			}
			tempBttn.setFont(newFont);
			
			x++;
			if (x > 5) {
				x = 0;
				y++;
			}
		}
	}

	/**
	 * Empty method, required when implementing ComponentListener.
	 * 
	 * @param evt the ComponentEvent object from the event that called this method
	 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
	 */
	@Override
	public void componentHidden(ComponentEvent evt) {
		// Do nothing
	}

	/**
	 * Calls helper method to resize appropriate components on frame movement.
	 * 
	 * @param evt the ComponentEvent object from the event that called this method
	 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
	 */
	@Override
	public void componentMoved(ComponentEvent evt) {
		if (evt.getSource() == clueFrame)
			resizeClueComponents();
	}

	/**
	 * Calls helper methods to resize appropriate components on frame resizing.
	 * 
	 * @param evt the ComponentEvent object from the event that called this method
	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	@Override
	public void componentResized(ComponentEvent evt) {
		if (evt.getSource() == clueFrame)
			resizeClueComponents();
		else if (evt.getSource() == controlFrame)
			resizeControlComponents();
	}

	/**
	 * Empty method, required when implementing ComponentListener.
	 * 
	 * @param evt the ComponentEvent object from the event that called this method
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	@Override
	public void componentShown(ComponentEvent evt) {
		// Do nothing
	}
	
	/**
	 * A listener class for button and menu actions performed in the controlFrame.
	 */
	private class ButtonListener implements ActionListener {
		
		/**
		 * Listener method triggered when an action is performed on components being listened to/
		 * 
		 * @param evt the ActionEvent object from the event that called this method
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent evt) {
			if (evt.getSource() instanceof JButton){
				Container compareCont = 
						(Container)controlFrame.getContentPane().getComponent(0);

				int buttonID = 0;
				for (int i=0; i<compareCont.getComponentCount(); i++) {
					if (evt.getSource() == compareCont.getComponent(i)) {
						buttonID = i;
						break;
					}
				}
				Container clueCont = 
						(Container)clueFrame.getContentPane().getComponent(1);
				JTextPane selectedClue = 
						(JTextPane)clueCont.getComponent(buttonID);
				//selectedClue.setText(t);
				selectedClue.setText("");
			}
			else if (evt.getSource() == controlFrame.getJMenuBar().getMenu(0).getMenuComponent(0)) {
				clueData.getNewClues(1);
				clueData.loadClueTexts(1);
			}
			else if (evt.getSource() == controlFrame.getJMenuBar().getMenu(0).getMenuComponent(1)) {
				clueData.getNewClues(2);
				clueData.loadClueTexts(2);
			}
			else if (evt.getSource() == controlFrame.getJMenuBar().getMenu(0).getMenuComponent(2)) {
				clueData.getNewClues(3);
				clueData.loadClueTexts(3);
			}
		}
	}
	
	/**
	 * A data structure class for holding data pertaining to trivia clues.
	 */
	private class ClueData {

		private String[][] roundOneClues;
		private String[][] roundTwoClues;
		private String finalClue;
		
		private String[][] roundOneAns;
		private String[][] roundTwoAns;
		private String finalAns;
		
		/**
		 * Constructs the appropriate arrays and Strings for holding clue data.
		 */
		private ClueData() {
			roundOneClues = new String[6][6];
			roundTwoClues = new String[6][6];
			finalClue = new String();
			
			roundOneAns = new String[6][6];
			roundTwoAns = new String[6][6];
			finalAns = new String();
		}
		
		/**
		 * Fetches fresh random clue data from jService, a Jeopardy data API.
		 * 
		 * @param roundNum either 1, 2, or 3 used to indicate which round of trivia to load
		 * @return False if an exception is encountered, otherwise returns true.
		 */
		private boolean getNewClues(int roundNum) {
			
			for (int i=0; i<6; i++) {

				Random rando = new Random();
				int randCat = 1 + rando.nextInt(18419);

				try {
					URL url = new URL("http://jservice.io/api/category?id=" + 
							randCat);
					InputStream inStream = url.openStream();
					BufferedReader inReader = new BufferedReader(
							new InputStreamReader(inStream));
					String inString = inReader.readLine();

					Pattern catPat = Pattern.compile("title\":\"(.+)\",\"clues");
					Matcher catMatch = catPat.matcher(inString);
					catMatch.find();
					if (roundNum == 1) {
						roundOneClues[i][0] = catMatch.group(1);
						for (int j=1; j<6; j++) {
							int qVal = 200 * j;
							catPat = Pattern.compile("answer\":\"(.+?)\",\"" +
									"question\":\"(.+?)\",\"value\":" + qVal);
							catMatch = catPat.matcher(inString);
							if (catMatch.find()) {
								roundOneAns[i][j] = catMatch.group(1);
								roundOneClues[i][j] = catMatch.group(2);
								System.out.println(catMatch.group(1));
								System.out.println(catMatch.group(2));
								inString = inString.substring(catMatch.end());
								// TODO: REGEX still too greedy, grabs old values from 1990s
							}
						}
					}
					else if (roundNum == 2) {
						roundTwoClues[i][0] = catMatch.group(1);
						for (int j=1; j<6; j++) {
							int qVal = 200 * j;
							catPat = Pattern.compile("answer\":\"(.+?)\",\"" +
									"question\":\"(.+?)\",\"value\":" + qVal);
							catMatch = catPat.matcher(inString);
							if (catMatch.find()) {
								roundTwoAns[i][j] = catMatch.group(1);
								roundTwoClues[i][j] = catMatch.group(2);
								inString = inString.substring(catMatch.end());
							}
						}
					}
					else if (roundNum == 3) {
						finalClue = catMatch.group(1);
						int qVal = 200 * (rando.nextInt(5) + 1);
						catPat = Pattern.compile("answer\":\"(.+?)\",\"" +
								"question\":\"(.+?)\",\"value\":" + qVal);
						catMatch = catPat.matcher(inString);
						if (catMatch.find()) {
							finalAns = catMatch.group(1);
						}
					}
					
					
					inReader.close();
					inStream.close();
					
				} catch (Exception e) {
					return false;
				}
			}
			return true;
		}
		
		/**
		 * Loads the trivia clues from objects in memory to the GUI.
		 * 
		 * @param roundNum either 1, 2, or 3 used to indicate which round of trivia to load
		 */
		private void loadClueTexts(int roundNum) {

			for (int i=0; i < 36; i++) {
				
				int x = i % 6;
				int y = i / 6;
				
				if (roundNum == 1) {
					if (y == 0)
						getClueComponent(i).setText(cleanString(roundOneClues[x][y].toUpperCase()));
					else
						getClueComponent(i).setText("$" + (200 * y));
				}
				else if (roundNum == 2) {
					if (y == 0)
						getClueComponent(i).setText(cleanString(roundTwoClues[x][y].toUpperCase()));
					else
						getClueComponent(i).setText("$" + (400 * y));
				}
				else if (roundNum ==3) {
					if (y == 0)
						getClueComponent(i).setText(cleanString(finalClue.toUpperCase()));
					else {
						getClueComponent(i).setText("Final");
					}
				}
			}
		}
		
		/**
		 * Helper method for replacing improperly formatted characters from the 
		 * jService clues with properly-displaying ones.
		 * 
		 * @param dirtyStr the "dirty" String to clean-up
		 * @return A cleaned-up version of the given "dirty" String
		 */
		private String cleanString(String dirtyStr) {
			String cleanStr;
			cleanStr = dirtyStr.replace("\\\"", "\"");
			cleanStr = cleanStr.replace("\\U0026", "&");
			return cleanStr;
		}
	}
}
