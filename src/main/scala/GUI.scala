package org.long.A4

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
import javax.swing.event.DocumentListener
import javax.swing.event.DocumentEvent

object GUI {
  var inputFolderPathTxt = ""
  var indexFilePathTxt = ""

  var firstTermTxt = ""
  var secondTermTxt = ""
  var queryTxt = ""
  var searchResultTxt: List[Int] = List.empty

  var pairs: List[(String, Seq[String])] = List.empty
  var unigramVocabulary: List[String] = List.empty
  var bigramVocabulary: List[String] = List.empty
  var combineVocabulary: List[String] = List.empty
  var globalIndex: Map[String, List[Int]] = Map.empty

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
  val indexFileSaveChooser = new JFileChooser()

  val vocabBorder = BorderFactory.createTitledBorder("Vocabulary: 0 row")
  val indexBorder = BorderFactory.createTitledBorder("Index: 0 row")

  val appIcon = new ImageIcon("assets/app512.png")
  val loadingIcon = new ImageIcon("assets/loading.gif")
  val aboutIcon = new ImageIcon("assets/about64.png")
  val infoIcon = new ImageIcon("assets/info64.png")
  val errorIcon = new ImageIcon("assets/error64.png")
  val searchIcon = new ImageIcon("assets/search32.png")
  val clearIcon = new ImageIcon("assets/bin32.png")
  val saveIcon = new ImageIcon("assets/save32.png")
  val checkIcon = new ImageIcon("assets/check32.png")
  val uncheckIcon = new ImageIcon("assets/uncheck32.png")
  val catIcon = new ImageIcon("assets/cat128.png")

  val folderPathStatLabel = new JLabel(uncheckIcon)
  val indexPathStatLabel = new JLabel(uncheckIcon)

  val globalFrame = new JFrame("Bigram Boolean Search")

  def main(args: Array[String]) {
    createWindow()
  }
  class buildVocabWorker extends SwingWorker[List[String], Unit] {
    override def doInBackground(): List[String] = {
      // Do the time-consuming task here
      val (temp1, temp2, temp3) = Indexer.buildVocab(inputFolderPathTxt)
      pairs = temp1
      unigramVocabulary = temp2
      bigramVocabulary = temp3
      val tempVocabulary = unigramVocabulary ++ bigramVocabulary
      vocabTextArea.setText(tempVocabulary.sorted.mkString("\n"))
      vocabBorder.setTitle("Vocabulary: " + vocabTextArea.getLineCount + " rows")      
      tempVocabulary
    }
    override def done(): Unit = {
      combineVocabulary = get()      
      globalFrame.setEnabled(true)
      loadingDialog.setVisible(false)
      folderPathStatLabel.setIcon(checkIcon)
    }
  }

  class loadIndexWorker extends SwingWorker[Unit, Unit] {
    override def doInBackground(): Unit = {
      indexFilePathTxt = fileChooser.getSelectedFile().getPath()
      filePathTextArea.setText(indexFilePathTxt)
      globalIndex = Utils.sortIndex(Indexer.loadIndex(indexFilePathTxt))
      indexTextArea.setText(
        globalIndex
          .map { case (term, docIds) =>
            term + "\t\t| " + docIds.sorted.mkString(" ")
          }
          .mkString("\n")
      )
      indexBorder.setTitle("Index: " + indexTextArea.getLineCount + " rows")
    }
    override def done(): Unit = {
      globalFrame.setEnabled(true)
      loadingDialog.setVisible(false)
      indexPathStatLabel.setIcon(checkIcon)
    }
  }

  class buildIndexWorker extends SwingWorker[Map[String, List[Int]], Unit] {
    override def doInBackground(): Map[String, List[Int]] = {
      val unigramIndex = Indexer.buildUnigramIndex(pairs, unigramVocabulary)
      val bigramIndex = Indexer.buildBigramIndex(pairs, bigramVocabulary)
      val tempIndex = Utils.sortIndex(unigramIndex ++ bigramIndex)
      indexTextArea.setText(
        tempIndex
          .map { case (term, docIds) =>
            term + "\t\t| " + docIds.mkString(" ")
          }
          .mkString("\n")
      )
      indexBorder.setTitle("Index: " + indexTextArea.getLineCount + " rows")
      tempIndex
    }

