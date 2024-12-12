package GUI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import Environment.DownloadTasksManager;
import Environment.FileBlockAnswerMessage;
import Environment.FileBlockRequestMessage;
import Environment.FileSearchResult;
import Environment.WordSearchMessage;
import Remote.PeerNode;

public class P2PGui {
	
	private JFrame frame; //janela principal
	private JList<String> list;
    private DefaultListModel<String> listModel;
	private File filesFolder; //folder que contem os ficheiros
	private JTextField text1; // onde se insere a procura
   // private JTextField textMessage; // Campo para mensagem a ser enviada
    private List<PeerNode> availableNodes;
    private Set<Integer> connectedNodes = new HashSet<>();


    private DownloadTasksManager downloadManager;

    
    private String remoteAddress = null; // Endereço do nó remoto
    private int remotePort = 0;          // Porta do nó remoto
    private int localPort = 8080;        // Porta do servidor local
    private ExecutorService executorService; // ExecutorService adicionado aqui

    public static final int BLOCK_SIZE = 10240;  // 10 KB

	
	public P2PGui (String title, int width, int height) {
		
		frame = new JFrame();
		frame.setTitle(title);
		frame.setSize(width, height);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);
        filesFolder = new File("C://Users//André Ivan-coca//eclipse-workspace/Projeto2024P2P_73155/files"); // Specify folder containing images
        availableNodes = new ArrayList<>();

        executorService = Executors.newCachedThreadPool();  // Inicializando ExecutorService

        
        startServer(localPort); // Inicia o servidor na porta local

