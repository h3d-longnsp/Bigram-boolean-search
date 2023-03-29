import javax.swing.{ JFrame, WindowConstants, JMenu, JMenuBar, JMenuItem, JSeparator, JFileChooser }
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import javax.swing.filechooser.FileNameExtensionFilter

object GUI {
    var index: Map[String, List[Int]] = Map.empty

    def main(args: Array[String]) {
        val frame = new JFrame()
        frame.setSize(1050, 600)           
        frame.setTitle("Bigram Boolean Search")
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        
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
        // frame.pack()
        frame.setResizable(false)
        frame.setVisible(true)
    }
}

