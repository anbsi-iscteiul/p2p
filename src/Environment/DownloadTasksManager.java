package Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Faz a gestao do download, inclui a lista de blocos que vão ser feitas o download

public class DownloadTasksManager {

    private static final int BLOCK_SIZE = 10240; // 10K por bloco
	private List<FileBlockRequestMessage> fileBlockRequests;
	
    private static final int THREAD_COUNT = 5; // Número de nós/threads de download
    private ExecutorService executorService;
    private List<String> nodes;  // Lista de endereços dos nós disponíveis

   
    //private String fileHash;

    public DownloadTasksManager(File file, List<String> nodes) {
        this.nodes = nodes;
        this.fileBlockRequests = new ArrayList<>();
        initializeDownloadTasks(file);
        this.executorService = Executors.newFixedThreadPool(THREAD_COUNT); // Define o número fixo de 5 threads
    }
    
    
    //divide o arquivo em blocos
    private void initializeDownloadTasks(File file) {
    	 long fileSize = file.length();
         int offset = 0;

         while (offset < fileSize) {
             int length = (int) Math.min(BLOCK_SIZE, fileSize - offset); // Define o tamanho do bloco
             FileBlockRequestMessage request = new FileBlockRequestMessage(
                 file.getName(), offset, length
             );
             fileBlockRequests.add(request);
             offset += length;
         }
     }
    //inicia o download
    public void startDownload() {
        for (FileBlockRequestMessage request : fileBlockRequests) {
        	executorService.submit(() -> downloadBlock(request));
        }
        executorService.shutdown();
    }
    
    // Faz o download de um bloco a partir de um nó
    private void downloadBlock(FileBlockRequestMessage request) {
        for (String node : nodes) {
            String[] addressParts = node.split(":");
            String host = addressParts[0];
            int port = Integer.parseInt(addressParts[1]);

            try (Socket socket = new Socket(host, port);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                out.writeObject(request);

                FileBlockAnswerMessage response = (FileBlockAnswerMessage) in.readObject();
                handleDownloadResponse(response);

                // Sucesso no download, sair do loop
                break;

            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Erro ao baixar o bloco: " + e.getMessage());
            }
        }
    }

    // Processa a resposta do download, salvando o bloco baixado
    private void handleDownloadResponse(FileBlockAnswerMessage response) {
        try (FileOutputStream fos = new FileOutputStream("downloaded_" + response.getFileHash(), true)) {
            fos.write(response.getData(), 0, response.getData().length);
        } catch (IOException e) {
            System.err.println("Erro ao salvar o bloco: " + e.getMessage());
        }
    }

    

    public List<FileBlockRequestMessage> getFileBlockRequests() {
        return fileBlockRequests;
    }



  
}