    override def done(): Unit = {
      globalIndex = get()
      globalFrame.setEnabled(true)
      loadingDialog.setVisible(false)
      indexPathStatLabel.setIcon(checkIcon)
    }
  }

  private def createWindow(): Unit = {
    val panel = new JPanel();
    panel.setLayout(null)
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
    globalFrame.setSize(1100, 900)
    globalFrame.setResizable(false)
    globalFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    createUI(globalFrame, panel)
    globalFrame.add(panel)
    globalFrame.setIconImage(appIcon.getImage())
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
    fileMenu.setMnemonic(KeyEvent.VK_F)
    val helpMenu = new JMenu("Help")
    helpMenu.setMnemonic(KeyEvent.VK_H)
    
    val filter = new FileNameExtensionFilter("Index text file (*.txt)", "txt");
    fileChooser.setFileFilter(filter)
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY)
    fileChooser.setDialogTitle("Select an index file")

    val folderChooser = new JFileChooser()
    folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    folderChooser.setDialogTitle("Select an input folder")

    val fileMenuItem1 = new JMenuItem("Select Input Folder")
    val fileMenuItem2 = new JMenuItem("Load Index File")
    val fileMenuItem3 = new JMenuItem("Close Program")
    fileMenuItem1.setMnemonic(KeyEvent.VK_O)
    fileMenuItem2.setMnemonic(KeyEvent.VK_L)
    fileMenuItem3.setMnemonic(KeyEvent.VK_P)

    val helpMenuItem1 = new JMenuItem("About")
    helpMenuItem1.setMnemonic(KeyEvent.VK_A)

