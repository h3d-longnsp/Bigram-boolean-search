## Prerequisites
- OpenJDK 11.0.18.
- Scala 2.13.10.
- Install [Scala (Metals)](https://marketplace.visualstudio.com/items?itemName=scalameta.metals) extension if you are using VS Code.
- Clone this project: `https://github.com/h3d-longnsp/Bigram-boolean-search.git`.
- Click the `Import build` prompt when open project or run `Metals: Import Build` manually in the command palette (`ctrl`+`shift`+`P`).

## Build and run
- Build the executable `jar` file for this project by open a terminal at the root folder and run `sbt assembly`.
- Or run the pre-built `boolean-search.jar` in `target/scala-2.13` by running `java -jar boolean-search.jar`.

## Usage
### 1. Graphical interface
- ![image](https://user-images.githubusercontent.com/99666567/229276573-1fd1922f-50dc-4442-9fa3-a9cb9180ced7.png)
- Shortcuts list:
- 
| Option                    | Shortcut          |
|---------------------------|-------------------|
| Select a input folder     | `Alt+F` + `Alt+O` |
| Select an index file      | `Alt+F` + `Alt+L` |
| Display `About` dialog    | `Alt+H` + `Alt+A` |
| Build Vocabulary          | `Alt+V`           |
| Build index               | `Alt+B`           |
| Search the selected query | `Alt+E`           |
| Save the index            | `Alt+S`           |
| Clear the search fields   | `Alt+C`           |
| Exit the program          | `Alt+F` + `Alt+P` |

### 2. Build vocabulary from input folder
- Choose `File>Select Input Folder` to select an input folder. A select folder dialog will display for users to choose an input folder. ![image](https://user-images.githubusercontent.com/99666567/229277399-4cc635e5-fa67-424c-b784-0ae474054248.png)
- Users can verify the path in the `Input Folder` text box. If the path is true then click the `Build Vocab` button. A loading dialog will appeared and the vocabulary for both unigram and bigram will be built. When done, the `Vocabulary` text box will display the total tokens count and the tokens. ![image](https://user-images.githubusercontent.com/99666567/229277610-e3a29afb-d735-4f3f-8ca4-13098ef0ffb3.png)
- If users haven't chosen a input folder, click the `Build Vocab` button will display an error dialog to notify the users. ![image](https://user-images.githubusercontent.com/99666567/229278080-5692a1f1-65a7-44b8-b7c6-cd3584f68c2d.png)

### 3. Load an index file
- Choose `File>Load Index File` to select an index file. A select file dialog will display for users to choose an index file. ![image](https://user-images.githubusercontent.com/99666567/229277709-3838abbd-f6c8-4cf6-b187-ebb5b2a0cd83.png)
- Users can verify the path in the `Index File` text box. A loading dialog will appeared and the chosen index will be loaded, the `Index` text box will display the total tokens count and the tokens with theirs corresponding documents ID. ![image](https://user-images.githubusercontent.com/99666567/229277928-42d01246-1027-4659-aa8c-bf5e7ed149b4.png)

### 4. Build index
- If users haven't built a vocabulary, click the `Build Index` button will display an error dialog to notify the users.![image](https://user-images.githubusercontent.com/99666567/229278023-ec2eb53b-f132-464b-97fd-8c63a4230922.png)
- When a vocabulary is built, the status label will change to green. Click the `Build Index` to build the index. A loading dialog will appeared and the index will be built. When done, the `Index` text box will display the total tokens count and the tokens with theirs corresponding documents ID. ![image](https://user-images.githubusercontent.com/99666567/229278182-b36ed899-c9a3-45c5-8d88-69bfdbac3596.png) ![image](https://user-images.githubusercontent.com/99666567/229278222-7062b8fd-9fb1-424a-8df2-b248f6b6514d.png)

### 5. Search
- To use search, users need to build an index file or load an existing one.
- There are 5 options to search for. Select the needed option and enter the terms then click `Search!` button. The result will be displayed in the `Result` text box. The query and the result count will be displayed in the `Query` text box.
- If a term/terms is empty, an error dialog will be displayed to notify the users.![image](https://user-images.githubusercontent.com/99666567/229278561-8e9630e0-616a-41dd-a9be-f637edc666c4.png)

- Examples: 
  - Search a single term: ![image](https://user-images.githubusercontent.com/99666567/229278489-de78d4a0-e778-499e-91ef-d42f2d10f8cc.png)
  - Search AND: ![image](https://user-images.githubusercontent.com/99666567/229278593-b542cba7-83c8-4fcc-8f29-cccbc6d4bb17.png)
  - Search OR: ![image](https://user-images.githubusercontent.com/99666567/229278604-b84b4613-d63a-4762-ab14-6ea0b8cb727a.png)
  - Search AND NOT: ![image](https://user-images.githubusercontent.com/99666567/229278620-57689021-8273-4f8a-9fac-602b820deefd.png)
  - Search OR NOT: ![image](https://user-images.githubusercontent.com/99666567/229278737-14c0bd05-5e81-4f85-a3bc-ca66c73a4512.png)

- To clear all the fields in `Search`, click the `Clear` button. ![image](https://user-images.githubusercontent.com/99666567/229278814-536d1d3c-e9d3-4573-addc-e3eec66636f6.png)

- To save the present index, click the `Save button`. A save dialog will appeared and the users will choose the name and location to save the index.![image](https://user-images.githubusercontent.com/99666567/229278899-8777f370-b5be-4f5b-8f28-216874eb33c4.png) ![image](https://user-images.githubusercontent.com/99666567/229278906-62c2c121-6a92-43c1-b29e-594f8732de9c.png)

