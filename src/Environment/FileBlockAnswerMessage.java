package Environment;

import java.io.Serializable;
import java.util.List;


public class FileBlockAnswerMessage {
	  	private String fileHash;
	    private int offset;
	    private byte[] data;

	    public FileBlockAnswerMessage(String fileHash, int offset, byte[] data) {
	        this.fileHash = fileHash;
	        this.offset = offset;
	        this.data = data;
	    }

	    public String getFileHash() {
	        return fileHash;
	    }

	    public int getOffset() {
	        return offset;
	    }

	    public byte[] getData() {
	        return data;
	    }
	}