import java.awt.Color
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSeparator
import javax.swing.JTextArea
import javax.swing.KeyStroke
import javax.swing.ScrollPaneConstants
import javax.swing.SwingConstants
import javax.swing.SwingWorker
import javax.swing.UIManager
import javax.swing.WindowConstants
import javax.swing.filechooser.FileNameExtensionFilter

object GUI {
  var inputFolderPathTxt = ""
  var indexFilePathTxt = ""

  var firstTermTxt = ""
  var secondTermTxt = ""
  var queryTxt = ""
  var searchResultTxt = ""

  var pairs: List[(String, Seq[String])] = List.empty
  var unigramVocabulary: List[String] = List.empty
  var bigramVocabulary: List[String] = List.empty
  var combineVocabulary: List[String] = List.empty
  var index: Map[String, List[Int]] = Map.empty

  val vocabTextArea = new JTextArea()
  val indexTextArea = new JTextArea()

  val term1TextArea = new JTextArea()
  val term2TextArea = new JTextArea()
  val queryTextArea = new JTextArea()
  val searchResultTextArea = new JTextArea()

  val filePathTextArea = new JTextArea(1, 1)
  val folderPathTextArea = new JTextArea(1, 1)

  val loadingDialog = new JDialog()
  val fileChooser = new JFileChooser()

  val loadingIcon = new ImageIcon("assets/loading.gif")
  val errorIcon = new ImageIcon("assets/error.png")
  val searchIcon = new ImageIcon("assets/search.png")

  val globalFrame = new JFrame("Bigram Boolean Search")

  class buildVocabWorker extends SwingWorker[List[String], Unit] {
    override def doInBackground(): List[String] = {
      // Do the time-consuming task here
      val (temp1, temp2, temp3) = Indexer.buildVocab(inputFolderPathTxt)
      pairs = temp1
      unigramVocabulary = temp2
      bigramVocabulary = temp3
      val tempVocabulary = unigramVocabulary ++ bigramVocabulary
      vocabTextArea.setText(tempVocabulary.sorted.mkString("\n"))
      tempVocabulary
    }
    override def done(): Unit = {
      loadingDialog.setVisible(false)
      globalFrame.setEnabled(true)
      combineVocabulary = get()
    }
  }
  class loadIndexWorker extends SwingWorker[Unit, Unit] {
    override def doInBackground(): Unit = {
      indexFilePathTxt = fileChooser.getSelectedFile().getPath()
      filePathTextArea.setText(indexFilePathTxt)
      index = Indexer.sortIndex(Indexer.loadIndex(indexFilePathTxt))
      indexTextArea.setText(
        index
          .map { case (term, docIds) =>
            term + "\t\t|\t" + docIds.mkString(" ")
          }
          .mkString("\n")
      )
    }
    override def done(): Unit = {
      loadingDialog.setVisible(false)
      globalFrame.setEnabled(true)
    }
  }

  class buildIndexWorker extends SwingWorker[Map[String, List[Int]], Unit] {
    override def doInBackground(): Map[String, List[Int]] = {
      val unigramIndex = Indexer.buildUnigramIndex(pairs, unigramVocabulary)
      val bigramIndex = Indexer.buildBigramIndex(pairs, bigramVocabulary)
      val tempIndex = Indexer.sortIndex(unigramIndex ++ bigramIndex)
      indexTextArea.setText(
        tempIndex
          .map { case (term, docIds) =>
            term + "\t\t|\t" + docIds.mkString(" ")
          }
          .mkString("\n")
      )
      tempIndex
    }

    override def done(): Unit = {
      index = get()
      loadingDialog.setVisible(false)
      globalFrame.setEnabled(true)
    }
  }

  private def createWindow(): Unit = {
    val panel = new JPanel();
    panel.setLayout(null)
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
    globalFrame.setSize(1025, 925)
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
    createButton(panel)
    createVocabArea(panel)
    createIndexArea(panel)
    createSearchArea(panel)
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
          inputFolderPathTxt = folderChooser.getSelectedFile().getPath()
          folderPathTextArea.setText(inputFolderPathTxt)
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
    scrollPane1.setBounds(25, 5, 325, 50)
    scrollPane2.setBounds(25, 60, 325, 50)

    panel.add(scrollPane1)
    panel.add(scrollPane2)
  }
  
