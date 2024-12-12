package Remote;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Environment.FileSearchResult;
import Environment.WordSearchMessage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PeerNode {

    private int localPort; // Porta em que este peer está ouvindo conexões
    private ExecutorService executorService;

    
    public PeerNode(int localPort) {

        this.localPort = localPort;
        this.executorService = Executors.newCachedThreadPool(); // Para gerenciar múltiplas conexões
    }

    public void startServer() {
        executorService.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(localPort)) {
                System.out.println("Servidor PeerNode iniciado na porta: " + localPort);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Conexão aceita de: " + clientSocket.getInetAddress());
                    executorService.submit(() -> handleClient(clientSocket));
                }
            } catch (IOException e) {
                System.err.println("Erro ao iniciar o servidor PeerNode na porta " + localPort + ": " + e.getMessage());
            }
        });
    }
 // Trata as requisições recebidas do nó local
    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Lê a consulta de pesquisa do cliente
            String searchQuery = in.readLine();
            System.out.println("Consulta de pesquisa recebida: " + searchQuery);

            // Define o diretório onde os arquivos estão localizados
            File folder = new File("C:/Users/André Ivan-coca/eclipse-workspace/Projeto2024P2P_73155/files"); // Raiz da pasta "files"
            if (!folder.exists() || !folder.isDirectory()) {
                System.err.println("Diretório não existe ou não é acessível: " + folder.getAbsolutePath());
                out.println("ERROR: Diretório não acessível");
                return;
            }

            // Lista para armazenar os arquivos correspondentes
            List<File> matchingFiles = new ArrayList<>();
            searchFilesInDirectory(folder, searchQuery, matchingFiles);

            if (matchingFiles.size() > 0) {
                for (File file : matchingFiles) {
                    System.out.println("A enviar o nome do arquivo: " + file.getName() + " do nó " + getPort());
                    out.println(file.getName()+ " " + getPort()); // Envia o nome do arquivo para o cliente
                }
            } else {
                System.out.println("Nenhum arquivo encontrado.");
            }

            // Envia "END" para sinalizar o final da lista de arquivos
            out.println("END");

        } catch (IOException e) {
            System.err.println("Erro ao processar o cliente: " + e.getMessage());
        }
    }

    private void searchFilesInDirectory(File directory, String searchQuery, List<File> matchingFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Se for um diretório, recurse nele
                    searchFilesInDirectory(file, searchQuery, matchingFiles);
                } else if (file.getName().toLowerCase().contains(searchQuery.toLowerCase())) {
                    // Se for um arquivo e o nome contiver o termo da pesquisa, adicione à lista
                    matchingFiles.add(file);
                }
            }
        }
    }
    private String calculateFileHash(File file) {
        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance("SHA-256"))) {
            byte[] buffer = new byte[1024];
            while (dis.read(buffer) != -1) {}
            MessageDigest md = dis.getMessageDigest();
            StringBuilder hash = new StringBuilder();
            for (byte b : md.digest()) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception e) {
            System.err.println("Erro ao calcular hash: " + file.getName());
            return null;
        }
    }
    
    private String getNodeAddress() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            System.err.println("Erro ao obter endereço do nó: " + e.getMessage());
            return "Desconhecido";
        }
    }
 

    public int getPort() {
        return localPort;
    }


    public void handleWordSearchMessage(Socket clientSocket) {
    	try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
    			ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

    		WordSearchMessage searchMessage = (WordSearchMessage) in.readObject();
    		String keyword = searchMessage.getKeyword();

    		// Pesquisar arquivos no nó
    		List<FileSearchResult> searchResults = searchFiles(keyword);

    		// Enviar os resultados de volta
    		out.writeObject(searchResults);

    	} catch (Exception e) {
    		System.err.println("Erro ao processar WordSearchMessage: " + e.getMessage());
    	}
    }

    private List<FileSearchResult> searchFiles(String keyword) {
        List<FileSearchResult> results = new ArrayList<>();
        File folder = new File("shared_files"); // Pasta onde os arquivos estão armazenados

        if (folder.exists() && folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (file.isFile() && file.getName().contains(keyword)) {
                    String fileHash = calculateFileHash(file);
                    results.add(new FileSearchResult(
                        file.getName(),
                        fileHash,
                        file.length(),
                        getNodeAddress(), // Método para obter o endereço do nó
                        getPort()
                    		));
                }
            }
        }
        return results;
    }
}

    // Envia uma mensagem para outro nó
//    public void sendMessage(String remoteAddress, int remotePort, String message) {
//        executorService.submit(() -> {
//            try (Socket socket = new Socket(remoteAddress, remotePort);
//                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
//                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
//
//                // Envia mensagem
//                writer.println(message);
//
//                // Lê a resposta
//                String response = reader.readLine();
//                System.out.println("Resposta recebida: " + response);
//
//            } catch (IOException e) {
//                System.err.println("Erro ao enviar mensagem: " + e.getMessage());
//            }
//        });
//    }