		addFrameContent();
	}
	
	//------------------------------Frame principal--------------------------------
	
	private void addFrameContent() {
		frame.setLayout(new BorderLayout());
		JPanel linha1 = new JPanel(new GridLayout(1,3));
		JPanel botoes = new JPanel(new GridLayout(2,1));
		
		//Labels
		JLabel lb1 = new JLabel("texto a procurar");
		
		//Caixa de texto
		text1 = new JTextField();
       // textMessage = new JTextField("Digite sua mensagem aqui");

		//Butoes
		JButton btSearch = new JButton("Procurar");
		JButton btDownload = new JButton("Download");
		JButton btNode = new JButton("Ligar nós");
        JButton btSendMessage = new JButton("Enviar Mensagem");

		//lista
		frame.add(linha1, BorderLayout.NORTH);
		frame.add(botoes, BorderLayout.EAST);
        frame.add(list, BorderLayout.CENTER); // Adiciona a lista ao centro
		frame.setResizable(false);
		
		linha1.add(lb1);
		linha1.add(text1);
		linha1.add(btSearch);
		
		botoes.add(btDownload);
		botoes.add(btNode);
        botoes.add(btSendMessage);

		// Ações dos botões
		btSearch.addActionListener(e -> searchFiles());
		//btSearch.addActionListener(e -> searchRemoteFiles());

		setupDownloadButton(btDownload);

        btNode.addActionListener(e -> connectToNode());
        
      //  btSendMessage.addActionListener(e -> sendMessage());
       
       // obterNomeDoFicheiro();

	}
	
	//----------------------------------------Frame para conectar o no---------------------------
	
	private void connectToNode() {
	    // novo JFrame para a interface de conexão
	    JFrame connectionFrame = new JFrame("Conectar ao Nó");
	    connectionFrame.setSize(300, 150);
	    connectionFrame.setLayout(new GridLayout(3, 2, 10, 10));
	    connectionFrame.setLocationRelativeTo(frame);

	    //interface
	    JLabel lblEndereco = new JLabel("Endereço:");
	    JTextField txtEndereco = new JTextField();
	    JLabel lblPorta = new JLabel("Porta:");
	    JTextField txtPorta = new JTextField();

	    JButton btnOk = new JButton("OK");
	    JButton btnCancelar = new JButton("Cancelar");

	    //Adiciona os componentes
	    connectionFrame.add(lblEndereco);
	    connectionFrame.add(txtEndereco);
	    connectionFrame.add(lblPorta);
	    connectionFrame.add(txtPorta);
	    connectionFrame.add(btnCancelar);
	    connectionFrame.add(btnOk);

	    //botão "Cancelar" fecha
	    btnCancelar.addActionListener(e -> connectionFrame.dispose());

	    // botão "OK" - tenta conectar ao nó com endereço e porta informados
	    btnOk.addActionListener(e -> {
	        String endereco = txtEndereco.getText();
	        String portaStr = txtPorta.getText();

	        if (!endereco.isEmpty() && !portaStr.isEmpty()) {
	            try {
//	                int porta = Integer.parseInt(portaStr);
//	                // Tentativa de conexão com o endereço e porta especificados
//	                remoteAddress = endereco;
//                    remotePort = porta;


	            	int porta = Integer.parseInt(portaStr);
	                connectedNodes.add(porta);

	                PeerNode newNode = new PeerNode(porta);
	                availableNodes.add(newNode);
                    JOptionPane.showMessageDialog(connectionFrame,
                            "Conectado ao nó: " + endereco + " na porta " + porta,
                            "Conexão Bem-sucedida", JOptionPane.INFORMATION_MESSAGE);
                    connectionFrame.dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(connectionFrame,
                            "Porta inválida. Insira um número.",
                            "Erro de Formato", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(connectionFrame,
                        "Por favor, insira endereço e porta.",
                        "Campos Vazios", JOptionPane.WARNING_MESSAGE);
            }
        });

        connectionFrame.setVisible(true);
    }
	
	//------------------------Parte da ligação--------------------------------
	
	private void startServer(int initialPort) {
	    new Thread(() -> {
	        int port = initialPort; // Começa com a porta fornecida, ao invés de 8080

	        // O loop agora tenta usar a porta passada para o nó
	        while (true) {
	            try (ServerSocket serverSocket = new ServerSocket(port)) {
	                final int finalPort = port;
	                SwingUtilities.invokeLater(() -> {
	                    frame.setTitle(frame.getTitle() + " - Porta: " + finalPort);
	                });

	                System.out.println("Servidor iniciado na porta " + port);

	                // Dynamically load files based on port (folder dl1, dl2, etc.)
	                String folderPath = "C:/Users/André Ivan-coca/eclipse-workspace/Projeto2024P2P_73155/files/dl" + (port - 8080 + 1);
	                File filesFolder = new File(folderPath);
	                if (filesFolder.exists() && filesFolder.isDirectory()) {
	                    File[] files = filesFolder.listFiles((File f) -> f.isFile());
	                    if (files != null) {
	                        for (File file : files) {
	                            SwingUtilities.invokeLater(() -> listModel.addElement(file.getName()));
	                        }
	                    } else {
	                        System.err.println("No files found in directory: " + filesFolder.getAbsolutePath());
	                    }
	                } else {
	                    System.err.println("Invalid directory: " + filesFolder.getAbsolutePath());
	                }

	                // Accept client connections and handle messages
	                while (true) {
	                    Socket clientSocket = serverSocket.accept();
	                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	                    String message = in.readLine();
	                    System.out.println("Mensagem recebida: " + message);

	                    // Update the GUI with the received message
	                    SwingUtilities.invokeLater(() -> listModel.addElement("Recebido: " + message));
	                }
	            } catch (IOException e) {
	                System.err.println("Erro ao iniciar o servidor na porta " + port + ": " + e.getMessage());

	                // Agora não tentamos mais a porta 8080 explicitamente
	                try {
	                    Thread.sleep(1000); // Optional: wait 1 second before trying the next port
	                } catch (InterruptedException ex) {
	                    Thread.currentThread().interrupt();
	                }

	                // Incrementa a porta e tenta novamente
	                port++;
	            }
	        }
	    }).start();
	}
	
	 public List<FileSearchResult> search(String keyword, String nodeAddress, int port) {
	        try (Socket socket = new Socket(nodeAddress, port);
	             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
	             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

	            WordSearchMessage searchMessage = new WordSearchMessage(keyword);
	            out.writeObject(searchMessage);

	            // Receber os resultados
	            List<FileSearchResult> results = (List<FileSearchResult>) in.readObject();
	            return results;

	        } catch (Exception e) {
	            System.err.println("Erro ao buscar arquivos: " + e.getMessage());
	            return null;
	        }
	    }
	 
//	
	
	//------------------------------------------Download-----------------------
	
	private void setupDownloadButton(JButton btDownload) {
	    btDownload.addActionListener(e -> {
	        String selectedFile = list.getSelectedValue(); // Arquivo selecionado na interface gráfica
	        if (selectedFile == null) {
	            JOptionPane.showMessageDialog(frame, "Por favor, selecione um arquivo para download.", "Erro", JOptionPane.WARNING_MESSAGE);
	            return;
	        }

	        // Caminho do arquivo que o nó possui
	        File fileToDownload = new File(filesFolder, selectedFile);
	        if (!fileToDownload.exists()) {
	            JOptionPane.showMessageDialog(frame, "Arquivo não encontrado no nó local.", "Erro", JOptionPane.ERROR_MESSAGE);
	            return;
	        }

	        // Simulação de nós disponíveis para o teste (IP e porta)
	        List<String> availableNodes = List.of(
	            "localhost:8080", 
	            "localhost:8081", 
	            "localhost:8082",
	            "localhost:8083"
	        );

	        // Criação e início do download
	        DownloadTasksManager downloadManager = new DownloadTasksManager(fileToDownload, availableNodes);
	        downloadManager.startDownload();

	        JOptionPane.showMessageDialog(frame, "Download iniciado. Acompanhe os logs para progresso.", "Info", JOptionPane.INFORMATION_MESSAGE);
	    });
	}
	
	//-----------------------------parte dos ficheiros--------------------------
	
	private void searchFiles() {
	    // Obtem o texto de pesquisa do campo de texto 
	    String searchText = text1.getText().toLowerCase();
	    listModel.clear();  // Limpa a lista atual para atualizar com os resultados filtrados

	    // Lista para armazenar resultados encontrados
	    List<File> matchingFiles = new ArrayList<>();

	    // Realiza a busca recursiva
	    searchInDirectory(filesFolder, searchText, matchingFiles);

	    
	    // Adiciona arquivos das pastas dos nós conectados
	    for (int port : connectedNodes) {
	        String folderPath = "C:/Users/André Ivan-coca/eclipse-workspace/Projeto2024P2P_73155/files/dl" + (port - 8080 + 1);
	        File folder = new File(folderPath);
	        searchInDirectory(folder, searchText, matchingFiles);
	    }
	    
	    // Atualiza a lista com os arquivos que contêm o texto de pesquisa
	    if (!matchingFiles.isEmpty()) {
	        for (File file : matchingFiles) {
	            listModel.addElement(file.getAbsolutePath()); // Adiciona os caminhos completos dos arquivos
	        }
	    } else {
	        // Exibe uma mensagem se nenhum arquivo correspondente for encontrado
	        JOptionPane.showMessageDialog(frame, "Nenhum arquivo encontrado com a palavra: " + searchText, "Resultado da Procura", JOptionPane.INFORMATION_MESSAGE);
	    }
	}
	
	// Método auxiliar para busca recursiva
	private void searchInDirectory(File directory, String searchText, List<File> results) {
	    if (directory != null && directory.isDirectory()) {
	        File[] files = directory.listFiles(); // Lista os arquivos e pastas no diretório atual
	        if (files != null) {
	            for (File file : files) {
	                if (file.isDirectory()) {
	                    // Se for um diretório, chama o método recursivamente
	                    searchInDirectory(file, searchText, results);
	                } else if (file.getName().toLowerCase().contains(searchText)) {
	                    // Adiciona o arquivo à lista de resultados se corresponder à pesquisa
	                    results.add(file);
	                }
	            }
	        }
	    }
	}
	public void open() {
		frame.setVisible(true);
	}
}

	
//		private void obterNomeDoFicheiro() {
//			 if (filesFolder != null && filesFolder.isDirectory()) {
//		            File[] files = filesFolder.listFiles(new FileFilter() {
//		                public boolean accept(File f) {
//		                    return f.isFile();
//		                }
//		            });
//
//		            if (files != null) {
//		                for (File file : files) {
//		                    listModel.addElement(file.getName());  //Add the file name to the list model
//		                }
//		            } else {
//		                System.err.println("No files found in the folder.");
//		            }
//		        } else {
//		            System.err.println("Invalid folder or folder does not exist.");
//		        }
//
//		}
	

//private void sendMessage() {
//if (remoteAddress != null && remotePort != 0) {
//  String message = textMessage.getText();
//
//  new Thread(() -> {
//      try (Socket socket = new Socket(remoteAddress, remotePort);
//           PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
//          out.println(message);  // Envia a mensagem
//          SwingUtilities.invokeLater(() -> listModel.addElement("Enviado: " + message));
//      } catch (IOException e) {
//          SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,
//                  "Erro ao enviar mensagem: " + e.getMessage(),
//                  "Erro", JOptionPane.ERROR_MESSAGE));
//      }
//  }).start();
//} else {
//  JOptionPane.showMessageDialog(frame, "Nenhum nó conectado!", "Erro", JOptionPane.WARNING_MESSAGE);
//}
//}


//private void searchRemoteFiles() {
//    String searchText = text1.getText().toLowerCase(); // Texto da caixa de pesquisa
//    if (searchText.isEmpty()) {
//        JOptionPane.showMessageDialog(frame, "Por favor, insira um termo para pesquisa.", "Aviso", JOptionPane.WARNING_MESSAGE);
//        return; // Não realizar pesquisa se o campo de pesquisa estiver vazio
//    }
//
//    // Não limpar a lista existente, só adicionar novos itens encontrados
//    if (remoteAddress != null && remotePort != 0) {
//        new Thread(() -> {
//            try (Socket socket = new Socket(remoteAddress, remotePort);
//                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
//
//                // Envia o termo de pesquisa para o nó remoto
//                out.println(searchText);
//
//                // Lê e exibe os arquivos encontrados
//                String fileName;
//                boolean filesFound = false;
//                while ((fileName = in.readLine()) != null) {
//                    if ("END".equals(fileName)) {
//                        break; // Fim da lista de arquivos
//                    }
//                    filesFound = true;
//
//                    // Adiciona o nome do arquivo à lista na interface gráfica
//                    String finalFileName = fileName; // Necessário para expressão lambda
//                    SwingUtilities.invokeLater(() -> listModel.addElement(finalFileName));
//                }
//
//                if (!filesFound) {
//                    SwingUtilities.invokeLater(() -> listModel.addElement("Nenhum arquivo encontrado."));
//                }
//
//            } catch (IOException e) {
//                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,
//                        "Erro ao buscar arquivos remotos: " + e.getMessage(),
//                        "Erro", JOptionPane.ERROR_MESSAGE));
//            }
//        }).start();
//    } else {
//        JOptionPane.showMessageDialog(frame, "Nenhum nó conectado!", "Erro", JOptionPane.WARNING_MESSAGE);
//    }
//}
