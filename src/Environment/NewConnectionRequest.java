package Environment;

public class NewConnectionRequest {
	
	//As liga¸c˜oes com outros n´os podem ser estabelecidas ativamente,
	//atrav´es do envio de um pedido de liga¸c˜ao, atrav´es do envio de uma mensagem da classe NewConnectionRequest, ou passivamente, quando se recebe
	//um desses pedidos. Logo que estabelecida esta liga¸c˜ao, deixa de ser relevante quem efetuou e quem recebeu o pedido, pois a comunica¸c˜ao poder´a ser
	//iniciada por qualquer um dos n´os.
	 private String nodeAddress;  // Endereço IP do nó
	    private int nodePort;        // Porta do nó

	    public NewConnectionRequest(String nodeAddress, int nodePort) {
	        this.nodeAddress = nodeAddress;
	        this.nodePort = nodePort;
	    }

	    public String getNodeAddress() {
	        return nodeAddress;
	    }

	    public int getNodePort() {
	        return nodePort;
	    }

	    @Override
	    public String toString() {
	        return "NewConnectionRequest{" +
	                "nodeAddress='" + nodeAddress + '\'' +
	                ", nodePort=" + nodePort +
	                '}';
	    }

}
