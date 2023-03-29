import javax.swing.{ JFrame, WindowConstants, JMenu, JMenuBar, JMenuItem, JSeparator, JFileChooser }
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.JTextArea
import java.awt.BorderLayout
import javax.swing.JScrollPane
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.BorderFactory
import javax.swing.ScrollPaneConstants
import javax.swing.UIManager

object GUI {
    var index: Map[String, List[Int]] = Map.empty
    var inputFolderPath = ""
    var indexFilePath = ""
    val vocabTextArea = new JTextArea()
    val filePathTextArea = new JTextArea(1, 1)
    val folderPathTextArea = new JTextArea(1, 1)

    private def createWindow(): Unit = {
        val frame = new JFrame("Bigram Boolean Search")
        val panel = new JPanel();
        panel.setLayout(null)
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
        frame.setSize(1050, 750)
        frame.setResizable(false)
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        createUI(frame, panel)
        // frame.pack()
        frame.add(panel)
        frame.setVisible(true)
    }

    private def createUI(frame: JFrame, panel: JPanel): Unit = {
        createMenuBar(frame)
        createPathsArea(panel)
        createVocabArea(panel)
        createButton(panel)
    }

    def createMenuBar(frame: JFrame): Unit = {
        val fileMenu = new JMenu("File")
        val helpMenu = new JMenu("Help")
        
        val fileChooser = new JFileChooser()
        val filter = new FileNameExtensionFilter("Index text file (*.txt)", "txt");
        fileChooser.setFileFilter(filter)
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY)
        fileChooser.setDialogTitle("Choose an index file")

        val folderChooser = new JFileChooser()
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setDialogTitle("Choose an input folder")

        val fileMenuItem1 = new JMenuItem("Open input folder")
        val fileMenuItem2 = new JMenuItem("Open index")
        val fileMenuItem3 = new JMenuItem("Quit")

        fileMenuItem1.addActionListener(new ActionListener {
          override def actionPerformed(e: ActionEvent): Unit = {
            if (folderChooser.showOpenDialog(fileMenuItem1) == JFileChooser.APPROVE_OPTION) {
              inputFolderPath = folderChooser.getSelectedFile().getPath()
            }
          }
        })

        fileMenuItem2.addActionListener(new ActionListener {
          override def actionPerformed(e: ActionEvent): Unit = {
            if (fileChooser.showOpenDialog(fileMenuItem1) == JFileChooser.APPROVE_OPTION) {
              indexFilePath = fileChooser.getSelectedFile().getPath()
            }
          }
        })

        fileMenuItem3.addActionListener(new ActionListener {
          override def actionPerformed(e: ActionEvent): Unit = {
            System.exit(0)
          }
        })

        fileMenu.add(fileMenuItem1)
        fileMenu.add(fileMenuItem2)
        fileMenu.add(new JSeparator()); // SEPARATOR        
        fileMenu.add(fileMenuItem3)

        val menuBar = new JMenuBar
        menuBar.add(fileMenu)
        menuBar.add(helpMenu)
        frame.setJMenuBar(menuBar)
    }

    def createButton(panel: JPanel): Unit = {
        val buildVocabBtn = new JButton("Build Vocab")
        buildVocabBtn.setBounds(30, 650, 150, 25)

        val buildIndexBtn = new JButton("Build Index")
        buildIndexBtn.setBounds(200, 650, 150, 25)

        buildVocabBtn.addActionListener(new ActionListener {
          override def actionPerformed(e: ActionEvent): Unit = {
            val liststr = List("123", "asafa")
            vocabTextArea.setText(liststr.mkString("\n"))
          }
        })

        panel.add(buildVocabBtn)
        panel.add(buildIndexBtn)
    }

    def createVocabArea(panel: JPanel): Unit = {
        vocabTextArea.setEditable(false)
        vocabTextArea.setColumns(1)
        
        val borderVocab = BorderFactory.createTitledBorder("Vocabulary")
        vocabTextArea.setBorder(borderVocab)

        val scrollPane = new JScrollPane(vocabTextArea)
        scrollPane.setBounds(30, 125, 150, 515)
        panel.add(scrollPane)
    }

    def createPathsArea(panel: JPanel): Unit = {
        UIManager.put("ScrollBar.width", 10);
        
        folderPathTextArea.setEditable(false)
        filePathTextArea.setEditable(false)
        
        val border1 = BorderFactory.createTitledBorder("Input folder")
        val scrollPane1 = new JScrollPane(folderPathTextArea)
        scrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER)
        folderPathTextArea.setBorder(border1)

        val border2 = BorderFactory.createTitledBorder("Index file")
        val scrollPane2 = new JScrollPane(filePathTextArea)
        scrollPane2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER)        
        filePathTextArea.setBorder(border2)

        folderPathTextArea.setText("/home/lovell/classroom/afsfas/f")
        filePathTextArea.setText("/home/lovell/classroom/afaf/awr3/afafs/inputreuter/12411.txt")

        scrollPane1.setBounds(30, 5, 325, 45)
        scrollPane2.setBounds(30, 60, 325, 45)

        panel.add(scrollPane1)
        panel.add(scrollPane2)        
    }

    def main(args: Array[String]) {
        createWindow()
    }
}