  def createButton(panel: JPanel): Unit = {
    val buildVocabBtn = new JButton("Build Vocab")
    buildVocabBtn.setBounds(475, 27, 125, 25)

    val buildIndexBtn = new JButton("Build Index")
    buildIndexBtn.setBounds(475, 66, 125, 25)

    buildVocabBtn.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        if (inputFolderPathTxt == "") {
          JOptionPane.showMessageDialog(
            null,
            "No input folder selected!",
            "Error",
            JOptionPane.ERROR_MESSAGE,
            errorIcon
          );
        } else {
          val worker = new buildVocabWorker
          loadingDialog.setVisible(true)
          globalFrame.setEnabled(false)
          worker.execute()
        }
      }
    })

    buildIndexBtn.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        if (combineVocabulary.isEmpty) {
          JOptionPane.showMessageDialog(
            null,
            "No vocabulary built!",
            "Error",
            JOptionPane.ERROR_MESSAGE,
            errorIcon
          );
        } else {
          val worker = new buildIndexWorker
          loadingDialog.setVisible(true)
          globalFrame.setEnabled(false)
          worker.execute()
        }
      }
    })

    panel.add(buildVocabBtn)
    panel.add(buildIndexBtn)
  }

  def createVocabArea(panel: JPanel): Unit = {
    vocabTextArea.setEditable(false)

    val borderVocab = BorderFactory.createTitledBorder("Vocabulary")
    val scrollPane = new JScrollPane(vocabTextArea)
    scrollPane.setBorder(borderVocab)
    scrollPane.setBounds(25, 125, 175, 450)
    panel.add(scrollPane)
  }

  def createIndexArea(panel: JPanel): Unit = {
    indexTextArea.setEditable(false)

    val borderIndex = BorderFactory.createTitledBorder("Index")
    val scrollPane = new JScrollPane(indexTextArea)
    scrollPane.setBorder(borderIndex)
    scrollPane.setBounds(250, 125, 750, 450)
    panel.add(scrollPane)
  }

  def createSearchArea(panel: JPanel): Unit = {
    // Create container panel
    val searchPanel = new JPanel()
    val searchBorder = BorderFactory.createTitledBorder("Search")
    searchPanel.setLayout(null)
    searchPanel.setBorder(searchBorder)
    searchPanel.setBounds(25, 600, 975, 250)

    // create first text area
    term1TextArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "none")
    val termBorder1 = BorderFactory.createTitledBorder("Term A:")
    val termScrollPane1 = new JScrollPane(term1TextArea)
    termScrollPane1.setVerticalScrollBarPolicy(
      ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
    )
    termScrollPane1.setBorder(termBorder1)
    termScrollPane1.setBounds(25, 35, 150, 50)

    // create second text area
    term2TextArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "none")
    val termBorder2 = BorderFactory.createTitledBorder("Term B:")
    val termScrollPane2 = new JScrollPane(term2TextArea)
    termScrollPane2.setVerticalScrollBarPolicy(
      ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
    )
    termScrollPane2.setBorder(termBorder2)
    termScrollPane2.setBounds(200, 35, 150, 50)

    // create option comboBox
    val optionBorder = BorderFactory.createTitledBorder("Option:")
    val optionComboBox = new JComboBox[String]
    optionComboBox.setBorder(optionBorder)
    optionComboBox.setBounds(400, 35, 200, 50)
    val options = List(
      "Search: A",
      "Search: A AND B",
      "Search: A OR B",
      "Search: A AND (NOT B)",
      "Search: A OR (NOT B)"
    )
    options.foreach(optionComboBox.addItem(_))

    // create search button
    val searchBtn = new JButton(searchIcon)
    searchBtn.setText("Search!")
    searchBtn.setToolTipText("Search the selected query.")
    searchBtn.setMnemonic(KeyEvent.VK_S)
    searchBtn.setBounds(700, 35, 125, 50)

    // add actionListener to searchBtn

    // create query text area
    val queryBorder = BorderFactory.createTitledBorder("Query:")
    queryBorder.setTitleColor(Color.PINK)
    queryTextArea.setEditable(false)
    val queryScrollPane = new JScrollPane(queryTextArea)
    queryScrollPane.setBorder(queryBorder)
    queryScrollPane.setBounds(175, 100, 625, 50)

    // create result text area
    val searchResultBorder = BorderFactory.createTitledBorder("Result:")
    searchResultBorder.setTitleColor(Color.BLUE)
    searchResultTextArea.setEditable(false)
    searchResultTextArea.setLineWrap(true)
    searchResultTextArea.setWrapStyleWord(true)
    val searchResultScrollPane = new JScrollPane(searchResultTextArea)
    searchResultScrollPane.setBorder(searchResultBorder)
    searchResultScrollPane.setBounds(175, 160, 625, 80)

    // add elements to searchPanel container
    searchPanel.add(termScrollPane1)
    searchPanel.add(termScrollPane2)
    searchPanel.add(optionComboBox)
    searchPanel.add(searchBtn)
    searchPanel.add(queryScrollPane)
    searchPanel.add(searchResultScrollPane)
    panel.add(searchPanel)
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
