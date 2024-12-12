package Environment;

import java.io.Serializable;

//Representa o pedido de procura a partir de uma palavra
//Como atributos a palavra a ser procurada, e o endereço e porto do nó que solicita 

public class WordSearchMessage implements Serializable {
    private String keyword;
   

    public WordSearchMessage(String keyword) {
        this.keyword = keyword;
  
    }

    public String getKeyword() {
        return keyword;
    }

}