    fileMenuItem1.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        if (
          folderChooser
            .showOpenDialog(null) == JFileChooser.APPROVE_OPTION
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
            .showOpenDialog(null) == JFileChooser.APPROVE_OPTION
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

    helpMenuItem1.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        JOptionPane.showMessageDialog(null, "Bigram Boolean Search\nAuthor: Long Nguyen\n01/04/2023", "About", JOptionPane.INFORMATION_MESSAGE, aboutIcon) 
      }
    })

    fileMenu.add(fileMenuItem1)
    fileMenu.add(fileMenuItem2)
    fileMenu.add(new JSeparator()); // SEPARATOR
    fileMenu.add(fileMenuItem3)

    helpMenu.add(helpMenuItem1)

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

    folderPathStatLabel.setBounds(450, 25, 35, 35)
    indexPathStatLabel.setBounds(450, 90, 35, 35)

    scrollPane1.setBorder(border1)
    scrollPane2.setBorder(border2)
    scrollPane1.setBounds(25, 15, 400, 50)
    scrollPane2.setBounds(25, 80, 400, 50)

    panel.add(scrollPane1)
    panel.add(scrollPane2)
    panel.add(folderPathStatLabel)
    panel.add(indexPathStatLabel)
  }
  
  def createButton(panel: JPanel): Unit = {
    val buildVocabBtn = new JButton("Build Vocab")
    buildVocabBtn.setMnemonic(KeyEvent.VK_V)
    buildVocabBtn.setBounds(700, 15, 125, 45)

    val buildIndexBtn = new JButton("Build Index")
    buildIndexBtn.setMnemonic(KeyEvent.VK_B)
    buildIndexBtn.setBounds(700, 80, 125, 45)

    val inAppIconLabel = new JLabel(catIcon)
    inAppIconLabel.setBounds(925, 10, 128, 128)

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
    panel.add(inAppIconLabel)
  }

  def createVocabArea(panel: JPanel): Unit = {
    vocabTextArea.setEditable(false)
    vocabTextArea.setLineWrap(true)
    vocabBorder.setTitleColor(Color.MAGENTA)
    val scrollPane = new JScrollPane(vocabTextArea)

    val vocabRowHeader = new JTextArea("0")
    vocabRowHeader.setEditable(false)
    scrollPane.setRowHeaderView(vocabRowHeader)

    vocabTextArea.getDocument.addDocumentListener(new DocumentListener {
      override def insertUpdate(e: DocumentEvent): Unit = updateRowHeader()
      override def removeUpdate(e: DocumentEvent): Unit = updateRowHeader()
      override def changedUpdate(e: DocumentEvent): Unit = updateRowHeader()
      private def updateRowHeader(): Unit = {
        val text = vocabTextArea.getText()
        val numRows = text.count(_ == '\n') + 1
        val rowHeaderText = (1 to numRows).mkString("\t|  \n")
        vocabRowHeader.setText(rowHeaderText)
      }
    })    

    scrollPane.setBorder(vocabBorder)
    scrollPane.setBounds(25, 435, 300, 385)
    panel.add(scrollPane)
  }

  def createIndexArea(panel: JPanel): Unit = {
    indexTextArea.setEditable(false)
    indexBorder.setTitleColor(Color.MAGENTA)
    val scrollPane = new JScrollPane(indexTextArea)

    val vocabRowHeader = new JTextArea("0")
    vocabRowHeader.setEditable(false)
    scrollPane.setRowHeaderView(vocabRowHeader)

    indexTextArea.getDocument.addDocumentListener(new DocumentListener {
      override def insertUpdate(e: DocumentEvent): Unit = updateRowHeader()
      override def removeUpdate(e: DocumentEvent): Unit = updateRowHeader()
      override def changedUpdate(e: DocumentEvent): Unit = updateRowHeader()
      private def updateRowHeader(): Unit = {
        val text = indexTextArea.getText()
        val numRows = text.count(_ == '\n') + 1
        val rowHeaderText = (1 to numRows).mkString("\t|  \n")
        vocabRowHeader.setText(rowHeaderText)
      }
    })

    scrollPane.setBorder(indexBorder)
    scrollPane.setBounds(375, 435, 700, 385)
    panel.add(scrollPane)
  }

  def createSearchArea(panel: JPanel): Unit = {
    // create container panel
    val searchPanel = new JPanel()
    val searchBorder = BorderFactory.createTitledBorder("Search")
    searchPanel.setLayout(null)
    searchPanel.setBorder(searchBorder)
    searchPanel.setBounds(25, 155, 1050, 250)

    // create first text area
    term1TextArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "none")
    val termBorder1 = BorderFactory.createTitledBorder("Term A:")
    val termScrollPane1 = new JScrollPane(term1TextArea)
    termScrollPane1.setVerticalScrollBarPolicy(
      ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
    )
    termScrollPane1.setBorder(termBorder1)
    termScrollPane1.setBounds(25, 35, 225, 50)

    // create second text area
    term2TextArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "none")
    val termBorder2 = BorderFactory.createTitledBorder("Term B:")
    val termScrollPane2 = new JScrollPane(term2TextArea)
    termScrollPane2.setVerticalScrollBarPolicy(
      ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
    )
    termScrollPane2.setBorder(termBorder2)
    termScrollPane2.setBounds(275, 35, 225, 50)

    // create option comboBox
    val optionBorder = BorderFactory.createTitledBorder("Option:")
    val optionComboBox = new JComboBox[String]
    optionComboBox.setBorder(optionBorder)
    optionComboBox.setBounds(575, 35, 225, 50)
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
    searchBtn.setMnemonic(KeyEvent.VK_E)
    searchBtn.setBounds(900, 35, 125, 50)

    // add actionListener to searchBtn
    searchBtn.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        if (globalIndex.isEmpty) {
          JOptionPane.showMessageDialog(null, "No index built!", "Error", JOptionPane.ERROR_MESSAGE, errorIcon)
        }
        else {
          val optionSelected = optionComboBox.getSelectedIndex()
          optionSelected match {
            case 0 => if (term1TextArea.getText() == "") {
                        JOptionPane.showMessageDialog(null, "Term A was empty!", "Error", JOptionPane.ERROR_MESSAGE, errorIcon)
                      }
                      else {
                        optionSearch(globalIndex)
                      }
            case 1 => if (term1TextArea.getText() == "" || term2TextArea.getText() == "") {
                        JOptionPane.showMessageDialog(null, "A term/terms was empty!", "Error", JOptionPane.ERROR_MESSAGE, errorIcon)
                      }
                      else {
                        optionSearchAnd(globalIndex)
                      }
            case 2 => if (term1TextArea.getText() == "" || term2TextArea.getText() == "") {
                        JOptionPane.showMessageDialog(null, "A term/terms was empty!", "Error", JOptionPane.ERROR_MESSAGE, errorIcon)
                      }
                      else {
                        optionSearchOr(globalIndex)
                      }
            case 3 => if (term1TextArea.getText() == "" || term2TextArea.getText() == "") {
                        JOptionPane.showMessageDialog(null, "A term/terms was empty!", "Error", JOptionPane.ERROR_MESSAGE, errorIcon)
                      }
                      else {
                        optionSearchAndNot(globalIndex)
                      }
            case 4 => if (term1TextArea.getText() == "" || term2TextArea.getText() == "") {
                        JOptionPane.showMessageDialog(null, "A term/terms was empty!", "Error", JOptionPane.ERROR_MESSAGE, errorIcon)
                      }
                      else {
                        optionSearchOrNot(globalIndex)
                      }
          }          
        }
      }
    })

    // create save button
    val saveBtn = new JButton(saveIcon)
    saveBtn.setText("Save")
    saveBtn.setToolTipText("Save the result index.")
    saveBtn.setMnemonic(KeyEvent.VK_S)
    saveBtn.setBounds(900, 110, 125, 50)

    // config indexFileSaveChooser
    val filter = new FileNameExtensionFilter("Index text file (*.txt)", "txt");
    indexFileSaveChooser.setFileFilter(filter)
    indexFileSaveChooser.setFileSelectionMode(JFileChooser.FILES_ONLY)    
    indexFileSaveChooser.setDialogTitle("Select a folder to save")

    // add actionListener to saveBtn
    saveBtn.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        if (globalIndex.isEmpty) {
          JOptionPane.showMessageDialog(null, "No index built!", "Error", JOptionPane.ERROR_MESSAGE, errorIcon)
        }
        else {
          if (indexFileSaveChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            val savePath = indexFileSaveChooser.getSelectedFile().getPath()
            Indexer.writeOutputToFile(savePath, globalIndex)
            JOptionPane.showMessageDialog(null, "Index saved to: " + savePath, "Message", JOptionPane.INFORMATION_MESSAGE, infoIcon) 
          }
        }       
      }
    })

    // create clear text fields button
    val clearBtn = new JButton(clearIcon)
    clearBtn.setText("Clear")
    clearBtn.setToolTipText("Clear all the text fields in search panel.")
    clearBtn.setMnemonic(KeyEvent.VK_C)
    clearBtn.setBounds(900, 185, 125, 50)

    // add actionListener to clearBtn
    clearBtn.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        term1TextArea.setText("")
        firstTermTxt = ""
        term2TextArea.setText("")
        secondTermTxt = ""
        queryTextArea.setText("")
        queryTxt = ""
        searchResultTextArea.setText("")
        searchResultTxt = List.empty
        JOptionPane.showMessageDialog(null, "Search fields cleared!", "Message", JOptionPane.INFORMATION_MESSAGE, infoIcon)        
      }
    })

    // create query text area
    val queryBorder = BorderFactory.createTitledBorder("Query:")
    queryBorder.setTitleColor(Color.PINK)
    queryTextArea.setEditable(false)
    val queryScrollPane = new JScrollPane(queryTextArea)
    queryScrollPane.setBorder(queryBorder)
    queryScrollPane.setBounds(25, 100, 775, 50)

    // create result text area
    val searchResultBorder = BorderFactory.createTitledBorder("Result:")
    searchResultBorder.setTitleColor(Color.BLUE)
    searchResultTextArea.setEditable(false)
    searchResultTextArea.setLineWrap(true)
    searchResultTextArea.setWrapStyleWord(true)
    val searchResultScrollPane = new JScrollPane(searchResultTextArea)
    searchResultScrollPane.setBorder(searchResultBorder)
    searchResultScrollPane.setBounds(25, 160, 775, 75)

    // add elements to searchPanel container
    searchPanel.add(termScrollPane1)
    searchPanel.add(termScrollPane2)
    searchPanel.add(optionComboBox)
    searchPanel.add(searchBtn)
    searchPanel.add(saveBtn)
    searchPanel.add(clearBtn)
    searchPanel.add(queryScrollPane)
    searchPanel.add(searchResultScrollPane)
    panel.add(searchPanel)
  }

  def optionSearch(index: Map[String, List[Int]]): Unit = {
    firstTermTxt = term1TextArea.getText().stripLeading().stripTrailing()
    searchResultTxt = BooleanSearch.search(index, firstTermTxt)    
    term1TextArea.setText(firstTermTxt)
    term2TextArea.setText("")
    queryTextArea.setText(firstTermTxt)
    if (searchResultTxt.isEmpty) {
      searchResultTextArea.setText("Not found!")  
    }
    else {
      queryTextArea.setText(firstTermTxt + "\t\t| " + searchResultTxt.length + " results")
      searchResultTextArea.setText(searchResultTxt.mkString(" "))
    }
  }

  def optionSearchAnd(index: Map[String, List[Int]]): Unit = {
    firstTermTxt = term1TextArea.getText().stripLeading().stripTrailing()
    secondTermTxt = term2TextArea.getText().stripLeading().stripTrailing()
    searchResultTxt = BooleanSearch.searchAnd(index, firstTermTxt, secondTermTxt)
    term1TextArea.setText(firstTermTxt)
    term2TextArea.setText(secondTermTxt)    
    queryTextArea.setText(firstTermTxt + " AND " + secondTermTxt)
    if (searchResultTxt.isEmpty) {
      searchResultTextArea.setText("Not found!")  
    }
    else {
      queryTextArea.setText(firstTermTxt + " AND " + secondTermTxt + "\t\t| " + searchResultTxt.length + " results")
      searchResultTextArea.setText(searchResultTxt.mkString(" "))
    }
  }  

  def optionSearchOr(index: Map[String, List[Int]]): Unit = {
    firstTermTxt = term1TextArea.getText().stripLeading().stripTrailing()
    secondTermTxt = term2TextArea.getText().stripLeading().stripTrailing()
    searchResultTxt = BooleanSearch.searchOr(index, firstTermTxt, secondTermTxt)
    term1TextArea.setText(firstTermTxt)
    term2TextArea.setText(secondTermTxt) 
    queryTextArea.setText(firstTermTxt + " OR " + secondTermTxt)
    if (searchResultTxt.isEmpty) {
      searchResultTextArea.setText("Not found!")  
    }
    else {
      queryTextArea.setText(firstTermTxt + " OR " + secondTermTxt + "\t\t| " + searchResultTxt.length + " results")
      searchResultTextArea.setText(searchResultTxt.mkString(" "))
    }
  }  

  def optionSearchAndNot(index: Map[String, List[Int]]): Unit = {
    firstTermTxt = term1TextArea.getText().stripLeading().stripTrailing()
    secondTermTxt = term2TextArea.getText().stripLeading().stripTrailing()
    searchResultTxt = BooleanSearch.searchAndNot(index, firstTermTxt, secondTermTxt)
    term1TextArea.setText(firstTermTxt)
    term2TextArea.setText(secondTermTxt)
    queryTextArea.setText(firstTermTxt + " AND (NOT " + secondTermTxt + ")")
    if (searchResultTxt.isEmpty) {
      searchResultTextArea.setText("Not found!")  
    }
    else {
      queryTextArea.setText(firstTermTxt + " AND (NOT " + secondTermTxt + ")" + "\t\t| " + searchResultTxt.length + " results")
      searchResultTextArea.setText(searchResultTxt.mkString(" "))
    }
  }

  def optionSearchOrNot(index: Map[String, List[Int]]): Unit = {
    firstTermTxt = term1TextArea.getText().stripLeading().stripTrailing()
    secondTermTxt = term2TextArea.getText().stripLeading().stripTrailing()
    searchResultTxt = BooleanSearch.searchOrNot(index, firstTermTxt, secondTermTxt)
    term1TextArea.setText(firstTermTxt)
    term2TextArea.setText(secondTermTxt)
    queryTextArea.setText(firstTermTxt + " OR (NOT " + secondTermTxt + ")")
    if (searchResultTxt.isEmpty) {
      searchResultTextArea.setText("Not found!")  
    }
    else {
      queryTextArea.setText(firstTermTxt + " OR (NOT " + secondTermTxt + ")" + "\t\t| " + searchResultTxt.length + " results")
      searchResultTextArea.setText(searchResultTxt.mkString(" "))
    }
  }

  def createLoadingFrame() = {
    loadingDialog.setTitle("Running...")
    loadingDialog.setResizable(false)
    loadingDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
    loadingDialog.add(new JLabel(loadingIcon))
    loadingDialog.setLocationRelativeTo(null)
    loadingDialog.pack()
  }
}
