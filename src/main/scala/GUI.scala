import javax.swing.{
    JFrame,
    WindowConstants,
    JMenu,
    JMenuBar,
    JMenuItem,
    JSeparator,
    JFileChooser,
    JScrollPane,
    JButton,
    JPanel,
    BorderFactory,
    ScrollPaneConstants,
    UIManager,
    JOptionPane,
    JTextArea
}
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import javax.swing.filechooser.FileNameExtensionFilter
import java.awt.BorderLayout
import javax.swing.ImageIcon
import javax.swing.JDialog
import javax.swing.JProgressBar
import javax.swing.JLabel
import java.awt.Dialog.ModalityType
import javax.swing.SwingWorker

object GUI {
    var index: Map[String, List[Int]] = Map.empty
    var inputFolderPath = ""
    var indexFilePath = ""
    var combineVocabulary: List[String] = List.empty

    val vocabTextArea = new JTextArea()
    val indexTextArea = new JTextArea()

    val filePathTextArea = new JTextArea(1, 1)
    val folderPathTextArea = new JTextArea(1, 1)

    val loadingDialog = new JDialog()

    val loadingIcon = new ImageIcon("assets/loading.gif")
    val errorIcon = new ImageIcon("assets/error.png")

    val globalFrame = new JFrame("Bigram Boolean Search")
    val fileChooser = new JFileChooser()

  class buildVocabWorker extends SwingWorker[String, Unit] {
    override def doInBackground(): String = {
      // Do the time-consuming task here
      Thread.sleep(5000)
      "In background thread"
    }
    override def done(): Unit = {
      loadingDialog.setVisible(false)
      globalFrame.setEnabled(true)
      println("Button clicked  " + get())
    }
  }
  class loadIndexWorker extends SwingWorker[Unit, Unit] {
    override def doInBackground(): Unit = {
      indexFilePath = fileChooser.getSelectedFile().getPath()
      filePathTextArea.setText(indexFilePath)
      index = Indexer.sortIndex(Indexer.loadIndex(indexFilePath))
      indexTextArea.setText(index.map { case (term, docIds) => term + "\t\t|\t" + docIds.mkString(" ") }.mkString("\n"))
    }
    override def done(): Unit = {
      loadingDialog.setVisible(false)
      globalFrame.setEnabled(true)
    }
  }  

  private def createWindow(): Unit = {
    val panel = new JPanel();
    panel.setLayout(null)
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
    globalFrame.setSize(1050, 750)
    globalFrame.setResizable(false)
    globalFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    createUI(globalFrame, panel)
    // globalFrame.pack()
    globalFrame.add(panel)
    globalFrame.setVisible(true)
  }

  private def createUI(frame: JFrame, panel: JPanel): Unit = {
    createMenuBar(frame)
    createPathsArea(panel)
    createVocabArea(panel)
    createIndexArea(panel)
    createButton(panel)
    createLoadingFrame()
  }

  def createMenuBar(frame: JFrame): Unit = {
    val fileMenu = new JMenu("File")
    val helpMenu = new JMenu("Help")

    val filter = new FileNameExtensionFilter("Index text file (*.txt)", "txt");
    fileChooser.setFileFilter(filter)
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY)
    fileChooser.setDialogTitle("Select an index file")

    val folderChooser = new JFileChooser()
    folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    folderChooser.setDialogTitle("Select an input folder")

    val fileMenuItem1 = new JMenuItem("Select input folder")
    val fileMenuItem2 = new JMenuItem("Open index file")
    val fileMenuItem3 = new JMenuItem("Quit")

    fileMenuItem1.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        if (
          folderChooser
            .showOpenDialog(fileMenuItem1) == JFileChooser.APPROVE_OPTION
        ) {
          inputFolderPath = folderChooser.getSelectedFile().getPath()
          folderPathTextArea.setText(inputFolderPath)
        }
      }
    })

    fileMenuItem2.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        if (
          fileChooser
            .showOpenDialog(fileMenuItem1) == JFileChooser.APPROVE_OPTION
        ) {
          val worker = new loadIndexWorker
          loadingDialog.setVisible(true)
          globalFrame.setEnabled(false)
          worker.execute()
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
        if (inputFolderPath == "") {
            JOptionPane.showMessageDialog(null, "No input folder chosen!", "Error", JOptionPane.ERROR_MESSAGE, errorIcon);
        }
        else {    
            loadingDialog.setVisible(true)
            val (pairs, unigramVocab, bigramVocab) = Indexer.buildVocab(inputFolderPath)
            combineVocabulary = unigramVocab ++ bigramVocab
            vocabTextArea.setText(combineVocabulary.sorted.mkString("\n"))
            loadingDialog.setVisible(false)
        }
      }
    })

    // buildIndexBtn.addActionListener(new ActionListener {
    //   override def actionPerformed(e: ActionEvent): Unit = {
    //     if (combineVocabulary.isEmpty) {
    //         JOptionPane.showMessageDialog(null, "No vocabulary built!", "Error", JOptionPane.ERROR_MESSAGE, errorIcon);
    //     }
    //     else {    
    //         loadingDialog.setVisible(true)
    //         println("build index....")
    //         loadingDialog.setVisible(false)
    //     }
    //   }
    // })    

    buildIndexBtn.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        val worker = new buildVocabWorker
        globalFrame.setEnabled(false)
        loadingDialog.setVisible(true)
        worker.execute()
      }
    })    

    panel.add(buildVocabBtn)
    panel.add(buildIndexBtn)
  }

  def createVocabArea(panel: JPanel): Unit = {
    vocabTextArea.setEditable(false)
    vocabTextArea.setColumns(1)

    val borderVocab = BorderFactory.createTitledBorder("Vocabulary")
    val scrollPane = new JScrollPane(vocabTextArea)
    scrollPane.setBorder(borderVocab)
    scrollPane.setBounds(30, 125, 150, 515)
    panel.add(scrollPane)
  }

  def createIndexArea(panel: JPanel): Unit = {
    indexTextArea.setEditable(false)
    indexTextArea.setColumns(1)

    val borderIndex = BorderFactory.createTitledBorder("Index")
    val scrollPane = new JScrollPane(indexTextArea)
    scrollPane.setBorder(borderIndex)
    scrollPane.setBounds(225, 125, 750, 515)
    panel.add(scrollPane)
  }

  def createPathsArea(panel: JPanel): Unit = {
    UIManager.put("ScrollBar.width", 12);

    folderPathTextArea.setEditable(false)
    filePathTextArea.setEditable(false)

    val border1 = BorderFactory.createTitledBorder("Input folder")
    val scrollPane1 = new JScrollPane(folderPathTextArea)
    scrollPane1.setVerticalScrollBarPolicy(
      ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
    )

    val border2 = BorderFactory.createTitledBorder("Index file")
    val scrollPane2 = new JScrollPane(filePathTextArea)
    scrollPane2.setVerticalScrollBarPolicy(
      ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
    )

    scrollPane1.setBorder(border1)
    scrollPane2.setBorder(border2)
    scrollPane1.setBounds(30, 5, 325, 50)
    scrollPane2.setBounds(30, 60, 325, 50)

    panel.add(scrollPane1)
    panel.add(scrollPane2)
  }

  def createLoadingFrame() = {
    loadingDialog.setTitle("Running...")
    loadingDialog.setResizable(false)
    loadingDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
    loadingDialog.add(new JLabel(loadingIcon))  
    loadingDialog.setLocationRelativeTo(null)
    loadingDialog.pack()   
  }

  def main(args: Array[String]) {
    createWindow()
  }
}
