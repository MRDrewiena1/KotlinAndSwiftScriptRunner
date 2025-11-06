import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ScriptRunner extends JFrame {
     private final JTextPane editor = new JTextPane();
     private final JTextArea output = new JTextArea();
     private final JComboBox<String> langSelect = new JComboBox<>(new String[]{"Kotlin","Swift"});
     private final JButton runButton = new JButton("Run");
     private final JButton stopButton = new JButton("Stop");
     private final JLabel statusLabel = new JLabel("Idle");
     private final JLabel exitLabel = new JLabel("Exit");

     private Process runningProcess;

     private final List<String> kotlinKeywords = List.of("func","let","var","if","else","for","while","return","class","import");
     private final List<String> swiftKeywords = List.of("fun","val","var","if","else","for","while","return","class","import");

     public ScriptRunner() {
         super("KotlinAndSwiftScriptRunner");

         setLayout(new BorderLayout());
         JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                 new JScrollPane(editor),new JScrollPane(output));
         splitPane.setDividerLocation(0.5);
         add(splitPane, BorderLayout.CENTER);

         JPanel topControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         topControlsPanel.add(new JLabel("Language:"));
         topControlsPanel.add(langSelect);
         topControlsPanel.add(runButton);
         topControlsPanel.add(stopButton);
         topControlsPanel.add(statusLabel);
         topControlsPanel.add(exitLabel);
         add(topControlsPanel, BorderLayout.NORTH);

         output.setEditable(false);
         loadCustomFont();

         runButton.addActionListener(e -> runScript());
         stopButton.addActionListener(e -> stopScript());
         editor.getDocument().addDocumentListener((SimpleDocumentListener) e -> highlightKeyWords());

         setSize(900,600);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setVisible(true);
     }

     private void runScript() {
         if(runningProcess != null && runningProcess.isAlive()) {
             JOptionPane.showMessageDialog(this, "Process already running!!!");
             return;
         }
         String code = editor.getText();
         String language = (String) langSelect.getSelectedItem();
         try {
             Path tmpFile = Files.createTempFile("script", language.equals("Kotlin") ? ".kts" : ".swift");
             Files.writeString(tmpFile, code);

             ProcessBuilder pb;
             if(language.equals("Kotlin")) {
                 pb = new ProcessBuilder("kotlinc","-script",tmpFile.toString());
             } else {
                 pb = new ProcessBuilder("/usr/bin/env","swift",tmpFile.toString());
             }

             pb.redirectErrorStream(true);
             runningProcess = pb.start();

             statusLabel.setText("Running...");
             exitLabel.setText("Exit: -");
             exitLabel.setForeground(Color.BLACK);
             output.setText("");

             new Thread(()->{
                 try (BufferedReader br = new BufferedReader(
                         new InputStreamReader(runningProcess.getInputStream()))) {
                     String line;
                     while ((line = br.readLine()) != null) {
                         final String ln = line;
                         SwingUtilities.invokeLater(()->{
                             appendLineWithLinks(ln);
                         });
                     }
                     int exitCode = runningProcess.waitFor();
                     SwingUtilities.invokeLater(()->{
                         statusLabel.setText("Idle");
                         exitLabel.setText("Exit: "+exitCode);
                         exitLabel.setForeground(exitCode == 0 ? Color.GREEN : Color.RED);
                     });
                 } catch (IOException e) {
                     e.printStackTrace();
                 } catch (InterruptedException e) {
                     throw new RuntimeException(e);
                 }
             }).start();
         } catch (IOException e) {
             output.setText("Error: " + e.getMessage());
         }
     }

     private void stopScript() {
         if(runningProcess != null && runningProcess.isAlive()) {
             runningProcess.destroy();
             statusLabel.setText("Stopped");
         }
     }

     private void highlightKeyWords() {
         SwingUtilities.invokeLater(()->{
             try {
                 StyledDocument document = editor.getStyledDocument();
                 StyleContext context = StyleContext.getDefaultStyleContext();
                 AttributeSet normal = context.addAttribute(
                         SimpleAttributeSet.EMPTY,
                         StyleConstants.Foreground,
                         Color.BLACK
                 );
                 AttributeSet keyword = context.addAttribute(
                         SimpleAttributeSet.EMPTY,
                         StyleConstants.Foreground,
                         Color.ORANGE
                 );

                 document.setCharacterAttributes(0, document.getLength(), normal, true);

                 List<String> keywords = langSelect.getSelectedItem()
                         .equals("Kotlin") ? kotlinKeywords : swiftKeywords;
                 String text = editor.getText(0,document.getLength());
                 for (String word : keywords) {
                     int i =  text.indexOf(word);
                     while (i >= 0) {
                         document.setCharacterAttributes(i, word.length(), keyword, false);
                         i = text.indexOf(word, i + 1);
                     }
                 }
             } catch (BadLocationException e) {
                 e.printStackTrace();
             }
         });
     }

     private void appendLineWithLinks(String line) {
         if(line.matches(".*:\\d+:\\d+: error:.*")){
             output.append(line+"\n");
             output.setCaretPosition(output.getDocument().getLength());

             int lineNum = extractLineNumber(line);
             if(lineNum > 0){
                 JButton link = new JButton(" Go to line "+lineNum);
                 link.setFont(new Font("Monospaced", Font.BOLD, 12));
                 link.setForeground(Color.RED);
                 link.addActionListener(e -> {
                     try {
                         int start = editor.getDocument()
                                 .getDefaultRootElement()
                                 .getElement(lineNum-1)
                                 .getStartOffset();
                         editor.requestFocus();
                         editor.setCaretPosition(start);
                         highlightLine(lineNum);
                     }catch (Exception ex) {
                         JOptionPane.showMessageDialog(
                                 this, "Line not found: "+lineNum,
                                 "Error", JOptionPane.ERROR_MESSAGE
                         );
                     }
                 });
                 output.append("  ");
                 output.insert("Click the link above. \n", output.getCaretPosition());
             }
         } else  {
             output.append(line+"\n");
         }
     }

     private int extractLineNumber(String line) {
         try {
             String[] parts = line.split(":");
             return Integer.parseInt(parts[1]);
         } catch (Exception e) {
             return -1;
         }
     }

     private void highlightLine(int lineNum) {
         try {
             int start = editor.getDocument()
                     .getDefaultRootElement()
                     .getElement(lineNum-1).getStartOffset();
             int end = editor.getDocument()
                     .getDefaultRootElement()
                     .getElement(lineNum-1).getEndOffset();

             editor.getHighlighter().removeAllHighlights();
             editor.getHighlighter().addHighlight(start,end,
                     new DefaultHighlighter.DefaultHighlightPainter(Color.RED));

             new Thread(()->{
                 try {
                     Thread.sleep(1000);
                     SwingUtilities.invokeLater(()->editor.getHighlighter().removeAllHighlights());
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }).start();

         } catch (Exception e) {
             e.printStackTrace();
         }
     }

     private void loadCustomFont(){
         try (InputStream is = getClass().getResourceAsStream("/src/resources/fonts/JetBrainsMono-Regular.ttf")) {
             if (is == null) {
                 System.err.println("Could not load Font");
                 return;
             }
             Font customFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(13f);
             GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
             ge.registerFont(customFont);

             editor.setFont(customFont);
             output.setFont(customFont);
             statusLabel.setFont(customFont);
             exitLabel.setFont(customFont);
             langSelect.setFont(customFont);
             runButton.setFont(customFont.deriveFont(Font.BOLD,13f));
             stopButton.setFont(customFont.deriveFont(Font.BOLD,13f));

         } catch (Exception e) {
             e.printStackTrace();
         }
     }

     public static void main(String[] args) {
         SwingUtilities.invokeLater(ScriptRunner::new);
     }

     interface SimpleDocumentListener extends DocumentListener {
         void update(DocumentEvent e);
         default void changedUpdate(DocumentEvent e) { update(e);}
         default void insertUpdate(DocumentEvent e) { update(e);}
         default void removeUpdate(DocumentEvent e) { update(e);}
     }
}
