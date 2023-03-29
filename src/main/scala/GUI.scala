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

object GUI {
    var index: Map[String, List[Int]] = Map.empty
    val textArea = new JTextArea()

    private def createWindow(): Unit = {
        val frame = new JFrame("Bigram Boolean Search")
        val panel = new JPanel();
        panel.setLayout(null)
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
        frame.setSize(1050, 600)
        frame.setResizable(false)
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        createUI(frame, panel)
        // frame.pack()
        frame.add(panel)
        frame.setVisible(true)
    }

    private def createUI(frame: JFrame, panel: JPanel): Unit = {
        createMenuBar(frame)
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
              val folderPath = folderChooser.getSelectedFile().getPath()
              synchronized {
                println("inside ")
                println("path: " + folderPath)    
              }
            }
          }
        })

        fileMenuItem2.addActionListener(new ActionListener {
          override def actionPerformed(e: ActionEvent): Unit = {
            if (fileChooser.showOpenDialog(fileMenuItem1) == JFileChooser.APPROVE_OPTION) {
              val filePath = fileChooser.getSelectedFile().getPath()
              synchronized {
                println("inside ")
                index = Indexer.loadIndex(filePath)                
                println(index.take(5).foreach(println))
              }
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
        buildVocabBtn.setBounds(30, 500, 150, 25)

        val buildIndexBtn = new JButton("Build Index")
        buildIndexBtn.setBounds(200, 500, 150, 25)

        buildVocabBtn.addActionListener(new ActionListener {
          override def actionPerformed(e: ActionEvent): Unit = {
            val liststr = List("123", "asafa")
            textArea.setText(liststr.mkString("\n"))
          }
        })

        panel.add(buildVocabBtn)
        panel.add(buildIndexBtn)
    }

    def createVocabArea(panel: JPanel): Unit = {
        textArea.setEditable(false)
        textArea.setColumns(1)
    
        val scrollPane = new JScrollPane(textArea)
        scrollPane.setBounds(30, 15, 150, 475)
        panel.add(scrollPane)
    }

    def refreshUI(frame: JFrame): Unit = {
        frame.getContentPane()
    }

    def main(args: Array[String]) {
        createWindow()
    }
}

